# Profile Center

## Requirement

支持用户进行头像、名称、密码编辑，并查看用户个人信息。

## Current Status

已实现并扩展。

## Backend

接口：

- `GET /api/auth/profile`
- `PUT /api/auth/profile`
- `PATCH /api/auth/password`

关键代码：

- `AuthController`
- `SysUserServiceImpl.currentProfile`
- `SysUserServiceImpl.updateCurrentProfile`
- `SysUserServiceImpl.changeCurrentPassword`
- `ProfileUpdateRequest`
- `ChangePasswordRequest`

## Frontend

- `apps/admin-web/src/views/ProfileView.vue`
- `apps/admin-web/src/api/modules/auth.ts`
- `apps/admin-web/src/stores/user.ts`

## Fields

- 账号：只读。
- 头像地址。
- 名称。
- 部门。
- 手机号。
- 邮箱。
- 原密码。
- 新密码。
- 确认新密码。

## Rules

- 只能修改当前登录用户自身资料。
- 修改密码必须校验原密码。
- 新密码不能与原密码一致。
- 修改资料后刷新本地登录用户状态。
- 头像、手机号、邮箱、密码必须校验。

## Verification

- 打开个人中心能加载当前用户信息。
- 修改头像、名称、手机号、邮箱后保存成功。
- 无效头像、手机号、邮箱阻止提交。
- 原密码错误时改密失败。
- 两次新密码不一致时前端阻止提交。

