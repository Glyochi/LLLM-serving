package com.gly_gateway.service.triton.api;

import com.gly_gateway.exception.triton.ValidationException;

public interface ModelAdapterRegistry {
    ModelAdapter forModelId(String modelId) throws ValidationException;   // or by enum ModelType
}
