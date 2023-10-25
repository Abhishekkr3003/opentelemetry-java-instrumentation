/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0;

import com.aerospike.client.Key;
import com.aerospike.client.cluster.Cluster;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class AerospikeRequest {
  private Cluster cluster;
  private Status status;
  private Integer errorCode;

  public static AerospikeRequest create(Command command, Key key) {
    return new AutoValue_AerospikeRequest(command, key);
  }

  public abstract Command getCommand();

  public abstract Key getKey();

  public String getOperation() {
    return getCommand().name();
  }

  public void setCluster(Cluster cluster) {
    this.cluster = cluster;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public void setErrorCode(Integer errorCode) {
    this.errorCode = errorCode;
  }

  public Cluster getCluster() {
    return this.cluster;
  }

  public Status getStatus() {
    return status;
  }

  public Integer getErrorCode() {
    return errorCode;
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
}
