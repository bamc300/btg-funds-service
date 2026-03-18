set -euo pipefail

cd "$(dirname "$0")/../.."

export MSYS2_ARG_CONV_EXCL="*"
export MSYS_NO_PATHCONV=1

ENVIRONMENT_NAME="${ENVIRONMENT_NAME:-dev}"
REGION="${AWS_REGION:-${AWS_DEFAULT_REGION:-}}"
ECR_REPOSITORY_NAME="${ECR_REPOSITORY_NAME:-btg-funds-service}"
DELETE_ECR_REPO="${DELETE_ECR_REPO:-false}"
DELETE_SSM_PARAMS="${DELETE_SSM_PARAMS:-false}"

SSM_BASIC_AUTH_PASSWORD_PARAM="${SSM_BASIC_AUTH_PASSWORD_PARAM:-/btg/funds/${ENVIRONMENT_NAME}/basicAuthPassword}"
SSM_SMTP_PASSWORD_PARAM="${SSM_SMTP_PASSWORD_PARAM:-/btg/funds/${ENVIRONMENT_NAME}/smtpPassword}"

if [[ -z "$REGION" ]]; then
  echo "Define AWS_REGION o AWS_DEFAULT_REGION" >&2
  exit 1
fi

aws_cmd() {
  if command -v aws >/dev/null 2>&1; then
    MSYS2_ARG_CONV_EXCL="*" MSYS_NO_PATHCONV=1 aws "$@"
    return
  fi
  local aws_exe="/c/Program Files/Amazon/AWSCLIV2/aws.exe"
  if [[ -f "$aws_exe" ]]; then
    MSYS2_ARG_CONV_EXCL="*" MSYS_NO_PATHCONV=1 "$aws_exe" "$@"
    return
  fi
  local aws_exe_bin="/c/Program Files/Amazon/AWSCLIV2/bin/aws.exe"
  if [[ -f "$aws_exe_bin" ]]; then
    MSYS2_ARG_CONV_EXCL="*" MSYS_NO_PATHCONV=1 "$aws_exe_bin" "$@"
    return
  fi
  echo "No se encontró AWS CLI (aws)." >&2
  exit 1
}

stack_network="btg-funds-network-$ENVIRONMENT_NAME"
stack_data="btg-funds-data-$ENVIRONMENT_NAME"
stack_app="btg-funds-app-fargate-$ENVIRONMENT_NAME"

delete_stack_if_exists() {
  local name="$1"
  if aws_cmd cloudformation describe-stacks --stack-name "$name" --region "$REGION" >/dev/null 2>&1; then
    aws_cmd cloudformation delete-stack --stack-name "$name" --region "$REGION"
    aws_cmd cloudformation wait stack-delete-complete --stack-name "$name" --region "$REGION" || true
  fi
}

delete_stack_if_exists "$stack_app"
delete_stack_if_exists "$stack_data"
delete_stack_if_exists "$stack_network"

if [[ "$DELETE_SSM_PARAMS" == "true" ]]; then
  aws_cmd ssm delete-parameter --name "$SSM_BASIC_AUTH_PASSWORD_PARAM" --region "$REGION" >/dev/null 2>&1 || true
  aws_cmd ssm delete-parameter --name "$SSM_SMTP_PASSWORD_PARAM" --region "$REGION" >/dev/null 2>&1 || true
fi

if [[ "$DELETE_ECR_REPO" == "true" ]]; then
  aws_cmd ecr delete-repository --repository-name "$ECR_REPOSITORY_NAME" --force --region "$REGION" >/dev/null 2>&1 || true
fi

echo "Cleanup completado (Fargate) para EnvironmentName=$ENVIRONMENT_NAME en $REGION"
