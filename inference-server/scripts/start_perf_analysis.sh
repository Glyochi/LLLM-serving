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


# DEBUGGING COMMANDS FROM MODEL_ANALYZER Conccurency issue


# genai-perf \
#   profile \
#   -m $MODEL_NAME \
#   --backend tensorrtllm \
#   --streaming \
#   --tokenizer $tokenizer_path \
#   -- -b 1 \
#   -u localhost:8001 \
#   -i grpc \
#   -f gemma-3-1b-it_tensorrt_llm_bls-results.csv \
#   --verbose-csv \
#   --concurrency-range 1 \
#   --measurement-mode count_windows \
#   --collect-metrics \
#   --metrics-url http://localhost:8002/metrics \
#   --metrics-interval 1000



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
#


# DEBUGGING COMMAND FOR MODEL ANALYZER LOADING EXPORTED JSON FILES BY genai-perf anf perf_analyzer
# genai-perf profile \
#   -m $MODEL_NAME \
#   --backend tensorrtllm \
#   --streaming \
#   --tokenizer /home/gly/projects/netflix/inference-server/model_analyzer_model_repository/_assets/gemma-3-1b-it/tokenizer \
#   -- \
#   -b 1 \
#   -u localhost:8001 \
#   -i grpc \
#   -f gemma-3-1b-it_tensorrt_llm_bls-results.csv \
#   --verbose-csv \
#   --concurrency-range 1 \
#   --measurement-mode count_windows \
#   --collect-metrics \
#   --metrics-url http://localhost:8002/metrics \
#   --metrics-interval 1000 \

# DEBUGGING COMMAND perf_analyzer 
# Server average accounted time was larger than client average wait time due to small sample size. Increase the measurement interval with `--measurement-interval` 
# Maybe this is a metrics endpoint not reachable, but i confirmed manually that it is reachable 
# genai-perf profile \
#   -m $MODEL_NAME \
#   --backend tensorrtllm \
#   --streaming \
#   --tokenizer /home/gly/projects/netflix/inference-server/model_analyzer_model_repository/_assets/gemma-3-1b-it/tokenizer \
#   -- \
#   -b 1 \
#   -u localhost:8001 \
#   -i grpc \
#   -f gemma-3-1b-it_tensorrt_llm_bls-results.csv \
#   --verbose-csv \
#   --concurrency-range 1 \
#   --measurement-mode count_windows \
#   --collect-metrics \
#   --metrics-url http://localhost:8002/metrics \
#   --metrics-interval 1000 \
#   --measurement-interval 10000 \
#   --measurement-request-count 100


perf_analyzer \
  -m $MODEL_NAME \
  --async \
  --input-data artifacts/gemma-3-1b-it_tensorrt_llm_bls-triton-tensorrtllm-concurrency1/inputs.json \
  -i grpc \
  --streaming \
  -u localhost:8001 \
  --shape max_tokens:1 \
  --shape text_input:1 \
  --concurrency-range 1 \
  --service-kind triton \
  --request-count 0 \
  --concurrency-range 1 \
  --warmup-request-count 0 \
  --measurement-interval 1000 \
  --stability-percentage 999 \
  --profile-export-file /opt/triton-model-analyzer/rm_me/profile_export.json \
  -b 1 \
  -u localhost:8001 \
  -i grpc \
  -f gemma-3-1b-it_tensorrt_llm_bls-results.csv \
  --verbose-csv \
  --measurement-interval 1000 \
  --concurrency-range 4 \
  --measurement-mode count_windows \
  --measurement-request-count 10 \
  --collect-metrics \
  --metrics-url http://localhost:8002/metrics \
  --metrics-interval 1000 \
  --verbose
