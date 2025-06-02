package com.es2.dem.enums;

public enum IngestionStatus {

// Enumeração que representa os diferentes estados de uma solicitação de ingestão de dados no DEM.
// Cada estado indica uma fase específica do processo de ingestão.

    PENDING,        //Solicitação de ingestão de dados pendente, aguardando início do processamento
    PROCESSING,     //Buscando provedor, extraindo dados e os transformando
    READY,          //Dados extraídos e transformados, prontos para serem enviados
    COMPLETED,      //Dados processados foram enviados para o MDM 
    FAILED;         //Ocorreu um  erro no processo de ingestão
}
