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

import com.es2.mdm.dto.CountryDTO;
import com.es2.mdm.service.CountryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/countries")
public class CountryController {

// Controller responsável por gerenciar países no MDM.
// Ele fornece endpoints para criar, listar, obter, atualizar e excluir países.
// Ele utiliza o CountryService para realizar as operações de negócios relacionadas aos países.
// As operações são realizadas através de requisições HTTP, onde cada método corresponde a um endpoint específico.

    private final CountryService countryService;

    @Autowired
    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    /**
     * Cria um novo país. Endpoint: POST /countries 
     * Body: CountryDTO
     */
    @PostMapping
    public ResponseEntity<CountryDTO> createCountry(@Valid @RequestBody CountryDTO countryDTO) {
        CountryDTO createdCountry = countryService.createCountry(countryDTO);
        return new ResponseEntity<>(createdCountry, HttpStatus.CREATED);
    }

    /**
     * Lista todos os países. Endpoint: GET /countries
     */
    @GetMapping
    public ResponseEntity<List<CountryDTO>> getAllCountries() {
        List<CountryDTO> countries = countryService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    /**
     * Obtém um país pelo ID. Endpoint: GET /countries/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CountryDTO> getCountryById(@PathVariable Integer id) {
        CountryDTO countryDTO = countryService.getCountryById(id); 
        return ResponseEntity.ok(countryDTO);
    }

    /**
     * Atualiza um país existente. Endpoint: PUT /countries/{id} 
     * Body: CountryDTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<CountryDTO> updateCountry(@PathVariable Integer id, @Valid @RequestBody CountryDTO countryDTO) {
        CountryDTO updatedCountry = countryService.updateCountry(id, countryDTO);
        return ResponseEntity.ok(updatedCountry);
    }

    /**
     * Deleta um país pelo ID. Endpoint: DELETE /countries/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCountry(@PathVariable Integer id) {
        countryService.deleteCountry(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
    /**
     * Recebe uma lista de países processados pelo DEM. Endpoint: POST /countries/callback
     * Body: List<CountryDTO>
     */
    @PostMapping("/callback")
    public ResponseEntity<String> receiveCountries(@RequestBody List<CountryDTO> processedCountries) {
        try {
            
            countryService.processAndSaveCountries(processedCountries);
            return ResponseEntity.ok("Dados de países recebidos e processados com sucesso.");
        } catch (Exception e) {
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar dados dos países: " + e.getMessage());
        }
    }
}
