# **Documentação das APIs RESTful**  **\- Sistema MDM & DEM \-**

Este documento descreve as APIs RESTful para os microsserviços MDM (Master Data Management) e DEM (Data Extraction Management).

## **1\. Microsserviço MDM (Master Data Management)**

Responsável pela gestão dos dados mestres (Países, Provedores) e pela orquestração da ingestão de dados com o DEM. 

**URL Base da API MDM:** `http://localhost:8081`

### **1.1. API de Países (`CountryController`)**

Gerencia os dados mestres de países. **Path Base:** `/countries`

* **Criar um Novo País**  
  * **Endpoint:** `POST /countries`  
  * **Descrição:** Cadastra um novo país no sistema.  
  * **Request Body:** `CountryDTO`
    ```json
        {
            "id": 454,
            "countryName": "Tonga",
            "numericCode": 776,
            "capitalCity": "Nuku'alofa",
            "population": 105697,
            "area": 747.0,
            "currencies": [
                {
                    "currencyId": 1012,
                    "currencyCode": "TOP",
                    "currencyName": "Tongan paʻanga",
                    "currencySymbol": "T$"
                }
            ]
        }
    ```

  * **Success Response:** `201 Created` com o `CountryDTO` criado (incluindo `id`, `createdAt`, `updatedAt`).  
  * **Error Responses:** `400 Bad Request` (se dados inválidos), `500 Internal Server Error`.  
* **Listar Todos os Países**  
  * **Endpoint:** `GET /countries`  
  * **Descrição:** Retorna uma lista de todos os países cadastrados.  
  * **Success Response:** `200 OK` com `List<CountryDTO>`.

* **Obter País por ID**  
  * **Endpoint:** `GET /countries/{id}`  
  * **Descrição:** Retorna os detalhes de um país específico pelo seu ID.  
  * **Path Variable:** `id` (Integer) \- ID do país.  
  * **Success Response:** `200 OK` com `CountryDTO`.  
  * **Error Responses:** `404 Not Found`.

* **Atualizar um País Existente**  
  * **Endpoint:** `PUT /countries/{id}`  
  * **Descrição:** Atualiza os dados de um país existente.  
  * **Path Variable:** `id` (Integer) \- ID do país a ser atualizado.  
  * **Request Body:** `CountryDTO` (campos a serem atualizados)
      ```json
        {
            "countryName": "Tonga Atualizada",
            "numericCode": 776,
            "capitalCity": "Nuku'alofa nova",
            "population": 105697,
            "area": 747.0,
            "currencies": [
                {
                    "currencyId": 1012,
                    "currencyCode": "TOP",
                    "currencyName": "Tongan paʻanga",
                    "currencySymbol": "T$"
                }
            ]
        }
      ```
  * **Success Response:** `200 OK` com o `CountryDTO` atualizado.  
  * **Error Responses:** `400 Bad Request`, `404 Not Found`.

* **Deletar um País**  
  * **Endpoint:** `DELETE /countries/{id}`  
  * **Descrição:** Remove um país do sistema pelo seu ID.  
  * **Path Variable:** `id` (Integer) \- ID do país a ser deletado.  
  * **Success Response:** `204 No Content`.  
  * **Error Responses:** `404 Not Found`.

    

### **1.2. API de Provedores (`ProviderController`)**

Gerencia os provedores de dados externos. **Path Base:** `/mdm/api/providers`

* **Criar um Novo Provedor**  
  * **Endpoint:** `POST /mdm/api/providers`  
  * **Descrição:** Cadastra um novo provedor de dados.  
  * **Request Body:** `ProviderDTO`
      ```json
        {
            "name": "RestCountries API",
            "category": "Dados Geográficos",
            "apiUrl": "https://restcountries.com/v3.1",
            "description": "API para dados de países"
        }
      ```
  * **Success Response:** `201 Created` com o `ProviderDTO` criado.  
  * **Error Responses:** `400 Bad Request`, `500 Internal Server Error`.

* **Listar Todos os Provedores**  
  * **Endpoint:** `GET /mdm/api/providers`  
  * **Descrição:** Retorna uma lista de todos os provedores cadastrados.  
  * **Success Response:** `200 OK` com `List<ProviderDTO>`.

