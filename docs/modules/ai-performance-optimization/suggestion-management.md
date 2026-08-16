# AI 性能优化：性能优化建议

## 页面范围

- 路由：`/performance/suggestion`，专用页面为 `apps/admin-web/src/views/ai/OptimizationSuggestionView.vue`。
- 页面按原型提供优化建议 ID、任务名称、创建者、处理状态筛选，表格多选、全选、CSV 导出和查看详情。
- 详情为居中弹窗，按优化对象、创建者、优化建议 ID、创建时间、待优化问题、待优化数据（SQL）、待优化内容顺序展示。
- 具备 `ai:optimization:handle` 的用户可在详情中确认正确或标记误报；关闭只关闭弹窗，不改变建议状态。

## 接口与数据

- 数据唯一来源为 `opt_report`，不新增演示报表表。
- `GET /api/ai/optimization-reports` 支持既有编号/问题关键字、处理状态，以及新增任务名称、创建者筛选；创建者名称按批量查询回填。
- `GET /api/ai/optimization-reports/{id}` 返回真实详情。
- `GET /api/ai/optimization-reports/export?reportIds={id}` 支持最多 100 条真实 CSV 导出，校验空清单和无效 ID，并写操作审计。
- 处理接口保持 `PATCH /api/ai/optimization-reports/{id}/handle`，不伪造优化 SQL 执行或结果。

## 验证

- 2026-08-13：后端 `mvn test` 82/82 通过，前端 `scripts/build-web.ps1` 通过。
- 登录态下建议筛选、CSV 下载、确认/误报处理及三角色权限仍需在可写测试数据环境补验。
