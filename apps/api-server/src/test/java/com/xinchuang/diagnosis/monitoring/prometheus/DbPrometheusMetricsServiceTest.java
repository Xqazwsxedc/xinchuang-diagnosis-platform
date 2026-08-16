package com.xinchuang.diagnosis.monitoring.prometheus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.xinchuang.diagnosis.monitoring.config.MonitoringProperties;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 验证数据库详情的五类监控指标会合并为原接口可消费的时间序列。 */
class DbPrometheusMetricsServiceTest {

    @Test
    /** 顶部实时值和趋势点同时包含新增的 MySQL 当前连接数。 */
    void combinesFiveMetricsIntoOverview() {
        MonitoringProperties properties = new MonitoringProperties();
        properties.getTargets().setNodeInstance("node:9100");
        properties.getTargets().setMysqlInstance("mysql:9104");
        PrometheusClient client = new FixedPrometheusClient();
        DbPrometheusMetricsService service = new DbPrometheusMetricsService(client,
            new PrometheusQueryFactory(properties), new PrometheusRangeResolver());

        var overview = service.load("1H");

        assertEquals("66.60", overview.getCpuUsage().toPlainString());
        assertEquals(42, overview.getCurrentConnections());
        assertEquals(2, overview.getPoints().size());
        assertEquals(42, overview.getPoints().get(0).getCurrentConnections());
        assertEquals("99.90", overview.getPoints().get(0).getCacheHitRate().toPlainString());
    }

    private static class FixedPrometheusClient implements PrometheusClient {
        private static final List<PrometheusPoint> POINTS = List.of(
            new PrometheusPoint(Instant.parse("2026-08-16T23:00:00Z"), new BigDecimal("66.6")),
            new PrometheusPoint(Instant.parse("2026-08-16T23:03:00Z"), new BigDecimal("66.6"))
        );

        @Override
        public List<PrometheusPoint> instantQuery(String promQl) {
            if (promQl.contains("threads_connected")) return List.of(point("42"));
            if (promQl.contains("node_disk")) return List.of(point("120"));
            if (promQl.contains("buffer_pool")) return List.of(point("99.9"));
            return List.of(point("66.6"));
        }

        @Override
        public List<PrometheusPoint> rangeQuery(String promQl, PrometheusRange range) {
            if (promQl.contains("threads_connected")) return values("42");
            if (promQl.contains("node_disk")) return values("120");
            if (promQl.contains("buffer_pool")) return values("99.9");
            return POINTS;
        }

        private PrometheusPoint point(String value) { return new PrometheusPoint(Instant.now(), new BigDecimal(value)); }
        private List<PrometheusPoint> values(String value) {
            return POINTS.stream().map(point -> new PrometheusPoint(point.timestamp(), new BigDecimal(value))).toList();
        }
    }
}
