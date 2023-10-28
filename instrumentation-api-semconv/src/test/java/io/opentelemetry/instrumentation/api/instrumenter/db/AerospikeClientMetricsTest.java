/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter.db;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.OperationListener;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.semconv.SemanticAttributes;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class AerospikeClientMetricsTest {

  static final double[] DEFAULT_BUCKETS = ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES.stream()
      .mapToDouble(d -> d).toArray();

  @Test
  @SuppressWarnings("deprecation")
    // until old http semconv are dropped in 2.0
  void collectsMetrics() {
    InMemoryMetricReader metricReader = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider = SdkMeterProvider.builder().registerMetricReader(metricReader)
        .build();

    OperationListener listener = AerospikeMetrics.get().create(meterProvider.get("test"));

    Attributes requestAttributes = Attributes.builder()
        .put(SemanticAttributes.DB_SYSTEM, "aerospike")
        .put(SemanticAttributes.DB_OPERATION, "GET")
        .put(AerospikeSemanticAttributes.AEROSPIKE_SET_NAME, "test-set")
        .put(AerospikeSemanticAttributes.AEROSPIKE_NAMESPACE, "test")
        .put(AerospikeSemanticAttributes.AEROSPIKE_USER_KEY, "data1")
        .build();

    Attributes responseAttributes = Attributes.builder()
        .put(SemanticAttributes.NET_SOCK_PEER_ADDR, "127.0.0.1")
        .put(SemanticAttributes.NET_SOCK_PEER_NAME, "localhost")
        .put(SemanticAttributes.NET_SOCK_PEER_PORT, 3000)
        .put(AerospikeSemanticAttributes.AEROSPIKE_TRANSFER_SIZE, 40)
        .put(AerospikeSemanticAttributes.AEROSPIKE_STATUS, "SUCCESS")
        .put(AerospikeSemanticAttributes.AEROSPIKE_ERROR_CODE, 0)
        .build();

    Context parent = Context.root().with(Span.wrap(
        SpanContext.create("ff01020304050600ff0a0b0c0d0e0f00", "090a0b0c0d0e0f00",
            TraceFlags.getSampled(), TraceState.getDefault())));

    Context context1 = listener.onStart(parent, requestAttributes, nanos(100));

    Context context2 = listener.onStart(Context.root(), requestAttributes, nanos(150));

    listener.onEnd(context1, responseAttributes, nanos(250));

    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric -> assertThat(metric)
                .hasName("aerospike.requests")
                .hasLongSumSatisfying(
                    counter -> counter
                        .hasPointsSatisfying(point -> point.hasValue(2)
                            .hasAttributesSatisfying(
                                equalTo(SemanticAttributes.DB_SYSTEM, "aerospike"),
                                equalTo(SemanticAttributes.DB_OPERATION, "GET"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_SET_NAME, "test-set"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_NAMESPACE, "test"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_USER_KEY, "data1"))
                            .hasExemplarsSatisfying(
                                exemplar -> exemplar.hasTraceId("ff01020304050600ff0a0b0c0d0e0f00")
                                    .hasSpanId("090a0b0c0d0e0f00")))),
            metric -> assertThat(metric)
                .hasName("aerospike.response")
                .hasLongSumSatisfying(
                    counter -> counter
                        .hasPointsSatisfying(point -> point.hasValue(1)
                            .hasAttributesSatisfying(
                                equalTo(SemanticAttributes.DB_SYSTEM, "aerospike"),
                                equalTo(SemanticAttributes.DB_OPERATION, "GET"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_SET_NAME, "test-set"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_NAMESPACE, "test"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_USER_KEY, "data1"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_ADDR, "127.0.0.1"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_NAME, "localhost"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_PORT, 3000),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_STATUS, "SUCCESS"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_ERROR_CODE, 0))
                            .hasExemplarsSatisfying(
                                exemplar -> exemplar.hasTraceId("ff01020304050600ff0a0b0c0d0e0f00")
                                    .hasSpanId("090a0b0c0d0e0f00")))),
            metric -> assertThat(metric)
                .hasName("aerospike.concurrreny")
                .hasLongSumSatisfying(
                    sum ->
                        sum.hasPointsSatisfying(
                            point ->
                                point
                                    .hasValue(1)
                            .hasAttributesSatisfying(
                                equalTo(SemanticAttributes.DB_SYSTEM, "aerospike"),
                                equalTo(SemanticAttributes.DB_OPERATION, "GET"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_SET_NAME, "test-set"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_NAMESPACE, "test"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_USER_KEY, "data1")))),
            metric -> assertThat(metric)
                .hasName("aerospike.client.duration")
                .hasUnit("ms")
                .hasHistogramSatisfying(
                    histogram -> histogram
                        .hasPointsSatisfying(point -> point.hasSum(150)
                            .hasAttributesSatisfying(
                                equalTo(SemanticAttributes.DB_SYSTEM, "aerospike"),
                                equalTo(SemanticAttributes.DB_OPERATION, "GET"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_SET_NAME, "test-set"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_NAMESPACE, "test"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_USER_KEY, "data1"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_ADDR, "127.0.0.1"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_NAME, "localhost"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_PORT, 3000),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_STATUS, "SUCCESS"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_ERROR_CODE, 0))
                            .hasExemplarsSatisfying(
                                exemplar -> exemplar.hasTraceId("ff01020304050600ff0a0b0c0d0e0f00")
                                    .hasSpanId("090a0b0c0d0e0f00")))),
            metric -> assertThat(metric)
                .hasName("aerospike.record.size")
                .hasUnit("By")
                .hasHistogramSatisfying(
                    histogram -> histogram
                        .hasPointsSatisfying(point -> point.hasSum(40)
                            .hasAttributesSatisfying(
                                equalTo(SemanticAttributes.DB_SYSTEM, "aerospike"),
                                equalTo(SemanticAttributes.DB_OPERATION, "GET"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_SET_NAME, "test-set"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_NAMESPACE, "test"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_USER_KEY, "data1"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_ADDR, "127.0.0.1"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_NAME, "localhost"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_PORT, 3000),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_STATUS, "SUCCESS"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_ERROR_CODE, 0))
                            .hasExemplarsSatisfying(
                                exemplar -> exemplar.hasTraceId("ff01020304050600ff0a0b0c0d0e0f00")
                                    .hasSpanId("090a0b0c0d0e0f00"))))
        );

    listener.onEnd(context2, responseAttributes, nanos(300));

    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric -> assertThat(metric)
                .hasName("aerospike.requests")
                .hasLongSumSatisfying(
                    counter -> counter
                        .hasPointsSatisfying(point -> point.hasValue(2)
                            .hasAttributesSatisfying(
                                equalTo(SemanticAttributes.DB_SYSTEM, "aerospike"),
                                equalTo(SemanticAttributes.DB_OPERATION, "GET"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_SET_NAME, "test-set"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_NAMESPACE, "test"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_USER_KEY, "data1")))),
            metric -> assertThat(metric)
                .hasName("aerospike.response")
                .hasLongSumSatisfying(
                    counter -> counter
                        .hasPointsSatisfying(point -> point.hasValue(2)
                            .hasAttributesSatisfying(
                                equalTo(SemanticAttributes.DB_SYSTEM, "aerospike"),
                                equalTo(SemanticAttributes.DB_OPERATION, "GET"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_SET_NAME, "test-set"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_NAMESPACE, "test"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_USER_KEY, "data1"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_ADDR, "127.0.0.1"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_NAME, "localhost"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_PORT, 3000),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_STATUS, "SUCCESS"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_ERROR_CODE, 0)))),
            metric -> assertThat(metric)
                .hasName("aerospike.concurrreny")
                .hasLongSumSatisfying(
                    counter -> counter
                        .hasPointsSatisfying(point -> point.hasValue(0)
                            .hasAttributesSatisfying(
                                equalTo(SemanticAttributes.DB_SYSTEM, "aerospike"),
                                equalTo(SemanticAttributes.DB_OPERATION, "GET"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_SET_NAME, "test-set"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_NAMESPACE, "test"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_USER_KEY, "data1")))),
            metric -> assertThat(metric)
                .hasName("aerospike.client.duration")
                .hasUnit("ms")
                .hasHistogramSatisfying(
                    histogram -> histogram
                        .hasPointsSatisfying(point -> point.hasSum(300)
                            .hasAttributesSatisfying(
                                equalTo(SemanticAttributes.DB_SYSTEM, "aerospike"),
                                equalTo(SemanticAttributes.DB_OPERATION, "GET"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_SET_NAME, "test-set"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_NAMESPACE, "test"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_USER_KEY, "data1"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_ADDR, "127.0.0.1"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_NAME, "localhost"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_PORT, 3000),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_STATUS, "SUCCESS"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_ERROR_CODE, 0)))),
            metric -> assertThat(metric)
                .hasName("aerospike.record.size")
                .hasUnit("By")
                .hasHistogramSatisfying(
                    histogram -> histogram
                        .hasPointsSatisfying(point -> point.hasSum(80)
                            .hasAttributesSatisfying(
                                equalTo(SemanticAttributes.DB_SYSTEM, "aerospike"),
                                equalTo(SemanticAttributes.DB_OPERATION, "GET"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_SET_NAME, "test-set"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_NAMESPACE, "test"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_USER_KEY, "data1"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_ADDR, "127.0.0.1"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_NAME, "localhost"),
                                equalTo(SemanticAttributes.NET_SOCK_PEER_PORT, 3000),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_STATUS, "SUCCESS"),
                                equalTo(AerospikeSemanticAttributes.AEROSPIKE_ERROR_CODE, 0))))
        );

  }

  private static long nanos(int millis) {
    return TimeUnit.MILLISECONDS.toNanos(millis);
  }
}
