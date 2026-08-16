# Database Standard

## Source Of Truth

数据库设计采用“设计蓝图 + 落地脚本 + 映射记录”三层准绳：

- 设计蓝图：`docs/数据库设计说明书_v1(1).docx`
- 可检索摘要：`docs/context/requirements/database-design.md`
- `apps/api-server/src/main/resources/schema.sql`
- `apps/api-server/src/main/resources/data.sql`
- `PROJECT_SPEC.md` 数据库模块

后续数据库新增或修改必须先对齐数据库设计说明书。当前 `schema.sql` 代表已落地运行状态，但不能作为继续偏离设计说明书的理由。

如果当前代码与说明书存在表名或字段名差异，必须在 `PROJECT_SPEC.md` 数据库模块记录映射和迁移策略，再继续开发。

## Naming

- 新增表优先使用数据库设计说明书中的命名。
- 系统权限表优先使用 `sys_dept`、`sys_user`、`sys_role`、`sys_user_role`、`sys_menu_permission`、`sys_role_menu`、`sys_role_data_scope`。
- 数据台账表优先使用 `bus_system`、`db_instance`、`bus_system_db`。
- AI 任务表优先使用 `ai_model`、`ai_task`、`task_exec_record`。
- 知识库表使用 `kb_instance`。
- 知识库附件使用 `kb_attachment`，通过 `knowledge_id` 逻辑关联 `kb_instance`；不得将客户端绝对路径或可执行存储路径写入数据库。
- 结果表使用 `fault_report`、`defect_report`、`opt_report`。
- 审计、导出和状态流转表使用 `result_status_log`、`operation_log`、`login_log`、`report_export_log`。
- 字典、配置和登录策略表使用 `sys_dict_type`、`sys_dict_item`、`sys_config`、`sys_login_policy`。
- 当前已落地的 `ledger_*`、`sys_menu` 等兼容命名不得继续扩散；新增差异命名必须先说明原因并更新映射。

## Common Fields

业务表默认包含：

- 说明书主键字段，例如 `user_id`、`role_id`、`system_id`、`db_instance_id`、`task_id`
- `created_at`
- `updated_at`
- `created_by`
- `updated_by`
- 逻辑删除字段优先按说明书使用 `is_deleted`；当前已落地表可保留 `deleted`，但需要在映射中说明。

## ID Handling

- 后端使用 MyBatis-Plus 雪花 ID 或与说明书兼容的 `BIGINT UNSIGNED` 主键。
- 后端通过 Jackson 将 Long 转成字符串。
- 前端所有 ID 按 `string` 处理。

## Required Relationships

- 用户与角色必须使用用户角色关联表表达多对多关系。
- 角色与菜单、按钮、接口权限必须使用角色权限关联表表达多对多关系。
- 业务系统与数据库实例必须支持多对多关系。
- AI 三大任务必须统一使用 `ai_task` 表承载，差异通过类型、对象和 JSON 参数表达。
- 故障预警、缺陷报表、性能优化建议必须分别建表。
- 结果处理状态必须写入 `result_status_log`。
- 导出行为必须写入 `report_export_log`。

## Dictionary Rules

业务表保存编码，展示层转换中文。至少需要维护以下字典：

- `TASK_TYPE`
- `PROCESS_STATUS`
- `KNOWLEDGE_TYPE`
- `AI_MODEL_TYPE`
- `HEALTH_STATUS`
- `EXECUTION_STATUS`

## Change Rules

- 数据库变更前必须先读 `docs/数据库设计说明书_v1(1).docx`、`docs/context/requirements/database-design.md` 和 `PROJECT_SPEC.md` 数据库模块。
- 数据库变更必须先设计映射，再更新 `schema.sql`。
- 已有库兼容字段必须通过初始化器补齐。
- 不直接删除字段。
- 不强制引入真实外键，优先通过业务逻辑维护关联。
- 附件记录和知识均使用逻辑删除；删除附件时保留物理文件，交由后续受控清理任务处理。
- 枚举值必须写入文档。
- 说明书内部存在重复或冲突表述时，必须先记录差异并让用户确认，不得静默选择。
- 敏感配置、模型密钥、数据库账号密码不得明文放在业务主表。

## Current Important Fields

- `sys_user.avatar_url`
- `sys_user.password_updated_at`
- `sys_user.failed_login_count`
- `sys_user.locked_until`
- `sys_role.data_scope`
- `sys_role.data_scope_config`
- `sys_role_data_scope` 是角色数据权限完整形态的后续扩展目标。
- 业务系统详情的收集记录使用 `bus_system_data_record`；数据库详情的采样和日志使用 `db_instance_metric`、`db_instance_log`。三者均通过逻辑 ID 关联当前兼容主表，不强制新增真实外键。
- 台账 CSV 导出统一复用 `report_export_log`，允许的台账扩展 `report_type` 为 `BUSINESS_DATA`、`DB_METRICS`、`DB_LOG`；必须记录导出人、时间、文件名和成功/失败状态。

## Verification

数据库结构变更后必须：

1. 更新 `schema.sql` 和必要的兼容初始化器。
2. 更新实体、DTO、VO、Mapper、Service、Controller。
3. 更新前端 API 类型和页面。
4. 更新字典、菜单权限和初始化数据。
5. 运行后端编译。
6. 涉及前端时运行前端构建。
7. 更新 `PROJECT_SPEC.md`、`docs/context/requirements/database-design.md` 和相关模块文档。
