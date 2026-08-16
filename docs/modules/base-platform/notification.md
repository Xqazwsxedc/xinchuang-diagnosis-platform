# Notification

## Current Status

已实现。

## Table

- `system_notification`

## Frontend

- 顶部通知按钮。
- 未读角标。
- `/notifications` 通知中心页面。

## Backend APIs

- `GET /api/notifications`
- `GET /api/notifications/unread-count`
- `PATCH /api/notifications/{id}/read`
- `PATCH /api/notifications/read-all`
- `DELETE /api/notifications/{id}`
- `DELETE /api/notifications/read`

## Current Producers

- 禁用账号。
- 管理员重置密码。
- 当前用户自助修改密码。

## Verification

- 有未读消息时显示角标。
- 未读数为 0 时不显示角标。
- 标记已读后未读数刷新。
- 删除和清空已读正常。

