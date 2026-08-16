# AI 性能优化：优化知识库管理

## 页面范围

- 路由：`/performance/knowledge`。
- 前端入口：`apps/admin-web/src/views/ai/OptimizationKnowledgeView.vue`。
- 页面按原型提供知识 ID、知识名称、创建者、创建时间筛选，列表分页、编辑和删除，以及新增优化知识。
- 新增和编辑抽屉包含知识名称、知识内容和附件上传；保存按钮分别显示“提交保存”和“保存更新”。

## 附件链路

- 前端请求固定提交 `knowledgeType=OPTIMIZATION`，防止故障知识或缺陷知识混入性能优化模块。
- 复用 `/api/ai/knowledge` 的 multipart 上传、`kb_instance` 和 `kb_attachment` 数据链路。
- 新增时真实保存附件；编辑时通过 `retainedAttachmentIds` 保留未删除附件，并上传新增文件。
- 图片附件支持预览，其他允许格式支持下载；知识和附件沿用逻辑删除，物理文件交由后续清理任务处理。
- 单个附件限制 20MB，每条知识最多 10 个附件，前端限制与既有故障/缺陷知识库一致。

## 接口与权限

- `GET /api/ai/knowledge?knowledgeType=OPTIMIZATION`：分页查询。
- `GET /api/ai/knowledge/{id}`：详情查询。
- `POST /api/ai/knowledge`、`PUT /api/ai/knowledge/{id}`：multipart 新增和编辑。
- `DELETE /api/ai/knowledge/{id}`：逻辑删除。
- 附件下载和图片预览复用 `/api/ai/knowledge/attachments/{attachmentId}/download|preview`。
- 页面查看权限为 `performance:knowledge:view`；知识写操作复用既有 `ai:knowledge:create/update/delete` 权限，后端鉴权规则和数据库结构不变。

## 验证

- 2026-08-13：`scripts/build-web.ps1` 的 `vue-tsc --noEmit` 与 Vite 生产构建通过。
- 登录态下的 OPTIMIZATION 新增、编辑保留附件、删除附件、图片预览、文件下载和三角色权限仍需在可写测试数据环境补验。
