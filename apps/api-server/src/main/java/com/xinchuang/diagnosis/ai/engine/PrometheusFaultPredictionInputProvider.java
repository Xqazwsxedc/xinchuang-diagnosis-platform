package com.xinchuang.diagnosis.ai.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinchuang.diagnosis.ai.engine.FaultPredictionMetricRegistry.MetricDefinition;
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
import com.xinchuang.diagnosis.monitoring.prometheus.PrometheusException;
import com.xinchuang.diagnosis.monitoring.prometheus.PrometheusRange;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * 将故障预测任务转换为模型可消费的时序请求。
 *
 * <p>该适配器负责读取资源级 Prometheus 配置、查询过去一小时指标、对齐多指标时间点，
 * 最后生成稳定的 {@link TimeSeriesInferenceRequest}。模型服务不直接接触数据库凭据。</p>
 */
@Component
public class PrometheusFaultPredictionInputProvider {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final TypeReference<Map<String, String>> SELECTOR_TYPE = new TypeReference<>() { };
    private final DbResourceMapper resourceMapper;
    private final DbMonitoringConfigMapper monitoringConfigMapper;
    private final DbPrometheusMetricsService metricsService;
    private final FaultPredictionMetricRegistry metricRegistry;
    private final ObjectMapper objectMapper;

    public PrometheusFaultPredictionInputProvider(DbResourceMapper resourceMapper,
                                                  DbMonitoringConfigMapper monitoringConfigMapper,
                                                  DbPrometheusMetricsService metricsService,
                                                  FaultPredictionMetricRegistry metricRegistry,
                                                  ObjectMapper objectMapper) {
        this.resourceMapper = resourceMapper;
        this.monitoringConfigMapper = monitoringConfigMapper;
        this.metricsService = metricsService;
        this.metricRegistry = metricRegistry;
        this.objectMapper = objectMapper;
    }

    /** 根据任务和执行记录加载历史特征，并构造一次模型推理请求。 */
    public FaultPredictionInput load(AiTask task, TaskExecRecord execution) {
        // 当前版本的时序异常检测只支持分布式数据库资源。
        if (!"DB_RESOURCE".equals(task.getTargetType())) {
            throw new BusinessException("第一版时序异常检测仅支持分布式数据库对象");
        }
        List<MetricDefinition> definitions = metricRegistry.requireSupported(task.getMetricCodes());
        DbResource resource = resourceMapper.selectById(task.getTargetId());
        if (resource == null) {
            throw new BusinessException("分布式数据库不存在");
        }
        DbMonitoringConfig config = monitoringConfigMapper.selectOne(new LambdaQueryWrapper<DbMonitoringConfig>()
            .eq(DbMonitoringConfig::getResourceId, resource.getId()));
        if (config == null || !"HEALTHY".equals(config.getStatus()) || !"NORMAL".equals(resource.getMonitorStatus())) {
            throw new BusinessException("当前数据库监控尚未通过连接测试，无法执行时序异常检测");
        }

        // 输入窗口由任务受理时写入执行记录，模型侧不自行决定时间范围。
        if (execution.getInputStartTime() == null || execution.getInputEndTime() == null) {
            throw new BusinessException("执行记录缺少历史输入窗口");
        }
        PrometheusRange range = new PrometheusRange(
            execution.getInputStartTime().atZone(BUSINESS_ZONE).toInstant(),
            execution.getInputEndTime().atZone(BUSINESS_ZONE).toInstant(), 60);
        // 将平台指标编码映射为 Prometheus 查询编码；selector 由资源监控配置提供。
        Set<String> prometheusCodes = definitions.stream().map(MetricDefinition::prometheusCode).collect(java.util.stream.Collectors.toSet());
        DbMetricsOverviewVO overview;
        try {
            overview = metricsService.load(range, prometheusCodes, config.getPrometheusBaseUrl(), config.getPrometheusAuthRef(),
                selector(config.getNodeTargetSelector()), selector(config.getDatabaseTargetSelector()));
        } catch (PrometheusException exception) {
            throw new BusinessException(503, exception.getMessage());
        }

        // 只有所有选中指标都存在的时间点才能作为一个完整模型样本。
        List<DbMetricPointVO> alignedPoints = overview.getPoints().stream()
            .filter(point -> definitions.stream().allMatch(definition -> definition.valueExtractor().apply(point) != null))
            .toList();
        if (alignedPoints.size() < 3) {
            throw new BusinessException("过去1小时内有效且对齐的指标数据不足3个点");
        }

        TimeSeriesInferenceRequest request = new TimeSeriesInferenceRequest();
        // requestId 用执行记录 ID 关联平台和 BentoML 返回，防止响应串单。
        request.setRequestId("execution-" + execution.getId());
        request.setModelCode("zscore-communication-test");
        request.setTimestamps(alignedPoints.stream()
            .map(point -> point.getSampledAt().atZone(BUSINESS_ZONE).toEpochSecond())
            .toList());
        Map<String, List<Double>> features = new LinkedHashMap<>();
        // 每个 feature 与 timestamps 必须一一对应，保持时序位置不变。
        for (MetricDefinition definition : definitions) {
            features.put(definition.engineCode(), alignedPoints.stream()
                .map(point -> value(definition.valueExtractor().apply(point)))
                .toList());
        }
        request.setFeatures(features);
        // 1.5 是通信验证模型的默认 Z-Score 异常阈值。
        request.setThreshold(1.5);
        return new FaultPredictionInput(request,
            LocalDateTime.ofInstant(range.start(), BUSINESS_ZONE),
            LocalDateTime.ofInstant(range.end(), BUSINESS_ZONE));
    }

    private Map<String, String> selector(String value) {
        // selector 保存在数据库 JSON 字段中，解析失败时阻止模型调用并提示重新配置。
        try {
            return objectMapper.readValue(value, SELECTOR_TYPE);
        } catch (Exception exception) {
            throw new BusinessException("监控 selector 配置损坏，请重新保存");
        }
    }

    private double value(BigDecimal value) {
        return value.doubleValue();
    }
}