* **Obter Provedor por ID**  
  * **Endpoint:** `GET /mdm/api/providers/{id}`  
  * **Descrição:** Retorna os detalhes de um provedor específico.  
  * **Path Variable:** `id` (Integer) \- ID do provedor.  
  * **Success Response:** `200 OK` com `ProviderDTO`.  
  * **Error Responses:** `404 Not Found`.

* **Atualizar um Provedor Existente**  
  * **Endpoint:** `PUT /mdm/api/providers/{id}`  
  * **Descrição:** Atualiza os dados de um provedor.  
  * **Path Variable:** `id` (Integer) \- ID do provedor.  
  * **Request Body:** `ProviderDTO`
      ```json
        {
            "name": "RestCountries Atualizado",
            "category": "NOVOS Dados Geográficos de Países",
            "apiUrl": "https://restcountries.com/v3.1",
            "description": "API pública para obter informações sobre países."
        }
      ```

  * **Success Response:** `200 OK` com `ProviderDTO` atualizado.  
  * **Error Responses:** `400 Bad Request`, `404 Not Found`.  
* **Deletar um Provedor**  
  * **Endpoint:** `DELETE /mdm/api/providers/{id}`  
  * **Descrição:** Remove um provedor.  
  * **Path Variable:** `id` (Integer) \- ID do provedor.  
  * **Success Response:** `204 No Content`.  
  * **Error Responses:** `404 Not Found`.

### **1.3. API de Administração de Ingestão (MDM interagindo com DEM)**

Endpoints para o MDM gerenciar e disparar processos de ETL no DEM. (Baseado no `AdminController.java` do MDM). **Path Base:** `/mdm/api/admin`

* **Disparar Ingestão de Dados no DEM para um Provedor**  
  * **Endpoint:** `POST /mdm/api/admin/ingest/provider/{providerId}`  
  * **Descrição:** Solicita ao DEM que inicie o processo de extração e transformação para o provedor com o ID fornecido.  
  * **Path Variable:** `providerId` (Integer) \- ID do provedor (registrado no MDM).  
  * **Request Body:** Nenhum.  
  * **Success Response:** `200 OK` (ou `202 Accepted`) com `DemIngestionResponseDTO` (detalhes do processo criado no DEM).  
  * **Error Responses:** `500 Internal Server Error`.

* **Verificar Status de um processo de Ingestão no DEM**  
  * **Endpoint:** `GET /mdm/api/admin/ingest/status/{demJobId}`  
  * **Descrição:** Consulta o DEM para obter o status de um processo de ingestão específico.  
  * **Path Variable:** `demJobId` (Integer) \- ID do processo retornado pelo DEM.  
  * **Success Response:** `200 OK` com `DemIngestionResponseDTO`.  
  * **Error Responses:** `404 Not Found`, `500 Internal Server Error`.

* **Listar Status de Todos os processos de Ingestão no DEM**  
  * **Endpoint:** `GET /mdm/api/admin/ingest/status/all`  
  * **Descrição:** Consulta o DEM para obter uma lista com o status de todos os processos de ingestão.  
  * **Success Response:** `200 OK` com `List<DemIngestionResponseDTO>`.  
  * **Error Responses:** `500 Internal Server Error`.

### **1.4. Endpoint de Callback de Ingestão (MDM recebendo dados do DEM)**

Endpoint que o DEM chama para enviar dados processados ao MDM.  
 **Path Base:** `/countries` (conforme `CountryController.java`)

