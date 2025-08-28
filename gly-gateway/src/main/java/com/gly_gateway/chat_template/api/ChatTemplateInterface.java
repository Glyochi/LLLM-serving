package com.gly_gateway.chat_template.api;

import java.util.List;

import com.gly_gateway.exception.triton.ValidationException;
import com.gly_gateway.model.triton.MessageData;

public interface ChatTemplateInterface {
 
  public String applyTemplate(String prompt);
  public String applyTemplate(List<MessageData> contents) throws ValidationException;

} 
