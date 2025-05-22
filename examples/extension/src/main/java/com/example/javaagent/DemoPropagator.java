/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.javaagent;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * See <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/context/api-propagators.md">
 * OpenTelemetry Specification</a> for more information about Propagators.
 *
 * @see DemoPropagatorProvider
 */
public class DemoPropagator implements TextMapPropagator {
  public static final String PARENT_SPAN_ID = "parent_span_id";
  public static final ContextKey<String> PARENT_SPAN_ID_KEY = ContextKey.named(PARENT_SPAN_ID);
  // public static final String X_B3_TRACE_ID = "X-B3-TraceId";
  // public static final ContextKey<String> X_B3_TRACE_ID_KEY = ContextKey.named(X_B3_TRACE_ID);
  // public static final String X_B3_PARENT_SPAN_ID = "X-B3-ParentSpanId";
  // public static final ContextKey<String> X_B3_PARENT_SPAN_ID_KEY = ContextKey.named(X_B3_PARENT_SPAN_ID);

  @Override
  public List<String> fields() {
    return Collections.singletonList(PARENT_SPAN_ID);
  }

  @Override
  public <C> void inject(Context context, C carrier, TextMapSetter<C> setter) {
    // System.out.println("DemoPropagator.inject, context(client span): " + context);
    // System.out.println("DemoPropagator.inject, SpanContext(server span): " + Span.current().getSpanContext());

    // String clientSpanId = Span.fromContext(context).getSpanContext().getSpanId();
    String serverSpanId = Span.current().getSpanContext().getSpanId();
    // System.out.println("DemoPropagator.inject, serverSpanId: " + serverSpanId + ", clientSpanId: " + clientSpanId);

    // System.out.println("DemoPropagator.inject, Baggage: ");
    // Baggage.current().forEach((k, v) -> {
    //   System.out.println(k + ": " + v.getValue());
    // });

    setter.set(carrier, PARENT_SPAN_ID, serverSpanId);
  }

  @Override
  public <C> Context extract(Context context, C carrier, TextMapGetter<C> getter) {
    // System.out.println("DemoPropagator.extract, context(parent client context): " + context);
    // System.out.println("DemoPropagator.extract, SpanContext(empty): " + Span.current().getSpanContext());

    String parentServerSpanId = Optional.ofNullable(getter.get(carrier, PARENT_SPAN_ID)).orElse("");

    // System.out.println("DemoPropagator.extract, Baggage.current(): ");
    // Baggage.current().forEach((k, v) -> {
    //   System.out.println(k + ": " + v.getValue());
    // });

    Baggage baggage = Baggage.builder()
            .put(PARENT_SPAN_ID, parentServerSpanId)
            .build();

    return context.with(PARENT_SPAN_ID_KEY, parentServerSpanId).with(baggage);
  }
}
