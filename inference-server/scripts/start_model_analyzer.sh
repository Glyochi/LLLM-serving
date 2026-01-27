# base_path="/home/gly/projects/netflix/inference-server"
# model_repo_path="$base_path/model-repository"
# output_path="$base_path/model_analyzer_results2"
# model_name="gemma-3-1b-it_tensorrt_llm_bls" 
# 
# triton_version="25.06"
# 
# model-analyzer profile --model-repository $model_repo_path \
#   --profile-models $model_name \
#   --model-type LLM \
#   --triton-launch-mode docker \
#   --triton-docker-image nvcr.io/nvidia/tritonserver:$triton_version-trtllm-python-py3 \
#   --output-model-repository $output_path \
#   --run-config-search-mode quick \
#   --override-output-model-repository
  
model-analyzer profile -f ./scripts/model_analyzer_config.yaml
