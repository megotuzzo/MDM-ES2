package com.es2.mdm.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.es2.mdm.dto.DemIngestionRequestDTO;
import com.es2.mdm.dto.DemIngestionResponseDTO;

@Service    // O serviço é anotado com @Service para ser gerenciado pelo Spring e injetado onde necessário.
public class DemIntegrationService {
 
// Serviço para integração com o DEM.
// Ele lida com solicitações de ingestão de dados e consulta de status de ingestão.
// Ele utiliza o RestTemplate para fazer chamadas HTTP para a API do DEM.
// O serviço é configurado com as URLs base do DEM e do MDM, que são injetadas.
// Ele também registra informações de log para rastrear o fluxo de dados e possíveis erros.
    private static final Logger logger = LoggerFactory.getLogger(DemIntegrationService.class); // Logger para registrar informações e erros
    private final RestTemplate restTemplate;    // RestTemplate para fazer chamadas HTTP


    @Value("${dem.api.base-url}") // URL base da API do DEM, injetada a partir do application.properties
    private String demApiBaseUrl;

    @Value("${mdm.callback.base-url}")  // URL base do MDM para callbacks, injetada a partir do application.properties
    private String mdmCallbackBaseUrl; 

    // Caminho do endpoint de callback do MDM para o DEM, onde o DEM enviará os dados processados.
    private final String mdmCallbackPath = "/countries/callback";   

    @Autowired
    public DemIntegrationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Solicita a ingestão de dados ao DEM.
    // Envia uma solicitação POST para o endpoint de ingestão do DEM com o ID do provedor MDM e a URL de sincronização do MDM.
    // mdmProviderId: ID do provedor MDM do qual os dados serão ingeridos.
    // Retorna: Resposta do DEM contendo o status da ingestão e outros detalhes.
    public DemIngestionResponseDTO requestDataIngestion(Integer mdmProviderId) {

        // Constrói a URL de ingestão do DEM e a URL completa de callback do MDM
        String demIngestionEndpoint = demApiBaseUrl + "/ingestion";
        String fullMdmSyncUrl = mdmCallbackBaseUrl + mdmCallbackPath;

        // Cria o objeto de solicitação para o DEM com o ID do provedor MDM e a URL de sincronização do MDM
        // O objeto DemIngestionRequestDTO encapsula os dados necessários para a solicitação de ingestão.
        DemIngestionRequestDTO requestToDem = new DemIngestionRequestDTO(mdmProviderId, fullMdmSyncUrl);
        logger.info("Solicitando ingestão ao DEM para providerId: {} com syncUrl: {}", mdmProviderId, fullMdmSyncUrl);

        try {
            // Envia a solicitação POST para o endpoint de ingestão do DEM
            // O RestTemplate faz a chamada HTTP e espera uma resposta do tipo DemIngestionResponseDTO.
            // A resposta contém informações sobre o status da ingestão e o ID do trabalho de ingestão no DEM.
            ResponseEntity<DemIngestionResponseDTO> response = restTemplate.postForEntity(
                    demIngestionEndpoint,
                    requestToDem,
                    DemIngestionResponseDTO.class);

            if (response.getStatusCode().is2xxSuccessful()) { //Verifica se é um status na casa 200 -> bem-sucedido
                logger.info("Solicitação de ingestão aceita pelo DEM: {}", response.getBody());
                return response.getBody();
            } else {
                logger.error("Falha ao solicitar ingestão ao DEM. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                return null;
            }
        } catch (RestClientException e) {
            logger.error("Erro de comunicação ao solicitar ingestão ao DEM para providerId: {}", mdmProviderId, e);
            return null;
        }
    }

    // Consulta o status de uma ingestão específica no DEM.
    // demJobId: ID do trabalho de ingestão no DEM para o qual o status será consultado.
    // Retorna: Resposta do DEM contendo o status da ingestão e outros detalhes.
    // O método é usado para verificar o progresso e o resultado de uma ingestão de dados iniciada anteriormente.
    public DemIngestionResponseDTO getIngestionStatus(Integer demJobId) {

        // Constrói a URL do endpoint de status de ingestão do DEM usando o ID do trabalho de ingestão
        String demStatusEndpoint = demApiBaseUrl + "/ingestion/" + demJobId;
        logger.info("Consultando status da ingestão {} no DEM", demJobId);

        try {
            // Envia uma solicitação GET para o endpoint de status de ingestão do DEM
            // O RestTemplate faz a chamada HTTP e espera uma resposta do tipo DemIngestionResponseDTO.
            // A resposta contém informações sobre o status da ingestão, como ID, status, caminhos dos dados e mensagens de status.
            ResponseEntity<DemIngestionResponseDTO> response = restTemplate.getForEntity(
                    demStatusEndpoint,
                    DemIngestionResponseDTO.class);

            if (response.getStatusCode().is2xxSuccessful()) {   //Verifica se é um status na casa 200 -> bem-sucedido
                logger.info("Status recebido do DEM para job {}: {}", demJobId, response.getBody());
                return response.getBody();
            } else {
                logger.error("Falha ao consultar status no DEM para job {}. Status: {}, Body: {}", demJobId, response.getStatusCode(), response.getBody());
                return null;
            }
        } catch (RestClientException e) {
            logger.error("Erro de comunicação ao consultar status no DEM para job {}: {}", demJobId, e);
            return null;
        }
    }


    // Método que consulta todos os trabalhos de ingestão no DEM.
    // Ele envia uma solicitação GET para o endpoint do DEM que lista todos os trabalhos de ingestão.
    // Retorna: Uma lista de DemIngestionResponseDTO contendo os detalhes e status dos trabalhos de ingestão.
    // Se a consulta falhar, retorna uma lista vazia ou pode lançar uma exceção personalizada.
    public List<DemIngestionResponseDTO> getAllDemIngestionJobs() {
        String demGetAllIngestionsUrl = demApiBaseUrl + "/ingestion"; // Endpoint do DEM que lista todos
        logger.info("Consultando todos os jobs de ingestão no DEM: {}", demGetAllIngestionsUrl);

        try {
            // O endpoint do DEM retorna uma List<IngestionDTO>.
            // Com RestTemplate, uma forma comum de obter uma lista é deserializar para um array e depois converter.
            ResponseEntity<DemIngestionResponseDTO[]> response = restTemplate.getForEntity(
                    demGetAllIngestionsUrl,
                    DemIngestionResponseDTO[].class); // Espera um array de DemIngestionResponseDTO

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Recebidos {} jobs de ingestão do DEM.", response.getBody().length);
                return Arrays.asList(response.getBody());
            } else {
                logger.error("Falha ao consultar todos os jobs de ingestão no DEM. Status: {}, Body: {}",
                             response.getStatusCode(), response.getBody());
                return Collections.emptyList(); // Lançar exceção
            }
        } catch (RestClientException e) {
            logger.error("Erro de comunicação ao consultar todos os jobs de ingestão no DEM: ", e);
            return Collections.emptyList(); 
        }
    }
}