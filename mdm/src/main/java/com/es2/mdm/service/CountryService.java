package com.es2.mdm.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.es2.mdm.dto.CountryDTO;
import com.es2.mdm.dto.CurrencyDTO;
import com.es2.mdm.model.Country;
import com.es2.mdm.model.Currency;
import com.es2.mdm.repository.CountryRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CountryService {

    private final CountryRepository countryRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Transactional
    public CountryDTO createCountry(CountryDTO countryDTO) {
        Country country = convertToEntity(countryDTO);
        Country savedCountry = countryRepository.save(country);
        return convertToDTO(savedCountry);
    }

    @Transactional(readOnly = true)
    public List<CountryDTO> getAllCountries() {
        return countryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CountryDTO getCountryById(Integer id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Country not found with id: " + id));
        return convertToDTO(country);
    }

    @Transactional
    public CountryDTO updateCountry(Integer id, CountryDTO countryDTO) {
        Country existingCountry = countryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Country not found with id: " + id));

        // Atualizar campos 
        existingCountry.setCountryName(countryDTO.getCountryName());
        existingCountry.setNumericCode(countryDTO.getNumericCode());
        existingCountry.setCapitalCity(countryDTO.getCapitalCity());
        existingCountry.setPopulation(countryDTO.getPopulation());
        existingCountry.setArea(countryDTO.getArea());

        // Atualizar moedas (substituí as existestes, sem tratamento para ficar mais simples)
        existingCountry.getCurrencies().clear();
        if (countryDTO.getCurrencies() != null) {
            countryDTO.getCurrencies().forEach(currencyDTO -> {
                Currency currency = convertToEntity(currencyDTO, existingCountry);
                existingCountry.addCurrency(currency);
            });
        }

        Country updatedCountry = countryRepository.save(existingCountry);
        return convertToDTO(updatedCountry);
    }

    @Transactional
    public void deleteCountry(Integer id) {
        if (!countryRepository.existsById(id)) {
            throw new EntityNotFoundException("Country not found with id: " + id);
        }
        countryRepository.deleteById(id);
    }

    //conversao entre entidades e DTOs
    private CountryDTO convertToDTO(Country country) {
        CountryDTO dto = new CountryDTO();
        dto.setId(country.getId());
        dto.setCountryName(country.getCountryName());
        dto.setNumericCode(country.getNumericCode());
        dto.setCapitalCity(country.getCapitalCity());
        dto.setPopulation(country.getPopulation());
        dto.setArea(country.getArea());
        if (country.getCreatedAt() != null) {
            dto.setCreatedAt(country.getCreatedAt().format(formatter));
        }
        if (country.getUpdatedAt() != null) {
            dto.setUpdatedAt(country.getUpdatedAt().format(formatter));
        }
        if (country.getCurrencies() != null) {
            dto.setCurrencies(country.getCurrencies().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private CurrencyDTO convertToDTO(Currency currency) {
        CurrencyDTO dto = new CurrencyDTO();
        dto.setCurrencyId(currency.getCurrencyId());
        dto.setCurrencyCode(currency.getCurrencyCode());
        dto.setCurrencyName(currency.getCurrencyName());
        dto.setCurrencySymbol(currency.getCurrencySymbol());
        return dto;
    }

    private Country convertToEntity(CountryDTO dto) {
        Country country = new Country();
        country.setCountryName(dto.getCountryName());
        country.setNumericCode(dto.getNumericCode());
        country.setCapitalCity(dto.getCapitalCity());
        country.setPopulation(dto.getPopulation());
        country.setArea(dto.getArea());

        if (dto.getCurrencies() != null) {
            dto.getCurrencies().forEach(currencyDTO -> {
                country.addCurrency(convertToEntity(currencyDTO, country));
            });
        }
        return country;
    }

    private Currency convertToEntity(CurrencyDTO dto, Country country) {
        Currency currency = new Currency();
        currency.setCurrencyCode(dto.getCurrencyCode());
        currency.setCurrencyName(dto.getCurrencyName());
        currency.setCurrencySymbol(dto.getCurrencySymbol());
        currency.setCountry(country);

        return currency;
    }
}
