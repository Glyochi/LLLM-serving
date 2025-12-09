#!/bin/bash
set -euo pipefail


NAMESPACE="observability"
# Auto seperate by " "
delete_array=($(helm ls --short -n $NAMESPACE))

echo "Removing old helm managed deployments..."
for to_delete in "${delete_array[@]}"; do
  helm uninstall -n $NAMESPACE "${to_delete}"
done

