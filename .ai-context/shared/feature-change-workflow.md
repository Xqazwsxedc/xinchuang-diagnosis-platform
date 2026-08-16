# Feature Change Workflow

本文件记录用户新增或修改功能时的 Vibe Coding Harness 标准流程。用户不需要分别给不同 AI 角色安排任务；由当前 Codex 主对话按角色视角串联执行。

## 适用场景

- 新增业务功能。
- 修改已有功能。
- 修复影响业务行为的缺陷。
- 调整权限、菜单、接口、数据库或页面交互。
- 做性能优化、结构优化或工程脚本调整。

## 总体流程

```text
用户提出需求
  ↓
ai-project-manager：确认范围、边界、风险、验收标准
  ↓
ai-backend：评估并实现数据库、接口、服务、权限、审计
  ↓
ai-frontend：评估并实现页面、路由、API 调用、状态、按钮权限
  ↓
ai-test：补充并执行编译、构建、接口、页面和权限验收
  ↓
ai-deploy：确认启动脚本、构建脚本、环境变量和运行说明是否受影响
  ↓
文档收口：更新 PROJECT_SPEC.md、docs、.ai-context，剔除过时内容
```

## 五步执行图

项目汇报与交接使用 `docs/diagrams/vibe-coding-harness-workflow.svg` 及对应 PNG。该图把标准流程细化为以下五步，每步均需保留检查证据：

1. 需求与约束：接收需求、原型和截图；对齐 `PROJECT_SPEC.md`、数据库设计说明、权限和数据边界；产出确认单、约束表和验收口径。技术栈：Markdown、Figma、数据库设计文档。
2. Harness 规划：项目经理拆范围和风险，前后端确认接口与页面，测试和部署确定验证顺序与回滚点；产出任务拆解和回写清单。技术栈：Codex、`.ai-context`、Git。
3. 分层实现：按数据库、DTO/Service/Controller、权限校验/审计日志、Vue 页面/路由/联调的顺序落地；产出可运行的前后端功能。技术栈：Spring Boot、MyBatis-Plus、MySQL、Vue 3、TypeScript、Element Plus。
4. 测试与质量：依次执行 JUnit 白盒、接口黑盒与权限回归、页面联调和生产构建、SonarQube 扫描及修复复测；产出测试报告、问题清单和修复记录。技术栈：JUnit、Mockito、JaCoCo、Playwright、SonarQube。
5. 构建与本机部署：构建后端 Jar 和前端静态资源，配置 MySQL、Nginx、环境变量，启动服务并进行浏览器和健康检查；产出可访问版本。技术栈：Maven、Spring Boot Jar、Nginx、MySQL、PowerShell。

任一步验证失败，必须回到该阶段修复并重新验证；五步完成后更新 `PROJECT_SPEC.md`、`docs/` 和受影响的 `.ai-context`，再进入下一轮需求。

## 执行规则

1. 每次开始前先读 `PROJECT_SPEC.md`、`.ai-context/shared` 和任务相关模块文档。
2. 需求不清或风险较高时，先给实施计划并等待用户确认。
3. 低风险小改动可直接执行，但仍需说明影响范围和验证结果。
4. 涉及数据库时，先改 `schema.sql`，必要时补初始化数据或兼容初始化器。
5. 涉及后端接口时，同步 DTO、VO、Service、Controller、权限编码和操作日志。
6. 涉及前端页面时，同步 API 类型、路由、菜单、Pinia 状态和按钮权限。
7. 涉及权限时，同时验证 `admin`、`operator`、`viewer` 的差异行为。
8. 每一步做对应验证；后端变更跑 Maven 编译，前端变更跑 Vite 构建，跨端变更跑 `scripts/verify-all.ps1`。
9. 每轮结束前必须更新 `PROJECT_SPEC.md`，并检查项目经理、后端、前端、测试、部署及共享上下文是否受影响；所有受影响角色必须同步更新对应 `compact.md`、共享基线和 `docs/`，不依赖用户提醒。
10. 若某个角色本轮未受影响，也必须在交接或总结中明确“无影响”，防止遗漏判断。
11. 不为单个需求随意重排项目结构；结构变更必须先说明风险、迁移步骤和回滚边界。

## 用户输入方式

用户可以直接说：

```text
新增【功能名】，按咱们现在的流程来。
```

或：

```text
按 Vibe Coding Harness 流程修改【功能名】，每一步验证，最后更新 PROJECT_SPEC.md 和 .ai-context。
```

Codex 应自动按本流程串起各 AI 角色视角，无需用户手动拆分多个对话。
