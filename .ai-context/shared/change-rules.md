# Change Rules

任何 AI 角色改动项目时必须遵守：

1. 先读 `PROJECT_SPEC.md`。
2. 再读 `.ai-context/shared/feature-change-workflow.md` 和本任务相关的 `.ai-context`、`docs/context`、`docs/standards`、`docs/modules`。
3. 涉及数据库时必须先读 `docs/数据库设计说明书_v1(1).docx`、`docs/context/requirements/database-design.md`、`PROJECT_SPEC.md` 的数据库模块和 `docs/standards/database-standard.md`；后续数据库设计必须严格对齐说明书。
4. 涉及接口时必须同步后端 DTO/VO、前端 API 类型、页面调用和接口文档。
5. 涉及权限时必须同步菜单权限、按钮权限、后端拦截规则和验收说明。
6. 每步修改后都要做对应验证；后端跑 Maven 编译，前端跑 `pnpm build`。
7. 每轮结束前必须更新 `PROJECT_SPEC.md`，剔除过时内容。
8. 后端已迁移到 `apps/api-server/`，前端已迁移到 `apps/admin-web/`；不要再次移动已迁移目录，后续只在统一脚本和文档中引用新路径。
