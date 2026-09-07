package com.xinchuang.diagnosis.ai.engine;

import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.ledger.vo.DbMetricPointVO;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FaultPredictionMetricRegistry {
    private final Map<String, MetricDefinition> definitions = definitions();

    public List<MetricDefinition> requireSupported(String metricCodes) {
        List<String> selected = Arrays.stream(metricCodes == null ? new String[0] : metricCodes.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .distinct()
            .toList();
        if (selected.isEmpty()) {
            throw new BusinessException("至少选择一项已接入的预测指标");
        }
        List<String> unsupported = selected.stream().filter(code -> !definitions.containsKey(code)).toList();
        if (!unsupported.isEmpty()) {
            throw new BusinessException("以下预测指标暂未接入真实数据：" + String.join("、", unsupported));
        }
        return selected.stream().map(definitions::get).toList();
    }

    private Map<String, MetricDefinition> definitions() {
        Map<String, MetricDefinition> values = new LinkedHashMap<>();
        values.put("CPU", new MetricDefinition("CPU", "cpuUsage", "cpu_usage", DbMetricPointVO::getCpuUsage));
        values.put("内存", new MetricDefinition("内存", "memoryUsage", "memory_usage", DbMetricPointVO::getMemoryUsage));
        values.put("磁盘IO", new MetricDefinition("磁盘IO", "diskIops", "disk_iops", point -> decimal(point.getDiskIops())));
        values.put("数据库连接数", new MetricDefinition("数据库连接数", "currentConnections", "current_connections",
            point -> decimal(point.getCurrentConnections())));
        values.put("缓存命中率", new MetricDefinition("缓存命中率", "cacheHitRate", "cache_hit_rate", DbMetricPointVO::getCacheHitRate));
        return Map.copyOf(values);
    }

    private static BigDecimal decimal(Integer value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    public record MetricDefinition(String platformCode, String prometheusCode, String engineCode,
                                   Function<DbMetricPointVO, BigDecimal> valueExtractor) { }
}
