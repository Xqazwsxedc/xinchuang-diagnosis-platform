# API Contract Standard

## Base Rules

- 后端接口统一前缀：`/api`。
- 前端 Axios baseURL 对应该前缀。
- 所有响应统一使用 `ApiResponse`。
- 分页统一使用 `PageResponse`。
- 时间字段使用 ISO 字符串，前端展示时格式化。
- Long ID 序列化为字符串，前端类型使用 `string`。

## Request Rules

- 新增和编辑必须使用 DTO。
- 状态变更使用独立 request DTO。
- 密码、邮箱、手机号、头像 URL 等字段必须校验。
- 不允许前端传入服务端自行确定的审计字段。

## Error Rules

- 401：未登录或登录过期。
- 403：没有操作权限。
- 业务错误：返回明确中文提示。
- 参数校验错误：由统一异常处理返回。

## Frontend Sync

后端接口变更后必须同步：

- `apps/admin-web/src/api/modules/*.ts`
- 页面表单字段。
- Pinia 状态。
- 模块文档。
- `PROJECT_SPEC.md`。

## Endpoint Naming

- 列表：`GET /resource`
- 详情：`GET /resource/{id}`
- 新增：`POST /resource`
- 编辑：`PUT /resource/{id}`
- 状态：`PATCH /resource/{id}/status`
- 删除：`DELETE /resource/{id}`
- 当前用户自助接口放在 `/auth/profile`、`/auth/password`。

