package com.xinchuang.diagnosis.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RoleSaveRequest {
    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_:-]*$", message = "角色编码需以字母开头，只能包含字母、数字、下划线、冒号和短横线")
    private String roleCode;

    @NotBlank
    @Size(max = 64)
    private String roleName;

    @NotBlank
    @Pattern(regexp = "ALL|DEPARTMENT|SELF|CUSTOM", message = "数据权限只能是 ALL/DEPARTMENT/SELF/CUSTOM")
    private String dataScope = "ALL";

    @Size(max = 500)
    private String dataScopeConfig;

    @NotBlank
    @Pattern(regexp = "ENABLED|DISABLED", message = "状态只能是 ENABLED/DISABLED")
    private String status;

    @Size(max = 500)
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
