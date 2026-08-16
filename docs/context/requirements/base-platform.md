# Base Platform Requirements

## Source Summary

Word 需求说明系统通过账号密码登录，登录后根据不同角色权限查看和使用平台功能。非功能需求要求统一用户界面、严格权限控制、数据安全和数据准确性。

## Requirement Items

### Authentication

要求：

- 用户通过账号和密码进入系统。
- 登录成功后进入信创数据库智能故障诊断及修复平台。
- 禁止未认证用户访问后台功能。

当前状态：已实现。

当前实现：

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- JWT 拦截器。
- BCrypt 密码存储。

### Access Control

要求：

- 用户登录后，对任何操作和信息查看都必须进行权限控制。
- 提供方便的权限设置、分配和管理能力。

当前状态：已实现一期企业后台权限能力。

当前实现：

- 动态菜单。
- 按钮权限。
- 后端接口权限编码拦截。
- 角色菜单/按钮权限分配。
- admin 超级管理员兜底。

### Notification

要求：

- 用户可查看系统推送信息。

当前状态：已实现。

当前实现：

- 通知列表、未读数、标记已读、全部已读、删除、清空已读。
- 账号禁用、密码重置、个人改密等场景生成通知。

### Profile Center

要求：

- 查看用户个人信息。
- 支持编辑头像、名称、密码。

当前状态：已实现并扩展。

当前实现：

- `GET /api/auth/profile`
- `PUT /api/auth/profile`
- `PATCH /api/auth/password`
- 当前用户可维护头像、名称、部门、手机号、邮箱。
- 当前用户改密必须校验原密码。

## Boundaries

当前阶段不实现统一单点登录、不实现多租户、不实现登录失败锁定策略。后续可通过 `sys_login_policy`、组织部门和更细粒度数据权限扩展。

