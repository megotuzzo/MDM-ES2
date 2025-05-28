package com.es2.mdm.service;

import com.es2.mdm.dto.ProviderDTO;
import com.es2.mdm.model.Provider;
import com.es2.mdm.repository.ProviderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProviderService {

    private final ProviderRepository providerRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @Autowired
    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Transactional
    public ProviderDTO createProvider(ProviderDTO providerDTO) {
        Provider provider = convertToEntity(providerDTO);
        Provider savedProvider = providerRepository.save(provider);
        return convertToDTO(savedProvider);
    }

    @Transactional(readOnly = true)
    public List<ProviderDTO> getAllProviders() {
        return providerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProviderDTO getProviderById(Integer id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found with id: " + id));
        return convertToDTO(provider);
    }

    @Transactional
    public ProviderDTO updateProvider(Integer id, ProviderDTO providerDTO) {
        Provider existingProvider = providerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found with id: " + id));

        existingProvider.setName(providerDTO.getName());
        existingProvider.setCategory(providerDTO.getCategory());
        existingProvider.setApiUrl(providerDTO.getApiUrl());
        existingProvider.setDescription(providerDTO.getDescription());

        Provider updatedProvider = providerRepository.save(existingProvider);
        return convertToDTO(updatedProvider);
    }

    @Transactional
    public void deleteProvider(Integer id) {
        if (!providerRepository.existsById(id)) {
            throw new EntityNotFoundException("Provider not found with id: " + id);
        }
        providerRepository.deleteById(id);
    }

    //conversao entre DTO e entidade
    private ProviderDTO convertToDTO(Provider provider) {
        ProviderDTO dto = new ProviderDTO();
        dto.setId(provider.getId());
        dto.setName(provider.getName());
        dto.setCategory(provider.getCategory());
        dto.setApiUrl(provider.getApiUrl());
        dto.setDescription(provider.getDescription());
        if (provider.getCreatedAt() != null) {
            dto.setCreatedAt(provider.getCreatedAt().format(formatter));
        }
        if (provider.getUpdatedAt() != null) {
            dto.setUpdatedAt(provider.getUpdatedAt().format(formatter));
        }
        return dto;
    }

    private Provider convertToEntity(ProviderDTO dto) {
        Provider provider = new Provider();

        provider.setName(dto.getName());
        provider.setCategory(dto.getCategory());
        provider.setApiUrl(dto.getApiUrl());
        provider.setDescription(dto.getDescription());

        return provider;
    }
}