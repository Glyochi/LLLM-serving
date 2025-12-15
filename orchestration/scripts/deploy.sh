#!/bin/bash
set -euo pipefail

NAMESPACE="observability"
SRC_DIR="/home/gly/projects/netflix/observability"

MINIKUBE_STR="minikube"
AWS_STR="aws"

ENV=${1:-"$MINIKUBE_STR"}   # usage: ./deploy.sh local  OR ./deploy.sh aws

case "$ENV" in
  "$MINIKUBE_STR")
    ;;
  "$AWS_STR")
    ;;
  *)
    echo "Unknown ENV=$ENV (expected: $MINIKUBE_STR | $AWS_STR)"
    exit 1
    ;;
esac




# Namespace
kubectl apply -f k8s/namespaces/observability.yaml

# Setting up aws resources
if [[ "$ENV" = "$AWS_STR" ]]; then
  # Setting up ebs for persistence storage
  kubectl apply -n "$NAMESPACE" -f k8s/aws/ebs-sc.yaml 
fi


# Adding repos
helm repo add grafana https://grafana.github.io/helm-charts
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add grafana https://grafana.github.io/helm-charts
helm repo add open-telemetry https://open-telemetry.github.io/opentelemetry-helm-charts
helm repo add nvidia https://helm.ngc.nvidia.com/nvidia
helm repo update

# Installing charts
helm upgrade --install grafana grafana/grafana \
  --namespace "$NAMESPACE" \
  -f k8s/grafana/values.yaml \
  -f k8s/grafana/values-$ENV.yaml
  

helm upgrade --install prometheus prometheus-community/prometheus \
  -n "$NAMESPACE" \
  -f k8s/prometheus/values.yaml \
  -f k8s/prometheus/values-$ENV.yaml

helm upgrade --install tempo grafana/tempo \
  -n "$NAMESPACE" \
  -f k8s/tempo/values.yaml \
  -f k8s/tempo/values-$ENV.yaml

helm upgrade --install otel-central open-telemetry/opentelemetry-collector \
  -n "$NAMESPACE" \
  --wait --timeout 1m \
  -f k8s/otelcol/central/values.yaml

helm upgrade --install otel-edge open-telemetry/opentelemetry-collector \
  -n "$NAMESPACE" \
  -f k8s/otelcol/edge/values.yaml

helm upgrade --install triton ./k8s/triton \
  -n "$NAMESPACE" \
  -f k8s/triton/triton.yaml \
  -f k8s/triton/values-$ENV.yaml

helm upgrade --install gateway ./k8s/gateway \
  -n "$NAMESPACE" \
  -f k8s/gateway/values-$ENV.yaml

#if [[ "$ENV" = "$AWS_STR" ]]; then
#  # Setting up ALB ingress for exposing services 
#  #kubectl apply -n "$NAMESPACE" -f k8s/aws/alb-ingress.yaml 
#fi
