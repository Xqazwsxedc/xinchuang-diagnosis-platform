package com.xinchuang.diagnosis.security;

import com.xinchuang.diagnosis.common.exception.BusinessException;
import com.xinchuang.diagnosis.system.entity.SysUser;
import com.xinchuang.diagnosis.system.service.SysMenuService;
import com.xinchuang.diagnosis.system.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<PermissionRule> PERMISSION_RULES = List.of(
        new PermissionRule("GET", "/dashboard/**", "dashboard:view"),
        new PermissionRule("GET", "/ai/models/**", "ai:model:view"),
        new PermissionRule("POST", "/ai/models", "ai:model:create"),
        new PermissionRule("PUT", "/ai/models/*", "ai:model:update"),
        new PermissionRule("DELETE", "/ai/models/*", "ai:model:delete"),
        new PermissionRule("GET", "/ai/knowledge/**", "ai:knowledge:view"),
        new PermissionRule("POST", "/ai/knowledge", "ai:knowledge:create"),
        new PermissionRule("PUT", "/ai/knowledge/*", "ai:knowledge:update"),
        new PermissionRule("DELETE", "/ai/knowledge/*", "ai:knowledge:delete"),
        new PermissionRule("GET", "/ai/tasks/executions", "ai:task:view"),
        new PermissionRule("GET", "/ai/fault-prediction/tasks", "ai:task:view"),
        new PermissionRule("GET", "/ai/fault-prediction/tasks/**", "ai:task:view"),
        new PermissionRule("POST", "/ai/fault-prediction/engine/verify", "ai:task:execute"),
        new PermissionRule("POST", "/ai/fault-prediction/tasks/*/execute", "ai:task:execute"),
        new PermissionRule("POST", "/ai/fault-prediction/tasks", "ai:task:create"),
        new PermissionRule("PUT", "/ai/fault-prediction/tasks/*", "ai:task:update"),
        new PermissionRule("DELETE", "/ai/fault-prediction/tasks/*", "ai:task:delete"),
        new PermissionRule("GET", "/ai/tasks/**", "ai:task:view"),
        new PermissionRule("POST", "/ai/tasks/*/execute", "ai:task:execute"),
        new PermissionRule("POST", "/ai/tasks", "ai:task:create"),
        new PermissionRule("PUT", "/ai/tasks/*", "ai:task:update"),
        new PermissionRule("DELETE", "/ai/tasks/*", "ai:task:delete"),
        new PermissionRule("GET", "/ai/fault-reports/**", "ai:fault:view"),
        new PermissionRule("POST", "/ai/fault-reports", "ai:fault:create"),
        new PermissionRule("PUT", "/ai/fault-reports/*", "ai:fault:update"),
        new PermissionRule("PATCH", "/ai/fault-reports/*/handle", "ai:fault:handle"),
        new PermissionRule("DELETE", "/ai/fault-reports/*", "ai:fault:delete"),
        new PermissionRule("GET", "/ai/fault-prediction/alerts", "ai:fault:view"),
        new PermissionRule("GET", "/ai/fault-prediction/alerts/**", "ai:fault:view"),
        new PermissionRule("PATCH", "/ai/fault-prediction/alerts/*/handle", "ai:fault:handle"),
        new PermissionRule("DELETE", "/ai/fault-prediction/alerts/*", "ai:fault:delete"),
        new PermissionRule("GET", "/ai/defect-reports/**", "ai:defect:view"),
        new PermissionRule("POST", "/ai/defect-reports", "ai:defect:create"),
        new PermissionRule("PUT", "/ai/defect-reports/*", "ai:defect:update"),
        new PermissionRule("PATCH", "/ai/defect-reports/*/handle", "ai:defect:handle"),
        new PermissionRule("DELETE", "/ai/defect-reports/*", "ai:defect:delete"),
        new PermissionRule("GET", "/ai/optimization-reports/**", "ai:optimization:view"),
        new PermissionRule("POST", "/ai/optimization-reports", "ai:optimization:create"),
        new PermissionRule("PUT", "/ai/optimization-reports/*", "ai:optimization:update"),
        new PermissionRule("PATCH", "/ai/optimization-reports/*/handle", "ai:optimization:handle"),
        new PermissionRule("DELETE", "/ai/optimization-reports/*", "ai:optimization:delete"),
        new PermissionRule("GET", "/ledger/business-systems/**", "ledger:visualization:view"),
        new PermissionRule("POST", "/ledger/business-systems", "ledger:business:create"),
        new PermissionRule("PUT", "/ledger/business-systems/*", "ledger:business:update"),
        new PermissionRule("DELETE", "/ledger/business-systems/*", "ledger:business:delete"),
        new PermissionRule("GET", "/ledger/db-resources/**", "ledger:visualization:view"),
        new PermissionRule("PUT", "/ledger/db-resources/*/monitoring-config", "ledger:database:update"),
        new PermissionRule("POST", "/ledger/db-resources/*/monitoring-config/test", "ledger:database:update"),
        new PermissionRule("POST", "/ledger/db-resources", "ledger:database:create"),
        new PermissionRule("PUT", "/ledger/db-resources/*", "ledger:database:update"),
        new PermissionRule("DELETE", "/ledger/db-resources/*", "ledger:database:delete"),
        new PermissionRule("GET", "/system/users/roles", "system:user:view"),
        new PermissionRule("GET", "/system/users/**", "system:user:view"),
        new PermissionRule("POST", "/system/users", "system:user:create"),
        new PermissionRule("PUT", "/system/users/*", "system:user:update"),
        new PermissionRule("PATCH", "/system/users/*/status", "system:user:status"),
        new PermissionRule("PATCH", "/system/users/*/password", "system:user:reset-password"),
        new PermissionRule("DELETE", "/system/users/*", "system:user:delete"),
        new PermissionRule("GET", "/system/roles/*/menus", "system:role:view"),
        new PermissionRule("PUT", "/system/roles/*/menus", "system:role:assign-menu"),
        new PermissionRule("GET", "/system/roles/**", "system:role:view"),
        new PermissionRule("POST", "/system/roles", "system:role:create"),
        new PermissionRule("PUT", "/system/roles/*", "system:role:update"),
        new PermissionRule("PATCH", "/system/roles/*/status", "system:role:status"),
        new PermissionRule("DELETE", "/system/roles/*", "system:role:delete"),
        new PermissionRule("GET", "/system/menus/**", "system:menu:view"),
        new PermissionRule("POST", "/system/menus", "system:menu:create"),
        new PermissionRule("PUT", "/system/menus/*", "system:menu:update"),
        new PermissionRule("PATCH", "/system/menus/*/status", "system:menu:status"),
        new PermissionRule("DELETE", "/system/menus/*", "system:menu:delete"),
        new PermissionRule("GET", "/system/logs/**", "system:audit-log:view"),
        new PermissionRule("GET", "/system/security/**", "system:security:view"),
        new PermissionRule("GET", "/system/dict/**", "system:dict:view"),
        new PermissionRule("POST", "/system/dict/**", "system:dict:create"),
        new PermissionRule("PUT", "/system/dict/**", "system:dict:update"),
        new PermissionRule("PATCH", "/system/dict/**", "system:dict:status"),
        new PermissionRule("DELETE", "/system/dict/**", "system:dict:delete"),
        new PermissionRule("GET", "/system/configs/**", "system:config:view"),
        new PermissionRule("POST", "/system/configs", "system:config:create"),
        new PermissionRule("PUT", "/system/configs/*", "system:config:update"),
        new PermissionRule("PATCH", "/system/configs/*/status", "system:config:status"),
        new PermissionRule("DELETE", "/system/configs/*", "system:config:delete")
    );

    private final JwtUtil jwtUtil;
    private final SysUserService sysUserService;
    private final SysMenuService sysMenuService;

    public JwtAuthInterceptor(JwtUtil jwtUtil, SysUserService sysUserService, SysMenuService sysMenuService) {
        this.jwtUtil = jwtUtil;
        this.sysUserService = sysUserService;
        this.sysMenuService = sysMenuService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = resolveToken(request);
        JwtUtil.JwtClaims claims = jwtUtil.parseToken(token);
        SysUser user = sysUserService.getEnabledUserById(claims.getUserId());
        if (user == null) {
            throw new BusinessException(401, "登录用户不存在或已禁用");
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setRoles(sysUserService.getRoleCodes(user.getId()));
        UserContext.set(loginUser);
        checkPermission(request, loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    private void checkPermission(HttpServletRequest request, LoginUser loginUser) {
        String path = request.getServletPath();
        String method = request.getMethod();
        if ("/auth/logout".equals(path) || "/auth/me".equals(path)) {
            return;
        }
        if (loginUser.getRoles().contains("admin")) {
            return;
        }
        String permissionCode = resolvePermissionCode(method, path);
        if (!StringUtils.hasText(permissionCode)) {
            return;
        }
        if (!sysMenuService.hasPermission(loginUser.getUserId(), permissionCode)) {
            throw new BusinessException(403, "当前账号没有操作权限");
        }
    }

    private String resolvePermissionCode(String method, String path) {
        for (PermissionRule rule : PERMISSION_RULES) {
            if (rule.matches(method, path)) {
                return rule.permissionCode();
            }
        }
        return null;
    }

    private record PermissionRule(String method, String pathPattern, String permissionCode) {
        boolean matches(String requestMethod, String requestPath) {
            return method.equalsIgnoreCase(requestMethod) && PATH_MATCHER.match(pathPattern, requestPath);
        }
    }
}
