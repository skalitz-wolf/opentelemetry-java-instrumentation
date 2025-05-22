/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.javaagent;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

import java.util.Optional;

import static com.example.javaagent.DemoPropagator.PARENT_SPAN_ID;
import static com.example.javaagent.DemoPropagator.PARENT_SPAN_ID_KEY;

/**
 * See <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/sdk.md#span-processor">
 * OpenTelemetry Specification</a> for more information about {@link SpanProcessor}.
 *
 * @see DemoAutoConfigurationCustomizerProvider
 */
public class DemoSpanProcessor implements SpanProcessor {

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {

    String parentServerSpanId = Optional.ofNullable(parentContext.get(PARENT_SPAN_ID_KEY)).orElse("");
    System.out.println("DemoSpanProcessor.onStart, parentServerSpanId: " + parentServerSpanId);

    System.out.println("DemoSpanProcessor.onStart, Baggage.fromContext(parentContext): ");
    Baggage.fromContext(parentContext).forEach((k, v) -> {
      System.out.println(k + ": " + v.getValue());
    });

    System.out.println("DemoSpanProcessor.onStart, Baggage.current(): ");
    Baggage.current().forEach((k, v) -> {
      System.out.println(k + ": " + v.getValue());
    });

    span.setAttribute(PARENT_SPAN_ID, parentServerSpanId);
  }

  @Override
  public boolean isStartRequired() {
    return true;
  }

  @Override
  public void onEnd(ReadableSpan span) {}

  @Override
  public boolean isEndRequired() {
    return false;
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode forceFlush() {
    return CompletableResultCode.ofSuccess();
  }
}
