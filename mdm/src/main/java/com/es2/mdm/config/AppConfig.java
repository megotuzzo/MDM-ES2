package com.es2.mdm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
// Configuração da classe AppConfig para definir beans do Spring 
//que serão utilizados em toda a aplicação, como o RestTemplate para chamadas HTTP.
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}