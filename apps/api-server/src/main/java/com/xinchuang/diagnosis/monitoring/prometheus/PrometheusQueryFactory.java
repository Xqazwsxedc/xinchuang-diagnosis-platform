package com.xinchuang.diagnosis.monitoring.prometheus;

import com.xinchuang.diagnosis.monitoring.config.MonitoringProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.Map;

/**
 * 集中维护数据库详情页使用的 PromQL。
 *
 * <p>无参数方法读取系统级目标；带 selector 的方法用于资源级监控配置。
 * Controller 和业务服务不直接拼接 PromQL，避免查询语句散落并降低注入风险。</p>
 */
@Component
public class PrometheusQueryFactory {
    private final MonitoringProperties properties;

    public PrometheusQueryFactory(MonitoringProperties properties) { this.properties = properties; }

    /** 计算 node_exporter 暴露的 CPU 非 idle 百分比。 */
    public String cpuUsage() {
        return "100 * (1 - avg by(instance) (rate(node_cpu_seconds_total{instance=\"%s\",mode=\"idle\"}[5m])))"
            .formatted(nodeInstance());
    }

    /** 根据可用内存和总内存计算内存使用率。 */
    public String memoryUsage() {
        return "100 * (1 - node_memory_MemAvailable_bytes{instance=\"%s\"} / node_memory_MemTotal_bytes{instance=\"%s\"})"
            .formatted(nodeInstance(), nodeInstance());
    }

    /** 汇总过滤掉 loop/ram 设备后的读写 IOPS。 */
    public String diskIops() {
        return "sum by(instance) (rate(node_disk_reads_completed_total{instance=\"%s\",device!~\"^(loop|ram).*\"}[1m]) + rate(node_disk_writes_completed_total{instance=\"%s\",device!~\"^(loop|ram).*\"}[1m]))"
            .formatted(nodeInstance(), nodeInstance());
    }

    /** 查询 mysqld_exporter 暴露的当前连接数。 */
    public String mysqlConnections() {
        return "mysql_global_status_threads_connected{instance=\"%s\"}".formatted(mysqlInstance());
    }

    /** 计算 InnoDB 缓存命中率，并用 clamp_min 避免除零。 */
    public String cacheHitRate() {
        return "100 * (1 - rate(mysql_global_status_innodb_buffer_pool_reads{instance=\"%s\"}[5m]) / clamp_min(rate(mysql_global_status_innodb_buffer_pool_read_requests{instance=\"%s\"}[5m]), 1))"
            .formatted(mysqlInstance(), mysqlInstance());
    }

    /** 使用资源级 node selector 生成服务器指标查询。 */
    public String cpuUsage(Map<String, String> selector) { return cpuUsage(PrometheusSelector.format(selector, "nodeTargetSelector")); }
    public String memoryUsage(Map<String, String> selector) { return memoryUsage(PrometheusSelector.format(selector, "nodeTargetSelector")); }
    public String diskIops(Map<String, String> selector) { return diskIops(PrometheusSelector.format(selector, "nodeTargetSelector")); }
    public String mysqlConnections(Map<String, String> selector) { return mysqlConnections(PrometheusSelector.format(selector, "databaseTargetSelector")); }
    public String cacheHitRate(Map<String, String> selector) { return cacheHitRate(PrometheusSelector.format(selector, "databaseTargetSelector")); }

    // 下面的重载只负责把已校验的 selector 注入对应指标表达式。
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
    /** 在已有 selector 中追加 mode/device 等指标专属标签。 */
    private String withLabel(String selector, String label) { return selector.substring(0, selector.length() - 1) + "," + label + "}"; }

    /** 读取系统级 node_exporter 目标，未配置时尽早失败。 */
    private String nodeInstance() { return requireTarget(properties.getTargets().getNodeInstance(), "node_exporter"); }
    /** 读取系统级 mysqld_exporter 目标，未配置时尽早失败。 */
    private String mysqlInstance() { return requireTarget(properties.getTargets().getMysqlInstance(), "mysqld_exporter"); }

    /** 防止生成一个没有目标实例的查询。 */
    private String requireTarget(String value, String name) {
        if (!StringUtils.hasText(value)) throw new PrometheusException(name + " 目标实例尚未配置");
        return value;
    }
}
