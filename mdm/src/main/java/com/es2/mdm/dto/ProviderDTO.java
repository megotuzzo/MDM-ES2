package com.es2.mdm.dto;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size; 


public class ProviderDTO {

// DTO para representar um provedor de dados no MDM (Master Data Management).
// Ele inclui informações como ID, nome, categoria, URL da API, descrição e timestamps de criação e atualização.
// Ele é usado para comunicar os detalhes do provedor de dados para o DEM.

    private Integer id;

    // Anotações de validação para garantir que os dados atendam aos requisitos especificados do Banco de Dados.
    @NotBlank(message = "Provider name cannot be blank")
    @Size(max = 255, message = "Provider name must be less than 100 characters")
    private String name;

    @Size(max = 100, message = "Category must be less than 100 characters")
    private String category;

    @NotBlank(message = "URL cannot be blank")
    @URL(message = "URL must be a valid URL")
    @Size(max = 500, message = "URL must be less than 500 characters")
    private String apiUrl;

    private String description;

    private String createdAt; 
    private String updatedAt; 

    public ProviderDTO() {
    }

    public ProviderDTO(Integer id, String name, String category, String apiUrl, String description, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.apiUrl = apiUrl;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}