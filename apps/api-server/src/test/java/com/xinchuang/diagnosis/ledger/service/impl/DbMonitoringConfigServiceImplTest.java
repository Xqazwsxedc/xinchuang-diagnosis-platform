package com.xinchuang.diagnosis.ledger.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.ledger.dto.DbMonitoringConfigRequest;
import com.xinchuang.diagnosis.ledger.entity.DbMonitoringConfig;
import com.xinchuang.diagnosis.ledger.entity.DbResource;
import com.xinchuang.diagnosis.ledger.mapper.DbMonitoringConfigMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbResourceMapper;
import com.xinchuang.diagnosis.monitoring.config.MonitoringProperties;
import com.xinchuang.diagnosis.monitoring.prometheus.PrometheusClient;
import com.xinchuang.diagnosis.monitoring.security.PrometheusEndpointValidator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DbMonitoringConfigServiceImplTest {
    private DbMonitoringConfigMapper configMapper;
    private DbResourceMapper resourceMapper;
    private PrometheusClient client;
    private DbMonitoringConfigServiceImpl service;
    private DbResource resource;

    @BeforeEach
    void setUp() {
        configMapper = mock(DbMonitoringConfigMapper.class);
        resourceMapper = mock(DbResourceMapper.class);
        client = mock(PrometheusClient.class);
        MonitoringProperties properties = new MonitoringProperties();
        properties.getPrometheus().setAllowedCidrs(List.of("192.0.2.0/24"));
        service = new DbMonitoringConfigServiceImpl(configMapper, resourceMapper, new ObjectMapper(),
            new PrometheusEndpointValidator(properties), client);
        resource = new DbResource();
        resource.setId(7L);
        when(resourceMapper.selectById(7L)).thenReturn(resource);
    }

    @Test
    void savingConfigurationLeavesItUnconfiguredUntilTestPasses() {
        when(configMapper.selectOne(any())).thenReturn(null);
        var request = request();
        var result = service.save(7L, request);
        assertEquals("UNCONFIGURED", result.getStatus());
        assertEquals("PENDING", resource.getMonitorStatus());
        verify(configMapper).insert(any(DbMonitoringConfig.class));
    }

    @Test
    void successfulTestRequiresBothTargetsAndMarksResourceHealthy() {
        DbMonitoringConfig config = config();
        when(configMapper.selectOne(any())).thenReturn(config);
        when(client.instantQuery(any(), any(), any())).thenReturn(List.of(new com.xinchuang.diagnosis.monitoring.prometheus.PrometheusPoint(java.time.Instant.now(), java.math.BigDecimal.ONE)));
        var result = service.test(7L);
        assertEquals("HEALTHY", result.getStatus());
        assertEquals("NORMAL", resource.getMonitorStatus());
    }

    @Test
    void failedTestMarksAbnormalWithoutDeletingExistingConfig() {
        DbMonitoringConfig config = config();
        when(configMapper.selectOne(any())).thenReturn(config);
        when(client.instantQuery(any(), any(), any())).thenReturn(List.of());
        assertThrows(BusinessException.class, () -> service.test(7L));
        assertEquals("ABNORMAL", config.getStatus());
        org.mockito.Mockito.verify(configMapper, org.mockito.Mockito.times(2)).updateById(config);
    }

    private DbMonitoringConfigRequest request() {
        DbMonitoringConfigRequest request = new DbMonitoringConfigRequest();
        request.setPrometheusBaseUrl("http://192.0.2.1:9090");
        request.setNodeTargetSelector(Map.of("job", "node-exporter", "instance", "10.0.0.10:9100"));
        request.setDatabaseTargetSelector(Map.of("job", "mysqld-exporter", "instance", "10.0.0.10:9104"));
        request.setDatabaseType("MYSQL");
        return request;
    }

    private DbMonitoringConfig config() {
        DbMonitoringConfig config = new DbMonitoringConfig();
        config.setResourceId(7L);
        config.setPrometheusBaseUrl("http://192.0.2.1:9090");
        config.setNodeTargetSelector("{\"instance\":\"10.0.0.10:9100\",\"job\":\"node-exporter\"}");
        config.setDatabaseTargetSelector("{\"instance\":\"10.0.0.10:9104\",\"job\":\"mysqld-exporter\"}");
        config.setDatabaseType("MYSQL");
        config.setStatus("UNCONFIGURED");
        return config;
    }
}
