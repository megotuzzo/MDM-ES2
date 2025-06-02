package com.es2.dem.dto;

//Campos do ProviderDTO que o DEM precisa.
public class MdmProviderResponseDTO {

    // DTO para representar um provedor MDM no DEM.
    // Ele inclui informações como ID, nome e URL da API do provedor.
    // Ele é usado para transferir dados entre a camada de apresentação e a camada de serviço.

    private Integer id;
    private String name;
    private String apiUrl;

    public MdmProviderResponseDTO() {

    }


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
}