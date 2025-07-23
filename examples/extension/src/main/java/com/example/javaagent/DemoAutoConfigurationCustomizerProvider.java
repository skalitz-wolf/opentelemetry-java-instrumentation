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
import org.apache.commons.lang3.Strings;

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
        ;
    }

    /**
     * 参数加载顺序：
     * 1. getDefaultProperties
     * 2. 命令行参数
     * 3. customizeProperties
     */
    private Map<String, String> customizeProperties(ConfigProperties config) {
        // env
        String env = StringUtils.defaultIfBlank(config.getString("jxlife.env"), System.getenv("PUBLISH_ENV"));
        // properties
        String serviceName = StringUtils.defaultIfBlank(config.getString("otel.service.name"), new SpringBootServiceNameDetector().createResource());
        String tracesExporter = config.getString("otel.traces.exporter");
        String tracesProtocol = config.getString("otel.exporter.otlp.traces.protocol");
        String tracesEndpoint = config.getString("otel.exporter.otlp.traces.endpoint");
        String sampler = config.getString("otel.traces.sampler");
        String samplerArg = config.getString("otel.traces.sampler.arg");

        if (Strings.CI.equals(config.getString("otel.javaagent.debug"), "true")) {
            Properties systemProperties = System.getProperties();
            if (systemProperties != null) {
                systemProperties.forEach((k, v) -> System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, System.getProperties(), " + k + ": " + v));
            }
            Map<String, String> systemEnv = System.getenv();
            if (systemEnv != null) {
                systemEnv.forEach((k, v) -> System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, System.getenv(), " + k + ": " + v));
            }
        }

        if (StringUtils.isBlank(env)) {
            // 无环境
            System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, env is blank.");
            tracesExporter = StringUtils.defaultIfBlank(tracesExporter, "none");
            sampler = StringUtils.defaultIfBlank(sampler, "parentbased_traceidratio");
            samplerArg = StringUtils.defaultIfBlank(samplerArg, "0");
        } else if (Strings.CI.containsAny(env, "prd", "prod", "tst", "test")) {
            System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, env is " + env);
            tracesExporter = StringUtils.defaultIfBlank(tracesExporter, "otlp");
            tracesProtocol = StringUtils.defaultIfBlank(tracesProtocol, "grpc");
            sampler = StringUtils.defaultIfBlank(sampler, "parentbased_jaeger_remote");
            // 生产环境
            if (Strings.CI.containsAny(env, "prd", "prod")) {
                tracesEndpoint = StringUtils.defaultIfBlank(tracesEndpoint, "http://jaeger-app-f5.prod.jxlife.com.cn:4317");
                samplerArg = StringUtils.defaultIfBlank(samplerArg, "endpoint=http://jaeger-app-f5.prod.jxlife.com.cn:5779,pollingIntervalMs=300000,initialSamplingRate=0.001");
            }
            // 测试环境
            if (Strings.CI.containsAny(env, "tst", "test")) {
                tracesEndpoint = StringUtils.defaultIfBlank(tracesEndpoint, "http://jaeger-app-f5.test.jxlife.com.cn:4317");
                samplerArg = StringUtils.defaultIfBlank(samplerArg, "endpoint=http://jaeger-app-f5.test.jxlife.com.cn:5779,pollingIntervalMs=10000,initialSamplingRate=0.01");
            }
        } else {
            // 其它环境
            System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, env is " + env + ", but there is no corresponding Jager");
            tracesExporter = StringUtils.defaultIfBlank(tracesExporter, "none");
            sampler = StringUtils.defaultIfBlank(sampler, "parentbased_traceidratio");
            samplerArg = StringUtils.defaultIfBlank(samplerArg, "0");
        }

        Map<String, String> properties = new HashMap<>();
        properties.put("otel.service.name", serviceName);
        // exporter
        properties.put("otel.traces.exporter", tracesExporter);
        properties.put("otel.metrics.exporter", StringUtils.defaultIfBlank(config.getString("otel.metrics.exporter"), "none"));
        properties.put("otel.logs.exporter", StringUtils.defaultIfBlank(config.getString("otel.logs.exporter"), "none"));
        // traces
        properties.put("otel.exporter.otlp.traces.protocol", tracesProtocol);
        properties.put("otel.exporter.otlp.traces.endpoint", tracesEndpoint);
        properties.put("otel.traces.sampler", sampler);
        properties.put("otel.traces.sampler.arg", samplerArg);
        // propagators
        properties.put("otel.propagators", StringUtils.defaultIfBlank(config.getString("otel.propagators"), "tracecontext,baggage,jxlife"));
        // instrumentation
        properties.put("otel.instrumentation.logback-mdc.enabled", StringUtils.defaultIfBlank(config.getString("otel.instrumentation.logback-mdc.enabled"), "true"));
        properties.put("otel.instrumentation.logback-mdc.add-baggage", StringUtils.defaultIfBlank(config.getString("otel.instrumentation.logback-mdc.add-baggage"), "true"));
        properties.put("otel.instrumentation.logback-appender.enabled", StringUtils.defaultIfBlank(config.getString("otel.instrumentation.logback-appender.enabled"), "true"));
        properties.put("otel.instrumentation.executors.enabled", StringUtils.defaultIfBlank(config.getString("otel.instrumentation.executors.enabled"), "true"));

        System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, otel.service.name: " + serviceName);
        System.out.println("DemoAutoConfigurationCustomizerProvider, customizeProperties, otel.exporter.traces.endpoint: " + tracesExporter + " @ " + tracesProtocol + " @ " + tracesEndpoint);

        return properties;
    }

}
