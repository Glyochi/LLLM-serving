text="translate English to German: This is good"
tokenizer_dir="/opt/tritonserver/projects/model-repository/_assets/gemma-2b-it/tokenizer"
beam_width=1

python3 /app/client/inflight_batcher_llm_client.py --text "$text" --request-output-len 200 --exclude-input-in-output --tokenizer-dir $tokenizer_dir --beam-width $beam_width --streaming
