package com.xinchuang.diagnosis.ai.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.xinchuang.diagnosis.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

class FaultPredictionMetricRegistryTest {
    private final FaultPredictionMetricRegistry registry = new FaultPredictionMetricRegistry();

    @Test
    void mapsSupportedPlatformMetricsToStableEngineCodes() {
        var definitions = registry.requireSupported("CPU,数据库连接数,缓存命中率");

        assertEquals(3, definitions.size());
        assertEquals("cpu_usage", definitions.get(0).engineCode());
        assertEquals("current_connections", definitions.get(1).engineCode());
    }

    @Test
    void rejectsMetricWithoutRealPrometheusMapping() {
        BusinessException exception = assertThrows(BusinessException.class,
            () -> registry.requireSupported("CPU,网络带宽"));

        assertEquals("以下预测指标暂未接入真实数据：网络带宽", exception.getMessage());
    }
}
