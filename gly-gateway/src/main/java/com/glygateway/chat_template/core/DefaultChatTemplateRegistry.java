package com.glygateway.chat_template.core;

import com.glygateway.model.triton.ModelId;
import com.glygateway.chat_template.api.ChatTemplate;
import com.glygateway.chat_template.api.ChatTemplateRegistry;
import com.glygateway.exception.triton.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DefaultChatTemplateRegistry implements ChatTemplateRegistry {
  private final Map<ModelId, ChatTemplate> byId;

  public DefaultChatTemplateRegistry(List<ChatTemplate> adapters) {
    this.byId = adapters.stream()
        .collect(Collectors.toUnmodifiableMap(ChatTemplate::modelId, Function.identity()));
  }

  @Override
  public ChatTemplate forModelId(ModelId modelId) throws ValidationException {
    var adapter = byId.getOrDefault(modelId, null);
    if (adapter == null) {
      throw new ValidationException("Unknown modelId: " + modelId);
    }
    return adapter;
  }
}
