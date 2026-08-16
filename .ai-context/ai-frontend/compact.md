# AI Frontend Compact

职责：维护 Vue 3 管理端、页面体验、前端 API 类型、Pinia 状态、路由和权限展示。

当前前端路径：`apps/admin-web/`。

必须先读：

- `PROJECT_SPEC.md`
- `.ai-context/shared/source-map.md`
- `docs/standards/frontend-coding-standard.md`
- `docs/standards/api-contract-standard.md`
- 相关模块文档

验证：

```powershell
cd "E:\vibe work\springboot work\apps\admin-web"
pnpm build
```

沙箱内如遇 Vite `EPERM`，按共享基线中的提权命令验证。

数据台账详情页约束：

- 列表“查看详情”必须跳转独立路由，不恢复为详情抽屉；业务系统为 `/data-ledger/business-systems/:id`，数据库为 `/data-ledger/db-resources/:id`。
- 详情页必须使用真实详情、记录、指标和日志接口；下载通过 Blob 响应触发浏览器保存，并对 JSON 错误响应显示清晰提示。
- 原型图表通过既有 `/ledger/db-resources/{id}/metrics` 消费 Spring Boot 转换后的 Prometheus 时间序列，时间范围与服务器日志查询保持一致；不得直连 Prometheus 或将 `db_instance_metric` 演示数据当作实时指标。
- 详情页顶部固定展示 CPU、内存、磁盘 IOPS、当前连接数四张卡片，趋势区展示 CPU、内存、当前连接数和缓存命中率。监控接口异常时只清空指标区并显示局部告警，资源摘要和服务器日志必须继续可用。
- Figma Make 原型中的业务记录和服务器日志均为“勾选后下载”：顶部下载按钮在无选中项时禁用，选中后展示数量；请求分别传递 `recordIds`、`logIds`，不可退化为前端假状态或无条件导出全量数据。
- 详情页视觉以 Figma Make `DataDetail.tsx` 为基准：40px 内容区、浅蓝基础信息栏、12px 面板圆角、分段页签和交替表格行；移动端收缩为单列且工具栏不遮挡内容。
- Element Plus 页签的选中态需要使用 flex 双向居中；禁止仅靠固定 `line-height` 对齐，以免蓝色页签内文字偏移。
- 业务系统详情的业务数据表为空时，表格 `#empty` 插槽必须保持无文字；只展示表格下方统一的 `el-empty` “暂无业务数据记录”状态，禁止显示 Element Plus 默认英文 `No Data`。

系统管理菜单约束：

- Figma 正式侧栏固定为“用户账号管理、角色权限管理、日志审计管理、系统基础配置、安全登录管控”五项。
- 菜单权限管理必须嵌入 `/system/roles` 的页签；字典管理和系统配置必须嵌入 `/system/config` 的页签。`/system/menus`、`/system/logs`、`/system/dicts`、`/system/configs` 仅保留兼容直达路由。
- `/system/security` 使用 `system:security:view`，通过 `/system/security/login-logs` 查询真实登录日志；不得以无数据的占位页替代。
- 后端菜单或权限发生变化时，`MainLayout.vue` 必须在挂载时调用 `/auth/me` 覆盖本地会话缓存；禁止仅依赖 localStorage 中的旧 `menus` 渲染动态侧栏。

故障知识库近期约束：

- `/fault-prediction/knowledge` 使用 `FaultKnowledgeView.vue`，不再为该路由扩展通用 `AiModuleView.vue`。
- 新建、编辑必须以 `FormData` 提交知识字段、保留附件 ID 和新增文件；取消编辑不得请求删除已有附件。
- 图片以受鉴权 Blob 预览，所有允许格式通过受鉴权 Blob 下载；前端限制只作体验控制，服务端为最终安全边界。
- `main.ts` 采用 Element Plus 按需注册时，页面使用 `ElUpload` 必须同时导入组件和 `upload/style/css`；遗漏注册会使上传控件无法工作。知识库上传区必须保留格式 `accept` 过滤、20MB/10 个提示、待上传文件队列及保存前移除能力，实际写入只发生在 `FormData` 保存请求。

