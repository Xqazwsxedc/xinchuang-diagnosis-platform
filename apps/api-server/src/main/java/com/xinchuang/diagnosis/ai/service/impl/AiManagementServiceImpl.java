package com.xinchuang.diagnosis.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xinchuang.diagnosis.ai.dto.AiPageQuery;
import com.xinchuang.diagnosis.ai.dto.ResultHandleRequest;
import com.xinchuang.diagnosis.ai.dto.TaskExecuteRequest;
import com.xinchuang.diagnosis.ai.entity.AiModel;
import com.xinchuang.diagnosis.ai.entity.AiTask;
import com.xinchuang.diagnosis.ai.entity.DefectReport;
import com.xinchuang.diagnosis.ai.entity.FaultReport;
import com.xinchuang.diagnosis.ai.entity.KnowledgeInstance;
import com.xinchuang.diagnosis.ai.entity.OptimizationReport;
import com.xinchuang.diagnosis.ai.entity.ResultStatusLog;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.ai.mapper.AiModelMapper;
import com.xinchuang.diagnosis.ai.mapper.AiTaskMapper;
import com.xinchuang.diagnosis.ai.mapper.DefectReportMapper;
import com.xinchuang.diagnosis.ai.mapper.FaultReportMapper;
import com.xinchuang.diagnosis.ai.mapper.KnowledgeInstanceMapper;
import com.xinchuang.diagnosis.ai.mapper.OptimizationReportMapper;
import com.xinchuang.diagnosis.ai.mapper.ResultStatusLogMapper;
import com.xinchuang.diagnosis.ai.mapper.TaskExecRecordMapper;
import com.xinchuang.diagnosis.ai.service.AiManagementService;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.ledger.entity.BusinessSystem;
import com.xinchuang.diagnosis.ledger.entity.DbResource;
import com.xinchuang.diagnosis.ledger.mapper.BusinessSystemMapper;
import com.xinchuang.diagnosis.ledger.mapper.DbResourceMapper;
import com.xinchuang.diagnosis.security.LoginUser;
import com.xinchuang.diagnosis.security.UserContext;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.entity.SysUser;
import com.xinchuang.diagnosis.system.mapper.SysUserMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiManagementServiceImpl implements AiManagementService {
    private static final String FAULT_PREDICTION = "FAULT_PREDICTION";
    private static final String DEFECT_DETECTION = "DEFECT_DETECTION";
    private static final String PERFORMANCE_OPTIMIZATION = "PERFORMANCE_OPTIMIZATION";
    private static final Set<String> FAULT_METRICS = Set.of("CPU", "内存", "磁盘IO", "网络带宽", "系统负载", "进程状态", "数据库连接数", "SQL响应时长", "事务成功率", "表空间使用率", "索引命中率", "锁等待次数", "日志增长速率", "缓存命中率", "业务请求时延", "数据入库速率", "业务成功率", "批量写库吞吐量", "连接池使用率", "等待线程数", "平均连接等待时长", "连接超时次数");
    private final AiModelMapper aiModelMapper;
    private final AiTaskMapper aiTaskMapper;
    private final TaskExecRecordMapper taskExecRecordMapper;
    private final KnowledgeInstanceMapper knowledgeInstanceMapper;
    private final FaultReportMapper faultReportMapper;
    private final DefectReportMapper defectReportMapper;
    private final OptimizationReportMapper optimizationReportMapper;
    private final ResultStatusLogMapper resultStatusLogMapper;
    private final BusinessSystemMapper businessSystemMapper;
    private final DbResourceMapper dbResourceMapper;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final SysUserMapper sysUserMapper;

    public AiManagementServiceImpl(AiModelMapper aiModelMapper,
                                   AiTaskMapper aiTaskMapper,
                                   TaskExecRecordMapper taskExecRecordMapper,
                                   KnowledgeInstanceMapper knowledgeInstanceMapper,
                                   FaultReportMapper faultReportMapper,
                                   DefectReportMapper defectReportMapper,
                                   OptimizationReportMapper optimizationReportMapper,
                                   ResultStatusLogMapper resultStatusLogMapper,
                                   BusinessSystemMapper businessSystemMapper,
                                   DbResourceMapper dbResourceMapper,
                                   AuditLogService auditLogService,
                                   ObjectMapper objectMapper,
                                   SysUserMapper sysUserMapper) {
        this.aiModelMapper = aiModelMapper;
        this.aiTaskMapper = aiTaskMapper;
        this.taskExecRecordMapper = taskExecRecordMapper;
        this.knowledgeInstanceMapper = knowledgeInstanceMapper;
        this.faultReportMapper = faultReportMapper;
        this.defectReportMapper = defectReportMapper;
        this.optimizationReportMapper = optimizationReportMapper;
        this.resultStatusLogMapper = resultStatusLogMapper;
        this.businessSystemMapper = businessSystemMapper;
        this.dbResourceMapper = dbResourceMapper;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public PageResponse<AiModel> pageModels(AiPageQuery query) {
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(AiModel::getModelCode, query.getKeyword()).or().like(AiModel::getModelName, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getModelType())) {
            wrapper.eq(AiModel::getModelType, query.getModelType());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(AiModel::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(AiModel::getUpdatedAt);
        Page<AiModel> page = aiModelMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public AiModel modelDetail(Long id) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException("AI模型不存在");
        }
        return model;
    }

    @Override
    public AiModel createModel(AiModel request) {
        requireText(request.getModelCode(), "模型编码不能为空");
        requireText(request.getModelName(), "模型名称不能为空");
        ensureModelCodeUnique(request.getModelCode(), null);
        if (!StringUtils.hasText(request.getStatus())) {
            request.setStatus("ENABLED");
        }
        aiModelMapper.insert(request);
        auditLogService.recordSuccess("AI模型", "CREATE", "AI_MODEL", request.getId(), "新增AI模型：" + request.getModelName());
        return modelDetail(request.getId());
    }

    @Override
    public AiModel updateModel(Long id, AiModel request) {
        AiModel model = modelDetail(id);
        requireText(request.getModelCode(), "模型编码不能为空");
        requireText(request.getModelName(), "模型名称不能为空");
        ensureModelCodeUnique(request.getModelCode(), id);
        model.setModelCode(request.getModelCode());
        model.setModelName(request.getModelName());
        model.setModelType(request.getModelType());
        model.setModelVersion(request.getModelVersion());
        model.setProvider(request.getProvider());
        model.setDescription(request.getDescription());
        model.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ENABLED");
        aiModelMapper.updateById(model);
        auditLogService.recordSuccess("AI模型", "UPDATE", "AI_MODEL", id, "编辑AI模型：" + model.getModelName());
        return modelDetail(id);
    }

    @Override
    public void deleteModel(Long id) {
        AiModel model = modelDetail(id);
        Long count = aiTaskMapper.selectCount(new LambdaQueryWrapper<AiTask>().eq(AiTask::getModelId, id));
        if (count != null && count > 0) {
            throw new BusinessException("该AI模型已被任务引用，不能删除");
        }
        aiModelMapper.deleteById(id);
        auditLogService.recordSuccess("AI模型", "DELETE", "AI_MODEL", id, "删除AI模型：" + model.getModelName());
    }

    @Override
    public PageResponse<KnowledgeInstance> pageKnowledge(AiPageQuery query) {
        LambdaQueryWrapper<KnowledgeInstance> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(KnowledgeInstance::getKnowledgeCode, query.getKeyword()).or().like(KnowledgeInstance::getKnowledgeName, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getKnowledgeType())) {
            wrapper.eq(KnowledgeInstance::getKnowledgeType, query.getKnowledgeType());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(KnowledgeInstance::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(KnowledgeInstance::getUpdatedAt);
        Page<KnowledgeInstance> page = knowledgeInstanceMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public KnowledgeInstance knowledgeDetail(Long id) {
        KnowledgeInstance knowledge = knowledgeInstanceMapper.selectById(id);
        if (knowledge == null) {
            throw new BusinessException("知识不存在");
        }
        return knowledge;
    }

    @Override
    public KnowledgeInstance createKnowledge(KnowledgeInstance request) {
        requireText(request.getKnowledgeCode(), "知识编码不能为空");
        requireText(request.getKnowledgeName(), "知识名称不能为空");
        requireText(request.getKnowledgeType(), "知识类型不能为空");
        requireText(request.getKnowledgeContent(), "知识内容不能为空");
        ensureKnowledgeCodeUnique(request.getKnowledgeCode(), null);
        if (!StringUtils.hasText(request.getStatus())) {
            request.setStatus("ENABLED");
        }
        if (!StringUtils.hasText(request.getSourceType())) {
            request.setSourceType("MANUAL");
        }
        if (request.getVersion() == null) {
            request.setVersion(1);
        }
        knowledgeInstanceMapper.insert(request);
        auditLogService.recordSuccess("AI知识库", "CREATE", "KB_INSTANCE", request.getId(), "新增知识：" + request.getKnowledgeName());
        return knowledgeDetail(request.getId());
    }

    @Override
    public KnowledgeInstance updateKnowledge(Long id, KnowledgeInstance request) {
        KnowledgeInstance knowledge = knowledgeDetail(id);
        requireText(request.getKnowledgeCode(), "知识编码不能为空");
        requireText(request.getKnowledgeName(), "知识名称不能为空");
        requireText(request.getKnowledgeType(), "知识类型不能为空");
        requireText(request.getKnowledgeContent(), "知识内容不能为空");
        ensureKnowledgeCodeUnique(request.getKnowledgeCode(), id);
        knowledge.setKnowledgeCode(request.getKnowledgeCode());
        knowledge.setKnowledgeName(request.getKnowledgeName());
        knowledge.setKnowledgeType(request.getKnowledgeType());
        knowledge.setKnowledgeContent(request.getKnowledgeContent());
        knowledge.setAttachmentNames(request.getAttachmentNames());
        knowledge.setSourceType(StringUtils.hasText(request.getSourceType()) ? request.getSourceType() : "MANUAL");
        knowledge.setSourceResultType(request.getSourceResultType());
        knowledge.setSourceResultId(request.getSourceResultId());
        knowledge.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ENABLED");
        knowledge.setVersion(request.getVersion() == null ? knowledge.getVersion() : request.getVersion());
        knowledgeInstanceMapper.updateById(knowledge);
        auditLogService.recordSuccess("AI知识库", "UPDATE", "KB_INSTANCE", id, "编辑知识：" + knowledge.getKnowledgeName());
        return knowledgeDetail(id);
    }

    @Override
    public void deleteKnowledge(Long id) {
        KnowledgeInstance knowledge = knowledgeDetail(id);
        knowledgeInstanceMapper.deleteById(id);
        auditLogService.recordSuccess("AI知识库", "DELETE", "KB_INSTANCE", id, "删除知识：" + knowledge.getKnowledgeName());
    }

    @Override
    public PageResponse<AiTask> pageTasks(AiPageQuery query) {
        LambdaQueryWrapper<AiTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(AiTask::getTaskCode, query.getKeyword()).or().like(AiTask::getTaskName, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getTaskType())) {
            wrapper.eq(AiTask::getTaskType, query.getTaskType());
        }
        if (query.getModelId() != null) {
            wrapper.eq(AiTask::getModelId, query.getModelId());
        }
        if (StringUtils.hasText(query.getCreatorName())) {
            List<Long> creatorIds = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .and(w -> w.like(SysUser::getUsername, query.getCreatorName())
                        .or().like(SysUser::getRealName, query.getCreatorName())))
                .stream().map(SysUser::getId).toList();
            if (creatorIds.isEmpty()) {
                return PageResponse.of(Collections.emptyList(), 0, query.getPageNo(), query.getPageSize());
            }
            wrapper.in(AiTask::getCreatedBy, creatorIds);
        }
        applyCreatedDateFilter(wrapper, query.getCreatedDate());
        if (StringUtils.hasText(query.getTargetType())) {
            wrapper.eq(AiTask::getTargetType, query.getTargetType());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(AiTask::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(AiTask::getUpdatedAt);
        Page<AiTask> page = aiTaskMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        populateCreatorNames(page.getRecords());
        return PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    private void applyCreatedDateFilter(LambdaQueryWrapper<AiTask> wrapper, String createdDate) {
        if (!StringUtils.hasText(createdDate)) {
            return;
        }
        try {
            LocalDate date = LocalDate.parse(createdDate);
            wrapper.ge(AiTask::getCreatedAt, date.atStartOfDay())
                .lt(AiTask::getCreatedAt, date.plusDays(1).atStartOfDay());
        } catch (DateTimeParseException ex) {
            throw new BusinessException("创建日期格式不合法，应为 yyyy-MM-dd");
        }
    }

    private void populateCreatorNames(List<AiTask> tasks) {
        Set<Long> creatorIds = tasks.stream().map(AiTask::getCreatedBy).filter(Objects::nonNull).collect(Collectors.toSet());
        if (creatorIds.isEmpty()) {
            return;
        }
        Map<Long, SysUser> users = sysUserMapper.selectBatchIds(creatorIds).stream()
            .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        tasks.forEach(task -> {
            SysUser user = users.get(task.getCreatedBy());
            if (user != null) {
                task.setCreatorName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername());
            }
        });
    }

    @Override
    public AiTask taskDetail(Long id) {
        AiTask task = aiTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("AI任务不存在");
        }
        populateCreatorNames(List.of(task));
        return task;
    }

    @Override
    public AiTask createTask(AiTask request) {
        if (isEngineTask(request.getTaskType()) && !StringUtils.hasText(request.getTaskCode())) {
            request.setTaskCode(taskCodePrefix(request.getTaskType()) + IdWorker.getId());
        }
        fillTaskForSave(request, null);
        LoginUser user = UserContext.get();
        if (user != null) {
            request.setCreatedBy(user.getUserId());
            request.setUpdatedBy(user.getUserId());
        }
        aiTaskMapper.insert(request);
        auditLogService.recordSuccess("AI任务", "CREATE", "AI_TASK", request.getId(), "新增AI任务：" + request.getTaskName());
        return taskDetail(request.getId());
    }

    @Override
    public AiTask updateTask(Long id, AiTask request) {
        AiTask task = taskDetail(id);
        request.setId(id);
        if (!StringUtils.hasText(request.getTaskCode())) {
            request.setTaskCode(task.getTaskCode());
        }
        fillTaskForSave(request, task);
        LoginUser user = UserContext.get();
        if (user != null) {
            task.setUpdatedBy(user.getUserId());
        }
        aiTaskMapper.updateById(task);
        auditLogService.recordSuccess("AI任务", "UPDATE", "AI_TASK", id, "编辑AI任务：" + task.getTaskName());
        return taskDetail(id);
    }

    @Override
    public void deleteTask(Long id) {
        AiTask task = taskDetail(id);
        aiTaskMapper.deleteById(id);
        auditLogService.recordSuccess("AI任务", "DELETE", "AI_TASK", id, "删除AI任务：" + task.getTaskName());
    }

    @Override
    public TaskExecRecord executeTask(Long id, TaskExecuteRequest request) {
        AiTask task = taskDetail(id);
        TaskExecRecord record = new TaskExecRecord();
        record.setTaskId(id);
        record.setTaskType(task.getTaskType());
        record.setTaskNameSnapshot(task.getTaskName());
        boolean waitingForEngine = isEngineTask(task.getTaskType());
        record.setExecutionStatus(waitingForEngine ? "PENDING" : "SUCCESS");
        record.setTriggerType(StringUtils.hasText(request.getTriggerType()) ? request.getTriggerType() : "MANUAL");
        record.setStartTime(LocalDateTime.now());
        if (!waitingForEngine) {
            record.setEndTime(LocalDateTime.now());
        }
        record.setResultCount(0);
        taskExecRecordMapper.insert(record);
        task.setExecutionCount((task.getExecutionCount() == null ? 0 : task.getExecutionCount()) + 1);
        task.setLastExecTime(record.getStartTime());
        aiTaskMapper.updateById(task);
        auditLogService.recordSuccess("AI任务", "EXECUTE", "AI_TASK", id,
            waitingForEngine ? "已受理AI任务，等待引擎处理：" + task.getTaskName() : "手动执行AI任务：" + task.getTaskName());
        return record;
    }

    @Override
    public PageResponse<TaskExecRecord> pageExecutions(AiPageQuery query) {
        LambdaQueryWrapper<TaskExecRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getTaskType())) {
            wrapper.eq(TaskExecRecord::getTaskType, query.getTaskType());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(TaskExecRecord::getExecutionStatus, query.getStatus());
        }
        wrapper.orderByDesc(TaskExecRecord::getStartTime);
        Page<TaskExecRecord> page = taskExecRecordMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PageResponse<FaultReport> pageFaultReports(AiPageQuery query) {
        LambdaQueryWrapper<FaultReport> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(FaultReport::getReportCode, query.getKeyword()).or().like(FaultReport::getTargetNameSnapshot, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getProcessStatus())) {
            wrapper.eq(FaultReport::getProcessStatus, query.getProcessStatus());
        }
        if (StringUtils.hasText(query.getLevel())) {
            wrapper.eq(FaultReport::getRiskLevel, query.getLevel());
        }
        if (StringUtils.hasText(query.getTargetType())) {
            wrapper.eq(FaultReport::getTargetType, query.getTargetType());
        }
        wrapper.orderByDesc(FaultReport::getAlertTime);
        Page<FaultReport> page = faultReportMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public FaultReport faultReportDetail(Long id) {
        FaultReport report = faultReportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("故障预警不存在");
        }
        return report;
    }

    @Override
    public FaultReport createFaultReport(FaultReport request) {
        requireText(request.getReportCode(), "预警编码不能为空");
        requireText(request.getFaultType(), "故障类型不能为空");
        fillTargetSnapshot(request.getTargetType(), request.getTargetId(), request::setTargetNameSnapshot);
        if (!StringUtils.hasText(request.getProcessStatus())) {
            request.setProcessStatus("PENDING");
        }
        if (!StringUtils.hasText(request.getRiskLevel())) {
            request.setRiskLevel("MEDIUM");
        }
        if (request.getAlertTime() == null) {
            request.setAlertTime(LocalDateTime.now());
        }
        faultReportMapper.insert(request);
        auditLogService.recordSuccess("AI故障预测", "CREATE", "FAULT_REPORT", request.getId(), "新增故障预警：" + request.getReportCode());
        return faultReportDetail(request.getId());
    }

    @Override
    public FaultReport updateFaultReport(Long id, FaultReport request) {
        FaultReport report = faultReportDetail(id);
        report.setIndicatorCategory(request.getIndicatorCategory());
        report.setFaultType(request.getFaultType());
        report.setRiskLevel(request.getRiskLevel());
        report.setExpectedStartTime(request.getExpectedStartTime());
        report.setExpectedEndTime(request.getExpectedEndTime());
        report.setConfidence(request.getConfidence());
        report.setForecastDescription(request.getForecastDescription());
        report.setHandleSuggestion(request.getHandleSuggestion());
        report.setTrendJson(request.getTrendJson());
        faultReportMapper.updateById(report);
        auditLogService.recordSuccess("AI故障预测", "UPDATE", "FAULT_REPORT", id, "编辑故障预警：" + report.getReportCode());
        return faultReportDetail(id);
    }

    @Override
    public void handleFaultReport(Long id, ResultHandleRequest request) {
        FaultReport report = faultReportDetail(id);
        String before = report.getProcessStatus();
        ensureFaultStatusTransition(before, request.getProcessStatus());
        report.setProcessStatus(request.getProcessStatus());
        report.setHandledAt(LocalDateTime.now());
        LoginUser user = UserContext.get();
        if (user != null) {
            report.setHandledBy(user.getUserId());
        }
        faultReportMapper.updateById(report);
        recordStatusLog("FAULT", id, before, request);
        auditLogService.recordSuccess("AI故障预测", "STATUS", "FAULT_REPORT", id, "处理故障预警：" + before + " -> " + request.getProcessStatus());
    }

    @Override
    public void deleteFaultReport(Long id) {
        FaultReport report = faultReportDetail(id);
        faultReportMapper.deleteById(id);
        auditLogService.recordSuccess("AI故障预测", "DELETE", "FAULT_REPORT", id, "删除故障预警：" + report.getReportCode());
    }

    @Override
    public PageResponse<DefectReport> pageDefectReports(AiPageQuery query) {
        LambdaQueryWrapper<DefectReport> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(DefectReport::getDefectCode, query.getKeyword()).or().like(DefectReport::getDefectProblem, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getProcessStatus())) {
            wrapper.eq(DefectReport::getProcessStatus, query.getProcessStatus());
        }
        if (StringUtils.hasText(query.getDefectCode())) {
            wrapper.like(DefectReport::getDefectCode, query.getDefectCode().trim());
        }
        if (StringUtils.hasText(query.getTaskName())) {
            wrapper.like(DefectReport::getTaskNameSnapshot, query.getTaskName().trim());
        }
        if (StringUtils.hasText(query.getCreatorName())) {
            List<Long> creatorIds = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .and(item -> item.like(SysUser::getUsername, query.getCreatorName().trim())
                        .or().like(SysUser::getRealName, query.getCreatorName().trim())))
                .stream().map(SysUser::getId).toList();
            if (creatorIds.isEmpty()) {
                return PageResponse.of(List.of(), 0, query.getPageNo(), query.getPageSize());
            }
            wrapper.in(DefectReport::getCreatedBy, creatorIds);
        }
        if (StringUtils.hasText(query.getLevel())) {
            wrapper.eq(DefectReport::getSeverityLevel, query.getLevel());
        }
        wrapper.orderByDesc(DefectReport::getUpdatedAt);
        Page<DefectReport> page = defectReportMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        populateDefectCreatorNames(page.getRecords());
        return PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public DefectReport defectReportDetail(Long id) {
        DefectReport report = defectReportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("缺陷报表不存在");
        }
        populateDefectCreatorNames(List.of(report));
        return report;
    }

    @Override
    public DefectReport createDefectReport(DefectReport request) {
        requireText(request.getDefectCode(), "缺陷编码不能为空");
        requireText(request.getDefectType(), "缺陷类型不能为空");
        requireText(request.getDefectProblem(), "缺陷问题不能为空");
        fillTargetSnapshot(request.getTargetType(), request.getTargetId(), request::setTargetNameSnapshot);
        if (!StringUtils.hasText(request.getProcessStatus())) {
            request.setProcessStatus("PENDING");
        }
        if (!StringUtils.hasText(request.getSeverityLevel())) {
            request.setSeverityLevel("MEDIUM");
        }
        defectReportMapper.insert(request);
        auditLogService.recordSuccess("AI缺陷检测", "CREATE", "DEFECT_REPORT", request.getId(), "新增缺陷报表：" + request.getDefectCode());
        return defectReportDetail(request.getId());
    }

    @Override
    public DefectReport updateDefectReport(Long id, DefectReport request) {
        DefectReport report = defectReportDetail(id);
        report.setDefectType(request.getDefectType());
        report.setSeverityLevel(request.getSeverityLevel());
        report.setDefectProblem(request.getDefectProblem());
        report.setDefectData(request.getDefectData());
        report.setDefectDescription(request.getDefectDescription());
        report.setRepairSuggestion(request.getRepairSuggestion());
        defectReportMapper.updateById(report);
        auditLogService.recordSuccess("AI缺陷检测", "UPDATE", "DEFECT_REPORT", id, "编辑缺陷报表：" + report.getDefectCode());
        return defectReportDetail(id);
    }

    @Override
    public void handleDefectReport(Long id, ResultHandleRequest request) {
        DefectReport report = defectReportDetail(id);
        String before = report.getProcessStatus();
        report.setProcessStatus(request.getProcessStatus());
        report.setHandledAt(LocalDateTime.now());
        LoginUser user = UserContext.get();
        if (user != null) {
            report.setHandledBy(user.getUserId());
        }
        defectReportMapper.updateById(report);
        recordStatusLog("DEFECT", id, before, request);
        auditLogService.recordSuccess("AI缺陷检测", "STATUS", "DEFECT_REPORT", id, "处理缺陷报表：" + before + " -> " + request.getProcessStatus());
    }

    @Override
    public void deleteDefectReport(Long id) {
        DefectReport report = defectReportDetail(id);
        defectReportMapper.deleteById(id);
        auditLogService.recordSuccess("AI缺陷检测", "DELETE", "DEFECT_REPORT", id, "删除缺陷报表：" + report.getDefectCode());
    }

    @Override
    public byte[] exportDefectReports(List<Long> reportIds) {
        if (reportIds == null || reportIds.isEmpty()) {
            throw new BusinessException("请至少选择一条缺陷报表");
        }
        if (reportIds.size() > 100) {
            throw new BusinessException("单次最多导出100条缺陷报表");
        }
        List<DefectReport> reports = defectReportMapper.selectBatchIds(reportIds);
        if (reports.size() != reportIds.stream().distinct().count()) {
            throw new BusinessException("导出清单包含不存在或已删除的缺陷报表");
        }
        populateDefectCreatorNames(reports);
        StringBuilder csv = new StringBuilder("\uFEFF缺陷ID,缺陷检查任务名称,创建者,创建时间,缺陷问题,检测结果确认\r\n");
        reports.forEach(item -> csv.append(csvValue(item.getDefectCode())).append(',')
            .append(csvValue(item.getTaskNameSnapshot())).append(',')
            .append(csvValue(item.getCreatorName())).append(',')
            .append(csvValue(item.getCreatedAt())).append(',')
            .append(csvValue(item.getDefectProblem())).append(',')
            .append(csvValue(item.getProcessStatus())).append("\r\n"));
        auditLogService.recordSuccess("AI缺陷检测", "EXPORT", "DEFECT_REPORT", null, "导出缺陷检测报表：" + reports.size() + "条");
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private void populateDefectCreatorNames(List<DefectReport> reports) {
        Set<Long> creatorIds = reports.stream().map(DefectReport::getCreatedBy).filter(Objects::nonNull).collect(Collectors.toSet());
        if (creatorIds.isEmpty()) return;
        Map<Long, SysUser> users = sysUserMapper.selectBatchIds(creatorIds).stream().collect(Collectors.toMap(SysUser::getId, Function.identity()));
        reports.forEach(report -> { SysUser user = users.get(report.getCreatedBy()); if (user != null) report.setCreatorName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername()); });
    }

    private String csvValue(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\"";
    }

    @Override
    public PageResponse<OptimizationReport> pageOptimizationReports(AiPageQuery query) {
        LambdaQueryWrapper<OptimizationReport> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(OptimizationReport::getReportCode, query.getKeyword()).or().like(OptimizationReport::getOptimizationProblem, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getProcessStatus())) {
            wrapper.eq(OptimizationReport::getProcessStatus, query.getProcessStatus());
        }
        if (StringUtils.hasText(query.getTaskName())) {
            wrapper.like(OptimizationReport::getTaskNameSnapshot, query.getTaskName().trim());
        }
        if (StringUtils.hasText(query.getCreatorName())) {
            List<Long> creatorIds = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .and(item -> item.like(SysUser::getUsername, query.getCreatorName().trim())
                        .or().like(SysUser::getRealName, query.getCreatorName().trim())))
                .stream().map(SysUser::getId).toList();
            if (creatorIds.isEmpty()) {
                return PageResponse.of(List.of(), 0, query.getPageNo(), query.getPageSize());
            }
            wrapper.in(OptimizationReport::getCreatedBy, creatorIds);
        }
        wrapper.orderByDesc(OptimizationReport::getUpdatedAt);
        Page<OptimizationReport> page = optimizationReportMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        populateOptimizationCreatorNames(page.getRecords());
        return PageResponse.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public OptimizationReport optimizationReportDetail(Long id) {
        OptimizationReport report = optimizationReportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("性能优化建议不存在");
        }
        populateOptimizationCreatorNames(List.of(report));
        return report;
    }

    @Override
    public byte[] exportOptimizationReports(List<Long> reportIds) {
        if (reportIds == null || reportIds.isEmpty()) {
            throw new BusinessException("请至少选择一条性能优化建议");
        }
        if (reportIds.size() > 100) {
            throw new BusinessException("单次最多导出100条性能优化建议");
        }
        List<OptimizationReport> reports = optimizationReportMapper.selectBatchIds(reportIds);
        if (reports.size() != reportIds.stream().distinct().count()) {
            throw new BusinessException("导出清单包含不存在或已删除的性能优化建议");
        }
        populateOptimizationCreatorNames(reports);
        StringBuilder csv = new StringBuilder("\uFEFF优化建议ID,任务名称,优化对象,优化问题,创建时间,创建者,处理状态\r\n");
        reports.forEach(item -> csv.append(csvValue(item.getReportCode())).append(',')
            .append(csvValue(item.getTaskNameSnapshot())).append(',')
            .append(csvValue(item.getTargetNameSnapshot())).append(',')
            .append(csvValue(item.getOptimizationProblem())).append(',')
            .append(csvValue(item.getCreatedAt())).append(',')
            .append(csvValue(item.getCreatorName())).append(',')
            .append(csvValue(item.getProcessStatus())).append("\r\n"));
        auditLogService.recordSuccess("AI性能优化", "EXPORT", "OPT_REPORT", null, "导出性能优化建议：" + reports.size() + "条");
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private void populateOptimizationCreatorNames(List<OptimizationReport> reports) {
        Set<Long> creatorIds = reports.stream().map(OptimizationReport::getCreatedBy).filter(Objects::nonNull).collect(Collectors.toSet());
        if (creatorIds.isEmpty()) return;
        Map<Long, SysUser> users = sysUserMapper.selectBatchIds(creatorIds).stream().collect(Collectors.toMap(SysUser::getId, Function.identity()));
        reports.forEach(report -> { SysUser user = users.get(report.getCreatedBy()); if (user != null) report.setCreatorName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername()); });
    }

    @Override
    public OptimizationReport createOptimizationReport(OptimizationReport request) {
        requireText(request.getReportCode(), "优化建议编码不能为空");
        requireText(request.getOptimizationType(), "优化类型不能为空");
        requireText(request.getOptimizationProblem(), "性能问题不能为空");
        fillTargetSnapshot(request.getTargetType(), request.getTargetId(), request::setTargetNameSnapshot);
        if (!StringUtils.hasText(request.getProcessStatus())) {
            request.setProcessStatus("PENDING");
        }
        optimizationReportMapper.insert(request);
        auditLogService.recordSuccess("AI性能优化", "CREATE", "OPT_REPORT", request.getId(), "新增优化建议：" + request.getReportCode());
        return optimizationReportDetail(request.getId());
    }

    @Override
    public OptimizationReport updateOptimizationReport(Long id, OptimizationReport request) {
        OptimizationReport report = optimizationReportDetail(id);
        report.setOptimizationType(request.getOptimizationType());
        report.setOptimizationProblem(request.getOptimizationProblem());
        report.setSqlText(request.getSqlText());
        report.setSqlFingerprint(request.getSqlFingerprint());
        report.setExecutionPlanJson(request.getExecutionPlanJson());
        report.setSlowSqlMetricsJson(request.getSlowSqlMetricsJson());
        report.setOptimizationContent(request.getOptimizationContent());
        report.setConfidence(request.getConfidence());
        report.setExecutedSql(request.getExecutedSql());
        report.setExecuteResult(request.getExecuteResult());
        report.setBeforeMetricsJson(request.getBeforeMetricsJson());
        report.setAfterMetricsJson(request.getAfterMetricsJson());
        optimizationReportMapper.updateById(report);
        auditLogService.recordSuccess("AI性能优化", "UPDATE", "OPT_REPORT", id, "编辑优化建议：" + report.getReportCode());
        return optimizationReportDetail(id);
    }

    @Override
    public void handleOptimizationReport(Long id, ResultHandleRequest request) {
        OptimizationReport report = optimizationReportDetail(id);
        String before = report.getProcessStatus();
        report.setProcessStatus(request.getProcessStatus());
        report.setHandledAt(LocalDateTime.now());
        LoginUser user = UserContext.get();
        if (user != null) {
            report.setHandledBy(user.getUserId());
        }
        optimizationReportMapper.updateById(report);
        recordStatusLog("OPTIMIZATION", id, before, request);
        auditLogService.recordSuccess("AI性能优化", "STATUS", "OPT_REPORT", id, "处理优化建议：" + before + " -> " + request.getProcessStatus());
    }

    @Override
    public void deleteOptimizationReport(Long id) {
        OptimizationReport report = optimizationReportDetail(id);
        optimizationReportMapper.deleteById(id);
        auditLogService.recordSuccess("AI性能优化", "DELETE", "OPT_REPORT", id, "删除优化建议：" + report.getReportCode());
    }

    private void fillTaskForSave(AiTask request, AiTask target) {
        requireText(request.getTaskCode(), "任务编码不能为空");
        requireText(request.getTaskName(), "任务名称不能为空");
        requireText(request.getTaskType(), "任务类型不能为空");
        requireText(request.getTargetType(), "目标类型不能为空");
        if (request.getTargetId() == null) {
            throw new BusinessException("目标ID不能为空");
        }
        ensureTaskCodeUnique(request.getTaskCode(), request.getId());
        if (FAULT_PREDICTION.equals(request.getTaskType()) && request.getModelId() == null) {
            throw new BusinessException("故障预测任务必须选择AI模型");
        }
        AiModel model = null;
        if (request.getModelId() != null) {
            model = aiModelMapper.selectById(request.getModelId());
            if (model == null) {
                throw new BusinessException("AI模型不存在");
            }
            if (!"ENABLED".equals(model.getStatus())) {
                throw new BusinessException("AI模型未启用");
            }
            if (!Objects.equals(model.getModelType(), request.getTaskType())) {
                throw new BusinessException("AI模型类型与任务类型不匹配");
            }
        }
        if (FAULT_PREDICTION.equals(request.getTaskType())) {
            validateFaultTaskConfiguration(request);
        } else if (DEFECT_DETECTION.equals(request.getTaskType())) {
            validateDefectTaskConfiguration(request);
        } else if (PERFORMANCE_OPTIMIZATION.equals(request.getTaskType())) {
            validateOptimizationTaskConfiguration(request);
        }
        String targetName = resolveTargetName(request.getTargetType(), request.getTargetId());
        AiTask entity = target == null ? request : target;
        entity.setTaskCode(request.getTaskCode());
        entity.setTaskName(request.getTaskName());
        entity.setTaskType(request.getTaskType());
        entity.setModelId(request.getModelId());
        entity.setModelNameSnapshot(model == null ? request.getModelNameSnapshot() : model.getModelName());
        entity.setTargetType(request.getTargetType());
        entity.setTargetId(request.getTargetId());
        entity.setTargetNameSnapshot(targetName);
        entity.setMetricCodes(request.getMetricCodes());
        entity.setParamJson(request.getParamJson());
        entity.setScheduleRule(request.getScheduleRule());
        entity.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ENABLED");
        entity.setNextExecTime(request.getNextExecTime());
        if (entity.getExecutionCount() == null) {
            entity.setExecutionCount(0);
        }
        if (entity.getResultCount() == null) {
            entity.setResultCount(0);
        }
    }

    private void validateFaultTaskConfiguration(AiTask request) {
        if (!StringUtils.hasText(request.getMetricCodes())) {
            throw new BusinessException("请至少选择一个预测指标");
        }
        List<String> metrics = List.of(request.getMetricCodes().split(",")).stream().map(String::trim).filter(StringUtils::hasText).toList();
        if (metrics.isEmpty() || metrics.stream().anyMatch(metric -> !FAULT_METRICS.contains(metric)) || metrics.stream().distinct().count() != metrics.size()) {
            throw new BusinessException("预测指标不合法或重复");
        }
        if (!StringUtils.hasText(request.getScheduleRule())) {
            throw new BusinessException("请至少配置一条定时预测规则");
        }
        try {
            List<FaultScheduleRule> schedules = objectMapper.readValue(request.getScheduleRule(), new TypeReference<>() {});
            if (schedules == null || schedules.isEmpty()) {
                throw new BusinessException("请至少配置一条定时预测规则");
            }
            for (FaultScheduleRule schedule : schedules) {
                if (schedule == null || !StringUtils.hasText(schedule.time()) || !schedule.time().matches("([01]\\d|2[0-3]):[0-5]\\d")) {
                    throw new BusinessException("定时规则时间格式不合法");
                }
                if (!"daily".equals(schedule.repeatType()) && !"weekly".equals(schedule.repeatType())) {
                    throw new BusinessException("定时规则重复周期不合法");
                }
                if ("weekly".equals(schedule.repeatType()) && (schedule.weekDays() == null || schedule.weekDays().isEmpty())) {
                    throw new BusinessException("每周规则至少选择一个星期");
                }
                if (schedule.predictionWindowHours() == null || !List.of(1, 2, 4, 12, 24).contains(schedule.predictionWindowHours())) {
                    throw new BusinessException("预测时间范围不合法");
                }
            }
            if (schedules.stream().map(rule -> rule.repeatType() + "|" + rule.time() + "|" + String.join(",", rule.weekDays() == null ? List.of() : rule.weekDays())).distinct().count() != schedules.size()) {
                throw new BusinessException("定时预测规则重复");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("定时预测规则格式不合法");
        }
    }

    /** 缺陷检测暂未接入真实引擎，保存时仍需校验原型要求的定时检测规则。 */
    private void validateDefectTaskConfiguration(AiTask request) {
        if (!StringUtils.hasText(request.getScheduleRule())) {
            throw new BusinessException("请至少配置一条定时检测规则");
        }
        try {
            JsonNode schedules = objectMapper.readTree(request.getScheduleRule());
            if (!schedules.isArray() || schedules.isEmpty()) {
                throw new BusinessException("请至少配置一条定时检测规则");
            }
            Set<String> uniqueRules = new java.util.HashSet<>();
            for (JsonNode schedule : schedules) {
                String time = schedule.path("time").asText("");
                String repeatType = schedule.path("repeatType").asText("");
                if (!time.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
                    throw new BusinessException("定时检测时间格式不合法");
                }
                if (!"daily".equals(repeatType) && !"weekly".equals(repeatType)) {
                    throw new BusinessException("定时检测重复周期不合法");
                }
                if ("weekly".equals(repeatType) && (!schedule.path("weekDays").isArray() || schedule.path("weekDays").isEmpty())) {
                    throw new BusinessException("每周检测规则至少选择一个星期");
                }
                String signature = time + "|" + repeatType + "|" + schedule.path("weekDays").toString();
                if (!uniqueRules.add(signature)) {
                    throw new BusinessException("定时检测规则重复");
                }
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("定时检测规则格式不合法");
        }
    }

    private boolean isEngineTask(String taskType) {
        return FAULT_PREDICTION.equals(taskType) || DEFECT_DETECTION.equals(taskType) || PERFORMANCE_OPTIMIZATION.equals(taskType);
    }

    private String taskCodePrefix(String taskType) {
        return switch (taskType) {
            case DEFECT_DETECTION -> "DEFECT-TASK-";
            case PERFORMANCE_OPTIMIZATION -> "OPT-TASK-";
            default -> "FAULT-TASK-";
        };
    }

    /** 性能优化任务沿用原型的对象、模型和定时设置，当前只受理到未来优化引擎。 */
    private void validateOptimizationTaskConfiguration(AiTask request) {
        if (request.getModelId() == null) {
            throw new BusinessException("性能优化任务必须选择AI模型");
        }
        if (!StringUtils.hasText(request.getScheduleRule())) {
            throw new BusinessException("请至少配置一条定时优化规则");
        }
        try {
            JsonNode schedules = objectMapper.readTree(request.getScheduleRule());
            if (!schedules.isArray() || schedules.isEmpty()) {
                throw new BusinessException("请至少配置一条定时优化规则");
            }
            Set<String> uniqueRules = new java.util.HashSet<>();
            for (JsonNode schedule : schedules) {
                String time = schedule.path("time").asText("");
                String repeatType = schedule.path("repeatType").asText("");
                if (!time.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
                    throw new BusinessException("定时优化时间格式不合法");
                }
                if (!"daily".equals(repeatType) && !"weekly".equals(repeatType)) {
                    throw new BusinessException("定时优化重复周期不合法");
                }
                if ("weekly".equals(repeatType) && (!schedule.path("weekDays").isArray() || schedule.path("weekDays").isEmpty())) {
                    throw new BusinessException("每周优化规则至少选择一个星期");
                }
                String signature = time + "|" + repeatType + "|" + schedule.path("weekDays").toString();
                if (!uniqueRules.add(signature)) {
                    throw new BusinessException("定时优化规则重复");
                }
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("定时优化规则格式不合法");
        }
    }

    private record FaultScheduleRule(String time, String repeatType, List<String> weekDays, Integer predictionWindowHours) {}

    private void fillTargetSnapshot(String targetType, Long targetId, java.util.function.Consumer<String> setter) {
        requireText(targetType, "目标类型不能为空");
        if (targetId == null) {
            throw new BusinessException("目标ID不能为空");
        }
        setter.accept(resolveTargetName(targetType, targetId));
    }

    private String resolveTargetName(String targetType, Long targetId) {
        if ("BUSINESS_SYSTEM".equals(targetType)) {
            BusinessSystem system = businessSystemMapper.selectById(targetId);
            if (system == null) {
                throw new BusinessException("业务系统不存在");
            }
            return system.getName();
        }
        if ("DB_RESOURCE".equals(targetType)) {
            DbResource resource = dbResourceMapper.selectById(targetId);
            if (resource == null) {
                throw new BusinessException("分布式数据库不存在");
            }
            return resource.getName();
        }
        throw new BusinessException("目标类型只能是 BUSINESS_SYSTEM/DB_RESOURCE");
    }

    private void recordStatusLog(String resultType, Long resultId, String beforeStatus, ResultHandleRequest request) {
        ResultStatusLog log = new ResultStatusLog();
        log.setResultType(resultType);
        log.setResultId(resultId);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(request.getProcessStatus());
        log.setOperationRemark(request.getRemark());
        LoginUser user = UserContext.get();
        if (user != null) {
            log.setOperatorId(user.getUserId());
            log.setOperatorNameSnapshot(user.getRealName());
        }
        resultStatusLogMapper.insert(log);
    }

    private void ensureFaultStatusTransition(String beforeStatus, String afterStatus) {
        boolean allowed = ("PENDING".equals(beforeStatus) && ("CONFIRMED".equals(afterStatus) || "FALSE_POSITIVE".equals(afterStatus)))
            || ("CONFIRMED".equals(beforeStatus) && "CLOSED".equals(afterStatus));
        if (!allowed) {
            throw new BusinessException("当前预警状态不允许执行该处理操作");
        }
    }

    private void ensureModelCodeUnique(String modelCode, Long currentId) {
        AiModel existing = aiModelMapper.selectOne(new LambdaQueryWrapper<AiModel>().eq(AiModel::getModelCode, modelCode));
        if (existing != null && !Objects.equals(existing.getId(), currentId)) {
            throw new BusinessException("模型编码已存在");
        }
    }

    private void ensureKnowledgeCodeUnique(String knowledgeCode, Long currentId) {
        KnowledgeInstance existing = knowledgeInstanceMapper.selectOne(new LambdaQueryWrapper<KnowledgeInstance>().eq(KnowledgeInstance::getKnowledgeCode, knowledgeCode));
        if (existing != null && !Objects.equals(existing.getId(), currentId)) {
            throw new BusinessException("知识编码已存在");
        }
    }

    private void ensureTaskCodeUnique(String taskCode, Long currentId) {
        AiTask existing = aiTaskMapper.selectOne(new LambdaQueryWrapper<AiTask>().eq(AiTask::getTaskCode, taskCode));
        if (existing != null && !Objects.equals(existing.getId(), currentId)) {
            throw new BusinessException("任务编码已存在");
        }
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(message);
        }
    }
}
