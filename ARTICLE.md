# SmallRye Fault Tolerance num gateway Pix que não pode falhar no susto

Eu tenho usado essa série para pegar uma extensão do Quarkus por vez e forçar um cenário que faria sentido fora de laboratório. Dessa vez fui numa peça que muita gente lembra só quando o provider começa a oscilar em produção.

## O cenário que eu montei

A aplicação simula um gateway Pix de fintech recebendo pedidos de cobrança e chamando um provedor externo de autorização. Quando esse provedor fica instável, a API não pode simplesmente responder erro para tudo e deixar o time operacional apagar incêndio no braço.

Para esse lab, o fluxo ficou assim:

- cobrança comum tenta novamente e segue o jogo
- cobrança mais alta não insiste para sempre
- quando o risco operacional aumenta, a resposta volta como análise manual

Foi aí que `quarkus-smallrye-fault-tolerance` fez sentido de verdade.

## Estrutura do projeto

```text
.
├── README.md
├── ARTICLE.md
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── dev
│   │   │       └── dailylab
│   │   │           └── payments
│   │   │               ├── PixChargeRequest.java
│   │   │               ├── PixChargeResource.java
│   │   │               ├── PixChargeResult.java
│   │   │               ├── PixChargeService.java
│   │   │               └── PixGatewayClient.java
│   │   └── resources
│   │       └── application.properties
│   └── test
│       └── java
│           └── dev
│               └── dailylab
│                   └── payments
│                       └── PixChargeResourceTest.java
└── mvnw
```

## Onde a extensão entrou de verdade

O coração do lab está no serviço. A chamada externa foi protegida com retry, timeout, circuit breaker e fallback no mesmo método.

```java
@Retry(maxRetries = 2, delay = 200, delayUnit = ChronoUnit.MILLIS)
@Timeout(value = 400, unit = ChronoUnit.MILLIS)
@CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.75, delay = 2, delayUnit = ChronoUnit.SECONDS)
@Fallback(fallbackMethod = "reserveForManualReview")
public PixChargeResult process(PixChargeRequest request) {
    int attempt = gatewayClient.nextAttempt();
    if (request.amount().compareTo(new BigDecimal("1500.00")) > 0) {
        throw new IllegalStateException("provider timeout on high-value pix charge");
    }
    if (attempt < 3) {
        throw new IllegalStateException("temporary provider instability on attempt " + attempt);
    }
    return new PixChargeResult(
            "APPROVED",
            "primary-pix-provider",
            request.amount(),
            "Charge approved after retry strategy stabilized the provider call",
            request.correlationId());
}
```

O que eu gosto aqui é que a regra fica visível. Não tem controller fazendo malabarismo, nem exception espalhada em três camadas diferentes. A decisão operacional está no ponto em que a falha realmente importa.

O endpoint HTTP ficou enxuto:

```java
@Path("/pix-charges")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PixChargeResource {

    private final PixChargeService service;

    public PixChargeResource(PixChargeService service) {
        this.service = service;
    }

    @POST
    public PixChargeResult create(PixChargeRequest request) {
        return service.process(request);
    }
}
```

## Configuração usada

A configuração ficou pequena porque o objetivo era mostrar a extensão resolvendo um caso concreto, não montar um castelo de properties sem necessidade.

```properties
quarkus.swagger-ui.always-include=true
quarkus.smallrye-openapi.info-title=Lab SmallRye Fault Tolerance
quarkus.smallrye-openapi.info-version=1.0.0
quarkus.smallrye-openapi.info-description=API de fallback para cobrancas Pix em um contexto de antifraude e estabilidade de gateway.
```

## Como rodar e testar

Subindo localmente:

```bash
./mvnw quarkus:dev
```

Cobrança que passa depois do retry:

```bash
curl -X POST http://localhost:8080/pix-charges \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "cust-01",
    "amount": 1200.00,
    "correlationId": "pix-001"
  }'
```

Cobrança que desvia para análise manual:

```bash
curl -X POST http://localhost:8080/pix-charges \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "cust-02",
    "amount": 2500.00,
    "correlationId": "pix-002"
  }'
```

Swagger:

- `http://localhost:8080/q/swagger-ui`

## O que eu tirei desse lab

Essa extensão ganha muito quando o exemplo deixa de ser acadêmico. No caso da cobrança Pix, retry e fallback não são enfeite: eles viram uma decisão de operação. Cobrança pequena pode insistir um pouco. Cobrança maior precisa preservar o sistema e cair para outro fluxo.

Também ficou claro que esse tipo de lab funciona melhor quando o código é curto, o cenário é específico e o teste cobre a resposta final. A leitura fica muito mais honesta.

## Link pro GitHub

Aplicação pública:

<https://github.com/dellamas/quarkus-smallrye-fault-tolerance>

Gostou e quer ver mais conteúdos como este? Então deixa uma ⭐ no repositório.

No LinkedIn eu sempre posto sobre eventos, comunidade Java e Quarkus:

[💼 LinkedIn](https://www.linkedin.com/in/luisfabriciodellamas/)

E o artigo deste projeto está no dev.to:

[✍️ dev.to](https://dev.to/dellamas)
