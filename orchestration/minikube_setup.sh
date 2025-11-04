becho () {
  print_str="****** $1 ******"
  echo "$print_str"
}
root_dir="/home/gly/projects/netflix"
profile_name="gly-cluster"
central_node_label="central-node-label"
inference_node_label="inference-node-label"

becho "Deleting old nodes..."
minikube delete --all
becho "Starting nodes..."
minikube start --driver=docker \
  --cpus=8 \
  --memory=32768 \
  --gpus all \
  --nodes 3 \
  -p "${profile_name}" \
  --mount-string="${root_dir}:/mnt/projects" \
  --mount
becho "Enabling addons..."
minikube addons enable registry
becho "Labelling nodes..."
kubectl label nodes "${profile_name}-m02" app="${central_node_label}"
kubectl label nodes "${profile_name}-m03" app="${inference_node_label}"
becho "Loading docker images..."
#minikube image load glygateway:latest -p "${profile_name}"
#minikube image load docker.io/library/busybox:1.31.1 -p "${profile_name}"
#minikube image load quay.io/prometheus/alertmanager:v0.28.1 -p "${profile_name}"
#minikube image load quay.io/prometheus/prometheus:v3.7.1 -p "${profile_name}"
#minikube image load quay.io/prometheus-operator/prometheus-config-reloader:v0.86.1 -p "${profile_name}"
#docker save --output docker.img \
#  glygateway:latest \
#  docker.io/library/busybox:1.31.1 \
#  quay.io/prometheus/alertmanager:v0.28.1 \
#  quay.io/prometheus/prometheus:v3.7.1 \
#  quay.io/prometheus-operator/prometheus-config-reloader:v0.86.1 \
#  nvcr.io/nvidia/tritonserver:25.06-trtllm-python-py3

minikube image load docker.img -p "${profile_name}"


# Cannot mount effectively to a specific node for some reason, the main node always get priority
# Settle with just mount-string in start command and mount only 1 volumes
#becho "Mounting directories for ${profile_name}-m03..."
#becho "$(minikube ip -p ${profile_name} -n "${profile_name}-m03")"
#minikube mount -p "${profile_name}" --ip "$(minikube ip -p ${profile_name} -n "${profile_name}-m03")" "${root_dir}/inference-server/model-repository:/mnt/model-repository" &
#minikube mount -p "${profile_name}" --ip "$(minikube ip -p ${profile_name} -n "${profile_name}-m03")" "${root_dir}/gly-gateway:/mnt/gly-gateway" & 

becho "It iz done"
while true; do
  sleep 1
done
