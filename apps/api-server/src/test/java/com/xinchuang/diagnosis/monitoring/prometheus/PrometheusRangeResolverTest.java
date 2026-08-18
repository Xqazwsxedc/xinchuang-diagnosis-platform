package com.xinchuang.diagnosis.monitoring.prometheus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.xinchuang.diagnosis.common.exception.BusinessException;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** 验证页面固定时间范围与 Prometheus start/end/step 的映射稳定。 */
class PrometheusRangeResolverTest {
    private final PrometheusRangeResolver resolver = new PrometheusRangeResolver();
    private final Instant end = Instant.parse("2026-08-17T00:00:00Z");

    @Test
    /** 1 小时范围使用一分钟步长，保证短期趋势足够细。 */
    void resolvesOneHourRange() {
        PrometheusRange range = resolver.resolve("1H", end);

        assertEquals(Instant.parse("2026-08-16T23:00:00Z"), range.start());
        assertEquals(60, range.stepSeconds());
    }

    @Test
    /** 14 天范围提高步长，避免向 Prometheus 请求过密点位。 */
    void resolvesFourteenDayRange() {
        PrometheusRange range = resolver.resolve("14D", end);

        assertEquals(Instant.parse("2026-08-03T00:00:00Z"), range.start());
        assertEquals(7200, range.stepSeconds());
    }

    @Test
    /** 非页面允许范围必须在请求 Prometheus 前拒绝。 */
    void rejectsUnknownRange() {
        assertThrows(BusinessException.class, () -> resolver.resolve("2H", end));
    }

    @Test
    /** 日期下载按上海时区覆盖结束日，且最多允许 90 天范围。 */
    void resolvesInclusiveDateRange() {
        PrometheusRange range = resolver.resolve(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 7));

        assertEquals(Instant.parse("2026-07-31T16:00:00Z"), range.start());
        assertEquals(Instant.parse("2026-08-07T16:00:00Z"), range.end());
    }

    @Test
    /** 日期缺失、倒序或超出窗口必须被后端拒绝。 */
    void rejectsInvalidDateRange() {
        assertThrows(BusinessException.class, () -> resolver.resolve(null, LocalDate.of(2026, 8, 7)));
        assertThrows(BusinessException.class, () -> resolver.resolve(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 8, 7)));
        assertThrows(BusinessException.class, () -> resolver.resolve(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 2)));
    }
}
