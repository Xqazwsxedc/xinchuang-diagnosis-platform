# Role Management

## Requirement

支持新增、查看、编辑、删除角色信息，分配菜单、按钮、数据权限。

## Current Status

已实现。

## Backend

接口：

- `GET /api/system/roles`
- `GET /api/system/roles/{id}`
- `POST /api/system/roles`
- `PUT /api/system/roles/{id}`
- `PATCH /api/system/roles/{id}/status`
- `DELETE /api/system/roles/{id}`
- `GET /api/system/roles/{id}/menus`
- `PUT /api/system/roles/{id}/menus`

关键代码：

- `SysRoleController`
- `SysRoleServiceImpl`
- `RoleSaveRequest`
- `RoleVO`

## Frontend

- `apps/admin-web/src/views/system/RoleManagementView.vue`
- `apps/admin-web/src/views/system/RolePermissionView.vue`（系统管理正式入口，Figma 对齐的左右分栏角色与权限配置页）
- `apps/admin-web/src/api/modules/system.ts`

## Data Scope

- `ALL`：全部数据。
- `DEPARTMENT`：本部门数据。
- `SELF`：本人数据。
- `CUSTOM`：自定义数据。

规则：

- `CUSTOM` 必填自定义范围。
- 非 `CUSTOM` 清空自定义范围。
- 已分配给用户的角色不能删除。

## Verification

- 可新增、编辑、删除未分配角色。
- 已分配角色删除失败。
- 自定义数据权限不填范围时保存失败。
- 菜单/按钮权限保存后重新登录生效。
- 角色权限正式页以左侧角色列表、右侧菜单/按钮/数据权限配置呈现；仅调整前端编排，不改变角色 CRUD、菜单授权和数据权限接口契约。
