# Windows 本机部署

## 架构

`浏览器 -> Nginx:80 -> Vue 静态文件 / Spring Boot:18080 -> MySQL:3306`

开发端口 `5173` 和 `8080` 不参与本机部署，可与部署环境同时存在。

## 首次部署

1. 将 `deployment.env.template` 复制为 `.env`，填写 MySQL 密码，并生成至少 32 位的 `JWT_SECRET`。`KNOWLEDGE_STORAGE_ROOT` 可留空，留空时故障知识库附件保存到部署运行目录的 `data/knowledge-files`；该目录不得配置为 Nginx 静态目录。
   数据库资源详情要显示真实监控指标时，同时填写 `MONITORING_PROMETHEUS_BASE_URL`、`MONITORING_TARGETS_NODE_INSTANCE` 和 `MONITORING_TARGETS_MYSQL_INSTANCE`。Vue 不会直接访问 Prometheus；基础地址仅供本机 Spring Boot 使用。
2. 执行 `powershell -ExecutionPolicy Bypass -File .\deploy\windows\install-nginx.ps1`。
3. 执行 `powershell -ExecutionPolicy Bypass -File .\deploy\windows\build.ps1`。
4. 执行 `powershell -ExecutionPolicy Bypass -File .\deploy\windows\start.ps1`。
5. 浏览器访问 `http://localhost`，然后执行 `status.ps1` 验证。

## 日常操作

- 更新代码后：执行 `build.ps1`，再执行 `stop.ps1` 和 `start.ps1`。`build.ps1` 会先清空 `runtime/site`，再同步当前前端构建结果，避免旧 hash 静态资源残留。
- 停止部署：执行 `stop.ps1`。
- 查看后端日志：`deploy/windows/runtime/logs/api.log` 与 `api-error.log`。
- 运行状态：执行 `status.ps1`。

## 安全配置

- `.env` 仅保存在本机，已被 Git 忽略。
- `JWT_SECRET` 必须是至少 32 位随机值。
- 新数据库创建管理员时才设置 `BOOTSTRAP_ADMIN_ENABLED=true` 和 `BOOTSTRAP_ADMIN_PASSWORD`；已有数据库保持 `false`。
- 首次以已有演示管理员登录后，应在个人中心修改密码。
- Prometheus 的目标 `instance` 可能随 Docker 容器重建变化。单机学习环境可先配置当前值，长期环境应改为稳定服务名或基于资源 ID 的映射。

## 验收

- `http://localhost` 返回前端页面。
- `http://localhost/api/health` 返回 HTTP 200。
- 管理员登录、系统管理菜单、台账查询和权限边界正常。
- 重启 `start.ps1` 后页面和健康检查恢复。
