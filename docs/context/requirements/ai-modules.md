# AI Module Requirements

## Source Summary

Word 需求包含三组 AI 能力：

- AI 故障预测：故障知识库、故障预测任务、故障智能预警。
- AI 缺陷检测：缺陷知识库、缺陷检测任务、缺陷检测报表。
- AI 性能优化：优化知识库、性能优化任务、性能优化建议。

需求共同点：

- 列表、搜索、分页。
- 新增、编辑、查看、删除。
- 任务配置支持 AI 模型选择、对象选择、指标选择、定时设置。
- 结果类页面支持查看详情和处理状态。

## Current Status

部分实现。

已实现一期数据壳：

- AI 模型。
- AI 任务。
- 任务执行记录。
- 知识库。
- 故障预警结果。
- 缺陷检测报表。
- 性能优化建议。
- 结果状态流转。
- 菜单、按钮权限和页面 CRUD。

暂不实现：

- 真实 AI 推理。
- 自动检测和自动修复。
- 真实指标、日志、SQL 执行计划采集。
- 模型服务调用。

## Current Tables

- `ai_model`
- `ai_task`
- `task_exec_record`
- `kb_instance`
- `kb_attachment`（故障知识库真实本地附件元数据）
- `fault_report`
- `defect_report`
- `opt_report`
- `result_status_log`
- `result_knowledge_ref`

## Future Enterprise Enhancements

- 接入真实模型服务。
- 增加任务调度器。
- 增加指标采集和趋势快照。
- 增加知识检索和结果沉淀规则。
- 增加自动修复审批、执行和回滚流程。

## 故障知识库管理（已完成）

- Figma 故障知识库页面已落地为专用路由 `/fault-prediction/knowledge`：按知识 ID、名称、创建者、创建日期筛选，展示创建者、创建时间和最新修改时间，支持分页、新增、编辑、逻辑删除。
- 知识主数据继续使用 `kb_instance`，故障知识固定为 `knowledge_type=FAULT`；后端自动生成知识编码，未逻辑删除的知识名称必须唯一。原 JSON CRUD 仍保留兼容，新页面使用 multipart 表单。
- 附件元数据使用 `kb_attachment`，每条最多 10 个、单个最大 20MB；允许图片、PDF、Office、TXT/MD、ZIP/RAR/7Z。服务端采用受控本地目录、随机存储名、路径归一化和图片内容校验。
- 图片支持鉴权预览；其余格式以及图片均可鉴权下载。知识删除、编辑移除附件均为逻辑删除，物理文件留待后续清理和恢复任务。
- 权限：`admin` 全权；`operator` 可查看、新增、编辑、下载，不可删除；`viewer` 仅查看、下载。服务端 `JwtAuthInterceptor` 为最终权限边界。
