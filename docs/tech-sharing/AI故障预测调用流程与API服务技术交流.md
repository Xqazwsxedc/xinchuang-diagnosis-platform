# AI 故障预测调用流程与 API 服务技术交流

> 适用场景：项目技术交流、阶段答辩和模块交接。  
> 文档版本：1.1；更新日期：2026-08-27。

## 1. 交流结论

AI 故障预测模块已完成 BentoML 第一版业务闭环：任务配置、真实 Prometheus 数据查询、异步执行、模型 HTTP 调用、执行状态、预警写入、展示和人工处置均已落地。“立即执行”先返回 `PENDING`，事务提交后后台原子领取为 `RUNNING`，最终进入 `SUCCESS` 或 `FAILED`。

**当前模型是用于通信和业务闭环验证的 Z-Score 时序异常检测模型，不是训练后的未来故障预测模型。** 第一版仅支持分布式数据库和 5 项真实指标；消息队列、定时规则自动触发、业务系统对象、其余 17 项指标和生产级模型仍属后续范围。

配套可编辑 FigJam 图：

- [AI 故障预测调用流程](https://www.figma.com/board/DZOFKU3TQjQIFCslDABPG1?utm_source=other&utm_content=edit_in_figjam)
- [AI API 分层与接口调用流程](https://www.figma.com/board/ddiJUgBWJLw52WnarLBUSZ?utm_source=other&utm_content=edit_in_figjam)

## 2. AI 故障预测调用流程与功能模块

### 2.1 模块划分

| 模块 | 页面/接口职责 | 核心数据 |
| --- | --- | --- |
| 故障知识库管理 | 管理故障案例、附件、知识内容，支撑未来模型知识增强 | `kb_instance`、`kb_attachment` |
| 故障预测任务配置 | 维护预测对象、AI 模型、22 项指标和定时规则 | `ai_model`、`ai_task` |
| 任务执行记录 | 记录人工或后续定时触发的受理、执行结果与失败信息 | `task_exec_record` |
| 故障智能预警 | 展示模型未来产生的预警，并完成确认、误报标记和关闭 | `fault_report`、`result_status_log` |
| 权限与审计 | 控制角色访问范围，记录新增、修改、删除和执行操作 | 权限菜单、审计日志 |

其中，`ai_task.task_type` 固定为 `FAULT_PREDICTION`，实现与缺陷检测、性能优化任务的统一存储和分类隔离。

### 2.2 端到端调用流程

```mermaid
flowchart LR
    A[用户配置预测任务] --> B[Vue 页面]
    B -->|Axios + JWT| C[Spring Boot Controller]
    C --> D[参数校验和权限校验]
    D --> E[(ai_task)]
    E --> F[立即执行或后续定时触发]
    F --> G[(task_exec_record: PENDING)]
    G --> H[事务后异步执行器]
    H --> L[Prometheus 过去1小时数据]
    L --> M[BentoML Z-Score 模型]
    M --> I[(fault_report)]
    I --> J[预警列表与人工处置]
    J --> K[(result_status_log)]
```

调用步骤说明：

1. 管理员或操作员在前端选择业务系统或分布式数据库、启用的故障预测模型、指标集合和每日/每周规则。
2. 前端经 Axios 携带 JWT 访问 Spring Boot API；拦截器先验证登录状态与接口权限。
3. Controller 使用 `@Valid` 校验请求格式，Service 再校验模型启用状态、目标对象存在性、指标重复、调度规则和任务编码唯一性。
4. 保存成功后，任务配置写入 `ai_task`：指标写入 `metric_codes`，定时规则以 JSON 写入 `schedule_rule`，`param_json` 留存引擎契约版本。
5. 用户点击“立即执行”后，平台写入一条 `task_exec_record`，状态为 `PENDING`、结果数为 `0`，并更新任务执行次数及最后执行时间。
6. 事务提交后后台执行器原子领取 `PENDING`，查询过去 1 小时真实 Prometheus 数据并调用 BentoML；成功写 `fault_report` 和 `SUCCESS`，失败写 `FAILED` 与脱敏原因。
7. 人工在故障智能预警中处理结果，状态按 `PENDING -> CONFIRMED/FALSE_POSITIVE -> CLOSED` 流转，处置备注写入 `result_status_log`，形成可审计闭环。

### 2.3 核心业务规则

- 预测对象类型只允许 `BUSINESS_SYSTEM` 与 `DB_RESOURCE`，并由后端校验目标存在。
- 每个任务至少选择一项、最多选择 22 项预测指标；后端拒绝空白和重复指标。
- 定时规则仅允许 `daily` 或 `weekly`；时间格式为 `HH:mm`；每周规则必须指定星期；预测窗口为 `1/2/4/12/24` 小时。
- 专用新增接口由服务端生成任务编码；编辑时服务端保留旧编码，前端无需传递内部编码。
- 执行状态的 `PENDING` 表示“平台已受理，等待 AI 引擎处理”，不代表模型已运行成功。
- 第一版只支持 `DB_RESOURCE` 与 `CPU/内存/磁盘IO/数据库连接数/缓存命中率`；其余配置由前后端共同拒绝。
- `predictionWindowHours` 暂为未来模型预留，当前历史异常检测固定读取过去 1 小时。
- 预警处置必须遵守状态机，非法跳转和缺失处理备注会被拒绝。

### 2.4 角色权限边界

| 角色 | 查询与详情 | 新增/编辑 | 立即执行 | 删除 |
| --- | --- | --- | --- | --- |
| `admin` | 允许 | 允许 | 允许 | 允许 |
| `operator` | 允许 | 允许 | 允许 | 不允许 |
| `viewer` | 允许 | 不允许 | 不允许 | 不允许 |

接口权限由 `JwtAuthInterceptor` 集中匹配，例如任务查询需要 `ai:task:view`，立即执行需要 `ai:task:execute`，预警处置需要 `ai:fault:handle`。

## 3. API 服务、接口设计及核心代码示例

### 3.1 分层设计

```mermaid
flowchart LR
    A[Vue 视图] --> B[Axios 请求封装]
    B --> C[JWT 鉴权]
    C --> D[Controller]
    D --> E[Service]
    E --> F[Mapper]
    F --> G[(MySQL)]
```

- **前端层**：页面负责表单交互、字段回显和错误提示；不承担权限判断和核心数据校验。
- **Controller 层**：定义 REST 路由、接收 DTO、触发 Bean Validation，并返回统一 `ApiResponse`。
- **Service 层**：承载事务、领域校验、状态机、审计与跨表编排。
- **Mapper 层**：通过 MyBatis-Plus 执行数据访问，不暴露业务规则。
- **数据层**：任务、执行记录、预警结果和处理日志使用关联主键形成链路。

统一响应结构如下，`traceId` 可用于定位日志和异常：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "traceId": "0a4dc64f-9d4f-4ee6-8e7c-cf147a9f451f",
  "timestamp": "2026-08-21T08:00:00Z"
}
```

### 3.2 关键接口

基础路径为 `/api`，下表省略该前缀。

| 方法 | 地址 | 权限 | 用途 |
| --- | --- | --- | --- |
| `GET` | `/ai/fault-prediction/tasks` | `ai:task:view` | 分页查询故障预测任务 |
| `GET` | `/ai/fault-prediction/tasks/{id}` | `ai:task:view` | 查询任务详情、执行记录摘要和关联预警 |
| `POST` | `/ai/fault-prediction/tasks` | `ai:task:create` | 新增任务，由服务端生成任务编码 |
| `PUT` | `/ai/fault-prediction/tasks/{id}` | `ai:task:update` | 更新任务配置，保持原任务编码 |
| `DELETE` | `/ai/fault-prediction/tasks/{id}` | `ai:task:delete` | 删除任务 |
| `POST` | `/ai/fault-prediction/tasks/{id}/execute` | `ai:task:execute` | 创建待引擎处理的执行记录 |
| `GET` | `/ai/fault-prediction/alerts` | `ai:fault:view` | 分页查询故障预警 |
| `GET` | `/ai/fault-prediction/alerts/{id}` | `ai:fault:view` | 查询预警及处理历史 |
| `PATCH` | `/ai/fault-prediction/alerts/{id}/handle` | `ai:fault:handle` | 人工确认、标记误报或关闭预警 |

### 3.3 新建任务请求示例

```http
POST /api/ai/fault-prediction/tasks
Authorization: Bearer <JWT>
Content-Type: application/json
```

```json
{
  "taskName": "核心库连接数预测",
  "modelId": 1001,
  "targetType": "DB_RESOURCE",
  "targetId": 2001,
  "metricCodes": ["mysql_threads_connected", "mysql_slow_queries"],
  "schedules": [
    {
      "time": "09:00",
      "repeatType": "daily",
      "weekDays": [],
      "predictionWindowHours": 4
    }
  ]
}
```

`taskCode` 不属于专用接口的前端入参。创建时由服务端生成；编辑时从原记录继承，避免客户端篡改或遗漏内部编码。

### 3.4 立即执行请求与响应语义

```http
POST /api/ai/fault-prediction/tasks/3001/execute
Authorization: Bearer <JWT>
Content-Type: application/json

{"triggerType":"MANUAL"}
```

成功后先返回执行记录。其关键字段为 `executionStatus=PENDING`、`startTime`、`resultCount=0`。后台随后异步推进状态，前端应展示“已受理，后台正在检测”，不能把受理响应当成检测完成。

### 3.5 核心代码示例

以下代码来自项目现有实现，删减了与主题无关的 import 和 getter/setter。

**1. 专用 Controller：DTO 校验与统一响应**  
来源：[FaultPredictionTaskController.java](../../apps/api-server/src/main/java/com/xinchuang/diagnosis/ai/controller/FaultPredictionTaskController.java)

```java
@RestController
@RequestMapping("/ai/fault-prediction/tasks")
public class FaultPredictionTaskController {
    private final FaultPredictionTaskService faultTaskService;

    @PostMapping
    public ApiResponse<FaultTaskDetailVO> create(
            @Valid @RequestBody FaultTaskSaveRequest request) {
        return ApiResponse.ok(faultTaskService.create(request));
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<TaskExecRecord> execute(
            @PathVariable Long id,
            @Valid @RequestBody TaskExecuteRequest request) {
        return ApiResponse.ok(faultTaskService.execute(id, request));
    }
}
```

**2. 执行受理：只创建待处理记录，不伪造模型结果**  
来源：[AiManagementServiceImpl.java](../../apps/api-server/src/main/java/com/xinchuang/diagnosis/ai/service/impl/AiManagementServiceImpl.java)

```java
public TaskExecRecord executeTask(Long id, TaskExecuteRequest request) {
    AiTask task = taskDetail(id);
    TaskExecRecord record = new TaskExecRecord();
    record.setTaskId(id);
    record.setTaskType(task.getTaskType());
    record.setTaskNameSnapshot(task.getTaskName());

    boolean waitingForEngine = isEngineTask(task.getTaskType());
    record.setExecutionStatus(waitingForEngine ? "PENDING" : "SUCCESS");
    record.setTriggerType(StringUtils.hasText(request.getTriggerType())
        ? request.getTriggerType() : "MANUAL");
    record.setStartTime(LocalDateTime.now());
    record.setResultCount(0);
    taskExecRecordMapper.insert(record);

    task.setExecutionCount((task.getExecutionCount() == null ? 0 : task.getExecutionCount()) + 1);
    task.setLastExecTime(record.getStartTime());
    aiTaskMapper.updateById(task);
    return record;
}
```

**3. 未来引擎适配边界：更新执行状态**  
来源：[FaultPredictionTaskServiceImpl.java](../../apps/api-server/src/main/java/com/xinchuang/diagnosis/ai/service/impl/FaultPredictionTaskServiceImpl.java)

```java
@Transactional(rollbackFor = Exception.class)
public TaskExecRecord updateExecutionFromEngine(
        Long taskId, Long executionId, FaultTaskExecutionUpdateRequest request) {
    faultTask(taskId);
    TaskExecRecord record = taskExecRecordMapper.selectById(executionId);
    if (record == null || !taskId.equals(record.getTaskId())
            || !"FAULT_PREDICTION".equals(record.getTaskType())) {
        throw new BusinessException("故障预测执行记录不存在");
    }
    if (("SUCCESS".equals(request.getExecutionStatus())
            || "FAILED".equals(request.getExecutionStatus()))
            && request.getEndTime() == null) {
        throw new BusinessException("已完成执行必须提供结束时间");
    }
    record.setExecutionStatus(request.getExecutionStatus());
    record.setStartTime(request.getStartTime());
    record.setEndTime(request.getEndTime());
    record.setResultCount(request.getResultCount());
    record.setErrorMessage(request.getErrorMessage());
    taskExecRecordMapper.updateById(record);
    return record;
}
```

该方法是**内部服务契约**，当前没有对外 HTTP Controller。未来接入时，应在认证后的引擎适配层调用它，或在该边界外增加受签名保护的回调接口，不能让前端直接写入执行状态。

## 4. 当前引擎实现与后续升级

当前已通过独立的“AI 引擎适配层”对接 BentoML，保持现有前端 API 和数据库结构稳定：

1. 事务提交事件立即派发，30 秒扫描器恢复遗留 `PENDING`，条件更新保证单个 Worker 领取。
2. 适配层从资源级 Prometheus 获取特征并调用 BentoML，平台不把数据库或 Prometheus 凭据交给模型服务。
3. 每个指标保留最强异常并幂等写入 `fault_report`；网络类失败最多重试 2 次。
4. 后续替换真实模型时保持请求/响应契约稳定，并补模型注册、灰度和效果评估。
5. 定时规则后续由独立调度器触发；高并发或跨节点部署时将进程内线程池升级为消息队列。

## 5. 代码与相关资料索引

| 类型 | 路径 |
| --- | --- |
| 任务接口 | `apps/api-server/src/main/java/com/xinchuang/diagnosis/ai/controller/FaultPredictionTaskController.java` |
| 预警接口 | `apps/api-server/src/main/java/com/xinchuang/diagnosis/ai/controller/FaultAlertController.java` |
| 任务业务服务 | `apps/api-server/src/main/java/com/xinchuang/diagnosis/ai/service/impl/FaultPredictionTaskServiceImpl.java` |
| 通用任务执行实现 | `apps/api-server/src/main/java/com/xinchuang/diagnosis/ai/service/impl/AiManagementServiceImpl.java` |
| 任务请求 DTO | `apps/api-server/src/main/java/com/xinchuang/diagnosis/ai/dto/FaultTaskSaveRequest.java` |
| 引擎状态更新 DTO | `apps/api-server/src/main/java/com/xinchuang/diagnosis/ai/dto/FaultTaskExecutionUpdateRequest.java` |
| 模块说明 | `docs/modules/ai-fault-prediction/task-config.md` |
| 数据库设计摘要 | `docs/context/requirements/database-design.md` |
| 流程图说明 | `docs/diagrams/ai-api-flow-diagrams.md` |

## 6. 交流时的推荐表述

“项目已经完成 BentoML 第一版时序异常检测闭环：平台可靠受理任务，从 Prometheus 获取真实指标，异步调用独立 Python 模型服务并生成可处置预警。当前 Z-Score 用于验证工程链路和异常检测，不把它误称为生产级未来故障预测模型；后续可在稳定契约内替换训练模型。”
