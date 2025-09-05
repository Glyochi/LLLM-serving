# offline_trtllm_generate.py
import json
import pathlib
import hashlib
import time
from typing import List, Dict, Any
import torch

import numpy as np

# TensorRT-LLM runtime API
from tensorrt_llm.runtime import ModelConfig, SamplingConfig, ModelRunner 
from tensorrt_llm.runtime import GenerationSession
import tensorrt_llm.bindings.executor as trtllm
import tensorrt_llm

# This is what tensorrt_llm model.py uses
print(trtllm)

# Hugging Face tokenizer (must match what you used to build the engine)
from transformers import AutoTokenizer

BASE_DIR = "/opt/tritonserver/projects"
ENGINE_DIR = f"{BASE_DIR}/model-repository/_assets/gemma-3-1b-it/engines"
# same dir you used at build time
TOKENIZER_DIR = f"{BASE_DIR}/model-repository/_assets/gemma-3-1b-it/tokenizer"
OUT_PATH = "./golden_gemma3.json"


def initialize_tensorrt_llm():
    # Load tokenizer
    tok = AutoTokenizer.from_pretrained(TOKENIZER_DIR, use_fast=True)

    # Load engine (single-GPU example; adapt to your engine layout)
    runner = ModelRunner.from_dir(engine_dir=ENGINE_DIR, rank=0)

    END_ID = tok.convert_tokens_to_ids("<end_of_turn>")
    # eos_token is just <eos> not <end_of_turn>, which isnt matching what Gemma 3 uses
    # END_ID = tok.convert_tokens_to_ids(tok.eos_token)
    PAD_ID = tok.pad_token_id
    if PAD_ID == None:
        PAD_ID = END_ID

    # SPECIAL_TOKENS_ATTRIBUTES = [
    #    "bos_token",
    #    "eos_token",
    #    "unk_token",
    #    "sep_token",
    #    "pad_token",
    #    "cls_token",
    #    "mask_token",
    #    "additional_special_tokens",
    # ]
    # for t in SPECIAL_TOKENS_ATTRIBUTES:
    #    print(f"{t} - {getattr(tok, t)}")

    def inference(conversation, seed, temperature, top_p, top_k, max_tokens):
        enc = tok.apply_chat_template(
            conversation["contents"], tokenize=True, return_dict=True, return_tensors="pt", add_generation_prompt=True)
        final_input_ids = torch.tensor(
            enc["input_ids"][0], device="cuda", dtype=torch.int32)

        """
        Return output in the format of (batch, beam, max_len) 
        which is then wrapped inside a dict as defined in SampleConfig
        [NOTE] Tho it's kinda weird not seeing pad token used for padding and seeing a bunch of end token at the end instead
        """
        samp = SamplingConfig(
            max_new_tokens=max_tokens,
            temperature=float(temperature),
            top_p=float(top_p),
            # <- Has to set top_k or else default to 1.0 => becomes deterministic
            top_k=float(top_k),
            # <- same RNG as Triton, which takes unsigned int 64
            random_seed=np.uint64(seed),
            end_id=END_ID,
            pad_id=PAD_ID,
            output_sequence_lengths=True,
            return_dict=True,
            # repetition_penalty=1.05,
        )
        """
        Taking out the repetition penalty because i want to validate the model tokens probability
        Repetition penalty is for later model perf benchmarking
        """
        # # Have to set it outside because init function itself dont let you initialize it?
        # samp.no_repeat_ngram_size = 4
        # samp.frequency_penalty = 1

        # Run generation (single request)
        output_dict = runner.generate(
            batch_input_ids=[final_input_ids],
            sampling_config=samp,
        )

        b = 0
        beam = 0
        # Get the final sequence length before padding/stop words
        L = int(output_dict["sequence_lengths"][b, beam])
        seq_full = output_dict["output_ids"][b, beam, :L].tolist()

        # 4) If you only want NEW tokens (exclude the prompt), drop the prompt length:
        prompt_len = final_input_ids.shape[0] if hasattr(
            final_input_ids, "shape") else len(final_input_ids)
        input_ids = seq_full[:prompt_len]
        output_ids = seq_full[prompt_len:]

        # 5) Decode ONLY the real tokens, not the padded tail:
        # Skip special tokens = false only for debugging/validating. Usually dont want to display this to user
        input = tok.decode([int(x) for x in input_ids],
                           skip_special_tokens=False)
        output = tok.decode([int(x) for x in output_ids],
                            skip_special_tokens=False)
        return input, output

    return inference


if __name__ == "__main__":
    inference = initialize_tensorrt_llm()

    conversations = {
        "contents": [{
            "role": "system",
            "content": f"You are not nice",
        },
            {
            "role": "user",
            "content": f"Tell me about groot from marvel",
        }]
    }

    input, output = inference(
        conversations, seed=1, temperature=0.0, top_p=0.9, top_k=0, max_tokens=1000)

    print("-" * 100)
    print(input)

    print("\n\n")

    print("-" * 100)
    print(output)
