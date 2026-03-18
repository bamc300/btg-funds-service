# Modelo de datos NoSQL (seleccionado)

## DynamoDB (single-table)

Tabla: `btg-funds-<env>`

La tabla se particiona por cliente para lecturas eficientes de suscripciones y transacciones.

### Tipos de ítems

1) Cliente

- `PK = CLIENT#<clientId>`
- `SK = PROFILE`
- Atributos: `clientId`, `balanceCop`, `notificationPreference`, `email`, `phone`

2) Suscripción

- `PK = CLIENT#<clientId>`
- `SK = SUBSCRIPTION#<subscriptionId>`
- Atributos: `subscriptionId`, `clientId`, `fundId`, `amountCop`, `status`, `createdAt`, `cancelledAt`

3) Transacción (histórico)

- `PK = CLIENT#<clientId>`
- `SK = TX#<occurredAtISO>#<transactionId>`
- Atributos: `txId`, `clientId`, `fundId`, `txType`, `amountCop`, `subscriptionId`, `occurredAt`

### Consultas típicas

- Suscripciones activas del cliente: `PK=CLIENT#<id>` y `begins_with(SK, 'SUBSCRIPTION#')`, filtrando `status=ACTIVE`
- Historial del cliente: `PK=CLIENT#<id>` y `begins_with(SK, 'TX#')` (ordenable por fecha por el prefijo ISO en `SK`)

### Catálogo de fondos

El catálogo de fondos es estático y se resuelve en la aplicación (no se persiste en DynamoDB).
