package com.xinchuang.diagnosis.monitoring.security;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.xinchuang.diagnosis.monitoring.config.MonitoringProperties;
import org.junit.jupiter.api.Test;

class PrometheusEndpointValidatorTest {
    @Test
    void rejectsPrivateAndUnsafeEndpointsByDefault() {
        MonitoringProperties properties = new MonitoringProperties();
        PrometheusEndpointValidator validator = new PrometheusEndpointValidator(properties);
        assertThrows(RuntimeException.class, () -> validator.validate("http://127.0.0.1:9090"));
        assertThrows(RuntimeException.class, () -> validator.validate("file:///etc/passwd"));
        assertThrows(RuntimeException.class, () -> validator.validate("http://user:pass@example.com"));
    }

    @Test
    void allowsExplicitlyConfiguredHost() {
        MonitoringProperties properties = new MonitoringProperties();
        properties.getPrometheus().setAllowedCidrs(java.util.List.of("192.0.2.0/24"));
        PrometheusEndpointValidator validator = new PrometheusEndpointValidator(properties);
        validator.validate("https://192.0.2.1:9090");
    }
}
