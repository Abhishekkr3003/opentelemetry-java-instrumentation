/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.SpanKindExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.db.AerospikeMetrics;
import io.opentelemetry.instrumentation.api.instrumenter.db.DbClientAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.db.DbClientSpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.net.PeerServiceAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.network.NetworkAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.network.ServerAttributesExtractor;
import io.opentelemetry.javaagent.bootstrap.internal.CommonConfig;

public final class AersopikeSingletons {
  private static final String INSTRUMENTATION_NAME = "io.opentelemetry.aerospike-client-7.0";

  private static final Instrumenter<AerospikeRequest, Void> INSTRUMENTER;

  static {
    AerospikeDbAttributesGetter dbAttributesGetter = new AerospikeDbAttributesGetter();
    AerospikeClientNetworkAttributesGetter netAttributesGetter = new AerospikeClientNetworkAttributesGetter();

    INSTRUMENTER =
        Instrumenter.<AerospikeRequest, Void>builder(
                GlobalOpenTelemetry.get(),
                INSTRUMENTATION_NAME,
                DbClientSpanNameExtractor.create(dbAttributesGetter))
            .addAttributesExtractor(new AerospikeAttributeExtractor())
            .addAttributesExtractor(DbClientAttributesExtractor.create(dbAttributesGetter))
            .addAttributesExtractor(ServerAttributesExtractor.create(netAttributesGetter))
            .addAttributesExtractor(NetworkAttributesExtractor.create(netAttributesGetter))
            .addAttributesExtractor(
                PeerServiceAttributesExtractor.create(
                    netAttributesGetter, CommonConfig.get().getPeerServiceResolver()))
            .addOperationMetrics(AerospikeMetrics.get())
            .buildInstrumenter(SpanKindExtractor.alwaysClient());
  }

  public static Instrumenter<AerospikeRequest, Void> instrumenter() {
    return INSTRUMENTER;
  }

  private AersopikeSingletons() {}
}
