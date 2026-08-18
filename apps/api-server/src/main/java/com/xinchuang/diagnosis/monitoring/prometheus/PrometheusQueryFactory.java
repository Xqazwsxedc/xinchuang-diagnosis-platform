package com.xinchuang.diagnosis.monitoring.prometheus;

import com.xinchuang.diagnosis.monitoring.config.MonitoringProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.Map;

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

    public String cpuUsage(Map<String, String> selector) { return cpuUsage(PrometheusSelector.format(selector, "nodeTargetSelector")); }
    public String memoryUsage(Map<String, String> selector) { return memoryUsage(PrometheusSelector.format(selector, "nodeTargetSelector")); }
    public String diskIops(Map<String, String> selector) { return diskIops(PrometheusSelector.format(selector, "nodeTargetSelector")); }
    public String mysqlConnections(Map<String, String> selector) { return mysqlConnections(PrometheusSelector.format(selector, "databaseTargetSelector")); }
    public String cacheHitRate(Map<String, String> selector) { return cacheHitRate(PrometheusSelector.format(selector, "databaseTargetSelector")); }

    private String cpuUsage(String selector) {
        return "100 * (1 - avg by(instance) (rate(node_cpu_seconds_total" + withLabel(selector, "mode=\"idle\"") + "[5m])))";
    }
    private String memoryUsage(String selector) {
        return "100 * (1 - node_memory_MemAvailable_bytes" + selector + " / node_memory_MemTotal_bytes" + selector + ")";
    }
    private String diskIops(String selector) {
        return "sum by(instance) (rate(node_disk_reads_completed_total" + withLabel(selector, "device!~\"^(loop|ram).*\"") + "[1m]) + rate(node_disk_writes_completed_total" + withLabel(selector, "device!~\"^(loop|ram).*\"") + "[1m]))";
    }
    private String mysqlConnections(String selector) { return "mysql_global_status_threads_connected" + selector; }
    private String cacheHitRate(String selector) {
        return "100 * (1 - rate(mysql_global_status_innodb_buffer_pool_reads" + selector + "[5m]) / clamp_min(rate(mysql_global_status_innodb_buffer_pool_read_requests" + selector + "[5m]), 1))";
    }
    private String withLabel(String selector, String label) { return selector.substring(0, selector.length() - 1) + "," + label + "}"; }

    private String nodeInstance() { return requireTarget(properties.getTargets().getNodeInstance(), "node_exporter"); }
    private String mysqlInstance() { return requireTarget(properties.getTargets().getMysqlInstance(), "mysqld_exporter"); }

    private String requireTarget(String value, String name) {
        if (!StringUtils.hasText(value)) throw new PrometheusException(name + " 目标实例尚未配置");
        return value;
    }
}
