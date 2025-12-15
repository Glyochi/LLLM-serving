
# Create AWS IAM OpenID Connect (OIDC) 
# so that it would enable IAM Roles for Service Account (IRSA) 
# for the engines-builder container to download s3 checkpoints
eksctl utils associate-iam-oidc-provider \
  --cluster glycluster \
  --approve \
  --region us-east-1

# Create IAM role
# Only need to create once
# To delete `aws iam delete-policy --policy-arn {}`
# Or delete on the AWS console that was easier for me
#
# file:// because the command also takes inline json
# aws iam create-policy \
#   --policy-name S3CheckpointsEnginesPolicy \
#   --policy-document file://k8s/aws/s3-checkpoints-engines-policy.json

# Create IRSA
# Delete iamserviceaccount just in case it doesnt show up in `kubectl get sa -A | grep engines-builder-sa`
# eksctl delete iamserviceaccount --cluster glycluster --name engines-builder-sa --namespace observability#
eksctl create iamserviceaccount \
  --name s3-checkpoints-engines-sa \
  --namespace observability \
  --cluster glycluster \
  --attach-policy-arn arn:aws:iam::762853498770:policy/S3CheckpointsEnginesPolicy \
  --approve \
  --override-existing-serviceaccounts \
  --region us-east-1
