package com.gly_gateway.chat_template;

import java.util.List;

import org.springframework.stereotype.Component;

import com.gly_gateway.exception.ValidationException;
import com.gly_gateway.model.MessageData;
import com.gly_gateway.model.Role;

@Component
public class GemmaChatTemplate implements ChatTemplateInterface {

  private final String BOS = "<bos>";
  private final String SOT = "<start_of_turn>";
  private final String EOT = "<end_of_turn>";

  private String convertRoleToString(Role role) throws ValidationException {
    switch (role) {
      case USER:
        return "user";
      case SYSTEM:
        return "user";
      case MODEL:
        return "model";
      default:
        throw new ValidationException("Invalid Role: '" + role + "'");
    }
  }

  public String applyTemplate(String prompt){
    return new String("<bos><start_of_turn>user\n" + prompt + "<end_of_turn><start_of_turn>model");
  }

  @Override
  public String applyTemplate(List<MessageData> contents) throws ValidationException {
	// TODO Auto-generated method stub
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
