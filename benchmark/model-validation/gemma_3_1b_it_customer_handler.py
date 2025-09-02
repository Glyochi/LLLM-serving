# handler.py
from transformers import AutoTokenizer, BitsAndBytesConfig, Gemma3ForCausalLM, pipeline
import torch
import random
import numpy as np
from typing import Dict, Any
import os

class Gemma_3_1b_it_Handler:
    def __init__(self, path="."):
        model_path = path

        quantization_config = BitsAndBytesConfig(load_in_8bit=True)

        self.model = Gemma3ForCausalLM.from_pretrained(
            model_path, quantization_config=quantization_config, device_map="cuda"
        ).eval()

        self.tokenizer = AutoTokenizer.from_pretrained(model_path)

    def _apply_seed(self, seed: int | None):
        if seed is None:
            return None
        # Set global RNGs (helps with reproducibility in token sampling)
        torch.manual_seed(seed)
        np.random.seed(seed)
        random.seed(seed)
        # Create per-request generator for model.generate
        gen = torch.Generator(device=self.model.device).manual_seed(seed)
        return gen

    def __call__(self, data: Dict[str, Any]) -> Dict[str, Any]:
        messages = data.get("conversations", {}).get("contents", [])
        params = data.get("parameters") or {}

        seed = params.get("seed", random.randint(1, 1000000))
        temperature = params.get("temperature", 0.8)
        top_p = params.get("top_p", 0.9)
        max_new_tokens = params.get("max_new_tokens", 256)

        inputs = self.tokenizer.apply_chat_template(
            messages,
            add_generation_prompt=True,
            tokenize=True,
            return_dict=True,
            return_tensors="pt",
        ).to(self.model.device)
        #.to(torch.bfloat16)

        generator = self._apply_seed(seed)

        with torch.inference_mode():
            outputs = self.model.generate(**inputs, 
                top_p=top_p,
                temperature=temperature,
                max_new_tokens=max_new_tokens,
                stop_strings=["<end_of_turn>"],
                tokenizer=self.tokenizer
            )

        decoded_inputs = self.tokenizer.batch_decode(inputs["input_ids"])
        decoded_outputs = self.tokenizer.batch_decode(outputs[:, inputs["input_ids"].shape[1]:])

        return {"inputs": decoded_inputs, "outputs": decoded_outputs}


if __name__ == "__main__":
    handler = Gemma_3_1b_it_Handler(path="/home/gly/projects/netflix/inference-server/checkpoints/gemma-3-1b-it/")
    output = handler({
        "conversations": {
            "contents": [{
                "role": "user",
                "content": "Tell me a creative joke NOW"
            }]
        },
        "parameters": {
            "seed": 12
        }
    })
    print(output)
