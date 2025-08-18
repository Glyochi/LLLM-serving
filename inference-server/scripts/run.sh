base_path="/opt/tritonserver/projects"
script_path="$base_path/tensorrtllm_backend/tensorrt_llm/examples"
model_dir="$base_path/checkpoints/llama-7b"
output_dir="$base_path/out"

# Has to include the model dir still because need to get the tokenizer
python3 $script_path/run.py --max_output_len=50 \
                  --tokenizer_dir $model_dir \
                  --engine_dir=$output_dir/llama/7B/trt_engines/fp16/1-gpu/
