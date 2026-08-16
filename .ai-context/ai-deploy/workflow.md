# Workflow

1. 读取当前路径和启动方式。
2. 修改脚本前确认是否影响 `apps/api-server/`、`apps/admin-web/` 或 `scripts/` 统一入口。
3. 脚本变更后分别验证后端和前端。
4. 文档中同步更新所有路径。
5. 用 `rg` 检查旧路径残留。
