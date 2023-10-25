/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0;

import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;

public final class AerospikeRequestContext<T> {
  private static final ThreadLocal<AerospikeRequestContext<?>> contextThreadLocal = new ThreadLocal<>();

  private Instrumenter<T, Void> instrumenter;
  private T request;
  private Context context;
  private Throwable throwable;

  private AerospikeRequestContext() {}

  public static <T> AerospikeRequestContext<T> attach() {
    AerospikeRequestContext<T> requestContext = current();
    // if there already is an active request context don't start a new one
    if (requestContext != null) {
      return null;
    }
    requestContext = new AerospikeRequestContext<>();
    contextThreadLocal.set(requestContext);
    return requestContext;
  }

  public void detachAndEnd() {
    contextThreadLocal.remove();
    if (request != null) {
      endSpan(instrumenter, context, request, throwable);
    }
  }

  /**
   * Schedule ending of instrumented operation when current {@link AerospikeRequestContext} is closed.
   */
  public static <T> void endIfNotAttached(
      Instrumenter<T, Void> instrumenter, Context context, T request, Throwable throwable) {
    AerospikeRequestContext<T> requestContext = current();
    if (requestContext == null || requestContext.request != null) {
      // end the span immediately if we are already tracking a request
      endSpan(instrumenter, context, request, throwable);
    } else {
      requestContext.instrumenter = instrumenter;
      requestContext.context = context;
      requestContext.request = request;
      requestContext.throwable = throwable;
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> AerospikeRequestContext<T> current() {
    return (AerospikeRequestContext<T>) contextThreadLocal.get();
  }

  private static <T> void endSpan(
      Instrumenter<T, Void> instrumenter, Context context, T request, Throwable throwable) {
    instrumenter.end(context, request, null, throwable);
  }
}
