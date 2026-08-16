# Menu Management

## Current Status

已实现。

## Purpose

维护后台菜单、页面和按钮权限，为动态菜单和后端权限拦截提供数据来源。

## Backend

接口：

- `GET /api/system/menus/tree`
- `GET /api/system/menus/{id}`
- `POST /api/system/menus`
- `PUT /api/system/menus/{id}`
- `PATCH /api/system/menus/{id}/status`
- `DELETE /api/system/menus/{id}`

关键代码：

- `SysMenuController`
- `SysMenuServiceImpl`
- `MenuSaveRequest`
- `MenuVO`

## Frontend

- `apps/admin-web/src/views/system/MenuManagementView.vue`
- `apps/admin-web/src/views/system/RolePermissionView.vue`（角色权限管理内的菜单权限页签）
- `apps/admin-web/src/components/DynamicMenuItem.vue`

## Rules

- 菜单类型当前使用 `MENU`、`BUTTON`。
- 权限编码应遵循 `module:resource:action`。
- 删除菜单前应检查子节点。
- 保存角色权限时补齐父级菜单。

## Verification

- 新增菜单后可在角色授权树看到。
- 禁用菜单后不应出现在动态菜单中。
- 按钮权限应影响页面按钮显示。
- 系统管理侧栏固定显示“用户账号管理、角色权限管理、日志审计管理、系统基础配置、安全登录管控”五项；菜单权限维护是角色权限管理的页签，不再单独出现在侧栏。
- 调整内置菜单时必须保持 `permission_code` 唯一；字典和系统配置等既有权限编码优先保留在原菜单 ID 上，只调整名称、层级和路由。
