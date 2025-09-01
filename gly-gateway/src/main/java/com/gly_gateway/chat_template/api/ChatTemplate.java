package com.gly_gateway.chat_template.api;


import com.gly_gateway.exception.triton.ValidationException;
import com.gly_gateway.model.triton.Conversation;
import com.gly_gateway.model.triton.ModelId;

public interface ChatTemplate {
 
  public ModelId modelId();
  public String applyTemplate(String prompt) throws ValidationException;
  public String applyTemplate(Conversation convo) throws ValidationException;

} 
