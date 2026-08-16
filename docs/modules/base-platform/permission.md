# Permission

## Current Status

已实现菜单权限、按钮权限、接口权限拦截和角色数据权限字段。

## Tables

- `sys_user`
- `sys_role`
- `sys_user_role`
- `sys_menu`
- `sys_role_menu`

## Data Scope Fields

- `sys_role.data_scope`
- `sys_role.data_scope_config`

## Backend

关键代码：

- `JwtAuthInterceptor`
- `SystemBootstrapInitializer`
- `SysMenuServiceImpl`
- `SysRoleServiceImpl`

## Frontend

关键代码：

- `apps/admin-web/src/router/index.ts`
- `apps/admin-web/src/layouts/MainLayout.vue`
- `apps/admin-web/src/components/DynamicMenuItem.vue`
- `apps/admin-web/src/stores/user.ts`

## Verification

- admin 可访问全部功能。
- operator 按授权访问业务和 AI 数据壳。
- viewer 只读。
- 取消按钮权限后对应按钮隐藏。
- 无接口权限时返回 403。

