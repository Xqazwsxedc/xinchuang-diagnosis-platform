package com.xinchuang.diagnosis.ai.engine;

import com.xinchuang.diagnosis.common.exception.BusinessException;
import java.util.List;
import java.util.Map;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BentoMlFaultPredictionEngine implements FaultPredictionEngine {
    private final RestClient client;

    public BentoMlFaultPredictionEngine(AiEngineProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        this.client = RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .requestFactory(requestFactory)
            .build();
    }

    @Override
    public TimeSeriesInferenceResponse predict(TimeSeriesInferenceRequest request) {
        validateShape(request);
        try {
            TimeSeriesInferenceResponse response = client.post()
                .uri("/predict")
                .body(request)
                .retrieve()
                .body(TimeSeriesInferenceResponse.class);
            if (response == null || !StringUtils.hasText(response.getRequestId())) {
                throw new BusinessException(503, "BentoML返回了无效响应");
            }
            if (!request.getRequestId().equals(response.getRequestId())) {
                throw new BusinessException(503, "BentoML响应与请求ID不一致");
            }
            return response;
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException(503, "BentoML时序模型服务不可用");
        }
    }

    private void validateShape(TimeSeriesInferenceRequest request) {
        int expectedSize = request.getTimestamps().size();
        for (Map.Entry<String, List<Double>> feature : request.getFeatures().entrySet()) {
            if (feature.getValue() == null || feature.getValue().size() != expectedSize) {
                throw new BusinessException("指标" + feature.getKey() + "的数据点数量与时间戳不一致");
            }
        }
    }
}
