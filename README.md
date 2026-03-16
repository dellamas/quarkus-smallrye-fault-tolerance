# Lab SmallRye Fault Tolerance

Esse lab nasceu de uma dor bem comum em fintech: cobrança Pix que depende de provedor externo e começa a falhar no pior momento. Em vez de deixar a API devolver erro de qualquer jeito quando a autorização oscila, a ideia aqui foi montar um fluxo pequeno, testável e com comportamento previsível.

## O que é

A aplicação recebe uma cobrança Pix e protege a chamada principal com retry, timeout, circuit breaker e fallback. Na prática, cobrança menor tenta de novo até estabilizar. Cobrança mais sensível desvia para análise manual antes de virar incidente na operação.

## Stack

- Java 21
- Quarkus
- SmallRye Fault Tolerance
- REST Jackson
- SmallRye OpenAPI
- JUnit 5 + Rest Assured

## Como rodar

```bash
./mvnw quarkus:dev
```

Depois disso:

- API: `http://localhost:8080/pix-charges`
- Swagger UI: `http://localhost:8080/q/swagger-ui`
- OpenAPI: `http://localhost:8080/q/openapi`

## Estrutura

- `PixChargeResource` recebe a cobrança
- `PixChargeService` concentra retry, timeout, circuit breaker e fallback
- `PixGatewayClient` simula a instabilidade do provedor
- `PixChargeResourceTest` cobre aprovação e desvio para análise manual

## Configuração

O `application.properties` ficou simples porque o ponto aqui era mostrar o comportamento da extensão em cima de um fluxo real de cobrança.

```properties
quarkus.swagger-ui.always-include=true
quarkus.smallrye-openapi.info-title=Lab SmallRye Fault Tolerance
quarkus.smallrye-openapi.info-description=API de fallback para cobrancas Pix em um contexto de antifraude e estabilidade de gateway.
```

## Testes

```bash
./mvnw test
```

Cobrança que passa depois das tentativas de retry:

```bash
curl -X POST http://localhost:8080/pix-charges \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "cust-01",
    "amount": 1200.00,
    "correlationId": "pix-001"
  }'
```

Cobrança que cai no fallback:

```bash
curl -X POST http://localhost:8080/pix-charges \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "cust-02",
    "amount": 2500.00,
    "correlationId": "pix-002"
  }'
```

Esse projeto faz parte da série de labs em que cada extensão tenta resolver um problema de verdade, sem firula e sem exemplo genérico montado só para caber no slide.

Gostou e quer ver mais conteúdos como este? Então deixa uma ⭐ no repositório.

No LinkedIn eu sempre posto sobre eventos, comunidade Java e Quarkus:

[💼 LinkedIn](https://www.linkedin.com/in/luisfabriciodellamas/)

E o artigo deste projeto está no dev.to:

[✍️ dev.to](https://dev.to/dellamas)

A versão em markdown que eu usei como base também ficou separada no repositório da série.
