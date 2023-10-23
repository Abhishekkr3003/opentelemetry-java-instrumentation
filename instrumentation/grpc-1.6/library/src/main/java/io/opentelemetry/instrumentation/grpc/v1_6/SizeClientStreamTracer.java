package io.opentelemetry.instrumentation.grpc.v1_6;

import io.grpc.CallOptions;
import io.grpc.ClientStreamTracer;
import io.grpc.Metadata;

class SizeClientStreamTracer extends ClientStreamTracer {
  private final GrpcRequest rpcRequest;

  SizeClientStreamTracer(GrpcRequest grpcRequest) {
    this.rpcRequest = grpcRequest;
  }

  @Override
  public void outboundUncompressedSize(long bytes) {
    rpcRequest.setRequestSize(bytes);
  }

  @Override
  public void inboundUncompressedSize(long bytes) {
    rpcRequest.setResponseSize(bytes);
  }

  public static Factory newFactory(GrpcRequest rpcRequest) {
    return new Factory() {
      @Override
      public ClientStreamTracer newClientStreamTracer(CallOptions callOptions, Metadata headers) {
        return new SizeClientStreamTracer(rpcRequest);
      }
    };
  }
}

