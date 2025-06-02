package com.es2.dem.model; 

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.es2.dem.enums.IngestionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "ingestion") 
public class Ingestion {

// Classe que representa uma solicitação de ingestão de dados no DEM.
// Ela contém informações sobre o provedor MDM, o status da ingestão, os caminhos dos dados brutos e transformados,
// a URL de sincronização do MDM, mensagens de status e timestamps de criação e atualização.
// A classe é anotada com @Entity para indicar que é uma entidade JPA e @Table para especificar o nome da tabela no banco de dados.
// A classe também possui anotações para mapear os campos para colunas do banco de dados, incluindo o uso de @Lob para campos de texto longo.

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