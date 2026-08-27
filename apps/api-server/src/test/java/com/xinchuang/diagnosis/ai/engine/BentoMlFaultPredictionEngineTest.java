package com.xinchuang.diagnosis.ai.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class BentoMlFaultPredictionEngineTest {
    @Test
    void sendsTimeSeriesAndReadsModelResponse() throws IOException {
        AtomicReference<String> receivedBody = new AtomicReference<>();
        HttpServer server = server(exchange -> {
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = ("{\"contractVersion\":\"1.0\",\"requestId\":\"exec-1\","
                + "\"engine\":\"BentoML\",\"modelCode\":\"zscore-communication-test\","
                + "\"modelVersion\":\"communication-test-1.0.0\",\"pointCount\":5,"
                + "\"anomalies\":[{\"metricCode\":\"cpu_usage\",\"index\":4,\"timestamp\":5,"
                + "\"value\":100.0,\"score\":2.0}]}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        try {
            TimeSeriesInferenceResponse response = engine(server).predict(request("exec-1"));

            assertEquals("BentoML", response.getEngine());
            assertEquals(5, response.getPointCount());
            assertEquals("cpu_usage", response.getAnomalies().get(0).getMetricCode());
            assertTrue(receivedBody.get().contains("\"features\""));
            assertTrue(receivedBody.get().contains("100.0"));
            assertTrue(!receivedBody.get().contains("\"request\""));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void rejectsMismatchedFeatureLengthBeforeCallingEngine() {
        TimeSeriesInferenceRequest request = request("exec-2");
        request.setFeatures(Map.of("cpu_usage", List.of(10.0, 20.0)));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> engine("http://127.0.0.1:1").predict(request));

        assertTrue(exception.getMessage().contains("数据点数量与时间戳不一致"));
    }

    private HttpServer server(com.sun.net.httpserver.HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/predict", handler);
        server.start();
        return server;
    }

    private BentoMlFaultPredictionEngine engine(HttpServer server) {
        return engine("http://127.0.0.1:" + server.getAddress().getPort());
    }

    private BentoMlFaultPredictionEngine engine(String baseUrl) {
        AiEngineProperties properties = new AiEngineProperties();
        properties.setBaseUrl(baseUrl);
        properties.setConnectTimeout(Duration.ofSeconds(1));
        properties.setReadTimeout(Duration.ofSeconds(1));
        return new BentoMlFaultPredictionEngine(properties);
    }

    private TimeSeriesInferenceRequest request(String requestId) {
        TimeSeriesInferenceRequest request = new TimeSeriesInferenceRequest();
        request.setRequestId(requestId);
        request.setTimestamps(List.of(1L, 2L, 3L, 4L, 5L));
        request.setFeatures(Map.of("cpu_usage", List.of(10.0, 10.0, 10.0, 10.0, 100.0)));
        return request;
    }
}
