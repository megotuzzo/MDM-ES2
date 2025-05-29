package com.es2.dem.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.es2.dem.dto.IngestionDTO;
import com.es2.dem.dto.IngestionRequestDTO;
import com.es2.dem.enums.IngestionStatus;
import com.es2.dem.model.Ingestion;
import com.es2.dem.repository.IngestionRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class IngestionService {

    private final IngestionRepository ingestionRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    public IngestionService(IngestionRepository ingestionRepository) {
        this.ingestionRepository = ingestionRepository;
    }

    @Transactional
    public IngestionDTO createIngestionRequest(IngestionRequestDTO requestDTO) {
        Ingestion newIngestion = new Ingestion();
        newIngestion.setMdmProviderId(requestDTO.getMdmProviderId());
        newIngestion.setMdmSyncUrl(requestDTO.getMdmSyncUrl());
        newIngestion.setStatus(IngestionStatus.PENDING); // status inicial

        // rawDataPath, transformedDataPath, statusMessage serão preenchidos depois
        Ingestion savedIngestion = ingestionRepository.save(newIngestion);
        // Aqui o processo de extração

        return convertToDTO(savedIngestion);
    }

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

    //conversão dto para entidade
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
        ingestion.setId(dto.getId());
        ingestion.setMdmProviderId(dto.getMdmProviderId());

        //conversao do status de String para Enum
        if (dto.getStatus() != null) {
            ingestion.setStatus(IngestionStatus.valueOf(dto.getStatus()));
        }
        ingestion.setRawDataPath(dto.getRawDataPath());
        ingestion.setTransformedDataPath(dto.getTransformedDataPath());
        ingestion.setStatusMessage(dto.getStatusMessage());

        //conversao das datas
        if (dto.getCreatedAt() != null) {
            ingestion.setCreatedAt(java.time.LocalDateTime.parse(dto.getCreatedAt(), formatter));
        }
        if (dto.getUpdatedAt() != null) {
            ingestion.setUpdatedAt(java.time.LocalDateTime.parse(dto.getUpdatedAt(), formatter));
        }
        return ingestion;
    }

}
