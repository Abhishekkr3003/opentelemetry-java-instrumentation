package io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0;

import com.aerospike.client.AerospikeException;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.db.AerospikeSemanticAttributes;
import javax.annotation.Nullable;

public class AerospikeAttributeExtractor implements
    AttributesExtractor<AerospikeRequest, Void> {

  @Override
  public void onStart(AttributesBuilder attributes, Context parentContext,
      AerospikeRequest aerospikeRequest) {
    attributes.put(
        AerospikeSemanticAttributes.AEROSPIKE_NAMESPACE,
        aerospikeRequest.getNamespace());
    attributes.put(AerospikeSemanticAttributes.AEROSPIKE_SET_NAME, aerospikeRequest.getSet());
    attributes.put(AerospikeSemanticAttributes.AEROSPIKE_USER_KEY, aerospikeRequest.getUserKey());
  }

  @Override
  public void onEnd(AttributesBuilder attributes, Context context,
      AerospikeRequest aerospikeRequest, @Nullable Void unused, @Nullable Throwable error) {
    attributes.put(AerospikeSemanticAttributes.AEROSPIKE_STATUS,
        aerospikeRequest.getStatus().name());
    if (error != null) {
      AerospikeException aerospikeException = (AerospikeException) error;
      attributes.put(AerospikeSemanticAttributes.AEROSPIKE_ERROR_CODE,
          aerospikeException.getResultCode());
    } else {
      attributes.put(AerospikeSemanticAttributes.AEROSPIKE_ERROR_CODE, 0);
      if (aerospikeRequest.getSize() != null) {
        attributes.put(AerospikeSemanticAttributes.AEROSPIKE_TRANSFER_SIZE,
            aerospikeRequest.getSize());
      }
    }
  }
}
