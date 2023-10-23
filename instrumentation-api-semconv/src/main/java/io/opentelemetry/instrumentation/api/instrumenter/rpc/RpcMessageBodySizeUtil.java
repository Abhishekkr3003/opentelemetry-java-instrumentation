package io.opentelemetry.instrumentation.api.instrumenter.rpc;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.semconv.SemanticAttributes;
import javax.annotation.Nullable;

final class RpcMessageBodySizeUtil {

  private static final AttributeKey<Long> RPC_REQUEST_BODY_SIZE = SemanticAttributes.MESSAGE_COMPRESSED_SIZE;

  private static final AttributeKey<Long> RPC_RESPONSE_BODY_SIZE = SemanticAttributes.MESSAGE_UNCOMPRESSED_SIZE;

  @Nullable
  static Long getRpcRequestBodySize(Attributes... attributesList) {
    return getAttribute(RPC_REQUEST_BODY_SIZE, attributesList);
  }

  @Nullable
  static Long getRpcResponseBodySize(Attributes... attributesList) {
    return getAttribute(RPC_RESPONSE_BODY_SIZE, attributesList);
  }

  @Nullable
  private static <T> T getAttribute(AttributeKey<T> key, Attributes... attributesList) {
    for (Attributes attributes : attributesList) {
      T value = attributes.get(key);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  private RpcMessageBodySizeUtil() {}
}

