import os
import json
import tempfile
from gemma_3_1b_it_custom_handler import Gemma_3_1b_it_Handler
from tensorrt_llm_runtime import initialize_tensorrt_llm
from tqdm import tqdm


PROMPTS_JSON_FILE_PATH = "./prompts-lite.json"
MODEL_RESPONSES_JSON_FILE_PATH = "./responses-lite.json"
# PROMPTS_JSON_FILE_PATH = "./prompts.json"
# MODEL_RESPONSES_JSON_FILE_PATH = "./responses.json"

ERROR_COUNT = 0

#handler = Gemma_3_1b_it_Handler(
#    path="/home/gly/projects/netflix/inference-server/checkpoints/gemma-3-1b-it/")
#
#
#def query(payload):
#    try:
#        response = handler(payload)
#        input = response["inputs"][0]
#        output = response["outputs"][0]
#        return input, output
#    except Exception as e:
#        ERROR_COUNT += 1
#        print(e)
#        return "ERROR", "ERROR"

tensorrt_llm_inference = initialize_tensorrt_llm()
def query(payload):
    try:
        conversations = payload["conversations"]
        seed = payload["parameters"]["seed"]
        temperature = payload["parameters"]["temperature"]
        top_p = payload["parameters"]["top_p"]
        top_k = payload["parameters"]["top_k"]
        max_tokens = payload["parameters"]["max_new_tokens"]
        input, output = tensorrt_llm_inference(conversations, seed=seed, temperature=temperature, top_p=top_p, top_k=top_k, max_tokens=max_tokens)
        return input, output
    except Exception as e:
        ERROR_COUNT += 1
        print(e)
        return "ERROR", "ERROR"



prompt_list = []
system_prompt_list = []
top_p_list = []
top_k_list = []
temperature_list = []
seed_list = []
max_new_tokens = 96
with open(PROMPTS_JSON_FILE_PATH, "r") as file:
    data = json.load(file)
    prompt_list = data["prompt"]
    system_prompt_list = data["system_prompt"]
    system_prompt_list.append("")
    top_p_list = data["top_p"]
    top_k_list = data["top_k"]
    temperature_list = data["temperature"]
    seed_list = data["seed"]


prompt_list = prompt_list[:1]
total_work = len(seed_list) * len(temperature_list) * \
    len(top_p_list) * len(top_k_list) * \
    len(prompt_list) * len(system_prompt_list)
print(f"TOTAL WORK: {total_work}")
pbar = tqdm(total=total_work)

gt_responses_data = []
for seed in seed_list:
    for temperature in temperature_list:
        for top_p in top_p_list:
            for top_k in top_k_list:
                for prompt in prompt_list:
                    for system_prompt in system_prompt_list:
                        if system_prompt == "":
                            conversations = {
                                "contents": [{
                                    "role": "user",
                                    "content": f"{prompt}",
                                }]
                            }
                        else:
                            conversations = {
                                "contents": [{
                                    "role": "system",
                                    "content": f"{system_prompt}",
                                },
                                    {
                                    "role": "user",
                                    "content": f"{prompt}",
                                }]
                            }

                        parameters = {
                            "seed": seed,
                            "do_sample": True,
                            "temperature": temperature,
                            "top_p": top_p,
                            "top_k": top_k,
                            "max_new_tokens": max_new_tokens

                        }
                        # print(json.dumps(parameters, indent=4))

                        payload = {
                            "conversations": conversations,
                            "parameters": parameters
                        }
                        input, output = query(payload)

                        gt_data = {
                            "conversations": conversations,
                            "parameters": parameters,
                            "input": input,
                            "output": output
                        }
                        gt_responses_data.append(gt_data)
                        with open(MODEL_RESPONSES_JSON_FILE_PATH, "w") as responses_file:
                            json.dump(gt_responses_data, responses_file, indent=4)
                        pbar.update(1)
pbar.close()
print(f"ERROR COUNT: {ERROR_COUNT}")
