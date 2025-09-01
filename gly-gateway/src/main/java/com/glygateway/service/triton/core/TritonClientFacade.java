package com.glygateway.service.triton.core;


import org.springframework.stereotype.Component;
import com.glygateway.exception.triton.InferenceFailedException;

import inference.GRPCInferenceServiceGrpc.GRPCInferenceServiceFutureStub;
import inference.GRPCInferenceServiceGrpc.GRPCInferenceServiceStub;
import inference.GrpcService.ModelInferRequest;
import inference.GrpcService.ModelInferResponse;
import inference.GrpcService.ModelStreamInferResponse;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Flux;

@Component
public class TritonClientFacade {

  private final GRPCInferenceServiceStub asyncStub;
  private final GRPCInferenceServiceFutureStub futureStub;

  public TritonClientFacade(GRPCInferenceServiceStub asyncStub, GRPCInferenceServiceFutureStub futureStub) {
    this.asyncStub = asyncStub;
    this.futureStub = futureStub;
  }

  public Flux<ModelInferResponse> stream(ModelInferRequest inferRequest) {

    Flux<ModelInferResponse> completion_stream = Flux.create(sink -> {

      StreamObserver<ModelStreamInferResponse> respObs = new StreamObserver<>() {
        @Override
        public void onNext(ModelStreamInferResponse streamInferResponse) {

          if (!streamInferResponse.getErrorMessage().isEmpty()) {
            sink.error(new RuntimeException("Triton error: " + streamInferResponse.getErrorMessage()));
            return;
          }

          sink.next(streamInferResponse.getInferResponse());
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
      reqObs.onNext(inferRequest);
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
