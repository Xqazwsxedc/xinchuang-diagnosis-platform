package com.xinchuang.diagnosis.monitoring.prometheus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 验证 Prometheus 原始 JSON 到安全数值点位的转换边界。 */
class PrometheusResponseParserTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PrometheusResponseParser parser = new PrometheusResponseParser();

    @Test
    /** matrix 中的正常样本应按时间和值完整保留。 */
    void parsesMatrixSamples() throws Exception {
        List<PrometheusPoint> points = parser.parse(objectMapper.readTree("""
            {"status":"success","data":{"result":[{"values":[[1723800000,"45.2"],[1723800060,"46.8"]]}]}}
            """), true);

        assertEquals(2, points.size());
        assertEquals("45.2", points.get(0).value().toPlainString());
        assertEquals(1723800060L, points.get(1).timestamp().getEpochSecond());
    }

    @Test
    /** vector 使用 value 字段，仍应转换为一个规范化点位。 */
    void parsesInstantVector() throws Exception {
        List<PrometheusPoint> points = parser.parse(objectMapper.readTree("""
            {"status":"success","data":{"result":[{"value":[1723800000,"12"]}]}}
            """), false);

        assertEquals(1, points.size());
        assertEquals("12", points.get(0).value().toPlainString());
    }

    @Test
    /** 无访问量时缓存命中率可能产生 NaN 或 Inf，必须过滤而非传给图表。 */
    void filtersInvalidPrometheusValues() throws Exception {
        List<PrometheusPoint> points = parser.parse(objectMapper.readTree("""
            {"status":"success","data":{"result":[{"values":[[1723800000,"NaN"],[1723800060,"+Inf"],[1723800120,"75.5"]]}]}}
            """), true);

        assertEquals(1, points.size());
        assertEquals("75.5", points.get(0).value().toPlainString());
    }

    @Test
    /** Prometheus 返回非 success 时不能伪装为空数据，应提示监控服务异常。 */
    void rejectsNonSuccessResponse() throws Exception {
        assertThrows(PrometheusException.class,
            () -> parser.parse(objectMapper.readTree("{\"status\":\"error\",\"data\":{}}"), false));
    }
}
