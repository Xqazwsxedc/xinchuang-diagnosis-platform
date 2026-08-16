# AI 缺陷检测：缺陷检测报表

## 页面范围

- 路由：`/defect-detection/report`。
- 前端入口：`apps/admin-web/src/views/ai/DefectReportView.vue`。
- 页面按原型提供缺陷 ID、任务名称、创建者、检测结果确认筛选，列表多选、全选、批量导出和查看详情。
- 不在此页面新增或编辑缺陷报表。真实 AI 引擎后续负责生成 `defect_report`；当前页面仅展示、查询、导出既有数据。

## 列表和详情

- 列表字段：缺陷 ID、缺陷检查任务名称、创建者、创建时间、缺陷问题、检测结果确认。
- 状态文案：`PENDING=未核对`、`CONFIRMED=确认`、`FALSE_POSITIVE=误报`、`CLOSED=已关闭`。
- “查看详情”使用居中报告弹窗，展示任务名称、创建者、创建时间、检测对象、缺陷问题、缺陷数据、缺陷描述。
- 详情底部支持关闭和导出当前报表；列表最多一次导出 100 条已勾选报表。

## 接口与权限

- `GET /api/ai/defect-reports`：兼容原分页接口，新增可选查询参数 `defectCode`、`taskName`、`creatorName`；保留既有 `keyword`、`processStatus`、`level`。
- `GET /api/ai/defect-reports/{id}`：真实详情。
- `GET /api/ai/defect-reports/export?reportIds={id}&reportIds={id}`：受鉴权 CSV 导出，文件名为“缺陷检测报表.csv”。
- 查询、详情、导出均复用 `ai:defect:view`；后端校验空清单、单次上限及不存在/已删除记录，并记录操作审计。
- 所有数据来自 `defect_report`，创建者由 `created_by` 批量回填，历史记录创建者为空时页面显示 `-`。

## 验证

- 2026-08-13：前端 `vue-tsc --noEmit` 和 Vite 生产构建通过。
- 2026-08-13：修复前端缺陷 ID 筛选参数由 `defectId` 错传为 `defectCode` 的契约问题；补充空导出清单拒绝、创建者批量回填及 CSV 核心字段白盒用例。后端 `mvn test` 79/79 通过，前端 `vue-tsc --noEmit` 和 Vite 生产构建通过。
- 登录态下的筛选、多选导出、CSV 内容、详情弹窗及 viewer 权限回归仍需在可写测试环境补验。
