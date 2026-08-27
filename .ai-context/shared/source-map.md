# Source Map

当前运行代码路径：

- 后端：`apps/api-server/`
- 前端：`apps/admin-web/`
- 项目说明：`PROJECT_SPEC.md`
- 需求 Word：`信创数据库智能故障诊断及修复平台需求规格说明书v2.docx`
- 数据库设计 Word：`docs/数据库设计说明书_v1(1).docx`
- 数据库设计摘要：`docs/context/requirements/database-design.md`
- 验收资料：`docs/一期基础接口测试与验收清单.md`、`docs/一期接口冒烟测试.http`
- 测试资料：`docs/testing/测试实施与质量报告.md`、`docs/testing/api-blackbox-test.ps1`、`docs/testing/frontend-integration-checklist.md`
- 测试 2.0：`docs/testing/测试实施与质量报告-2.0.md`、`docs/testing/api-blackbox-test-2.0.ps1`、`docs/testing/results/deployment-api-blackbox-result-2.0.json`
- 测试用例设计：`docs/testing/测试用例设计说明-2.0.md`
- 结构交接：`docs/context/enterprise-structure-handoff.md`
- 一键启动：`start-all.bat`、`scripts/dev-all.ps1`
- Windows 本机部署：`deploy/windows/README.md`、`deploy/windows/build.ps1`、`deploy/windows/start.ps1`、`deploy/windows/stop.ps1`、`deploy/windows/status.ps1`
- 本机部署验收：`docs/testing/windows-local-deployment-report.md`、`docs/testing/results/deployment-api-blackbox-result.json`
- 统一脚本：`scripts/dev-api.ps1`、`scripts/build-api.ps1`、`scripts/dev-web.ps1`、`scripts/build-web.ps1`、`scripts/verify-all.ps1`
- 脚本说明：`scripts/README.md`
- 功能新增/修改 Harness 流程：`.ai-context/shared/feature-change-workflow.md`

后端关键目录：

- `apps/api-server/src/main/java/com/xinchuang/diagnosis/auth`：登录、当前用户。
- `apps/api-server/src/main/java/com/xinchuang/diagnosis/security`：JWT、用户上下文、权限拦截。
- `apps/api-server/src/main/java/com/xinchuang/diagnosis/system`：用户、角色、菜单、字典、配置、日志、通知。
- `apps/api-server/src/main/java/com/xinchuang/diagnosis/ledger`：数据台账。
- `apps/api-server/src/main/java/com/xinchuang/diagnosis/ai`：AI 数据壳。
- `apps/api-server/src/main/resources/schema.sql`：当前已落地数据库执行脚本，变更前必须先对齐数据库设计说明书。
- `apps/api-server/src/main/resources/data.sql`：演示数据和默认数据。

前端关键目录：

- `apps/admin-web/src/api`：Axios 封装和 API 类型。
- `apps/admin-web/src/stores`：Pinia 状态。
- `apps/admin-web/src/router`：路由和权限守卫。
- `apps/admin-web/src/layouts/MainLayout.vue`：后台主布局和动态菜单。
- `apps/admin-web/src/views/system`：系统管理页面。
- `apps/admin-web/src/views/ProfileView.vue`：个人中心。
- `apps/admin-web/src/views/data-ledger`：数据台账。
- `apps/admin-web/src/views/ai`：AI 数据壳页面。
- `apps/admin-web/src/views/ai/FaultKnowledgeView.vue`：Figma 对齐的故障知识库专用页面。
- `apps/api-server/src/main/java/com/xinchuang/diagnosis/ai/service/FaultKnowledgeService.java`：故障知识库业务、附件逻辑删除和审计。
- `apps/api-server/src/main/java/com/xinchuang/diagnosis/ai/service/KnowledgeFileStorageService.java`：本地附件安全存储与读取。
- `docs/modules/ai-fault-prediction/knowledge-base.md`：故障知识库模块说明。
- `docs/tech-sharing/AI故障预测调用流程与API服务技术交流.md`：AI 故障预测调用流程、模块划分、API 契约和核心代码示例。
- `scripts/_common.ps1`：统一开发环境、工具路径与本机运行环境变量加载；`scripts/dev-api.ps1` 必须在启动前调用其本机环境加载函数。

企业级上下文目录：

- `.ai-context/`：AI 角色协作上下文。
- `scripts/`：项目级启动、构建、验证脚本。
- `docs/context/`：需求、基线、接口和领域上下文。
- `docs/standards/`：工程规范。
- `docs/modules/`：模块级说明。
- `docs/tech-sharing/`：技术分享沉淀。
