package com.es2.mdm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.es2.mdm.model.Country;

// Repositório para a entidade Country, estendendo JpaRepository para operações CRUD.
@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {
    Optional<Country> findByNumericCode(Integer numericCode);
    Optional<Country> findByCountryName(String countryName); 
}
