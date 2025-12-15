#!/bin/bash
set -euo pipefail


cluster_name="glycluster"

# Create ssh keys for remoting in later
ssh-keygen -t ed25519 -C "gly-eks" -f ~/.ssh/gly-eks

eksctl create cluster -f scripts/aws/eks_cluster.yaml
aws eks update-kubeconfig --name "$cluster_name" --region us-east-1


# IAM Roles for Service Account (IRSA) for services 
# driver, load balancer, auto scaler controllers sometimes need AWS APIs => Need IAM role
eksctl utils associate-iam-oidc-provider --cluster "$cluster_name" --approve


# EBS CSI driver
# On AWS, use EBS volumes.
# CSI = Container Storage Interface, which handles EBS management
# Enabling the addon EBS CSI addon for the cluster 
eksctl create addon --name aws-ebs-csi-driver \
  --cluster "$cluster_name" \
  --force
