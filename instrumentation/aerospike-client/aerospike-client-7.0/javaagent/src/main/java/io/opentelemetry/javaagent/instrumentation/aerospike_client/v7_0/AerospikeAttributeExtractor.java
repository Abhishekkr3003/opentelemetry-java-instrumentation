package io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0;

import static io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0.Status.FAILURE;
import static io.opentelemetry.javaagent.instrumentation.aerospike_client.v7_0.Status.SUCCESS;

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
    if (error != null) {
      attributes.put(AerospikeSemanticAttributes.AEROSPIKE_STATUS, FAILURE.name());
      AerospikeException aerospikeException = (AerospikeException) error;
      attributes.put(AerospikeSemanticAttributes.AEROSPIKE_ERROR_CODE,
          aerospikeException.getResultCode());
    } else {
      attributes.put(AerospikeSemanticAttributes.AEROSPIKE_STATUS, SUCCESS.name());
      attributes.put(AerospikeSemanticAttributes.AEROSPIKE_ERROR_CODE, 0);
    }
  }
}
