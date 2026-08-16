# Project Scripts

本目录保存项目级启动、构建和验证脚本。所有脚本都应从项目根目录调用。

## Commands

一键启动后端和前端：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev-all.ps1
```

也可以直接双击项目根目录 `start-all.bat`。

启动后端：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev-api.ps1
```

构建后端：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-api.ps1
```

启动前端：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev-web.ps1
```

构建前端：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-web.ps1
```

全量验证：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verify-all.ps1
```

## Script Responsibilities

- `_common.ps1`：统一解析项目根目录、后端目录、前端目录、JDK/Maven、Node/pnpm。
- `dev-all.ps1`：分别打开后端和前端 PowerShell 窗口，适合本地一键启动。
- `dev-api.ps1`：进入 `apps/api-server/` 并执行 `mvn spring-boot:run`。
- `build-api.ps1`：进入 `apps/api-server/` 并执行 `mvn -DskipTests compile`。
- `dev-web.ps1`：进入 `apps/admin-web/` 并执行 `pnpm dev`。
- `build-web.ps1`：进入 `apps/admin-web/` 并执行 `pnpm build`。
- `verify-all.ps1`：依次执行后端构建和前端构建。

## Environment Notes

- 后端脚本会在 `JAVA_HOME` 或 `MAVEN_HOME` 为空或无效时，优先使用本机已知路径。
- 前端脚本会优先使用 Codex 内置 Node/pnpm，并设置 `COREPACK_ENABLE_PROJECT_SPEC=0`、`npm_config_manage_package_manager_versions=false`。
- 如果本机已经正确配置全局 Java、Maven、Node、pnpm，也可以直接执行对应原生命令。
- `dev-api.ps1` 在未设置 `JWT_SECRET` 时仅为当前开发进程生成随机密钥；生产或长期部署必须显式设置至少 32 位随机 `JWT_SECRET`。
- 新数据库如需创建管理员，显式设置 `BOOTSTRAP_ADMIN_ENABLED=true` 和 `BOOTSTRAP_ADMIN_PASSWORD`。初始化仅在账号不存在时执行，绝不重置或重新启用已有管理员账号。
