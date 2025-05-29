package com.es2.mdm.service;

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

//Lógica de chamadas HTTP para o DEM
@Service
public class DemIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(DemIntegrationService.class);
    private final RestTemplate restTemplate;

    @Value("${dem.api.base-url}")
    private String demApiBaseUrl;

    @Value("${mdm.callback.base-url}")
    private String mdmCallbackBaseUrl; 

    //endpoint de callback do MDM para o DEM
    private final String mdmCallbackPath = "/countries/callback"; 

    @Autowired
    public DemIntegrationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public DemIngestionResponseDTO requestDataIngestion(Integer mdmProviderId) {
        String demIngestionEndpoint = demApiBaseUrl + "/ingestion";
        String fullMdmSyncUrl = mdmCallbackBaseUrl + mdmCallbackPath; //junta a URL

        DemIngestionRequestDTO requestToDem = new DemIngestionRequestDTO(mdmProviderId, fullMdmSyncUrl);
        logger.info("Solicitando ingestão ao DEM para providerId: {} com syncUrl: {}", mdmProviderId, fullMdmSyncUrl);

        try {
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

    public DemIngestionResponseDTO getIngestionStatus(Integer demJobId) {
        String demStatusEndpoint = demApiBaseUrl + "/ingestion/" + demJobId;
        logger.info("Consultando status da ingestão {} no DEM", demJobId);

        try {
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
}