# AI 缺陷检测：缺陷知识库管理

## 页面与范围

- 页面路由：`/defect-detection/knowledge`。
- 前端入口：`apps/admin-web/src/views/ai/DefectKnowledgeView.vue`。
- 页面包含知识 ID、名称、创建者、创建时间筛选，缺陷知识列表、分页、新增抽屉、编辑抽屉和删除确认。
- 新增/编辑抽屉包含知识名称、知识内容和附件上传；真实缺陷检测算法不在本模块实现。

## 附件能力

缺陷知识库复用故障知识库已验证的附件链路，但业务类型固定为 `DEFECT`：

- `POST /api/ai/knowledge`：multipart 新增。
- `PUT /api/ai/knowledge/{id}`：multipart 更新，使用 `retainedAttachmentIds` 保留已有附件。
- `GET /api/ai/knowledge/attachments/{attachmentId}/download`：受鉴权下载。
- `GET /api/ai/knowledge/attachments/{attachmentId}/preview`：图片受鉴权预览。
- `DELETE /api/ai/knowledge/{id}`：知识及其附件逻辑删除，物理文件由后续清理任务处理。

前端提交的 `FormData` 必须包含 `knowledgeType=DEFECT`，不得把故障知识写入缺陷列表。附件支持图片、PDF、Office、TXT/MD 和 ZIP/RAR/7Z，单个文件不超过 20MB，每条知识最多 10 个附件；服务端校验是最终边界。

## 权限与数据隔离

- 页面按钮复用 `ai:knowledge:create`、`ai:knowledge:update`、`ai:knowledge:delete`。
- 后端仍由 `JwtAuthInterceptor` 负责最终鉴权，前端按钮隐藏不能替代后端权限校验。
- 列表请求固定使用 `knowledgeType=DEFECT`，编辑打开详情后再次校验返回类型，防止跨类型误编辑。

## 验证记录

- 2026-08-11：前端 `scripts/build-web.ps1` 通过，`vue-tsc --noEmit` 与 Vite 生产构建均通过；后端 `mvn test` 75/75 通过。
- 已验证构建产物包含独立 `DefectKnowledgeView` 页面资源。
- 登录态下的新增、编辑、附件下载/预览/逻辑删除仍需在本机测试账号和可写数据库环境中执行页面集成回归。
