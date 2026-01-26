base_path="/home/gly/projects/netflix/inference-server"
model_repo_path="$base_path/model-repository"
output_path="$base_path/model_analyzer_results"


home_path="/opt/triton-model-analyzer"


# Important: You must ensure the <path-to-output-model-repo> is identical on both sides of the mount (or else Tritonserver cannot load the model)
# Seems like its trying to spin up an outside docker container from inside this container, thats why the path matter
docker run -it --rm --gpus all \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $model_repo_path:$model_repo_path \
    -v $output_path:$output_path \
    --net=host model-analyzer
