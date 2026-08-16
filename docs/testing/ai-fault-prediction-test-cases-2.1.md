# AI 测试用例 2.1

范围：故障知识库、故障预测任务配置、故障智能预警、任务执行记录与角色权限。

## 白盒用例

| 编号 | 类/方法 | 场景与预期 |
| --- | --- | --- |
| AI-WB-01~07 | `AiRequestValidationV21Test` | 拒绝非法目标、超过 22 项指标、空指标、非法时间/周期、非法引擎状态与预警状态。 |
| AI-WB-08~16 | `AiManagementServiceImplFaultTaskTest` | 模型必选、每周必须选星期、指标不可重复、服务端生成任务编码、立即执行生成 `PENDING`、创建者批量回填、日期校验、预警状态机及处理历史。 |
| AI-WB-17 | `FaultAlertServiceImplTest.handleRequiresProcessingRemark` | 处理预警必须填写备注，空备注不得委派下游服务。 |
| AI-WB-18~22 | `FaultKnowledgeServiceImplTest` | 知识名称与附件必填、详情不存在、历史空创建人列表/详情兼容。 |
| AI-WB-23~27 | `FaultPredictionTaskServiceImplTest` | 引擎回调记录归属、成功执行结果、完成必须有结束时间、非故障任务拒绝详情、编辑保留原任务编码。 |

## 黑盒用例

| 编号 | 接口/操作 | 预期 |
| --- | --- | --- |
| AI-BB-01 | `GET /ai/knowledge?knowledgeType=FAULT` | admin 查询故障知识库返回 200。 |
| AI-BB-02 | `GET /ai/fault-prediction/tasks` | admin 查询专用故障任务列表返回 200。 |
| AI-BB-03 | 专用任务列表传 `createdDate=2026-99-99` | 返回统一业务码 400。 |
| AI-BB-04 | `GET /ai/fault-prediction/alerts` | admin 查询预警专用列表返回 200。 |
| AI-BB-05 | 预警列表传非法 `processStatus` | 返回统一业务码 400。 |
| AI-BB-06 | viewer 创建故障任务 | HTTP 403。 |
| AI-BB-07 | viewer 处理预警 | HTTP 403。 |
| AI-BB-08 | operator 删除故障任务 | HTTP 403。 |
| AI-BB-09 | operator 删除预警 | HTTP 403。 |
| AI-BB-10 | 未登录访问专用任务/预警/知识库接口 | HTTP 401。 |

成功创建、编辑、删除任务，知识附件真实上传/下载/预览，以及预警确认/误报/关闭会写入业务数据，必须在隔离数据库中执行并清理，当前生产样演示库不执行成功写入。
