package com.glygateway.service.triton.api;

import com.glygateway.model.triton.ModelId;
import com.glygateway.exception.triton.ValidationException;

public interface ModelAdapterRegistry {
    ModelAdapter forModelId(ModelId modelId) throws ValidationException;
}
