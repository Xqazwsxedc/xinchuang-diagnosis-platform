package com.xinchuang.diagnosis.ai.engine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xinchuang.diagnosis.ai.engine.FaultPredictionExecutionStateService.ExecutionContext;
import com.xinchuang.diagnosis.ai.entity.AiTask;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FaultPredictionExecutionOrchestratorTest {
    @Mock private FaultPredictionExecutionStateService stateService;
    @Mock private PrometheusFaultPredictionInputProvider inputProvider;
    @Mock private FaultPredictionEngine engine;
    private FaultPredictionExecutionOrchestrator orchestrator;

    @BeforeEach
    void setUp() { orchestrator = new FaultPredictionExecutionOrchestrator(stateService, inputProvider, engine); }

    @Test
    void retriesTemporaryEngineFailureAndCompletesExecution() {
        ExecutionContext context = context();
        FaultPredictionInput input = input();
        TimeSeriesInferenceResponse response = response();
        when(stateService.claim(2L)).thenReturn(context);
        when(inputProvider.load(context.task(), context.record())).thenReturn(input);
        when(engine.predict(input.request()))
            .thenThrow(new BusinessException(503, "temporary"))
            .thenThrow(new BusinessException(503, "temporary"))
            .thenReturn(response);

        orchestrator.execute(2L);

        verify(engine, times(3)).predict(input.request());
        verify(stateService).complete(context, input, response);
    }

    @Test
    void recordsPermanentInputFailureWithoutCallingEngine() {
        ExecutionContext context = context();
        when(stateService.claim(2L)).thenReturn(context);
        when(inputProvider.load(context.task(), context.record())).thenThrow(new BusinessException("没有指标数据"));

        orchestrator.execute(2L);

        verify(stateService).fail(2L, "没有指标数据");
        verify(engine, times(0)).predict(any());
    }

    private ExecutionContext context() {
        AiTask task = new AiTask(); task.setId(1L);
        TaskExecRecord record = new TaskExecRecord(); record.setId(2L);
        return new ExecutionContext(task, record);
    }

    private FaultPredictionInput input() {
        TimeSeriesInferenceRequest request = new TimeSeriesInferenceRequest(); request.setRequestId("execution-2");
        request.setTimestamps(List.of(1L, 2L, 3L)); request.setFeatures(Map.of("cpu_usage", List.of(1.0, 1.0, 10.0)));
        return new FaultPredictionInput(request, LocalDateTime.now().minusHours(1), LocalDateTime.now());
    }

    private TimeSeriesInferenceResponse response() {
        TimeSeriesInferenceResponse response = new TimeSeriesInferenceResponse(); response.setRequestId("execution-2");
        response.setContractVersion("1.0"); response.setPointCount(3); response.setAnomalies(List.of());
        return response;
    }
}
