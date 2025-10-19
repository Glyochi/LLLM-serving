root_dir="/home/gly/projects/netflix"
minikube delete --all
#minikube addons enable storage
minikube start --driver=docker --gpus all 
minikube image load glygateway:latest 
minikube image load docker.io/library/busybox:1.31.1
minikube image load quay.io/prometheus/alertmanager:v0.28.1
minikube image load quay.io/prometheus-operator/prometheus-config-reloader:v0.86.1
minikube image load nvcr.io/nvidia/tritonserver:25.06-trtllm-python-py3

minikube mount "${root_dir}/inference-server/model-repository:/mnt/model-repository" &
minikube mount "${root_dir}/gly-gateway:/mnt/gly-gateway" & 
while true; do
  sleep 1
done
