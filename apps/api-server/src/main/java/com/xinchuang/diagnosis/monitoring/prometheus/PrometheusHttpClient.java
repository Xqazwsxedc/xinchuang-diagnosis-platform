package com.xinchuang.diagnosis.monitoring.prometheus;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinchuang.diagnosis.monitoring.config.MonitoringProperties;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Spring Boot 到 Prometheus HTTP API 的唯一网络出口。 */
@Component
public class PrometheusHttpClient implements PrometheusClient {
    private final MonitoringProperties properties;
    private final PrometheusResponseParser parser;

    public PrometheusHttpClient(MonitoringProperties properties, PrometheusResponseParser parser) {
        this.properties = properties;
        this.parser = parser;
    }

    @Override
    public List<PrometheusPoint> instantQuery(String promQl) {
        return request("/api/v1/query", promQl, null, false);
    }

    @Override
    public List<PrometheusPoint> rangeQuery(String promQl, PrometheusRange range) {
        return request("/api/v1/query_range", promQl, range, true);
    }

    private List<PrometheusPoint> request(String path, String promQl, PrometheusRange range, boolean isRange) {
        if (!StringUtils.hasText(properties.getPrometheus().getBaseUrl())) {
            throw new PrometheusException("监控服务地址尚未配置");
        }
        try {
            JsonNode response = client().get()
                // PromQL 的标签选择器含有花括号，不能交给 Spring URI 模板展开。
                .uri(buildRequestUri(path, promQl, range))
                .retrieve()
                .body(JsonNode.class);
            return parser.parse(response, isRange);
        } catch (PrometheusException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new PrometheusException("监控服务暂不可用，请稍后重试", exception);
        }
    }

    private URI buildRequestUri(String path, String promQl, PrometheusRange range) {
        String baseUrl = properties.getPrometheus().getBaseUrl().replaceFirst("/+$", "");
        StringBuilder uri = new StringBuilder(baseUrl).append(path)
            .append("?query=")
            .append(encode(promQl));
        if (range != null) {
            uri.append("&start=").append(range.start().getEpochSecond())
                .append("&end=").append(range.end().getEpochSecond())
                .append("&step=").append(range.stepSeconds());
        }
        return URI.create(uri.toString());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private RestClient client() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getPrometheus().getConnectTimeout());
        requestFactory.setReadTimeout(properties.getPrometheus().getReadTimeout());
        return RestClient.builder()
            .baseUrl(properties.getPrometheus().getBaseUrl())
            .requestFactory(requestFactory)
            .build();
    }
}
