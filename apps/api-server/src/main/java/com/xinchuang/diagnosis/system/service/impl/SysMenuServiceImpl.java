package com.xinchuang.diagnosis.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.system.dto.MenuSaveRequest;
import com.xinchuang.diagnosis.system.dto.MenuStatusRequest;
import com.xinchuang.diagnosis.system.entity.SysMenu;
import com.xinchuang.diagnosis.system.entity.SysRole;
import com.xinchuang.diagnosis.system.entity.SysRoleMenu;
import com.xinchuang.diagnosis.system.entity.SysUserRole;
import com.xinchuang.diagnosis.system.mapper.SysMenuMapper;
import com.xinchuang.diagnosis.system.mapper.SysRoleMapper;
import com.xinchuang.diagnosis.system.mapper.SysRoleMenuMapper;
import com.xinchuang.diagnosis.system.mapper.SysUserRoleMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.service.SysMenuService;
import com.xinchuang.diagnosis.system.vo.MenuVO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class SysMenuServiceImpl implements SysMenuService {
    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final AuditLogService auditLogService;

    public SysMenuServiceImpl(SysMenuMapper sysMenuMapper,
                              SysRoleMapper sysRoleMapper,
                              SysRoleMenuMapper sysRoleMenuMapper,
                              SysUserRoleMapper sysUserRoleMapper,
                              AuditLogService auditLogService) {
        this.sysMenuMapper = sysMenuMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<MenuVO> tree() {
        return buildTree(sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder)));
    }

    @Override
    public MenuVO detail(Long id) {
        return toVO(getById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuVO create(MenuSaveRequest request) {
        ensureValidParent(null, request.getParentId());
        ensurePermissionUnique(request.getPermissionCode(), null);
        SysMenu menu = new SysMenu();
        fill(menu, request);
        sysMenuMapper.insert(menu);
        auditLogService.recordSuccess("菜单权限管理", "CREATE", "SYS_MENU", menu.getId(), "新增菜单权限：" + menu.getMenuName());
        return detail(menu.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuVO update(Long id, MenuSaveRequest request) {
        SysMenu menu = getById(id);
        ensureValidParent(id, request.getParentId());
        ensurePermissionUnique(request.getPermissionCode(), id);
        fill(menu, request);
        sysMenuMapper.updateById(menu);
        auditLogService.recordSuccess("菜单权限管理", "UPDATE", "SYS_MENU", id, "编辑菜单权限：" + menu.getMenuName());
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, MenuStatusRequest request) {
        SysMenu menu = getById(id);
        menu.setStatus(request.getStatus());
        sysMenuMapper.updateById(menu);
        auditLogService.recordSuccess("菜单权限管理", "STATUS", "SYS_MENU", id, "更新菜单权限状态：" + menu.getMenuName() + " -> " + request.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysMenu menu = getById(id);
        Long childCount = sysMenuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BusinessException("该菜单存在子节点，不能删除");
        }
        Long roleCount = sysRoleMenuMapper.selectCount(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, id));
        if (roleCount != null && roleCount > 0) {
            throw new BusinessException("该菜单已分配给角色，不能删除");
        }
        sysMenuMapper.deleteById(id);
        auditLogService.recordSuccess("菜单权限管理", "DELETE", "SYS_MENU", id, "删除菜单权限：" + menu.getMenuName());
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        ensureRoleExists(roleId);
        return sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId))
            .stream()
            .map(SysRoleMenu::getMenuId)
            .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleMenus(Long roleId, List<Long> menuIds) {
        ensureRoleExists(roleId);
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (CollectionUtils.isEmpty(menuIds)) {
            return;
        }
        LinkedHashSet<Long> normalizedMenuIds = includeAncestorMenuIds(menuIds);
        List<SysMenu> menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>().in(SysMenu::getId, normalizedMenuIds));
        if (menus.size() != normalizedMenuIds.size()) {
            throw new BusinessException("包含无效菜单权限");
        }
        for (Long menuId : normalizedMenuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            sysRoleMenuMapper.insert(roleMenu);
        }
        auditLogService.recordSuccess("角色管理", "ASSIGN_PERMISSION", "SYS_ROLE", roleId, "分配角色菜单权限，权限数量：" + normalizedMenuIds.size());
    }

    @Override
    public List<MenuVO> getUserMenuTree(Long userId) {
        List<SysMenu> menus = getUserMenus(userId).stream()
            .filter(menu -> "MENU".equals(menu.getMenuType()))
            .filter(menu -> Integer.valueOf(1).equals(menu.getVisible()))
            .filter(menu -> "ENABLED".equals(menu.getStatus()))
            .toList();
        return buildTree(menus);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        return getUserMenus(userId).stream()
            .filter(menu -> "ENABLED".equals(menu.getStatus()))
            .map(SysMenu::getPermissionCode)
            .filter(StringUtils::hasText)
            .distinct()
            .toList();
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (!StringUtils.hasText(permissionCode)) {
            return true;
        }
        return getUserPermissions(userId).contains(permissionCode);
    }

    private List<SysMenu> getUserMenus(Long userId) {
        List<Long> roleIds = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
            .stream()
            .map(SysUserRole::getRoleId)
            .toList();
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }
        List<Long> menuIds = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds))
            .stream()
            .map(SysRoleMenu::getMenuId)
            .distinct()
            .toList();
        if (CollectionUtils.isEmpty(menuIds)) {
            return List.of();
        }
        LinkedHashSet<Long> normalizedMenuIds = includeAncestorMenuIds(menuIds);
        return sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>().in(SysMenu::getId, normalizedMenuIds).orderByAsc(SysMenu::getSortOrder));
    }

    private LinkedHashSet<Long> includeAncestorMenuIds(List<Long> menuIds) {
        LinkedHashSet<Long> normalizedMenuIds = new LinkedHashSet<>(menuIds);
        List<SysMenu> menus = sysMenuMapper.selectList(null);
        Map<Long, SysMenu> menuMap = menus.stream().collect(Collectors.toMap(SysMenu::getId, menu -> menu));
        List<Long> pendingIds = new ArrayList<>(normalizedMenuIds);
        for (int i = 0; i < pendingIds.size(); i++) {
            SysMenu menu = menuMap.get(pendingIds.get(i));
            if (menu == null || menu.getParentId() == null || menu.getParentId() == 0) {
                continue;
            }
            Long parentId = menu.getParentId();
            if (normalizedMenuIds.add(parentId)) {
                pendingIds.add(parentId);
            }
        }
        return normalizedMenuIds;
    }

    private List<MenuVO> buildTree(List<SysMenu> menus) {
        List<MenuVO> nodes = menus.stream()
            .sorted(Comparator.comparing(SysMenu::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
            .map(this::toVO)
            .toList();
        Map<Long, MenuVO> nodeMap = nodes.stream().collect(Collectors.toMap(MenuVO::getId, node -> node));
        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO node : nodes) {
            if (node.getParentId() == null || node.getParentId() == 0 || !nodeMap.containsKey(node.getParentId())) {
                roots.add(node);
            } else {
                nodeMap.get(node.getParentId()).getChildren().add(node);
            }
        }
        return roots;
    }

    private SysMenu getById(Long id) {
        SysMenu menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单权限不存在");
        }
        return menu;
    }

    private void ensureRoleExists(Long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
    }

    private void ensureValidParent(Long menuId, Long parentId) {
        if (parentId == null || parentId == 0) {
            return;
        }
        if (Objects.equals(menuId, parentId)) {
            throw new BusinessException("父级菜单不能选择自身");
        }
        SysMenu parent = sysMenuMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException("父级菜单不存在");
        }
        Set<Long> visitedIds = new HashSet<>();
        while (parent != null && parent.getParentId() != null && parent.getParentId() != 0) {
            if (!visitedIds.add(parent.getId())) {
                throw new BusinessException("菜单层级存在循环，不能保存");
            }
            if (Objects.equals(menuId, parent.getId())) {
                throw new BusinessException("父级菜单不能选择当前菜单的子节点");
            }
            parent = sysMenuMapper.selectById(parent.getParentId());
        }
    }

    private void ensurePermissionUnique(String permissionCode, Long currentId) {
        if (!StringUtils.hasText(permissionCode)) {
            return;
        }
        SysMenu existing = sysMenuMapper.selectOne(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getPermissionCode, permissionCode));
        if (existing != null && !Objects.equals(existing.getId(), currentId)) {
            throw new BusinessException("权限编码已存在");
        }
    }

    private void fill(SysMenu menu, MenuSaveRequest request) {
        menu.setParentId(request.getParentId());
        menu.setMenuType(request.getMenuType());
        menu.setMenuName(request.getMenuName());
        menu.setPermissionCode(request.getPermissionCode());
        menu.setRoutePath(request.getRoutePath());
        menu.setComponentPath(request.getComponentPath());
        menu.setIcon(request.getIcon());
        menu.setSortOrder(request.getSortOrder());
        menu.setVisible(request.getVisible());
        menu.setStatus(request.getStatus());
        menu.setRemark(request.getRemark());
    }

    private MenuVO toVO(SysMenu menu) {
        MenuVO vo = new MenuVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setMenuType(menu.getMenuType());
        vo.setMenuName(menu.getMenuName());
        vo.setPermissionCode(menu.getPermissionCode());
        vo.setRoutePath(menu.getRoutePath());
        vo.setComponentPath(menu.getComponentPath());
        vo.setIcon(menu.getIcon());
        vo.setSortOrder(menu.getSortOrder());
        vo.setVisible(menu.getVisible());
        vo.setStatus(menu.getStatus());
        vo.setRemark(menu.getRemark());
        vo.setCreatedAt(menu.getCreatedAt());
        vo.setUpdatedAt(menu.getUpdatedAt());
        return vo;
    }
}
