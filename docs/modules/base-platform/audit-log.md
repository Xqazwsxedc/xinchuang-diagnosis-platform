# Audit Log

## Current Status

已实现登录日志和操作日志。

## Tables

- `login_log`
- `operation_log`

## Backend

关键代码：

- `AuditLogController`
- `AuditLogService`
- `AuditLogServiceImpl`
- `LoginLogMapper`
- `OperationLogMapper`

## Current Coverage

- 登录成功和失败。
- 用户管理写操作。
- 角色管理写操作。
- 菜单管理写操作。
- 字典和配置写操作。
- 通知操作。
- 个人资料和密码修改。
- AI 数据壳写操作。

## Verification

- 执行写操作后可在日志审计页面查询。
- 登录失败应有失败记录和原因。

