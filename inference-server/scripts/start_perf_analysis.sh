MODEL_NAME="gemma-3-1b-it_tensorrt_llm_bls"
home_path="/workspace"
model_repo_path="$home_path/model-repository"
tokenizer_path="$model_repo_path/_assets/gemma-3-1b-it/tokenizer/"

NUM_PROMPTS=10
REQUEST_COUNT=20

genai-perf \
  profile \
  -m $MODEL_NAME \
  -u localhost:8001 \
  --server-metrics-url http://localhost:8002/metrics \
  --backend tensorrtllm \
  --request-count $REQUEST_COUNT \
  --num-prompts $NUM_PROMPTS \
  --synthetic-input-tokens-mean 200 \
  --output-tokens-mean 1000 \
  --tokenizer $tokenizer_path \
#  --generate-plots 

# perf_analyzer -m $MODEL_NAME \
#   --profile \
#   -b 4 \
#   --concurrency-range 2:16:2 \
#   -u localhost:8001 \
#   -i grpc \
#   --streaming \
#   --input-data random


#   --shape bad_words:1,4 \
#   --shape embedding_bias_weights:1 \
#   --shape embedding_bias_words:1 \
#   --shape image_bytes_input:1 \
#   --shape image_input:1 \
#   --shape lora_config:1 \
#   --shape lora_weights:1 \
#   --shape prompt_embedding_table:1 \
#   --shape stop_words:1 \
#   --shape video_bytes_input:4,-1 \

#perf_analyzer -m $MODEL_NAME -b 4 --shape <input layer>:<input shape> --concurrency-range <lower number of request>:<higher number of request>:<step>
