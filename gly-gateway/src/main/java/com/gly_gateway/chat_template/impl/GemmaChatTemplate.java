package com.gly_gateway.chat_template.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.gly_gateway.chat_template.api.ChatTemplateInterface;
import com.gly_gateway.exception.triton.ValidationException;
import com.gly_gateway.model.triton.MessageData;
import com.gly_gateway.model.triton.Role;

@Component
public class GemmaChatTemplate implements ChatTemplateInterface {

  private final String BOS = "<bos>";
  private final String SOT = "<start_of_turn>";
  private final String EOT = "<end_of_turn>\n";

  private String convertRoleToString(Role role) throws ValidationException {
    switch (role) {
      case USER:
        return "user\n";
      case SYSTEM:
        return "user\n";
      case MODEL:
        return "model\n";
      default:
        throw new ValidationException("Invalid Role: '" + role + "'");
    }
  }

  public String applyTemplate(String prompt){
    StringBuilder result = new StringBuilder();
    result.append(BOS);
    result.append(SOT);
    result.append(Role.USER);
    result.append(prompt);
    result.append(EOT);
    result.append(SOT);
    result.append(Role.MODEL);
    return result.toString(); 
  }

  @Override
  public String applyTemplate(List<MessageData> contents) throws ValidationException {
    StringBuilder result = new StringBuilder();
    result.append(BOS);
    
    for (MessageData data : contents){
      result.append(SOT);
      result.append(convertRoleToString(data.getRole()));
      result.append(data.getContent());
      result.append(EOT);
    }
    result.append(SOT);
    result.append(convertRoleToString(Role.USER));

    return result.toString();
  }

}
