package com.es2.dem.dto;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class IngestionRequestDTO {

// DTO para representar uma solicitação de ingestão de dados no DEM.
// Ele inclui informações como ID do provedor MDM e URL de sincronização.
// Ele é usado para transferir dados entre a camada de apresentação e a camada de serviço.
// Inclui validações para garantir que os dados sejam consistentes e atendam aos requisitos do sistema.

    @NotNull(message = "Provider id cannot be blank")
    private Integer mdmProviderId;

    @NotBlank(message = "Sync URL cannot be blank")
    @URL(message = "URL must be a valid URL")
    private String mdmSyncUrl;

    public IngestionRequestDTO() {
    }

    public IngestionRequestDTO(Integer mdmProviderId, String mdmSyncUrl) {
        this.mdmProviderId = mdmProviderId;
        this.mdmSyncUrl = mdmSyncUrl;
    }

    public Integer getMdmProviderId() {
        return mdmProviderId;
    }

    public void setMdmProviderId(Integer mdmProviderId) {
        this.mdmProviderId = mdmProviderId;
    }

    public String getMdmSyncUrl() {
        return mdmSyncUrl;
    }

    public void setMdmSyncUrl(String mdmSyncUrl) {
        this.mdmSyncUrl = mdmSyncUrl;
    }
}