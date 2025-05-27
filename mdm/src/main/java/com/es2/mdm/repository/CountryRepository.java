package com.es2.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.es2.mdm.model.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {

}
