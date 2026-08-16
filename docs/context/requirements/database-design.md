# Database Design Blueprint

来源文件：`docs/数据库设计说明书_v1(1).docx`。

本文档是 Word 数据库设计说明书的 Vibe Coding 可检索摘要。后续任何数据库表、字段、索引、枚举、实体映射、接口入参出参相关开发，都必须先阅读本文件和原始 Word 说明书，再修改 `schema.sql`、`data.sql`、后端实体和前端类型。

## 权威规则

- `docs/数据库设计说明书_v1(1).docx` 是企业级数据库设计蓝图。
- `docs/context/requirements/database-design.md` 是该蓝图的可检索摘要。
- `apps/api-server/src/main/resources/schema.sql` 是当前已落地数据库执行脚本。
- 新增数据库能力必须优先按数据库设计说明书设计，不得只按临时页面字段随意建表。
- 当前已落地表如果与说明书命名不完全一致，必须在 `PROJECT_SPEC.md` 数据库模块中登记映射，并采用兼容迁移。
- 如说明书内部存在重复表述或字段名差异，必须先在计划中说明差异并让用户确认，不允许自行静默选择。

## 主业务链路

```text
数据台账
  -> 指标采集
  -> AI 任务配置
  -> 定时执行
  -> 生成故障预警 / 缺陷报表 / 优化建议
  -> 知识库检索或沉淀
  -> 人工确认 / 标记误报 / 关闭
  -> 数据大屏展示
```

## 模块与核心表

| 模块 | 对应 UI | 核心表 |
| --- | --- | --- |
| 用户权限模块 | 用户账号管理、角色权限管理、个人中心 | `sys_dept`、`sys_user`、`sys_role`、`sys_user_role`、`sys_menu_permission`、`sys_role_menu`、`sys_role_data_scope` |
| 数据台账模块 | 数据可视化台账、业务系统/分布式数据库新增编辑 | `bus_system`、`db_instance`、`bus_system_db` |
| AI 任务模块 | 故障预测任务配置、缺陷检测任务配置、性能优化任务配置 | `ai_model`、`ai_task`、`task_exec_record` |
| 知识库模块 | 故障知识库、缺陷知识库、优化知识库 | `kb_instance` |
| 结果管理模块 | 故障智能预警、缺陷检测报表、性能优化建议及详情页 | `fault_report`、`defect_report`、`opt_report` |
| 审计与配置模块 | 日志审计管理、系统基础配置、安全登录管控 | `result_status_log`、`operation_log`、`login_log`、`report_export_log`、`sys_dict_type`、`sys_dict_item`、`sys_config`、`sys_login_policy` |

## 表设计总览

| 表名 | 中文名称 | 用途 | 核心约束 |
| --- | --- | --- | --- |
| `sys_dept` | 部门表 | 保存组织部门信息，用于用户所属部门、数据权限和审计展示 | PK: `dept_id`；UK: `dept_code` |
| `sys_user` | 用户表 | 保存平台用户账号信息，对应用户账号管理和个人中心 | PK: `user_id`；UK: `username`；FK: `dept_id` |
| `sys_role` | 角色表 | 保存系统管理员、运维工程师、开发人员、只读用户等角色 | PK: `role_id`；UK: `role_code` |
| `sys_user_role` | 用户角色关联表 | 保存用户与角色授权关系 | 联合 PK: `user_id, role_id` |
| `sys_menu_permission` | 菜单权限表 | 保存目录、菜单、按钮和接口权限 | PK: `menu_id`；UK: `permission_code` |
| `sys_role_menu` | 角色菜单权限关联表 | 保存角色拥有的菜单、按钮、接口权限 | 联合 PK: `role_id, menu_id` |
| `sys_role_data_scope` | 角色数据权限表 | 保存角色数据权限范围 | PK: `role_data_scope_id` |
| `bus_system` | 业务系统表 | 保存业务系统信息 | PK: `system_id`；UK: `system_code` |
| `db_instance` | 数据库实例表 | 保存分布式数据库或普通数据库实例信息 | PK: `db_instance_id`；UK: `db_code` |
| `bus_system_db` | 业务系统数据库关联表 | 保存业务系统与数据库实例关联关系 | 联合 PK: `system_id, db_instance_id` |
| `ai_model` | AI 模型表 | 保存 AI 模型配置 | PK: `model_id`；UK: `model_code` |
| `ai_task` | AI 任务主表 | 统一保存故障预测、缺陷检测、性能优化任务 | PK: `task_id`；UK: `task_code` |
| `task_exec_record` | 任务执行记录表 | 保存 AI 任务每次执行状态、输入时间范围和结果数量 | PK: `execution_id` |
| `kb_instance` | 知识库表 | 统一保存故障、缺陷、优化知识 | PK: `knowledge_id`；UK: `knowledge_code` |
| `kb_attachment` | 知识库附件表 | 保存知识附件元数据和受控本地存储定位 | PK: `id`；逻辑关联 `knowledge_id` -> `kb_instance` |
| `fault_report` | 故障预警表 | 保存故障预测任务生成的预警结果 | PK: `report_id`；UK: `report_code` |
| `defect_report` | 缺陷检测结果表 | 保存缺陷检测任务生成的缺陷结果和报表详情 | PK: `defect_report_id`；UK: `defect_code` |
| `opt_report` | 性能优化建议表 | 保存 SQL 优化、索引优化、参数优化等建议 | PK: `report_id`；UK: `report_code` |
| `result_status_log` | 结果状态流转日志表 | 记录故障预警、缺陷报表、优化建议状态变化 | PK: `status_log_id` |
| `operation_log` | 操作日志表 | 记录新增、编辑、删除、导出、确认等操作 | PK: `operation_log_id` |
| `login_log` | 登录日志表 | 记录用户登录成功或失败情况 | PK: `login_log_id` |
| `report_export_log` | 报告导出日志表 | 记录结果报告导出行为 | PK: `export_log_id` |
| `sys_dict_type` | 字典类型表 | 保存任务类型、处理状态、知识类型等字典分类 | PK: `dict_type_id`；UK: `dict_code` |
| `sys_dict_item` | 字典项表 | 保存具体字典项 | PK: `dict_item_id`；UK: `dict_code, item_value` |
| `sys_config` | 系统配置表 | 保存系统级参数配置 | PK: `config_id`；UK: `config_key` |
| `sys_login_policy` | 安全登录管控表 | 保存账号锁定、密码有效期、登录失败次数等策略 | PK: `policy_id`；UK: `policy_key` |

