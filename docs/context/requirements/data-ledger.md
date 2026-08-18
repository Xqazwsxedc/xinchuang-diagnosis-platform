# Data Ledger Requirements

## Source Summary

Word 需求要求数据台账支持业务系统和分布式数据库列表，提供名称、创建人、数据状态筛选，以及新增、编辑、查看、删除。详情页展示基础信息、业务数据、服务器指标、服务器日志，并提供下载入口。分布式数据库列表同时展示资源级监控接入状态；未成功接入监控时，当前数据状况统一显示为“未知”。

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

### 指标下载弹窗（2026-08-18）

- 数据库资源详情页点击“下载指标”必须先打开“下载指标数据”弹窗，不得直接下载。
- 弹窗支持开始日期、结束日期、最近 7 天/30 天/90 天、本月快捷选择；结束日期按自然日包含处理，后端最大日期跨度为 90 天。
- 指标选择采用白名单：`cpuUsage`（CPU利用率）、`memoryUsage`（内存占比）、`cacheHitRate`（缓存命中率），支持全选和清空；至少选择一项才允许确认下载。
- 既有下载路径保持不变，旧的 `range` 参数继续兼容；新参数为 `startDate`、`endDate`、`metrics`。后端按所选指标执行 Prometheus query_range，并仅生成所选 CSV 列，同时继续写入导出审计。
- 日期缺失、倒序、超过 90 天、指标为空或包含未知编码时返回明确业务错误；未接入监控资源仍返回 409，不得回退到演示表或其他资源指标。
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

## Database Monitoring Onboarding Status

- 旧的一次性安装会话、令牌、安装命令和回调流程已停用；平台不执行 SSH、安装脚本、Docker Compose、Prometheus 配置改写或自动 target 注册。
- 已实现资源级 Prometheus 连接配置：每个数据库资源独立保存 Prometheus 基础地址、凭据引用、数据库类型、node selector、database selector、状态和最近检查结果；新链路不读取旧全局 target，也不自动迁移旧配置。
- MySQL 强制使用两组精确 selector：node selector 查询 CPU、内存、磁盘 IOPS；database selector 查询当前连接数、缓存命中率。前端不拼 PromQL，后端只生成白名单模板。
- 已提供 `GET/PUT /api/ledger/db-resources/{id}/monitoring-config` 和 `POST /api/ledger/db-resources/{id}/monitoring-config/test`。测试服务端执行 Prometheus `up` 与 MySQL 健康指标查询，任一 selector 缺失或目标为空时状态为异常，已有配置不删除。
- 状态为 `UNCONFIGURED/TESTING/HEALTHY/ABNORMAL`；未通过测试的资源不能查询指标，避免资源串用。Prometheus 不可用时仅指标区域报错，日志和资源 CRUD 保持可用。
- 已加入 SSRF 防护：仅 HTTP(S)、默认拒绝、服务端域名/CIDR allowlist、禁止用户信息/回环/链路本地/云元数据地址，HTTP 重定向不自动跟随；selector 只允许安全 label 键和值。
- 旧 `MONITORING_PROMETHEUS_*` 和 `MONITORING_TARGETS_*` 配置暂时保留用于回滚兼容，但资源级查询不回退到旧配置。部署必须显式配置 `MONITORING_PROMETHEUS_ALLOWED_HOSTS` 或 `MONITORING_PROMETHEUS_ALLOWED_CIDRS`。
- 阶段五尚缺真实客户 Prometheus 的手工端到端验收：配置保存、连接成功/失败、目标不存在、资源切换隔离、指标区域局部错误和 CSV 下载需要在允许访问的 Prometheus 环境验证。

## Detail Empty State and Responsive Layout Update (2026-08-18)

- 分布式数据库详情的服务器日志为空时不挂载 `el-table`，只展示中文空状态“当前时间范围暂无服务器日志”，禁止出现 Element Plus 默认 `No Data`。
- 指标趋势区仅在存在趋势点时渲染四个图表和缓存命中率圆环；无指标时只展示“暂无指标数据”，不渲染空图表骨架。监控接口告警仍可单独显示。
- 中等宽度窗口下摘要与指标卡片使用两列布局，日志内容区自带横向滚动并保持表格最小可读宽度，避免详情摘要和日志区域错位；接口、数据库结构和下载契约不变。
