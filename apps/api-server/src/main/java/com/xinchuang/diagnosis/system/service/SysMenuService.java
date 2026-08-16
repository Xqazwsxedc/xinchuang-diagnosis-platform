package com.xinchuang.diagnosis.system.service;

import com.xinchuang.diagnosis.system.dto.MenuSaveRequest;
import com.xinchuang.diagnosis.system.dto.MenuStatusRequest;
import com.xinchuang.diagnosis.system.vo.MenuVO;
import java.util.List;

public interface SysMenuService {
    List<MenuVO> tree();

    MenuVO detail(Long id);

    MenuVO create(MenuSaveRequest request);

    MenuVO update(Long id, MenuSaveRequest request);

    void updateStatus(Long id, MenuStatusRequest request);

    void delete(Long id);

    List<Long> getRoleMenuIds(Long roleId);

    void assignRoleMenus(Long roleId, List<Long> menuIds);

    List<MenuVO> getUserMenuTree(Long userId);

    List<String> getUserPermissions(Long userId);

    boolean hasPermission(Long userId, String permissionCode);
}
