# bash all_images.sh | xargs -n 2 sh -c 'echo "$0 $1"'
# echo "glygateway" "latest"
# echo "docker.io/library/busybox" "1.31.1"
# echo "quay.io/prometheus/alertmanager" "v0.28.1"
# echo "quay.io/prometheus/prometheus" "v3.7.1"
# echo "nvcr.io/nvidia/tritonserver" "25.06-trtllm-python-py3"
echo "gemma-3-triton-server" "latest"
