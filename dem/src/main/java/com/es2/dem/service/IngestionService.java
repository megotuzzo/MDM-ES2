package com.es2.dem.service;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.es2.dem.dto.CountryDTO;
import com.es2.dem.dto.CurrencyDTO;
import com.es2.dem.dto.IngestionDTO;
import com.es2.dem.dto.IngestionRequestDTO;
import com.es2.dem.dto.MdmProviderResponseDTO;
import com.es2.dem.enums.IngestionStatus;
import com.es2.dem.model.Ingestion;
import com.es2.dem.repository.IngestionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;

@Service
public class IngestionService {

// Classe de serviço para gerenciar solicitações de ingestão de dados no DEM.
// Ela lida com a criação de novas solicitações de ingestão, inicia o processo de extração de dados de provedores externos,
// transforma os dados brutos em um formato adequado e salva os resultados em arquivos.
// A classe também se comunica com o MDM para obter detalhes do provedor e enviar dados transformados.
// Ela utiliza o IngestionRepository para acessar o banco de dados e o RestTemplate para fazer chamadas HTTP para APIs externas.
// A classe é anotada com @Service para indicar que é um componente de serviço Spring.

    private static final Logger logger = LoggerFactory.getLogger(IngestionService.class);           // Logger para registrar informações e erros
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;             // Formato de data e hora para timestamps
    private static final DateTimeFormatter fileTimestampFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");  // Formato de data e hora para nomes de arquivos

    private final IngestionRepository ingestionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // ObjectMapper para manipulação de JSON

    @Value("${mdm.api.base-url}")   //URL base da API do MDM, injetada a partir do application.properties
    private String mdmApiBaseUrl;

    @Value("${dem.storage.base-path}")   // Caminho base para armazenamento de dados no DEM, injetado a partir do application.properties
    private String demStorageBasePath; 

    @Autowired
    public IngestionService(IngestionRepository ingestionRepository, RestTemplate restTemplate, ObjectMapper objectMapper) { 
        this.ingestionRepository = ingestionRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }


    // Método para criar uma nova solicitação de ingestão de dados.
    // Ele recebe um IngestionRequestDTO, valida os dados e inicia o processo de extração.
    // Ele salva a solicitação no banco de dados e inicia o processo de extração de forma assíncrona.
    // Retorna um IngestionDTO representando a solicitação criada.
    @Transactional
    public IngestionDTO createIngestionRequest(IngestionRequestDTO requestDTO) {
        logger.info("Recebida solicitação de ingestão para mdmProviderId: {} e mdmSyncUrl: {}",
                requestDTO.getMdmProviderId(), requestDTO.getMdmSyncUrl());

        // Criar um novo registro de ingestão
        Ingestion newIngestion = new Ingestion();
        newIngestion.setMdmProviderId(requestDTO.getMdmProviderId());
        newIngestion.setMdmSyncUrl(requestDTO.getMdmSyncUrl());
        newIngestion.setStatus(IngestionStatus.PENDING);
        newIngestion.setStatusMessage("Solicitação de ingestão recebida.");

        // Salvar o registro de ingestão no banco de dados
        Ingestion savedIngestion = ingestionRepository.save(newIngestion);
        logger.info("Registro de ingestão criado com ID: {}", savedIngestion.getId());

        // Iniciar o processo de extração de forma assíncrona
        startExtractionProcess(savedIngestion.getId());

        return convertToDTO(savedIngestion);
    }

