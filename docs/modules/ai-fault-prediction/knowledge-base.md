# 故障知识库管理

## 已实现范围

- 页面路由：`/fault-prediction/knowledge`，按 Figma Make `KnowledgeBase.tsx` 实现筛选区、列表、分页、右侧新增/编辑抽屉和删除确认。
- 筛选：知识 ID、知识名称、创建者、创建日期。
- 列表：知识 ID、名称、创建者、创建时间、最新修改时间、编辑/删除操作。
- 新建或更新必须填写知识名称、知识内容并至少保留一个附件；知识名称在未删除记录中唯一。

## 附件与接口

- `POST /api/ai/knowledge`：兼容 JSON 旧接口；multipart 新接口用于页面新增。
- `PUT /api/ai/knowledge/{id}`：兼容 JSON 旧接口；multipart 新接口在保存时根据 `retainedAttachmentIds` 完成附件逻辑删除和新增关联。
- `GET /api/ai/knowledge/attachments/{attachmentId}/download`：所有允许查看知识的角色可下载。
- `GET /api/ai/knowledge/attachments/{attachmentId}/preview`：仅图片可预览。
- 限制：最多 10 个；每个不超过 20MB；白名单为图片、PDF、Office、TXT/MD、ZIP/RAR/7Z。

## 数据与安全

- 主表：`kb_instance`；附件表：`kb_attachment`。
- 文件根目录：`storage.knowledge.local-root`，默认 `./data/knowledge-files`；可用部署环境变量 `KNOWLEDGE_STORAGE_ROOT` 覆盖。
- 文件采用随机服务端文件名和相对路径，禁止使用客户端路径；图片写入后执行内容校验。
- 知识、附件均逻辑删除；物理文件保留给后续清理和恢复任务。
- 每次新建、编辑、删除均写入操作审计。

## 权限

- `admin`：查看、新增、编辑、删除、下载、预览。
- `operator`：查看、新增、编辑、下载、预览。
- `viewer`：查看、下载、预览。

前端按钮只负责体验控制，后端 `JwtAuthInterceptor` 为最终拦截边界。

## 历史数据兼容

- 允许既有演示记录的 `created_by` 为空；列表和详情接口将创建者名称返回为空，不阻断知识内容、附件与权限信息的读取。
- 服务端未处理异常统一记录完整日志，客户端仅接收既有统一错误响应，不暴露堆栈信息。
- 2026-08-04 回归结果：开发版 `GET /api/ai/knowledge?knowledgeType=FAULT&pageNo=1&pageSize=10` 返回 HTTP `200`、业务码 `200`，共返回 2 条记录。

## 上传交互

- 上传区使用已注册的 Element Plus `ElUpload`，以蓝色虚线拖拽面板呈现，支持点击和拖拽选择。
- 浏览器文件选择器按后端白名单过滤；选择后展示待上传文件名、大小和移除操作。保存时统一以 `FormData` 的 `files` 字段提交，不在选择文件时提前写入数据库。
- 已有附件与待上传附件分别展示；已有附件可预览、下载或从本次编辑的保留清单中移除，待上传附件可在保存前撤销。
- 2026-08-04 实测：管理员上传 Markdown 附件、受鉴权下载及知识逻辑删除均返回成功。
