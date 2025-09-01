# Following this guide
- `https://github.com/triton-inference-server/tutorials/blob/main/Popular_Models_Guide/Llama2/trtllm_guide.md`
- Good read `https://docs.nvidia.com/deeplearning/triton-inference-server/user-guide/docs/getting_started/trtllm_user_guide.html`
    - KV cache management and KV cache reused
    - Triton Decoding strategy + spec decoding
    

# General Requirements
- Nvidia Drivers + Nvidia-container-toolkit + tritonserver container 
- Cloned repos of tensorrtllm_backend and tensorrt_llm (should be a folder in tensorrtllm_backend)
    - tensorrtllm_backend (latest, doesnt seem to matter that much) `https://github.com/triton-inference-server/tensorrtllm_backend`
    - tensorrt_llm (latest at the time `v0.21.0`) `https://github.com/NVIDIA/TensorRT-LLM/releases/tag/v0.21.0`
        - Needed for
            - building .engines files
                - It's model family specific so navigate to `/tensorrtllm_backend/tensorrt_llm/example/models/core/{model_family}/README.md`
            - triton_backend folder for `model-repository`, which has
                - config.pbtxt files (ensemble, preprocessing, postprocessing, tensorrt_llm, tensorrt_llm_bls)
                - fill_template.py in tools

# Project structure 
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

# Building .engine files
## Requirements
- Custom Docker image built with dependencies installed on it `/tensorrtllm_backend/tensorrt_llm/example/{model_family}/requirements.txt`
- Model checkpoint downloaded from huggingface (git clone with lfs). Not the raw one from huggingface-cli
- Engine files built from that model inside the customer container
    - There's instruction on how to convert hf checkpoint to triton consumalbe (.onnx?) and then convert to .engine files in `Tensorrtllm_backend/tensorrt_llm/examples/{model_family}/README.md`
    - Don't always work lol

## Llama 7b
- Follow the README.md should be good enough
- Quantize is iffy need more experiment has not gotten far with it yet
- Too big for 4090 still

## Gemma 2b/Gemma 2b it
- Follow the README.md but need to modify convert_checkpoint.py, bug in GemmaConfig.from_hugging_face (quant_config somehow mistook as mapping python wth)
- Fit noicely on 4090

# Setting up model-repository for Triton 
[YOU HAVE TO MAINTAIN THE STRUCTURE YOURSELF AND MOVE THE TOKENIZER/ENGINE FILES TO WHERE THEY SUPPOSED TO BE]
Need to configure `config.pbtxt` file in the `model_repository` 
  - File content: `https://github.com/NVIDIA/TensorRT-LLM/tree/main/triton_backend/all_models/inflight_batcher_llm`
  - What they mean: `https://github.com/triton-inference-server/tensorrtllm_backend/blob/main/docs/model_config.md#tensorrt_llm_bls-model`
  - Use `scripts/fill_config.sh` to populate the config.pbtxt files.
      - Make sure to update the script for the current model
        - Right now it only support `sp model` (`https://docs.nvidia.com/deeplearning/triton-inference-server/user-guide/docs/tensorrtllm_backend/docs/gemma.html`) 
      - [Notes] sometimes, there's a missing `parameter` in one of the config file => script crash. Just add it in the source file and that should works.
      - Set up tensorrtllm_backend and pull with git lfs so we have skeleton config.pbtxt
  - Folder structure for `single model deployment`:
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
    - For `multiple model deployment`, it's a little bit more tricky
        - Triton treats each of these `preprocessing`, `postprocessing`, etc... folder as `one model`, which could run on singular instance or multiple instances (For single gpu 1 instance is usually good enough)
        - These `models` need to have the same name for
            - name of the model in config.pbtxt
            - folder name
            - For example, if i have custom folder called `gemma-gly-preprocessing`, that will be come the model name, and it has to be the same inside config.pbtxt file inside that folder
        - `tensorrt_llm_bls` is in charged of coordinating all these models into a pipeline, so it needs to get the correct model names
            - You can change the `field tensorrt_llm_model_name` in the config.pbtxt file to point to the correct custom model for inferencing (say `gemma-gly-tensorrt_llm`)
            - BUT YOU CANNOT change the fields for `preprocessing` and `postprocessing`, they are hardcoded in the mode.py file for `tensorrt_llm_bls`
                - You could modify the mode.py file to add two new fields in config.pbtxt (Doable)
                - I went with just swapping out the hardcoded values in the `fill_config.sh` script. 
                    - It's the easier way as I don't have to worry about changing the code and keeping track if my source file model.py is the default one or my modified version
                    - Also its like 5 more lines of code compared to ehhh
        - There's also another option to make triton takes in multiple model-repository.
            - Haven't explored this yet, but likely gonna run into the same naming issues (Could be wrong), given that to do inference you have to specified the custom model name
        - model-repository/ (MOSTLY THE SAME OTHER THAN THE FOLDER NAME/CUSTOM MODEL NAME)
            - gemma-gly-tensorrt_llm_bls/
                - 1/
                    - model.py `# Orchestrates the loop & streaming`
                - config.pbtxt
            - gemma-gly-preprocessing/
                - 1/
                    - model.py `# Tokenizer (HF AutoTokenizer)`
                config.pbtxt
            - gemma-gly-tensorrt_llm/
                - 1/ `# (empty here; engines live outside under _assets)`
                - config.pbtxt
            - gemma-gly-postprocessing/
                - 1/
                    - model.py `# Detokenizer`
                - config.pbtxt
            - other-custom-model-names
            - _assets/



