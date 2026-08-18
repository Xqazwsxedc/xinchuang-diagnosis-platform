# AI Backend Compact

职责：维护 Spring Boot API、数据库实体、权限拦截、服务逻辑、审计日志和参数校验。

当前后端路径：`apps/api-server/`。

必须先读：

- `PROJECT_SPEC.md`
- `.ai-context/shared/source-map.md`
- `docs/standards/backend-coding-standard.md`
- `docs/standards/database-standard.md`
- `docs/context/requirements/database-design.md`
- `docs/standards/permission-standard.md`
- 相关模块文档

验证：

```powershell
cd "E:\vibe work\springboot work\apps\api-server"
mvn -DskipTests compile
```

系统管理近期约束：

- 用户分页必须批量加载角色关系，禁止恢复逐行角色查询。
- 用户、角色、菜单、字典、配置、通知写操作必须同时保证 DTO 校验、事务和操作日志。
- 菜单层级变更必须校验父节点存在性及循环引用；角色/菜单权限必须与 `JwtAuthInterceptor` 和初始化菜单编码同步。
- 系统管理初始化菜单固定为五个侧栏入口；菜单权限、字典、配置能力以隐藏按钮权限和组合页面方式保留，不得删除既有 CRUD 接口和权限编码。
- `SystemBootstrapInitializer` 更新既有内置菜单时必须保持 `sys_menu.permission_code` 唯一，必要时通过保留原权限所属菜单 ID 调整名称和层级，禁止产生编码互换时的短暂重复。
- 安全登录管控的只读接口为 `GET /system/security/login-logs`，由 `system:security:view` 拦截，复用 `AuditLogService`，不复制登录日志查询逻辑。

数据台账近期约束：

- 必须保持 `ledger_business_system`、`ledger_db_resource`、`ledger_business_system_db` 的既有映射和公开 API 字段兼容性。
- 台账写操作必须具备 DTO 校验、事务边界、对象存在性校验和审计一致性；列表关联数据必须批量加载。
- `SystemBootstrapInitializer` 仅对 `admin`、`operator`、`viewer` 三个预置角色精确同步文档默认授权，禁止以初始化逻辑覆盖自定义角色。
- 详情数据新增表必须使用 `bus_system_data_record`、`db_instance_metric`、`db_instance_log`，逻辑关联当前 `ledger_*` 主表；不得为页面演示创建未登记的临时表。
- 台账 CSV 下载复用 `report_export_log`，`report_type` 使用 `BUSINESS_DATA`、`DB_METRICS`、`DB_LOG` 并同时记录操作日志；下载接口必须验证资源和记录归属。

测试 2.0 后端基线：

- 新增 `BusinessSystemServiceImplV2Test`，覆盖关联数据库缺失、关联数据库不存在、详情不存在和删除关联清理/审计。
- 新增 `DbResourceServiceImplV2Test`，覆盖资源不存在、主表/关系表关联保护和正常删除审计。
- 新增 `LedgerRequestValidationV2Test`，覆盖状态、关联 ID、IP、时间范围和日志级别约束。
- `mvn verify` 为 35/35 通过，JaCoCo 行覆盖率 10.61%；后续优先补用户、角色、菜单、详情服务和 AI 状态流转的写入、事务与并发分支。

故障知识库近期约束：

- `kb_instance` 为唯一知识主表，真实附件必须落 `kb_attachment`；禁止继续把 `attachment_names` 作为附件事实来源，旧字段仅兼容回写。
- 上传文件必须通过 `KnowledgeFileStorageService`，禁止客户端路径、原文件名直存或无白名单类型；限制 20MB/个、10 个/知识，图片写后必须校验。
- 知识和附件均逻辑删除；下载、预览必须先校验知识和附件均未删除。`operator` 不得获得 `ai:knowledge:delete`。
- 历史兼容：`kb_instance.created_by` 允许旧演示数据为空；列表和详情组装 VO 时必须先判断 ID 非空，再查询创建人，不能直接对不可变空 Map 使用空键查询。未处理异常由 `GlobalExceptionHandler` 记录服务端堆栈，接口仍返回统一 500 响应。

故障预测任务近期约束：

