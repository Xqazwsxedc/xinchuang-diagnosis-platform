# Project Baseline

基线日期：2026-07-19。

本文件记录结构版改造基线和迁移进度。第 0 到第 4 步只新增企业级上下文、规范和模块文档；第 5、6、7、8、9、10 步已完成 `apps/` 基础目录、后端目录迁移、前端目录迁移、统一脚本、结构交接文档和最终验证。

## Original Structure

```text
springboot work/
├── backend/
├── frontend/
├── docs/
├── PROJECT_SPEC.md
├── README.md
├── start-frontend.bat
└── 信创数据库智能故障诊断及修复平台需求规格说明书v2.docx
```

## Current Structure After Step 10

```text
springboot work/
├── apps/
│   ├── api-server/
│   └── admin-web/
├── docs/
│   └── context/
│       └── enterprise-structure-handoff.md
├── scripts/
│   ├── dev-all.ps1
│   ├── dev-api.ps1
│   ├── build-api.ps1
│   ├── dev-web.ps1
│   ├── build-web.ps1
│   ├── verify-all.ps1
│   └── README.md
├── PROJECT_SPEC.md
├── README.md
├── start-all.bat
├── start-frontend.bat
└── 信创数据库智能故障诊断及修复平台需求规格说明书v2.docx
```

## Runtime Baseline

后端：

- 路径：`apps/api-server/`
- 技术栈：Spring Boot 3、Java 17、MyBatis-Plus、MySQL。
- 默认接口前缀：`/api`
- 基线验证：`mvn -DskipTests compile` 通过。
- 第 6 步迁移后验证：在 `apps/api-server/` 下 `mvn -DskipTests compile` 通过。
- 第 8 步统一脚本验证：`scripts/build-api.ps1` 通过。

前端：

- 路径：`apps/admin-web/`
- 技术栈：Vue 3、Vite、TypeScript、Element Plus、Pinia、Vue Router、Axios。
- 开发端口：`5173`
- 基线验证：`pnpm build` 通过。
- 第 7 步迁移后验证：在 `apps/admin-web/` 下 `pnpm build` 通过。
- 第 7 步迁移后已在新目录重建 `node_modules`，并新增 `apps/admin-web/pnpm-workspace.yaml` 记录 pnpm 构建脚本审批。
- 第 8 步统一脚本验证：`scripts/build-web.ps1` 通过。

统一验证：

- `scripts/verify-all.ps1` 通过。
- 第 10 步最终验证：`scripts/verify-all.ps1` 通过。

## Known Warnings

- 前端构建有 Sass legacy JS API deprecation warning。
- Rollup 对第三方依赖 pure 注释位置有提示。
- 主 chunk 超过 500 kB，当前不阻断。
- 沙箱内执行 Vite 构建可能触发 `EPERM`，提权后可通过。

## Current Scope

已实现：

- 登录、退出、当前用户。
- 用户账号管理。
- 角色权限管理。
- 菜单权限和按钮权限。
- 角色数据权限字段。
- 个人中心自助资料和密码修改。
- 数据台账、数据大屏。
- 日志审计、字典、系统配置、通知中心。
- AI 三大模块一期数据壳。

暂不实现：

- 真实 AI 模型推理。
- 真实指标采集和日志采集引擎。
- 自动修复。
- 生产级部署和高可用。
- 完整组织部门树。
