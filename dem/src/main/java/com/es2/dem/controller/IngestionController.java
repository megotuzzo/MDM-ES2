package com.es2.dem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.es2.dem.dto.IngestionDTO;
import com.es2.dem.dto.IngestionRequestDTO;
import com.es2.dem.service.IngestionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/dem/api/ingestion") 
public class IngestionController {

// Controlador REST para gerenciar trabalhos de ingestão de dados no DEM.)
// Ele fornece endpoints para solicitar uma nova ingestão, consultar o status de um trabalho de ingestão específico e listar todos os trabalhos de ingestão.
// Ele utiliza o IngestionService para realizar as operações de negócios relacionadas à ingestão de dados.
// As operações são realizadas através de requisições HTTP, onde cada método corresponde a um endpoint específico.

    private final IngestionService ingestionService;

    @Autowired
    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Solicita uma nova ingestão de dados.
     * Body: IngestionRequestDTO
     */
    @PostMapping
    public ResponseEntity<IngestionDTO> requestIngestion(@Valid @RequestBody IngestionRequestDTO requestDTO) {
        IngestionDTO createdIngestionJob = ingestionService.createIngestionRequest(requestDTO);
        // Retorna o status 202 Accepted, indicando que o trabalho foi aceito para processamento
        return new ResponseEntity<>(createdIngestionJob, HttpStatus.ACCEPTED);
    }

    /**
     * MDM consulta o status de um trabalho de ingestão específico.
     */
    @GetMapping("/{id}")
    public ResponseEntity<IngestionDTO> getIngestionStatus(@PathVariable Integer id) {
        IngestionDTO ingestionJob = ingestionService.getIngestionById(id);
        return ResponseEntity.ok(ingestionJob);
    }

    /**
     * MDM lista todos os trabalhos de ingestão.
     */
    @GetMapping
    public ResponseEntity<List<IngestionDTO>> getAllIngestions() {
        List<IngestionDTO> ingestionJobs = ingestionService.getAllIngestions();
        return ResponseEntity.ok(ingestionJobs);
    }
}