package com.xinchuang.diagnosis.monitoring.prometheus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PrometheusSelectorTest {
    @Test
    void formatsTwoExactSelectorsWithoutPromQlOperators() {
        assertEquals("{instance=\"10.0.0.10:9100\",job=\"node-exporter\"}",
            PrometheusSelector.format(Map.of("job", "node-exporter", "instance", "10.0.0.10:9100"), "nodeTargetSelector"));
    }

    @Test
    void rejectsUnsafeLabelsAndEmptySelectors() {
        assertThrows(RuntimeException.class, () -> PrometheusSelector.format(Map.of("job", "x|up"), "selector"));
        assertThrows(RuntimeException.class, () -> PrometheusSelector.format(Map.of("job]", "x"), "selector"));
        assertThrows(RuntimeException.class, () -> PrometheusSelector.format(Map.of(), "selector"));
    }
}