# Streaming tokens
- Some pointers here, but no tutorial on grpc request `https://docs.nvidia.com/deeplearning/triton-inference-server/user-guide/docs/tensorrtllm_backend/docs/encoder_decoder.html#run-with-decoupled-mode-streaming`
- Triton follows `Kserve convention`
- Postman needs this grpc `https://github.com/triton-inference-server/common/tree/main/protobuf`
- grpc_service.proto documentation `https://docs.nvidia.com/deeplearning/triton-inference-server/archives/triton_inference_server_1140/user-guide/docs/protobuf_api/grpc_service.proto.html`

# Model configurations
- Good read on decoding strategy `https://blog.gopenai.com/general-understanding-of-decoding-strategies-commonly-used-in-text-generation-512128bacfeb`
    - TLDR: no clear answer which is the best
    - Either do benchmark for the task itself
    - Or read online and see what strategy is best suited for my task and send it
- Decoding strategy for better chat bot experiences 
    - `seed`
        - Random seed lol, for sampling random generator
    - `sampling` - `https://medium.com/thinking-sand/llm-sampling-explained-selecting-the-next-token-b897b5984833`
        - Greedy, pick highest prob
        - Litearlly for loop + random to pick based on prob distribution (say c1: 10, c2: 1, c3: 4). Pick highest occurerence (c1).
    - `temperature` - `https://medium.com/@kelseyywang/a-comprehensive-guide-to-llm-temperature-%EF%B8%8F-363a40bbc91f`
        - Model outputs logits, then passed to a softmax function to get probabilities distribution 
        - Let `z_i` be the logit value of token at index i, `P_i` be the probability of token at index i, `T` be temperature
            => Prob formula: `P_i = e^(z_i / T) / Sum_for_all_j(e^z_j / T)`
            => Log Prob formula: `log(P_i) = z_i / T - log(Sum_for_all_j(e^(z_j / T)))`
        - Look up a softmax curve, should be what `P_i` looks like
            - As `T decreases` => `(z_i / T) increases` => `P_i` grows rate increases => More definite answer as only few tokens have high probabilities => Less variety
            - As `T increases` => `(z_i / T) decreases` => `P_i` grows rate decreases => Less definite answer as everything clump together => More variety  
        - It's very subjective in term of how `good` a model is. Benchmarking is needed (`promptfoo` is used in the article if output can be defined easily)
    - `top_k`
        - Used with `sampling` technique, to remove the least probable tokens out of the random equation
        - Need to normalize the probabilities after trim down to only `k` tokens
        - [Practical application] it seems that `sampling` + `top_k` + decently high `temperature` gives `diverse and high-quality` response compared to `beam search` 
    - `top_p`
        - Also used with `sampling` technique just like `top_k`, but instead of fixed cut-off, it dynamically cut off tokens only when the compounded probabilities of selected token exceeded `p`
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
- TRT-LLM uses a default/fixed seed if not provided on per request.
- [TODO] Look into speculative decoding
    - How does a big model `validate?` the accuracy of the generated tokens from draft models/speculative heads. Need more reading.
    - EAGLE (draft models)
    - MEDUSA (need to train the speculative heads)
    - T5 model
    - Pytorch blog post claims Medusa speculative heads is more affective in term of quality/latency gains than using draft models `https://pytorch.org/blog/hitchhikers-guide-speculative-decoding/`
    - Triton recommended EAGLE over MEDUSA for 1.6x speedup + 0.8 over 0.6 accuracy `https://docs.nvidia.com/deeplearning/triton-inference-server/user-guide/docs/tutorials/Feature_Guide/Speculative_Decoding/TRT-LLM/README.html#medusa`
- `Triton/GPU terminology`
    - Scheduler: a request router, which routes to instances
        - Dynamic batching
    - Instance: an executor, with its own weights/workspace/KV cache, running on its own CUDA stream
        - With multiple instances, triton splits traffic accross them.
        - With LLM, the model has instances such as
            - preprocessing (CPU)
            - tensorrt_llm (GPU)
            - postprocessing (CPU)
            - tensorrt_llm_bls or ensemble (CPU)
        - Each existing as 1 instance. And for single gpu 1 instance is good enough. Should use batching or in-flight/continous batching to increase throughput
    - 

# GEN AI for benchmarking
