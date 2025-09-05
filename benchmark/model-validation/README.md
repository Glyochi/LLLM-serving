# Model validation
## Methodology
- To validate the models hosted by triton, we need to compare their outputs with some sort of ground truth

## Set up
- generate_responses.py
    - Load prompts.json/prompts-lite.json
    - Mix and match all variations and feed that to the model
    - Write to response.json/response-lite.json
- prompts.json/prompts-lite.json
    - contains input variations such as
        - `top_p` 
        - `temperature`
        - `seed`
        - `system_prompt`
        - `prompt`
- response.json/response-lite.json
    - contains outputs generated from input variations
        - `conversations`
            - `contents` which is an array of
                - `role` of the messager (user/system/model)
                - `content` is the prompt itself
        - `parameters`
            - The mixed and matched paramaters loaded from `prompts.json`
        - `input`
            - This is the `conversation` after `chat template` has been applied
            - Should be 1 string, with all the <> tokens
        - `output`
            - This is the generated response of the model
            - It DOES NOT contain the input prompt
            - It should stops with `<end_of_turn>` if the model finishes before running out of max tokens
- compare_ground_truth.py
    - randomly select one request/response from generate_responses.py, 
    then make that requests to (triton llm, tensorrt_llm.runtime, or hf + transformers)
    - compare the `template correctness` and `output correctness` visually side-by-side


## Approaches
### [FAILED] HF - Transformers library
- I decided we go with hosting a model using `transformers` library 
    - It's the easiest approach
    - Can compare both `template correctness` and `output correctness`
    - HOWEVER, not using the built .engine file but the checkpoint from HF
- [model_specific]_custom_handler_requirements.txt
    - Models might have different dependencies. It's best to keep them separated
- [model_specific]_custom_handler.py 
    - Custom handler for inference endpoint (cloud) if you want it on the cloud `https://huggingface.co/docs/inference-endpoints/en/guides/custom_handler`
    - Gemma 3 example `https://huggingface.co/google/gemma-3-1b-it`
    - Load models from HF checkpoint
    - Apply template on the inputs
    - Set the seed to maintain reproducibility
    - Generate responses
- [FINDINGS] 
    - It seems that the seed used by `transformer` isn't the same as for triton. So I cannot compare the generated output correctness.
    - Tho `tokenizer` library is still usable to validate `template correctness`


### [FAILED] Triton tensorrt_llm.runtime
- Reuse the same container to host triton server, tap into its `tensorrt_llm.runtime` and do inference directly
    - Any .engine files building/environment discrepencies should be gone 
    - SHOULD BE EXACTLY THE SAME as Triton LLM output
- tensorrt_llm_runtime.py
    - Loads the .engine files + tokenizer files directly from triton model-repository
- start_benchmark_container.sh
    - Mounts the code + all the needed dependencies onto the containers, similar to start_triton.sh script
- [FINDINGS]
    - Idk even when setting
        - top_k = 1
        - top_p = 0
        - temperature = 1
    - The output getting from `triton llm` server still differs from `tensorrt_llm.runtime`
    - Also it seems that for `triton llm`, in `tensorrt_llm` model, its using `tensorrt_llm.binding.executor as trtllm` to do inferencing.
    Not entirely sure how is that different from just importing from `tensorrt_llm.runtime`.

### [BONUS] injecting code to `tensorrt_llm_bls model.py`
- Inject some print statements into model.py to print out the SamplingConfig sent from the java server
- [FINDINGS]
    - Most of the time the values match. What is sent from java server is the same as on triton
    - There's some numerical unstability when handling float numbers (0.9 becomes 0.8999999)


# VERDICT
- For now, leave it as is. But might need to come back and dive into C code to understand `tensorrt_llm.binding.executor` more
- Or maybe there's a better way to validate model's output correctness
