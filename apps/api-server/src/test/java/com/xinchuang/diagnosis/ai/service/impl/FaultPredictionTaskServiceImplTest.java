package com.xinchuang.diagnosis.ai.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.ai.dto.FaultTaskExecutionUpdateRequest;
import com.xinchuang.diagnosis.ai.dto.FaultTaskSaveRequest;
import com.xinchuang.diagnosis.ai.entity.AiTask;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.ai.mapper.TaskExecRecordMapper;
import com.xinchuang.diagnosis.ai.service.AiManagementService;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
/**
 * 测试故障预测任务专用服务，重点验证它与未来 AI 引擎之间的边界。
 * 确保面向页面的请求不能绕过任务类型、执行状态和任务编码约束。
 */
class FaultPredictionTaskServiceImplTest {
    @Mock private AiManagementService aiManagementService;
    @Mock private TaskExecRecordMapper taskExecRecordMapper;
    private FaultPredictionTaskServiceImpl service;

    @BeforeEach
    void setUp() { service = new FaultPredictionTaskServiceImpl(aiManagementService, taskExecRecordMapper, new ObjectMapper()); }

    @Test
    /** AI 引擎回调不能修改其他任务所属的执行记录。 */
    void engineUpdateRejectsRecordOutsideFaultTask() {
        when(aiManagementService.taskDetail(1L)).thenReturn(task("FAULT_PREDICTION"));
        TaskExecRecord record = new TaskExecRecord(); record.setId(2L); record.setTaskId(9L); record.setTaskType("FAULT_PREDICTION");
        when(taskExecRecordMapper.selectById(2L)).thenReturn(record);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.updateExecutionFromEngine(1L, 2L, update("RUNNING")));

        assertEquals("故障预测执行记录不存在", exception.getMessage());
    }

    @Test
    /** AI 引擎完成回调必须同时保存状态、结果数量和完成信息。 */
    void engineUpdateCompletesPendingRecordWithResult() {
        when(aiManagementService.taskDetail(1L)).thenReturn(task("FAULT_PREDICTION"));
        TaskExecRecord record = new TaskExecRecord(); record.setId(2L); record.setTaskId(1L); record.setTaskType("FAULT_PREDICTION"); record.setExecutionStatus("PENDING");
        when(taskExecRecordMapper.selectById(2L)).thenReturn(record);
        FaultTaskExecutionUpdateRequest request = update("SUCCESS"); request.setEndTime(LocalDateTime.now()); request.setResultCount(3);

        TaskExecRecord result = service.updateExecutionFromEngine(1L, 2L, request);

        assertEquals("SUCCESS", result.getExecutionStatus()); assertEquals(3, result.getResultCount()); verify(taskExecRecordMapper).updateById(record);
    }

    @Test
    /** 缺少结束时间会产生审计不完整的执行记录，必须拒绝。 */
    void engineCompletionRequiresEndTimeToAvoidFalseCompletion() {
        when(aiManagementService.taskDetail(1L)).thenReturn(task("FAULT_PREDICTION"));
        TaskExecRecord record = new TaskExecRecord(); record.setId(2L); record.setTaskId(1L); record.setTaskType("FAULT_PREDICTION");
        when(taskExecRecordMapper.selectById(2L)).thenReturn(record);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.updateExecutionFromEngine(1L, 2L, update("SUCCESS")));

        assertEquals("已完成执行必须提供结束时间", exception.getMessage());
        verify(taskExecRecordMapper, never()).updateById(record);
    }

    @Test
    /** 专用服务必须先拒绝其他 AI 模块的任务，不能继续查询执行记录。 */
    void detailRejectsTaskOutsideFaultPredictionBoundary() {
        when(aiManagementService.taskDetail(1L)).thenReturn(task("DEFECT_DETECTION"));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.detail(1L));

        assertEquals("任务不是故障预测任务", exception.getMessage());
        verify(taskExecRecordMapper, never()).selectPage(any(Page.class), any());
    }

    @Test
    /** 通过公开 DTO 编辑任务时，必须保留数据库中已有的内部任务编码。 */
    void updatePreservesExistingTaskCodeForDedicatedRequest() {
        AiTask existing = task("FAULT_PREDICTION");
        existing.setTaskCode("FAULT-TASK-EXISTING");
        when(aiManagementService.taskDetail(1L)).thenReturn(existing);
        when(taskExecRecordMapper.selectPage(any(Page.class), any())).thenReturn(new Page<>());

        service.update(1L, saveRequest());

        verify(aiManagementService).updateTask(eq(1L), argThat(updated -> "FAULT-TASK-EXISTING".equals(updated.getTaskCode())));
    }

    private AiTask task(String type) { AiTask task = new AiTask(); task.setId(1L); task.setTaskType(type); return task; }
    private FaultTaskExecutionUpdateRequest update(String status) { FaultTaskExecutionUpdateRequest request = new FaultTaskExecutionUpdateRequest(); request.setExecutionStatus(status); request.setStartTime(LocalDateTime.now()); return request; }
    private FaultTaskSaveRequest saveRequest() {
        FaultTaskSaveRequest request = new FaultTaskSaveRequest();
        request.setTaskName("编辑后的任务"); request.setModelId(1L); request.setTargetType("BUSINESS_SYSTEM"); request.setTargetId(1L); request.setMetricCodes(List.of("CPU"));
        FaultTaskSaveRequest.FaultTaskScheduleRule schedule = new FaultTaskSaveRequest.FaultTaskScheduleRule();
        schedule.setTime("08:00"); schedule.setRepeatType("daily"); schedule.setWeekDays(List.of()); schedule.setPredictionWindowHours(4); request.setSchedules(List.of(schedule));
        return request;
    }
}
