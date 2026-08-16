package com.xinchuang.diagnosis.ai.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 2.1 validation regression suite for the stable AI-facing request contracts.
 * The cases intentionally exercise invalid client data without invoking an AI engine.
 */
class AiRequestValidationV21Test {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    /** 故障预测对象当前只允许业务系统和分布式数据库两种类型。 */
    void faultTaskRejectsUnsupportedTargetType() {
        FaultTaskSaveRequest request = validTask();
        request.setTargetType("HOST");

        assertFalse(validator.validateProperty(request, "targetType").isEmpty());
    }

    @Test
    /** 页面固定提供 22 项指标，超过数量的请求必须在接口入口拒绝。 */
    void faultTaskRejectsMoreThanTwentyTwoMetrics() {
        FaultTaskSaveRequest request = validTask();
        request.setMetricCodes(java.util.Collections.nCopies(23, "CPU_USAGE"));

        assertFalse(validator.validateProperty(request, "metricCodes").isEmpty());
    }

    @Test
    /** 嵌套集合校验必须阻止空白指标进入 AI 引擎契约。 */
    void faultTaskRejectsBlankMetricEntry() {
        FaultTaskSaveRequest request = validTask();
        request.setMetricCodes(List.of("CPU_USAGE", " "));

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    /** 定时规则时间必须是有效的 24 小时制 HH:mm 格式。 */
    void nestedScheduleRejectsInvalidTime() {
        FaultTaskSaveRequest request = validTask();
        request.getSchedules().get(0).setTime("25:99");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    /** 当前持久化 JSON 契约只支持每日和每周两种重复规则。 */
    void nestedScheduleRejectsUnsupportedRepeatType() {
        FaultTaskSaveRequest request = validTask();
        request.getSchedules().get(0).setRepeatType("monthly");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    /** 未来 AI 引擎只能更新契约明确允许的执行状态。 */
    void engineUpdateRejectsUnsupportedExecutionStatus() {
        FaultTaskExecutionUpdateRequest request = new FaultTaskExecutionUpdateRequest();
        request.setExecutionStatus("PENDING");
        request.setStartTime(LocalDateTime.now());

        assertFalse(validator.validateProperty(request, "executionStatus").isEmpty());
    }

    @Test
    /** 预警处理必须使用与服务端状态机一致的状态枚举。 */
    void alertHandleRejectsUnsupportedStatus() {
        ResultHandleRequest request = new ResultHandleRequest();
        request.setProcessStatus("IGNORED");

        assertFalse(validator.validateProperty(request, "processStatus").isEmpty());
    }

    private FaultTaskSaveRequest validTask() {
        FaultTaskSaveRequest request = new FaultTaskSaveRequest();
        request.setTaskName("2.1 回归任务");
        request.setModelId(1L);
        request.setTargetType("BUSINESS_SYSTEM");
        request.setTargetId(1L);
        request.setMetricCodes(List.of("CPU_USAGE"));
        FaultTaskSaveRequest.FaultTaskScheduleRule schedule = new FaultTaskSaveRequest.FaultTaskScheduleRule();
        schedule.setTime("08:00");
        schedule.setRepeatType("daily");
        schedule.setPredictionWindowHours(4);
        request.setSchedules(List.of(schedule));
        return request;
    }
}
