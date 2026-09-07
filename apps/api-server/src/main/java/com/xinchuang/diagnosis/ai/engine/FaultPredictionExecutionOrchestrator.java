package com.xinchuang.diagnosis.ai.engine;

import com.xinchuang.diagnosis.ai.engine.FaultPredictionExecutionStateService.ExecutionContext;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 单次故障预测执行的异步编排器。
 *
 * <p>它把执行拆成“原子领取 -> 读取 Prometheus 特征 -> 调用模型 -> 写回结果”四步，
 * 具体状态更新由 {@link FaultPredictionExecutionStateService} 统一完成。</p>
 */
@Service
public class FaultPredictionExecutionOrchestrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FaultPredictionExecutionOrchestrator.class);
    private final FaultPredictionExecutionStateService stateService;
    private final PrometheusFaultPredictionInputProvider inputProvider;
    private final FaultPredictionEngine engine;

    public FaultPredictionExecutionOrchestrator(FaultPredictionExecutionStateService stateService,
                                                PrometheusFaultPredictionInputProvider inputProvider,
                                                FaultPredictionEngine engine) {
        this.stateService = stateService;
        this.inputProvider = inputProvider;
        this.engine = engine;
    }

    public void execute(Long executionId) {
        // claim 使用条件更新保证同一条 PENDING 记录只会被一个 Worker 处理。
        ExecutionContext context = stateService.claim(executionId);
        if (context == null) return;
        try {
            // 输入提供器负责资源校验、历史窗口查询和模型请求组装。
            FaultPredictionInput input = inputProvider.load(context.task(), context.record());
            // 模型服务不可用时仅重试可恢复的 503，最终失败仍由状态服务记录。
            TimeSeriesInferenceResponse response = predictWithRetry(input.request());
            stateService.complete(context, input, response);
        } catch (RuntimeException exception) {
            // 对外只保存可识别的失败原因，避免异步线程异常丢失执行状态。
            LOGGER.error("Fault prediction execution {} failed", executionId, exception);
            stateService.fail(executionId, exception.getMessage());
        }
    }

    private TimeSeriesInferenceResponse predictWithRetry(TimeSeriesInferenceRequest request) {
        BusinessException lastFailure = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return engine.predict(request);
            } catch (BusinessException exception) {
                lastFailure = exception;
                if (exception.getCode() != 503 || attempt == 3) throw exception;
                try {
                    // 递增短暂退避，降低模型服务瞬时故障时的集中重试压力。
                    Thread.sleep(300L * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(503, "时序模型调用被中断");
                }
            }
        }
        throw lastFailure == null ? new BusinessException(503, "时序模型调用失败") : lastFailure;
    }
}
