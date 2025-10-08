#!/bin/bash
set -euo pipefail

retry() {
  local -r max_tries="$1"; shift
  local -i n=1
  local rc=0
  local sleep_time=4

  until "$@"; do           # <- a failing condition won't exit the script with -e
    rc=$?
    if (( n >= max_tries )); then
      echo "Failed after $n attempts (rc=$rc)" >&2
      return "$rc"
    fi
    echo "Attempt $n failed (rc=$rc); retrying in ${sleep_time}s..." >&2
    sleep $sleep_time
    ((n++))
    # For some reasons if stopping the script and running it right away sometimes fail to check if pods are running
    # Getting into a new shell helps fix this 
    #exec -l bash
  done
}

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

helm upgrade --install otel-collector open-telemetry/opentelemetry-collector \
  -n observability \
  -f k8s/otelcol/values.yaml

helm upgrade --install triton ./k8s/triton \
  -n observability \
  -f k8s/triton/triton.yaml

retry 5 kubectl port-forward "service/grafana" 30558:80 -n observability --address='0.0.0.0'
