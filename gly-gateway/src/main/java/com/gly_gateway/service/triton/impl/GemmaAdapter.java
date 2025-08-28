package com.gly_gateway.service.triton.impl;

import java.util.Random;

import org.springframework.stereotype.Component;

import com.gly_gateway.chat_template.impl.GemmaChatTemplate;
import com.gly_gateway.service.triton.config.GemmaConfig;
import com.gly_gateway.exception.triton.ValidationException;
import com.gly_gateway.model.triton.Conversation;
import com.gly_gateway.model.triton.InferenceParams;
import com.gly_gateway.service.triton.core.AbstractModelAdapter;
import com.gly_gateway.service.triton.core.TritonClientFacade;
import com.gly_gateway.service.triton.core.BufferCodec;

import inference.GrpcService.ModelInferRequest;
import reactor.core.publisher.Flux;

@Component
public class GemmaAdapter extends AbstractModelAdapter {
  private final GemmaChatTemplate chatTemplate;
  private final TritonClientFacade triton;
  private final GemmaConfig config;

  public GemmaAdapter(GemmaChatTemplate chatTemplate,
      TritonClientFacade triton, GemmaConfig config) {
    super();
    this.chatTemplate = chatTemplate;
    this.triton = triton;
    this.config = config;
  }

  @Override
  public String modelId() {
    return "gemma-2b-it";
  }

  @Override
  public ModelInferRequest buildRequest(Conversation agentChatRequest, InferenceParams inferenceParams) throws ValidationException {
    Random random = new Random();

    var input_text_input = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("text_input").setDatatype("BYTES").addShape(1).addShape(1);

    // Java doesn't have uint64/unsigned long. So just randomize a long which should
    // be sufficient for random purpose
    long float_seed = random.nextLong();
    var input_seed = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("seed").setDatatype("UINT64").addShape(1).addShape(1);

    int int_max_tokens = Math.min(inferenceParams.getMaxTokens(), config.getMaxTokens());
    var input_max_tokens = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("max_tokens").setDatatype("INT32").addShape(1).addShape(1);

    float float_temperature = inferenceParams.getTemperature() != null ? inferenceParams.getTemperature() : config.getTemperature();
    var input_temperature = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("temperature").setDatatype("FP32").addShape(1).addShape(1);

    float float_top_p = config.getTopP();
    var input_top_p = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("top_p").setDatatype("FP32").addShape(1).addShape(1);

    var bool_contents_stream = inferenceParams.getStream() != null ? inferenceParams.getStream() : false;
    var input_stream = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("stream").setDatatype("BOOL").addShape(1).addShape(1);

    return ModelInferRequest.newBuilder()
        .setModelName("tensorrt_llm_bls")
        .setModelVersion("1")
        .addInputs(0, input_text_input)
        .addInputs(1, input_seed)
        .addInputs(2, input_max_tokens)
        .addInputs(3, input_temperature)
        .addInputs(4, input_top_p)
        .addInputs(5, input_stream)
        .addRawInputContents(
            BufferCodec.encodeStringToBytes(chatTemplate.applyTemplate(agentChatRequest.getContents())))
        .addRawInputContents(BufferCodec.encodeLong64ToBytes(float_seed))
        .addRawInputContents(BufferCodec.encodeInt32ToBytes(int_max_tokens))
        .addRawInputContents(BufferCodec.encodeFloat32ToBytes(float_temperature))
        .addRawInputContents(BufferCodec.encodeFloat32ToBytes(float_top_p))
        .addRawInputContents(BufferCodec.encodeBoolToBytes(bool_contents_stream))
        .build();
  }

  @Override
  public Flux<String> stream(ModelInferRequest inferRequest) {
    return triton.stream(inferRequest).map(ir -> parseModelInferResponse(ir)).filter(os -> os.isPresent())
        .map(os -> os.get());
  }

}
