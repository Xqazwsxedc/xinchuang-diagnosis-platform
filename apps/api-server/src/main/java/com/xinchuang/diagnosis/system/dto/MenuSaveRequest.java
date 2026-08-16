package com.xinchuang.diagnosis.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class MenuSaveRequest {
    @NotNull
    private Long parentId;

    @NotBlank
    @Pattern(regexp = "MENU|BUTTON", message = "菜单类型只能是 MENU/BUTTON")
    private String menuType;

    @NotBlank
    @Size(max = 64)
    private String menuName;

    @Size(max = 128)
    private String permissionCode;

    @Size(max = 255)
    private String routePath;

    @Size(max = 255)
    private String componentPath;

    @Size(max = 64)
    private String icon;

    @NotNull
    private Integer sortOrder;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer visible;

    @NotBlank
    @Pattern(regexp = "ENABLED|DISABLED", message = "状态只能是 ENABLED/DISABLED")
    private String status;

    @Size(max = 500)
    private String remark;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getRoutePath() {
        return routePath;
    }

    public void setRoutePath(String routePath) {
        this.routePath = routePath;
    }

    public String getComponentPath() {
        return componentPath;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
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
