/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.javaagent;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.HashMap;
import java.util.Map;

/**
 * This is one of the main entry points for Instrumentation Agent's customizations. It allows
 * configuring the {@link AutoConfigurationCustomizer}. See the {@link
 * #customize(AutoConfigurationCustomizer)} method below.
 *
 * <p>Also see https://github.com/open-telemetry/opentelemetry-java/issues/2022
 *
 * @see AutoConfigurationCustomizerProvider
 * @see DemoPropagatorProvider
 */
@AutoService(AutoConfigurationCustomizerProvider.class)
public class DemoAutoConfigurationCustomizerProvider
        implements AutoConfigurationCustomizerProvider {

    @Override
    public void customize(AutoConfigurationCustomizer autoConfiguration) {
        autoConfiguration
                // .addTracerProviderCustomizer(this::configureSdkTracerProvider)
                .addPropertiesSupplier(this::getDefaultProperties)
        ;
    }

    private SdkTracerProviderBuilder configureSdkTracerProvider(
            SdkTracerProviderBuilder tracerProvider, ConfigProperties config) {
        return tracerProvider
                // .setIdGenerator(new DemoIdGenerator())
                // .setSpanLimits(SpanLimits.builder().setMaxNumberOfAttributes(1024).build())
                .addSpanProcessor(new DemoSpanProcessor())
                // .addSpanProcessor(SimpleSpanProcessor.create(new DemoSpanExporter()))
                ;
    }

    private Map<String, String> getDefaultProperties() {
        Map<String, String> properties = new HashMap<>();
        // instrumentation
        properties.put("otel.instrumentation.logback-mdc.enabled", "true");
        properties.put("otel.instrumentation.logback-mdc.add-baggage", "true");
        properties.put("otel.instrumentation.logback-appender.enabled", "true");
        properties.put("otel.instrumentation.executors.enabled", "true");
        // propagators
        properties.put("otel.propagators", "tracecontext,baggage,jx");
        // exporter
        properties.put("otel.traces.exporter", "otlp,logging-otlp");
        properties.put("otel.metrics.exporter", "none");
        properties.put("otel.logs.exporter", "none");
        return properties;
    }
}
