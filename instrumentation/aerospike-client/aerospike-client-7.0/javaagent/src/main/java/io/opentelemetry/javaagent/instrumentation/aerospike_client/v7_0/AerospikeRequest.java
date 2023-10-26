/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0;

import com.aerospike.client.Key;
import com.aerospike.client.cluster.Node;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class AerospikeRequest {
  private Node node;
  private Integer size;
  private Status status;

  public static AerospikeRequest create(Command command, Key key) {
    return new AutoValue_AerospikeRequest(command, key);
  }

  public abstract Command getCommand();

  public abstract Key getKey();

  public String getOperation() {
    return getCommand().name();
  }

  public void setNode(Node node) {
    this.node = node;
  }

  public Node getNode() {
    return this.node;
  }

  public String getNamespace() {
    Key key = getKey();
    if (key != null) {
      return key.namespace;
    }
    return null;
  }

  public String getSet() {
    Key key = getKey();
    if (key != null) {
      return key.setName;
    }
    return null;
  }

  public String getUserKey() {
    Key key = getKey();
    if (key != null) {
      return key.userKey.toString();
    }
    return null;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }
}
