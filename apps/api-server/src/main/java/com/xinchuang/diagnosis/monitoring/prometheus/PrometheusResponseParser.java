package com.xinchuang.diagnosis.monitoring.prometheus;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** 解析 Prometheus vector/matrix 响应，并丢弃 NaN、Inf 等无法展示的样本。 */
@Component
public class PrometheusResponseParser {
    public List<PrometheusPoint> parse(JsonNode body, boolean range) {
        if (body == null || !"success".equals(body.path("status").asText())) {
            throw new PrometheusException("监控服务返回了不可识别的响应");
        }
        JsonNode results = body.path("data").path("result");
        if (!results.isArray() || results.isEmpty()) return List.of();
        JsonNode series = results.get(0);
        JsonNode samples = range ? series.path("values") : singleValue(series);
        if (!samples.isArray()) return List.of();
        List<PrometheusPoint> points = new ArrayList<>();
        if (range) {
            for (JsonNode sample : samples) addPoint(points, sample);
        } else {
            addPoint(points, samples);
        }
        return points;
    }

    private JsonNode singleValue(JsonNode series) { return series.path("value"); }

    private void addPoint(List<PrometheusPoint> points, JsonNode sample) {
        if (!sample.isArray() || sample.size() < 2) return;
        try {
            BigDecimal value = new BigDecimal(sample.get(1).asText());
            if (value.signum() == 0 || value.abs().compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) < 0) {
                long milliseconds = sample.get(0).decimalValue().movePointRight(3).longValue();
                points.add(new PrometheusPoint(Instant.ofEpochMilli(milliseconds), value));
            }
        } catch (NumberFormatException | ArithmeticException ignored) {
            // Prometheus 可能返回 NaN、Inf；该类点不能进入图表或 CSV。
        }
    }
}
