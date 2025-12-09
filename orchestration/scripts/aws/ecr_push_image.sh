AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
AWS_REGION=us-east-1

REPO_NAME=${1:-""}   # usage: ./deploy.sh local  OR ./deploy.sh aws
VERSION=${2:-"latest"}
if [[ "$REPO_NAME" = "" ]]; then
  echo "Invalid repo name. Cannot be empty"
  exit
fi

aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login \
      --username "AWS" \
      --password-stdin "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

# Tag local image to ECR name
docker tag "${REPO_NAME}:${VERSION}" \
  "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${REPO_NAME}:${VERSION}"

# Push to ECR
docker push "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${REPO_NAME}:${VERSION}"
