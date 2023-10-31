/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package net.media.javaagent.instrumentation;

import io.grpc.ClientInterceptor;

// Holds singleton references.
public final class GrpcSingletons {

  public static final ClientInterceptor CLIENT_INTERCEPTOR;

  static {
    CLIENT_INTERCEPTOR = new TracingClientInterceptor();
  }

  private GrpcSingletons() {}
}
