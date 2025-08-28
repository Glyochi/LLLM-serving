package com.gly_gateway.model.triton;

import com.gly_gateway.model.triton.Role;
import jakarta.validation.constraints.NotNull;

public class MessageData {

  @NotNull
  final Role role;
  @NotNull
  final String content;

  public MessageData(Role role, String content) {
    this.role = role;
    this.content = content;
  }

  public String toString() {
    return "<MessageData: role '" + role + "', contents '" + content + "'>";
  }

  public Role getRole() {
	return role;
  }

  public String getContent() {
	return content;
  }

}
