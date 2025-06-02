package com.es2.mdm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.es2.mdm.dto.ProviderDTO;
import com.es2.mdm.service.ProviderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/mdm/api/providers") 
public class ProviderController {

// Controller responsável por gerenciar provedores de dados no MDM (Master Data Management).
// Ele fornece endpoints para criar, listar, obter, atualizar e excluir provedores de dados.
// Ele utiliza o ProviderService para realizar as operações de negócios relacionadas aos provedores.
// As operações são realizadas através de requisições HTTP, onde cada método corresponde a um endpoint específico.

    private final ProviderService providerService;

    @Autowired 
    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    /**
     * Cria um novo provedor. Endpoint: POST /mdm/api/providers
     * Body: ProviderDTO
     */

    @PostMapping
    public ResponseEntity<ProviderDTO> createProvider(@Valid @RequestBody ProviderDTO providerDTO) {
        ProviderDTO createdProvider = providerService.createProvider(providerDTO);
        return new ResponseEntity<>(createdProvider, HttpStatus.CREATED);
    }

    /**
     * Lista todos os provedores. Endpoint: GET /mdm/api/providers
     */
    @GetMapping
    public ResponseEntity<List<ProviderDTO>> getAllProviders() {
        List<ProviderDTO> providers = providerService.getAllProviders();
        return ResponseEntity.ok(providers);
    }

    /**
     * Obtém um provedor pelo ID. Endpoint: GET /mdm/api/providers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProviderDTO> getProviderById(@PathVariable Integer id) {
        ProviderDTO providerDTO = providerService.getProviderById(id);
        return ResponseEntity.ok(providerDTO);
    }

    /**
     * Atualiza um provedor pelo ID. Endpoint: PUT /mdm/api/providers/{id}
     * Body: ProviderDTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProviderDTO> updateProvider(@PathVariable Integer id, @Valid @RequestBody ProviderDTO providerDTO) {
        ProviderDTO updatedProvider = providerService.updateProvider(id, providerDTO);
        return ResponseEntity.ok(updatedProvider);
    }

    /**
     * Deleta um provedor pelo ID. Endpoint: DELETE /mdm/api/providers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(@PathVariable Integer id) {
        providerService.deleteProvider(id);
        return ResponseEntity.noContent().build();
    }
}