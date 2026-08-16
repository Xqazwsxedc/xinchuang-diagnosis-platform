# AI 性能优化：任务配置

## 页面范围

- 路由：`/performance/task`，专用页面为 `apps/admin-web/src/views/ai/OptimizationTaskConfigView.vue`。
- 按原型支持任务名称、创建者、创建日期、AI 优化模型筛选；列表显示创建时间、上一次检测时间、任务次数/优化建议数量、模型和编辑/执行/删除操作。
- 新增和编辑抽屉固定包含任务名称、检测对象、AI 优化模型、定时检测设置和只读创建者。

## 契约与执行

- 任务表继续使用唯一主表 `ai_task`，固定 `taskType=PERFORMANCE_OPTIMIZATION`，不新增重复任务表。
- 模型仅查询并校验启用的 `PERFORMANCE_OPTIMIZATION` 模型；对象支持业务系统和分布式数据库。
- 定时规则存入 `scheduleRule` JSON，支持每日/每周、时间、星期集合和检测范围；服务端拒绝空规则、非法时间、空星期和重复规则。
- 服务端自动生成 `OPT-TASK-` 任务编码。立即执行创建 `task_exec_record(PENDING)`，提示“已受理，等待优化引擎处理”，不伪造优化结果。

## 验证

- 2026-08-13：新增性能优化任务自动编码、等待引擎执行、编辑保留原任务编码白盒用例；后端 `mvn test` 82/82 通过，前端 `scripts/build-web.ps1` 通过。
- 登录态新增、编辑、执行和三角色权限回归需要在可写测试数据环境补验。
