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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

    /**
     * 参数加载顺序：
     * 1. getDefaultProperties
     * 2. 命令行参数
     * 3. customizeProperties
     */
    private Map<String, String> customizeProperties(ConfigProperties config) {
        String serviceName = StringUtils.defaultIfBlank(config.getString("otel.service.name"), config.getString("spring.application.name"));
        String activeProfile = StringUtils.defaultIfBlank(config.getString("spring.profiles.active"), System.getenv("PUBLISH_ENV"));
        String tracesExporter = config.getString("otel.traces.exporter");
        String tracesProtocol = config.getString("otel.exporter.otlp.traces.protocol");
        String tracesEndpoint = config.getString("otel.exporter.otlp.traces.endpoint");

        Properties systemProperties = System.getProperties();
        if (systemProperties != null) {
            systemProperties.forEach((k, v) -> System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, System.getProperties(), " + k + ": " + v));
        }
        Map<String, String> systemEnv = System.getenv();
        if (systemEnv != null) {
            systemEnv.forEach((k, v) -> System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, System.getenv(), " + k + ": " + v));
        }

        if (StringUtils.isBlank(activeProfile)) {
            // 无环境
            System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, activeProfile is blank.");
            tracesExporter = StringUtils.defaultIfBlank(tracesExporter, "none");
        } else if (StringUtils.contains(activeProfile, "prd") || StringUtils.contains(activeProfile, "tst")) {
            tracesExporter = StringUtils.defaultIfBlank(tracesExporter, "otlp");
            tracesProtocol = StringUtils.defaultIfBlank(tracesProtocol, "grpc");
            // 生产环境
            if (StringUtils.contains(activeProfile, "prd")) {
                tracesEndpoint = StringUtils.defaultIfBlank(tracesEndpoint, "http://jaeger-app-f5.prod.jxlife.com.cn:4317");
                System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, tracesEndpoint = " + tracesEndpoint);
            }
            // 测试环境
            if (StringUtils.contains(activeProfile, "tst")) {
                tracesEndpoint = StringUtils.defaultIfBlank(tracesEndpoint, "http://jaeger-app-f5.test.jxlife.com.cn:4317");
                System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, tracesEndpoint = " + tracesEndpoint);
            }
        } else {
            // 其它环境
            System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, activeProfile is neither prd or tst.");
            tracesExporter = StringUtils.defaultIfBlank(tracesExporter, "none");
        }

        Map<String, String> properties = new HashMap<>();
        properties.put("otel.service.name", serviceName);
        properties.put("otel.traces.exporter", tracesExporter);
        properties.put("otel.exporter.otlp.traces.protocol", tracesProtocol);
        properties.put("otel.exporter.otlp.traces.endpoint", tracesEndpoint);
        System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, otel.exporter.traces.endpoint: " + tracesExporter + " @ " + tracesProtocol + " @ " + tracesEndpoint);

        return properties;
    }

    private Map<String, String> getDefaultProperties() {
        System.out.println("DemoAutoConfigurationCustomizerProvider, getDefaultProperties");
        Map<String, String> properties = new HashMap<>();

        // exporter
        // properties.put("otel.traces.exporter", "none");
        properties.put("otel.metrics.exporter", "none");
        properties.put("otel.logs.exporter", "none");

        // traces
        properties.put("otel.traces.sampler", "parentbased_traceidratio");
        properties.put("otel.traces.sampler.arg", "0.01");
        // properties.put("otel.traces.sampler", "parentbased_jaeger_remote");
        // properties.put("otel.traces.sampler.arg", "endpoint=http://10.1.143.205:5779,pollingIntervalMs=5000,initialSamplingRate=0.01");

        // propagators
        properties.put("otel.propagators", "tracecontext,baggage,jxlife");

        // instrumentation
        properties.put("otel.instrumentation.logback-mdc.enabled", "true");
        properties.put("otel.instrumentation.logback-mdc.add-baggage", "true");
        properties.put("otel.instrumentation.logback-appender.enabled", "true");
        properties.put("otel.instrumentation.executors.enabled", "true");

        return properties;
    }
}
