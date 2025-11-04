#!/bin/bash
set -euo pipefail


NAMESPACE="observability"
SRC_DIR="/home/gly/projects/netflix/observability"

if $(helm uninstall -n $NAMESPACE $(helm ls --short -n $NAMESPACE)); then 
  echo "Removing old helm managed deployments..."
fi
# Namespace
kubectl apply -f k8s/namespaces/observability.yaml

# Adding repos
helm repo add grafana https://grafana.github.io/helm-charts
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add grafana https://grafana.github.io/helm-charts
helm repo add open-telemetry https://open-telemetry.github.io/opentelemetry-helm-charts
helm repo add nvidia https://helm.ngc.nvidia.com/nvidia
helm repo update

# Installing charts
helm upgrade --install grafana grafana/grafana \
  --namespace observability \
  --values k8s/grafana/values.yaml

helm upgrade --install prometheus prometheus-community/prometheus \
  -n observability \
  -f k8s/prometheus/values.yaml

helm upgrade --install tempo grafana/tempo \
  -n observability \
  -f k8s/tempo/values.yaml

helm upgrade --install otel-central open-telemetry/opentelemetry-collector \
  -n observability \
  --wait --timeout 1m \
  -f k8s/otelcol/central/values.yaml

helm upgrade --install otel-edge open-telemetry/opentelemetry-collector \
  -n observability \
  -f k8s/otelcol/edge/values.yaml

helm upgrade --install triton ./k8s/triton \
  -n observability \
  -f k8s/triton/triton.yaml

helm upgrade --install gateway ./k8s/gateway \
  -n observability
