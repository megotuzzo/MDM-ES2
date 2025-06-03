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
