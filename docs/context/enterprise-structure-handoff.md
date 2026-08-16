# Enterprise Structure Handoff

更新日期：2026-07-20。

本文档记录结构版企业级改造的交接状态，供后续 Vibe Coding 角色、人类开发者和验收人员快速定位项目入口。

## Current Structure

```text
springboot work/
├── apps/
│   ├── api-server/
│   └── admin-web/
├── scripts/
├── docs/
├── .ai-context/
├── README.md
├── PROJECT_SPEC.md
├── start-all.bat
└── start-frontend.bat
```

## Runtime Ownership

- 后端运行代码：`apps/api-server/`
- 前端运行代码：`apps/admin-web/`
- 项目级脚本：`scripts/`
- AI 协作上下文：`.ai-context/`
- 需求、规范、模块说明和验收材料：`docs/`
- 项目最新说明书：`PROJECT_SPEC.md`

## Unified Commands

一键启动：

```text
start-all.bat
```

后端启动：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev-api.ps1
```

前端启动：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev-web.ps1
```

后端构建：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-api.ps1
```

前端构建：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-web.ps1
```

全量验证：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verify-all.ps1
```

## Migration Status

- 第 5 步：已创建 `apps/` 和 `scripts/`。
- 第 6 步：后端已由 `backend/` 迁移到 `apps/api-server/`。
- 第 7 步：前端已由 `frontend/` 迁移到 `apps/admin-web/`。
- 第 8 步：已新增统一启动、构建、验证脚本。
- 第 9 步：已固化结构交接文档和脚本说明。
- 第 10 步：已执行最终全量验证，并更新项目说明和上下文。

## Guardrails

- 不要再次移动 `apps/api-server/` 或 `apps/admin-web/`。
- 新增后端功能时优先在 `apps/api-server/src/main/java/com/xinchuang/diagnosis` 下按模块扩展。
- 新增前端功能时优先在 `apps/admin-web/src/api`、`src/views`、`src/router`、`src/stores` 中按现有模式扩展。
- 涉及脚本变更时同步更新 `scripts/README.md`、`README.md`、`PROJECT_SPEC.md` 和 `.ai-context/shared/verification-baseline.md`。
- 每轮结束前必须运行与改动相关的验证命令，并把结果写回 `PROJECT_SPEC.md`。

## Known Environment Notes

- 后端脚本会优先使用 `C:\Program Files\Java\jdk-17` 和 `E:\maven\apache-maven-3.9.16`。
- 前端脚本会优先使用 Codex 内置 Node/pnpm，并关闭 pnpm 版本自管理，避免禁网环境下尝试获取 pnpm 元数据。
- `apps/admin-web/pnpm-workspace.yaml` 用于允许 `esbuild`、`@parcel/watcher`、`vue-demi` 执行安装脚本。
- Maven 依赖解析或 Vite/pnpm 写临时文件在沙箱中可能触发权限问题，验证时可在确认命令安全后提权运行。
