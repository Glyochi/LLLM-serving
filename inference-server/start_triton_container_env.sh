base_path="/home/gly/projects/netflix/inference-server"
hf_path="/home/gly/.cache/huggingface"
checkpoints_path="$base_path/checkpoints"
engine_path="$base_path/engines"
tensorrtllm_backend_path="$base_path/tensorrtllm_backend"
build_script_path="$base_path/scripts"
triton_version="25.06"

model_repo="$base_path/model-repository"

home_path="/opt/tritonserver/projects"

external_network="observability"
container_name="triton"

docker run --rm -it --shm-size=4g \
    --ulimit memlock=-1 --ulimit stack=67108864 --gpus all \
    --name $container_name \
    -p 8000:8000 \
    -p 8001:8001 \
    -p 8002:8002 \
    -v $hf_path:/root/.cache/huggingface \
    -v $tensorrtllm_backend_path:$home_path/tensorrtllm_backend \
    -v $engine_path:$home_path/engines \
    -v $checkpoints_path:$home_path/checkpoints \
    -v $build_script_path:$home_path/scripts \
    -v $model_repo:$home_path/model-repository \
    hosting-llm-image
    # --network $external_network \
    #gemma-3-triton-server
    #gemma-triton-server
    #llama-triton-server
