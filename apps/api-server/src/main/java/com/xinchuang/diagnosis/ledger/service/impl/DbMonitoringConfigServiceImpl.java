package com.xinchuang.diagnosis.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.ledger.dto.DbMonitoringConfigRequest;
import com.xinchuang.diagnosis.ledger.entity.DbMonitoringConfig;
import com.xinchuang.diagnosis.ledger.entity.DbResource;
import com.xinchuang.diagnosis.ledger.mapper.DbMonitoringConfigMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbResourceMapper;
import com.xinchuang.diagnosis.ledger.service.DbMonitoringConfigService;
import com.xinchuang.diagnosis.ledger.vo.DbMonitoringConfigVO;
import com.xinchuang.diagnosis.monitoring.prometheus.PrometheusClient;
import com.xinchuang.diagnosis.monitoring.prometheus.PrometheusException;
import com.xinchuang.diagnosis.monitoring.prometheus.PrometheusSelector;
import com.xinchuang.diagnosis.monitoring.security.PrometheusEndpointValidator;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DbMonitoringConfigServiceImpl implements DbMonitoringConfigService {
    private static final TypeReference<Map<String, String>> SELECTOR_TYPE = new TypeReference<>() { };
    private final DbMonitoringConfigMapper configMapper;
    private final DbResourceMapper resourceMapper;
    private final ObjectMapper objectMapper;
    private final PrometheusEndpointValidator endpointValidator;
    private final PrometheusClient prometheusClient;

    public DbMonitoringConfigServiceImpl(DbMonitoringConfigMapper configMapper, DbResourceMapper resourceMapper,
                                         ObjectMapper objectMapper, PrometheusEndpointValidator endpointValidator,
                                         PrometheusClient prometheusClient) {
        this.configMapper = configMapper;
        this.resourceMapper = resourceMapper;
        this.objectMapper = objectMapper;
        this.endpointValidator = endpointValidator;
        this.prometheusClient = prometheusClient;
    }

    @Override
    public DbMonitoringConfigVO get(Long resourceId) {
        requireResource(resourceId);
        DbMonitoringConfig config = find(resourceId);
        return config == null ? empty() : toVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DbMonitoringConfigVO save(Long resourceId, DbMonitoringConfigRequest request) {
        requireResource(resourceId);
        String baseUrl = endpointValidator.validate(request.getPrometheusBaseUrl()).toString();
        PrometheusSelector.format(request.getNodeTargetSelector(), "nodeTargetSelector");
        PrometheusSelector.format(request.getDatabaseTargetSelector(), "databaseTargetSelector");
        if (!"MYSQL".equalsIgnoreCase(request.getDatabaseType())) throw new BusinessException("当前仅支持 MYSQL 指标适配");
        if (StringUtils.hasText(request.getPrometheusAuthRef()) && request.getPrometheusAuthRef().length() > 128) {
            throw new BusinessException("Prometheus 凭据引用不合法");
        }
        DbMonitoringConfig config = find(resourceId);
        if (config == null) {
            config = new DbMonitoringConfig();
            config.setResourceId(resourceId);
        }
        config.setPrometheusBaseUrl(baseUrl);
        config.setPrometheusAuthRef(StringUtils.hasText(request.getPrometheusAuthRef()) ? request.getPrometheusAuthRef().trim() : null);
        try {
            config.setNodeTargetSelector(objectMapper.writeValueAsString(request.getNodeTargetSelector()));
            config.setDatabaseTargetSelector(objectMapper.writeValueAsString(request.getDatabaseTargetSelector()));
        } catch (JsonProcessingException ex) {
            throw new BusinessException("监控 selector 配置无法保存");
        }
        config.setDatabaseType(request.getDatabaseType().trim().toUpperCase());
        config.setStatus("UNCONFIGURED");
        config.setLastErrorMessage(null);
        if (config.getId() == null) configMapper.insert(config); else configMapper.updateById(config);
        DbResource resource = requireResource(resourceId);
        resource.setMonitorStatus("PENDING");
        resource.setMonitorMessage("监控配置已保存，尚未测试连接");
        resource.setMonitorLastCheckedAt(null);
        resourceMapper.updateById(resource);
        return toVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DbMonitoringConfigVO test(Long resourceId) {
        requireResource(resourceId);
        DbMonitoringConfig config = find(resourceId);
        if (config == null) throw new BusinessException(409, "监控尚未配置");
        config.setStatus("TESTING");
        configMapper.updateById(config);
        LocalDateTime checkedAt = LocalDateTime.now();
        try {
            Map<String, String> node = parseSelector(config.getNodeTargetSelector());
            Map<String, String> database = parseSelector(config.getDatabaseTargetSelector());
            if (prometheusClient.instantQuery(config.getPrometheusBaseUrl(), config.getPrometheusAuthRef(), "up" + nodeSelector(node)).isEmpty()) {
                throw new PrometheusException("未找到 nodeTargetSelector 对应目标");
            }
            String healthQuery = "mysql_global_status_threads_connected" + nodeSelector(database);
            if (prometheusClient.instantQuery(config.getPrometheusBaseUrl(), config.getPrometheusAuthRef(), healthQuery).isEmpty()) {
                throw new PrometheusException("未找到 databaseTargetSelector 对应 MySQL 目标");
            }
            config.setStatus("HEALTHY");
            config.setLastErrorMessage(null);
            config.setLastCheckedAt(checkedAt);
            configMapper.updateById(config);
            DbResource resource = requireResource(resourceId);
            resource.setMonitorStatus("NORMAL");
            resource.setMonitorMessage("Prometheus 连接和 MySQL 目标验证通过");
            resource.setMonitorLastCheckedAt(checkedAt);
            resourceMapper.updateById(resource);
            return toVO(config);
        } catch (RuntimeException ex) {
            String message = safeMessage(ex);
            config.setStatus("ABNORMAL");
            config.setLastErrorMessage(message);
            config.setLastCheckedAt(checkedAt);
            configMapper.updateById(config);
            DbResource resource = requireResource(resourceId);
            resource.setMonitorStatus("ABNORMAL");
            resource.setMonitorMessage(message);
            resource.setMonitorLastCheckedAt(checkedAt);
            resourceMapper.updateById(resource);
            throw new BusinessException(503, message);
        }
    }

    private String nodeSelector(Map<String, String> selector) { return PrometheusSelector.format(selector, "selector"); }
    private Map<String, String> parseSelector(String value) {
        try { return objectMapper.readValue(value, SELECTOR_TYPE); } catch (JsonProcessingException ex) { throw new BusinessException("监控 selector 配置损坏"); }
    }
    private DbMonitoringConfig find(Long resourceId) {
        return configMapper.selectOne(new LambdaQueryWrapper<DbMonitoringConfig>().eq(DbMonitoringConfig::getResourceId, resourceId));
    }
    private DbResource requireResource(Long resourceId) {
        DbResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) throw new BusinessException("分布式数据库不存在");
        return resource;
    }
    private DbMonitoringConfigVO empty() {
        DbMonitoringConfigVO vo = new DbMonitoringConfigVO();
        vo.setStatus("UNCONFIGURED");
        return vo;
    }
    private DbMonitoringConfigVO toVO(DbMonitoringConfig config) {
        DbMonitoringConfigVO vo = new DbMonitoringConfigVO();
        vo.setPrometheusBaseUrl(config.getPrometheusBaseUrl());
        vo.setCredentialsConfigured(StringUtils.hasText(config.getPrometheusAuthRef()));
        vo.setNodeTargetSelector(parseSelector(config.getNodeTargetSelector()));
        vo.setDatabaseTargetSelector(parseSelector(config.getDatabaseTargetSelector()));
        vo.setDatabaseType(config.getDatabaseType());
        vo.setStatus(config.getStatus());
        vo.setLastCheckedAt(config.getLastCheckedAt());
        vo.setLastErrorMessage(config.getLastErrorMessage());
        return vo;
    }
    private String safeMessage(Throwable ex) {
        String message = ex.getMessage();
        if (!StringUtils.hasText(message)) return "Prometheus 连接失败";
        String sanitized = message.replaceAll("(?i)(authorization|token|password)\\s*[:=]\\s*\\S+", "$1=[REDACTED]")
            .replaceAll("https?://[^\\s]+", "Prometheus 地址");
        return sanitized.substring(0, Math.min(500, sanitized.length()));
    }
}
