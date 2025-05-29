package com.es2.mdm.dto;

//Entrada da API do DEM
public class DemIngestionRequestDTO {
    private Integer mdmProviderId;
    private String mdmSyncUrl;

    public DemIngestionRequestDTO(Integer mdmProviderId, String mdmSyncUrl) {
        this.mdmProviderId = mdmProviderId;
        this.mdmSyncUrl = mdmSyncUrl;
    }

    public Integer getMdmProviderId() { return mdmProviderId; }
    public String getMdmSyncUrl() { return mdmSyncUrl; }
}