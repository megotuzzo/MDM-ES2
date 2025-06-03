##Primeiro, segue a explicação de funcionamento geral do fluxo e lógica, sendo possível acompanhar pelos logs e banco de dados H2. 
##Em seguida, segue o passo a passo para fazer a execução do fluxo de forma manual, fazendo cada requisição individualmente.

---

# Fluxo Automático End-to-End

Este guia descreve os passos para iniciar as aplicações **MDM** (Sistema de Gestão de Dados Mestres) e **DEM** (Extração de Dados), cadastrar um provedor de dados no MDM e disparar o processo de ingestão de dados, que será executado pelo DEM.

---

## ✅ Pré-requisitos

- **Java Development Kit (JDK)**: Versão 21 ou compatível instalada.
- **Maven**: Instalado e configurado (para compilar os projetos, se necessário).
- **Aplicações Compiladas**: Os arquivos `.jar` dos projetos MDM e DEM devem estar gerados (ex: via `mvn clean package`).
- **Ferramenta para Requisições HTTP**: Postman, Insomnia ou `curl` para interagir com as APIs.

---

## 🚀 Passo 1: Iniciar as Aplicações

Você precisará iniciar **ambas** as aplicações (MDM e DEM) em terminais separados.

### Iniciar o Microserviço MDM

1. Navegue até o diretório onde está o arquivo `.jar` do seu projeto **MDM**.
2. Execute a aplicação (ex: classe MdmApplication.java ou `java -jar mdm-application.jar`).
3. Por padrão, o MDM será iniciado na porta `8081`.

### Iniciar o Microserviço DEM

1. Navegue até o diretório onde está o arquivo `.jar` do seu projeto **DEM**.
2. Execute a aplicação (ex: classe DemApplication.java ou `java -jar dem-application.jar`).
3. Por padrão, o DEM será iniciado na porta `8082`.

> Aguarde as duas aplicações iniciarem completamente. Você pode acompanhar os logs no console.

---

## 🧾 Passo 2: Cadastrar um Provedor de Dados no MDM

Antes que o DEM possa buscar dados, o MDM precisa saber de qual provedor extrair.

### Exemplo: Cadastro da API RestCountries

- **Ferramenta**: Postman (ou similar)
- **Método HTTP**: `POST`
- **URL**: `http://localhost:8081/mdm/api/providers`

#### Corpo da Requisição (JSON):

```json
{
  "name": "RestCountries API",
  "category": "Dados Geográficos de Países",
  "apiUrl": "https://restcountries.com/v3.1",
  "description": "API pública para obter informações sobre países."
}
```

### O que esperar:

- Resposta HTTP `201 Created`
- O corpo da resposta conterá os detalhes do provedor cadastrado, incluindo um `id` gerado pelo MDM.  
> Anote esse `id`, pois será necessário no próximo passo.

---

## 📥 Passo 3: Disparar o Processo de Ingestão de Dados via MDM

Com o provedor cadastrado, você pode solicitar que o MDM inicie o processo de ingestão.

- **Método HTTP**: `POST`
- **URL**:  
  `http://localhost:8081/mdm/api/admin/ingest/provider/{providerId}`  
  Substitua `{providerId}` pelo `id` do provedor (ex: `1`)

- **Corpo da Requisição**: não é necessário

### O que esperar:

- Resposta HTTP `200 OK` ou `202 Accepted`
- O corpo da resposta conterá:
  - ID do job no DEM (`demJobId`)
  - Status inicial (geralmente `PENDING`)
> Anote o `demJobId` para verificações posteriores.

---

## 🔄 Passo 4: Acompanhar o Fluxo Automático

### Fluxo de Execução

1. **MDM chama o DEM**:
   - `DemIntegrationService` envia solicitação para `POST /dem/api/ingestion`

2. **DEM processa (assíncrono)**:
   - Busca os detalhes do provedor no MDM
   - Busca os dados da API externa (RestCountries)
   - Salva os dados brutos em `./data/dem/raw/`
   - Transforma os dados
   - Salva os dados transformados em `./data/dem/transformed/`
   - Atualiza o status do job para `READY`
   - Envia os dados transformados para o endpoint de callback do MDM:
     - `http://localhost:8081/countries/callback`
   - Atualiza o status final do job para `COMPLETED` ou `FAILED`

