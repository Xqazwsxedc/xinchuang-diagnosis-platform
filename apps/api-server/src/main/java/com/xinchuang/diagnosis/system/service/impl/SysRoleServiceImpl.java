package com.xinchuang.diagnosis.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.system.dto.RolePageQuery;
import com.xinchuang.diagnosis.system.dto.RoleSaveRequest;
import com.xinchuang.diagnosis.system.dto.RoleStatusRequest;
import com.xinchuang.diagnosis.system.entity.SysRole;
import com.xinchuang.diagnosis.system.entity.SysUserRole;
import com.xinchuang.diagnosis.system.mapper.SysRoleMapper;
import com.xinchuang.diagnosis.system.mapper.SysUserRoleMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.service.SysRoleService;
import com.xinchuang.diagnosis.system.vo.RoleVO;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SysRoleServiceImpl implements SysRoleService {
    private static final Logger log = LoggerFactory.getLogger(SysRoleServiceImpl.class);

    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final AuditLogService auditLogService;

    public SysRoleServiceImpl(SysRoleMapper sysRoleMapper,
                              SysUserRoleMapper sysUserRoleMapper,
                              AuditLogService auditLogService) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    public PageResponse<RoleVO> page(RolePageQuery query) {
        LambdaQueryWrapper<SysRole> wrapper = buildQuery(query);
        wrapper.orderByDesc(SysRole::getUpdatedAt);
        Page<SysRole> page = sysRoleMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(page.getRecords().stream().map(this::toVO).toList(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public RoleVO detail(Long id) {
        return toVO(getById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO create(RoleSaveRequest request) {
        ensureRoleCodeUnique(request.getRoleCode(), null);
        SysRole role = new SysRole();
        fill(role, request);
        sysRoleMapper.insert(role);
        log.info("role created, id={}, roleCode={}", role.getId(), role.getRoleCode());
        auditLogService.recordSuccess("角色管理", "CREATE", "SYS_ROLE", role.getId(), "新增角色：" + role.getRoleCode());
        return detail(role.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO update(Long id, RoleSaveRequest request) {
        SysRole role = getById(id);
        ensureRoleCodeUnique(request.getRoleCode(), id);
        fill(role, request);
        sysRoleMapper.updateById(role);
        log.info("role updated, id={}, roleCode={}", role.getId(), role.getRoleCode());
        auditLogService.recordSuccess("角色管理", "UPDATE", "SYS_ROLE", id, "编辑角色：" + role.getRoleCode());
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, RoleStatusRequest request) {
        SysRole role = getById(id);
        role.setStatus(request.getStatus());
        sysRoleMapper.updateById(role);
        log.info("role status updated, id={}, status={}", id, request.getStatus());
        auditLogService.recordSuccess("角色管理", "STATUS", "SYS_ROLE", id, "更新角色状态：" + role.getRoleCode() + " -> " + request.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysRole role = getById(id);
        Long count = sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        if (count != null && count > 0) {
            throw new BusinessException("该角色已分配给用户，不能删除");
        }
        sysRoleMapper.deleteById(id);
        log.info("role deleted, id={}", id);
        auditLogService.recordSuccess("角色管理", "DELETE", "SYS_ROLE", id, "删除角色：" + role.getRoleCode());
    }

    private LambdaQueryWrapper<SysRole> buildQuery(RolePageQuery query) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, query.getKeyword())
                .or()
                .like(SysRole::getRoleName, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getRoleCode())) {
            wrapper.like(SysRole::getRoleCode, query.getRoleCode());
        }
        if (StringUtils.hasText(query.getRoleName())) {
            wrapper.like(SysRole::getRoleName, query.getRoleName());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(SysRole::getStatus, query.getStatus());
        }
        return wrapper;
    }

    private SysRole getById(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    private void ensureRoleCodeUnique(String roleCode, Long currentId) {
        SysRole existing = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode));
        if (existing != null && !Objects.equals(existing.getId(), currentId)) {
            throw new BusinessException("角色编码已存在");
        }
    }

    private void fill(SysRole role, RoleSaveRequest request) {
        if ("CUSTOM".equals(request.getDataScope()) && !StringUtils.hasText(request.getDataScopeConfig())) {
            throw new BusinessException("自定义数据权限必须填写数据范围");
        }
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDataScope(request.getDataScope());
        role.setDataScopeConfig("CUSTOM".equals(request.getDataScope()) ? request.getDataScopeConfig() : null);
        role.setStatus(request.getStatus());
        role.setRemark(request.getRemark());
    }

    private RoleVO toVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDataScope(role.getDataScope());
        vo.setDataScopeConfig(role.getDataScopeConfig());
        vo.setStatus(role.getStatus());
        vo.setRemark(role.getRemark());
        vo.setCreatedAt(role.getCreatedAt());
        vo.setUpdatedAt(role.getUpdatedAt());
        return vo;
    }
}
