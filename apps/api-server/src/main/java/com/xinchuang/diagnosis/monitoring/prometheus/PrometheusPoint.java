package com.xinchuang.diagnosis.monitoring.prometheus;

import java.math.BigDecimal;
import java.time.Instant;

/** 已完成安全数值转换的单个 Prometheus 样本点。 */
public record PrometheusPoint(Instant timestamp, BigDecimal value) { }
