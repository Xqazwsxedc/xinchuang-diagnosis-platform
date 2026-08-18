# Requirements Context Index

需求来源：

- `信创数据库智能故障诊断及修复平台需求规格说明书v2.docx`
- `docs/数据库设计说明书_v1(1).docx`

本文档集将 Word 需求转换为 Vibe Coding 可读的模块上下文。后续实现或重构时，必须先阅读对应模块文档。

## Modules

- `base-platform.md`：基座能力，包括认证、权限、统一工程能力、通知、个人中心基础接口。
- `system-management.md`：系统管理，包括用户账号、角色权限、菜单按钮权限、数据权限。
- `data-ledger.md`：数据台账和数据大屏。
- `ai-modules.md`：AI 故障预测、AI 缺陷检测、AI 性能优化。
- `non-functional.md`：界面、安全、兼容性、数据准确性。
- `database-design.md`：数据库设计说明书的可检索摘要；后续数据库开发必须严格对齐。
- `prometheus-springboot-integration-prompt.md`：Prometheus 真实指标接入需求。已完成数据库资源详情的后端代理、配置化映射、五类指标、页面与 CSV 改造；真实环境联调仍需本机配置基础地址后执行。
- `database-monitoring-onboarding-prompt.md`：数据库资源客户自配 Prometheus 接入需求。旧安装会话/令牌/回调流程已停用；资源级 Prometheus 配置、双 selector、服务端连接测试、SSRF 防护和详情页配置入口已实现，真实 Prometheus 端到端验收待执行。

## Requirement Status Rules

每个需求只能标记为：

- 已实现：代码、页面、接口和数据结构已落地，并通过基础构建验证。
- 部分实现：已有一期数据壳或基础能力，但未覆盖完整需求。
- 未实现：当前代码没有真实能力。
- 暂不实现：已明确不属于当前阶段。

不要把未来计划写成已实现。
