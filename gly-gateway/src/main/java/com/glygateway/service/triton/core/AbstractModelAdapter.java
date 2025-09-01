package com.glygateway.service.triton.core;

import com.glygateway.service.triton.api.ModelAdapter;
import com.google.protobuf.ByteString;

import inference.GrpcService.ModelInferResponse;

import java.util.Optional;

public abstract class AbstractModelAdapter implements ModelAdapter {

  protected Optional<String> parseModelInferResponse(ModelInferResponse response) {

    // Get the index of 'text_output', and parse the raw_output_contents accordingly
    for (var index = 0; index < response.getOutputsCount(); index++) {
      if (response.getOutputs(index).getName().equals("text_output")) {
        ByteString blob = response.getRawOutputContents(index);
        return Optional.of(BufferCodec.decodeBytesToString(blob));
      }
    }
    return Optional.empty();
  }
  // Add helpers: common header building, error mapping, logging, etc.
}
