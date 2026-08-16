package com.xinchuang.diagnosis.monitoring.prometheus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import com.xinchuang.diagnosis.monitoring.config.MonitoringProperties;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/** Prometheus HTTP 客户端回归测试，防止标签选择器中的花括号被 Spring 当作 URI 模板变量。 */
class PrometheusHttpClientTest {

    @Test
    /** 含 instance 标签的 PromQL 必须被编码后正常发送到 Prometheus。 */
    void encodesPromQlLabelSelectorInsteadOfExpandingItAsUriTemplate() throws IOException {
        AtomicReference<String> rawQuery = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/v1/query", exchange -> {
            rawQuery.set(exchange.getRequestURI().getRawQuery());
            byte[] body = "{\"status\":\"success\",\"data\":{\"resultType\":\"vector\",\"result\":[{\"value\":[1723766400,\"42\"]}]}}"
                .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            MonitoringProperties properties = new MonitoringProperties();
            properties.getPrometheus().setBaseUrl("http://localhost:" + server.getAddress().getPort());
            PrometheusHttpClient client = new PrometheusHttpClient(properties, new PrometheusResponseParser());

            List<PrometheusPoint> points = client.instantQuery("mysql_global_status_threads_connected{instance=\"mysql:9104\"}");

            assertEquals(1, points.size());
            assertTrue(rawQuery.get().contains("%7B"));
            assertTrue(rawQuery.get().contains("%7D"));
        } finally {
            server.stop(0);
        }
    }
}
