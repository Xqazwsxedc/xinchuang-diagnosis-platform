package com.xinchuang.diagnosis.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class FaultTaskSaveRequest {
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 150, message = "任务名称不能超过150个字符")
    private String taskName;
    @NotNull(message = "请选择AI模型")
    private Long modelId;
    @NotBlank(message = "目标类型不能为空")
    @Pattern(regexp = "BUSINESS_SYSTEM|DB_RESOURCE", message = "目标类型不合法")
    private String targetType;
    @NotNull(message = "请选择预测对象")
    private Long targetId;
    @NotEmpty(message = "请至少选择一个预测指标")
    @Size(max = 22, message = "预测指标数量不合法")
    private List<@NotBlank(message = "预测指标不能为空") String> metricCodes = new ArrayList<>();
    @NotEmpty(message = "请至少配置一条定时预测规则")
    @Valid
    private List<FaultTaskScheduleRule> schedules = new ArrayList<>();

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public Long getModelId() { return modelId; }
    public void setModelId(Long modelId) { this.modelId = modelId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public List<String> getMetricCodes() { return metricCodes; }
    public void setMetricCodes(List<String> metricCodes) { this.metricCodes = metricCodes; }
    public List<FaultTaskScheduleRule> getSchedules() { return schedules; }
    public void setSchedules(List<FaultTaskScheduleRule> schedules) { this.schedules = schedules; }

    public static class FaultTaskScheduleRule {
        @NotBlank(message = "定时规则时间不能为空")
        @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "定时规则时间格式不合法")
        private String time;
        @NotBlank(message = "定时规则重复周期不能为空")
        @Pattern(regexp = "daily|weekly", message = "定时规则重复周期不合法")
        private String repeatType;
        private List<String> weekDays = new ArrayList<>();
        @NotNull(message = "请选择预测时间范围")
        // 兼容早期已保存的演示规则；未携带范围时按原型默认的 4 小时处理。
        private Integer predictionWindowHours = 4;

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public String getRepeatType() { return repeatType; }
        public void setRepeatType(String repeatType) { this.repeatType = repeatType; }
        public List<String> getWeekDays() { return weekDays; }
        public void setWeekDays(List<String> weekDays) { this.weekDays = weekDays; }
        public Integer getPredictionWindowHours() { return predictionWindowHours; }
        public void setPredictionWindowHours(Integer predictionWindowHours) { this.predictionWindowHours = predictionWindowHours; }
    }
}
