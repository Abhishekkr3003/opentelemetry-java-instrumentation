package io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperClass;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isProtected;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;

import com.aerospike.client.cluster.Node;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class AerospikeNodeInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return hasSuperClass(named("com.aerospike.client.command.SyncCommand"));
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        isMethod()
            .and(isProtected())
            .and(named("getNode"))
            .and(returns(named("com.aerospike.client.cluster.Node")))
            .and(takesNoArguments()),
        this.getClass().getName() + "$NodeSyncCommandAdvice");
  }

  @SuppressWarnings("unused")
  public static class NodeSyncCommandAdvice {

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void stopSpan(
        @Advice.Return Node node) {
      AerospikeRequestContext context = AerospikeRequestContext.current();
      if (context != null) {
        AerospikeRequest request = context.getRequest();
        request.setNode(node);
      }
    }
  }
}
