# Project Overview

项目名称：信创数据库智能故障诊断及修复平台。

目标：建设面向国产分布式数据库运维场景的企业级后台系统，覆盖数据台账、数据大屏、AI 故障预测、AI 缺陷检测、AI 性能优化、系统管理、消息通知和个人中心。

当前技术栈：

- 后端：Spring Boot 3、Java 17、MyBatis-Plus、MySQL、JWT、BCrypt。
- 前端：Vue 3、Vite、TypeScript、Element Plus、Pinia、Vue Router、Axios。
- 数据库：MySQL，平台库为 `xc_diagnosis`。
- 数据库设计蓝图：`docs/数据库设计说明书_v1(1).docx`，可检索摘要为 `docs/context/requirements/database-design.md`；后续数据库开发必须严格对齐该说明书。

项目工作流图：`docs/diagrams/vibe-coding-harness-workflow.svg` 和 PNG 展示需求与约束、Harness 规划、分层实现、测试与质量、构建与本机部署五步闭环；每步包含流程、检查点、产出和对应技术栈，详细执行规则见 `.ai-context/shared/feature-change-workflow.md`。

当前重点：

- 基座模块：认证、权限、动态菜单、统一返回、异常处理、参数校验、审计日志、字典、配置、通知。
- 系统管理模块：用户账号、角色权限、菜单权限、数据权限字段、个人中心。
- AI 数据壳：已具备一期数据表、接口、权限和页面，真实 AI 推理暂不实现。

结构版迁移状态：

- 后端已由 `backend/` 迁移为 `apps/api-server/`。
- 前端已由 `frontend/` 迁移为 `apps/admin-web/`。
- 当前已完成第 5、6、7、8、9、10 步；已新增统一启动、构建、全量验证脚本、结构交接文档和脚本说明。
- 当前已新增 Windows 一键启动入口：`start-all.bat`，底层调用 `scripts/dev-all.ps1` 分别打开后端和前端窗口。
