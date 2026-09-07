package com.xinchuang.diagnosis.ai.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinchuang.diagnosis.ai.engine.FaultPredictionMetricRegistry.MetricDefinition;
import com.xinchuang.diagnosis.ai.entity.AiTask;
import com.xinchuang.diagnosis.ai.entity.FaultReport;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.ai.mapper.AiTaskMapper;
import com.xinchuang.diagnosis.ai.mapper.FaultReportMapper;
import com.xinchuang.diagnosis.ai.mapper.TaskExecRecordMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FaultPredictionExecutionStateService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private final TaskExecRecordMapper executionMapper;
    private final AiTaskMapper taskMapper;
    private final FaultReportMapper reportMapper;
    private final FaultPredictionMetricRegistry metricRegistry;
    private final ObjectMapper objectMapper;

    public FaultPredictionExecutionStateService(TaskExecRecordMapper executionMapper, AiTaskMapper taskMapper,
                                                 FaultReportMapper reportMapper, FaultPredictionMetricRegistry metricRegistry,
                                                 ObjectMapper objectMapper) {
        this.executionMapper = executionMapper;
        this.taskMapper = taskMapper;
        this.reportMapper = reportMapper;
        this.metricRegistry = metricRegistry;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public ExecutionContext claim(Long executionId) {
        int updated = executionMapper.update(null, new LambdaUpdateWrapper<TaskExecRecord>()
            .eq(TaskExecRecord::getId, executionId)
            .eq(TaskExecRecord::getTaskType, "FAULT_PREDICTION")
            .eq(TaskExecRecord::getExecutionStatus, "PENDING")
            .set(TaskExecRecord::getExecutionStatus, "RUNNING")
            .set(TaskExecRecord::getUpdatedAt, LocalDateTime.now())
            .set(TaskExecRecord::getErrorMessage, null));
        if (updated != 1) return null;
        TaskExecRecord record = executionMapper.selectById(executionId);
        AiTask task = record == null ? null : taskMapper.selectById(record.getTaskId());
        if (record == null || task == null || !"FAULT_PREDICTION".equals(task.getTaskType())) {
            fail(executionId, "执行记录关联的故障预测任务不存在");
            return null;
        }
        return new ExecutionContext(task, record);
    }

    @Transactional(rollbackFor = Exception.class)
    public void complete(ExecutionContext context, FaultPredictionInput input, TimeSeriesInferenceResponse response) {
        TaskExecRecord current = executionMapper.selectById(context.record().getId());
        if (current == null || !"RUNNING".equals(current.getExecutionStatus())) return;
        List<MetricDefinition> definitions = metricRegistry.requireSupported(context.task().getMetricCodes());
        Map<String, MetricDefinition> byEngineCode = definitions.stream()
            .collect(Collectors.toMap(MetricDefinition::engineCode, Function.identity()));
        Map<String, TimeSeriesInferenceResponse.AnomalyPoint> strongestByMetric = response.getAnomalies().stream()
            .filter(point -> byEngineCode.containsKey(point.getMetricCode()))
            .collect(Collectors.toMap(TimeSeriesInferenceResponse.AnomalyPoint::getMetricCode, Function.identity(),
                (left, right) -> score(left) >= score(right) ? left : right, LinkedHashMap::new));

        int inserted = 0;
        for (Map.Entry<String, TimeSeriesInferenceResponse.AnomalyPoint> entry : strongestByMetric.entrySet()) {
            MetricDefinition definition = byEngineCode.get(entry.getKey());
            TimeSeriesInferenceResponse.AnomalyPoint anomaly = entry.getValue();
            String reportCode = reportCode(current.getId(), definition.engineCode());
            Long existing = reportMapper.selectCount(new LambdaQueryWrapper<FaultReport>()
                .eq(FaultReport::getReportCode, reportCode));
            if (existing != null && existing > 0) continue;
            FaultReport report = report(context.task(), current, input, definition, anomaly, reportCode);
            reportMapper.insert(report);
            inserted++;
        }

        current.setInputStartTime(input.inputStartTime());
        current.setInputEndTime(input.inputEndTime());
        current.setExecutionStatus("SUCCESS");
        current.setEndTime(LocalDateTime.now());
        Long executionResultCount = reportMapper.selectCount(new LambdaQueryWrapper<FaultReport>()
            .eq(FaultReport::getExecutionId, current.getId()));
        current.setResultCount(executionResultCount == null ? inserted : executionResultCount.intValue());
        current.setErrorMessage(null);
        executionMapper.updateById(current);

        if (inserted > 0) {
            taskMapper.update(null, new LambdaUpdateWrapper<AiTask>()
                .eq(AiTask::getId, context.task().getId())
                .setSql("result_count = COALESCE(result_count, 0) + " + inserted));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void fail(Long executionId, String message) {
        TaskExecRecord record = executionMapper.selectById(executionId);
        if (record == null || !("RUNNING".equals(record.getExecutionStatus()) || "PENDING".equals(record.getExecutionStatus()))) return;
        record.setExecutionStatus("FAILED");
        record.setEndTime(LocalDateTime.now());
        record.setResultCount(0);
        record.setErrorMessage(safeMessage(message));
        executionMapper.updateById(record);
    }

    private FaultReport report(AiTask task, TaskExecRecord execution, FaultPredictionInput input,
                               MetricDefinition definition, TimeSeriesInferenceResponse.AnomalyPoint anomaly,
                               String reportCode) {
        LocalDateTime anomalyTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(anomaly.getTimestamp()), BUSINESS_ZONE);
        FaultReport report = new FaultReport();
        report.setReportCode(reportCode);
        report.setTaskId(task.getId());
        report.setExecutionId(execution.getId());
        report.setTargetType(task.getTargetType());
        report.setTargetId(task.getTargetId());
        report.setTargetNameSnapshot(task.getTargetNameSnapshot());
        report.setAlertTime(LocalDateTime.now());
        report.setIndicatorCategory(definition.platformCode());
        report.setFaultType(definition.platformCode() + "时序异常");
        report.setRiskLevel(riskLevel(score(anomaly)));
        report.setExpectedStartTime(anomalyTime);
        report.setExpectedEndTime(anomalyTime);
        report.setConfidence(confidence(score(anomaly)));
        report.setForecastDescription("基于过去1小时真实 Prometheus 数据检测到异常点；当前为异常检测，不代表未来故障预测。异常分数："
            + BigDecimal.valueOf(score(anomaly)).setScale(2, RoundingMode.HALF_UP));
        report.setHandleSuggestion("请核查该指标在异常时间附近的运行状态、关联日志与资源负载。");
        report.setTrendJson(trendJson(input.request(), definition.engineCode()));
        report.setProcessStatus("PENDING");
        return report;
    }

    private String trendJson(TimeSeriesInferenceRequest request, String engineCode) {
        List<Double> values = request.getFeatures().get(engineCode);
        List<Map<String, Object>> points = java.util.stream.IntStream.range(0, request.getTimestamps().size())
            .mapToObj(index -> Map.<String, Object>of(
                "time", LocalDateTime.ofInstant(Instant.ofEpochSecond(request.getTimestamps().get(index)), BUSINESS_ZONE)
                    .format(DateTimeFormatter.ofPattern("HH:mm")),
                "value", values.get(index)))
            .toList();
        try {
            return objectMapper.writeValueAsString(points);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("指标趋势序列化失败", exception);
        }
    }

    private String reportCode(Long executionId, String engineCode) {
        return "AI-" + executionId + "-" + engineCode.toUpperCase();
    }

    private String riskLevel(double score) {
        if (score < 2) return "LOW";
        if (score < 3) return "MEDIUM";
        if (score < 4) return "HIGH";
        return "CRITICAL";
    }

    private BigDecimal confidence(double score) {
        return BigDecimal.valueOf(Math.min(99.99, 50 + score * 12.5)).setScale(2, RoundingMode.HALF_UP);
    }

    private double score(TimeSeriesInferenceResponse.AnomalyPoint point) {
        return point.getScore() == null ? 0 : point.getScore();
    }

    private String safeMessage(String message) {
        String value = StringUtils.hasText(message) ? message : "时序异常检测执行失败";
        value = value.replaceAll("(?i)(authorization|token|password)\\s*[:=]\\s*\\S+", "$1=[REDACTED]")
            .replaceAll("https?://[^\\s]+", "外部服务地址");
        return value.substring(0, Math.min(500, value.length()));
    }

    public record ExecutionContext(AiTask task, TaskExecRecord record) { }
}
