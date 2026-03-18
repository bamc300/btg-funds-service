set -euo pipefail

cd "$(dirname "$0")/../.."

export MSYS2_ARG_CONV_EXCL="*"
export MSYS_NO_PATHCONV=1

ENVIRONMENT_NAME="${ENVIRONMENT_NAME:-dev}"
REGION="${AWS_REGION:-${AWS_DEFAULT_REGION:-}}"
USE_NETWORK_STACK="${USE_NETWORK_STACK:-true}"
ECR_REPOSITORY_NAME="${ECR_REPOSITORY_NAME:-btg-funds-service}"
IMAGE_TAG="${IMAGE_TAG:-latest}"

EXPOSE_WITH_ALB="${EXPOSE_WITH_ALB:-false}"

PERSISTENCE_TYPE="${PERSISTENCE_TYPE:-DYNAMODB}"

NOTIFICATIONS_MODE="${NOTIFICATIONS_MODE:-SMTP}"
SMTP_HOST="${BTG_NOTIFICATIONS_SMTP_HOST:-smtp.gmail.com}"
SMTP_PORT="${BTG_NOTIFICATIONS_SMTP_PORT:-587}"
SMTP_USERNAME="${BTG_NOTIFICATIONS_SMTP_USERNAME:-}"

SSM_BASIC_AUTH_PASSWORD_PARAM="${SSM_BASIC_AUTH_PASSWORD_PARAM:-/btg/funds/${ENVIRONMENT_NAME}/basicAuthPassword}"
SSM_SMTP_PASSWORD_PARAM="${SSM_SMTP_PASSWORD_PARAM:-/btg/funds/${ENVIRONMENT_NAME}/smtpPassword}"

SES_FROM_EMAIL="${SES_FROM_EMAIL:-}"

if [[ -z "$REGION" ]]; then
  echo "Define AWS_REGION o AWS_DEFAULT_REGION" >&2
  exit 1
fi

normalize_ssm_param_name() {
  local name="$1"
  name="$(echo -n "$name" | tr -d '\r' | sed 's/[[:space:]]*$//')"
  if [[ -z "$name" ]]; then
    echo "" && return 0
  fi
  if [[ "${name:0:1}" != "/" ]]; then
    name="/$name"
  fi
  echo -n "$name"
}

validate_ssm_param_name() {
  local name="$1"
  if [[ -z "$name" ]]; then
    echo "Nombre de parámetro SSM vacío" >&2
    exit 1
  fi
  if [[ "${name:0:1}" != "/" ]]; then
    echo "El nombre de parámetro SSM debe empezar con '/': $name" >&2
    exit 1
  fi
  if [[ ! "$name" =~ ^/[a-zA-Z0-9_.\-/]+$ ]]; then
    echo "Nombre de parámetro SSM inválido (solo / a-z A-Z 0-9 _ . -): $name" >&2
    exit 1
  fi
}

aws_cmd() {
  if command -v aws >/dev/null 2>&1; then
    MSYS2_ARG_CONV_EXCL="*" MSYS_NO_PATHCONV=1 aws "$@"
    return
  fi
  local aws_exe="/c/Program Files/Amazon/AWSCLIV2/aws.exe"
  if [[ -f "$aws_exe" ]]; then
    MSYS2_ARG_CONV_EXCL="*" "$aws_exe" "$@"
    return
  fi
  local aws_exe_bin="/c/Program Files/Amazon/AWSCLIV2/bin/aws.exe"
  if [[ -f "$aws_exe_bin" ]]; then
    MSYS2_ARG_CONV_EXCL="*" "$aws_exe_bin" "$@"
    return
  fi
  echo "No se encontró AWS CLI (aws)." >&2
  exit 1
}

docker_cmd() {
  if command -v docker >/dev/null 2>&1; then
    docker "$@"
    return
  fi
  local docker_exe="/c/Program Files/Docker/Docker/resources/bin/docker.exe"
  if [[ -f "$docker_exe" ]]; then
    "$docker_exe" "$@"
    return
  fi
  echo "No se encontró Docker CLI (docker)." >&2
  exit 1
}

