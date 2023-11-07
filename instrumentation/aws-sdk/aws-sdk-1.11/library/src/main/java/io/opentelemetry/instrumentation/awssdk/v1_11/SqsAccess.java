/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.awssdk.v1_11;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.Request;
import com.amazonaws.Response;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.internal.Timer;
import io.opentelemetry.javaagent.tooling.muzzle.NoMuzzle;
import java.util.Collections;
import java.util.Map;

final class SqsAccess {
  private SqsAccess() {}

  private static final boolean enabled = PluginImplUtil.isImplPresent("SqsImpl");

  @NoMuzzle
  static boolean afterResponse(
      Request<?> request,
      Response<?> response,
      Timer timer,
      Context parentContext,
      TracingRequestHandler requestHandler) {
    return enabled
        && SqsImpl.afterResponse(request, response, timer, parentContext, requestHandler);
  }

  @NoMuzzle
  static boolean beforeMarshalling(AmazonWebServiceRequest request) {
    return enabled && SqsImpl.beforeMarshalling(request);
  }

  @NoMuzzle
  static Map<String, String> getMessageAttributes(Request<?> request) {
    return enabled ? SqsImpl.getMessageAttributes(request) : Collections.emptyMap();
  }
}
