import url_paths from "../configs/Urls";
import type { Prompt } from "../models/PromptTypes";
import GlygatewayBaseService from "./GlygatewayBaseService";

class GlygatewayAgentService extends GlygatewayBaseService {
  constructor() {
    super();
  }

  async streamComplete(prompt: Prompt) {
    var path = url_paths.GlygatewayAgentService.streamComplete2;
    var url = this.url + path;
    var data = {
      conversation: {
        contents: [
          {
            role: "SYSTEM",
            content: "You are a helpful assistant.",
          },
          {
            role: "USER",
            content: "Explain quantum computing in simple terms.",
          },
        ],
      },
      inferenceParams: {
        modelId: "gemma-3-1b-it_tensorrt_llm_bls",
        maxTokens: 100,
        temperature: 1.0,
        seed: 809357414,
        topP: 0.9,
        stream: true,
      },
    };

    const response = await fetch(url, {
      method: "POST",
      headers: this.headers,
      body: JSON.stringify(prompt),
    });

    return response;
  }
}

export default new GlygatewayAgentService();