    // Método assíncrono para iniciar o processo de extração de dados.
    // Ele busca os detalhes do provedor no MDM, extrai dados da fonte externa,
    // salva os dados brutos em um arquivo e chama o método de transformação para processar os dados.
    // Ele atualiza o status da ingestão conforme o progresso do processo.
    @Async 
    // Async para evitar o longo tempo de espera do MDM (pode ocasionar timeouts) e
    // evitar o bloqueio do thread principal do servidor.
    public void startExtractionProcess(Integer ingestionId) {
        // Verifica se a ID de ingestão é válida
        Ingestion ingestion = ingestionRepository.findById(ingestionId).orElse(null);
        if (ingestion == null) {
            logger.error("Ingestion ID {} não encontrado para iniciar extração.", ingestionId);
            return;
        }

        ingestion.setStatusMessage("Iniciando processo de extração: buscando detalhes do provedor.");
        ingestionRepository.save(ingestion);

        // Variáveis para armazenar a URL da API do provedor e o nome do provedor para o caminho do arquivo
        String providerApiUrl = null;
        String providerNameForPath = "provider_" + ingestion.getMdmProviderId();

        try {
            // Obter Detalhes do Provedor do MDM 
            String urlToCallMdm = mdmApiBaseUrl + "/providers/" + ingestion.getMdmProviderId();
            logger.info("Chamando MDM: {}", urlToCallMdm);
            // Faz a chamada para a API do MDM para obter detalhes do provedor
            // A resposta deve ser do tipo MdmProviderResponseDTO, que contém a URL da API do provedor
            ResponseEntity<MdmProviderResponseDTO> mdmResponse = restTemplate.getForEntity(urlToCallMdm, MdmProviderResponseDTO.class);

            if (!mdmResponse.getStatusCode().is2xxSuccessful() || mdmResponse.getBody() == null) {
                throw new RestClientException("Falha ao obter detalhes do provedor do MDM: " + mdmResponse.getStatusCode());
            }
            
            // Extrai a URL da API do provedor e o nome para o caminho do arquivo
            providerApiUrl = mdmResponse.getBody().getApiUrl();
            if (mdmResponse.getBody().getName() != null && !mdmResponse.getBody().getName().isBlank()) {
                providerNameForPath = mdmResponse.getBody().getName().replaceAll("\\s+", "_").toLowerCase();
            }
            if (providerApiUrl == null || providerApiUrl.isBlank()) {
                throw new RuntimeException("API URL do provedor não fornecida pelo MDM.");
            }
            // Atualiza o status da ingestão com a mensagem de sucesso
            ingestion.setStatusMessage("Detalhes do provedor obtidos. Buscando dados da fonte externa...");
            ingestionRepository.save(ingestion);

            //Extrair Dados da Fonte Externa:
            logger.info("Buscando dados de: {}", providerApiUrl);
            // Faz a chamada para a API externa para obter os dados brutos
            // A resposta deve ser uma lista de países em formato JSON
            ResponseEntity<String> externalApiResponse = restTemplate.getForEntity(providerApiUrl + "/all", String.class);

            if (!externalApiResponse.getStatusCode().is2xxSuccessful() || externalApiResponse.getBody() == null) {
                throw new RestClientException("Falha ao buscar dados da fonte externa: " + externalApiResponse.getStatusCode());
            }

            // Atualiza o status da ingestão com a mensagem de sucesso
            String rawData = externalApiResponse.getBody();
            ingestion.setStatusMessage("Dados brutos obtidos. Salvando...");
            ingestionRepository.save(ingestion);

            // Salvar Dados Brutos em Arquivo:
            String timestamp = LocalDateTime.now().format(fileTimestampFormatter);
            // Cria o diretório para armazenar os dados brutos, se não existir
            Path rawDataDirectory = Paths.get(demStorageBasePath, "raw", providerNameForPath);
            Files.createDirectories(rawDataDirectory);

            // Define o nome do arquivo com base no ID de ingestão e timestamp
            String rawFileName = "raw_ingestion_" + ingestionId + "_" + timestamp + ".json";
            Path rawFilePath = rawDataDirectory.resolve(rawFileName);
            Files.writeString(rawFilePath, rawData);
            logger.info("Dados brutos salvos em: {}", rawFilePath.toString());

            // Atualiza o registro de ingestão com o caminho dos dados brutos e mensagem de status
            ingestion.setRawDataPath(rawFilePath.toString());
            ingestion.setStatusMessage("Dados brutos salvos. Iniciando transformação.");
            ingestionRepository.save(ingestion);

            // Chama o método para transformar os dados brutos e salvá-los
            transformAndSaveData(ingestion, providerNameForPath);

        } catch (Exception e) { 
            logger.error("Falha no processo de extração para Ingestion ID {}: ", ingestionId, e);
            if (ingestion != null) {
                ingestion.setStatus(IngestionStatus.FAILED);
                ingestion.setStatusMessage("Falha na extração: " + e.getMessage());
                ingestionRepository.save(ingestion);
            }
        }
    }


