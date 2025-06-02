package com.es2.mdm.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size; 

public class CountryDTO {

// DTO para representar um país, incluindo informações como nome, código numérico, capital, população, área e moedas.
// Ele inclui anotações de validação para garantir que os dados atendam aos requisitos especificados do Banco de Dados.

    private Integer id;
    @NotBlank(message = "Country name cannot be blank")
    @Size(max = 100, message = "Country name must be less than 100 characters")
    private String countryName;

    @NotNull(message = "Numeric code cannot be null")
    private Integer numericCode;

    @Size(max = 100, message = "Capital city must be less than 100 characters")
    private String capitalCity;

    @Positive(message = "Population must be a positive number")
    private Integer population;

    @Positive(message = "Area must be a positive number")
    private Float area;

    @Valid // Anotação para validar a lista de moedas, garantindo que cada moeda na lista atenda às suas próprias regras de validação.
    private List<CurrencyDTO> currencies = new ArrayList<>();

    private String createdAt;
    private String updatedAt;

    public CountryDTO() {
    }

    public CountryDTO(Integer id, String countryName, Integer numericCode, String capitalCity, Integer population, Float area, List<CurrencyDTO> currencies) {
        this.id = id;
        this.countryName = countryName;
        this.numericCode = numericCode;
        this.capitalCity = capitalCity;
        this.population = population;
        this.area = area;
        this.currencies = currencies;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public Integer getNumericCode() {
        return numericCode;
    }

    public void setNumericCode(Integer numericCode) {
        this.numericCode = numericCode;
    }

    public String getCapitalCity() {
        return capitalCity;
    }

    public void setCapitalCity(String capitalCity) {
        this.capitalCity = capitalCity;
    }

    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(Integer population) {
        this.population = population;
    }

    public Float getArea() {
        return area;
    }

    public void setArea(Float area) {
        this.area = area;
    }

    public List<CurrencyDTO> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<CurrencyDTO> currencies) {
        this.currencies = currencies;
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
