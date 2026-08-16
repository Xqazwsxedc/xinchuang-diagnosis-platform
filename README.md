# 信创数据库智能故障诊断及修复平台

前后端分离项目骨架，基于第一期范围搭建。

## 目录

- `apps/api-server`：Spring Boot 3 + Java 17 + MyBatis-Plus + MySQL
- `apps/admin-web`：Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router + Axios

## 一键启动

双击项目根目录：

```text
start-all.bat
```

该脚本会分别打开后端和前端 PowerShell 窗口。

访问地址：

- 前端：`http://localhost:5173`
- 后端健康检查：`http://localhost:8080/api/health`

## 后端启动

先准备 JDK 17、Maven、MySQL，并创建数据库：

```sql
CREATE DATABASE xc_diagnosis DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

本地数据库连接信息放在 `deploy/windows/.env`，该文件已被 Git 忽略，不会提交到远程仓库。首次使用时请确认其中的 `DB_PASSWORD` 与本机 MySQL 一致。

启动：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev-api.ps1
```

可通过环境变量覆盖数据库连接：

```bash
DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=xc_diagnosis DB_USERNAME=root DB_PASSWORD=123456 mvn spring-boot:run
```

健康检查：

```bash
curl http://localhost:8080/api/health
```

## 前端启动

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev-web.ps1
```

默认访问：`http://localhost:5173`

如果只想进入前端目录运行局部脚本，也可以使用：

```powershell
cd "E:\vibe work\springboot work\apps\admin-web"
.\run-dev.ps1
```

或者安装 pnpm 后再运行：

```powershell
corepack enable
corepack prepare pnpm@latest --activate
pnpm dev
```

## 统一验证

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-api.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\build-web.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\verify-all.ps1
```

`verify-all.ps1` 会依次执行后端 `mvn -DskipTests compile` 和前端 `pnpm build`。

脚本说明见：`scripts/README.md`

## 结构交接

企业级结构迁移后的运行入口、目录边界和交接注意事项见：

- `docs/context/enterprise-structure-handoff.md`

## 一期验收材料

一期收尾验收请先阅读：

- `docs/一期基础接口测试与验收清单.md`
- `docs/一期接口冒烟测试.http`

建议流程：

1. 重启后端，让最新表结构、菜单权限和演示数据初始化生效。
2. 重新登录前端，刷新本地缓存的菜单和权限。
3. 按验收清单逐项检查页面功能和权限。
4. 使用 `.http` 文件做基础接口冒烟测试：先登录获取 token，再把 token 填入 `@token`。
