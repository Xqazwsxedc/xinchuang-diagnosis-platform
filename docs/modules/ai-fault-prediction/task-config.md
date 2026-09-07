# 故障预测任务配置

## 范围

路由：`/fault-prediction/task`。

本模块完成故障预测任务的配置、查询、编辑、逻辑删除、详情和人工受理，不实现真实模型推理、定时调度器或自动生成预警。

## 页面与交互

- 列表支持按任务名称、创建者、创建日期和 AI 模型筛选；展示任务名称、创建者、创建时间、预测对象、模型、上次预测时间、执行次数与故障数量。
- 新增和编辑使用抽屉：业务系统/分布式数据库二选一，切换类型后清空对象；仅展示已启用的 `FAULT_PREDICTION` 模型。
- 固定 22 项预测指标，支持全选、清空和多选。
- 可增加多条每日或每周定时规则；每周必须选择星期；预测范围仅允许 `1`、`2`、`4`、`12`、`24` 小时。
- 历史演示规则可能缺少 `predictionWindowHours`；编辑页面必须归一化为默认 `4` 小时，不能向保存请求发送 `undefined`。新规则必须完整保存预测范围。
- 详情读取 `GET /ai/tasks/{id}` 最新数据，可跳转 `/fault-prediction/alert`。
- 立即执行仅创建一条待处理执行记录并提示“已受理，等待 AI 引擎处理”，不伪造成功预警。

## 数据与接口契约

- 主表：`ai_task`，任务类型固定为 `FAULT_PREDICTION`；`metric_codes` 保存逗号分隔指标，`schedule_rule` 保存 JSON 数组，`param_json` 写入 `engineStatus=WAITING_ENGINE` 和 `contractVersion=1.0`。
- 执行表：`task_exec_record`。人工执行写入 `execution_status=PENDING`、`trigger_type=MANUAL`、`start_time`，`end_time` 保持为空。
- 沿用接口：`GET/POST /ai/tasks`、`GET/PUT/DELETE /ai/tasks/{id}`、`POST /ai/tasks/{id}/execute`；列表新增可选参数 `modelId`、`creatorName`、`createdDate`（`yyyy-MM-dd`），不破坏旧调用。
- 专用接口：`GET /ai/fault-prediction/tasks`、`GET /ai/fault-prediction/tasks/{id}`、`POST/PUT/DELETE /ai/fault-prediction/tasks/{id}`、`POST /ai/fault-prediction/tasks/{id}/execute`。专用接口使用独立 DTO/VO，旧通用接口继续保留兼容。
- 专用新增接口由服务端生成任务编码；专用编辑接口不接收任务编码，服务层必须保留数据库中已有的 `taskCode`，不得要求前端传递内部编码字段。
- 后端保存时再次校验：模型存在、启用、模型类型匹配、故障任务已选择模型、指标与定时规则合法、目标对象存在、任务编码唯一。
- 任务列表批量回填创建者名称，不允许逐条查询用户；历史任务创建人为空时返回空名称，由前端显示 `-`。

## 权限

| 角色 | 查询/详情 | 新增/编辑/立即执行 | 删除 |
| --- | --- | --- | --- |
| admin | 是 | 是 | 是 |
| operator | 是 | 是 | 否 |
| viewer | 是 | 否 | 否 |

权限编码：`ai:task:view`、`ai:task:create`、`ai:task:update`、`ai:task:execute`、`ai:task:delete`。三种角色当前均可查看全部任务，未在本模块追加行级数据隔离。

## BentoML 第一版执行闭环

- 第一版仅允许分布式数据库对象。业务系统选项保留但禁用，后端保存和执行时再次校验。
- 22 项指标继续展示；已接入 `CPU`、`内存`、`磁盘IO`、`数据库连接数`、`缓存命中率`，其他指标禁用并标注“暂未开发”。
- 人工执行仍先返回 `PENDING`。事务提交后，由 `faultPredictionExecutor` 异步原子领取为 `RUNNING`，不会阻塞浏览器请求。
- `input_start_time/input_end_time` 固化过去 1 小时历史输入窗口，同时作为第一版执行标记，恢复扫描不会处理历史演示 `PENDING` 数据。
- Spring Boot 读取资源级 Prometheus 配置和 selector，查询真实区间指标、按时间对齐并移除任何所选指标缺失的点；少于 3 个有效点时执行失败。
- BentoML 使用版本化 JSON 契约返回异常点；平台每个指标只保留分数最高异常，幂等写入 `fault_report`，再完成执行记录。
- 当前 `predictionWindowHours` 仅作为未来预测模型的预留配置；Z-Score 第一版执行固定读取过去 1 小时，不声称预测未来故障。
- 定时规则仍只保存配置，自动调度触发尚未实现。

## 验证

2026-08-27：BentoML 第一版业务闭环核心测试 15/15 通过，后端全量 `mvn verify` 111/111 通过；后端 238 个源文件编译通过，前端类型检查和生产构建通过，Python 模型单测通过。真实 Spring Boot -> BentoML 通信此前已使用 5 点 CPU 序列验证成功；本轮真实 Prometheus 任务执行需使用已完成资源级监控配置的数据库在登录页面执行验收。

2026-08-05：后端全量 `mvn verify` 49/49 通过，JaCoCo 行覆盖率 17.37%（826/4756）；前端生产构建通过。新增专用接口契约后回归通过，移除服务层未使用的 Mapper 依赖并补齐专用列表根路径权限规则。运行时健康检查返回 200，未登录访问专用任务列表返回 401。登录态 CRUD 黑盒和浏览器视觉对照仍需在具备测试密码及可用浏览器会话的环境补测。

2026-08-06：修复早期演示任务定时规则缺少预测范围导致编辑保存校验失败的问题。前端对旧规则默认补齐 `4` 小时，后端 DTO 对缺省值提供同样兜底，演示 SQL 补齐字段；定时预测设置抽屉按原型改为纵向全宽字段布局，创建者移至末尾只读展示。全局参数校验错误现在返回首个具体校验消息。后端 `mvn test` 52/52 通过，前端生产构建通过。

2026-08-11：修复专用编辑接口未继承原任务编码导致“任务编码不能为空”。编辑请求保持不要求 `taskCode`，服务层在调用通用更新服务前从原任务复制编码；新增回归用例后，后端 `mvn test` 53/53 通过。
