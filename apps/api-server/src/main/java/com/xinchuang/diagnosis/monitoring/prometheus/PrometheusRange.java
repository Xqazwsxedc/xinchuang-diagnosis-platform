package com.xinchuang.diagnosis.monitoring.prometheus;

import java.time.Instant;

/** Prometheus 区间查询的统一时间边界与采样步长。 */
public record PrometheusRange(Instant start, Instant end, long stepSeconds) { }
