minikube delete
minikube start --driver=docker --gpus all
minikube mount /home/gly/projects/netflix/inference-server/model-repository:/mnt/model-repository