3. **MDM recebe dados**:
   - `CountryController` recebe os dados no callback
   - `CountryService.processAndSaveCountries` persiste os dados no banco de dados

---

## 🧪 Verificação do Processo

### Logs

- Acompanhe os consoles das aplicações MDM e DEM para verificar cada etapa.

### Status via API do MDM

- Verificar status de um job específico:

```http
GET http://localhost:8081/mdm/api/admin/ingest/status/{demJobId}
```

- Verificar todos os jobs:

```http
GET http://localhost:8081/mdm/api/admin/ingest/status/all
```

### Verificação de Arquivos

- Diretórios utilizados pelo DEM:
  - Dados brutos: `/data/dem/raw`
  - Dados transformados: `/data/dem/transformed`

### Verificação nos Bancos de Dados H2

- **Console H2 do MDM**:  
  `http://localhost:8081/h2-mdm-console`
    username: sa_mdm
    password:
  Verifique as tabelas: `COUNTRY` e `CURRENCY`

- **Console H2 do DEM**:  
  `http://localhost:8082/h2-dem-console`
    username: sa_dem
    password:
  Verifique a tabela: `INGESTION`

---

## 📎 Observações

- Certifique-se de que as portas `8081` e `8082` estejam livres antes de iniciar as aplicações.
- Verifique as configurações de caminhos nos `application.properties`.

---

##Segue o teste manual:

# 🧪 Fluxo de Teste Manual: Integração MDM & DEM

Este guia detalha como executar e testar manualmente o fluxo de integração entre os microsserviços **MDM (Master Data Management)** e **DEM (Data Extraction Management)** utilizando o Postman para realizar as requisições HTTP.

---

## 🛠️ Pré-requisitos

* **Aplicações em Execução:**
    * O microsserviço **MDM** deve estar rodando (na porta `8081`).
    * O microsserviço **DEM** deve estar rodando (na porta `8082`).
* **Postman (ou similar):** Instalado e pronto para uso.

---

## ⚙️ Configurações

Os arquivos `application.properties` de cada serviço informam a base das URLs necessárias para a execução do projeto. Verifique as seguintes propriedades:

* **No DEM:**
    * `mdm.api.base-url`
* **No MDM:**
    * `dem.api.base-url`
    * `mdm.callback.base-url`

---

## 🚀 Passo a Passo da Execução Manual

Siga as etapas abaixo para testar o fluxo completo:

### Etapa 1: (MDM) Cadastrar um Provedor de Dados

**Objetivo:** Registrar a fonte de dados externa (`restcountries`) no MDM.

**Requisição (Postman):**
* **Método:** `POST`
* **URL:** `http://localhost:8081/mdm/api/providers`
* **Headers:** `Content-Type: application/json`
* **Body (JSON):**
    ```json
    {
      "name": "RestCountries API",
      "category": "Dados Geográficos Globais",
      "apiUrl": "[https://restcountries.com/v3.1](https://restcountries.com/v3.1)",
      "description": "API pública para informações detalhadas sobre países."
    }
    ```

**Verificação:** ✅
* Resposta HTTP `201 Created` do MDM.
* O corpo da resposta deve conter o provedor criado, incluindo seu `id`. **Anote este `id`** (ex: `1`).
* (Opcional) Verifique o H2 Console do MDM (`http://localhost:8081/h2-mdm-console`) e a tabela `PROVIDER`.

---

### Etapa 2: (MDM) Disparar a Solicitação de Ingestão para o DEM

**Objetivo:** Instruir o MDM a pedir para o DEM processar os dados do provedor cadastrado.

**Requisição (Postman):**
* **Método:** `POST`
* **URL:** `http://localhost:8081/mdm/api/admin/ingest/provider/{providerId}`
    * Substitua `{providerId}` pelo `id` obtido na Etapa 1 (ex: `http://localhost:8081/mdm/api/admin/ingest/provider/1`).
    * *Este endpoint é servido pelo `AdminController.java` do MDM.*
* **Body:** Não é necessário.

