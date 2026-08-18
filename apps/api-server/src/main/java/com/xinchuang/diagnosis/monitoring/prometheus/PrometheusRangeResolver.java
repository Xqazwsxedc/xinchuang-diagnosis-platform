package com.xinchuang.diagnosis.monitoring.prometheus;

import com.xinchuang.diagnosis.common.exception.BusinessException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/** 将页面的固定时间范围转换为 Prometheus 查询参数，避免范围逻辑散落在业务服务中。 */
@Component
public class PrometheusRangeResolver {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    public PrometheusRange resolve(String range, Instant end) {
        Duration duration = switch (range) {
            case "1H" -> Duration.ofHours(1);
            case "3H" -> Duration.ofHours(3);
            case "6H" -> Duration.ofHours(6);
            case "12H" -> Duration.ofHours(12);
            case "1D" -> Duration.ofDays(1);
            case "3D" -> Duration.ofDays(3);
            case "7D" -> Duration.ofDays(7);
            case "14D" -> Duration.ofDays(14);
            default -> throw new BusinessException("指标时间范围不合法");
        };
        return new PrometheusRange(end.minus(duration), end, stepSeconds(duration));
    }

    public PrometheusRange resolve(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new BusinessException("指标日期范围不合法");
        }
        if (startDate.plusDays(90).isBefore(endDate)) {
            throw new BusinessException("指标日期范围不能超过90天");
        }
        Instant start = startDate.atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
        return new PrometheusRange(start, end, stepSeconds(java.time.Duration.between(start, end)));
    }

    private long stepSeconds(Duration duration) {
        long hours = duration.toHours();
        if (hours <= 1) return 60;
        if (hours <= 6) return 180;
        if (hours <= 12) return 300;
        if (hours <= 24) return 600;
        if (hours <= 72) return 1800;
        if (hours <= 168) return 3600;
        return 7200;
    }
}
