package com.gly_gateway.chat_template;

import java.util.List;

import com.gly_gateway.exception.ValidationException;
import com.gly_gateway.model.MessageData;

public interface ChatTemplateInterface {
 
  public String applyTemplate(String prompt);
  public String applyTemplate(List<MessageData> contents) throws ValidationException;

} 