- `ai_task` 是故障预测任务唯一主表，`task_type=FAULT_PREDICTION` 时必须选择已启用且同类型的 `ai_model`；不得为页面另建任务或调度规则表。
- `schedule_rule` 必须是 JSON 数组。每条规则只允许 `daily`、`weekly`，时间为 `HH:mm`，每周规则至少一个星期，预测范围只允许 `1/2/4/12/24` 小时；`metric_codes` 至少一项。
- 人工执行故障预测任务只创建 `task_exec_record(PENDING)`，`end_time` 必须为空，审计文案为等待引擎；不得伪造 `SUCCESS` 或生成报告。真实引擎后续负责 `PENDING -> RUNNING -> SUCCESS/FAILED` 和 `fault_report` 写入。
- `/ai/tasks` 可选筛选参数为 `modelId`、`creatorName`、`createdDate(yyyy-MM-dd)`；创建人回填必须批量查询，旧数据 `created_by=null` 必须兼容。
- 故障预测任务另提供 `/ai/fault-prediction/tasks` 专用 DTO/VO 接口，覆盖列表、详情、保存、删除和人工受理；旧 `/ai/tasks` 继续兼容。`FaultPredictionTaskServiceImpl` 不保留未使用的 `AiTaskMapper` 依赖，专用列表根路径必须有精确 `GET` 权限规则。
- 2026-08-05 后端 `mvn verify` 为 49/49 通过，JaCoCo 行覆盖率 17.37%（826/4756）；运行时健康检查 200，未登录专用列表 401。登录态写接口和 AI 引擎回调仍待独立测试凭据/引擎接入后验证。
- 故障智能预警使用 `fault_report` 和 `result_status_log`，专用接口为 `/ai/fault-prediction/alerts`；预警写入仍由未来引擎负责，人工端只允许处理和管理员删除。服务层状态机固定为 `PENDING -> CONFIRMED/FALSE_POSITIVE`、`CONFIRMED -> CLOSED`，专用处理备注不能为空；operator 只有 `ai:fault:view`、`ai:fault:handle`，不得保留预警新增、编辑、删除权限。
- 2026-08-05 后端全量为 52/52 通过，JaCoCo 18.04%（879/4872）；隔离实例专用预警接口未登录返回 401。
- 2026-08-06 故障任务兼容修复：旧演示 `schedule_rule` 缺少 `predictionWindowHours` 时，DTO 缺省按 4 小时处理；`data.sql` 已补齐完整规则字段。全局校验异常返回首个字段错误或具体消息，不再一律返回笼统“请求参数不合法”。任务主表、接口字段和服务校验边界未改变；后端 `mvn test` 52/52 通过。
- 2026-08-11 专用故障任务编辑接口不接收 `taskCode`；`FaultPredictionTaskServiceImpl.update()` 必须继承原任务编码后再调用通用更新服务，避免通用编码必填校验阻断编辑。对应白盒回归已加入，后端 `mvn test` 53/53 通过。

缺陷知识库近期约束：

- 缺陷知识与故障知识共用 `kb_instance`、`kb_attachment` 和 `/ai/knowledge` 附件接口，不新增重复表或重复存储链路。
- 缺陷页面必须传 `knowledgeType=DEFECT`；服务层必须保留类型字段，查询端按知识类型隔离，附件详情必须校验所属知识未逻辑删除。
- 旧 `attachment_names` 只作兼容回写，不作为真实附件来源；真实文件必须落 `kb_attachment` 并通过 `KnowledgeFileStorageService` 保存。
- 自动编码按类型生成：故障使用 `FAULT-KB-`，缺陷使用 `DEFECT-KB-`，优化使用 `OPT-KB-`；新增、编辑、删除审计文案按知识类型标识，避免缺陷操作被记录为故障操作。

缺陷检测任务近期约束：

- `ai_task` 继续作为唯一任务主表，`task_type=DEFECT_DETECTION`；保存必须校验缺陷检测模型已启用、目标对象存在和定时规则合法。
- 缺陷定时规则保存为 `schedule_rule` JSON 数组，服务端校验时间、每日/每周周期、每周星期和重复规则；不新增调度表。
- 当前未接入真实缺陷检测引擎，缺陷任务人工执行与故障任务一样写入 `task_exec_record(PENDING)`，结束时间为空，不能伪造 `SUCCESS` 或缺陷报表。

