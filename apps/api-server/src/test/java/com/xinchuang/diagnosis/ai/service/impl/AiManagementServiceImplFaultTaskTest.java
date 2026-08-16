package com.xinchuang.diagnosis.ai.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinchuang.diagnosis.ai.dto.AiPageQuery;
import com.xinchuang.diagnosis.ai.dto.ResultHandleRequest;
import com.xinchuang.diagnosis.ai.dto.TaskExecuteRequest;
import com.xinchuang.diagnosis.ai.entity.AiModel;
import com.xinchuang.diagnosis.ai.entity.AiTask;
import com.xinchuang.diagnosis.ai.entity.DefectReport;
import com.xinchuang.diagnosis.ai.entity.FaultReport;
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
import com.xinchuang.diagnosis.ai.entity.KnowledgeInstance;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.ledger.mapper.BusinessSystemMapper;
import com.xinchuang.diagnosis.ledger.entity.BusinessSystem;
import com.xinchuang.diagnosis.ledger.mapper.DbResourceMapper;
import com.xinchuang.diagnosis.system.entity.SysUser;
import com.xinchuang.diagnosis.system.mapper.SysUserMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
/**
 * 覆盖故障预测任务和故障预警共用的 AI 服务规则。
 * 通过 Mapper Mock 验证每个拒绝分支不会把非法数据写入数据库。
 */
