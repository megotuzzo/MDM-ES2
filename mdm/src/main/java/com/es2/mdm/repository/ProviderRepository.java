package com.es2.mdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.es2.mdm.model.Provider;

// Repositório para a entidade Provider, estendendo JpaRepository para operações CRUD.
@Repository
public interface ProviderRepository extends JpaRepository<Provider, Integer> {

}