package com.es2.mdm.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.es2.mdm.dto.DemIngestionResponseDTO;
import com.es2.mdm.service.DemIntegrationService;

@RestController
@RequestMapping("/mdm/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final DemIntegrationService demIntegrationService;
    
    @Autowired
    public AdminController(DemIntegrationService demIntegrationService) {
        this.demIntegrationService = demIntegrationService;
    }

     
    //  Endpoint para ativar o processo de ingestão de dados no DEM com um provedor específico.
    //  Recebe o ID do provedor MDM e inicia a ingestão de dados no DEM.
    //  Retorna uma resposta com o status da ingestão e o ID do job no DEM.
    @PostMapping("/ingest/provider/{providerId}")
    public ResponseEntity<?> startDemIngestion(@PathVariable Integer providerId) {
        logger.info("Recebida solicitação administrativa para ativar ingestão para o providerId: {}", providerId);
        try {
            // Chama o serviço de integração com o DEM para iniciar a ingestão de dados
            // O serviço retorna um objeto DemIngestionResponseDTO com o status da ingestão e o ID do job no DEM.
            DemIngestionResponseDTO demResponse = demIntegrationService.requestDataIngestion(providerId);
            if (demResponse != null) {
                logger.info("Ingestão disparada no DEM com sucesso. Job ID do DEM: {}", demResponse.getId());
                return ResponseEntity.ok(demResponse);

            } else { // Se a resposta do serviço for nula, significa que houve um erro
                logger.warn("DemIntegrationService retornou nulo para providerId: {}", providerId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .body("Falha ao ativar ingestão no DEM ou DEM não retornou uma resposta válida.");
            }
        } catch (Exception e) { 
            logger.error("Erro ao tentar ativar ingestão no DEM para providerId: " + providerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Erro interno no MDM ao tentar contatar o DEM: " + e.getMessage());
        }
    }

    // Este endpoint é usado para consultar o progresso e o resultado de uma ingestão de dados iniciada anteriormente.
    // Ele recebe o ID do job de ingestão no DEM e retorna o status atual, incluindo mensagens de erro ou sucesso.
    @GetMapping("/ingest/status/{demJobId}")
    public ResponseEntity<?> getDemIngestionStatus(@PathVariable Integer demJobId) {
        logger.info("Recebida solicitação administrativa para verificar status do job {} no DEM", demJobId);
        try {
            // Chama o serviço de integração com o DEM para obter o status do job de ingestão
            // O serviço retorna um objeto DemIngestionResponseDTO com o status atual da ingestão.
            DemIngestionResponseDTO demResponse = demIntegrationService.getIngestionStatus(demJobId);
            if (demResponse != null) {
                return ResponseEntity.ok(demResponse);
            } else {
                // Se a resposta do serviço for nula, significa que houve um erro na consulta
                logger.warn("DemIntegrationService retornou nulo para a consulta de status do job: {}", demJobId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND) 
                                     .body("Job de ingestão não encontrado no DEM ou falha na comunicação.");
            }
        } catch (Exception e) {
            logger.error("Erro ao tentar verificar status do job {} no DEM: ", demJobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Erro interno no MDM ao tentar consultar status no DEM: " + e.getMessage());
        }
    }

   
    // Este endpoint é usado para consultar o status de todas ingestões no DEM.
    // Ele retorna uma lista de objetos DemIngestionResponseDTO, cada um representando o status de um job de ingestão.
    @GetMapping("/ingest/status/all")
    public ResponseEntity<?> getAllDemIngestionStatuses() {
        logger.info("Recebida solicitação administrativa para verificar status de todos os jobs no DEM");
        try {
            List<DemIngestionResponseDTO> demResponses = demIntegrationService.getAllDemIngestionJobs();
            // A lista pode estar vazia se não houver jobs ou se houver um erro tratado no serviço que retorna lista vazia.
            // Se demIntegrationService puder lançar exceções, o GlobalExceptionHandler do MDM pode pegá-las.
            return ResponseEntity.ok(demResponses);
        } catch (Exception e) { // Captura exceções que podem vir do DemIntegrationService (se ele as lançar)
            logger.error("Erro ao tentar verificar status de todos os jobs no DEM: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Erro interno no MDM ao tentar consultar todos os status no DEM: " + e.getMessage());
        }
    }
}


