package com.es2.mdm.dto;


public class DemIngestionRequestDTO {
    
// DTO para representar uma solicitação de ingestão de dados do MDM (Master Data Management).
// Ele inclui o ID do provedor MDM e a URL de sincronização do MDM para enviar ao DEM, que trata os dados.


    private Integer mdmProviderId;
    private String mdmSyncUrl;

    public DemIngestionRequestDTO(Integer mdmProviderId, String mdmSyncUrl) {
        this.mdmProviderId = mdmProviderId;
        this.mdmSyncUrl = mdmSyncUrl;
    }

    public Integer getMdmProviderId() { return mdmProviderId; }
    public String getMdmSyncUrl() { return mdmSyncUrl; }
}