/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.javaagent;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import org.apache.commons.lang3.StringUtils;

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
public class DemoAutoConfigurationCustomizerProvider implements AutoConfigurationCustomizerProvider {

    @Override
    public void customize(AutoConfigurationCustomizer autoConfiguration) {
        autoConfiguration
                .addPropertiesCustomizer(this::customizeProperties)
                .addPropertiesSupplier(this::getDefaultProperties)
        ;
    }

    private Map<String, String> customizeProperties(ConfigProperties config) {
        String otlpEndpoint = System.getProperty("otel.exporter.otlp.endpoint");
        if (StringUtils.isNotBlank(otlpEndpoint)) {
            System.out.println("DemoAutoConfigurationCustomizerProvider, otel.exporter.otlp.endpoint: " + otlpEndpoint);
            return new HashMap<>();
        }
        String activeProfile = config.getString("spring.profiles.active");
        if (activeProfile != null && activeProfile.contains("prd")) {
            otlpEndpoint = "http://jxlife-framework-jaeger.prd.jxlife.com.cn/v1/traces";
        } else {
            otlpEndpoint = "http://jxlife-framework-jaeger.tst.jxlife.com.cn/v1/traces";
        }
        Map<String, String> properties = new HashMap<>();
        properties.put("otel.exporter.otlp.endpoint", otlpEndpoint);
        System.out.println("DemoAutoConfigurationCustomizerProvider, otel.exporter.otlp.endpoint: " + otlpEndpoint);
        return properties;
    }

    private Map<String, String> getDefaultProperties() {
        Map<String, String> properties = new HashMap<>();
        // traces
        properties.put("otel.traces.sampler", "parentbased_traceidratio");
        properties.put("otel.traces.sampler.arg", "0.1");
        // instrumentation
        properties.put("otel.instrumentation.logback-mdc.enabled", "true");
        properties.put("otel.instrumentation.logback-mdc.add-baggage", "true");
        properties.put("otel.instrumentation.logback-appender.enabled", "true");
        properties.put("otel.instrumentation.executors.enabled", "true");
        // propagators
        properties.put("otel.propagators", "tracecontext,baggage,jxlife");
        // exporter
        properties.put("otel.traces.exporter", "otlp");
        properties.put("otel.metrics.exporter", "none");
        properties.put("otel.logs.exporter", "none");

        return properties;
    }
}
