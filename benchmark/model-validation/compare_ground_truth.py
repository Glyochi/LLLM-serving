import json
import random
from utils.string_compare import show_comparison
from utils.api_services import make_inference_request
from tensorrt_llm_runtime import initialize_tensorrt_llm

#tensorrt_llm_inference = initialize_tensorrt_llm()
#def query(payload):
#    try:
#        conversations = payload["conversations"]
#        seed = payload["parameters"]["seed"]
#        temperature = payload["parameters"]["temperature"]
#        top_p = payload["parameters"]["top_p"]
#        max_tokens = payload["parameters"]["max_new_tokens"]
#        input, output = tensorrt_llm_inference(conversations, seed=seed, temperature=temperature, top_p=top_p, max_tokens=max_tokens)
#        return input, output
#    except Exception as e:
#        ERROR_COUNT += 1
#        print(e)
#        return "ERROR", "ERROR"

MODEL_ID = "gemma-3-1b-it_tensorrt_llm_bls"
COMPARE_COUNT = 1
#GROUND_TRUTH_JSON_FILE_PATH = "./ground_truth/gemma-3-1b-it_tensorrt_llm_runtime_ground-truth-responses.json"
GROUND_TRUTH_JSON_FILE_PATH = "./responses-lite.json"

SERVER_URL = "http://glyml:8090/agent/complete"



ground_truth_list = []
with open(GROUND_TRUTH_JSON_FILE_PATH, "r") as file:
    ground_truth_list = json.load(file)

selected_ground_truth_list = []
tmp_set = set()
for i in range(COMPARE_COUNT):
    # Might have dup its aight
    random_index = random.randint(0, len(ground_truth_list) - 1)
    while random_index in tmp_set:
        random_index = random.randint(0, len(ground_truth_list) - 1)
    tmp_set.add(random_index)
    selected_ground_truth_list.append(ground_truth_list[random_index])


for ground_truth_response in selected_ground_truth_list:

    conversations = ground_truth_response["conversations"]
    parameters = ground_truth_response["parameters"]
    gt_input = ground_truth_response["input"]
    gt_output = ground_truth_response["output"]
    input, output = make_inference_request(SERVER_URL, MODEL_ID, ground_truth_response)
    #payload = {
    #    "conversations": conversations,
    #    "parameters": parameters
    #}
    #input, output = query(payload)

    print("\n\n")
    print("=" * 100)
    print(json.dumps(parameters, indent=4))

    print("\n\n")
    print("=" * 100)
    show_comparison(input, gt_input, width=50, sidebyside=True, compact=False)

    print("\n\n")
    print("=" * 100)
    show_comparison(output, gt_output, width=50, sidebyside=True, compact=False)

