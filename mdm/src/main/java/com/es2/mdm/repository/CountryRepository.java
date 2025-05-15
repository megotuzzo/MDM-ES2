package com.es2.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.es2.mdm.model.Country;

public interface CountryRepository extends JpaRepository<Country, Long> {

}
