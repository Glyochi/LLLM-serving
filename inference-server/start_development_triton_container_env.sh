base_path="/home/gly/projects/netflix/inference-server"
build_script_path="$base_path/scripts"
model_repo="$base_path/model-repository"
perf_results_path="$base_path/perf_results"

home_path="/workspace"
container_name="dev-triton"


# -v /var/run/docker.sock:/var/run/docker.sock \ is for docker control inside the docker container (scari)
docker run --rm -it \
    --network host \
    --name $container_name \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $build_script_path:$home_path/scripts \
    -v $model_repo:$home_path/model-repository \
    -v $perf_results_path:$home_path/artifacts \
    nvcr.io/nvidia/tritonserver:25.06-py3-sdk \

