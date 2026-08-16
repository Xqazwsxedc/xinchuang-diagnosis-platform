# Shared Vibe Coding Context

本目录保存所有 AI 角色共同遵守的项目上下文。任何角色开始工作前，应先读取：

1. `PROJECT_SPEC.md`
2. `.ai-context/shared/project-overview.md`
3. `.ai-context/shared/source-map.md`
4. `.ai-context/shared/verification-baseline.md`
5. `.ai-context/shared/feature-change-workflow.md`
6. 涉及数据库时读取 `docs/数据库设计说明书_v1(1).docx`、`docs/context/requirements/database-design.md`、`docs/standards/database-standard.md`
7. 与本次任务相关的 `docs/context`、`docs/standards`、`docs/modules`

当前项目正在从普通前后端项目升级为企业级 Vibe Coding 工程。第 5、6、7、8、9、10 步已完成，后端当前路径为 `apps/api-server/`，前端当前路径为 `apps/admin-web/`，项目级启动、构建、验证入口位于 `scripts/`，结构交接文档为 `docs/context/enterprise-structure-handoff.md`。

新增或修改功能时，当前 Codex 主对话按 `feature-change-workflow.md` 串联项目经理、后端、前端、测试和部署视角，用户不需要分别创建多个对话或手动分发任务。

后续数据库开发必须严格对齐 `docs/数据库设计说明书_v1(1).docx`。当前已落地数据库与说明书存在命名差异时，以 `PROJECT_SPEC.md` 数据库模块中的映射和兼容迁移策略为准。
