package com.gly_gateway.service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.gly_gateway.chat_template.GemmaChatTemplate;
import com.gly_gateway.exception.InferenceFailedException;
import com.gly_gateway.exception.ValidationException;
import com.gly_gateway.model.AgentChatRequest;
import com.google.protobuf.ByteString;

import ch.qos.logback.core.net.SyslogOutputStream;
import inference.GRPCInferenceServiceGrpc.GRPCInferenceServiceFutureStub;
import inference.GRPCInferenceServiceGrpc.GRPCInferenceServiceStub;
import inference.GrpcService.ModelInferRequest;
import inference.GrpcService.ModelInferRequest.InferInputTensor;
import inference.GrpcService.ModelInferResponse;
import inference.GrpcService.ModelStreamInferResponse;
import io.grpc.stub.StreamObserver;
import inference.GrpcService.InferParameter;
import inference.GrpcService.InferTensorContents;
import reactor.core.publisher.Flux;

@Service
public class AgentService {

  private final GRPCInferenceServiceStub asyncStub;
  private final GRPCInferenceServiceFutureStub futureStub;

  private final GemmaChatTemplate gemmaChatTemplate;

  public AgentService(GRPCInferenceServiceStub asyncStub, GRPCInferenceServiceFutureStub futureStub,
      GemmaChatTemplate gemmaChatTemplate) {
    this.asyncStub = asyncStub;
    this.futureStub = futureStub;
    this.gemmaChatTemplate = gemmaChatTemplate;
  }

  static ByteString encodeStringToBytes(String text) {
    // BYTES encoding: [uint32 / 4 bytes for data length][bytes] repeated;
    // little-endian
    var text_bytes = text.getBytes(StandardCharsets.UTF_8);
    int total = 4 + text_bytes.length;

    ByteBuffer buf = ByteBuffer.allocate(total).order(ByteOrder.LITTLE_ENDIAN);

    byte[] b = text_bytes;
    buf.putInt(b.length);
    buf.put(b);

    // Reset the pointer from writing mode to reading mode
    buf.flip();
    return ByteString.copyFrom(buf);
  }

  static ByteString encodeInt32ToBytes(int value) {
    // BYTES encoding: [int32 / 4 bytes]
    ByteBuffer buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
    buf.putInt(value);
    buf.flip();
    return ByteString.copyFrom(buf);
  }

  static ByteString encodeLong64ToBytes(long value) {
    // BYTES encoding: [int64 / 8 bytes]
    ByteBuffer buf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
    buf.putLong(value);
    buf.flip();
    return ByteString.copyFrom(buf);
  }

  static ByteString encodeFloat32ToBytes(float value) {
    // BYTES encoding: [float32 / 4 bytes]
    ByteBuffer buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
    buf.putFloat(value);
    buf.flip();
    return ByteString.copyFrom(buf);
  }

  static ByteString encodeBoolToBytes(boolean value) {
    // BYTES encoding: [boolean / 1 byte]
    // 1 for true and 0 for false
    ByteBuffer buf = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN);
    buf.put(value ? (byte) 1 : (byte) 0);
    buf.flip();
    return ByteString.copyFrom(buf);
  }

  private ModelInferRequest createModelInferRequest(AgentChatRequest agentChatRequest) throws ValidationException {
    Random random = new Random();

    var input_text_input = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("text_input").setDatatype("BYTES").addShape(1).addShape(1);

    // Java doesn't have uint64/unsigned long. So just randomize a long which should be sufficient for random purpose 
    long float_seed = random.nextLong();
    var input_seed = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("seed").setDatatype("UINT64").addShape(1).addShape(1);

    int int_max_tokens = 1000;
    var input_max_tokens = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("max_tokens").setDatatype("INT32").addShape(1).addShape(1);

    float float_temperature = 1.0f;
    var input_temperature = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("temperature").setDatatype("FP32").addShape(1).addShape(1);

    float float_top_p = 0.95f;
    var input_top_p = ModelInferRequest.InferInputTensor.newBuilder()
        .setName("top_p").setDatatype("FP32").addShape(1).addShape(1);

    var bool_contents_stream = false;
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
        .addRawInputContents(encodeStringToBytes(gemmaChatTemplate.applyTemplate(agentChatRequest.getContents())))
        .addRawInputContents(encodeLong64ToBytes(float_seed))
        .addRawInputContents(encodeInt32ToBytes(int_max_tokens))
        .addRawInputContents(encodeFloat32ToBytes(float_temperature))
        .addRawInputContents(encodeFloat32ToBytes(float_top_p))
        .addRawInputContents(encodeBoolToBytes(bool_contents_stream))
        .build();
  }

  private Optional<String> parseModelInferResponse(ModelInferResponse response) {

    // Get the index of 'text_output', and parse the raw_output_contents accordingly
    for (var index = 0; index < response.getOutputsCount(); index++) {
      if (response.getOutputs(index).getName().equals("text_output")) {
        ByteString blob = response.getRawOutputContents(index);
        ByteBuffer buf = blob.asReadOnlyByteBuffer().order(ByteOrder.LITTLE_ENDIAN);
        // Read and advance by 4 bytes (1 int)
        int len = buf.getInt();
        byte[] b = new byte[len];
        buf.get(b);
        var text_output = new String(b, StandardCharsets.UTF_8);
        return Optional.of(text_output);
      }
    }
    return Optional.empty();
  }

  public Flux<String> agentComplete(AgentChatRequest agentChatRequest) throws ValidationException {

    ModelInferRequest req = createModelInferRequest(agentChatRequest);

    Flux<String> completion_stream = Flux.create(sink -> {

      StreamObserver<ModelStreamInferResponse> respObs = new StreamObserver<>() {
        @Override
        public void onNext(ModelStreamInferResponse r) {

          if (!r.getErrorMessage().isEmpty()) {
            sink.error(new RuntimeException("Triton error: " + r.getErrorMessage()));
            return;
          }

          ModelInferResponse ir = r.getInferResponse();

          Optional<String> opt_text_output = parseModelInferResponse(ir);
          if (opt_text_output.isPresent()) {
            sink.next(opt_text_output.get());
          }
        }

        @Override
        public void onError(Throwable t) {
          sink.error(new InferenceFailedException("Inferencing Failed.", t));
        }

        @Override
        public void onCompleted() {
          sink.complete();
        }
      };

      // 2) Request observer: send the request, then complete
      StreamObserver<ModelInferRequest> reqObs = asyncStub.modelStreamInfer(respObs);

      // Send the request and finish the client side of the stream
      reqObs.onNext(req);
      reqObs.onCompleted();

      // Cancel propagation: if HTTP client disconnects, close GRPC stream
      sink.onCancel(() -> {
        try {
          reqObs.onCompleted();
        } catch (Exception ignored) {
        }
      });

    });

    return completion_stream;
  }

}