stack_status() {
  local name="$1"
  aws_cmd cloudformation describe-stacks --stack-name "$name" --query "Stacks[0].StackStatus" --output text --region "$REGION" 2>/dev/null || true
}

delete_stack_if_rollback_complete() {
  local name="$1"
  local status
  status="$(stack_status "$name")"
  if [[ "$status" == "ROLLBACK_COMPLETE" ]]; then
    echo "Stack $name está en ROLLBACK_COMPLETE. Eliminando para re-crear..."
    aws_cmd cloudformation delete-stack --stack-name "$name" --region "$REGION"
    aws_cmd cloudformation wait stack-delete-complete --stack-name "$name" --region "$REGION" || true
  fi
}

cfn_deploy() {
  local name="$1"
  shift
  delete_stack_if_rollback_complete "$name"
  aws_cmd cloudformation deploy --stack-name "$name" --region "$REGION" "$@"
}

account_id="$(aws_cmd sts get-caller-identity --query Account --output text --region "$REGION")"
if [[ -z "$account_id" ]]; then
  echo "No se pudo resolver AccountId (revisa credenciales AWS)." >&2
  exit 1
fi

aws_cmd ecr describe-repositories --repository-names "$ECR_REPOSITORY_NAME" --region "$REGION" >/dev/null 2>&1 || \
  aws_cmd ecr create-repository --repository-name "$ECR_REPOSITORY_NAME" --region "$REGION" >/dev/null

ecr_uri="$account_id.dkr.ecr.$REGION.amazonaws.com"
image_uri="$ecr_uri/$ECR_REPOSITORY_NAME:$IMAGE_TAG"

aws_cmd ecr get-login-password --region "$REGION" | docker_cmd login --username AWS --password-stdin "$ecr_uri" >/dev/null
docker_cmd build -t "$image_uri" .
docker_cmd push "$image_uri"

stack_network="btg-funds-network-$ENVIRONMENT_NAME"
stack_data="btg-funds-data-$ENVIRONMENT_NAME"
stack_app="btg-funds-app-fargate-$ENVIRONMENT_NAME"

SSM_BASIC_AUTH_PASSWORD_PARAM="$(normalize_ssm_param_name "$SSM_BASIC_AUTH_PASSWORD_PARAM")"
SSM_SMTP_PASSWORD_PARAM="$(normalize_ssm_param_name "$SSM_SMTP_PASSWORD_PARAM")"
validate_ssm_param_name "$SSM_BASIC_AUTH_PASSWORD_PARAM"
validate_ssm_param_name "$SSM_SMTP_PASSWORD_PARAM"

echo "SSM BasicAuth param: $SSM_BASIC_AUTH_PASSWORD_PARAM"
echo "SSM SMTP param:     $SSM_SMTP_PASSWORD_PARAM"

if [[ "$USE_NETWORK_STACK" == "true" ]]; then
  cfn_deploy "$stack_network" \
    --template-file "cloudformation/00-network.yml" \
    --parameter-overrides "EnvironmentName=$ENVIRONMENT_NAME" \
    --capabilities CAPABILITY_NAMED_IAM
fi

cfn_deploy "$stack_data" \
  --template-file "cloudformation/01-data-dynamodb.yml" \
  --parameter-overrides \
    "EnvironmentName=$ENVIRONMENT_NAME" \
    "TableName=btg-funds" \
    "BillingMode=PROVISIONED" \
    "ReadCapacityUnits=1" \
    "WriteCapacityUnits=1"

echo "Creando/actualizando secretos en SSM Parameter Store (SecureString)."

if aws_cmd ssm get-parameter --name "$SSM_BASIC_AUTH_PASSWORD_PARAM" --with-decryption --region "$REGION" >/dev/null 2>&1; then
  :
