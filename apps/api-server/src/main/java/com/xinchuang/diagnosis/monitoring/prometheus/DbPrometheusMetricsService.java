package com.xinchuang.diagnosis.monitoring.prometheus;

import com.xinchuang.diagnosis.ledger.vo.DbMetricPointVO;
import com.xinchuang.diagnosis.ledger.vo.DbMetricsOverviewVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Service;

/**
 * 将 Prometheus 的五类数据库/服务器指标转换为详情页稳定的接口契约。
 *
 * <p>该服务同时提供实时值和历史点位，并把不同指标按时间戳合并到同一个
 * {@link DbMetricPointVO} 中。Prometheus HTTP 和原始 JSON 细节由下层组件隔离。</p>
 */
@Service
public class DbPrometheusMetricsService {
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Shanghai");
    private final PrometheusClient client;
    private final PrometheusQueryFactory queries;
    private final PrometheusRangeResolver rangeResolver;

    public DbPrometheusMetricsService(PrometheusClient client,
                                      PrometheusQueryFactory queries,
                                      PrometheusRangeResolver rangeResolver) {
        this.client = client;
        this.queries = queries;
        this.rangeResolver = rangeResolver;
    }

    public DbMetricsOverviewVO load(String rangeCode) {
        // 页面快捷范围先解析成 PrometheusRange，再加载全部支持的指标。
        PrometheusRange range = rangeResolver.resolve(rangeCode, Instant.now());
        return load(range, Set.of("cpuUsage", "memoryUsage", "diskIops", "currentConnections", "cacheHitRate"));
    }

    public DbMetricsOverviewVO load(PrometheusRange range, Set<String> metricCodes) {
        // baseUrl 为 null 时使用系统级 Prometheus 配置，保留兼容调用方式。
        return loadInternal(range, metricCodes, null, null, null, null);
    }

    public DbMetricsOverviewVO load(PrometheusRange range, Set<String> metricCodes, String baseUrl, String authRef,
                                    Map<String, String> nodeSelector, Map<String, String> databaseSelector) {
        return loadInternal(range, metricCodes, baseUrl, authRef, nodeSelector, databaseSelector);
    }

    private DbMetricsOverviewVO loadInternal(PrometheusRange range, Set<String> metricCodes, String baseUrl, String authRef,
                                             Map<String, String> nodeSelector, Map<String, String> databaseSelector) {
        boolean resourceScoped = baseUrl != null;
        DbMetricsOverviewVO overview = new DbMetricsOverviewVO();
        // 服务器指标使用 node selector，数据库指标使用 database selector。
        String cpu = resourceScoped ? queries.cpuUsage(nodeSelector) : queries.cpuUsage();
        String memory = resourceScoped ? queries.memoryUsage(nodeSelector) : queries.memoryUsage();
        String disk = resourceScoped ? queries.diskIops(nodeSelector) : queries.diskIops();
        String connections = resourceScoped ? queries.mysqlConnections(databaseSelector) : queries.mysqlConnections();
        String cache = resourceScoped ? queries.cacheHitRate(databaseSelector) : queries.cacheHitRate();
        if (metricCodes.contains("cpuUsage")) overview.setCpuUsage(latest(baseUrl, authRef, cpu));
        if (metricCodes.contains("memoryUsage")) overview.setMemoryUsage(latest(baseUrl, authRef, memory));
        if (metricCodes.contains("diskIops")) overview.setDiskIops(integer(latest(baseUrl, authRef, disk)));
        if (metricCodes.contains("currentConnections")) overview.setCurrentConnections(integer(latest(baseUrl, authRef, connections)));
        if (metricCodes.contains("cacheHitRate")) overview.setCacheHitRate(latest(baseUrl, authRef, cache));

        // TreeMap 同时完成按时间排序和同一时间戳的多指标合并。
        Map<Instant, DbMetricPointVO> points = new TreeMap<>();
        if (metricCodes.contains("cpuUsage")) merge(points, range(baseUrl, authRef, cpu, range), DbMetricPointVO::setCpuUsage);
        if (metricCodes.contains("memoryUsage")) merge(points, range(baseUrl, authRef, memory, range), DbMetricPointVO::setMemoryUsage);
        if (metricCodes.contains("diskIops")) mergeInteger(points, range(baseUrl, authRef, disk, range), DbMetricPointVO::setDiskIops);
        if (metricCodes.contains("currentConnections")) mergeInteger(points, range(baseUrl, authRef, connections, range), DbMetricPointVO::setCurrentConnections);
        if (metricCodes.contains("cacheHitRate")) merge(points, range(baseUrl, authRef, cache, range), DbMetricPointVO::setCacheHitRate);
        overview.setPoints(List.copyOf(points.values()));
        return overview;
    }

    private BigDecimal latest(String baseUrl, String authRef, String query) {
        // 即时查询没有结果时返回 null，让前端显示空状态而不是伪造 0。
        return (baseUrl == null ? client.instantQuery(query) : client.instantQuery(baseUrl, authRef, query)).stream().findFirst().map(point -> scale(point.value())).orElse(null);
    }

    private List<PrometheusPoint> range(String baseUrl, String authRef, String query, PrometheusRange selectedRange) {
        // 区间查询返回多个样本，后续由 merge/mergeInteger 写入统一时间轴。
        return baseUrl == null ? client.rangeQuery(query, selectedRange) : client.rangeQuery(baseUrl, authRef, query, selectedRange);
    }

    private void merge(Map<Instant, DbMetricPointVO> target, List<PrometheusPoint> source,
                       BiConsumer<DbMetricPointVO, BigDecimal> setter) {
        // CPU、内存、缓存命中率保留两位小数。
        for (PrometheusPoint point : source) {
            setter.accept(point(target, point.timestamp()), scale(point.value()));
        }
    }

    private void mergeInteger(Map<Instant, DbMetricPointVO> target, List<PrometheusPoint> source,
                              BiConsumer<DbMetricPointVO, Integer> setter) {
        // IOPS 和连接数按页面契约转换为整数。
        for (PrometheusPoint point : source) {
            setter.accept(point(target, point.timestamp()), integer(point.value()));
        }
    }

    private DbMetricPointVO point(Map<Instant, DbMetricPointVO> target, Instant timestamp) {
        return target.computeIfAbsent(timestamp, key -> {
            DbMetricPointVO point = new DbMetricPointVO();
            // Prometheus 使用 UTC 时间戳，页面按业务时区 Asia/Shanghai 展示。
            point.setSampledAt(LocalDateTime.ofInstant(key, DISPLAY_ZONE));
            return point;
        });
    }

    private BigDecimal scale(BigDecimal value) {
        // 统一实时卡片和折线图的显示精度。
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private Integer integer(BigDecimal value) {
        // 保留“无数据”和数值 0 的语义差异。
        return value == null ? null : value.setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
