package com.xinchuang.diagnosis.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xinchuang.diagnosis.system.entity.SysRole;
import com.xinchuang.diagnosis.system.entity.SysConfig;
import com.xinchuang.diagnosis.system.entity.SysUser;
import com.xinchuang.diagnosis.system.entity.SysUserRole;
import com.xinchuang.diagnosis.system.mapper.SysConfigMapper;
import com.xinchuang.diagnosis.system.mapper.SysDictItemMapper;
import com.xinchuang.diagnosis.system.mapper.SysDictTypeMapper;
import com.xinchuang.diagnosis.system.mapper.SysMenuMapper;
import com.xinchuang.diagnosis.system.mapper.SysRoleMapper;
import com.xinchuang.diagnosis.system.mapper.SysRoleMenuMapper;
import com.xinchuang.diagnosis.system.mapper.SysUserMapper;
import com.xinchuang.diagnosis.system.mapper.SysUserRoleMapper;
import com.xinchuang.diagnosis.system.mapper.SystemNotificationMapper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SystemBootstrapInitializerTest {
    private final SysUserMapper sysUserMapper = org.mockito.Mockito.mock(SysUserMapper.class);
    private final SysUserRoleMapper sysUserRoleMapper = org.mockito.Mockito.mock(SysUserRoleMapper.class);
    private final SysConfigMapper sysConfigMapper = org.mockito.Mockito.mock(SysConfigMapper.class);

    @Test
    void existingAdministratorIsNeverResetOrReenabled() throws Exception {
        SysUser existing = new SysUser();
        existing.setId(1L);
        existing.setStatus("DISABLED");
        existing.setPasswordHash(new BCryptPasswordEncoder().encode("user-selected-password"));
        when(sysUserMapper.selectOne(any())).thenReturn(existing);

        SysUser result = invokeEnsureBootstrapAdmin(initializer(false, ""), null);

        assertEquals(existing, result);
        verify(sysUserMapper, never()).insert(any(SysUser.class));
        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    void enabledBootstrapCreatesAdministratorFromEnvironmentPassword() throws Exception {
        when(sysUserMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(99L);
            return 1;
        }).when(sysUserMapper).insert(any(SysUser.class));
        SysRole adminRole = new SysRole();
        adminRole.setId(10L);

        SysUser result = invokeEnsureBootstrapAdmin(initializer(true, "strong-bootstrap-password"), adminRole);

        assertEquals("admin", result.getUsername());
        assertEquals("ENABLED", result.getStatus());
        verify(sysUserRoleMapper).insert(any(SysUserRole.class));
    }

    @Test
    void enabledBootstrapRejectsMissingPassword() throws Exception {
        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
            () -> ensureBootstrapAdminMethod(initializer(true, "")).invoke(initializer(true, ""), new Object[] {null}));

        assertEquals(IllegalStateException.class, exception.getCause().getClass());
    }

    @Test
    void disabledBootstrapDoesNotCreateAdministrator() throws Exception {
        when(sysUserMapper.selectOne(any())).thenReturn(null);

        SysUser result = invokeEnsureBootstrapAdmin(initializer(false, ""), null);

        assertNull(result);
        verify(sysUserMapper, never()).insert(any(SysUser.class));
    }

    @Test
    void clearsLegacyDefaultPasswordConfiguration() throws Exception {
        SysConfig legacyDefaultPassword = new SysConfig();
        legacyDefaultPassword.setConfigKey("DEFAULT_PASSWORD");
        legacyDefaultPassword.setConfigValue("password");
        when(sysConfigMapper.selectOne(any())).thenReturn(legacyDefaultPassword);

        invokeEnsureConfigs(initializer(false, ""));

        assertEquals("", legacyDefaultPassword.getConfigValue());
        verify(sysConfigMapper).updateById(legacyDefaultPassword);
    }

    @Test
    void disabledBootstrapSupportsConfiguredAdministratorName() throws Exception {
        when(sysUserMapper.selectOne(any())).thenReturn(null);

        SysUser result = invokeEnsureBootstrapAdmin(initializer(false, "", "platform-admin"), null);

        assertNull(result);
        verify(sysUserMapper, never()).insert(any(SysUser.class));
    }

    @Test
    void createsMissingSystemConfigurationsWithoutDefaultPassword() throws Exception {
        when(sysConfigMapper.selectOne(any())).thenReturn(null);

        invokeEnsureConfigs(initializer(false, ""));

        verify(sysConfigMapper, org.mockito.Mockito.times(4)).insert(any(SysConfig.class));
    }

    private SystemBootstrapInitializer initializer(boolean enabled, String password) {
        return initializer(enabled, password, "");
    }

    private SystemBootstrapInitializer initializer(boolean enabled, String password, String username) {
        return new SystemBootstrapInitializer(
            sysUserMapper,
            org.mockito.Mockito.mock(SysRoleMapper.class),
            sysUserRoleMapper,
            org.mockito.Mockito.mock(SysMenuMapper.class),
            org.mockito.Mockito.mock(SysRoleMenuMapper.class),
            org.mockito.Mockito.mock(SysDictTypeMapper.class),
            org.mockito.Mockito.mock(SysDictItemMapper.class),
            sysConfigMapper,
            org.mockito.Mockito.mock(SystemNotificationMapper.class),
            new BCryptPasswordEncoder(),
            enabled,
            username,
            password
        );
    }

    private SysUser invokeEnsureBootstrapAdmin(SystemBootstrapInitializer initializer, SysRole role) throws Exception {
        return (SysUser) ensureBootstrapAdminMethod(initializer).invoke(initializer, role);
    }

    private Method ensureBootstrapAdminMethod(SystemBootstrapInitializer initializer) throws NoSuchMethodException {
        Method method = initializer.getClass().getDeclaredMethod("ensureBootstrapAdmin", SysRole.class);
        method.setAccessible(true);
        return method;
    }

    private void invokeEnsureConfigs(SystemBootstrapInitializer initializer) throws Exception {
        Method method = initializer.getClass().getDeclaredMethod("ensureConfigs");
        method.setAccessible(true);
        method.invoke(initializer);
    }
}