class AiManagementServiceImplFaultTaskTest {
    @Mock private AiModelMapper aiModelMapper;
    @Mock private AiTaskMapper aiTaskMapper;
    @Mock private TaskExecRecordMapper taskExecRecordMapper;
    @Mock private KnowledgeInstanceMapper knowledgeInstanceMapper;
    @Mock private FaultReportMapper faultReportMapper;
    @Mock private DefectReportMapper defectReportMapper;
    @Mock private OptimizationReportMapper optimizationReportMapper;
    @Mock private ResultStatusLogMapper resultStatusLogMapper;
    @Mock private BusinessSystemMapper businessSystemMapper;
    @Mock private DbResourceMapper dbResourceMapper;
    @Mock private AuditLogService auditLogService;
    @Mock private SysUserMapper sysUserMapper;
    private AiManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AiManagementServiceImpl(aiModelMapper, aiTaskMapper, taskExecRecordMapper,
            knowledgeInstanceMapper, faultReportMapper, defectReportMapper, optimizationReportMapper,
            resultStatusLogMapper, businessSystemMapper, dbResourceMapper, auditLogService,
            new ObjectMapper(), sysUserMapper);
    }

    @Test
    /** 故障预测任务未选择模型时，不能进入持久化流程。 */
    void createRejectsFaultTaskWithoutModel() {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.createTask(faultTask()));

        assertEquals("故障预测任务必须选择AI模型", exception.getMessage());
        verify(aiTaskMapper, never()).insert(any(AiTask.class));
    }

    @Test
    /** 每周规则未选择星期时含义不明确，必须在写入前拒绝。 */
    void createRejectsWeeklyRuleWithoutWeekDay() {
        AiTask task = faultTask();
        task.setModelId(1L);
        task.setScheduleRule("[{\"time\":\"08:00\",\"repeatType\":\"weekly\",\"weekDays\":[],\"predictionWindowHours\":4}]");
        when(aiModelMapper.selectById(1L)).thenReturn(enabledFaultModel());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.createTask(task));

        assertEquals("每周规则至少选择一个星期", exception.getMessage());
        verify(aiTaskMapper, never()).insert(any(AiTask.class));
    }

    @Test
    /** 重复指标会污染预测输入，不能创建任务。 */
    void createRejectsDuplicateMetricsBeforeWritingData() {
        AiTask task = faultTask();
        task.setModelId(1L);
        task.setMetricCodes("CPU,CPU");
        when(aiModelMapper.selectById(1L)).thenReturn(enabledFaultModel());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.createTask(task));

        assertEquals("预测指标不合法或重复", exception.getMessage());
        verify(aiTaskMapper, never()).insert(any(AiTask.class));
    }

    @Test
    /** 任务编码属于服务端内部标识，必须由服务端生成，不能信任前端传值。 */
    void createGeneratesFaultTaskCodeOnServer() {
        AiTask task = faultTask();
        task.setTaskCode(null);
        task.setModelId(1L);
        BusinessSystem system = new BusinessSystem();
        system.setId(1L);
        system.setName("订单系统");
        when(aiModelMapper.selectById(1L)).thenReturn(enabledFaultModel());
        when(businessSystemMapper.selectById(1L)).thenReturn(system);
        doAnswer(invocation -> { invocation.getArgument(0, AiTask.class).setId(11L); return 1; }).when(aiTaskMapper).insert(any(AiTask.class));
        when(aiTaskMapper.selectById(11L)).thenReturn(task);

        service.createTask(task);

        assertEquals(true, task.getTaskCode().startsWith("FAULT-TASK-"));
        verify(aiTaskMapper).insert(task);
    }

    @Test
    /** 人工执行只代表进入 AI 引擎队列，状态必须保持为 PENDING。 */
    void executeCreatesPendingRecordForFaultPrediction() {
        AiTask task = faultTask();
        task.setId(8L);
        when(aiTaskMapper.selectById(8L)).thenReturn(task);
        TaskExecuteRequest request = new TaskExecuteRequest();
        request.setTriggerType("MANUAL");

        TaskExecRecord result = service.executeTask(8L, request);

        ArgumentCaptor<TaskExecRecord> captor = ArgumentCaptor.forClass(TaskExecRecord.class);
        verify(taskExecRecordMapper).insert(captor.capture());
        assertEquals("PENDING", result.getExecutionStatus());
        assertNull(result.getEndTime());
        assertEquals("PENDING", captor.getValue().getExecutionStatus());
        assertNull(captor.getValue().getEndTime());
        assertEquals(1, task.getExecutionCount());
    }

    @Test
    /** 缺陷检测引擎尚未接入时，立即检测同样只能进入等待引擎状态。 */
    void executeCreatesPendingRecordForDefectDetection() {
        AiTask task = defectTask();
        task.setId(18L);
        when(aiTaskMapper.selectById(18L)).thenReturn(task);
        TaskExecRecord result = service.executeTask(18L, new TaskExecuteRequest());

        assertEquals("PENDING", result.getExecutionStatus());
        assertNull(result.getEndTime());
        verify(taskExecRecordMapper).insert(any(TaskExecRecord.class));
    }

    @Test
    /** 缺陷任务每周规则未选择星期时，必须在写入任务前拒绝。 */
    void createRejectsDefectWeeklyRuleWithoutWeekDay() {
        AiTask task = defectTask();
        task.setModelId(2L);
        task.setScheduleRule("[{\"time\":\"08:00\",\"repeatType\":\"weekly\",\"weekDays\":[],\"detectionRange\":\"PREVIOUS_DAY\"}]");
        when(aiModelMapper.selectById(2L)).thenReturn(enabledDefectModel());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.createTask(task));

        assertEquals("每周检测规则至少选择一个星期", exception.getMessage());
        verify(aiTaskMapper, never()).insert(any(AiTask.class));
    }

    @Test
    /** 列表创建人名称必须批量回填，不能引入 N+1 查询。 */
    void pagePopulatesCreatorNameInOneBatch() {
        AiTask task = faultTask();
        task.setId(2L);
        task.setCreatedBy(9L);
        Page<AiTask> page = new Page<>(1, 10);
        page.setRecords(List.of(task));
        page.setTotal(1);
        SysUser user = new SysUser();
        user.setId(9L);
        user.setRealName("任务创建人");
        when(aiTaskMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(sysUserMapper.selectBatchIds(any())).thenReturn(List.of(user));

        PageResponse<AiTask> response = service.pageTasks(new AiPageQuery());

        assertEquals("任务创建人", response.getRecords().get(0).getCreatorName());
        verify(sysUserMapper).selectBatchIds(any());
    }

    @Test
    /** 创建日期非法时，必须在调用分页 Mapper 前失败。 */
    void pageRejectsInvalidCreatedDate() {
        AiPageQuery query = new AiPageQuery();
        query.setCreatedDate("2026/08/05");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.pageTasks(query));

        assertEquals("创建日期格式不合法，应为 yyyy-MM-dd", exception.getMessage());
        verify(aiTaskMapper, never()).selectPage(any(Page.class), any());
    }

    @Test
    /** 待处理预警不能跳过确认直接关闭。 */
    void handleRejectsFaultStatusTransitionOutsideWorkflow() {
        FaultReport report = new FaultReport();
        report.setId(20L);
        report.setProcessStatus("PENDING");
        when(faultReportMapper.selectById(20L)).thenReturn(report);
        ResultHandleRequest request = new ResultHandleRequest();
        request.setProcessStatus("CLOSED");
        request.setRemark("不允许直接关闭");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.handleFaultReport(20L, request));

        assertEquals("当前预警状态不允许执行该处理操作", exception.getMessage());
        verify(faultReportMapper, never()).updateById(any(FaultReport.class));
    }

    @Test
    /** 合法确认既要更新预警，也要追加处理历史记录。 */
    void handleAllowsPendingAlertToBeConfirmedAndRecordsHistory() {
        FaultReport report = new FaultReport();
        report.setId(21L);
        report.setReportCode("FAULT-21");
        report.setProcessStatus("PENDING");
        when(faultReportMapper.selectById(21L)).thenReturn(report);
        ResultHandleRequest request = new ResultHandleRequest();
        request.setProcessStatus("CONFIRMED");
        request.setRemark("已确认，安排排查");

        service.handleFaultReport(21L, request);

        assertEquals("CONFIRMED", report.getProcessStatus());
        verify(faultReportMapper).updateById(report);
        verify(resultStatusLogMapper).insert(any(ResultStatusLog.class));
    }

    @Test
    /** 未选择报表时，导出请求必须被拒绝，避免生成无意义的空文件。 */
    void exportRejectsEmptyReportIds() {
        BusinessException exception = assertThrows(BusinessException.class,
            () -> service.exportDefectReports(List.of()));

        assertEquals("请至少选择一条缺陷报表", exception.getMessage());
        verify(defectReportMapper, never()).selectBatchIds(any());
    }

    @Test
    /** 导出前批量回填创建者，并验证 CSV 包含原型列表要求的核心字段。 */
    void exportIncludesCreatorAndReportFields() {
        DefectReport report = new DefectReport();
        report.setId(31L);
        report.setDefectCode("DEFECT-31");
        report.setTaskNameSnapshot("订单接口缺陷检测");
        report.setCreatedBy(9L);
        report.setCreatedAt(java.time.LocalDateTime.of(2026, 8, 13, 10, 30));
        report.setDefectProblem("接口响应超时");
        report.setProcessStatus("PENDING");
        SysUser user = new SysUser();
        user.setId(9L);
        user.setRealName("测试管理员");
        when(defectReportMapper.selectBatchIds(List.of(31L))).thenReturn(List.of(report));
        when(sysUserMapper.selectBatchIds(any())).thenReturn(List.of(user));

        String csv = new String(service.exportDefectReports(List.of(31L)), java.nio.charset.StandardCharsets.UTF_8);

        assertTrue(csv.contains("缺陷ID,缺陷检查任务名称,创建者"));
        assertTrue(csv.contains("DEFECT-31"));
        assertTrue(csv.contains("订单接口缺陷检测"));
        assertTrue(csv.contains("测试管理员"));
        assertTrue(csv.contains("接口响应超时"));
        verify(sysUserMapper).selectBatchIds(any());
    }

    @Test
    /** 性能优化任务的编码由服务端生成，前端无需也不应维护内部编码。 */
    void createGeneratesOptimizationTaskCodeOnServer() {
        AiTask task = optimizationTask();
        task.setTaskCode(null);
        task.setModelId(3L);
        BusinessSystem system = new BusinessSystem();
        system.setId(1L);
        system.setName("订单系统");
        when(aiModelMapper.selectById(3L)).thenReturn(enabledOptimizationModel());
        when(businessSystemMapper.selectById(1L)).thenReturn(system);
        doAnswer(invocation -> { invocation.getArgument(0, AiTask.class).setId(33L); return 1; }).when(aiTaskMapper).insert(any(AiTask.class));
        when(aiTaskMapper.selectById(33L)).thenReturn(task);

        service.createTask(task);

        assertTrue(task.getTaskCode().startsWith("OPT-TASK-"));
        verify(aiTaskMapper).insert(task);
    }

    @Test
    /** 优化引擎未接入时，人工执行只能生成等待处理记录，不能伪造成功结果。 */
    void executeCreatesPendingRecordForOptimization() {
        AiTask task = optimizationTask();
        task.setId(34L);
        when(aiTaskMapper.selectById(34L)).thenReturn(task);

        TaskExecRecord result = service.executeTask(34L, new TaskExecuteRequest());

        assertEquals("PENDING", result.getExecutionStatus());
        assertNull(result.getEndTime());
        verify(taskExecRecordMapper).insert(any(TaskExecRecord.class));
    }

    @Test
    /** 性能优化专用编辑请求不暴露内部任务编码，服务端必须保留原编码。 */
    void updateOptimizationTaskRetainsExistingCodeWhenClientOmitsIt() {
        AiTask existing = optimizationTask();
        existing.setId(35L);
        existing.setTaskCode("OPT-TASK-EXISTING");
        existing.setModelId(3L);
        AiTask request = optimizationTask();
        request.setTaskName("性能优化回归任务-已编辑");
        request.setModelId(3L);
        BusinessSystem system = new BusinessSystem();
        system.setId(1L);
        system.setName("订单系统");
        when(aiTaskMapper.selectById(35L)).thenReturn(existing);
        when(aiModelMapper.selectById(3L)).thenReturn(enabledOptimizationModel());
        when(businessSystemMapper.selectById(1L)).thenReturn(system);

        service.updateTask(35L, request);

        assertEquals("OPT-TASK-EXISTING", existing.getTaskCode());
        verify(aiTaskMapper).updateById(existing);
    }

    private AiTask faultTask() {
        AiTask task = new AiTask();
        task.setTaskCode("FAULT-TASK-TEST");
        task.setTaskName("故障预测回归任务");
        task.setTaskType("FAULT_PREDICTION");
        task.setTargetType("BUSINESS_SYSTEM");
        task.setTargetId(1L);
        task.setMetricCodes("CPU,内存");
        task.setScheduleRule("[{\"time\":\"08:00\",\"repeatType\":\"daily\",\"weekDays\":[],\"predictionWindowHours\":4}]");
        task.setStatus("ENABLED");
        return task;
    }

    private AiModel enabledFaultModel() {
        AiModel model = new AiModel();
        model.setId(1L);
        model.setModelType("FAULT_PREDICTION");
        model.setStatus("ENABLED");
        return model;
    }

    private AiTask defectTask() {
        AiTask task = new AiTask();
        task.setTaskCode("DEFECT-TASK-TEST");
        task.setTaskName("缺陷检测回归任务");
        task.setTaskType("DEFECT_DETECTION");
        task.setTargetType("BUSINESS_SYSTEM");
        task.setTargetId(1L);
        task.setScheduleRule("[{\"time\":\"08:00\",\"repeatType\":\"daily\",\"weekDays\":[],\"detectionRange\":\"PREVIOUS_DAY\"}]");
        task.setStatus("ENABLED");
        return task;
    }

    private AiModel enabledDefectModel() {
        AiModel model = new AiModel();
        model.setId(2L);
        model.setModelType("DEFECT_DETECTION");
        model.setStatus("ENABLED");
        return model;
    }

    private AiTask optimizationTask() {
        AiTask task = new AiTask();
        task.setTaskName("性能优化回归任务");
        task.setTaskType("PERFORMANCE_OPTIMIZATION");
        task.setTargetType("BUSINESS_SYSTEM");
        task.setTargetId(1L);
        task.setScheduleRule("[{\"time\":\"08:00\",\"repeatType\":\"daily\",\"weekDays\":[],\"detectionRange\":\"PREVIOUS_DAY\"}]");
        task.setStatus("ENABLED");
        return task;
    }

    private AiModel enabledOptimizationModel() {
        AiModel model = new AiModel();
        model.setId(3L);
        model.setModelType("PERFORMANCE_OPTIMIZATION");
        model.setStatus("ENABLED");
        return model;
    }
}
