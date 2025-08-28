package com.gly_gateway.service.triton.core;

import com.gly_gateway.exception.triton.ValidationException;
import com.gly_gateway.service.triton.api.ModelAdapter;
import com.gly_gateway.service.triton.api.ModelAdapterRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DefaultModelAdapterRegistry implements ModelAdapterRegistry {
  private final Map<String, ModelAdapter> byId;

  public DefaultModelAdapterRegistry(List<ModelAdapter> adapters) {
    this.byId = adapters.stream()
        .collect(Collectors.toUnmodifiableMap(ModelAdapter::modelId, Function.identity()));
  }

  @Override
  public ModelAdapter forModelId(String modelId) throws ValidationException {
    var adapter = byId.getOrDefault(modelId, null);
    if (adapter == null) {
      throw new ValidationException("Unknown modelId: " + modelId);
    }
    return adapter;
  }
}
