/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0;

import static io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge.currentContext;
import static io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0.AersopikeSingletons.instrumenter;
import static io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0.Command.GET;
import static io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0.Command.PUT;
import static io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0.Status.FAILURE;
import static io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0.Status.RECORD_NOT_FOUND;
import static io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0.Status.SUCCESS;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import com.aerospike.client.Key;
import com.aerospike.client.Record;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class AerospikeClientSyncCommandInstrumentation implements TypeInstrumentation {
  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("com.aerospike.client.AerospikeClient");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        isMethod()
            .and(isPublic())
            .and(named("get"))
            .and(takesArguments(3).or(takesArguments(2)))
            .and(takesArgument(1, named("com.aerospike.client.Key"))),
        this.getClass().getName() + "$GetCommandAdvice");

    transformer.applyAdviceToMethod(
        isMethod()
            .and(isPublic())
            .and(named("put"))
            .and(takesArguments(3).or(takesArguments(2)))
            .and(takesArgument(1, named("com.aerospike.client.Key"))),

        this.getClass().getName() + "$PutCommandAdvice");
  }

  @SuppressWarnings("unused")
  public static class GetCommandAdvice {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static AerospikeRequestContext onEnter(
        @Advice.Argument(1) Key key,
        @Advice.Local("otelAerospikeRequest") AerospikeRequest request,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {

      System.out.println("Entering get enter");
      Context parentContext = currentContext();
      request = AerospikeRequest.create(GET, key);
      if (!instrumenter().shouldStart(parentContext, request)) {
        return null;
      }

      context = instrumenter().start(parentContext, request);
      scope = context.makeCurrent();
      System.out.println("Exiting get enter");
      return AerospikeRequestContext.attach(request);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void stopSpan(
        @Advice.Return Record record,
        @Advice.Thrown Throwable throwable,
        @Advice.Enter AerospikeRequestContext requestContext,
        @Advice.Local("otelAerospikeRequest") AerospikeRequest request,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {

      System.out.println("Entering get exit");
      if (throwable != null) {
        request.setStatus(FAILURE);
      } else if (record == null) {
        request.setStatus(RECORD_NOT_FOUND);
      } else {
        request.setStatus(SUCCESS);
      }
      if (scope == null) {
        return;
      }

      scope.close();
      if (requestContext != null) {
        requestContext.endSpan(instrumenter(), context, request, throwable);
      }
      requestContext.detachAndEnd();

      System.out.println("exiting get exit");
    }
  }

  @SuppressWarnings("unused")
  public static class PutCommandAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static AerospikeRequestContext onEnter(
        @Advice.Argument(1) Key key,
        @Advice.Local("otelAerospikeRequest") AerospikeRequest request,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {
      System.out.println("Entering put enter");
      Context parentContext = currentContext();
      request = AerospikeRequest.create(PUT, key);
      if (!instrumenter().shouldStart(parentContext, request)) {
        return null;
      }
      context = instrumenter().start(parentContext, request);
      scope = context.makeCurrent();
      System.out.println("Exiting put enter");
      return AerospikeRequestContext.attach(request);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void stopSpan(
        @Advice.Thrown Throwable throwable,
        @Advice.Enter AerospikeRequestContext requestContext,
        @Advice.Local("otelAerospikeRequest") AerospikeRequest request,
        @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {
      System.out.println("Entering put exit");
      if (throwable != null) {
        request.setStatus(FAILURE);
      } else {
        request.setStatus(SUCCESS);
      }
      if (scope == null) {
        return;
      }

      scope.close();
      if (requestContext != null) {
        requestContext.endSpan(instrumenter(), context, request, throwable);
      }
      requestContext.detachAndEnd();
      System.out.println("Exiting put exit");
    }
  }
}
