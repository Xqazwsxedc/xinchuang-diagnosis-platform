# AI 缺陷检测：任务配置

## 页面范围

- 路由：`/defect-detection/task`。
- 前端入口：`apps/admin-web/src/views/ai/DefectTaskConfigView.vue`。
- 已实现列表筛选、分页、新增、编辑、详情、立即检测和删除。
- 新增/编辑抽屉按原型实现任务名称、检测对象、AI 检测模型、定时检测设置和创建者只读展示。
- 详情抽屉展示任务名称、检测对象、创建者、创建时间、任务次数、上一次检测时间、AI 检测模型、定时规则和最近执行记录。

## 定时检测规则

- 每条规则包含 `time`、`repeatType`、`weekDays`、`detectionRange`。
- 重复周期支持 `daily`/`weekly`；每周规则至少选择一个星期；相同时间和周期的规则不能重复。
- 检测范围支持：`PREVIOUS_DAY`、`CURRENT_DAY`、`PREVIOUS_TWO_DAYS`，分别对应原型中的“只检测前一天的”“只检测当天的”“检测前两天的”。
- 规则以 JSON 数组保存到 `ai_task.schedule_rule`，不新增调度规则表。

## 接口与数据

页面复用现有 AI 任务接口，但所有请求固定使用 `taskType=DEFECT_DETECTION`：

- `GET /api/ai/tasks?taskType=DEFECT_DETECTION`：任务列表。
- `GET /api/ai/tasks/{id}`：详情。
- `POST /api/ai/tasks`、`PUT /api/ai/tasks/{id}`：新增/编辑。
- `POST /api/ai/tasks/{id}/execute`：立即检测。
- `DELETE /api/ai/tasks/{id}`：逻辑删除。
- `GET /api/ai/models?modelType=DEFECT_DETECTION&status=ENABLED`：模型选项。

主表仍为 `ai_task`，后端校验模型类型、模型启用状态、检测对象存在性、定时规则格式和重复规则。缺陷任务没有真实检测引擎时，立即检测只创建 `task_exec_record(PENDING)`，不伪造成功结果或缺陷报表。

## 权限

复用 `ai:task:view/create/update/execute/delete`。admin 全权，operator 可查看、新增、编辑、立即检测但不可删除，viewer 仅查看；前端按钮控制只用于体验，后端拦截器是最终边界。

## 验证

- 2026-08-11：前端 `vue-tsc --noEmit` 与生产构建通过。
- 2026-08-11：后端 `mvn test` 77/77 通过；新增缺陷任务定时规则和等待引擎白盒用例。
- 2026-08-11：详情抽屉按原型调整为纵向只读表单：圆角浅色输入框、定时规则卡片、独立滚动正文和底部固定“关闭”按钮；移除原型中不存在的执行记录表格。
- Figma Make 源码本次因 MCP View seat 调用额度限制无法继续读取，页面视觉以用户提供的四张原型图和现有故障任务页面规范为准；登录态浏览器视觉、CRUD 和执行接口仍需在可写测试环境补验。
