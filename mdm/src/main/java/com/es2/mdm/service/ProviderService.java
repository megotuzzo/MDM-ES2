package com.es2.mdm.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.es2.mdm.dto.ProviderDTO;
import com.es2.mdm.model.Provider;
import com.es2.mdm.repository.ProviderRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProviderService {

// Serviço responsável pela lógica de negócios relacionada aos provedores de serviços no MDM.
// Ele fornece métodos para criar, ler, atualizar e excluir provedores, além de converter entre DTOs e entidades.

    private final ProviderRepository providerRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @Autowired
    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    // Cria um novo provedor no banco de dados.
    // Este método recebe um ProviderDTO, converte-o em uma entidade Provider, salva no repositório e retorna o ProviderDTO salvo.
    @Transactional
    public ProviderDTO createProvider(ProviderDTO providerDTO) {
        Provider provider = convertToEntity(providerDTO);
        Provider savedProvider = providerRepository.save(provider);
        return convertToDTO(savedProvider);
    }

    // Obtém todos os provedores do banco de dados.
    // Este método busca todos os provedores do repositório, converte cada entidade Provider em ProviderDTO e retorna uma lista de ProviderDTOs.
    @Transactional(readOnly = true)
    public List<ProviderDTO> getAllProviders() {
        return providerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Obtém um provedor pelo ID.
    // Este método busca o provedor pelo ID no repositório, converte a entidade Provider em ProviderDTO e retorna o ProviderDTO.
    // Se o provedor não for encontrado, lança uma EntityNotFoundException.
    // O método utiliza o Optional para lidar com a possibilidade de o provedor não existir.
    @Transactional(readOnly = true)
    public ProviderDTO getProviderById(Integer id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Provider not found with id: " + id));
        return convertToDTO(provider);
    }

    // Atualiza um provedor existente no banco de dados.
    // Este método recebe um ID e um ProviderDTO, busca o provedor existente pelo ID, 
    // atualiza seus campos com os valores do DTO e salva as alterações no repositório.
    // Se o provedor não for encontrado, lança uma EntityNotFoundException.
    // O método retorna o ProviderDTO atualizado.
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


    // Exclui um provedor pelo ID.
    // Este método verifica se o provedor existe no repositório antes de tentar excluí-lo.
    // Se o provedor não for encontrado, lança uma EntityNotFoundException.
    @Transactional
    public void deleteProvider(Integer id) {
        if (!providerRepository.existsById(id)) {
            throw new EntityNotFoundException("Provider not found with id: " + id);
        }
        providerRepository.deleteById(id);
    }

    //Métodos de conversão de entidades entre DTO's e modelos.
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