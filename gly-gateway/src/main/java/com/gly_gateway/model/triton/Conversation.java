package com.gly_gateway.model.triton;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class Conversation {

  @Valid
  @NotNull
  final List<MessageData> contents;

  public Conversation(List<MessageData> contents) {
    this.contents = contents;
  }

  @Override
  public String toString() {
    return "Conversation{" +
        "contents='" + contents + '\'' +
        '}';
  }

  public List<MessageData> getContents() {
    return contents;
  }

}
