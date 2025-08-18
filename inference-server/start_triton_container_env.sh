base_path="/home/gly/projects/netflix/inference-server"
checkpoints_path="$base_path/checkpoints"
engine_path="$base_path/engines"
tensorrtllm_backend_path="$base_path/tensorrtllm_backend"
build_script_path="$base_path/scripts"
triton_version="25.06"

model_repo="$base_path/model-repository"

home_path="/opt/tritonserver/projects"

docker run --rm -it --net host --shm-size=4g \
    --ulimit memlock=-1 --ulimit stack=67108864 --gpus all \
    -v $tensorrtllm_backend_path:$home_path/tensorrtllm_backend \
    -v $engine_path:$home_path/engines \
    -v $checkpoints_path:$home_path/checkpoints \
    -v $build_script_path:$home_path/scripts \
    -v $model_repo:$home_path/model-repository \
    gemma-triton-server
    #llama-triton-server
# docker run --rm -it --net host --shm-size=4g \
#     --ulimit memlock=-1 --ulimit stack=67108864 --gpus all \
#     -v $tensorrtllm_backend_path:/$home_path/tensorrtllm_backend \
#     -v $checkpoints_path:/$home_path/Model-hf \
#     -v $engine_path:/$home_path/engines \
#     -v $engine_path:/$home_path/engines \
#     -v $build_script_path:/$home_path/build_script.sh \
#     nvcr.io/nvidia/tritonserver:$triton_version-trtllm-python-py3
