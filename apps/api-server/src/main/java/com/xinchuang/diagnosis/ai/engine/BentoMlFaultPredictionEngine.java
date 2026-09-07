package com.xinchuang.diagnosis.ai.engine;

import com.xinchuang.diagnosis.common.exception.BusinessException;
import java.util.List;
import java.util.Map;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * BentoML 时序模型的 HTTP 适配器。
 *
 * <p>本类只负责发送推理请求和校验响应契约，不参与 Prometheus 数据采集或结果持久化。
 * 这样后续替换模型服务时可以保持平台任务接口不变。</p>
 */
@Component
public class BentoMlFaultPredictionEngine implements FaultPredictionEngine {
    private final RestClient client;

    public BentoMlFaultPredictionEngine(AiEngineProperties properties) {
        // 超时从配置注入，避免模型服务不可用时长期占用执行线程。
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        this.client = RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .requestFactory(requestFactory)
            .build();
    }

    /** 调用 BentoML /predict，并校验请求 ID、契约版本和点数。 */
    @Override
    public TimeSeriesInferenceResponse predict(TimeSeriesInferenceRequest request) {
        validateShape(request);
        try {
            // Java DTO 与 Python Pydantic 模型共享同一时序请求契约。
            TimeSeriesInferenceResponse response = client.post()
                .uri("/predict")
                .body(request)
                .retrieve()
                .body(TimeSeriesInferenceResponse.class);
            if (response == null || !StringUtils.hasText(response.getRequestId())) {
                throw new BusinessException(503, "BentoML返回了无效响应");
            }
            // 请求 ID 不一致通常意味着响应串单，不能写回当前执行记录。
            if (!request.getRequestId().equals(response.getRequestId())) {
                throw new BusinessException(503, "BentoML响应与请求ID不一致");
            }
            if (!request.getContractVersion().equals(response.getContractVersion())
                    || response.getPointCount() == null
                    || response.getPointCount() != request.getTimestamps().size()
                    || response.getAnomalies() == null) {
                // 点数和契约版本是最基本的响应完整性检查。
                throw new BusinessException(503, "BentoML响应契约不匹配");
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
        // 每个指标序列必须与时间戳长度一致，防止模型错位计算。
        for (Map.Entry<String, List<Double>> feature : request.getFeatures().entrySet()) {
            if (feature.getValue() == null || feature.getValue().size() != expectedSize) {
                throw new BusinessException("指标" + feature.getKey() + "的数据点数量与时间戳不一致");
            }
        }
    }
}
