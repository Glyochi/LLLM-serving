### tensorrt_llm_backend/tensorrt_llm v0.12.0
# # This might only work for sp model
# # https://docs.nvidia.com/deeplearning/triton-inference-server/user-guide/docs/tensorrtllm_backend/docs/gemma.html
# BASE_PATH="/home/gly/projects/netflix/inference-server"
# DOCKER_BASE_PATH="/opt/tritonserver/projects"
# TOOL_PATH="$BASE_PATH/tensorrtllm_backend/tensorrt_llm/triton_backend"
# MODEL_DIRECTORY="$BASE_PATH/model-repository"
# DOCKER_MODEL_DIRECTORY="$DOCKER_BASE_PATH/model-repository"
# 
# 
# TOKENIZER_DIR="$DOCKER_MODEL_DIRECTORY/_assets/gemma-2b-it/tokenizer"
# ENGINE_PATH="$DOCKER_MODEL_DIRECTORY/_assets/gemma-2b-it/engines"
# 
# 
# cp $TOOL_PATH/all_models/inflight_batcher_llm/preprocessing $MODEL_DIRECTORY -r
# cp $TOOL_PATH/all_models/inflight_batcher_llm/postprocessing $MODEL_DIRECTORY -r
# cp $TOOL_PATH/all_models/inflight_batcher_llm/tensorrt_llm $MODEL_DIRECTORY -r
# cp $TOOL_PATH/all_models/inflight_batcher_llm/tensorrt_llm_bls $MODEL_DIRECTORY -r
# cp $TOOL_PATH/all_models/inflight_batcher_llm/ensemble $MODEL_DIRECTORY -r
# 
# 
# python3 $TOOL_PATH/tools/fill_template.py -i $MODEL_DIRECTORY/preprocessing/config.pbtxt tokenizer_dir:${TOKENIZER_DIR},tokenizer_type:sp,triton_max_batch_size:64,preprocessing_instance_count:1,add_special_tokens:True
# python3 $TOOL_PATH/tools/fill_template.py -i $MODEL_DIRECTORY/postprocessing/config.pbtxt tokenizer_dir:${TOKENIZER_DIR},tokenizer_type:sp,triton_max_batch_size:64,postprocessing_instance_count:1
# python3 $TOOL_PATH/tools/fill_template.py -i $MODEL_DIRECTORY/tensorrt_llm_bls/config.pbtxt triton_max_batch_size:64,decoupled_mode:True,bls_instance_count:1,accumulate_tokens:False,logits_datatype:TYPE_FP32
# python3 $TOOL_PATH/tools/fill_template.py -i $MODEL_DIRECTORY/tensorrt_llm/config.pbtxt triton_backend:tensorrtllm,triton_max_batch_size:64,decoupled_mode:True,max_beam_width:1,engine_dir:${ENGINE_PATH},max_tokens_in_paged_kv_cache:2560,max_attention_window_size:2560,kv_cache_free_gpu_mem_fraction:0.5,exclude_input_in_output:True,enable_kv_cache_reuse:False,batching_strategy:inflight_fused_batching,max_queue_delay_microseconds:0,batch_scheduler_policy:guaranteed_no_evict,encoder_input_features_data_type:TYPE_FP16,logits_datatype:TYPE_FP32
# python3 $TOOL_PATH/tools/fill_template.py -i $MODEL_DIRECTORY/ensemble/config.pbtxt triton_max_batch_size:${MAX_BATCH_SIZE},logits_datatype:TYPE_FP32 

### IMPORTANT PARAMS
# max_tokens_in_paged_kv_cache:2560,
# max_attention_window_size:2560,
# kv_cache_free_gpu_mem_fraction:0.5,
# exclude_input_in_output:True,
# enable_kv_cache_reuse:False,
# batching_strategy:inflight_fused_batching,
# max_queue_delay_microseconds:0,
# batch_scheduler_policy:guaranteed_no_evict,
# encoder_input_features_data_type:TYPE_FP16,
# logits_datatype:TYPE_FP32




# Works for tensorrt_llm v0.21.0

MODEL_NAME="gemma-3-1b-it"
BASE_PATH="/home/gly/projects/netflix/inference-server"
DOCKER_BASE_PATH="/opt/tritonserver/projects"
DOCKER_MODEL_DIRECTORY="$DOCKER_BASE_PATH/model-repository"

ENGINE_DIR="$DOCKER_MODEL_DIRECTORY/_assets/${MODEL_NAME}/engines"
TOKENIZER_DIR="$DOCKER_MODEL_DIRECTORY/_assets/${MODEL_NAME}/tokenizer"

MODEL_FOLDER="$BASE_PATH/model-repository"
TRITON_MAX_BATCH_SIZE=4
INSTANCE_COUNT=1
MAX_QUEUE_DELAY_MS=0
MAX_QUEUE_SIZE=0
TOOL_PATH="$BASE_PATH/tensorrtllm_backend/tensorrt_llm/triton_backend"
FILL_TEMPLATE_SCRIPT="${TOOL_PATH}/tools/fill_template.py"
DECOUPLED_MODE=true
LOGITS_DATATYPE=TYPE_FP32
KV_CACHE_FREE_GPU_MEM_FRACTION=0.49


