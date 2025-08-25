package com.gly_gateway.model;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


public class AgentChatRequest {

  @Valid
  @NotNull
  final List<MessageData> contents;

  public AgentChatRequest(List<MessageData> contents) {
    this.contents = contents;
  }

  @Override
  public String toString() {
    return "AgentChatRequest{" +
      "contents='" + contents + '\'' + 
      '}';
  }

  public List<MessageData> getContents() {
	return contents;
  }

}
