package com.es2.mdm.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "country")
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CountryID")
    private Integer id;

    @Column(name = "CountryName", length = 100, nullable = false, unique = true)
    private String countryName;

    @Column(name = "NumericCode", unique = true)
    private Integer numericCode;

    @Column(name = "CapitalCity", length = 100)
    private String capitalCity;

    @Column(name = "Population")
    private Integer population;

    @Column(name = "Area")
    private Float area;

    // "mappedBy = "country"" indica que a entidade Currency gerencia o relacionamento (com a FK CountryID).
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Currency> currencies = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    public Country() {
    }

    public Country(String countryName, Integer numericCode, String capitalCity, Integer population, Float area) {
        this.countryName = countryName;
        this.numericCode = numericCode;
        this.capitalCity = capitalCity;
        this.population = population;
        this.area = area;
    }

    // Getters e Setters
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

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
        for (Currency currency : currencies) {
            currency.setCountry(this);
        }
    }

    public void addCurrency(Currency currency) {
        this.currencies.add(currency);
        currency.setCountry(this);
    }

    public void removeCurrency(Currency currency) {
        this.currencies.remove(currency);
        currency.setCountry(null);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // toString, equals, hashCode (opcional, mas recomendado)
}
