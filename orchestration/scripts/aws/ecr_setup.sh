AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
AWS_REGION="us-east-1"

REPO_NAME=${1:-""}   # usage: ./deploy.sh local  OR ./deploy.sh aws
if [[ "$REPO_NAME" = "" ]]; then
  echo "Invalid repo name. Cannot be empty"
  exit
fi

aws ecr create-repository \
  --repository-name "$REPO_NAME" \
  --image-scanning-configuration scanOnPush=true \
  --region $AWS_REGION

