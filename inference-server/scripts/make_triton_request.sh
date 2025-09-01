
#MODEL_NAME="gemma-2b-it"
MODEL_NAME="gemma-3-1b-it_tensorrt_llm_bls"
TEXT="What is machine learning?"
MAX_TOKENS=1000
data=$(jq -n \
  --arg text_input "$TEXT" \
  --argjson max_tokens "$MAX_TOKENS" \
  --arg bad_words "" \
  --arg stop_words "<end_of_turn>" \
  '{text_input: $text_input, max_tokens: $max_tokens, bad_words: $bad_words, stop_words: $stop_words}')

curl -X POST localhost:8000/v2/models/${MODEL_NAME}/generate \
  -d "$data" \
  | jq .

