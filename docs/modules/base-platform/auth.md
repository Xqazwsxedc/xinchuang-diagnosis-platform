# Authentication

## Current Status

已实现。

## Backend

关键接口：

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `GET /api/auth/profile`
- `PUT /api/auth/profile`
- `PATCH /api/auth/password`

关键代码：

- `AuthController`
- `AuthServiceImpl`
- `JwtUtil`
- `JwtAuthInterceptor`
- `UserContext`
- `LoginUserVO`

## Rules

- 密码使用 BCrypt。
- 默认账号：`admin / password`。
- 禁用账号不能登录。
- 当前用户资料修改只能作用于登录用户自身。
- 当前用户改密必须校验原密码。

## Verification

- 正确账号密码可登录。
- 错误密码登录失败并记录登录日志。
- 禁用账号无法登录。
- `/auth/me` 返回用户、角色、菜单和权限。
- `/auth/profile` 返回当前用户资料。

