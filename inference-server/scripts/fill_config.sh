# This might only work for sp model
# https://docs.nvidia.com/deeplearning/triton-inference-server/user-guide/docs/tensorrtllm_backend/docs/gemma.html
BASE_PATH="/home/gly/projects/netflix/inference-server"
DOCKER_BASE_PATH="/opt/tritonserver/projects"
TOOL_PATH="$BASE_PATH/tensorrtllm_backend/tensorrt_llm/triton_backend"
MODEL_DIRECTORY="$BASE_PATH/model-repository"
DOCKER_MODEL_DIRECTORY="$DOCKER_BASE_PATH/model-repository"


TOKENIZER_DIR="$DOCKER_MODEL_DIRECTORY/_assets/gemma-2b-it/tokenizer"
ENGINE_PATH="$DOCKER_MODEL_DIRECTORY/_assets/gemma-2b-it/engines"

cp $TOOL_PATH/all_models/inflight_batcher_llm/preprocessing $MODEL_DIRECTORY -r
cp $TOOL_PATH/all_models/inflight_batcher_llm/postprocessing $MODEL_DIRECTORY -r
cp $TOOL_PATH/all_models/inflight_batcher_llm/tensorrt_llm $MODEL_DIRECTORY -r
cp $TOOL_PATH/all_models/inflight_batcher_llm/tensorrt_llm_bls $MODEL_DIRECTORY -r
cp $TOOL_PATH/all_models/inflight_batcher_llm/ensemble $MODEL_DIRECTORY -r

python3 $TOOL_PATH/tools/fill_template.py -i $MODEL_DIRECTORY/preprocessing/config.pbtxt tokenizer_dir:${TOKENIZER_DIR},tokenizer_type:sp,triton_max_batch_size:64,preprocessing_instance_count:1,add_special_tokens:True
python3 $TOOL_PATH/tools/fill_template.py -i $MODEL_DIRECTORY/postprocessing/config.pbtxt tokenizer_dir:${TOKENIZER_DIR},tokenizer_type:sp,triton_max_batch_size:64,postprocessing_instance_count:1
python3 $TOOL_PATH/tools/fill_template.py -i $MODEL_DIRECTORY/tensorrt_llm_bls/config.pbtxt triton_max_batch_size:64,decoupled_mode:True,bls_instance_count:1,accumulate_tokens:False,logits_datatype:TYPE_FP32
python3 $TOOL_PATH/tools/fill_template.py -i $MODEL_DIRECTORY/tensorrt_llm/config.pbtxt triton_backend:tensorrtllm,triton_max_batch_size:64,decoupled_mode:True,max_beam_width:1,engine_dir:${ENGINE_PATH},max_tokens_in_paged_kv_cache:2560,max_attention_window_size:2560,kv_cache_free_gpu_mem_fraction:0.5,exclude_input_in_output:True,enable_kv_cache_reuse:False,batching_strategy:inflight_fused_batching,max_queue_delay_microseconds:0,batch_scheduler_policy:guaranteed_no_evict,encoder_input_features_data_type:TYPE_FP16,logits_datatype:TYPE_FP32
python3 $TOOL_PATH/tools/fill_template.py -i $MODEL_DIRECTORY/ensemble/config.pbtxt triton_max_batch_size:${MAX_BATCH_SIZE},logits_datatype:TYPE_FP32 
