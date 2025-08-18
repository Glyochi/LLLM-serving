### Following this guide
- https://github.com/triton-inference-server/tutorials/blob/main/Popular_Models_Guide/Llama2/trtllm_guide.md

### General Requirements
- Nvidia Drivers + Nvidia-container-toolkit + tritonserver container 

### Project structure 
- scripts: helpful scripts
    - Building .engine files
    - Filling configs for `model-repository`
    - Starting triton server
    - .etc
- checkpoints: contains HG face checkpoints, downloaded to be used for building .engine files
- Dockerfiles: Collection of Dockerfile to build images with dependencies on top of `nvcr.io/nvidia/tritonserver:25.06-trtllm-python-py3`.
    - Installing dependencies on top of the base image manually doesn't always work. And have to be redone everytime the container is brought up.
    - Just quality of life thing.
- engines: where all the .engines files should be outputed to 
- tensorrtllm_backend: git repo of tensorrtllm_backend, which has tensorrt_llm inside
    - Needed for scripts/dependencies for building .engine files
    - Needed for skeleton config.pbtxt files for `model-repository` (git lfs pull, main branch)
- model-repository: `model-repository` with a specific structure
- start_triton_container_env.sh:
    - Bash script to start container with everything mounted
    - All volumes are mounted to `/opt/tritonserver/projects`
    - [Building .engine files]:
        - tensorrtllm_backend 
        - engines
        - checkpoints
        - scripts
    - [Running triton]:
        - model-repository
        - scripts

### Building .engine files
# Requirements
- Custom Docker image built with dependencies installed on it `/tensorrtllm_backend/tensorrt_llm/example/{model_family}/requirements.txt`
- Model checkpoint downloaded from huggingface (git clone with lfs). Not the raw one from huggingface-cli
- Engine files built from that model inside the customer container
    - There's instruction on how to convert hf checkpoint to triton consumalbe (.onnx?) and then convert to .engine files in `Tensorrtllm_backend/tensorrt_llm/examples/{model_family}/README.md`
    - Don't always work lol

# Llama 7b
- Follow the README.md should be good enough
- Quantize is iffy need more experiment has not gotten far with it yet
- Too big for 4090 still

# Gemma 2b/Gemma 2b it
- Follow the README.md but need to modify convert_checkpoint.py, bug in GemmaConfig.from_hugging_face (quant_config somehow mistook as mapping python wth)
- Fit noicely on 4090

### Setting up model-repository for Triton 
[YOU HAVE TO MAINTAIN THE STRUCTURE YOURSELF AND MOVE THE TOKENIZER/ENGINE FILES TO WHERE THEY SUPPOSED TO BE]
Need to configure `config.pbtxt` file in the `model_repository` 
  - File content: `https://github.com/NVIDIA/TensorRT-LLM/tree/main/triton_backend/all_models/inflight_batcher_llm`
  - What they mean: `https://github.com/triton-inference-server/tensorrtllm_backend/blob/main/docs/model_config.md#tensorrt_llm_bls-model`
  - Use `scripts/fill_config.sh` to populate the config.pbtxt files.
      - Make sure to update the script for the current model
      - Set up tensorrtllm_backend and pull with git lfs so we have skeleton config.pbtxt
  - Folder structure for gemma-2b-it:
      - model-repository/
          - tensorrt_llm_bls/
              - 1/
                  - model.py `# Orchestrates the loop & streaming`
              - config.pbtxt
          - preprocessing/
              - 1/
                  - model.py `# Tokenizer (HF AutoTokenizer)`
              config.pbtxt
          - tensorrt_llm/
              - 1/
                  # (empty here; engines live outside under _assets)
              - config.pbtxt
          - postprocessing/
              - 1/
                  - model.py `# Detokenizer`
              - config.pbtxt

          - _assets/
              - gemma-2b-it/
                  - tokenizer/ `# HF tokenizer files. Get these from hf checkpoint`
                      - tokenizer_config.json
                      - tokenizer.json
                      - tokenizer.model
                  - engines/ `# TRT-LLM engine_dir produced by build`
                      - config.json
                      - rank0.engine (or more) `# Built engine file for gemma-2b-it`



