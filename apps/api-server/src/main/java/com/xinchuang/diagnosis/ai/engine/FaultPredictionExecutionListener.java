package com.xinchuang.diagnosis.ai.engine;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class FaultPredictionExecutionListener {
    private final FaultPredictionExecutionDispatcher dispatcher;

    public FaultPredictionExecutionListener(FaultPredictionExecutionDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onRequested(FaultPredictionExecutionRequestedEvent event) {
        dispatcher.submit(event.executionId());
    }
}
