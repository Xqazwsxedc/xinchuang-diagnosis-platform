# System Management Requirements

## Source Summary

Word 需求中系统管理包括用户账号管理和角色权限管理。

原始要求摘要：

- 用户账号管理：列表展示账号、姓名、部门、联系方式、账号状态、角色、操作。
- 用户账号管理：支持新增、查看、编辑、删除、禁用用户信息。
- 角色权限管理：支持新增、查看、编辑、删除角色信息，分配菜单、按钮、数据权限。

## User Account Management

当前状态：已实现。

当前实现：

- 用户列表分页。
- 按账号、姓名、状态搜索。
- 新增用户。
- 编辑用户。
- 启用/禁用用户。
- 重置密码。
- 删除用户。
- 分配角色。
- 维护头像地址。

关键字段：

- `username`
- `real_name`
- `avatar_url`
- `department`
- `phone`
- `email`
- `status`
- `roleIds`

数据准确性要求：

- 用户名唯一。
- 用户名格式校验。
- 密码长度和空白字符校验。
- 手机号格式校验。
- 邮箱格式校验。
- 头像地址格式校验。
- 状态枚举校验。

## Role Permission Management

当前状态：已实现。

当前实现：

- 角色列表分页。
- 按角色编码、角色名称、状态搜索。
- 新增、编辑、启用/禁用、删除。
- 查看详情。
- 分配菜单权限。
- 分配按钮权限。
- 配置数据权限。

数据权限：

- `ALL`：全部数据。
- `DEPARTMENT`：本部门数据。
- `SELF`：本人数据。
- `CUSTOM`：自定义数据。

规则：

- `CUSTOM` 必须填写自定义数据范围。
- 非 `CUSTOM` 保存时清空自定义范围。
- 当前数据权限先作为角色权限边界配置，后续真实业务查询过滤可继续扩展。

## Menu And Button Permission

当前状态：已实现。

当前实现：

- `sys_menu` 保存菜单和按钮。
- `sys_role_menu` 保存角色权限。
- 前端动态菜单和按钮显示。
- 后端按 `permission_code` 拦截。
- 保存角色权限时补齐父级菜单。

## System Management Navigation

当前系统管理侧栏必须与 Figma 原型保持一致，固定只展示以下五项，顺序不可随意调整：

1. 用户账号管理：`/system/users`，权限 `system:user:view`。
2. 角色权限管理：`/system/roles`，权限 `system:role:view`；页面内包含“角色管理”和“菜单权限”页签。
3. 日志审计管理：`/system/audit`，权限 `system:audit-log:view`。
4. 系统基础配置：`/system/config`，权限 `system:config:view`；页面内包含“字典管理”和“系统配置”页签。
5. 安全登录管控：`/system/security`，权限 `system:security:view`；展示真实登录审计数据。

兼容规则：

- `/system/menus`、`/system/logs`、`/system/dicts`、`/system/configs` 保留为历史直达路由，不得再作为侧栏菜单项。
- 菜单、字典和配置的 CRUD 接口及按钮权限编码保持不变。
- 后端菜单由 `SystemBootstrapInitializer` 统一初始化；修改内置菜单编码时必须考虑 `sys_menu.permission_code` 的唯一约束，不能在同一事务中产生临时重复值。

## Gaps

- 组织部门表 `sys_dept` 未实现。
- 多目标细粒度数据权限明细表 `sys_role_data_scope` 未实现，当前先由 `sys_role.data_scope` 承载。
