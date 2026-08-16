# Windows 本机部署验收报告

## 1. 目的与范围

本报告记录信创数据库智能故障诊断及修复平台在 Windows 个人电脑上的初步部署验证。范围包含前端静态资源发布、Nginx 反向代理、Spring Boot 服务、本机 MySQL 连接和核心接口回归；不包含公网暴露、高可用、监控告警、备份恢复或生产安全加固。

## 2. 部署拓扑

```text
浏览器
  -> Nginx :80
     -> Vue 静态文件
     -> /api -> Spring Boot :18080 -> MySQL80 :3306
```

开发端口 `5173`、`8080` 不参与本部署，可与部署实例并存。

## 3. 部署资产与安全边界

- 部署脚本位于 `deploy/windows/`：Nginx 安装、构建、启动、停止和状态检查。
- 构建产物位于 `deploy/windows/runtime/`，不纳入版本库。
- 本机环境变量保存于 `deploy/windows/.env`，已由 Git 忽略；其中的数据库密码、JWT 密钥和管理员引导密码不得写入源码、报告或提交记录。
- Spring Boot 启动时将 `DEPLOY_API_PORT` 映射为 `SERVER_PORT`；Nginx 配置从同一个变量生成代理目标，避免端口配置不一致。
- 仅在初始化全新数据库时才允许显式开启管理员引导；已有管理员账号绝不由部署启动流程重置密码、状态或角色。

## 4. 2026-08-04 验收结果

| 编号 | 验证项 | 预期 | 实测 |
| --- | --- | --- | --- |
| DEP-01 | 后端构建和前端生产构建 | 可生成运行产物 | 通过 |
| DEP-02 | 后端健康检查 | `http://127.0.0.1:18080/api/health` 返回 200 | 通过 |
| DEP-03 | Nginx 前端入口 | `http://localhost` 返回 200 | 通过 |
| DEP-04 | Nginx API 代理 | `http://localhost/api/health` 返回 200 | 通过 |
| DEP-05 | 黑盒接口回归 | 14 项核心接口和权限边界通过 | 通过，14/14 |
| DEP-06 | 停止后重启 | PID 文件和 80/18080 端口被释放，重启后恢复健康 | 通过 |

黑盒接口结果文件：`docs/testing/results/deployment-api-blackbox-result.json`。

## 5. 日常操作

1. 首次执行 `deploy/windows/install-nginx.ps1` 和 `deploy/windows/build.ps1`。
2. 执行 `deploy/windows/start.ps1`，在浏览器访问 `http://localhost`。
3. 执行 `deploy/windows/status.ps1` 检查前端与后端健康状态。
4. 更新代码后，先执行构建，再停止并重新启动部署实例。
5. 停止实例使用 `deploy/windows/stop.ps1`。

完整操作说明见 `deploy/windows/README.md`。

## 6. 后续边界

本机部署已可用于课程演示和本地验收。若进入生产环境，仍需补充 HTTPS、服务账户、日志轮转、Windows 服务化或容器化、数据库备份恢复、监控告警、访问控制、漏洞修复和高可用设计。
