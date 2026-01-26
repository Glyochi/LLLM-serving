import url_paths from "../configs/Urls";
import type { Prompt } from "../models/PromptTypes";
import GlygatewayBaseService from "./GlygatewayBaseService";

class GlygatewayAgentService extends GlygatewayBaseService {
  constructor() {
    super();
  }

  async streamComplete(prompt: Prompt) {
    var path = url_paths.GlygatewayAgentService.streamComplete;
    var url = this.url + path;

    const response = await fetch(url, {
      method: "POST",
      headers: this.headers,
      body: JSON.stringify(prompt),
    });

    return response;
  }
}

export default new GlygatewayAgentService();
