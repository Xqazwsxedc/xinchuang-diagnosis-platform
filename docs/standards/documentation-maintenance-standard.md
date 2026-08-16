# Documentation Maintenance Standard

## Required Rule

每轮项目修改结束前必须更新 `PROJECT_SPEC.md`，不依赖用户提醒。

## What To Update

根据改动同步更新：

- 当前项目结构。
- 数据库表、字段、枚举。
- 接口入参、出参、路径。
- 前端页面和路由。
- 权限编码。
- 已完成内容。
- 待实现内容。
- 验证结果。

## Remove Outdated Content

更新文档时必须删除或改正：

- 已实现但仍写成后续的内容。
- 已废弃路径。
- 已变更字段名。
- 与代码不一致的接口说明。
- 过时的阶段计划。

## Vibe Coding Context

当任务涉及企业级协作时，还需更新：

- `.ai-context/shared/*`
- 对应角色的 `compact.md`
- `docs/context/*`
- `docs/modules/*`
- `docs/standards/*`

每轮结束前必须检查 `ai-project-manager`、`ai-backend`、`ai-frontend`、`ai-test`、`ai-deploy` 和 `shared` 是否受影响。受影响目录必须更新对应 `compact.md` 或共享基线；未受影响角色也必须在本轮交接记录中标明无影响。

## Verification

文档更新后至少执行：

```powershell
rg -n "关键词" PROJECT_SPEC.md docs .ai-context
```

确认新内容可被后续 AI 检索到。
