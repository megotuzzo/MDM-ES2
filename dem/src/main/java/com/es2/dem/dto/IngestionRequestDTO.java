package com.es2.dem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public class IngestionRequestDTO {

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

    // Getters e Setters
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