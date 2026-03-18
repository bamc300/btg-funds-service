# BTG Funds Service (Spring Boot)

Plataforma backend para que los clientes gestionen sus fondos de inversión sin asesor.

## Arquitectura

Estructura basada en hexagonal:

- `domain`: entidades, enums y excepciones de negocio
- `application`: puertos (input/output) y casos de uso
- `infrastructure`: adaptadores (persistencia/noificaciones), configuración y seguridad
- `entrypoints`: controladores REST y DTOs

## Modelo NoSQL (propuesto)

Ver [no-sql-model.md](docs/no-sql-model.md).

## Consultas SQL

Consulta solicitada para la BD `BTG`: [consulta.md](docs/consulta.md).

## API

Swagger UI:

- `GET /swagger-ui.html`
  - OpenAPI JSON: `GET /v3/api-docs`

Endpoints principales:

- `GET /funds`
- `POST /clients`
- `POST /clients/{clientId}/funds/{fundId}/subscriptions`
- `DELETE /clients/{clientId}/subscriptions/{subscriptionId}`
- `GET /clients/{clientId}/transactions`

Regla de saldo insuficiente:

- `No tiene saldo disponible para vincularse al fondo <Nombre del fondo>`

## Seguridad

HTTP Basic para los endpoints (Swagger y health quedan públicos).

Valores por defecto:

- usuario: `btg`
- password: `btg`

## Ejecutar en local

Requisitos:

- JDK 23
- Maven

Ejecutar:

```bash
mvn test
mvn spring-boot:run
```

Probar con el cliente por defecto `client-1` (semilla al iniciar):

```bash
curl -u btg:btg http://localhost:8080/funds
curl -u btg:btg -X POST http://localhost:8080/clients/client-1/funds/4/subscriptions -H "Content-Type: application/json" -d "{}"
curl -u btg:btg http://localhost:8080/clients/client-1/transactions
```

## Despliegue AWS (CloudFormation)

Guía (Fargate) en [aws-cloudformation.md](docs/aws-cloudformation.md).

Swagger (ejemplo en ALB):

- `http://btg-fu-loadb-umijvy5ub622-39264399.us-east-1.elb.amazonaws.com/swagger-ui/index.html#/fund-controller/list`

## Notificaciones (Email/SMS)

En este proyecto, el envío está implementado por puerto y, por defecto, usa un adaptador en memoria (no realiza llamadas externas).
