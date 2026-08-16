package com.xinchuang.diagnosis.monitoring.prometheus;

import java.util.List;

/** 将 Prometheus HTTP 细节隔离在基础设施层，业务服务只消费规范化点位。 */
public interface PrometheusClient {
    List<PrometheusPoint> instantQuery(String promQl);
    List<PrometheusPoint> rangeQuery(String promQl, PrometheusRange range);
}
