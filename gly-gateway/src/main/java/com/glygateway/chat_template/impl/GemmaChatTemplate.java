package com.glygateway.chat_template.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.glygateway.chat_template.api.ChatTemplate;
import com.glygateway.exception.triton.ValidationException;
import com.glygateway.model.triton.Conversation;
import com.glygateway.model.triton.MessageData;
import com.glygateway.model.triton.ModelId;
import com.glygateway.model.triton.Role;

@Component
public class GemmaChatTemplate implements ChatTemplate {

  private final String BOS = "<bos>";
  private final String SOT = "<start_of_turn>";
  private final String EOT = "<end_of_turn>\n";

  private String convertRoleToString(Role role) throws ValidationException {
    switch (role) {
      case USER:
        return "user\n";
      case ASSISTANT:
        return "model\n";
      default:
        throw new ValidationException(String.format("[%s] Invalid Role: '%s'", modelId(), role));
    }
  }

  public ModelId modelId() {
    return ModelId.Gemma;
  }

  public List<String> stopTokens() {
    return List.of(EOT);
  }

  public String applyTemplate(String prompt) throws ValidationException{
    return applyTemplate(new Conversation(List.of(new MessageData(Role.USER, prompt)))); 
  }

  @Override
  public String applyTemplate(Conversation convo) throws ValidationException {
    StringBuilder result = new StringBuilder();
    result.append(BOS);
    
    List<MessageData> loop_messages = convo.getContents();

    for (int i = 0; i < loop_messages.size(); i++) {
      MessageData current_message = loop_messages.get(i);
      if (current_message.getRole() == Role.USER != (i % 2 == 0)) {
        throw new ValidationException(String.format("[%s] Invalid conversation. Conversation roles must alternate user/assistant/user/assistant", modelId()));
      }

      result.append(SOT);
      result.append(convertRoleToString(current_message.getRole()));
      result.append(current_message.getContent());
      result.append(EOT);
    }
    result.append(SOT);
    result.append(convertRoleToString(Role.ASSISTANT));

    System.out.println(String.format("[%s] Chat template applied", modelId()));
    System.out.println(result.toString());
    return result.toString();
  }

}
