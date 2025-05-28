package com.es2.dem.dto;

public class IngestionDTO {

    private Integer id;
    private Integer mdmProviderId;
    private String status; 
    private String rawDataPath;
    private String transformedDataPath;
    // mdmSyncUrl ???
    private String statusMessage;
    private String createdAt; 
    private String updatedAt; 

    public IngestionDTO() {
    }

    public IngestionDTO(Integer id, Integer mdmProviderId, String status, String rawDataPath, String transformedDataPath, String statusMessage, String createdAt, String updatedAt) {
        this.id = id;
        this.mdmProviderId = mdmProviderId;
        this.status = status;
        this.rawDataPath = rawDataPath;
        this.transformedDataPath = transformedDataPath;
        this.statusMessage = statusMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMdmProviderId() {
        return mdmProviderId;
    }

    public void setMdmProviderId(Integer mdmProviderId) {
        this.mdmProviderId = mdmProviderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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