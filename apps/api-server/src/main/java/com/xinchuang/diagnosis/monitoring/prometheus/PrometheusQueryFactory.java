package com.xinchuang.diagnosis.monitoring.prometheus;

import com.xinchuang.diagnosis.monitoring.config.MonitoringProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 本页面使用的 PromQL 集中定义，实例地址始终从本地配置读取。 */
@Component
public class PrometheusQueryFactory {
    private final MonitoringProperties properties;

    public PrometheusQueryFactory(MonitoringProperties properties) { this.properties = properties; }

    public String cpuUsage() {
        return "100 * (1 - avg by(instance) (rate(node_cpu_seconds_total{instance=\"%s\",mode=\"idle\"}[5m])))"
            .formatted(nodeInstance());
    }

    public String memoryUsage() {
        return "100 * (1 - node_memory_MemAvailable_bytes{instance=\"%s\"} / node_memory_MemTotal_bytes{instance=\"%s\"})"
            .formatted(nodeInstance(), nodeInstance());
    }

    public String diskIops() {
        return "sum by(instance) (rate(node_disk_reads_completed_total{instance=\"%s\",device!~\"^(loop|ram).*\"}[1m]) + rate(node_disk_writes_completed_total{instance=\"%s\",device!~\"^(loop|ram).*\"}[1m]))"
            .formatted(nodeInstance(), nodeInstance());
    }

    public String mysqlConnections() {
        return "mysql_global_status_threads_connected{instance=\"%s\"}".formatted(mysqlInstance());
    }

    public String cacheHitRate() {
        return "100 * (1 - rate(mysql_global_status_innodb_buffer_pool_reads{instance=\"%s\"}[5m]) / clamp_min(rate(mysql_global_status_innodb_buffer_pool_read_requests{instance=\"%s\"}[5m]), 1))"
            .formatted(mysqlInstance(), mysqlInstance());
    }

    private String nodeInstance() { return requireTarget(properties.getTargets().getNodeInstance(), "node_exporter"); }
    private String mysqlInstance() { return requireTarget(properties.getTargets().getMysqlInstance(), "mysqld_exporter"); }

    private String requireTarget(String value, String name) {
        if (!StringUtils.hasText(value)) throw new PrometheusException(name + " 目标实例尚未配置");
        return value;
    }
}
