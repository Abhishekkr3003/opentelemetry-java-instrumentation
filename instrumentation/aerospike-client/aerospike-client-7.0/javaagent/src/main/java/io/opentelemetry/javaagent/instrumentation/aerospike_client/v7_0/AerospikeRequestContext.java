/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0;

import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;

public final class AerospikeRequestContext {
  private static final ThreadLocal<AerospikeRequestContext> contextThreadLocal = new ThreadLocal<>();
  private AerospikeRequest request;
  private Context context;

  private AerospikeRequestContext() {}

  public static AerospikeRequestContext attach(AerospikeRequest request, Context context) {

    AerospikeRequestContext requestContext = new AerospikeRequestContext();
    requestContext.request = request;
    requestContext.context = context;
    contextThreadLocal.set(requestContext);
    return requestContext;
  }

  public void detachAndEnd() {
    contextThreadLocal.remove();
  }

  /**
   * Schedule ending of instrumented operation when current {@link AerospikeRequestContext} is closed.
   */
//  public static <T> void endIfNotAttached(
//      Instrumenter<T, Void> instrumenter, Context context, T request, Throwable throwable) {
//    AerospikeRequestContext<T> requestContext = current();
//    if (requestContext == null || requestContext.request != null) {
//      // end the span immediately if we are already tracking a request
//      endSpan(instrumenter, context, request, throwable);
//    } else {
//      requestContext.instrumenter = instrumenter;
//      requestContext.context = context;
//      requestContext.request = request;
//      requestContext.throwable = throwable;
//    }
//  }
  @SuppressWarnings("unchecked")
  public static AerospikeRequestContext current() {
    return contextThreadLocal.get();
  }

  public void endSpan(
      Instrumenter<AerospikeRequest, Void> instrumenter, Context context, AerospikeRequest request,
      Throwable throwable) {
    instrumenter.end(context, request, null, throwable);
  }

  public AerospikeRequest getRequest() {
    return request;
  }

  public Context getContext() {
    return context;
  }
}
