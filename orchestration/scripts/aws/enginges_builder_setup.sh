

# Delete iamserviceaccount just in case it doesnt show up in `kubectl get sa -A | grep engines-builder-sa`
# eksctl delete iamserviceaccount --cluster glycluster --name engines-builder-sa --namespace defaul


# Create AWS IAM OpenID Connect (OIDC) 
# so that it would enable IAM Roles for Service Account (IRSA) 
# for the engines-builder container to download s3 checkpoints
eksctl utils associate-iam-oidc-provider \
  --cluster glycluster \
  --approve \
  --region us-east-1

# Create IAM role
# Only need to create once
# file:// because the command also takes inline json
# aws iam create-policy \
#   --policy-name EnginesBuilderS3Policy \
#   --policy-document file://k8s/aws/engines-builder-s3-policy.json

# Create IRSA
eksctl create iamserviceaccount \
  --name engines-builder-sa \
  --namespace default \
  --cluster glycluster \
  --attach-policy-arn arn:aws:iam::762853498770:policy/EnginesBuilderS3Policy \
  --approve \
  --override-existing-serviceaccounts \
  --region us-east-1
