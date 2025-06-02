package com.es2.dem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.es2.dem.model.Ingestion;

// Interface que estende JpaRepository para fornecer operações CRUD para a entidade Ingestion. 
@Repository
public interface IngestionRepository extends JpaRepository<Ingestion, Integer>{
    
}
