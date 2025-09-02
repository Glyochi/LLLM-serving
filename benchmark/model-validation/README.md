# Model validation
## Methodology
- To validate the models hosted by triton, we need to compare their outputs with some sort of ground truth
- I decided we go with hosting a model using `transformers` library, example came straight from `huggingface` (`https://huggingface.co/google/gemma-3-1b-it`)
    - It's the easiest approach
    - Can compare both `template correctness` and `output correctness`

## Set up 
- [model_specific]_custom_handler_requirements.txt
    - Models might have different dependencies. It's best to keep the separated
- [model_specific]_custom_handler.py
    - Load models from HF checkpoint
    - Apply template on the inputs
    - Set the seed to maintain reproducibility
    - Generate responses
- main.py
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


