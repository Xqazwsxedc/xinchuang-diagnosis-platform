# User Management

## Requirement

账号管理列表展示账号、姓名、部门、联系方式、账号状态、角色、操作。支持新增、查看、编辑、删除、禁用用户信息。

## Current Status

已实现，并补充头像地址维护。

## Backend

接口：

- `GET /api/system/users`
- `GET /api/system/users/{id}`
- `POST /api/system/users`
- `PUT /api/system/users/{id}`
- `PATCH /api/system/users/{id}/status`
- `PATCH /api/system/users/{id}/password`
- `DELETE /api/system/users/{id}`
- `GET /api/system/users/roles`

关键代码：

- `SysUserController`
- `SysUserServiceImpl`
- `UserCreateRequest`
- `UserUpdateRequest`
- `ResetPasswordRequest`
- `UserVO`

## Frontend

- `apps/admin-web/src/views/system/UserManagementView.vue`
- `apps/admin-web/src/api/modules/system.ts`

## Data Accuracy

- 用户名唯一。
- 用户名格式校验。
- 密码禁止空白字符。
- 手机号格式校验。
- 邮箱格式校验。
- 头像地址格式校验。
- 状态枚举校验。

## Verification

- 新增用户成功。
- 重复用户名失败。
- 编辑头像、手机号、邮箱成功。
- 无效手机号、邮箱、头像地址阻止提交。
- 禁用用户后不能登录。

