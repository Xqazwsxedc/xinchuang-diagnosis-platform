package com.xinchuang.diagnosis.system.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class RoleMenuAssignRequest {
    @NotNull
    private List<Long> menuIds = Collections.emptyList();

    public List<Long> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }
}
