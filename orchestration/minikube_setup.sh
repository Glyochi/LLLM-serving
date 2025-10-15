root_dir="/home/gly/projects/netflix"
minikube delete
minikube start --driver=docker --gpus all 
minikube image load glygateway:latest
minikube mount "${root_dir}/inference-server/model-repository:/mnt/model-repository" &
minikube mount "${root_dir}/gly-gateway:/mnt/gly-gateway" & 
while true; do
  sleep 1
done