else
  echo "Define password para Basic Auth (usuario btg)."
  read -r -s -p "BTG_BASIC_AUTH_PASSWORD: " basic_pass
  echo ""
  aws_cmd ssm put-parameter --name "$SSM_BASIC_AUTH_PASSWORD_PARAM" --type SecureString --value "$basic_pass" --overwrite --region "$REGION" >/dev/null || {
    echo "Error creando parámetro SSM: $SSM_BASIC_AUTH_PASSWORD_PARAM" >&2
    exit 1
  }
fi

if [[ "$NOTIFICATIONS_MODE" == "SMTP" ]]; then
  if [[ -z "$SMTP_USERNAME" ]]; then
    echo "Define BTG_NOTIFICATIONS_SMTP_USERNAME (por ejemplo tu Gmail)" >&2
    exit 1
  fi

  if aws_cmd ssm get-parameter --name "$SSM_SMTP_PASSWORD_PARAM" --with-decryption --region "$REGION" >/dev/null 2>&1; then
    :
  else
    echo "Define password SMTP (App Password)."
    read -r -s -p "SMTP_PASSWORD: " smtp_pass
    echo ""
    aws_cmd ssm put-parameter --name "$SSM_SMTP_PASSWORD_PARAM" --type SecureString --value "$smtp_pass" --overwrite --region "$REGION" >/dev/null || {
      echo "Error creando parámetro SSM: $SSM_SMTP_PASSWORD_PARAM" >&2
      exit 1
    }
  fi
fi

cfn_deploy "$stack_app" \
  --template-file "cloudformation/03-app-fargate.yml" \
  --capabilities CAPABILITY_NAMED_IAM \
  --parameter-overrides \
    "EnvironmentName=$ENVIRONMENT_NAME" \
    "ContainerImage=$image_uri" \
    "ExposeWithAlb=$EXPOSE_WITH_ALB" \
    "BtgUsername=btg" \
    "BasicAuthPasswordSsmParamName=$SSM_BASIC_AUTH_PASSWORD_PARAM" \
    "PersistenceType=$PERSISTENCE_TYPE" \
    "NotificationsMode=$NOTIFICATIONS_MODE" \
    "SmtpHost=$SMTP_HOST" \
    "SmtpPort=$SMTP_PORT" \
    "SmtpUsername=$SMTP_USERNAME" \
    "SmtpPasswordSsmParamName=$SSM_SMTP_PASSWORD_PARAM" \
    "SesFromEmail=$SES_FROM_EMAIL"

cluster_name="$(aws_cmd cloudformation describe-stacks --stack-name "$stack_app" --query "Stacks[0].Outputs[?OutputKey=='ClusterName'].OutputValue" --output text --region "$REGION")"
service_name="$(aws_cmd cloudformation describe-stacks --stack-name "$stack_app" --query "Stacks[0].Outputs[?OutputKey=='ServiceName'].OutputValue" --output text --region "$REGION")"
log_group="$(aws_cmd cloudformation describe-stacks --stack-name "$stack_app" --query "Stacks[0].Outputs[?OutputKey=='LogGroupName'].OutputValue" --output text --region "$REGION")"
alb_dns="$(aws_cmd cloudformation describe-stacks --stack-name "$stack_app" --query "Stacks[0].Outputs[?OutputKey=='AlbDnsName'].OutputValue" --output text --region "$REGION" 2>/dev/null || true)"

echo "ImageUri: $image_uri"
echo "ClusterName: $cluster_name"
echo "ServiceName: $service_name"
echo "LogGroup: $log_group"
if [[ -n "$alb_dns" && "$alb_dns" != "None" ]]; then
  echo "PublicUrl: http://$alb_dns/"
  echo "Swagger:  http://$alb_dns/swagger-ui.html"
fi

echo "Logs (últimos eventos):"
aws_cmd logs tail "$log_group" --since 10m --region "$REGION" || true
