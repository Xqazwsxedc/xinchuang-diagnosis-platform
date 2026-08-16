# AI 故障预测测试代码路径 2.1

| 分类 | 文件路径 | 说明 |
| --- | --- | --- |
| 请求校验白盒 | `apps/api-server/src/test/java/com/xinchuang/diagnosis/ai/dto/AiRequestValidationV21Test.java` | DTO、嵌套规则、引擎与预警状态校验 |
| 任务服务白盒 | `apps/api-server/src/test/java/com/xinchuang/diagnosis/ai/service/impl/AiManagementServiceImplFaultTaskTest.java` | 通用任务服务规则与状态机 |
| 专用任务白盒 | `apps/api-server/src/test/java/com/xinchuang/diagnosis/ai/service/impl/FaultPredictionTaskServiceImplTest.java` | 专用接口边界、编码、引擎回调 |
| 预警白盒 | `apps/api-server/src/test/java/com/xinchuang/diagnosis/ai/service/impl/FaultAlertServiceImplTest.java` | 预警处理备注校验 |
| 知识库白盒 | `apps/api-server/src/test/java/com/xinchuang/diagnosis/ai/service/impl/FaultKnowledgeServiceImplTest.java` | 知识、附件与历史数据兼容 |
| 黑盒脚本 | `docs/testing/api-blackbox-test-2.1.ps1` | 专项接口、非法参数与角色权限 |
| 黑盒结果 | `docs/testing/results/api-blackbox-result-2.1.json` | 实际 HTTP 结果 |
| 页面清单 | `docs/testing/frontend-integration-checklist.md` | WEB21-05 至 WEB21-08 |
| 模块说明 | `docs/modules/ai-fault-prediction/knowledge-base.md`、`task-config.md` | 功能契约与验收边界 |
