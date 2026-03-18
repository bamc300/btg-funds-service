# Despliegue AWS (Fargate)

Este repo queda estandarizado para despliegue en **ECS Fargate**, con logs en **CloudWatch** y secretos en **SSM Parameter Store (SecureString)**.

## Plantillas usadas

- Red (VPC + subnets públicas): `cloudformation/00-network.yml`
- DynamoDB (single-table): `cloudformation/01-data-dynamodb.yml`
- Aplicación (ECS Fargate + logs + ALB opcional): `cloudformation/03-app-fargate.yml`

## Despliegue (Git Bash)

Script: `scripts/aws/bootstrap-fargate.bash`

Ejemplo (Gmail SMTP + ALB):

```bash
cd /c/git/btg-funds-service

export AWS_REGION="us-east-1"
export EXPOSE_WITH_ALB="true"

export NOTIFICATIONS_MODE="SMTP"
export BTG_NOTIFICATIONS_SMTP_USERNAME="monkyfonso@gmail.com"

bash ./scripts/aws/bootstrap-fargate.bash
```

El script:

- crea repo ECR si no existe, build/push de la imagen
- despliega Network + DynamoDB + Fargate
- guarda passwords en SSM (te los pide por consola)
- muestra logs recientes con `aws logs tail`

## Swagger

- Ruta: `/swagger-ui/index.html`
- Ejemplo (ALB): `http://btg-fu-loadb-umijvy5ub622-39264399.us-east-1.elb.amazonaws.com/swagger-ui/index.html#/fund-controller/list`

Si ves `Failed to load remote configuration`, prueba:

- `http://<ALB_DNS>/v3/api-docs` (debe responder)
- revisa logs con: `aws logs tail /btg/funds-service/dev --since 10m --region us-east-1`

## Limpieza (Fargate)

Script: `scripts/aws/cleanup-fargate.bash`

```bash
cd /c/git/btg-funds-service
export AWS_REGION="us-east-1"
bash ./scripts/aws/cleanup-fargate.bash
```

Borrar también ECR:

```bash
export DELETE_ECR_REPO="true"
bash ./scripts/aws/cleanup-fargate.bash
```

Borrar también parámetros SSM:

```bash
export DELETE_SSM_PARAMS="true"
bash ./scripts/aws/cleanup-fargate.bash
```
