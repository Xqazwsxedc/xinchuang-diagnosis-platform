# Prometheus 数据库指标接入测试记录

日期：2026-08-17  
范围：分布式数据库详情页的实时指标代理、CSV 与前端展示。

## 已执行

| 类型 | 用例 | 结果 | 证据 |
| --- | --- | --- | --- |
| 白盒 | matrix 响应解析 | 通过 | `PrometheusResponseParserTest` |
| 白盒 | vector 响应解析 | 通过 | `PrometheusResponseParserTest` |
| 白盒 | `NaN`、`Inf` 过滤 | 通过 | `PrometheusResponseParserTest` |
| 白盒 | 非 `success` 响应拒绝 | 通过 | `PrometheusResponseParserTest` |
| 白盒 | 1H 与 14D 范围、步长映射 | 通过 | `PrometheusRangeResolverTest` |
| 白盒 | 非法范围拒绝 | 通过 | `PrometheusRangeResolverTest` |
| 白盒 | CPU、内存、IOPS、连接数、缓存命中率合并 | 通过 | `DbPrometheusMetricsServiceTest` |
| 白盒 | 含 `{instance=...}` 的 PromQL URI 编码与 HTTP 请求 | 通过 | `PrometheusHttpClientTest` |
| 构建 | 后端全量 JUnit | 通过，91/91 | `apps/api-server/target/surefire-reports/` |
| 构建 | Vue 类型检查与生产构建 | 通过 | `scripts/build-web.ps1` |

## 待执行的真实环境验收

- 登录进入任一数据库资源详情，验证其余七个时间范围、四张实时卡片、四个趋势区域和 CSV 内容。
- 暂停或配置错误 Prometheus，验证指标接口为 HTTP 503/统一错误提示，页面摘要与日志页签仍可用。
- 使用 admin/operator/viewer 登录态验证读取和下载权限保持既有 `ledger:visualization:view` 边界。

## 2026-08-17 部署回归问题

- 真实登录态请求 `/api/ledger/db-resources/{id}/metrics?range=1D` 曾复现 HTTP 500；日志定位为 PromQL 标签花括号被 Spring URI 模板误解析，查询未发送到 Prometheus。
- 已修复为查询参数百分号编码，并新增 `PrometheusHttpClientTest` 防回归。后端全量 `mvn test` 为 92/92 通过；重新构建、重启后，同一登录态接口返回业务码 200、14 个趋势点，且 CPU/当前连接数均有实时数值。该 1D 实时查询场景通过。
