# AI Deploy Compact

职责：维护启动、构建、部署、环境变量和脚本说明。

当前已完成 Windows 本机初步部署：Nginx 承载 Vue 静态文件并将 `/api` 反向代理至隔离的 Spring Boot 端口，后端连接本机 MySQL。该能力用于个人电脑演示与验收，不等同于生产高可用部署。

结构交接文档：`docs/context/enterprise-structure-handoff.md`。
脚本说明文档：`scripts/README.md`。

当前统一脚本：

- `scripts/dev-all.ps1`
- `scripts/dev-api.ps1`
- `scripts/build-api.ps1`
- `scripts/dev-web.ps1`
- `scripts/build-web.ps1`
- `scripts/verify-all.ps1`

本机部署脚本：

- `deploy/windows/install-nginx.ps1`
- `deploy/windows/build.ps1`
- `deploy/windows/start.ps1`
- `deploy/windows/stop.ps1`
- `deploy/windows/status.ps1`

部署规则：

- `.env` 只保存在本机，禁止提交或写入密码、JWT 密钥。
- 前端使用 `http://localhost`，后端使用 `DEPLOY_API_PORT`（当前为 `18080`），不得占用开发端口 `5173`、`8080`。
- 变更部署脚本后必须依次验证后端 `/api/health`、Nginx 首页、Nginx `/api/health`，再执行黑盒接口回归。
- 测试 2.0 已经通过 Nginx 部署入口 `http://localhost/api` 执行 50/50；部署回归优先使用 `docs/testing/api-blackbox-test-2.0.ps1`，其测试密码仅由当前进程的 `DIAGNOSIS_TEST_PASSWORD` 提供。
- 故障知识库上传根目录配置为 `KNOWLEDGE_STORAGE_ROOT`，留空时使用部署运行目录下的 `data/knowledge-files`。构建或重启后必须检查上传目录可由后端创建，且不得把其暴露为 Nginx 静态目录。
- `start-all.bat` 的开发后端通过 `Import-LocalRuntimeEnvironment` 读取本机 `deploy/windows/.env` 中允许的非空变量，避免开发链路回退到错误的数据库默认密码。该读取仅作用于当前 PowerShell 进程，绝不输出或提交密码。
- 2026-08-05 故障预测任务配置补齐未改变部署结构或环境变量契约；后端新代码需重启开发/部署实例后生效。本轮仅完成临时开发实例健康和未登录鉴权验证，未进行部署版登录态任务 CRUD 黑盒。
- 2026-08-05 故障智能预警改造未改变部署结构或新增环境变量。隔离端口 18081 已验证新代码健康和未登录接口鉴权，临时实例已停止；正式开发/部署实例需重启后载入本轮代码。
- 2026-08-06 故障预测任务前后端兼容与布局修复未改变部署结构、端口或环境变量契约；正式开发/部署实例需重启后加载新 Jar 和前端静态资源。后端编译/测试、前端生产构建均通过。
- Prometheus 接入已落地。开发 `scripts/_common.ps1` 与部署 `deploy/windows/_common.ps1` 都从未提交的 `.env` 读取 `MONITORING_PROMETHEUS_BASE_URL`、超时和目标映射；模板只保留 `REPLACE_*` 占位符，禁止提交真实地址、Docker IP、密码或密钥。当前本机 `.env` 已写单机目标映射，基础地址为空；填入后必须重启开发/部署后端，再验证详情指标、CSV 和 `/api/health`。
- 2026-08-17 部署回归：本项目残留 Nginx 子进程曾持续占用 `80` 端口，导致部署启动脚本提前退出并保留旧页面。重启后 Nginx 监听 `80`、Spring Boot 监听 `18080`，`/api/health` 返回 200。Prometheus URI 修复已重新打包到部署 Jar，登录态 1D 指标接口经 Nginx 返回 200；后续每次修改该模块仍必须运行 `deploy/windows/build.ps1` 后重启，不可复用旧 Jar。

必须先读：

- `PROJECT_SPEC.md`
- `.ai-context/shared/verification-baseline.md`
- `README.md`
- `docs/standards/documentation-maintenance-standard.md`
