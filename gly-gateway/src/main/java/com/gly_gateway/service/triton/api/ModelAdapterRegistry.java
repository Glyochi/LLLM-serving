package com.gly_gateway.service.triton.api;

import com.gly_gateway.model.triton.ModelId;
import com.gly_gateway.exception.triton.ValidationException;

public interface ModelAdapterRegistry {
    ModelAdapter forModelId(ModelId modelId) throws ValidationException;
}
