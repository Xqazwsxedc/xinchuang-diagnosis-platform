# 客户自配 Prometheus 接入测试记录

日期：2026-08-18

## 白盒测试

| 用例 | 结果 | 证据 |
| --- | --- | --- |
| Prometheus selector 精确格式化 | 通过 | `PrometheusSelectorTest` |
| selector label 注入、空 selector 拒绝 | 通过 | `PrometheusSelectorTest` |
| 默认拒绝回环、file、用户信息地址 | 通过 | `PrometheusEndpointValidatorTest` |
| 显式 CIDR allowlist 地址校验 | 通过 | `PrometheusEndpointValidatorTest` |
| Prometheus vector/matrix 响应解析 | 通过 | `PrometheusResponseParserTest` |
| PromQL URL 编码、指标聚合回归 | 通过 | `PrometheusHttpClientTest`、`DbPrometheusMetricsServiceTest` |

完整后端验证：`mvn verify`，包含资源配置状态机测试后 101/101 通过，构建成功。

## 前端验证

- `scripts/build-web.ps1` 通过。
- `vue-tsc --noEmit` 通过。
- 详情页包含 Prometheus 配置、保存配置和测试连接入口；前端不直接请求 Prometheus。

## 已实现接口

- `GET /api/ledger/db-resources/{id}/monitoring-config`
- `PUT /api/ledger/db-resources/{id}/monitoring-config`
- `POST /api/ledger/db-resources/{id}/monitoring-config/test`
- 原有 `/api/ledger/db-resources/{id}/metrics` 和 `/metrics/download` 已切换到资源级配置。

## 尚待手工验收

- 使用实际客户 Prometheus 验证 node/database 两组 target。
- 验证目标不存在、Prometheus 超时、HTTP 错误和空结果的页面状态。
- 验证两个资源配置不同 target 时不会串用指标。
- 验证测试失败不会删除已有配置。
- 验证真实指标趋势和 CSV 下载。

## 部署注意

默认拒绝 Prometheus 地址，部署必须配置：

```text
MONITORING_PROMETHEUS_ALLOWED_HOSTS
MONITORING_PROMETHEUS_ALLOWED_CIDRS
```

旧安装令牌、安装命令、回调、自动 exporter 部署和自动 Prometheus target 注册仍未实现，符合当前计划范围。
