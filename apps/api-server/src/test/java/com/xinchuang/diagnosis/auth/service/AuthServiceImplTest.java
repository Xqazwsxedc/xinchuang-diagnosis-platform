package com.xinchuang.diagnosis.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private SysUserService sysUserService;
    @Mock
    private SysMenuService sysMenuService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(sysUserMapper, sysUserService, sysMenuService, auditLogService, passwordEncoder, jwtUtil);
    }

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void loginRecordsFailureWhenAccountDoesNotExist() {
        when(sysUserMapper.selectOne(any())).thenReturn(null);

        LoginRequest request = loginRequest("unknown", "password");
        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));

        assertEquals("账号或密码错误", exception.getMessage());
        verify(auditLogService).recordLogin(null, "unknown", "FAILED", "账号不存在");
    }

    @Test
    void loginRecordsFailureWhenPasswordIsWrong() {
        SysUser user = user("ENABLED");
        when(sysUserMapper.selectOne(any())).thenReturn(user);

        LoginRequest request = loginRequest("admin", "wrong");
        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));

        assertEquals("账号或密码错误", exception.getMessage());
        verify(auditLogService).recordLogin(1L, "admin", "FAILED", "密码错误");
    }

    @Test
    void loginRejectsDisabledUserAfterPasswordVerification() {
        SysUser user = user("DISABLED");
        when(sysUserMapper.selectOne(any())).thenReturn(user);

        LoginRequest request = loginRequest("admin", "password");
        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));

        assertEquals("用户已禁用，请联系管理员", exception.getMessage());
        verify(auditLogService).recordLogin(1L, "admin", "FAILED", "用户已禁用");
    }

    @Test
    void loginReturnsTokenAndCurrentPermissionsForEnabledUser() {
        SysUser user = user("ENABLED");
        when(sysUserMapper.selectOne(any())).thenReturn(user);
        when(jwtUtil.generateToken(1L, "admin")).thenReturn("test-token");
        when(sysUserService.getRoleCodes(1L)).thenReturn(List.of("admin"));
        when(sysMenuService.getUserMenuTree(1L)).thenReturn(List.of());
        when(sysMenuService.getUserPermissions(1L)).thenReturn(List.of("system:user:view"));

        LoginResponseVO response = authService.login(loginRequest("admin", "password"));

        assertEquals("test-token", response.getToken());
        assertEquals("admin", response.getUser().getUsername());
        assertEquals(List.of("system:user:view"), response.getUser().getPermissions());
        verify(auditLogService).recordLogin(1L, "admin", "SUCCESS", null);
    }

    @Test
    void currentUserRejectsMissingContext() {
        BusinessException exception = assertThrows(BusinessException.class, authService::currentUser);

        assertEquals(401, exception.getCode());
    }

    @Test
    void currentUserLoadsFreshUserData() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        UserContext.set(loginUser);
        when(sysUserService.getEnabledUserById(1L)).thenReturn(user("ENABLED"));
        when(sysUserService.getRoleCodes(1L)).thenReturn(List.of("admin"));
        when(sysMenuService.getUserMenuTree(1L)).thenReturn(List.of());
        when(sysMenuService.getUserPermissions(1L)).thenReturn(List.of("system:user:view"));

        LoginUserVO currentUser = authService.currentUser();

        assertNotNull(currentUser);
        assertEquals("admin", currentUser.getUsername());
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private SysUser user(String status) {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setRealName("系统管理员");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setStatus(status);
        return user;
    }
}
