package com.es2.mdm.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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

// Serviço responsável por gerenciar as operações CRUD relacionadas a países.
// Ele utiliza o repositório CountryRepository para interagir com o banco de dados e realizar operações de criação, leitura, atualização e exclusão de países.

    private final CountryRepository countryRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME; // Formato de data e hora ISO

    @Autowired
    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    // Cria um novo país no banco de dados.
    // O método é anotado com @Transactional para garantir que a operação seja executada dentro de uma transação.
    // Este método recebe um CountryDTO, converte-o em uma entidade Country, salva no repositório e retorna o CountryDTO salvo.
    @Transactional
    public CountryDTO createCountry(CountryDTO countryDTO) {
        Country country = convertToEntity(countryDTO);
        Country savedCountry = countryRepository.save(country);
        return convertToDTO(savedCountry);
    }

    // Obtém todos os países do banco de dados.
    // O método é anotado com @Transactional(readOnly = true) para indicar que é uma operação de leitura.
    // Ele busca todos os países do repositório, converte cada entidade Country em CountryDTO e retorna uma lista de CountryDTOs.
    @Transactional(readOnly = true)
    public List<CountryDTO> getAllCountries() {
        return countryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Obtém um país pelo ID.
    // O método é anotado com @Transactional(readOnly = true) para indicar que é uma operação de leitura.
    // Ele busca o país pelo ID no repositório, converte a entidade Country em CountryDTO e retorna o CountryDTO.
    // Se o país não for encontrado, lança uma EntityNotFoundException.
    // O método utiliza o Optional para lidar com a possibilidade de o país não existir.
    @Transactional(readOnly = true)
    public CountryDTO getCountryById(Integer id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Country not found with id: " + id));
        return convertToDTO(country);
    }

    // Atualiza um país existente.
    // O método é anotado com @Transactional para garantir que a operação seja executada dentro de uma transação.
    // Ele busca o país pelo ID, atualiza os campos com os valores do CountryDTO fornecido, atualiza as moedas associadas e salva o país atualizado no repositório.
    // Se o país não for encontrado, lança uma EntityNotFoundException.
    // O método também substitui as moedas existentes por novas moedas.
    // O método utiliza o Optional para lidar com a possibilidade de o país não existir.
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

        // Atualizar moedas (substituí as existestes, sem tratamento de duplicidade)
        // Limpa as moedas existentes antes de adicionar as novas
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

    // Deleta um país pelo ID.
    // Ele verifica se o país existe no repositório pelo ID fornecido.
    // Se o país não for encontrado, lança uma EntityNotFoundException.
    // Ele então deleta o país do repositório.
    @Transactional
    public void deleteCountry(Integer id) {
        if (!countryRepository.existsById(id)) {
            throw new EntityNotFoundException("Country not found with id: " + id);
        }
        countryRepository.deleteById(id);
    }


    // Processa uma lista de CountryDTOs recebidos do DEM e salva ou atualiza os países no banco de dados.
    // O método verifica se a lista está vazia ou nula e retorna imediatamente se for o caso.
    // Para cada CountryDTO na lista, ele verifica se já existe um país com o mesmo código numérico.
    // Se existir, atualiza os campos do país existente com os valores do CountryDTO.
    // Se não existir, cria um novo país a partir do CountryDTO e o salva no repositório.
    @Transactional
    public void processAndSaveCountries(List<CountryDTO> countryDTOs) {
        if (countryDTOs == null || countryDTOs.isEmpty()) { // Verifica se a lista de CountryDTOs está vazia ou nula
            return;
        }

        for (CountryDTO dto : countryDTOs) {    // Para cada CountryDTO na lista, verifica se já existe um país com o mesmo código numérico
            Optional<Country> existingCountryOpt = Optional.empty();
            if (dto.getNumericCode() != null) { 
                existingCountryOpt = countryRepository.findByNumericCode(dto.getNumericCode());

            } 

            Country countryToSave;
            if (existingCountryOpt.isPresent()) { //Se ja existe um país com o mesmo código numérico, atualiza o país
                countryToSave = existingCountryOpt.get();
                // Atualiza os campos de countryToSave com os valores de dto
                countryToSave.setCountryName(dto.getCountryName());
                countryToSave.setCapitalCity(dto.getCapitalCity());
                countryToSave.setPopulation(dto.getPopulation());
                countryToSave.setArea(dto.getArea());
                
                countryToSave.getCurrencies().clear();
                if (dto.getCurrencies() != null) { 
                    dto.getCurrencies().forEach(currencyDTO -> countryToSave.addCurrency(convertToEntity(currencyDTO, countryToSave)));
                }

            } else {    //Cria novo país
                countryToSave = convertToEntity(dto); 
            }
            countryRepository.save(countryToSave);
        }
    }

    //Métodos de conversão de entidades entre DTO's e modelos.
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
