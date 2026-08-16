package com.xinchuang.diagnosis.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;

@TableName("sys_role")
public class SysRole extends BaseEntity {
    private String roleCode;
    private String roleName;
    private String dataScope;
    private String dataScopeConfig;
    private String status;
    private String remark;

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    public String getDataScopeConfig() {
        return dataScopeConfig;
    }

    public void setDataScopeConfig(String dataScopeConfig) {
        this.dataScopeConfig = dataScopeConfig;
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