**Verificação:** ✅
* O MDM deve responder com os detalhes iniciais do job de ingestão criado no DEM (um `DemIngestionResponseDTO` com o `id` do job no DEM e status inicial `PENDING`). **Anote o `id` do job do DEM** (ex: `34`).
* **Logs do MDM:** 📄 Devem mostrar a chamada ao `DemIntegrationService.requestDataIngestion` e a tentativa de chamar `POST /dem/api/ingestion` do DEM.
* **Logs do DEM:** 📄 Devem mostrar o recebimento da chamada no `IngestionController.requestIngestion` e a criação do registro `Ingestion` com status `PENDING`.

---

### Etapa 3: (DEM) Acompanhar o Processamento ETL

**Objetivo:** Verificar se o DEM executa todas as etapas de extração e transformação. O método `startExtractionProcess` no `IngestionService` do DEM deve estar sendo executado (em background devido ao `@Async`).

**Verificação (principalmente via Logs e Banco de Dados do DEM):** ✅
* **Logs do DEM:** 📄
    * Status do job `Ingestion` mudando para `PROCESSING`.
    * Chamada para `GET /mdm/api/providers/{providerId}` do MDM para buscar a `apiUrl`.
    * Chamada para a API externa (ex: `https://restcountries.com/v3.1/all`).
    * Mensagem "Dados brutos recebidos..." e "Dados brutos salvos em: `/data/dem/raw`".
    * Mensagem "Iniciando transformação..." e "{X} países transformados...".
    * Mensagem "Dados transformados salvos em: `/data/dem/transformed`".
    * Status do job `Ingestion` mudando para `READY`.
* **Sistema de Arquivos (DEM):** 🗂️ Verifique os diretórios configurados em `dem.storage.base-path` (`data/dem/raw/` e `data/dem/transformed/`) para os arquivos JSON.
* **Banco de Dados do DEM:** 💾 No H2 Console do DEM (`http://localhost:8082/h2-dem-console`), consulte a tabela `INGESTION` e verifique se o job (id anotado na Etapa 2) está com `STATUS = 'READY'` e os campos `RAW_DATA_PATH` e `TRANSFORMED_DATA_PATH` preenchidos.

---

### Etapa 4: (DEM) Envio dos Dados para o MDM e Finalização do Job

**Objetivo:** Verificar se o DEM, após transformar os dados e mudar o status para `READY`, envia esses dados para o endpoint de callback do MDM.

**Verificação (Logs do DEM e do MDM):** ✅
* **Logs do DEM:** 📄
    * Mensagem indicando "Tentando enviar para MDM na URL: `{mdmSyncUrl}`".
    * (Se sucesso) Mensagem "MDM processou os dados com sucesso..." e status do job `Ingestion` mudando para `COMPLETED`.
    * (Se falha) Mensagem de erro na comunicação com o MDM ou erro retornado pelo MDM, e status do job `Ingestion` mudando para `FAILED`.
* **Logs do MDM:** 📄
    * Recebimento da requisição `POST` no endpoint `/countries/callback`.
    * Execução do `CountryService.processAndSaveCountries`.
    * Mensagens de sucesso ou erro durante a persistência dos dados no MDM.
* **Banco de Dados do MDM:** 💾 Verifique as tabelas `COUNTRY` e `CURRENCY` para confirmar se os dados dos países foram criados ou atualizados.
* **Banco de Dados do DEM:** 💾 Consulte novamente o job na tabela `INGESTION`. O status deve ser `COMPLETED` (ou `FAILED` se ocorreu algum problema).

---

### Etapa 5: (MDM) Consultar Status de Ingestões (Opcional)

Você pode usar os endpoints do `AdminController.java` do MDM para verificar os status das ingestões no DEM:

* **Consultar uma ingestão específica:**
    * **Método:** `GET`
    * **URL:** `http://localhost:8081/mdm/api/admin/ingest/status/{demJobId}`
        * Substitua `{demJobId}` pelo `id` do job do DEM anotado na Etapa 2.
* **Consultar todas as ingestões:**
    * **Método:** `GET`
    * **URL:** `http://localhost:8081/mdm/api/admin/ingest/status/all`

---
