# Motor de Orquestração de Casos - (Projeto Completo)

**Autor:** Manus AI
**Data:** 29 de Janeiro de 2026

Este repositório contém o código-fonte completo e independente para o **Motor de Orquestração de Casos**. A aplicação gerencia o ciclo de vida de tickets de atendimento, expondo uma API RESTful baseada no padrão **TMF621 (Trouble Ticket)**.

---

## 1. Visão Geral do Fluxo

O objetivo deste projeto é interceptar e gerenciar a criação de casos de atendimento. O fluxo que você solicitou funciona da seguinte maneira:

1.  **Simulação do Consumidor da API criando um Caso**: Uma ferramenta como o Postman ou cURL faz uma requisição HTTP para a nossa API, simulando um 'cliente' que abre um ticket pelo aplicativo.
2.  **Motor de Orquestração (Esta API)**: A nossa aplicação, rodando em `localhost:8080`, recebe a requisição.
    - Valida os dados.
    - Salva o caso em um banco de dados em memória (H2).
    - Chama o **Salesforce Adapter** para sincronizar o caso.
3.  **Salesforce Adapter**: Este componente, dentro da nossa aplicação, faz uma chamada para a API do Salesforce.
4.  **Salesforce Mock**: Para não depender do Salesforce real, um *mock* (simulador) rodando em um container Docker em `localhost:8081` recebe a chamada e retorna uma resposta de sucesso, como se fosse o Salesforce de verdade.
5.  **Resposta Final**: O Motor de Orquestração retorna uma resposta de sucesso para o "Consumidor da API", confirmando que o ticket foi criado e fornecendo um número de protocolo.

**Conclusão:** A chamada que antes ia direto para o Salesforce agora **passa por você** (pelo motor de orquestração), que centraliza a lógica de negócio.

---

## 2. Stack Tecnológica

| Tecnologia | Versão | Propósito |
|---|---|---|
| **Java** | 21 | Linguagem principal com suporte a Virtual Threads |
| **Spring Boot** | 3.2.1 | Framework para criação da aplicação |
| **Spring WebFlux** | 3.2.1 | APIs reativas e não-bloqueantes |
| **JPA / Hibernate** | - | Mapeamento objeto-relacional para o banco de dados |
| **H2 Database** | - | Banco de dados em memória para simplicidade |
| **Resilience4j** | 2.2.0 | Padrões de resiliência (Circuit Breaker, Retry) |
| **Docker** | - | Containerização dos serviços de mock |
| **WireMock** | 3.3.1 | Simulação das APIs do Salesforce e Genesys |
| **Springdoc OpenAPI** | 2.3.0 | Documentação interativa da API (Swagger UI) |
| **Maven** | - | Gerenciamento de dependências e build |

---

## 3. Como Executar o Projeto (Passo a Passo)

### Pré-requisitos

- **Java 21** instalado (verifique com `java -version`)
- **Maven 3.9+** instalado (verifique com `mvn -version`)
- **Docker** e **Docker Compose** instalados e rodando

### Passo 1: Iniciar os Mocks (Simuladores)

No terminal, na raiz do projeto (`/home/ubuntu/motor-orquestracao-vivo/`), execute o seguinte comando:

```bash
docker-compose up -d
```

- **O que isso faz?** Inicia os containers Docker para os simuladores (mocks) do Salesforce e Genesys. O Salesforce Mock ficará disponível em `http://localhost:8081`.
- **Como verificar?** Execute `docker ps` para ver os containers `salesforce-mock` e `genesys-mock` em execução.

### Passo 2: Compilar o Projeto Java

No mesmo terminal, na raiz do projeto, compile o código-fonte com o Maven:

```bash
mvn clean install
```

- **O que isso faz?** Baixa as dependências e compila todo o código Java que eu gerei, criando um pacote executável.

### Passo 3: Executar a Aplicação (O Motor de Orquestração)

Agora, inicie a aplicação Spring Boot:

```bash
mvn spring-boot:run
```

