# Modelo de datos NoSQL

El sistema necesita:

- Clientes con saldo y preferencia de notificación
- Catálogo de fondos (estático)
- Suscripciones activas/canceladas
- Historial de transacciones (aperturas y cancelaciones)

## Opción A: DynamoDB (single-table)

Tabla: `btg-funds`

Particiones por cliente para lecturas eficientes (suscripciones y transacciones).

### Tipos de ítems

1) Cliente

- `PK = CLIENT#<clientId>`
- `SK = PROFILE`
- Atributos: `balanceCop`, `notificationPreference`, `email`, `phone`

2) Suscripción

- `PK = CLIENT#<clientId>`
- `SK = SUBSCRIPTION#<subscriptionId>`
- Atributos: `fundId`, `amountCop`, `status`, `createdAt`, `cancelledAt`

3) Transacción (histórico)

- `PK = CLIENT#<clientId>`
- `SK = TX#<occurredAtISO>#<transactionId>`
- Atributos: `fundId`, `type`, `amountCop`, `subscriptionId`

4) Fondos (catálogo)

- `PK = FUND`
- `SK = <fundId>`
- Atributos: `name`, `minimumAmountCop`, `category`

### Consultas típicas

- Suscripciones activas del cliente: `PK=CLIENT#id` y filtrar `SK` por `SUBSCRIPTION#` + `status=ACTIVE`
- Historial: `PK=CLIENT#id` y filtrar `SK` por `TX#` (ordenable por fecha)
- Fondo por id: `PK=FUND` y `SK=<fundId>`

## Opción B: MongoDB (colecciones)

- `clients` (1 documento por cliente)
- `funds` (1 documento por fondo)
- `subscriptions` (1 documento por suscripción)
- `transactions` (append-only)

