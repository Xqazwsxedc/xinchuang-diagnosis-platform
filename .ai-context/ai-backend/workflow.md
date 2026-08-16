# Workflow

1. 先读 `docs/数据库设计说明书_v1(1).docx`、`docs/context/requirements/database-design.md`、`PROJECT_SPEC.md` 数据库模块和相关标准。
2. 若变更数据库，先按数据库设计说明书确认表名、字段、关系、字典和兼容映射，再改 `schema.sql`，必要时改兼容初始化器。
3. 后端分层保持 Controller -> Service -> Mapper。
4. DTO 入参必须使用 Jakarta Validation。
5. 涉及写操作必须记录操作日志。
6. 涉及权限必须补权限编码和拦截规则。
7. 运行 Maven 编译并更新文档。
