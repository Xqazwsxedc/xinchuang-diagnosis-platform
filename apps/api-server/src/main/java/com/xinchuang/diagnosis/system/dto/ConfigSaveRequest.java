package com.xinchuang.diagnosis.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ConfigSaveRequest {
    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键最多100个字符")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "配置键需使用大写字母、数字和下划线，并以字母开头")
    private String configKey;

    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称最多100个字符")
    private String configName;

    @Size(max = 1000, message = "配置值最多1000个字符")
    private String configValue;

    @NotBlank(message = "配置类型不能为空")
    @Pattern(regexp = "TEXT|NUMBER|BOOLEAN|JSON", message = "配置类型只能是 TEXT/NUMBER/BOOLEAN/JSON")
    private String configType;

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "ENABLED|DISABLED", message = "状态只能是 ENABLED/DISABLED")
    private String status;

    @Size(max = 500, message = "备注最多500个字符")
    private String remark;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
