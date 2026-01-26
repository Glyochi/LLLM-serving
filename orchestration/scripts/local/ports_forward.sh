#!/bin/bash
set -euo pipefail

# Find all "kubectl port-forward" PIDs (avoid matching the grep itself)
mapfile -t pids < <(ps aux | grep 'kubectl port-forward' | awk '{print $2}')
pids=("${pids[@]::${#pids[@]}-1}")

if (( ${#pids[@]} == 0 )); then
  echo "No kubectl port-forward processes found to be deleted."
else
  echo "Killing PIDs: ${pids}"
  sudo kill -9 ${pids}
fi

echo ""
echo "FORWARDING..."


kubectl port-forward "service/grafana" 30558:80 -n observability --address='0.0.0.0' &
kubectl port-forward "service/gateway" 8090:8090 -n observability --address='0.0.0.0' &
kubectl port-forward "service/triton" 8002:8002 -n observability --address='0.0.0.0' &
kubectl port-forward "service/triton" 8001:8001 -n observability --address='0.0.0.0' &

while true
do
  sleep 5
done
