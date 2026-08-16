package com.xinchuang.diagnosis.ai.service;

import com.xinchuang.diagnosis.ai.dto.FaultTaskExecutionUpdateRequest;
import com.xinchuang.diagnosis.ai.dto.FaultTaskPageQuery;
import com.xinchuang.diagnosis.ai.dto.FaultTaskSaveRequest;
import com.xinchuang.diagnosis.ai.dto.TaskExecuteRequest;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.ai.vo.FaultTaskDetailVO;
import com.xinchuang.diagnosis.ai.vo.FaultTaskVO;
import com.xinchuang.diagnosis.common.api.PageResponse;

public interface FaultPredictionTaskService {
    PageResponse<FaultTaskVO> page(FaultTaskPageQuery query);
    FaultTaskDetailVO detail(Long id);
    FaultTaskDetailVO create(FaultTaskSaveRequest request);
    FaultTaskDetailVO update(Long id, FaultTaskSaveRequest request);
    void delete(Long id);
    TaskExecRecord execute(Long id, TaskExecuteRequest request);
    TaskExecRecord updateExecutionFromEngine(Long taskId, Long executionId, FaultTaskExecutionUpdateRequest request);
}
