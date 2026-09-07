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
import com.xinchuang.diagnosis.ai.engine.FaultPredictionExecutionRequestedEvent;
import com.xinchuang.diagnosis.ai.engine.FaultPredictionMetricRegistry;
import com.xinchuang.diagnosis.ai.entity.AiTask;
import com.xinchuang.diagnosis.ai.entity.FaultReport;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.ai.mapper.FaultReportMapper;
import com.xinchuang.diagnosis.ai.mapper.TaskExecRecordMapper;
import com.xinchuang.diagnosis.ai.service.AiManagementService;
import com.xinchuang.diagnosis.ai.service.FaultPredictionTaskService;
import com.xinchuang.diagnosis.ai.vo.FaultTaskDetailVO;
import com.xinchuang.diagnosis.ai.vo.FaultTaskVO;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 故障预测任务的专用业务服务。
 *
 * <p>负责把通用 AI 任务能力限制在 {@code FAULT_PREDICTION} 类型，并在立即执行时
 * 记录历史输入窗口、发布执行请求事件。模型调用本身不在事务请求线程中完成。</p>
 */
@Service
public class FaultPredictionTaskServiceImpl implements FaultPredictionTaskService {
    private static final String TASK_TYPE = "FAULT_PREDICTION";
    private final AiManagementService aiManagementService;
    private final TaskExecRecordMapper taskExecRecordMapper;
    private final FaultReportMapper faultReportMapper;
    private final ObjectMapper objectMapper;
    private final FaultPredictionMetricRegistry metricRegistry;
    private final ApplicationEventPublisher eventPublisher;

    public FaultPredictionTaskServiceImpl(AiManagementService aiManagementService,
                                          TaskExecRecordMapper taskExecRecordMapper, FaultReportMapper faultReportMapper,
                                          ObjectMapper objectMapper, FaultPredictionMetricRegistry metricRegistry,
                                          ApplicationEventPublisher eventPublisher) {
        this.aiManagementService = aiManagementService;
        this.taskExecRecordMapper = taskExecRecordMapper;
        this.faultReportMapper = faultReportMapper;
        this.objectMapper = objectMapper;
        this.metricRegistry = metricRegistry;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PageResponse<FaultTaskVO> page(FaultTaskPageQuery query) {
        // 复用通用任务分页能力，同时固定任务类型，避免混入其他 AI 模块。
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
        // 详情页同时需要最近执行记录和该任务生成的预警。
        Page<TaskExecRecord> page = taskExecRecordMapper.selectPage(new Page<>(1, 10), new LambdaQueryWrapper<TaskExecRecord>()
            .eq(TaskExecRecord::getTaskId, id).orderByDesc(TaskExecRecord::getStartTime));
        List<FaultReport> faultReports = faultReportMapper.selectList(new LambdaQueryWrapper<FaultReport>()
            .eq(FaultReport::getTaskId, id).orderByDesc(FaultReport::getAlertTime));
        return FaultTaskDetailVO.from(task, page.getRecords(), faultReports);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FaultTaskDetailVO create(FaultTaskSaveRequest request) {
        // toEntity 会固化任务类型、指标字符串、调度规则和引擎契约版本。
        AiTask created = aiManagementService.createTask(toEntity(request));
        return detail(created.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FaultTaskDetailVO update(Long id, FaultTaskSaveRequest request) {
        AiTask existing = faultTask(id);
        AiTask updated = toEntity(request);
        // 任务编码是服务端标识，编辑时沿用旧值，防止客户端篡改或遗漏。
        updated.setTaskCode(existing.getTaskCode());
        aiManagementService.updateTask(id, updated);
        return detail(id);
    }

    @Override
    public void delete(Long id) {
        // 先确认任务属于故障预测类型，再交给通用服务执行删除。
        faultTask(id);
        aiManagementService.deleteTask(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskExecRecord execute(Long id, TaskExecuteRequest request) {
        AiTask task = faultTask(id);
        validateEngineScope(task.getTargetType(), task.getMetricCodes());
        // 通用服务只负责受理并创建执行记录，故障预测任务应先处于 PENDING。
        TaskExecRecord record = aiManagementService.executeTask(id, request);
        LocalDateTime inputEndTime = LocalDateTime.now();
        record.setInputStartTime(inputEndTime.minusHours(1));
        record.setInputEndTime(inputEndTime);
        taskExecRecordMapper.updateById(record);
        // 发布事件后由监听器/调度器异步领取，避免 HTTP 请求等待模型返回。
        eventPublisher.publishEvent(new FaultPredictionExecutionRequestedEvent(id, record.getId()));
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskExecRecord updateExecutionFromEngine(Long taskId, Long executionId, FaultTaskExecutionUpdateRequest request) {
        // 这是内部引擎回写边界，不应让前端直接修改执行状态。
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
        // 通用任务表包含多种 AI 任务，这里做类型隔离。
        AiTask task = aiManagementService.taskDetail(id);
        if (!TASK_TYPE.equals(task.getTaskType())) { throw new BusinessException("任务不是故障预测任务"); }
        return task;
    }

    private AiTask toEntity(FaultTaskSaveRequest request) {
        try {
            // 当前引擎只接受 DB_RESOURCE 和注册表中支持的指标。
            validateEngineScope(request.getTargetType(), String.join(",", request.getMetricCodes()));
            AiTask task = new AiTask();
            task.setTaskName(request.getTaskName()); task.setTaskType(TASK_TYPE); task.setModelId(request.getModelId());
            task.setTargetType(request.getTargetType()); task.setTargetId(request.getTargetId());
            task.setMetricCodes(String.join(",", request.getMetricCodes())); task.setScheduleRule(objectMapper.writeValueAsString(request.getSchedules()));
            task.setParamJson("{\"engineStatus\":\"WAITING_ENGINE\",\"contractVersion\":\"1.0\"}"); task.setStatus("ENABLED");
            return task;
        } catch (JsonProcessingException ex) { throw new BusinessException("定时预测规则格式不合法"); }
    }

    private void validateEngineScope(String targetType, String metricCodes) {
        // 第一版模型契约固定为分布式数据库对象和有限指标集合。
        if (!"DB_RESOURCE".equals(targetType)) {
            throw new BusinessException("第一版时序异常检测仅支持分布式数据库对象");
        }
        metricRegistry.requireSupported(metricCodes);
    }
}
