# Data Ledger Requirements

## Source Summary

Word 需求要求数据台账支持业务系统和分布式数据库列表，提供名称、创建人、数据状态筛选，以及新增、编辑、查看、删除。详情页展示基础信息、业务数据、服务器指标、服务器日志，并提供下载入口。

## Current Status

部分实现。

已实现：

- 业务系统 CRUD。
- 分布式数据库资源 CRUD。
- 业务系统关联多个数据库。
- 列表筛选、分页、详情抽屉。
- 数据大屏基于台账数据做概览统计。
- 业务系统、分布式数据库均提供独立详情页，列表“查看详情”跳转详情路由。
- 业务系统详情展示业务数据收集记录，并支持选中记录批量或单条 CSV 下载。
- 分布式数据库详情展示基础信息、CPU/内存/磁盘 IOPS/MySQL 当前连接数、缓存命中率趋势与服务器日志；支持指标下载、按选中日志下载及时间范围筛选。
- 下载行为写入 `report_export_log`，并同步写入操作日志。
- 保存请求对名称、负责人、状态、关联数据库数量及关联 ID 做边界校验；IP 地址按 IPv4 0-255 段校验。
- 业务系统、数据库资源的新增、编辑、删除均有事务边界；分页列表批量加载关联数据，避免逐行关联查询。
- 台账写接口使用 `ledger:*` 动态权限编码：`admin`、`operator` 可维护，`viewer` 仅可查询。

部分实现或未实现：

- 业务数据与服务器日志仍为数据库初始化的演示数据，尚未接入真实日志采集任务。
- 数据库资源详情的五项监控指标已由 Spring Boot 代理 Prometheus HTTP API 查询；指标不回写 `db_instance_metric`，该表保留为历史演示和未来采集扩展兼容表。Prometheus 基础地址或目标映射未配置、网络超时或返回异常时，接口返回可识别的 `503`，页面仅显示监控区域告警，不回退伪造演示数据。
- 下载生成 CSV 内容并记录审计，尚未接入文件对象存储或长期文件留存。

## Current Tables

- `ledger_business_system`
- `ledger_db_resource`
- `ledger_business_system_db`
- `bus_system_data_record`
- `db_instance_metric`
- `db_instance_log`
- `report_export_log`

## Current APIs

- `/api/ledger/business-systems`
- `/api/ledger/db-resources`
- `/api/dashboard/overview`
- `/api/ledger/business-systems/{id}/data-records`
- `/api/ledger/business-systems/{id}/data-records/download`（可选 `recordIds`，逗号分隔、最多 100 条）
- `/api/ledger/business-systems/{id}/data-records/{recordId}/download`
- `/api/ledger/db-resources/{id}/metrics`
- `/api/ledger/db-resources/{id}/metrics/download`
- `/api/ledger/db-resources/{id}/logs`
- `/api/ledger/db-resources/{id}/logs/download`（可选 `logIds`，逗号分隔、最多 100 条）

## Quality Baseline (2026-07-27)

- 未改变既有接口路径、请求字段或响应结构；兼容保留业务系统保存请求中的 `databaseId` 字段。
- 业务系统关联关系保留现有“物理替换后重建”语义，避免改变关联表唯一约束与历史行为。
- 非法状态筛选、空的业务系统保存请求均返回业务码 `400`，且不写入数据。
- `viewer` 查询台账返回 HTTP `200`，对业务系统新增、删除返回 HTTP `403`。

## Detail Page Baseline (2026-07-29)

- 业务系统详情路由：`/data-ledger/business-systems/:id`；分布式数据库详情路由：`/data-ledger/db-resources/:id`。
- 新增表按数据库设计蓝图命名：`bus_system_data_record`、`db_instance_metric`、`db_instance_log`；现有 `ledger_*` 主表映射保持不变。
- `report_export_log.report_type` 在既有 AI 报告类型外，兼容扩展 `BUSINESS_DATA`、`DB_METRICS`、`DB_LOG`，不新建重复导出审计表。
- 详情查询和下载均复用 `ledger:visualization:view`；`admin`、`operator`、`viewer` 均可读和下载，新增、编辑、删除仍按既有写权限控制。
- 选中下载必须由后端重新校验记录归属、时间范围和 ID 合法性；不能仅依赖前端选中状态。

## Future Enterprise Enhancements

- 增加指标定义和采样表。
- 增加日志采集记录表。
- 增加附件/下载记录表。
- 增加数据库连接凭据的安全存储方案。

## UI Contract Update (2026-08-11)

- 数据可视化台账仅保留一个“新增”入口和一个 520px 新增抽屉；抽屉顶部使用“业务系统 / 分布式数据库”横向等分页签，在同一抽屉中切换两类资源表单。
- 业务系统新增表单展示业务系统名称、关联的分布式数据库、创建人、访问地址、备注说明；分布式数据库新增表单展示名称、公网 IP、内网 IP、创建人。提交区固定在抽屉底部。
- 健康状态仍按既有 API 契约随保存请求提交：新增默认 `NORMAL`，编辑保留既有值；该字段不在原型新增表单中展示，不得因此删除或变更后端字段。
- 业务系统关联数据库仍为至少一项的多选，保存仍兼容 `databaseId` 与 `databaseIds`，不得将多对多关联降级为单选。
- 新增页签依据 `ledger:business:create`、`ledger:database:create` 展示可用资源类型；编辑时锁定原记录类型，避免跨资源类型修改。

## Prometheus Detail Metrics Update (2026-08-17)

- 指标链路固定为 `Vue -> /api/ledger/db-resources/{id}/metrics -> Spring Boot -> Prometheus HTTP API`；前端禁止直连 Prometheus，服务器日志接口和台账 CRUD 不在本轮范围内。
- 支持 `1H/3H/6H/12H/1D/3D/7D/14D`。CPU、内存、磁盘 IOPS、当前连接数和缓存命中率均使用即时查询展示顶部值，使用区间查询生成趋势点；CSV 使用同一份实时查询结果，增加“当前连接数”列。
- 配置仅来自 `MONITORING_PROMETHEUS_*` 和 `MONITORING_TARGETS_*` 环境变量。当前单机验证目标映射写入未提交的 `deploy/windows/.env`，后续容器重建应替换为稳定映射方案。
