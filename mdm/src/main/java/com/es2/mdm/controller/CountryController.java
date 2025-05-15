package com.es2.mdm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.es2.mdm.model.Country;
import com.es2.mdm.repository.CountryRepository;

@RestController
@RequestMapping("/countries")
public class CountryController {

    @Autowired
    private CountryRepository countryRepository;

    @GetMapping
    public List<Country> getAll() {
        return countryRepository.findAll();
    }

    @PostMapping
    public Country create(@RequestBody Country country) {
        return countryRepository.save(country);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Country> update(@PathVariable Long id, @RequestBody Country updated) {
        return countryRepository.findById(id)
                .map(country -> {
                    country.setCountryName(updated.getCountryName());
                    // O QUE MAIS?
                    return ResponseEntity.ok(countryRepository.save(country));
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (countryRepository.existsById(id)) {
            countryRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
