FROM gemma-3-triton-server 
#FROM nvcr.io/nvidia/tritonserver:25.06-trtllm-python-py3
#
#
#COPY ./tensorrtllm_backend/tensorrt_llm/examples/models/core/gemma/requirements.txt ./requirements.txt
#COPY ./tensorrtllm_backend/tensorrt_llm/examples/constraints.txt ./constraints.txt
#
#RUN pip3 install -r ./requirements.txt -c constraints.txt
COPY ./scripts/build_script.sh ./build_script.sh
COPY ./tensorrtllm_backend/ ./projects/tensorrtllm_backend/

