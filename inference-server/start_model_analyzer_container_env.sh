base_path="/home/gly/projects/netflix/inference-server"
build_script_path="$base_path/scripts"
model_checkpoints_path="$base_path/checkpoints"
output_path="$base_path/model_analyzer_results"

model_repository_path="$base_path/model_analyzer_model_repository"

home_path="/opt/triton-model-analyzer"

container_name="model_analyzer"

# Important: You must ensure the <path-to-output-model-repo> is identical on both sides of the mount (or else Tritonserver cannot load the model)
# Seems like its trying to spin up an outside docker container from inside this container, thats why the path matter
docker run -it --rm --gpus all \
    --name $container_name \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $build_script_path:$home_path/scripts \
    -v $model_repository_path:$model_repository_path \
    -v $output_path:$output_path \
    --net=host \
    model-analyzer:r24.12
    #model-analyzer:r25.06_both
    #model-analyzer


