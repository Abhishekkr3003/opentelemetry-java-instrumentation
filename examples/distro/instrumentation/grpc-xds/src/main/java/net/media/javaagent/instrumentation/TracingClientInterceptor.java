/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package net.media.javaagent.instrumentation;

import io.grpc.Attributes;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.internal.GrpcAttributes;
import io.grpc.xds.InternalXdsAttributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;

final class TracingClientInterceptor implements ClientInterceptor {

  TracingClientInterceptor() {}

  @Override
  public <REQUEST, RESPONSE> ClientCall<REQUEST, RESPONSE> interceptCall(
      MethodDescriptor<REQUEST, RESPONSE> method, CallOptions callOptions, Channel next) {
    System.out.println("call intercepted");
    return new TracingClientCall<>(next.newCall(method, callOptions));
  }

  final class TracingClientCall<REQUEST, RESPONSE>
      extends ForwardingClientCall.SimpleForwardingClientCall<REQUEST, RESPONSE> {

    TracingClientCall(ClientCall<REQUEST, RESPONSE> delegate) {
      super(delegate);
    }

    @Override
    public void start(Listener<RESPONSE> responseListener, Metadata headers) {
      System.out.println("Span: " + Java8BytecodeBridge.currentSpan());
      System.out.println("Attributes: " + this.getAttributes());
      Attributes attributes = this.getAttributes();
      if (attributes != null && attributes.get(GrpcAttributes.ATTR_CLIENT_EAG_ATTRS) != null) {
        Attributes addressAttributes = attributes.get(GrpcAttributes.ATTR_CLIENT_EAG_ATTRS);
        if (addressAttributes != null
            && addressAttributes.get(InternalXdsAttributes.ATTR_CLUSTER_NAME) != null) {
          String connectionLabel = addressAttributes.get(InternalXdsAttributes.ATTR_CLUSTER_NAME);
          if (connectionLabel != null) {
            Span currentSpan = Java8BytecodeBridge.currentSpan();
            currentSpan.setAttribute("connection", connectionLabel);
          }
        }
      }
    }
  }
}