    // Transforma os dados brutos obtidos da fonte externa em um formato adequado
    // e salva os dados transformados em um arquivo.
    // Ele atualiza o status da ingestão para READY após a transformação.
    // O método também lida com erros de I/O e outros erros durante o processo de transformação.
    private void transformAndSaveData(Ingestion ingestion, String providerNameForPath) {
        logger.info("Iniciando transformação para Ingestion ID: {}. Lendo de: {}", ingestion.getId(), ingestion.getRawDataPath());
        ingestion.setStatusMessage("Transformando dados..."); 
        ingestionRepository.save(ingestion);

        List<CountryDTO> transformedCountriesList = null; 

        try {   
            // Usa o ObjectMapper para ler o conteúdo JSON do arquivo
            String rawJsonContent = Files.readString(Paths.get(ingestion.getRawDataPath()));

            // Converte o conteúdo JSON em uma lista de mapas (cada mapa representa um país)
            // Usando TypeReference para mapear o JSON para uma lista de mapas
            // A lista de mapas contém os dados brutos dos países, que serão transformados em CountryDTO
            List<Map<String, Object>> rawCountriesData = objectMapper.readValue(rawJsonContent,
                    new TypeReference<List<Map<String, Object>>>() {});

            // Transformar Dados Brutos em CountryDTO
            List<CountryDTO> transformedCountries = new ArrayList<>();
            for (Map<String, Object> rawCountryMap : rawCountriesData) {
                JsonNode rawCountryNode = objectMapper.convertValue(rawCountryMap, JsonNode.class);
                CountryDTO mdmCountryDTO = new CountryDTO(); 

                // Preencher os campos do CountryDTO com os dados do rawCountryNode
                if (rawCountryNode.hasNonNull("name") && rawCountryNode.get("name").hasNonNull("common")) {
                    mdmCountryDTO.setCountryName(rawCountryNode.get("name").get("common").asText());
                }
                if (rawCountryNode.hasNonNull("ccn3")) {
                    try {
                        // Tenta converter o código numérico para Integer
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
                    List<CurrencyDTO> currencyDTOs = new ArrayList<>();
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
                transformedCountriesList.add(mdmCountryDTO);
            }
            logger.info("{} países transformados para Ingestion ID: {}", transformedCountries.size(), ingestion.getId());

            // Salvar dados transformados em arquivo com nome baseado no ID de ingestão e timestamp
            String timestamp = LocalDateTime.now().format(fileTimestampFormatter);
            Path transformedDataDirectory = Paths.get(demStorageBasePath, "transformed", providerNameForPath);
            Files.createDirectories(transformedDataDirectory);
            String transformedFileName = "transformed_ingestion_" + ingestion.getId() + "_" + timestamp + ".json";
            Path transformedFilePath = transformedDataDirectory.resolve(transformedFileName);

            // Escreve a lista de CountryDTO transformados no arquivo JSON
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(transformedFilePath.toFile(), transformedCountries);
            logger.info("Dados transformados salvos em: {}", transformedFilePath.toString());

            // Atualiza o registro de ingestão com o caminho dos dados transformados e status
            ingestion.setTransformedDataPath(transformedFilePath.toString());
            ingestion.setStatus(IngestionStatus.READY);
            ingestion.setStatusMessage("Dados transformados e prontos para envio ao MDM.");

        } catch (IOException e) { // Erros de I/O ao ler ou salvar arquivos
            logger.error("Erro de I/O durante a transformação para Ingestion ID {}: ", ingestion.getId(), e);
            ingestion.setStatus(IngestionStatus.FAILED);
            ingestion.setStatusMessage("Erro ao ler/salvar arquivo durante transformação: " + e.getMessage());
        } catch (Exception e) { // Erros durante a transformação
            logger.error("Erro inesperado durante a transformação para Ingestion ID {}: ", ingestion.getId(), e);
            ingestion.setStatus(IngestionStatus.FAILED);
            ingestion.setStatusMessage("Erro inesperado na transformação: " + e.getMessage());
        }
        ingestionRepository.save(ingestion);

        // Se a transformação foi bem-sucedida, chama o método para enviar os dados ao MDM
        // O envio só deve ocorrer se a transformação resultou em uma lista não vazia de CountryDTO
        if (ingestion.getStatus() == IngestionStatus.READY && transformedCountriesList != null && !transformedCountriesList.isEmpty()) {
            logger.info("Ingestion ID {}: Dados prontos. Tentando enviar para MDM na URL: {}", ingestion.getId(), ingestion.getMdmSyncUrl());
            ingestion.setStatus(IngestionStatus.PROCESSING); 
            ingestion.setStatusMessage("Enviando dados para o MDM...");
            ingestionRepository.save(ingestion);

            try {
                // Chamada POST para enviar os dados transformados ao MDM
                // A URL de callback do MDM é obtida do registro de ingestão
                ResponseEntity<String> mdmResponse = restTemplate.postForEntity(
                        ingestion.getMdmSyncUrl(),          // A URL de callback do MDM
                        transformedCountriesList,           // O corpo da requisição
                        String.class);         // O tipo esperado da resposta do MDM

                // Verifica se a resposta do MDM foi bem-sucedida
                if (mdmResponse.getStatusCode().is2xxSuccessful()) {
                    logger.info("MDM processou os dados com sucesso para Ingestion ID: {}. Resposta: {}", ingestion.getId(), mdmResponse.getBody());
                    ingestion.setStatus(IngestionStatus.COMPLETED);
                    ingestion.setStatusMessage("Dados enviados e processados com sucesso pelo MDM.");
                } else {
                    // Se o MDM retornou um erro, atualiza o status da ingestão para FAILED
                    logger.error("MDM retornou erro para Ingestion ID: {}. Status: {}, Body: {}",
                                 ingestion.getId(), mdmResponse.getStatusCode(), mdmResponse.getBody());
                    ingestion.setStatus(IngestionStatus.FAILED);
                    ingestion.setStatusMessage("Falha ao sincronizar com MDM: " + mdmResponse.getStatusCode() + " - " + mdmResponse.getBody());
                }
            } catch (RestClientException e) { // Erros de comunicação com o MDM
                logger.error("Erro de comunicação ao enviar dados para MDM (Ingestion ID {}): ", ingestion.getId(), e);
                ingestion.setStatus(IngestionStatus.FAILED);
                ingestion.setStatusMessage("Erro de comunicação ao enviar para MDM: " + e.getMessage());
            }
            ingestionRepository.save(ingestion); // Salva o status final (COMPLETED ou FAILED)
            
        // Se a transformação resultou em uma lista vazia, atualiza o status da ingestão para FAILED
        } else if (ingestion.getStatus() == IngestionStatus.READY && (transformedCountriesList == null || transformedCountriesList.isEmpty())) {
            logger.warn("Ingestion ID {}: Nenhum dado transformado para enviar ao MDM.", ingestion.getId());
            ingestion.setStatus(IngestionStatus.FAILED); 
            ingestion.setStatusMessage("Transformação resultou em lista vazia, nada a enviar.");
            ingestionRepository.save(ingestion);
        }
    }

    // Métodos padrões para obter informações e conversão de entidades
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