package com.es2.dem.dto;

//campos do ProviderDTO que o DEM precisa
public class MdmProviderResponseDTO {
    private Integer id;
    private String name;
    private String apiUrl;

    public MdmProviderResponseDTO() {

    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
}