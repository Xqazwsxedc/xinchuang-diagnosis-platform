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
import java.util.TreeMap;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Service;

/** 将五类 Prometheus 指标转换为数据库资源详情页的稳定接口契约。 */
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
        PrometheusRange range = rangeResolver.resolve(rangeCode, Instant.now());
        DbMetricsOverviewVO overview = new DbMetricsOverviewVO();
        overview.setCpuUsage(latest(queries.cpuUsage()));
        overview.setMemoryUsage(latest(queries.memoryUsage()));
        overview.setDiskIops(integer(latest(queries.diskIops())));
        overview.setCurrentConnections(integer(latest(queries.mysqlConnections())));
        overview.setCacheHitRate(latest(queries.cacheHitRate()));

        Map<Instant, DbMetricPointVO> points = new TreeMap<>();
        merge(points, client.rangeQuery(queries.cpuUsage(), range), DbMetricPointVO::setCpuUsage);
        merge(points, client.rangeQuery(queries.memoryUsage(), range), DbMetricPointVO::setMemoryUsage);
        mergeInteger(points, client.rangeQuery(queries.diskIops(), range), DbMetricPointVO::setDiskIops);
        mergeInteger(points, client.rangeQuery(queries.mysqlConnections(), range), DbMetricPointVO::setCurrentConnections);
        merge(points, client.rangeQuery(queries.cacheHitRate(), range), DbMetricPointVO::setCacheHitRate);
        overview.setPoints(List.copyOf(points.values()));
        return overview;
    }

    private BigDecimal latest(String query) {
        return client.instantQuery(query).stream().findFirst().map(point -> scale(point.value())).orElse(null);
    }

    private void merge(Map<Instant, DbMetricPointVO> target, List<PrometheusPoint> source,
                       BiConsumer<DbMetricPointVO, BigDecimal> setter) {
        for (PrometheusPoint point : source) {
            setter.accept(point(target, point.timestamp()), scale(point.value()));
        }
    }

    private void mergeInteger(Map<Instant, DbMetricPointVO> target, List<PrometheusPoint> source,
                              BiConsumer<DbMetricPointVO, Integer> setter) {
        for (PrometheusPoint point : source) {
            setter.accept(point(target, point.timestamp()), integer(point.value()));
        }
    }

    private DbMetricPointVO point(Map<Instant, DbMetricPointVO> target, Instant timestamp) {
        return target.computeIfAbsent(timestamp, key -> {
            DbMetricPointVO point = new DbMetricPointVO();
            point.setSampledAt(LocalDateTime.ofInstant(key, DISPLAY_ZONE));
            return point;
        });
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private Integer integer(BigDecimal value) {
        return value == null ? null : value.setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
