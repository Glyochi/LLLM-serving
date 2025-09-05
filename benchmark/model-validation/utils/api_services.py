import requests
import sys
import unicodedata
import json


def _extract_text(obj):
    if obj is None:
        return None
    if isinstance(obj, str):
        return obj
    if isinstance(obj, dict):
        for k in ("delta", "text", "content", "token", "data", "message", "value"):
            v = obj.get(k)
            if isinstance(v, str):
                return v
            if isinstance(v, dict):
                c = v.get("content")
                if isinstance(c, str):
                    return c
        try:
            v = obj["choices"][0]["delta"].get("content")
            if isinstance(v, str):
                return v
        except Exception:
            pass
        return None
    if isinstance(obj, list):
        parts = [p for p in obj if isinstance(p, str)]
        return "".join(parts) if parts else None
    return None


def _looks_alnum(c):
    # True for letters or digits (Unicode aware)
    return c and (c.isalnum() or unicodedata.category(c).startswith("L") or unicodedata.category(c).startswith("N"))


def _normalize_piece(piece):
    if not piece:
        return piece
    # 1) SentencePiece: turn ▁ into space
    piece = piece.replace("▁", " ")
    return piece


def _maybe_insert_space(prev_char, piece):
    """
    Insert a space if last emitted char and first non-space of current piece are both alnum.
    Avoids adding spaces before common closing punctuation.
    """
    if not piece:
        return piece

    # If piece already begins with whitespace or newline, don't touch
    if piece[:1].isspace():
        return piece

    first = piece[0]

    # Don't add space before closing punctuation
    if first in ".,!?;:)]}”’\"":
        return piece

    # Add a space if previous is alnum and current starts alnum
    if _looks_alnum(prev_char) and _looks_alnum(first):
        return " " + piece
    return piece


def make_stream_inference_request(server_url, model_id, ground_truth_response):
    conversation = ground_truth_response["conversations"]
    inference_params = ground_truth_response["parameters"]
    input = ground_truth_response["input"]
    output = ground_truth_response["output"]

    # Format for gly-gateway server
    conversation_content = conversation["contents"]
    for message in conversation_content:
        message["role"] = message["role"].upper()

    inference_params["stream"] = False
    inference_params["modelId"] = model_id
    inference_params["maxTokens"] = inference_params["max_new_tokens"]
    del inference_params["max_new_tokens"]

    data = {
        "conversation": conversation,
        "inferenceParams": inference_params
    }
    with requests.post(server_url, headers={
        "Content-Type": "application/json"
    }, json=data, stream=True) as r:
        r.raise_for_status()
        ctype = r.headers.get("Content-Type", "")

        acc = []
        last_char = ""  # track last emitted character to decide spacing

        for line in r.iter_lines(decode_unicode=True):
            if not line or line.startswith(":"):
                continue

            if line.startswith("event:"):
                # Optional: handle named events if your server uses them
                continue

            if line.startswith("data:"):
                data = line[len("data:"):].strip()
                if data in ("[DONE]", "DONE", "done"):
                    break

                try:
                    obj = json.loads(data)
                except json.JSONDecodeError:
                    obj = data

                piece = _extract_text(obj)
                if piece is None and isinstance(obj, str):
                    piece = obj

                if piece:
                    piece = _normalize_piece(piece)
                    # Convert escaped newlines if server sent raw strings (json.loads will already have real \n)
                    # piece = piece.encode("utf-8").decode("unicode_escape")  # enable only if you actually see literal "\n"
                    piece = _maybe_insert_space(last_char, piece)

                    acc.append(piece)
                    # sys.stdout.write(piece)
                    # sys.stdout.flush()
                    last_char = piece[-1]

        return "".join(acc)

def format_body(ground_truth_response, model_id):

    conversation = ground_truth_response["conversations"]
    convo_contents = conversation["contents"]
    formatted_convo_contents = []
    for message in convo_contents:
        formatted_convo_contents.append({
            "role": message["role"].upper(),
            "content": message["content"]
        })
        
    parameters = ground_truth_response["parameters"]

    data = {
        "conversation": {
            "contents": formatted_convo_contents,
        },
        "inferenceParams": {
            "modelId": model_id,
            "maxTokens": parameters["max_new_tokens"],
            "temperature": parameters["temperature"],
            "seed": parameters["seed"],
            "topP": parameters["top_p"],
            "topK": parameters["top_k"],
            "stream": False
        }
    }
    return data

def make_inference_request(server_url, model_id, ground_truth_response):
    conversation = ground_truth_response["conversations"]
    inference_params = ground_truth_response["parameters"]

    # Format for gly-gateway server

    data = format_body(ground_truth_response, model_id)
    print("-" * 100)
    print(json.dumps(data, indent=4))
    response = requests.post(server_url, headers={
        "Content-Type": "application/json"
    }, json=data)

    response_data = response.json()
    print(response_data)
    input = response_data["input"]
    output = response_data["output"]
    return input, output

