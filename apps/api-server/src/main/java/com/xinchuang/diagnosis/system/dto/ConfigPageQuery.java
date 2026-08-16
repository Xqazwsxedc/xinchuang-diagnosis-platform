package com.xinchuang.diagnosis.system.dto;

import com.xinchuang.diagnosis.common.query.PageQuery;

public class ConfigPageQuery extends PageQuery {
    private String configKey;
    private String configName;
    private String configType;
    private String status;

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
}
