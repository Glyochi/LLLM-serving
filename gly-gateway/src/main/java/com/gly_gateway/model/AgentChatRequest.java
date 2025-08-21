package com.gly_gateway.model;

import java.util.UUID;

public class AgentChatRequest {

  private String prefill;

  public AgentChatRequest() {
  }

  public AgentChatRequest(String prefill) {
    this.prefill = prefill;
  }

  @Override
  public String toString() {
    return "AgentChatRequest{" +
      "prefill='" + prefill + '\'' + 
      '}';
  }

  public String getPrefill() {
    return prefill;
  }

  public void setPrefill(String prefill) {
    this.prefill = prefill;
  }

}
