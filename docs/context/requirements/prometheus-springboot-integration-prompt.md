# Spring Boot 接入 Prometheus 指标：开发提示词

请先阅读现有项目代码和既有接口，再在不破坏现有页面、权限、数据库逻辑的前提下，实现 Spring Boot 后端接入 Prometheus HTTP API 的能力。

## 一、项目目标

这是一个 Spring Boot + Vue 的管理平台。当前要完成的是“数据库资源详情”页面中的监控指标部分：让页面展示真实的服务器与 MySQL 指标，而不是只从项目自身 MySQL 的模拟指标表读取数据。

目标链路：

```text
Vue 页面
  -> Spring Boot 后端接口
  -> Prometheus HTTP API
  -> node_exporter / mysqld_exporter
```

前端不得直接请求 Prometheus。必须由 Spring Boot 统一请求 Prometheus、转换结果并返回项目自己的 DTO。

## 二、已完成的云端环境

云服务器上已经通过 Docker 部署并验证正常：

```text
MySQL 测试实例       容器名：monitor-mysql
node_exporter        容器名：node-exporter，内部端口：9100
mysqld_exporter      容器名：mysqld-exporter，内部端口：9104
Prometheus           容器名：prometheus，Web/API 端口：9090
```

Prometheus 已成功采集以下三个目标，状态均为 `up`：

```text
prometheus
node-exporter
mysqld-exporter
```

Prometheus Web/API 的基础地址应当通过配置文件注入，不能硬编码在 Java 代码中：

```yaml
monitoring:
  prometheus:
    base-url: http://<PROMETHEUS_HOST>:9090
```

开发机上的 Spring Boot 需要访问云服务器的 Prometheus 地址。不要使用 `localhost:9090`，除非运行环境中 Prometheus 也在本机。

## 三、现有项目约束

项目已有与数据库资源指标、日志相关的实体和接口，至少包括：

```text
DbInstanceMetric
DbInstanceLog
/api/ledger/db-resources/{id}/metrics
/api/ledger/db-resources/{id}/logs
```

请先定位这些 Controller、Service、DTO、Mapper 和 Vue 页面，复用当前项目的响应包装、异常处理、鉴权、命名和代码风格。

不要进行无关重构，不要修改现有登录、权限、资源管理或日志功能。

## 四、本阶段范围

### 要实现

1. 新增 Prometheus HTTP API 客户端或 Service。
2. 支持即时查询：`/api/v1/query`。
3. 支持区间查询：`/api/v1/query_range`。
4. 将 Prometheus 的 `vector` / `matrix` 返回结果转换成项目页面可直接使用的 DTO。
5. 先接入以下指标：
   - CPU 使用率
   - 内存使用率
   - 磁盘 IOPS
   - MySQL 当前连接数
   - MySQL 缓存命中率
6. 支持页面已有的时间范围：如 1 小时、3 小时、6 小时、12 小时、1 天、3 天、7 天、14 天。根据范围合理计算 `start`、`end` 和 `step`。
7. 保留或适配现有的 CSV 下载接口，使其下载真实指标数据。
8. 添加有价值的单元测试，重点覆盖：Prometheus 响应解析、时间范围转换、空结果、非法数值、网络异常。

### 本阶段不要实现

- 不部署 Grafana。
- 不部署 Loki、Promtail。
- 不让 Vue 直接调用 Prometheus。
- 不把 Prometheus 全量时序数据回写到项目 MySQL。
- 不重做整套页面视觉设计。
- 不修改数据库资源日志页的来源；业务日志仍可继续使用项目现有 `db_instance_log` 数据。

## 五、指标与 PromQL

PromQL 应集中管理，不要散落在 Controller 或前端代码中。

### CPU 使用率（百分比）

```promql
100 * (1 - avg by(instance)(rate(node_cpu_seconds_total{mode="idle"}[5m])))
```

### 内存使用率（百分比）

```promql
100 * (1 - node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)
```

### 磁盘 IOPS

```promql
sum by(instance)(
  rate(node_disk_reads_completed_total[1m])
  +
  rate(node_disk_writes_completed_total[1m])
)
```

