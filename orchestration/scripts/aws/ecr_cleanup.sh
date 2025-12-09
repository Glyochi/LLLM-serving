REPO_NAME=${1:-""}   # usage: ./deploy.sh local  OR ./deploy.sh aws
if [[ "$REPO_NAME" = "" ]]; then
  echo "Invalid repo name. Cannot be empty"
  exit
fi

aws ecr delete-repository --repository-name "$REPO_NAME" --force
