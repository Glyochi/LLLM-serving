package com.glygateway.chat_template.api;

import com.glygateway.model.triton.ModelId;
import com.glygateway.exception.triton.ValidationException;

public interface ChatTemplateRegistry {
    ChatTemplate forModelId(ModelId modelId) throws ValidationException;
}