实现时注意排除 loop、ram 等无意义设备；具体标签过滤方式请以 Prometheus 实际返回的 `device` 标签为准。

### MySQL 当前连接数

```promql
mysql_global_status_threads_connected
```

### MySQL InnoDB 缓存命中率（百分比）

即时展示可使用：

```promql
100 * (
  1
  -
  mysql_global_status_innodb_buffer_pool_reads
  /
  mysql_global_status_innodb_buffer_pool_read_requests
)
```

区间统计可使用 `rate(...[5m])`，但当时间窗口内没有数据库访问时，分子和分母可能都是 0，结果会出现 `NaN`。后端必须安全处理 `NaN`、`Inf`、空结果，不能导致接口 500 或前端图表崩溃。

## 六、资源 ID 与 Prometheus 目标映射

当前学习环境只有一台云服务器，Prometheus 中的目标使用 Docker 内部地址作为 `instance` 标签。实现时不要把这些内部 IP 写死在业务代码。

请设计可配置的映射方式，优先级如下：

1. 若现有数据库资源表已包含监控目标、IP、实例地址等字段，复用该字段。
2. 若没有合适字段，第一阶段可使用配置项，例如：

```yaml
monitoring:
  targets:
    node-instance: <NODE_EXPORTER_INSTANCE>
    mysql-instance: <MYSQLD_EXPORTER_INSTANCE>
```

3. 代码结构必须便于未来改成“每个资源 ID 对应不同 Prometheus target”。

不要把 Docker 内部 IP 固化到 Controller、DTO 或 Vue 中。

## 七、Prometheus API 与 DTO 要求

Prometheus 常用接口：

```text
GET /api/v1/query
GET /api/v1/query_range
```

`query_range` 典型返回中，点位格式为：

```json
[1723800000, "45.2"]
```

请将其转换为前端友好的结构，例如：

```json
{
  "metric": "cpuUsage",
  "unit": "%",
  "currentValue": 45.2,
  "points": [
    { "timestamp": 1723800000, "value": 45.2 }
  ]
}
```

具体字段名称应优先匹配当前前端已使用的接口契约。若需要调整，请同时修改前后端并保持兼容。

后端需要负责：

- Unix 时间戳转换或原样返回的统一约定。
- 字符串数值解析为数值类型。
- 单位统一和显示精度。
- `NaN`、`Inf`、空数组处理。
- Prometheus 网络超时、非 2xx、`status != success` 的可读错误处理。

推荐使用 Spring Boot 3 自带的 `RestClient`；如果项目已有统一 HTTP 客户端封装，则优先复用。

## 八、推荐实现顺序

1. 阅读现有指标接口和 Vue 页面，确认当前 JSON 结构。
2. 增加 `monitoring.prometheus.base-url` 配置与可配置超时。
3. 实现 Prometheus API Client。
4. 只完成 CPU 时间序列接口，并用本地运行的 Spring Boot 调通。
5. 增加内存、IOPS、MySQL 连接数、缓存命中率。
6. 将 DTO 接入现有 Vue 折线图、环形图和顶部统计卡片。
7. 验证时间范围切换、空数据、Prometheus 不可用、CSV 下载。
8. 添加测试并运行项目现有构建/测试命令。

## 九、验收标准

完成后应满足：

1. 数据库资源详情页的 CPU、内存、IOPS、缓存命中率图表来自 Prometheus 的真实查询结果。
2. 顶部统计卡片能展示相应实时值。
3. 时间范围切换会向后端传递范围参数，并返回相应的时序点。
4. MySQL 或 Prometheus 无数据时，页面显示空状态或 `--`，而不是 `NaN`、`Infinity` 或报错。
5. Prometheus 暂时不可访问时，接口有可识别的错误信息，页面不会整体崩溃。
6. 现有日志列表功能不受影响。
7. 没有在代码、Git 提交或前端中写入服务器密码、数据库密码、私钥或真实公网 IP。

## 十、交付内容

请在完成后提供：

1. 修改文件列表及每个文件的用途。
2. 新增/调整的 API 列表与示例返回。
3. 每个页面指标对应的 PromQL。
4. 如何在本地配置 Prometheus 地址。
5. 已执行的测试与结果。
6. 尚未解决的限制或后续建议。
