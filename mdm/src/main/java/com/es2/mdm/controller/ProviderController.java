package com.es2.mdm.controller;

import com.es2.mdm.dto.ProviderDTO;
import com.es2.mdm.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mdm/api/providers") 
public class ProviderController {

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