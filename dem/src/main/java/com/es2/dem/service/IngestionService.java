package com.es2.dem.service;

import com.es2.dem.dto.CountryDTO;
import com.es2.dem.dto.CurrencyDTO;
import com.es2.dem.dto.IngestionDTO;
import com.es2.dem.dto.IngestionRequestDTO;
import com.es2.dem.dto.MdmProviderResponseDTO;
// TODO: Adicione os imports para as classes CountryDTO e CurrencyDTO do MDM (ou suas cópias no DEM)
// Exemplo: import com.es2.dem.dto.mdm.CountryDTO;
// Exemplo: import com.es2.dem.dto.mdm.CurrencyDTO;
import com.es2.dem.enums.IngestionStatus;
import com.es2.dem.model.Ingestion;
import com.es2.dem.repository.IngestionRepository;

import com.fasterxml.jackson.core.type.TypeReference; // Para parsear Listas com Jackson
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;   // Importe o ObjectMapper

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IngestionService {

    private static final Logger logger = LoggerFactory.getLogger(IngestionService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter fileTimestampFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final IngestionRepository ingestionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // ObjectMapper para manipulação de JSON

    @Value("${mdm.api.base-url}")
    private String mdmApiBaseUrl;

    @Value("${dem.storage.base-path}")
    private String demStorageBasePath; // ex: ./data_dem (configurado no application.properties)

    @Autowired
    public IngestionService(IngestionRepository ingestionRepository,
                           RestTemplate restTemplate,
                           ObjectMapper objectMapper) { // Injete o ObjectMapper
        this.ingestionRepository = ingestionRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public IngestionDTO createIngestionRequest(IngestionRequestDTO requestDTO) {
        // ... (lógica existente para criar o Ingestion inicial) ...
        // [Copie a lógica do seu IngestionService.java fornecido anteriormente aqui]
        // A última linha deve ser a chamada para startExtractionProcess
        logger.info("Recebida solicitação de ingestão para mdmProviderId: {} e mdmSyncUrl: {}",
                requestDTO.getMdmProviderId(), requestDTO.getMdmSyncUrl());

        Ingestion newIngestion = new Ingestion();
        newIngestion.setMdmProviderId(requestDTO.getMdmProviderId());
        newIngestion.setMdmSyncUrl(requestDTO.getMdmSyncUrl());
        newIngestion.setStatus(IngestionStatus.PENDING);
        newIngestion.setStatusMessage("Solicitação de ingestão recebida.");

        Ingestion savedIngestion = ingestionRepository.save(newIngestion);
        logger.info("Registro de ingestão criado com ID: {}", savedIngestion.getId());

        // Idealmente, chame de forma assíncrona
        startExtractionProcess(savedIngestion.getId());

        return convertToDTO(savedIngestion);
    }

    // @Async // Considere tornar este método assíncrono
    public void startExtractionProcess(Integer ingestionId) {
        Ingestion ingestion = ingestionRepository.findById(ingestionId).orElse(null);
        if (ingestion == null) {
            logger.error("Ingestion ID {} não encontrado para iniciar extração.", ingestionId);
            return;
        }

        ingestion.setStatus(IngestionStatus.PROCESSING);
        ingestion.setStatusMessage("Iniciando processo de extração: buscando detalhes do provedor.");
        ingestionRepository.save(ingestion);

        String providerApiUrl = null;
        String providerNameForPath = "provider_" + ingestion.getMdmProviderId(); // Default

        try {
            // --- Passo 1: Buscar Detalhes do Provedor no MDM ---
            String urlToCallMdm = mdmApiBaseUrl + "/providers/" + ingestion.getMdmProviderId();
            logger.info("Chamando MDM: {}", urlToCallMdm);
            ResponseEntity<MdmProviderResponseDTO> mdmResponse = restTemplate.getForEntity(urlToCallMdm, MdmProviderResponseDTO.class);

            if (!mdmResponse.getStatusCode().is2xxSuccessful() || mdmResponse.getBody() == null) {
                throw new RestClientException("Falha ao obter detalhes do provedor do MDM: " + mdmResponse.getStatusCode());
            }
            providerApiUrl = mdmResponse.getBody().getApiUrl();
            if (mdmResponse.getBody().getName() != null && !mdmResponse.getBody().getName().isBlank()) {
                providerNameForPath = mdmResponse.getBody().getName().replaceAll("\\s+", "_").toLowerCase();
            }
            if (providerApiUrl == null || providerApiUrl.isBlank()) {
                throw new RuntimeException("API URL do provedor não fornecida pelo MDM.");
            }
            ingestion.setStatusMessage("Detalhes do provedor obtidos. Buscando dados da fonte externa...");
            ingestionRepository.save(ingestion);

            // --- Passo 2: Extrair Dados da Fonte Externa ---
            logger.info("Buscando dados de: {}", providerApiUrl);
            // Assumindo que a API restcountries sempre tem /all para todos os países
            ResponseEntity<String> externalApiResponse = restTemplate.getForEntity(providerApiUrl + "/all", String.class);
            if (!externalApiResponse.getStatusCode().is2xxSuccessful() || externalApiResponse.getBody() == null) {
                throw new RestClientException("Falha ao buscar dados da fonte externa: " + externalApiResponse.getStatusCode());
            }
            String rawData = externalApiResponse.getBody();
            ingestion.setStatusMessage("Dados brutos obtidos. Salvando...");
            ingestionRepository.save(ingestion);

            // --- Passo 3: Salvar Dados Brutos em Arquivo ---
            String timestamp = LocalDateTime.now().format(fileTimestampFormatter);
            Path rawDataDirectory = Paths.get(demStorageBasePath, "raw", providerNameForPath);
            Files.createDirectories(rawDataDirectory);
            String rawFileName = "raw_ingestion_" + ingestionId + "_" + timestamp + ".json";
            Path rawFilePath = rawDataDirectory.resolve(rawFileName);
            Files.writeString(rawFilePath, rawData);
            logger.info("Dados brutos salvos em: {}", rawFilePath.toString());

            ingestion.setRawDataPath(rawFilePath.toString());
            ingestion.setStatusMessage("Dados brutos salvos. Iniciando transformação.");
            ingestionRepository.save(ingestion);

            // --- Chamar o novo método para Transformação ---
            transformAndSaveData(ingestion, providerNameForPath);

        } catch (Exception e) { // Captura RestClientException e outras (IOException, RuntimeException)
            logger.error("Falha no processo de extração para Ingestion ID {}: ", ingestionId, e);
            if (ingestion != null) {
                ingestion.setStatus(IngestionStatus.FAILED);
                ingestion.setStatusMessage("Falha na extração: " + e.getMessage());
                ingestionRepository.save(ingestion);
            }
        }
    }

    /**
     * Novo método para transformar os dados brutos e salvá-los.
     */
    private void transformAndSaveData(Ingestion ingestion, String providerNameForPath) {
        logger.info("Iniciando transformação para Ingestion ID: {}. Lendo de: {}", ingestion.getId(), ingestion.getRawDataPath());
        ingestion.setStatusMessage("Transformando dados..."); // Mensagem intermediária
        ingestionRepository.save(ingestion);

        try {
            String rawJsonContent = Files.readString(Paths.get(ingestion.getRawDataPath()));
            List<Map<String, Object>> rawCountriesData = objectMapper.readValue(rawJsonContent,
                    new TypeReference<List<Map<String, Object>>>() {});

            List<CountryDTO> transformedCountries = new ArrayList<>(); // Use o CountryDTO do MDM (ou sua cópia no DEM)
            for (Map<String, Object> rawCountryMap : rawCountriesData) {
                JsonNode rawCountryNode = objectMapper.convertValue(rawCountryMap, JsonNode.class);
                CountryDTO mdmCountryDTO = new CountryDTO(); // DTO do MDM

                if (rawCountryNode.hasNonNull("name") && rawCountryNode.get("name").hasNonNull("common")) {
                    mdmCountryDTO.setCountryName(rawCountryNode.get("name").get("common").asText());
                }
                if (rawCountryNode.hasNonNull("ccn3")) {
                    try {
                        mdmCountryDTO.setNumericCode(Integer.parseInt(rawCountryNode.get("ccn3").asText()));
                    } catch (NumberFormatException e) {
                        logger.warn("Skipping numericCode for country '{}': ccn3 '{}' is not a valid integer.",
                                mdmCountryDTO.getCountryName(), rawCountryNode.get("ccn3").asText());
                    }
                }
                if (rawCountryNode.hasNonNull("capital") && rawCountryNode.get("capital").isArray() && !rawCountryNode.get("capital").isEmpty()) {
                    mdmCountryDTO.setCapitalCity(rawCountryNode.get("capital").get(0).asText());
                }
                if (rawCountryNode.hasNonNull("population")) {
                    mdmCountryDTO.setPopulation(rawCountryNode.get("population").asInt());
                }
                if (rawCountryNode.hasNonNull("area")) {
                    mdmCountryDTO.setArea(rawCountryNode.get("area").floatValue());
                }

                if (rawCountryNode.hasNonNull("currencies") && rawCountryNode.get("currencies").isObject()) {
                    List<CurrencyDTO> currencyDTOs = new ArrayList<>(); // Use o CurrencyDTO do MDM
                    Iterator<Map.Entry<String, JsonNode>> currenciesIterator = rawCountryNode.get("currencies").fields();
                    while (currenciesIterator.hasNext()) {
                        Map.Entry<String, JsonNode> entry = currenciesIterator.next();
                        CurrencyDTO currencyDTO = new CurrencyDTO();
                        currencyDTO.setCurrencyCode(entry.getKey());
                        JsonNode currencyDetails = entry.getValue();
                        if (currencyDetails.hasNonNull("name")) currencyDTO.setCurrencyName(currencyDetails.get("name").asText());
                        if (currencyDetails.hasNonNull("symbol")) currencyDTO.setCurrencySymbol(currencyDetails.get("symbol").asText());
                        currencyDTOs.add(currencyDTO);
                    }
                    mdmCountryDTO.setCurrencies(currencyDTOs);
                }
                transformedCountries.add(mdmCountryDTO);
            }
            logger.info("{} países transformados para Ingestion ID: {}", transformedCountries.size(), ingestion.getId());

            // Salvar dados transformados
            String timestamp = LocalDateTime.now().format(fileTimestampFormatter);
            Path transformedDataDirectory = Paths.get(demStorageBasePath, "transformed", providerNameForPath);
            Files.createDirectories(transformedDataDirectory);
            String transformedFileName = "transformed_ingestion_" + ingestion.getId() + "_" + timestamp + ".json";
            Path transformedFilePath = transformedDataDirectory.resolve(transformedFileName);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(transformedFilePath.toFile(), transformedCountries);
            logger.info("Dados transformados salvos em: {}", transformedFilePath.toString());

            ingestion.setTransformedDataPath(transformedFilePath.toString());
            ingestion.setStatus(IngestionStatus.READY); // Status READY (PRONTO_PARA_MDM)
            ingestion.setStatusMessage("Dados transformados e prontos para envio ao MDM.");

        } catch (IOException e) {
            logger.error("Erro de I/O durante a transformação para Ingestion ID {}: ", ingestion.getId(), e);
            ingestion.setStatus(IngestionStatus.FAILED);
            ingestion.setStatusMessage("Erro ao ler/salvar arquivo durante transformação: " + e.getMessage());
        } catch (Exception e) { // Outros erros (ex: parsing JSON, mapeamento)
            logger.error("Erro inesperado durante a transformação para Ingestion ID {}: ", ingestion.getId(), e);
            ingestion.setStatus(IngestionStatus.FAILED);
            ingestion.setStatusMessage("Erro inesperado na transformação: " + e.getMessage());
        }
        ingestionRepository.save(ingestion);

        // O próximo passo aqui seria chamar o mdmSyncUrl se o status for READY
        if (ingestion.getStatus() == IngestionStatus.READY) {
            // TODO: Implementar a chamada para o MDM (próximo passo)
            logger.info("Ingestion ID {}: Dados prontos. Próximo passo: enviar para MDM.", ingestion.getId());
        }
    }

    // --- Métodos de Consulta e Conversão (como antes) ---
    @Transactional(readOnly = true)
    public IngestionDTO getIngestionById(Integer id) {
        Ingestion ingestion = ingestionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ingestion job not found with id: " + id));
        return convertToDTO(ingestion);
    }

    @Transactional(readOnly = true)
    public List<IngestionDTO> getAllIngestions() {
        return ingestionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private IngestionDTO convertToDTO(Ingestion ingestion) {
        IngestionDTO dto = new IngestionDTO();
        dto.setId(ingestion.getId());
        dto.setMdmProviderId(ingestion.getMdmProviderId());
        if (ingestion.getStatus() != null) {
            dto.setStatus(ingestion.getStatus().name()); 
        }
        dto.setRawDataPath(ingestion.getRawDataPath());
        dto.setTransformedDataPath(ingestion.getTransformedDataPath());
        dto.setStatusMessage(ingestion.getStatusMessage());
        if (ingestion.getCreatedAt() != null) {
            dto.setCreatedAt(ingestion.getCreatedAt().format(formatter));
        }
        if (ingestion.getUpdatedAt() != null) {
            dto.setUpdatedAt(ingestion.getUpdatedAt().format(formatter));
        }
        return dto;
    }

    private Ingestion convertToEntity(IngestionDTO dto) {
        Ingestion ingestion = new Ingestion();
        ingestion.setMdmProviderId(dto.getMdmProviderId());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                ingestion.setStatus(IngestionStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                logger.warn("Status inválido '{}' no DTO para Ingestion ID {}. Status não será definido pelo DTO.", dto.getStatus(), dto.getId());
            }
        }
        ingestion.setRawDataPath(dto.getRawDataPath());
        ingestion.setTransformedDataPath(dto.getTransformedDataPath());
        ingestion.setStatusMessage(dto.getStatusMessage());
        return ingestion;
    }
}