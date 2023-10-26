/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0;

import static io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge.currentContext;
import static io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0.AersopikeSingletons.instrumenter;
import static io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0.Status.FAILURE;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.cluster.Node;
import com.aerospike.client.command.Command;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.util.VirtualField;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class AerospikeClientAsyncReadCommandInstrumentation implements TypeInstrumentation {
  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("com.aerospike.client.async.AsyncRead");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        isMethod()
            .and(named("getNode"))
            .and(returns(named("com.aerospike.client.cluster.Node")))
            .and(takesArgument(0, named("com.aerospike.client.cluster.Cluster"))),
        this.getClass().getName() + "$AsyncWriteCommandAdvice");
  }

  @SuppressWarnings("unused")
  public static class AsyncWriteCommandAdvice {

    @SuppressWarnings("BareDotMetacharacter")
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static AerospikeRequestContext onEnter(
        @Advice.Local("otelAerospikeRequest") AerospikeRequest request,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope,
        @Advice.FieldValue("key") Key key) {
      Context parentContext = currentContext();
      request = AerospikeRequest.create(Operation.Type.READ.name(), key);
      if (!instrumenter().shouldStart(parentContext, request)) {
        return null;
      }
      context = instrumenter().start(parentContext, request);
      scope = context.makeCurrent();
      return AerospikeRequestContext.attach(request, context);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void stopSpan(
        @Advice.Thrown Throwable throwable,
        @Advice.Return Node node,
        @Advice.Enter AerospikeRequestContext requestContext,
        @Advice.This Command command,
        @Advice.Local("otelAerospikeRequest") AerospikeRequest request,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {
      VirtualField.find(Command.class, AerospikeRequestContext.class).set(command, requestContext);
      request.setNode(node);
      if (throwable != null) {
        request.setStatus(FAILURE);
        if (requestContext != null) {
          requestContext.endSpan(instrumenter(), context, request, throwable);
          requestContext.detachAndEnd();
        }
      }
      if (scope == null) {
        return;
      }
      scope.close();
    }
  }
}