mkdir -p $MODEL_FOLDER/${MODEL_NAME}_preprocessing
mkdir -p $MODEL_FOLDER/${MODEL_NAME}_postprocessing
mkdir -p $MODEL_FOLDER/${MODEL_NAME}_tensorrt_llm
mkdir -p $MODEL_FOLDER/${MODEL_NAME}_tensorrt_llm_bls
mkdir -p $MODEL_FOLDER/${MODEL_NAME}_ensemble

cp -rf $TOOL_PATH/all_models/inflight_batcher_llm/preprocessing/* $MODEL_FOLDER/${MODEL_NAME}_preprocessing
cp -rf $TOOL_PATH/all_models/inflight_batcher_llm/postprocessing/* $MODEL_FOLDER/${MODEL_NAME}_postprocessing
cp -rf $TOOL_PATH/all_models/inflight_batcher_llm/tensorrt_llm/* $MODEL_FOLDER/${MODEL_NAME}_tensorrt_llm
cp -rf $TOOL_PATH/all_models/inflight_batcher_llm/tensorrt_llm_bls/* $MODEL_FOLDER/${MODEL_NAME}_tensorrt_llm_bls
cp -rf $TOOL_PATH/all_models/inflight_batcher_llm/ensemble/* $MODEL_FOLDER/${MODEL_NAME}_ensemble


ensemble_model_name="${MODEL_NAME}_ensemble"
python3 ${FILL_TEMPLATE_SCRIPT} -i ${MODEL_FOLDER}/${ensemble_model_name}/config.pbtxt triton_max_batch_size:${TRITON_MAX_BATCH_SIZE},logits_datatype:${LOGITS_DATATYPE}
sed -i "s/name: \"ensemble\"/name: \"${ensemble_model_name}\"/" ${MODEL_FOLDER}/${ensemble_model_name}/config.pbtxt

preprocessing_model_name="${MODEL_NAME}_preprocessing"
python3 ${FILL_TEMPLATE_SCRIPT} -i ${MODEL_FOLDER}/${preprocessing_model_name}/config.pbtxt tokenizer_dir:${TOKENIZER_DIR},triton_max_batch_size:${TRITON_MAX_BATCH_SIZE},preprocessing_instance_count:${INSTANCE_COUNT}
sed -i "s/name: \"preprocessing\"/name: \"${preprocessing_model_name}\"/" ${MODEL_FOLDER}/${preprocessing_model_name}/config.pbtxt

tensorrt_llm_model_name="${MODEL_NAME}_tensorrt_llm"
python3 ${FILL_TEMPLATE_SCRIPT} -i ${MODEL_FOLDER}/${tensorrt_llm_model_name}/config.pbtxt triton_backend:tensorrtllm,triton_max_batch_size:${TRITON_MAX_BATCH_SIZE},decoupled_mode:${DECOUPLED_MODE},engine_dir:${ENGINE_DIR},max_queue_delay_microseconds:${MAX_QUEUE_DELAY_MS},batching_strategy:inflight_fused_batching,max_queue_size:${MAX_QUEUE_SIZE},encoder_input_features_data_type:TYPE_FP16,logits_datatype:${LOGITS_DATATYPE},kv_cache_free_gpu_mem_fraction:${KV_CACHE_FREE_GPU_MEM_FRACTION}
sed -i "s/name: \"tensorrt_llm\"/name: \"${tensorrt_llm_model_name}\"/" ${MODEL_FOLDER}/${tensorrt_llm_model_name}/config.pbtxt

postprocessing_model_name="${MODEL_NAME}_postprocessing"
python3 ${FILL_TEMPLATE_SCRIPT} -i ${MODEL_FOLDER}/${postprocessing_model_name}/config.pbtxt tokenizer_dir:${TOKENIZER_DIR},triton_max_batch_size:${TRITON_MAX_BATCH_SIZE},postprocessing_instance_count:${INSTANCE_COUNT}
sed -i "s/name: \"postprocessing\"/name: \"${postprocessing_model_name}\"/" ${MODEL_FOLDER}/${postprocessing_model_name}/config.pbtxt

tensorrt_llm_bls_model_name="${MODEL_NAME}_tensorrt_llm_bls"
python3 ${FILL_TEMPLATE_SCRIPT} -i ${MODEL_FOLDER}/${tensorrt_llm_bls_model_name}/config.pbtxt triton_max_batch_size:${TRITON_MAX_BATCH_SIZE},decoupled_mode:${DECOUPLED_MODE},bls_instance_count:${INSTANCE_COUNT},logits_datatype:${LOGITS_DATATYPE},tensorrt_llm_model_name:${tensorrt_llm_model_name}
sed -i "s/name: \"tensorrt_llm_bls\"/name: \"${tensorrt_llm_bls_model_name}\"/" ${MODEL_FOLDER}/${tensorrt_llm_bls_model_name}/config.pbtxt
sed -i "s/preproc_model_name=\"preprocessing\"/preproc_model_name=\"${preprocessing_model_name}\"/" ${MODEL_FOLDER}/${tensorrt_llm_bls_model_name}/**/model.py
sed -i "s/postproc_model_name=\"postprocessing\"/postproc_model_name=\"${postprocessing_model_name}\"/" ${MODEL_FOLDER}/${tensorrt_llm_bls_model_name}/**/model.py