故障预测任务近期约束：

- `/fault-prediction/task` 必须使用 `FaultTaskConfigView.vue`，不得回退为通用 `AiModuleView.vue`；页面必须保留原型的列表筛选、任务抽屉、详情抽屉和预警跳转。
- 模型仅加载已启用的 `FAULT_PREDICTION` 项；对象类型切换必须清空目标；固定 22 指标支持全选/清空；多条每日/每周规则的前端校验只作体验保障，后端为最终边界。
- “立即执行”提示等待 AI 引擎，不能显示预测已完成；详情必须重新请求任务详情，避免展示列表缓存的旧执行次数或调度规则。
- 按钮显示遵循 `ai:task:*`：admin 全权，operator 无删除，viewer 只读；隐藏按钮不是安全边界。
- 2026-08-05 后端故障任务专用接口契约已稳定为 `/ai/fault-prediction/tasks` 列表、详情、保存、删除、人工受理；前端构建通过。登录态页面交互和 Figma 视觉对照仍需在有测试账号的浏览器会话补验，不能用构建通过替代页面验收。
- `/fault-prediction/alert` 必须使用 `FaultAlertView.vue`，不再扩展通用 `AiModuleView.vue`。详情抽屉以原型为基准展示只读字段、置信度、`trendJson` 趋势、说明、建议和处理历史；页面不得提供新增、编辑预警。处理按钮依赖 `ai:fault:handle`，删除仅依赖 `ai:fault:delete`，viewer 不得看见处理按钮。
- 预警详情抽屉当前视觉契约：584px 宽，标题栏固定、正文独立滚动、底部固定三按钮“确认 / 误报 / 关闭”；趋势图区域 225px，说明和建议为浅色圆角只读框。按钮不满足状态机或权限时禁用，不能通过前端绕过后端。
- 2026-08-06 预警详情的首屏字段必须直接写在 `FaultAlertView.vue` 模板中，不能使用运行时字符串模板组件；顺序固定为故障 ID、预测对象、告警时间、指标类别、预测故障类型、预计发生时间。`open`、`opened` 和详情加载完成后都要将 `.alert-drawer .el-drawer__body` 复位到顶部，避免用户只看到趋势图及其后的内容。前端生产构建通过，登录态视觉复核待补。
- 2026-08-06 `/system/roles` 使用 `RolePermissionView.vue` 作为正式 Figma 对齐入口：固定双栏，左侧角色列表，右侧菜单权限、按钮权限和数据权限页签。必须复用 `/system/roles`、`/menus` 既有接口；菜单保存继续补齐父级，数据权限继续使用角色更新接口。无对应写权限时保存按钮禁用，不能只依赖后端拒绝。`main.ts` 的 Element Plus 按需注册已补 `ElRadio` 与样式；前端生产构建通过。
# 最近前端调整

- `RolePermissionView.vue` 的权限页签必须占满权限配置区域，三个页签等宽；选中态为整块蓝色背景，不能随文本宽度收缩。
- 2026-08-06 故障预测任务抽屉修复：`FaultTaskConfigView.vue` 编辑历史任务时将缺失的 `predictionWindowHours` 归一化为 4，避免规则文本出现 `undefined` 或保存时遗漏必填字段；定时预测设置按 Figma 原型改为规则列表、添加规则卡片、时间/预测范围/重复周期纵向全宽布局，创建者移至底部只读展示。前端 `scripts/build-web.ps1` 通过。
- 2026-08-06 定时预测设置宽度二次修正：给 `.schedule-form-item .el-form-item__content` 和规则卡片显式设置 `width: 100%`、`min-width: 0`，避免 Element Plus 表单内容区将原型中的全宽布局压缩成窄列；前端生产构建再次通过。

缺陷知识库近期约束：

