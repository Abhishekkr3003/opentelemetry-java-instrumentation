/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package net.media.javaagent.instrumentation;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import java.util.Collections;
import java.util.List;

@AutoService(InstrumentationModule.class)
public class GrpcXdsInstrumentationModule extends InstrumentationModule {
  public GrpcXdsInstrumentationModule() {
    super("grpc-xds", "grpc-xds-1.59");
  }

  @Override
  public int order() {
    return 1;
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    System.out.println("typeInstrumentations called");
    return Collections.singletonList(new GrpcClientBuilderBuildInstrumentation());
  }
}
