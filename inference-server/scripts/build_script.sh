
base_path="/opt/tritonserver/projects"
script_path="$base_path/tensorrtllm_backend/tensorrt_llm/examples"

tmp_dir="$base_path/tmp"
output_dir="$base_path/engines"

mkdir -p $tmp_dir
mkdir -p $output_dir

# model_dir="$base_path/checkpoints/llama-7b"
# FP16
# python3 $script_path/llama/convert_checkpoint.py --model_dir $model_dir \
#                               --output_dir $tmp_dir/tllm_checkpoint_1gpu_fp16 \
#                               --dtype float16
# 
# trtllm-build --checkpoint_dir $tmp_dir/tllm_checkpoint_1gpu_fp16 \
#             --output_dir $output_dir/llama/7B/trt_engines/fp16/1-gpu \
#             --gemm_plugin auto


# FP16, Weight only FP8
# python3 $script_path/llama/convert_checkpoint.py --model_dir $model_dir \
#                               --output_dir $tmp_dir/tllm_checkpoint_1gpu_fp16_wq \
#                               --dtype float16
#                               --use_weight_only \
#                               --weight_only_precision int8
# 
# trtllm-build --checkpoint_dir $tmp_dir/tllm_checkpoint_1gpu_fp16_wq \
#             --output_dir $output_dir/llama/7B/trt_engines/weight_only/1-gpu/ \
#             --gemm_plugin auto



# GEMMA
model_dir="$base_path/checkpoints/gemma-2b-it"
CKPT_PATH=$tmp_dir/models/gemma_nv/checkpoints/tmp_2b_it
UNIFIED_CKPT_PATH=$tmp_dir/checkpoints/tmp_2b_it_tensorrt_llm/bf16/tp1/

python3 $script_path/gemma/convert_checkpoint.py \
    --ckpt-type hf \
    --model-dir ${model_dir} \
    --dtype float16 \
    --world-size 1 \
    --output-model-dir ${UNIFIED_CKPT_PATH}


### Build engine

ENGINE_PATH=$output_dir/gemma/2B/bf16/1-gpu/
trtllm-build --checkpoint_dir ${UNIFIED_CKPT_PATH} \
             --gemm_plugin auto \
             --max_batch_size 8 \
             --max_input_len 3000 \
             --max_seq_len 3100 \
             --output_dir ${ENGINE_PATH}

