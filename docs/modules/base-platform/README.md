# Base Platform Module

系统基座模块提供企业后台通用能力。

## Scope

- 登录认证。
- JWT 鉴权。
- 当前用户上下文。
- 动态菜单。
- 按钮权限。
- 后端接口权限拦截。
- 统一返回。
- 统一异常处理。
- 参数校验。
- 操作日志。
- 登录日志。
- 字典管理。
- 系统配置。
- 通知中心。

## Key Documents

- `auth.md`
- `permission.md`
- `audit-log.md`
- `config-dict.md`
- `notification.md`

## Source Code

- 后端：`apps/api-server/src/main/java/com/xinchuang/diagnosis/auth`
- 后端：`apps/api-server/src/main/java/com/xinchuang/diagnosis/security`
- 后端：`apps/api-server/src/main/java/com/xinchuang/diagnosis/system`
- 前端：`apps/admin-web/src/layouts/MainLayout.vue`
- 前端：`apps/admin-web/src/stores`
- 前端：`apps/admin-web/src/router`

## Verification

- 登录成功。
- 未登录访问后台跳转登录。
- 权限不足返回 403 或前端拦截。
- 菜单按角色动态展示。
- 写操作进入操作日志。
- 通知未读数正确刷新。

