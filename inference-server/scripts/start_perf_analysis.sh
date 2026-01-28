MODEL_NAME="gemma-3-1b-it_tensorrt_llm_bls"
home_path="/workspace"
model_repo_path="$home_path/model-repository"
tokenizer_path="$model_repo_path/_assets/gemma-3-1b-it/tokenizer/"

NUM_PROMPTS=10
REQUEST_COUNT=20

# WORKING COMMAND FOR LOCAL MINIKUBE
# genai-perf \
#   profile \
#   -m $MODEL_NAME \
#   -u localhost:8001 \
#   --server-metrics-url http://localhost:8002/metrics \
#   --backend tensorrtllm \
#   --request-count $REQUEST_COUNT \
#   --num-prompts $NUM_PROMPTS \
#   --synthetic-input-tokens-mean 200 \
#   --output-tokens-mean 1000 \
#   --tokenizer $tokenizer_path \
# #  --generate-plots 


# DEBUGGING COMMANDS FROM MODEL_ANALYZER


genai-perf \
  profile \
  -m $MODEL_NAME \
  --backend tensorrtllm \
  --streaming \
  --tokenizer $tokenizer_path \
  -- -b 1 \
  -u localhost:8001 \
  -i grpc \
  -f gemma-3-1b-it_tensorrt_llm_bls-results.csv \
  --verbose-csv \
  --concurrency-range 1 \
  --measurement-mode count_windows \
  --collect-metrics \
  --metrics-url http://localhost:8002/metrics \
  --metrics-interval 1000



# perf_analyzer \
#   -m $MODEL_NAME \
#   --async \
#   --stability-percentage 999 \
#   --request-count 10 \
#   -i grpc \
#   --streaming \
#   --shape max_tokens:1 \
#   --shape text_input:1 \
#   -u localhost:8001 \
#   --concurrency-range 1 \
#   --service-kind triton \
#   -b 1 \
#   -u localhost:8001 \
#   -i grpc \
#   -f gemma-3-1b-it_tensorrt_llm_bls-results.csv \
#   --verbose-csv \
#   --measurement-mode count_windows \
#   --collect-metrics \
#   --metrics-url http://localhost:8002/metrics \
#   --metrics-interval 1000 \
#   --input-data /workspace/artifacts/gemma-3-1b-it_tensorrt_llm_bls-triton-tensorrtllm-concurrency1/inputs.json \
#   --profile-export-file /workspace/artifacts/gemma-3-1b-it_tensorrt_llm_bls-triton-tensorrtllm-concurrency1/profile_export.json
