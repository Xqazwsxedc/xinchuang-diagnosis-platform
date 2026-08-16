package com.xinchuang.diagnosis.system.service;

import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.system.dto.RolePageQuery;
import com.xinchuang.diagnosis.system.dto.RoleSaveRequest;
import com.xinchuang.diagnosis.system.dto.RoleStatusRequest;
import com.xinchuang.diagnosis.system.vo.RoleVO;

public interface SysRoleService {
    PageResponse<RoleVO> page(RolePageQuery query);

    RoleVO detail(Long id);

    RoleVO create(RoleSaveRequest request);

    RoleVO update(Long id, RoleSaveRequest request);

    void updateStatus(Long id, RoleStatusRequest request);

    void delete(Long id);
}
