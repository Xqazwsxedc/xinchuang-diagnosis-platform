# AI 故障预测专项测试报告 2.1

## 执行结果

| 类型 | 用例数 | 通过 | 失败 | 证据 |
| --- | ---: | ---: | ---: | --- |
| 白盒定向回归 | 27 | 27 | 0 | Maven Surefire：`apps/api-server/target/surefire-reports/` |
| 黑盒专项回归 | 10 | 10 | 0 | `docs/testing/results/api-blackbox-result-2.1.json` 中 AI 相关记录 |

白盒命令：`mvn test -Dtest=AiManagementServiceImplFaultTaskTest,FaultAlertServiceImplTest,FaultKnowledgeServiceImplTest,FaultPredictionTaskServiceImplTest,AiRequestValidationV21Test`。

## 结论

故障预测的输入边界、模型与调度规则、任务编码兼容、`PENDING` 受理语义、引擎回调完成条件、知识库历史兼容、预警状态和删除权限均通过。本轮未模拟真实 AI 推理结果；这符合当前“保留引擎接口、不伪造预测结果”的架构约束。

## 遗留验收

- 隔离库执行任务 CRUD、立即执行、任务删除、预警状态流转和审计数据核对。
- multipart 附件上传、图片预览、下载、逻辑删除及物理文件保留。
- admin/operator/viewer 浏览器页面按钮、抽屉、Figma 视觉与数据回显。
