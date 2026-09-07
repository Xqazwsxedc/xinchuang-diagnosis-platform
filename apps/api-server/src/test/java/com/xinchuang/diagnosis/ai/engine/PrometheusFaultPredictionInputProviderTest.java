package com.xinchuang.diagnosis.ai.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinchuang.diagnosis.ai.entity.AiTask;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.ledger.entity.DbMonitoringConfig;
import com.xinchuang.diagnosis.ledger.entity.DbResource;
import com.xinchuang.diagnosis.ledger.mapper.DbMonitoringConfigMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbResourceMapper;
import com.xinchuang.diagnosis.ledger.vo.DbMetricPointVO;
import com.xinchuang.diagnosis.ledger.vo.DbMetricsOverviewVO;
import com.xinchuang.diagnosis.monitoring.prometheus.DbPrometheusMetricsService;
import com.xinchuang.diagnosis.monitoring.prometheus.PrometheusRange;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrometheusFaultPredictionInputProviderTest {
    @Mock private DbResourceMapper resourceMapper;
    @Mock private DbMonitoringConfigMapper configMapper;
    @Mock private DbPrometheusMetricsService metricsService;
    private PrometheusFaultPredictionInputProvider provider;

    @BeforeEach
    void setUp() {
        provider = new PrometheusFaultPredictionInputProvider(resourceMapper, configMapper, metricsService,
            new FaultPredictionMetricRegistry(), new ObjectMapper());
    }

    @Test
    void buildsAlignedEngineFeaturesFromRealMetricPoints() {
        arrangeHealthyMonitoring();
        DbMetricsOverviewVO overview = new DbMetricsOverviewVO();
        overview.setPoints(List.of(point(0, 10, 50), point(1, 20, 60), point(2, 30, 70)));
        when(metricsService.load(any(PrometheusRange.class), anySet(), eq("http://prometheus"), eq(null), anyMap(), anyMap()))
            .thenReturn(overview);

        FaultPredictionInput input = provider.load(task("CPU,数据库连接数"), execution());

        assertEquals("execution-9", input.request().getRequestId());
        assertEquals(List.of(10.0, 20.0, 30.0), input.request().getFeatures().get("cpu_usage"));
        assertEquals(List.of(50.0, 60.0, 70.0), input.request().getFeatures().get("current_connections"));
        assertEquals(3, input.request().getTimestamps().size());
    }

    @Test
    void rejectsInputWhenAlignedPointsAreInsufficient() {
        arrangeHealthyMonitoring();
        DbMetricsOverviewVO overview = new DbMetricsOverviewVO();
        overview.setPoints(List.of(point(0, 10, 50), point(1, 20, null), point(2, 30, null)));
        when(metricsService.load(any(PrometheusRange.class), anySet(), eq("http://prometheus"), eq(null), anyMap(), anyMap()))
            .thenReturn(overview);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> provider.load(task("CPU,数据库连接数"), execution()));

        assertEquals("过去1小时内有效且对齐的指标数据不足3个点", exception.getMessage());
    }

    private void arrangeHealthyMonitoring() {
        DbResource resource = new DbResource(); resource.setId(1L); resource.setMonitorStatus("NORMAL");
        DbMonitoringConfig config = new DbMonitoringConfig(); config.setStatus("HEALTHY");
        config.setPrometheusBaseUrl("http://prometheus"); config.setNodeTargetSelector("{}"); config.setDatabaseTargetSelector("{}");
        when(resourceMapper.selectById(1L)).thenReturn(resource);
        when(configMapper.selectOne(any())).thenReturn(config);
    }

    private AiTask task(String metrics) {
        AiTask task = new AiTask(); task.setTargetType("DB_RESOURCE"); task.setTargetId(1L); task.setMetricCodes(metrics);
        return task;
    }

    private TaskExecRecord execution() {
        TaskExecRecord record = new TaskExecRecord(); record.setId(9L);
        record.setInputStartTime(LocalDateTime.of(2026, 8, 27, 10, 0));
        record.setInputEndTime(LocalDateTime.of(2026, 8, 27, 11, 0));
        return record;
    }

    private DbMetricPointVO point(int minute, Integer cpu, Integer connections) {
        DbMetricPointVO point = new DbMetricPointVO();
        point.setSampledAt(LocalDateTime.of(2026, 8, 27, 10, minute));
        point.setCpuUsage(cpu == null ? null : BigDecimal.valueOf(cpu));
        point.setCurrentConnections(connections);
        return point;
    }
}
