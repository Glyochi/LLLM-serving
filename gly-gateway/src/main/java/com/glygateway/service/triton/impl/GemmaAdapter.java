package com.glygateway.service.triton.impl;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.glygateway.chat_template.api.ChatTemplate;
import com.glygateway.chat_template.api.ChatTemplateRegistry;
import com.glygateway.exception.triton.ValidationException;
import com.glygateway.model.triton.Conversation;
import com.glygateway.model.triton.InferenceParams;
import com.glygateway.model.triton.ModelId;
import com.glygateway.service.triton.config.GemmaConfig;
import com.glygateway.service.triton.core.AbstractModelAdapter;
import com.glygateway.service.triton.core.BufferCodec;
import com.glygateway.service.triton.core.TritonClientFacade;
import com.google.protobuf.ByteString;

import inference.GrpcService.ModelInferRequest;
import io.micrometer.observation.ObservationRegistry;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class GemmaAdapter extends AbstractModelAdapter {
  private final ChatTemplate chatTemplate;
  private final TritonClientFacade triton;
  private final GemmaConfig config;
  private final ObservationRegistry observationRegistry;

  public GemmaAdapter(ChatTemplateRegistry chatTemplateRegistry,
      TritonClientFacade triton, GemmaConfig config, ObservationRegistry observationRegistry) throws ValidationException {
    super();
    this.chatTemplate = chatTemplateRegistry.forModelId(modelId());
    this.triton = triton;
    this.config = config;
	this.observationRegistry = observationRegistry;
  }

  @Override
  public ModelId modelId() {
    return ModelId.Gemma;
  }

  @Override
  public String debugApplyChatTemplate(Conversation agentChatRequest) throws ValidationException {
    return chatTemplate.applyTemplate(new Conversation(agentChatRequest.getContents()));
  }

  @Override
  public Mono<ModelInferRequest> buildRequest2(Conversation agentChatRequest, InferenceParams inferenceParams) throws ValidationException {
    return Mono.fromCallable(() -> buildRequest(agentChatRequest, inferenceParams)).name("apply-template").tap(Micrometer.observation(observationRegistry));
  }

  @Override
  public ModelInferRequest buildRequest(Conversation agentChatRequest, InferenceParams inferenceParams) throws ValidationException {
    Random random = new Random();

    var input_text_input = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("text_input").setDatatype("BYTES").addShape(1).addShape(1);

    // Java doesn't have uint64/unsigned long. So just randomize a long which should
    // be sufficient for random purpose
    long float_seed = inferenceParams.getSeed() != null ? inferenceParams.getSeed() : random.nextLong();
    var input_seed = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("seed").setDatatype("UINT64").addShape(1).addShape(1);

    int int_max_tokens = Math.min(inferenceParams.getMaxTokens(), config.maxTokens());
    var input_max_tokens = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("max_tokens").setDatatype("INT32").addShape(1).addShape(1);

    float float_temperature = inferenceParams.getTemperature() != null ? inferenceParams.getTemperature() : config.temperature();
    var input_temperature = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("temperature").setDatatype("FP32").addShape(1).addShape(1);

    float float_top_p = inferenceParams.getTopP() != null ? inferenceParams.getTopP() : config.topP();
    var input_top_p = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("top_p").setDatatype("FP32").addShape(1).addShape(1);

    int int_top_k = inferenceParams.getTopK() != null ? inferenceParams.getTopK() : config.topK();
    var input_top_k = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("top_k").setDatatype("INT32").addShape(1).addShape(1);

    var bool_contents_stream = inferenceParams.getStream() != null ? inferenceParams.getStream() : false;
    var input_stream = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("stream").setDatatype("BOOL").addShape(1).addShape(1);

    List<String> stopTokens = chatTemplate.stopTokens();
    var input_stop_tokens = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("stop_words").setDatatype("BYTES").addShape(1).addShape(stopTokens.size());
    Optional<ByteString> input_stop_tokens_byte = stopTokens.stream().map(BufferCodec::encodeStringToBytes).reduce((a, b) -> a.concat(b));

    var bool_exclude_input_in_output = inferenceParams.getExcludeInputInOutput() != null ? inferenceParams.getExcludeInputInOutput() : true;
    var input_exclude_input_in_output = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("exclude_input_in_output").setDatatype("BOOL").addShape(1).addShape(1);

    return ModelInferRequest.newBuilder()
        .setModelName(modelId().toString())
        .setModelVersion("1")
        .addInputs(0, input_text_input)
        .addInputs(1, input_seed)
        .addInputs(2, input_max_tokens)
        .addInputs(3, input_temperature)
        .addInputs(4, input_top_p)
        .addInputs(5, input_top_k)
        .addInputs(6, input_stream)
        .addInputs(7, input_stop_tokens)
        .addInputs(8, input_exclude_input_in_output)
        .addRawInputContents(
            BufferCodec.encodeStringToBytes(chatTemplate.applyTemplate(new Conversation(agentChatRequest.getContents()))))
        .addRawInputContents(BufferCodec.encodeLong64ToBytes(float_seed))
        .addRawInputContents(BufferCodec.encodeInt32ToBytes(int_max_tokens))
        .addRawInputContents(BufferCodec.encodeFloat32ToBytes(float_temperature))
        .addRawInputContents(BufferCodec.encodeFloat32ToBytes(float_top_p))
        .addRawInputContents(BufferCodec.encodeInt32ToBytes(int_top_k))
        .addRawInputContents(BufferCodec.encodeBoolToBytes(bool_contents_stream))
        .addRawInputContents(
            input_stop_tokens_byte.isPresent() ? input_stop_tokens_byte.get() : BufferCodec.encodeStringToBytes("")
        )
        .addRawInputContents(BufferCodec.encodeBoolToBytes(bool_exclude_input_in_output))
        .build();
  }

  @Override
  public Flux<String> stream(ModelInferRequest inferRequest) {
    return triton.stream(inferRequest).map(ir -> parseModelInferResponse(ir)).filter(os -> os.isPresent())
        .map(os -> os.get());
  }

}
