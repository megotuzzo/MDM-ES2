package com.es2.mdm.dto;

// Espelha os IngestionDTO do DEM
public class DemIngestionResponseDTO {
    private Integer id;
    private Integer mdmProviderId;
    private String status;
    private String rawDataPath;
    private String transformedDataPath;
    private String statusMessage;
    private String createdAt;
    private String updatedAt;

    
    public DemIngestionResponseDTO() {

    }

    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }


    public Integer getMdmProviderId() {
        return mdmProviderId;
    }

    public void setMdmProviderId(Integer mdmProviderId) {
        this.mdmProviderId = mdmProviderId;
    }


    public String getRawDataPath() {
        return rawDataPath;
    }


    public void setRawDataPath(String rawDataPath) {
        this.rawDataPath = rawDataPath;
    }


    public String getTransformedDataPath() {
        return transformedDataPath;
    }


    public void setTransformedDataPath(String transformedDataPath) {
        this.transformedDataPath = transformedDataPath;
    }


    public String getStatusMessage() {
        return statusMessage;
    }


    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
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