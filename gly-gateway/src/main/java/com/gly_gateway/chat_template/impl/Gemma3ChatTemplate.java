package com.gly_gateway.chat_template.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.gly_gateway.chat_template.api.ChatTemplate;
import com.gly_gateway.exception.triton.ValidationException;
import com.gly_gateway.model.triton.Conversation;
import com.gly_gateway.model.triton.MessageData;
import com.gly_gateway.model.triton.ModelId;
import com.gly_gateway.model.triton.Role;

@Component
public class Gemma3ChatTemplate implements ChatTemplate {

  private final String BOS = "<bos>";
  private final String SOT = "<start_of_turn>";
  private final String EOT = "<end_of_turn>\n";

  private String convertRoleToString(Role role) throws ValidationException {
    switch (role) {
      case SYSTEM:
        return "user\n";
      case USER:
        return "user\n";
      case ASSISTANT:
        return "model\n";
      default:
        throw new ValidationException(String.format("[%s] Invalid Role: '%s'", modelId(), role));
    }
  }

  public ModelId modelId() {
    return ModelId.Gemma3;
  }

  public String applyTemplate(String prompt) {
    return "";
  }

  @Override
  public String applyTemplate(Conversation convo) throws ValidationException {
    StringBuilder result = new StringBuilder();
    result.append(BOS);

    String first_user_prefix = "";
    List<MessageData> contents = convo.getContents();
    List<MessageData> loop_messages = contents;
    if (contents.get(0).getRole() == Role.SYSTEM) {
      first_user_prefix = contents.get(0).getContent() + "\n\n";
      loop_messages = contents.subList(1, contents.size());
    }

    for (int i = 0; i < loop_messages.size(); i++) {
      MessageData current_message = loop_messages.get(i);
      if (current_message.getRole() == Role.USER != (i % 2 == 0)) {
        throw new ValidationException(String.format("[%s] Invalid conversation. Conversation roles must alternate (system - not required)/user/assistant/user/assistant", modelId()));
      }
      result.append(SOT);
      if (i == 0) {
        result.append(first_user_prefix);
      }
      result.append(convertRoleToString(current_message.getRole()));
      result.append(current_message.getContent());
      result.append(EOT);
    }
    result.append(SOT);
    result.append(convertRoleToString(Role.USER));

    return result.toString();
  }

}
