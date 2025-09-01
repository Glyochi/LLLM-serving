#MODEL_NAME="gemma-2b-it"
MODEL_NAME="gemma-2b-it_tensorrt_llm_bls"
curl -X POST localhost:8000/v2/models/${MODEL_NAME}/generate -d '{"text_input": "What is machine learning?", "max_tokens": 20, "bad_words": "", "stop_words": ""}' 
