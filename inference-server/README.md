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
        - Right now it only support `sp model` (`https://docs.nvidia.com/deeplearning/triton-inference-server/user-guide/docs/tensorrtllm_backend/docs/gemma.html`) 
      - [Notes] sometimes, there's a missing `parameter` in one of the config file => script crash. Just add it in the source file and that should works.
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
              - 1/ `# (empty here; engines live outside under _assets)`
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

### Streaming tokens
- Some pointers here, but no tutorial on grpc request `https://docs.nvidia.com/deeplearning/triton-inference-server/user-guide/docs/tensorrtllm_backend/docs/encoder_decoder.html#run-with-decoupled-mode-streaming`
- Triton follows `Kserve convention`
- Postman needs this grpc `https://github.com/triton-inference-server/common/tree/main/protobuf`
- grpc_service.proto documentation `https://docs.nvidia.com/deeplearning/triton-inference-server/archives/triton_inference_server_1140/user-guide/docs/protobuf_api/grpc_service.proto.html`

### Model configurations
- Good read on decoding strategy `https://blog.gopenai.com/general-understanding-of-decoding-strategies-commonly-used-in-text-generation-512128bacfeb`
- To make model behaves more like a chatbot, need to modify
    - `sampling` - `https://medium.com/thinking-sand/llm-sampling-explained-selecting-the-next-token-b897b5984833`
        - Greedy, pick highest prob
        - Litearlly for loop + random to pick based on prob distribution (say c1: 10, c2: 1, c3: 4). Pick highest occurerence (c1).
    - `seed`
    - `temperature` - `https://medium.com/@kelseyywang/a-comprehensive-guide-to-llm-temperature-%EF%B8%8F-363a40bbc91f`
        - Model outputs logits, then passed to a softmax function to get probabilities distribution 
        - Let `z_i` be the logit value of token at index i, `P_i` be the probability of token at index i, `T` be temperature
            => Formula: `P_i = e^(z_i / T) / Sum_for_all_j(e^z_j / T)`
        - Look up a softmax curve, should be what `P_i` looks like
            - As `T decreases` => `(z_i / T) increases` => `P_i` grows rate increases => More definite answer as only few tokens have high probabilities => Less variety
            - As `T increases` => `(z_i / T) decreases` => `P_i` grows rate decreases => Less definite answer as everything clump together => More variety  
    - `beam_width`
        - Pseudocode: `https://www.geeksforgeeks.org/machine-learning/introduction-to-beam-search-algorithm/`
        - Application + Math: `https://medium.com/ai-assimilating-intelligence/building-intuition-on-log-probabilities-in-language-models-8fd00f34c03c`
        - Basically a `BFS + Heuristic` to pick k best node/token per steps instead of all posibilities
            - Algorithm is suboptimal, not guaranteeing optimal solution
            - Pick k per steps => Heuristic dependent
            - Best/heuristic function
                - `Log Probabilities`
                    - Need to understand alternative, compounding probabilities of all tokens per sequence
                        - Probabilities range is [0, 1]
                        - [Difficult handling] 
                            - Result probability grows closer to 0 as sequence grows 
                            - Possibly `underflow` where values close to zero are rounded to zero. 
                            - Also hard to intepret (Diff between 0.00357 and 0.00326?) 
                        - [Expensive Op - Eh idk about this one chief] 
                            - A lot of mulitplications to compound up to a sequence.
                            But it seems only for cases you have to evaluate different subsets of the same sequence `https://cs.stackexchange.com/questions/77135/why-is-adding-log-probabilities-faster-than-multiplying-probabilities`
                    - How this help
                        - Log of Probabilities is [-∞, 0]
                        - [Easier handling]
                            - Result log probabilities grows from 0 to -∞ as sequence grows
                            - No more `underflow` (unless it gets under SMALLEST_POSSIBLE_FLOAT of course) => the cool kids call it `numerically stable`
                            - Just adding log prob for each token seems easier at least by hand, easier to debug?
                            - Much easier to intepret (Diff between -2.447 and -2.486, Ok it seems slightly better but not WOW better lol)
                            - Can always revert back to probability with `prob = e ^ log(prob)` 
                        - [Cheaper Op - Eh idk about this one chief] 
                            - Compounding log probabilities is a lot summations, since `log(a * b) = log(a) + log(b)`.
                            - Still, needing to do log op, idk seems expensive ngl
                            - Idk about this one, log seems about the same as mul, if not more.
                            It seems that the only benefit in term of performance is to evaluate subsets of the same sequence => You can cache the log and only have to do additions from there. 
                    - TLDR
                        - Seems like `underflow` is the main reason why log prob is good
                        - Also easier to handle means easier to debug
                        - `Seeing model sequence's probability means that if under/over a certain threshhold`, we can intepret that and response in some way
                        (Goal for model response's prob or trigger asking for more info from prompt) 
        - Shortcomings:
            - Repetitions of same words
            - Expensive computation
            - Boring/predictable?
            - [Alternatives] Read the decoding strategy blog post
    - `top_k`
    - `top_p`
- TRT-LLM uses a default/fixed seed if not provided on per request.
-

### GEN AI for benchmarking
