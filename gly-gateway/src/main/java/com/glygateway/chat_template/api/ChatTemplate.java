package com.glygateway.chat_template.api;


import com.glygateway.exception.triton.ValidationException;
import com.glygateway.model.triton.Conversation;
import com.glygateway.model.triton.ModelId;

public interface ChatTemplate {
 
  public ModelId modelId();
  public String applyTemplate(String prompt) throws ValidationException;
  public String applyTemplate(Conversation convo) throws ValidationException;

} 