* **Receber Dados de Países Processados**  
  * **Endpoint:** `POST /countries/callback`  
  * **Descrição:** Endpoint para o DEM enviar uma lista de países que foram extraídos e transformados. O MDM processará esses dados para criar ou atualizar seus registros (upsert).  
  * **Request Body:** `List<CountryDTO>`
      ```json
        [ 
            {
            "id" : null,
            "countryName" : "Mongolia",
            "numericCode" : 496,
            "capitalCity" : "Ulan Bator",
            "population" : 3278292,
            "area" : 1564110.0,
            "currencies" : [ {
                "currencyId" : null,
                "currencyCode" : "MNT",
                "currencyName" : "Mongolian tögrög",
                "currencySymbol" : "₮"
            } ],
            "createdAt" : null,
            "updatedAt" : null
            }, {
            "id" : null,
            "countryName" : "Panama",
            "numericCode" : 591,
            "capitalCity" : "Panama City",
            "population" : 4314768,
            "area" : 75417.0,
            "currencies" : [ {
                "currencyId" : null,
                "currencyCode" : "PAB",
                "currencyName" : "Panamanian balboa",
                "currencySymbol" : "B/."
                }]
            } 
        ]
      ```
  * **Success Response:** `200 OK` com uma mensagem de sucesso (ex: "Dados de países recebidos e processados com sucesso.").  
  * **Error Responses:** `400 Bad Request`, `500 Internal Server Error`.

---

##  **2\. Microsserviço DEM (Data Extraction Management)**

Responsável por extrair dados de fontes externas, transformá-los e enviá-los ao MDM. **URL Base da API DEM:** `http://localhost:8082`

### **2.1. API de Ingestão (`IngestionController`)**

Gerencia os trabalhos de ingestão de dados. **Path Base:** `/dem/api/ingestion`

* **Solicitar Novo Processo de Ingestão**

  * **Endpoint:** `POST /dem/api/ingestion`  
  * **Descrição:** Endpoint chamado pelo MDM para solicitar que o DEM inicie um novo processo de extração e transformação para um provedor específico.  
  * **Request Body:** `IngestionRequestDTO`
      ```json
        {
          "mdmProviderId": 1,
          "mdmSyncUrl": "http://localhost:8081/countries/callback"
        }
      ```
  * **Success Response:** `202 Accepted` com `IngestionDTO` (detalhes da ingestão criado no DEM com seu ID e status inicial).  
  * **Error Responses:** `400 Bad Request`, `500 Internal Server Error`.

* **Obter Status de um processo de Ingestão**  
  * **Endpoint:** `GET /dem/api/ingestion/{id}`  
  * **Descrição:** Retorna os detalhes e o status atual de um processo de ingestão específico.  
  * **Path Variable:** `id` (Integer) \- ID do processo de ingestão no DEM.  
  * **Success Response:** `200 OK` com `IngestionDTO`.  
  * **Error Responses:** `404 Not Found`.

* **Listar Todos os processos de Ingestão**  
  * **Endpoint:** `GET /dem/api/ingestion`  
  * **Descrição:** Retorna uma lista de todos os processos de ingestão gerenciados pelo DEM.  
  * **Success Response:** `200 OK` com `List<IngestionDTO>`.

---

##  **3\. Schemas de DTOs Principais (Resumo)**

Para detalhes completos dos campos, tipos e validações, consulte as classes DTO nos respectivos projetos.

### **3.1. DTOs do MDM**

* **`CountryDTO`**  
  * Campos: `id`, `countryName`, `numericCode`, `capitalCity`, `population`, `area`, `currencies` (lista de `CurrencyDTO`), `createdAt`, `updatedAt`.

* **`CurrencyDTO`**  
  * Campos: `currencyId`, `currencyCode`, `currencyName`, `currencySymbol`.

* **`ProviderDTO`**  
  * Campos: `id`, `name`, `category`, `apiUrl`, `description`, `createdAt`, `updatedAt`.

* **`DemIngestionRequestDTO` (DTO no MDM para enviar ao DEM)**  
  * Campos: `mdmProviderId` (Integer), `mdmSyncUrl` (String).

* **`DemIngestionResponseDTO` (DTO no MDM para receber do DEM)**  
  * Campos: `id` (do job DEM), `mdmProviderId`, `status`, `rawDataPath`, `transformedDataPath`, `statusMessage`, `createdAt`, `updatedAt`.

### **3.2. DTOs do DEM**

* **`IngestionRequestDTO`** (Recebido do MDM)  
  * Campos: `mdmProviderId` (Integer), `mdmSyncUrl` (String).

* **`IngestionDTO`** (Retornado pela API do DEM)  
  * Campos: `id` (do job DEM), `mdmProviderId`, `status` (String), `rawDataPath`, `transformedDataPath`, `statusMessage`, `createdAt`, `updatedAt`.

