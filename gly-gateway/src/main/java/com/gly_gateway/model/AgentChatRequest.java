package com.gly_gateway.model;

public class AgentChatRequest {

  private String prompt;

  public AgentChatRequest() {
  }

  public AgentChatRequest(String prompt) {
    this.prompt = prompt;
  }

  @Override
  public String toString() {
    return "AgentChatRequest{" +
      "prompt='" + prompt + '\'' + 
      '}';
  }

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

}
