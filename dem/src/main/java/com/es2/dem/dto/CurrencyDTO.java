package com.es2.dem.dto;

public class CurrencyDTO {

// DTO para representar uma moeda, incluindo informações como ID, código, nome e símbolo.
// Ele é usado para transferir dados entre a camada de apresentação e a camada de serviço.
// Inclui construtores, getters e setters para facilitar a manipulação dos dados da moeda.

    private Integer currencyId;
    private String currencyCode;
    private String currencyName;
    private String currencySymbol;

    public CurrencyDTO() {
    }

    public CurrencyDTO(Integer currencyId, String currencyCode, String currencyName, String currencySymbol) {
        this.currencyId = currencyId;
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.currencySymbol = currencySymbol;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }
}