缺陷检测报表近期约束：

- `defect_report` 是缺陷检测报表唯一事实表；报表页的详情、筛选和 CSV 导出必须从该表读取，不新增演示专用表。
- `/ai/defect-reports` 保持旧接口兼容，新增 `defectCode`、`taskName`、`creatorName` 可选筛选；创建者名称必须批量回填，兼容历史空创建人。
- `/ai/defect-reports/export` 复用 `ai:defect:view` 权限，限制单次 100 条，必须校验全部导出 ID 有效并写操作审计；不接受前端传入的报表内容。
- 2026-08-13 报表回归：空导出清单在服务层拒绝；有效导出按创建者 ID 批量回填并输出 UTF-8 CSV 核心字段，白盒用例已覆盖。后端 `mvn test` 79/79 通过，未修改表结构或既有接口。
- 2026-08-13 性能优化知识库继续复用 `FaultKnowledgeServiceImpl` 的 `OPTIMIZATION` 类型分支、`kb_instance/kb_attachment` 和附件存储接口；本轮未修改后端表结构和接口。前端提交必须携带 `knowledgeType=OPTIMIZATION`，避免跨类型数据串入。
- 2026-08-13 性能优化任务纳入引擎任务范围：服务端生成 `OPT-TASK-` 编码，校验启用的同类型模型及每日/每周优化规则，执行记录固定 `PENDING` 等待引擎。优化建议继续以 `opt_report` 为事实表，新增任务名称/创建者筛选、批量创建者回填与 CSV 导出审计，单次上限 100 条。
- 性能优化任务更新请求不要求客户端回传内部 `taskCode`；通用更新服务必须继承原任务编码。2026-08-13 后端全量 `mvn test` 82/82 通过。
- 2026-08-13 权限回归修复：默认 `operator` 仅可新增、编辑、处理、查看缺陷/性能报表，不得拥有 `ai:defect:delete` 或 `ai:optimization:delete`。该默认授权由 `SystemBootstrapInitializer.ensureRoleMenus()` 精确同步；不得影响自定义角色。3.0 黑盒已验证两条删除请求均为 HTTP 403。
- 审计时间规则：`MybatisMetaObjectHandler.updateFill()` 必须强制覆盖实体中已有的 `updatedAt`。更新服务通常先读取实体，不能使用不会覆盖非空值的 `strictUpdateFill`，否则数据库和台账列表会错误保留创建/旧更新时间。
- Prometheus 指标已接入：`monitoring/prometheus` 负责 `/api/v1/query`、`/api/v1/query_range`、配置化 PromQL、目标映射、范围/数值解析；`DbPrometheusMetricsService` 将五类指标转换为既有台账 DTO，`LedgerDetailServiceImpl` 仅负责资源校验、导出审计和 `PrometheusException -> BusinessException(503)`。不得让 Controller 或 Vue 拼 PromQL；不得将错误静默回退为 `db_instance_metric`。日志接口仍从 `db_instance_log` 查询。
- 2026-08-17 运行时修复：`PrometheusHttpClient` 不得将带 `{instance=...}` 的 PromQL 直接交给 Spring URI 模板构造器，否则花括号会被当作变量而抛出 500。必须先百分号编码查询参数并以 `URI` 传入 `RestClient`；`PrometheusHttpClientTest` 必须保留该标签选择器回归场景。
- 数据库资源接入管理第二阶段需求已导入。实施时必须优先兼容扩展 `ledger_db_resource`，统一 `PENDING/INSTALLING/VERIFYING/NORMAL/ABNORMAL` 状态；令牌只存哈希且只能在生成安装会话响应中返回一次明文。不得把 Docker IP、Prometheus instance、真实公网地址或数据库密码写入业务代码、日志或 DTO。
- 2026-08-18 指标下载接口保留旧 `range` 兼容，并支持 `startDate/endDate/metrics`。日期由 `PrometheusRangeResolver` 按 Asia/Shanghai 自然日转换，最多 90 天；`metrics` 只允许 `cpuUsage/memoryUsage/cacheHitRate`，服务按白名单查询并生成对应 CSV 列。下载仍必须校验资源 `monitor_status=NORMAL`、写入导出审计，禁止回退演示指标。
