package com.es2.dem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.es2.dem.model.Ingestion;

@Repository
public interface IngestionRepository extends JpaRepository<Ingestion, Integer>{
    
}
