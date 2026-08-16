package com.xinchuang.diagnosis.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinchuang.diagnosis.ai.dto.AiPageQuery;
import com.xinchuang.diagnosis.ai.dto.FaultTaskExecutionUpdateRequest;
import com.xinchuang.diagnosis.ai.dto.FaultTaskPageQuery;
import com.xinchuang.diagnosis.ai.dto.FaultTaskSaveRequest;
import com.xinchuang.diagnosis.ai.dto.TaskExecuteRequest;
import com.xinchuang.diagnosis.ai.entity.AiTask;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.ai.mapper.TaskExecRecordMapper;
import com.xinchuang.diagnosis.ai.service.AiManagementService;
import com.xinchuang.diagnosis.ai.service.FaultPredictionTaskService;
import com.xinchuang.diagnosis.ai.vo.FaultTaskDetailVO;
import com.xinchuang.diagnosis.ai.vo.FaultTaskVO;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FaultPredictionTaskServiceImpl implements FaultPredictionTaskService {
    private static final String TASK_TYPE = "FAULT_PREDICTION";
    private final AiManagementService aiManagementService;
    private final TaskExecRecordMapper taskExecRecordMapper;
    private final ObjectMapper objectMapper;

    public FaultPredictionTaskServiceImpl(AiManagementService aiManagementService,
                                          TaskExecRecordMapper taskExecRecordMapper, ObjectMapper objectMapper) {
        this.aiManagementService = aiManagementService;
        this.taskExecRecordMapper = taskExecRecordMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResponse<FaultTaskVO> page(FaultTaskPageQuery query) {
        AiPageQuery pageQuery = new AiPageQuery();
        pageQuery.setPageNo(query.getPageNo()); pageQuery.setPageSize(query.getPageSize());
        pageQuery.setKeyword(query.getKeyword()); pageQuery.setModelId(query.getModelId());
        pageQuery.setCreatorName(query.getCreatorName()); pageQuery.setCreatedDate(query.getCreatedDate()); pageQuery.setTaskType(TASK_TYPE);
        PageResponse<AiTask> page = aiManagementService.pageTasks(pageQuery);
        return PageResponse.of(page.getRecords().stream().map(FaultTaskVO::from).toList(), page.getTotal(), page.getPageNo(), page.getPageSize());
    }

    @Override
    public FaultTaskDetailVO detail(Long id) {
        AiTask task = faultTask(id);
        Page<TaskExecRecord> page = taskExecRecordMapper.selectPage(new Page<>(1, 10), new LambdaQueryWrapper<TaskExecRecord>()
            .eq(TaskExecRecord::getTaskId, id).orderByDesc(TaskExecRecord::getStartTime));
        return FaultTaskDetailVO.from(task, page.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FaultTaskDetailVO create(FaultTaskSaveRequest request) {
        AiTask created = aiManagementService.createTask(toEntity(request));
        return detail(created.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FaultTaskDetailVO update(Long id, FaultTaskSaveRequest request) {
        AiTask existing = faultTask(id);
        AiTask updated = toEntity(request);
        updated.setTaskCode(existing.getTaskCode());
        aiManagementService.updateTask(id, updated);
        return detail(id);
    }

    @Override
    public void delete(Long id) {
        faultTask(id);
        aiManagementService.deleteTask(id);
    }

    @Override
    public TaskExecRecord execute(Long id, TaskExecuteRequest request) {
        faultTask(id);
        return aiManagementService.executeTask(id, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskExecRecord updateExecutionFromEngine(Long taskId, Long executionId, FaultTaskExecutionUpdateRequest request) {
        faultTask(taskId);
        TaskExecRecord record = taskExecRecordMapper.selectById(executionId);
        if (record == null || !taskId.equals(record.getTaskId()) || !TASK_TYPE.equals(record.getTaskType())) {
            throw new BusinessException("故障预测执行记录不存在");
        }
        if ("SUCCESS".equals(request.getExecutionStatus()) || "FAILED".equals(request.getExecutionStatus())) {
            if (request.getEndTime() == null) { throw new BusinessException("已完成执行必须提供结束时间"); }
        }
        record.setExecutionStatus(request.getExecutionStatus()); record.setStartTime(request.getStartTime());
        record.setEndTime(request.getEndTime()); record.setResultCount(request.getResultCount()); record.setErrorMessage(request.getErrorMessage());
        taskExecRecordMapper.updateById(record);
        return record;
    }

    private AiTask faultTask(Long id) {
        AiTask task = aiManagementService.taskDetail(id);
        if (!TASK_TYPE.equals(task.getTaskType())) { throw new BusinessException("任务不是故障预测任务"); }
        return task;
    }

    private AiTask toEntity(FaultTaskSaveRequest request) {
        try {
            AiTask task = new AiTask();
            task.setTaskName(request.getTaskName()); task.setTaskType(TASK_TYPE); task.setModelId(request.getModelId());
            task.setTargetType(request.getTargetType()); task.setTargetId(request.getTargetId());
            task.setMetricCodes(String.join(",", request.getMetricCodes())); task.setScheduleRule(objectMapper.writeValueAsString(request.getSchedules()));
            task.setParamJson("{\"engineStatus\":\"WAITING_ENGINE\",\"contractVersion\":\"1.0\"}"); task.setStatus("ENABLED");
            return task;
        } catch (JsonProcessingException ex) { throw new BusinessException("定时预测规则格式不合法"); }
    }
}