- **O que isso faz?** Inicia o seu **Motor de Orquestração**. A API estará pronta para receber requisições em `http://localhost:8080`.
- **Como verificar?** Você verá o logo do Spring Boot no terminal e logs indicando que a aplicação iniciou na porta 8080.

---

## 4. Como Testar: Simulando a Chamada do Consumidor da API

Com os mocks e a sua aplicação rodando, vamos simular a criação de um ticket, como se viesse do Consumidor da API.

Abra um **novo terminal** e execute o comando `curl` abaixo. Este comando é o equivalente ao Consumidor da API chamando a sua API.

```bash
curl -X POST http://localhost:8080/tmf-api/troubleTicket/v4/troubleTicket \
  -H "Content-Type: application/json" \
  -d 
  {
    "name": "Problema com fatura",
    "description": "Minha fatura deste mês veio com o dobro do valor esperado.",
    "ticketType": "Reclamação",
    "priority": "High",
    "severity": "Major",
    "channel": {
      "id": "app",
      "name": "Consumidor da API"
    },
    "relatedParty": [
      {
        "@referredType": "Contact",
        "id": "003xx000004TmiAAE",
        "name": "João Silva"
      }
    ]
  }

```

### O que Aconteceu?

1.  **Sua API recebeu a requisição**: No terminal onde a aplicação Java está rodando, você verá logs como:
    ```
    INFO c.v.c.i.r.TroubleTicketController    : 📨 POST /troubleTicket - Criando ticket: Problema com fatura
    INFO c.v.c.a.service.CaseService          : 🎫 Iniciando criação de caso: Problema com fatura
    INFO c.v.c.a.service.CaseService          : 💾 Caso salvo localmente: protocol=VIVO-167501... 
    INFO c.v.c.i.a.s.SalesforceAdapter        : 📤 Enviando caso para Salesforce: Problema com fatura
    INFO c.v.c.i.a.s.SalesforceAdapter        : ✅ Caso criado no Salesforce: ID=500xx000000...
    INFO c.v.c.a.service.CaseService          : ✅ Caso sincronizado com Salesforce: sfId=500xx000000...
    INFO c.v.c.i.r.TroubleTicketController    : ✅ Ticket criado: VIVO-167501...
    ```

2.  **Você receberá uma resposta no terminal do `curl`**: A resposta será um JSON confirmando a criação, parecido com isto:
    ```json
    {
      "id": "VIVO-167501...",
      "href": "/tmf-api/troubleTicket/v4/troubleTicket/VIVO-167501...",
      "name": "Problema com fatura",
      "description": "Minha fatura deste mês veio com o dobro do valor esperado.",
      "ticketType": "Reclamação",
      "priority": "High",
      "severity": "Major",
      "status": "new",
      "creationDate": "2026-01-29T18:30:00.000Z",
      "lastUpdate": "2026-01-29T18:30:00.000Z",
      "salesforceCaseId": "500xx000000...",
      "protocol": "VIVO-167501..."
    }
    ```

**Parabéns! Você acabou de orquestrar a criação de um caso, interceptando a chamada e controlando o fluxo.**

---

## 5. Links Úteis

- **Documentação da API (Swagger UI)**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
  - Aqui você pode explorar e testar todos os endpoints da API de forma interativa.

- **Console do Banco de Dados (H2)**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - **JDBC URL**: `jdbc:h2:mem:casemanagement`
  - **User Name**: `sa`
  - **Password**: (deixe em branco)
  - Use o console para executar queries SQL e ver os dados salvos na tabela `CASES`.

- **Logs do Salesforce Mock**: Para ver as requisições que o seu motor fez para o Salesforce, verifique os logs do container Docker:
  ```bash
  docker logs salesforce-mock
  ```

## 6. Para Encerrar

- Para parar a aplicação Java, pressione `Ctrl + C` no terminal onde ela está rodando.
- Para parar os mocks, execute `docker-compose down` na raiz do projeto.
