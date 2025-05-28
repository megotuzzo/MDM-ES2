package com.es2.dem.controller;

import com.es2.dem.dto.IngestionDTO;
import com.es2.dem.dto.IngestionRequestDTO;
import com.es2.dem.service.IngestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dem/api/ingestion") 
public class IngestionController {

    private final IngestionService ingestionService;

    @Autowired
    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Endpoint para o MDM solicitar uma nova ingestão de dados.
     */
    @PostMapping
    public ResponseEntity<IngestionDTO> requestIngestion(@Valid @RequestBody IngestionRequestDTO requestDTO) {
        IngestionDTO createdIngestionJob = ingestionService.createIngestionRequest(requestDTO);
        // Retorna o status 202 Accepted, indicando que o trabalho foi aceito para processamento
        return new ResponseEntity<>(createdIngestionJob, HttpStatus.ACCEPTED);
    }

    /**
     * Endpoint para o MDM consultar o status de um trabalho de ingestão específico.
     */
    @GetMapping("/{id}")
    public ResponseEntity<IngestionDTO> getIngestionStatus(@PathVariable Integer id) {
        IngestionDTO ingestionJob = ingestionService.getIngestionById(id);
        return ResponseEntity.ok(ingestionJob);
    }

    /**
     * Endpoint para o MDM listar todos os trabalhos de ingestão.
     */
    @GetMapping
    public ResponseEntity<List<IngestionDTO>> getAllIngestions() {
        List<IngestionDTO> ingestionJobs = ingestionService.getAllIngestions();
        return ResponseEntity.ok(ingestionJobs);
    }
}