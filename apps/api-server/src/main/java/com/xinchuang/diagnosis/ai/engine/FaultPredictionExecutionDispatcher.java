package com.xinchuang.diagnosis.ai.engine;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class FaultPredictionExecutionDispatcher {
    private final FaultPredictionExecutionOrchestrator orchestrator;

    public FaultPredictionExecutionDispatcher(FaultPredictionExecutionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Async("faultPredictionExecutor")
    public void submit(Long executionId) {
        orchestrator.execute(executionId);
    }
}
