package com.xinchuang.diagnosis.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.security.LoginUser;
import com.xinchuang.diagnosis.security.UserContext;
import com.xinchuang.diagnosis.system.dto.ChangePasswordRequest;
import com.xinchuang.diagnosis.system.dto.ResetPasswordRequest;
import com.xinchuang.diagnosis.system.dto.ProfileUpdateRequest;
import com.xinchuang.diagnosis.system.dto.UserCreateRequest;
import com.xinchuang.diagnosis.system.dto.UserPageQuery;
import com.xinchuang.diagnosis.system.dto.UserStatusRequest;
import com.xinchuang.diagnosis.system.dto.UserUpdateRequest;
import com.xinchuang.diagnosis.system.entity.SysRole;
import com.xinchuang.diagnosis.system.entity.SysUser;
import com.xinchuang.diagnosis.system.entity.SysUserRole;
import com.xinchuang.diagnosis.system.mapper.SysRoleMapper;
import com.xinchuang.diagnosis.system.mapper.SysUserMapper;
import com.xinchuang.diagnosis.system.mapper.SysUserRoleMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.service.NotificationService;
import com.xinchuang.diagnosis.system.service.SysUserService;
import com.xinchuang.diagnosis.system.dto.NotificationCreateCommand;
import com.xinchuang.diagnosis.system.vo.RoleVO;
import com.xinchuang.diagnosis.system.vo.UserVO;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class SysUserServiceImpl implements SysUserService {
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public SysUserServiceImpl(SysUserMapper sysUserMapper,
                              SysRoleMapper sysRoleMapper,
                              SysUserRoleMapper sysUserRoleMapper,
                              BCryptPasswordEncoder passwordEncoder,
                              AuditLogService auditLogService,
                              NotificationService notificationService) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Override
    public PageResponse<UserVO> page(UserPageQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
                .or()
                .like(SysUser::getRealName, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getUsername())) {
            wrapper.like(SysUser::getUsername, query.getUsername());
        }
        if (StringUtils.hasText(query.getRealName())) {
            wrapper.like(SysUser::getRealName, query.getRealName());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysUser::getUpdatedAt);
        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        return PageResponse.of(toVOs(page.getRecords()), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public UserVO detail(Long id) {
        SysUser user = getById(id);
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO create(UserCreateRequest request) {
        ensureUsernameUnique(request.getUsername(), null);
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setDepartment(request.getDepartment());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());
        user.setRemark(request.getRemark());
        sysUserMapper.insert(user);
        assignRoles(user.getId(), request.getRoleIds());
        auditLogService.recordSuccess("用户管理", "CREATE", "SYS_USER", user.getId(), "新增用户：" + user.getUsername());
        return detail(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO update(Long id, UserUpdateRequest request) {
        SysUser user = getById(id);
        user.setRealName(request.getRealName());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setDepartment(request.getDepartment());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());
        user.setRemark(request.getRemark());
        sysUserMapper.updateById(user);
        assignRoles(id, request.getRoleIds());
        auditLogService.recordSuccess("用户管理", "UPDATE", "SYS_USER", id, "编辑用户：" + user.getUsername());
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, UserStatusRequest request) {
        SysUser user = getById(id);
        user.setStatus(request.getStatus());
        sysUserMapper.updateById(user);
        auditLogService.recordSuccess("用户管理", "STATUS", "SYS_USER", id, "更新用户状态：" + user.getUsername() + " -> " + request.getStatus());
        if ("DISABLED".equals(request.getStatus())) {
            notifyUser(user.getId(), "账号已被禁用", "您的账号「" + user.getUsername() + "」已被管理员禁用，如有疑问请联系管理员。", "SECURITY", "HIGH", "SYS_USER", user.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, ResetPasswordRequest request) {
        SysUser user = getById(id);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        sysUserMapper.updateById(user);
        auditLogService.recordSuccess("用户管理", "RESET_PASSWORD", "SYS_USER", id, "重置用户密码：" + user.getUsername());
        notifyUser(user.getId(), "密码已被重置", "您的账号「" + user.getUsername() + "」密码已被管理员重置，请及时登录后修改密码。", "SECURITY", "NORMAL", "SYS_USER", user.getId());
    }

    @Override
    public UserVO currentProfile() {
        return detail(currentUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateCurrentProfile(ProfileUpdateRequest request) {
        Long userId = currentUserId();
        SysUser user = getById(userId);
        user.setRealName(request.getRealName());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setDepartment(request.getDepartment());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        sysUserMapper.updateById(user);
        auditLogService.recordSuccess("个人中心", "UPDATE", "SYS_USER", userId, "更新个人资料：" + user.getUsername());
        return detail(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeCurrentPassword(ChangePasswordRequest request) {
        Long userId = currentUserId();
        SysUser user = getById(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("原密码不正确");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("新密码不能与原密码一致");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(user);
        auditLogService.recordSuccess("个人中心", "CHANGE_PASSWORD", "SYS_USER", userId, "修改个人密码：" + user.getUsername());
        notifyUser(userId, "密码已修改", "您的账号「" + user.getUsername() + "」密码已修改。若非本人操作，请立即联系管理员。", "SECURITY", "HIGH", "SYS_USER", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysUser user = getById(id);
        sysUserMapper.deleteById(id);
        auditLogService.recordSuccess("用户管理", "DELETE", "SYS_USER", id, "删除用户：" + user.getUsername());
    }

    @Override
    public List<RoleVO> roles() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getStatus, "ENABLED")
            .orderByAsc(SysRole::getRoleCode)
        ).stream().map(this::toRoleVO).toList();
    }

    @Override
    public SysUser getEnabledUserById(Long id) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getId, id)
            .eq(SysUser::getStatus, "ENABLED"));
    }

    @Override
    public List<String> getRoleCodes(Long userId) {
        List<Long> roleIds = getRoleIds(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
            .in(SysRole::getId, roleIds)
            .eq(SysRole::getStatus, "ENABLED")
        ).stream().map(SysRole::getRoleCode).toList();
    }

    private SysUser getById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private Long currentUserId() {
        LoginUser loginUser = UserContext.get();
        if (loginUser == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return loginUser.getUserId();
    }

    private void ensureUsernameUnique(String username, Long currentId) {
        SysUser existing = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (existing != null && !Objects.equals(existing.getId(), currentId)) {
            throw new BusinessException("用户名已存在");
        }
    }

    private void assignRoles(Long userId, List<Long> roleIds) {
        Set<Long> normalizedRoleIds = roleIds == null
            ? Collections.emptySet()
            : roleIds.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        if (CollectionUtils.isEmpty(normalizedRoleIds)) {
            sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
            return;
        }
        List<SysRole> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
            .in(SysRole::getId, normalizedRoleIds)
            .eq(SysRole::getStatus, "ENABLED"));
        if (roles.size() != normalizedRoleIds.size()) {
            throw new BusinessException("包含无效角色");
        }
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        for (Long roleId : normalizedRoleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
        }
    }

    private void notifyUser(Long userId, String title, String content, String noticeType, String priority, String businessType, Long businessId) {
        NotificationCreateCommand command = new NotificationCreateCommand();
        command.setReceiverId(userId);
        command.setTitle(title);
        command.setContent(content);
        command.setNoticeType(noticeType);
        command.setPriority(priority);
        command.setBusinessType(businessType);
        command.setBusinessId(businessId);
        notificationService.create(command);
    }

    private List<Long> getRoleIds(Long userId) {
        return sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
            .stream()
            .map(SysUserRole::getRoleId)
            .toList();
    }

    private List<UserVO> toVOs(List<SysUser> users) {
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        Set<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toSet());
        Map<Long, List<Long>> roleIdsByUserId = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds))
            .stream()
            .collect(Collectors.groupingBy(SysUserRole::getUserId,
                Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));
        Set<Long> roleIds = roleIdsByUserId.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toSet());
        Map<Long, SysRole> rolesById = roleIds.isEmpty()
            ? Collections.emptyMap()
            : sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds))
                .stream()
                .collect(Collectors.toMap(SysRole::getId, role -> role));
        return users.stream()
            .map(user -> toVO(user, roleIdsByUserId.getOrDefault(user.getId(), Collections.emptyList()), rolesById))
            .toList();
    }

    private UserVO toVO(SysUser user) {
        List<Long> roleIds = getRoleIds(user.getId());
        Map<Long, SysRole> rolesById = roleIds.isEmpty()
            ? Collections.emptyMap()
            : sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds))
                .stream()
                .collect(Collectors.toMap(SysRole::getId, role -> role));
        return toVO(user, roleIds, rolesById);
    }

    private UserVO toVO(SysUser user, List<Long> roleIds, Map<Long, SysRole> rolesById) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setDepartment(user.getDepartment());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setRemark(user.getRemark());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        vo.setRoleIds(roleIds);
        if (!CollectionUtils.isEmpty(roleIds)) {
            vo.setRoles(roleIds.stream()
                .map(rolesById::get)
                .filter(Objects::nonNull)
                .map(this::toRoleVO)
                .toList());
        }
        return vo;
    }

    private RoleVO toRoleVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDataScope(role.getDataScope());
        vo.setDataScopeConfig(role.getDataScopeConfig());
        vo.setStatus(role.getStatus());
        vo.setRemark(role.getRemark());
        return vo;
    }
}
