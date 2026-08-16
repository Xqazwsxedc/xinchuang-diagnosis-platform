# System Management Module

系统管理模块覆盖用户账号、角色权限、菜单权限、日志审计、基础配置、安全登录审计和个人中心。

## Pages

- `/system/users`
- `/system/roles`
- `/system/audit`
- `/system/config`
- `/system/security`
- `/profile`

兼容直达路由：`/system/menus`、`/system/logs`、`/system/dicts`、`/system/configs`。它们不出现在左侧菜单，原有功能分别收纳到“角色权限管理”和“系统基础配置”页签。

## Backend Packages

- `apps/api-server/src/main/java/com/xinchuang/diagnosis/system`
- `apps/api-server/src/main/java/com/xinchuang/diagnosis/auth`

## Documents

- `user-management.md`
- `role-management.md`
- `menu-management.md`
- `profile-center.md`
- `code-quality-optimization.md`

## Current Requirement Match

- 用户账号管理：已覆盖账号、姓名、部门、联系方式、账号状态、角色、操作。
- 角色权限管理：已覆盖角色 CRUD、菜单权限、按钮权限、数据权限。
- 日志审计管理：已覆盖登录日志与操作日志。
- 系统基础配置：已覆盖字典管理与系统配置。
- 安全登录管控：已通过独立只读接口展示真实登录日志，权限编码为 `system:security:view`。
- 个人中心：已覆盖头像、名称、密码编辑和个人信息查看。
- 代码质量：已完成系统管理模块死代码、重复查询、事务一致性、参数校验和前端按钮权限专项打磨。
