FROM nvcr.io/nvidia/tritonserver:25.06-trtllm-python-py3


COPY ./tensorrtllm_backend/tensorrt_llm/examples/models/core/gemma/requirements.txt ./requirements.txt
COPY ./tensorrtllm_backend/tensorrt_llm/examples/constraints.txt ./constraints.txt
RUN pip3 install -r ./requirements.txt -c constraints.txt