- `/defect-detection/knowledge` 使用 `DefectKnowledgeView.vue`，不得继续使用 `AiModuleView.vue` 占位页。
- 缺陷页面复用故障知识库的 FormData 附件交互、图片 Blob 预览、文件 Blob 下载、待上传队列和已有附件保留清单，但所有新增/编辑请求必须固定提交 `knowledgeType=DEFECT`。
- 列表固定按 `DEFECT` 查询；编辑详情返回非 `DEFECT` 时必须阻止提交，避免跨知识类型编辑。
- 模块说明见 `docs/modules/ai-defect-detection/knowledge-base.md`。

缺陷检测任务近期约束：

- `/defect-detection/task` 使用 `DefectTaskConfigView.vue`，不得回退到 `AiModuleView.vue`；页面固定查询 `taskType=DEFECT_DETECTION`，模型只加载已启用的 `DEFECT_DETECTION` 模型。
- 缺陷任务抽屉按原型保留新增/编辑/详情三种状态，定时规则支持每日/每周、时间、检测范围和规则删除；取消不发送请求，编辑保留任务编码以兼容通用任务接口。
- 缺陷任务详情抽屉视觉契约：580px 宽，字段按任务名称、检测对象、创建者、创建时间、任务次数、上一次检测时间、AI 检测模型、定时规则纵向排列；使用禁用输入框和定时规则卡片，正文滚动、底部固定“关闭”，不得改回描述表格或在详情首屏插入执行记录表格。

缺陷检测报表近期约束：

- `/defect-detection/report` 使用 `DefectReportView.vue`，不得回退为通用 `AiModuleView.vue`。
- 页面必须保留缺陷 ID、任务名称、创建者、结果确认筛选，多选/全选及真实 Blob CSV 导出；无选中记录时导出按钮禁用。
- 详情按原型使用居中报告弹窗，顺序为任务摘要、缺陷问题、缺陷数据、缺陷描述；不可把通用 CRUD 编辑表单或处理动作插入该详情。
- 2026-08-13 修复报表列表“缺陷 ID”筛选参数：前端 `AiQuery` 与 `DefectReportView.vue` 必须传 `defectCode`，与后端 `AiPageQuery.defectCode` 对齐；不得恢复为 `defectId`。前端 `vue-tsc --noEmit` 和生产构建通过。
- 2026-08-13 性能优化知识库使用专用 `OptimizationKnowledgeView.vue`，路由 `/performance/knowledge` 不得回退到 `AiModuleView.vue`；列表和抽屉固定使用 `knowledgeType=OPTIMIZATION`，附件交互复用故障/缺陷知识库的真实上传、保留、删除、预览和下载实现。前端构建通过，登录态写操作待补验。
- 2026-08-13 性能优化的 `/performance/task`、`/performance/suggestion` 分别使用 `OptimizationTaskConfigView.vue`、`OptimizationSuggestionView.vue`，不得回退到 `AiModuleView.vue`。任务页固定 `PERFORMANCE_OPTIMIZATION` 和模型类型，建议页使用真实 Blob CSV 导出及详情确认/误报处理。

数据台账新增抽屉契约（2026-08-11）：

- `DataLedgerView.vue` 的新增动作必须打开唯一的“新增”抽屉；顶部使用 `业务系统 / 分布式数据库` 等宽页签，不再以两个独立新增抽屉呈现。
- 新增页签必须分别遵循 `ledger:business:create`、`ledger:database:create`；无权限类型禁用，编辑必须锁定资源类型。切换新增页签需重置未提交表单与校验，不能混用两类字段。
- 业务系统表单保留数据库多选及 `databaseId`/`databaseIds` 请求兼容；原型未显示健康状态，新增默认 `NORMAL`、编辑保留既有状态，禁止删除 API 的 `status` 字段。
- 本轮仅改前端交互和样式，`scripts/build-web.ps1` 已通过；登录态新增、两页签切换、必填校验及保存回归仍需在可写测试数据环境补测。
