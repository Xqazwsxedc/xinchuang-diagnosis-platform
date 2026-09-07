package com.xinchuang.diagnosis.ai.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.ai.mapper.TaskExecRecordMapper;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FaultPredictionPendingExecutionScheduler {
    private final TaskExecRecordMapper executionMapper;
    private final FaultPredictionExecutionDispatcher dispatcher;

    public FaultPredictionPendingExecutionScheduler(TaskExecRecordMapper executionMapper,
                                                     FaultPredictionExecutionDispatcher dispatcher) {
        this.executionMapper = executionMapper;
        this.dispatcher = dispatcher;
    }

    @Scheduled(initialDelayString = "${ai.engine.pending-scan-initial-delay-ms:10000}",
        fixedDelayString = "${ai.engine.pending-scan-delay-ms:30000}")
    public void dispatchPending() {
        executionMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<TaskExecRecord>()
            .eq(TaskExecRecord::getTaskType, "FAULT_PREDICTION")
            .eq(TaskExecRecord::getExecutionStatus, "RUNNING")
            .isNotNull(TaskExecRecord::getInputStartTime)
            .lt(TaskExecRecord::getUpdatedAt, LocalDateTime.now().minusMinutes(5))
            .set(TaskExecRecord::getExecutionStatus, "PENDING")
            .set(TaskExecRecord::getErrorMessage, "检测进程中断，任务已重新排队"));
        executionMapper.selectPage(new Page<>(1, 20), new LambdaQueryWrapper<TaskExecRecord>()
                .eq(TaskExecRecord::getTaskType, "FAULT_PREDICTION")
                .eq(TaskExecRecord::getExecutionStatus, "PENDING")
                .isNotNull(TaskExecRecord::getInputStartTime)
                .orderByAsc(TaskExecRecord::getStartTime))
            .getRecords().forEach(record -> dispatcher.submit(record.getId()));
    }
}
