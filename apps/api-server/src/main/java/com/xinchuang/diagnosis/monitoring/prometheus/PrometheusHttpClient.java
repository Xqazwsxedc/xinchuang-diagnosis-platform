package com.xinchuang.diagnosis.monitoring.prometheus;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinchuang.diagnosis.monitoring.config.MonitoringProperties;
import java.net.URI;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Spring Boot 到 Prometheus HTTP API 的唯一网络出口。
 *
 * <p>上层只处理规范化的 {@link PrometheusPoint}，本类负责构造 query/query_range
 * 请求、编码 PromQL、设置超时和可选认证，并把网络错误转换成统一的
 * {@link PrometheusException}。</p>
 */
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
        // 系统级配置模式：调用 Prometheus 即时查询接口。
        return request("/api/v1/query", promQl, null, false);
    }

    @Override
    public List<PrometheusPoint> rangeQuery(String promQl, PrometheusRange range) {
        // 系统级配置模式：调用 Prometheus 区间查询接口。
        return request("/api/v1/query_range", promQl, range, true);
    }

    @Override
    public List<PrometheusPoint> instantQuery(String baseUrl, String authRef, String promQl) {
        // 资源级模式使用资源保存的 Prometheus 地址和认证引用。
        return request(baseUrl, authRef, "/api/v1/query", promQl, null, false);
    }

    @Override
    public List<PrometheusPoint> rangeQuery(String baseUrl, String authRef, String promQl, PrometheusRange range) {
        // 资源级区间查询与系统级查询共用解析和错误处理逻辑。
        return request(baseUrl, authRef, "/api/v1/query_range", promQl, range, true);
    }

    private List<PrometheusPoint> request(String path, String promQl, PrometheusRange range, boolean isRange) {
        if (!StringUtils.hasText(properties.getPrometheus().getBaseUrl())) {
            throw new PrometheusException("监控服务地址尚未配置");
        }
        try {
            // PromQL 通过 query 参数传递；返回值交给 parser 校验 status 和样本格式。
            JsonNode response = client(properties.getPrometheus().getBaseUrl(), null).get()
                // PromQL 的标签选择器含有花括号，不能交给 Spring URI 模板展开。
                .uri(buildRequestUri(properties.getPrometheus().getBaseUrl(), path, promQl, range))
                .retrieve()
                .body(JsonNode.class);
            return parser.parse(response, isRange);
        } catch (PrometheusException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new PrometheusException("监控服务暂不可用，请稍后重试", exception);
        }
    }

    private List<PrometheusPoint> request(String baseUrl, String authRef, String path, String promQl, PrometheusRange range, boolean isRange) {
        try {
            // 资源级请求和系统级请求保持同一响应契约，只有地址和认证来源不同。
            JsonNode response = client(baseUrl, authRef).get()
                .uri(buildRequestUri(baseUrl, path, promQl, range))
                .retrieve()
                .body(JsonNode.class);
            return parser.parse(response, isRange);
        } catch (PrometheusException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new PrometheusException("监控服务暂不可用，请稍后重试", exception);
        }
    }

    private URI buildRequestUri(String baseUrl, String path, String promQl, PrometheusRange range) {
        String normalizedBaseUrl = baseUrl.replaceFirst("/+$", "");
        StringBuilder uri = new StringBuilder(normalizedBaseUrl).append(path)
            .append("?query=")
            .append(encode(promQl));
        if (range != null) {
            // Prometheus 接收 Unix 秒级时间戳和 step 秒数。
            uri.append("&start=").append(range.start().getEpochSecond())
                .append("&end=").append(range.end().getEpochSecond())
                .append("&step=").append(range.stepSeconds());
        }
        return URI.create(uri.toString());
    }

    private String encode(String value) {
        // 空格编码为 %20，避免 application/x-www-form-urlencoded 的 '+' 被 PromQL 误读。
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private RestClient client(String baseUrl, String authRef) {
        // 关闭重定向，避免监控地址被重定向到未预期的目标。
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setInstanceFollowRedirects(false);
            }
        };
        requestFactory.setConnectTimeout(properties.getPrometheus().getConnectTimeout());
        requestFactory.setReadTimeout(properties.getPrometheus().getReadTimeout());
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory);
        if (StringUtils.hasText(authRef)) {
            // 业务数据只保存认证引用，真实 Authorization 值从服务端配置读取。
            String authorization = properties.getPrometheus().getAuthRefs().get(authRef);
            if (!StringUtils.hasText(authorization)) throw new PrometheusException("Prometheus 凭据引用未配置");
            builder.defaultHeader("Authorization", authorization);
        }
        return builder.build();
    }
}
