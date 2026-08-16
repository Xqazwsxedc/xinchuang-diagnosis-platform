package com.xinchuang.diagnosis.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinchuang.diagnosis.auth.dto.LoginRequest;
import com.xinchuang.diagnosis.auth.vo.LoginResponseVO;
import com.xinchuang.diagnosis.auth.vo.LoginUserVO;
import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.security.JwtUtil;
import com.xinchuang.diagnosis.security.LoginUser;
import com.xinchuang.diagnosis.security.UserContext;
import com.xinchuang.diagnosis.system.entity.SysUser;
import com.xinchuang.diagnosis.system.mapper.SysUserMapper;
import com.xinchuang.diagnosis.system.service.AuditLogService;
import com.xinchuang.diagnosis.system.service.SysMenuService;
import com.xinchuang.diagnosis.system.service.SysUserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final SysUserMapper sysUserMapper;
    private final SysUserService sysUserService;
    private final SysMenuService sysMenuService;
    private final AuditLogService auditLogService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(SysUserMapper sysUserMapper,
                           SysUserService sysUserService,
                           SysMenuService sysMenuService,
                           AuditLogService auditLogService,
                           BCryptPasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserService = sysUserService;
        this.sysMenuService = sysMenuService;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponseVO login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, request.getUsername()));
        if (user == null) {
            auditLogService.recordLogin(null, request.getUsername(), "FAILED", "账号不存在");
            throw new BusinessException("账号或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            auditLogService.recordLogin(user.getId(), request.getUsername(), "FAILED", "密码错误");
            throw new BusinessException("账号或密码错误");
        }
        if (!"ENABLED".equals(user.getStatus())) {
            auditLogService.recordLogin(user.getId(), request.getUsername(), "FAILED", "用户已禁用");
            throw new BusinessException("用户已禁用，请联系管理员");
        }
        auditLogService.recordLogin(user.getId(), request.getUsername(), "SUCCESS", null);
        LoginResponseVO response = new LoginResponseVO();
        response.setToken(jwtUtil.generateToken(user.getId(), user.getUsername()));
        response.setUser(toLoginUserVO(user));
        return response;
    }

    @Override
    public LoginUserVO currentUser() {
        LoginUser loginUser = UserContext.get();
        if (loginUser == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        SysUser user = sysUserService.getEnabledUserById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException(401, "登录用户不存在或已禁用");
        }
        return toLoginUserVO(user);
    }

    private LoginUserVO toLoginUserVO(SysUser user) {
        LoginUserVO vo = new LoginUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setDepartment(user.getDepartment());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setRoles(sysUserService.getRoleCodes(user.getId()));
        vo.setMenus(sysMenuService.getUserMenuTree(user.getId()));
        vo.setPermissions(sysMenuService.getUserPermissions(user.getId()));
        return vo;
    }
}
