import type { QueryClient } from "@tanstack/react-query";
import type { Conversation, InferenceParams, Message, Prompt } from "../models/PromptTypes";
import { global_keys, default_inference_params } from "../configs/Constants";

type NdjsonOnMessage = (msg: any) => void;
type NdjsonOnParseError = (err: Error, badLine: string) => void;

async function consumeNdjsonResponse(
  response: Response,
  onMessage: NdjsonOnMessage,
  onParseError: NdjsonOnParseError | null,
): Promise<void> {
  if (!response.ok) {
    var errText: string = "";
    try {
      errText = await response.text();
    } catch (e) {
      errText = "";
    }
    throw new Error("HTTP " + response.status + " " + response.statusText + (errText ? ": " + errText : ""));
  }

  if (response.body == null) {
    throw new Error("Streaming not supported: response.body is null");
  }

  var reader: ReadableStreamDefaultReader<Uint8Array> = response.body.getReader();
  var decoder: TextDecoder = new TextDecoder("utf-8");
  var buffer: string = "";

  try {
    while (true) {
      var result = await reader.read();
      var done: boolean = result.done === true;
      var value: Uint8Array | undefined = result.value;

      if (done) {
        break;
      }
      if (value) {
        buffer += decoder.decode(value, { stream: true });

        // Consume complete lines
        while (true) {
          var newlineIndex: number = buffer.indexOf("\n");
          if (newlineIndex === -1) {
            break;
          }

          var line: string = buffer.slice(0, newlineIndex);
          buffer = buffer.slice(newlineIndex + 1);

          var trimmed: string = line.trim();
          if (trimmed.length === 0) {
            continue;
          }

          try {
            var obj: any = JSON.parse(trimmed);

            onMessage(obj);
          } catch (e: any) {
            var parseErr: Error = e instanceof Error ? e : new Error(String(e));
            if (onParseError != null) {
              onParseError(parseErr, trimmed);
            }
            throw new Error("Failed to parse NDJSON line: " + trimmed);
          }
        }
      }
    }

    // Flush decoder + parse remaining buffer (in case final \n missing)
    buffer += decoder.decode();
    var tail: string = buffer.trim();
    if (tail.length > 0) {
      var lastObj: any = JSON.parse(tail);
      onMessage(lastObj);
    }
  } finally {
    reader.releaseLock();
  }
}

function getPromptWithGlobalSettings(client: QueryClient, conversation: Conversation) {
  const inferenceParams: InferenceParams = {
    modelId: "gemma-3-1b-it_tensorrt_llm_bls",
    maxTokens: client.getQueryData([global_keys.maxTokens]) ?? default_inference_params.maxTokens,
    temperature: client.getQueryData([global_keys.temperature]) ?? default_inference_params.temperature,
    seed: client.getQueryData([global_keys.seed]) ?? default_inference_params.seed,
    topP: client.getQueryData([global_keys.topP]) ?? default_inference_params.topP,
    stream: true,
  };
  const prompt: Prompt = { conversation: conversation, inferenceParams: inferenceParams };
  return prompt;
}

export { consumeNdjsonResponse, getPromptWithGlobalSettings };