## 主要关系

- 用户与角色：`sys_user` - `sys_user_role` - `sys_role`，多对多。
- 角色与权限：`sys_role` - `sys_role_menu` - `sys_menu_permission`，多对多。
- 角色与数据权限：`sys_role` - `sys_role_data_scope`，一对多。
- 菜单树：`sys_menu_permission.parent_id` 自关联。
- 业务系统与数据库：`bus_system` - `bus_system_db` - `db_instance`，多对多。
- AI 模型与任务：`ai_model` - `ai_task`，一对多。
- 任务与对象：`ai_task.target_type` + `target_id` 逻辑引用业务系统或数据库实例。
- 任务与执行记录：`ai_task` - `task_exec_record`，一对多。
- 执行记录与结果：`task_exec_record` - `fault_report` / `defect_report` / `opt_report`，一对多。

## 字典要求

业务表保存编码，前端或服务端通过字典项转换中文展示，避免中文状态值散落在表字段中。

| 字典编码 | 字典名称 | 建议取值 |
| --- | --- | --- |
| `TASK_TYPE` | 任务类型 | `FAULT_PREDICTION`、`DEFECT_DETECTION`、`PERFORMANCE_OPTIMIZATION` |
| `PROCESS_STATUS` | 处理状态 | `PENDING`、`CONFIRMED`、`FALSE_ALARM` |
| `KNOWLEDGE_TYPE` | 知识类型 | `FAULT`、`DEFECT`、`OPTIMIZATION` |
| `AI_MODEL_TYPE` | AI 模型类型 | `FAULT`、`DEFECT`、`OPTIMIZATION` |
| `HEALTH_STATUS` | 健康状态 | `NORMAL`、`WARNING`、`OFFLINE` |
| `EXECUTION_STATUS` | 执行状态 | `PENDING`、`RUNNING`、`SUCCESS`、`FAILED` |

## 重点字段要求

- 用户表必须支持头像字段：`avatar_url`。
- 用户表必须支持登录安全字段：`password_updated_at`、`failed_login_count`、`locked_until`。
- 组织部门必须纳入后续数据权限设计：`sys_dept`。
- 角色数据权限完整形态为 `sys_role_data_scope`；当前如采用 `sys_role.data_scope` / `data_scope_config` 过渡，必须保留向明细表迁移的空间。
- 业务系统与数据库必须支持多对多关系。
- AI 三大任务必须统一抽象到 `ai_task`，差异通过 `task_type`、`target_type`、`target_id`、`param_json`、`schedule_rule_json` 表达。
- 故障预测任务的 `schedule_rule` 保存规则 JSON 数组；每项含 `time`、`repeatType`、`weekDays`、`predictionWindowHours`，不为一期页面另建重复调度规则表。人工受理时必须新增 `task_exec_record` 并以 `PENDING` 表示等待外部 AI 引擎，未完成记录的 `end_time` 必须为空。
- 故障、缺陷、优化结果必须分别建表，不混放在单一结果表。
- 结果状态流转必须写入 `result_status_log`。
- 导出行为必须写入 `report_export_log`。
- 敏感配置、模型密钥、数据库账号密码不能明文放在业务主表，后续应拆分到加密凭据表、配置中心或密钥管理能力。
- 知识附件不得只以逗号分隔名称作为唯一事实来源；真实文件能力使用 `kb_attachment` 保存原文件名、随机存储名、相对路径、类型、扩展名、大小和逻辑删除状态。物理文件由独立清理任务处理，不随业务逻辑删除立即移除。

## 当前实现映射提醒

当前代码中存在已经落地并验证通过的兼容命名，例如：

- `ledger_business_system` 对应设计说明书 `bus_system`。
- `ledger_db_resource` 对应设计说明书 `db_instance`。
- `ledger_business_system_db` 对应设计说明书 `bus_system_db`。
- `sys_menu` 对应设计说明书 `sys_menu_permission`。
- 当前角色数据权限先落在 `sys_role.data_scope` / `sys_role.data_scope_config`，后续完整形态应向 `sys_role_data_scope` 扩展。
- `bus_system_data_record` 是业务系统详情的业务数据收集记录扩展表，逻辑关联 `ledger_business_system.id`。
- `db_instance_metric`、`db_instance_log` 是数据库实例详情的指标与日志扩展表，逻辑关联 `ledger_db_resource.id`。
- `report_export_log` 已按说明书落地；除 AI 报告类型外，已登记台账导出兼容类型 `BUSINESS_DATA`、`DB_METRICS`、`DB_LOG`，用于统一审计而不重复建表。

后续开发不得继续扩大命名差异。新增表和字段应优先贴近数据库设计说明书；如为了兼容当前代码必须采用不同命名，需要先更新 `PROJECT_SPEC.md` 的映射表并说明原因。
