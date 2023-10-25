/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0;

import com.aerospike.client.cluster.Cluster;
import com.aerospike.client.cluster.Node;
import io.opentelemetry.instrumentation.api.instrumenter.network.NetworkAttributesGetter;
import io.opentelemetry.instrumentation.api.instrumenter.network.ServerAttributesGetter;
import java.net.InetSocketAddress;
import javax.annotation.Nullable;

final class AerospikeClientNetworkAttributesGetter
    implements ServerAttributesGetter<AerospikeRequest, Void>,
        NetworkAttributesGetter<AerospikeRequest, Void> {

  @Override
  @Nullable
  public InetSocketAddress getNetworkPeerInetSocketAddress(
      AerospikeRequest aerospikeRequest, @Nullable Void unused) {
    Cluster cluster = aerospikeRequest.getCluster();

    if (cluster != null) {
      Node node = cluster.getNodes()[0];
      if(node != null) {
        return node.getAddress();
      }
    }
    return null;
  }
}
