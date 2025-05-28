package com.es2.dem.model; 

import com.es2.dem.enums.IngestionStatus; 
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingestion") 
public class Ingestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "mdm_provider_id", nullable = false)
    private Integer mdmProviderId; 

    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private IngestionStatus status;

    @Column(name = "raw_data_path")
    private String rawDataPath; 

    @Column(name = "transformed_data_path")
    private String transformedDataPath; 

    @Column(name = "mdm_sync_url") 
    private String mdmSyncUrl;

    @Lob //mensagens longass
    @Column(name = "status_message")
    private String statusMessage; 

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Ingestion() {
    }

    public Ingestion(Integer mdmProviderId, IngestionStatus status, String mdmSyncUrl) {
        this.mdmProviderId = mdmProviderId;
        this.status = status;
        this.mdmSyncUrl = mdmSyncUrl;
    }

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

    public IngestionStatus getStatus() {
        return status;
    }

    public void setStatus(IngestionStatus status) {
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

    public String getMdmSyncUrl() {
        return mdmSyncUrl;
    }

    public void setMdmSyncUrl(String mdmSyncUrl) {
        this.mdmSyncUrl = mdmSyncUrl;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}