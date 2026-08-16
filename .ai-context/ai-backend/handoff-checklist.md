# Handoff Checklist

- 数据表、实体、DTO、VO 是否一致。
- 数据库设计是否已对齐 `docs/数据库设计说明书_v1(1).docx` 和 `docs/context/requirements/database-design.md`。
- 如当前实现与数据库设计说明书存在命名差异，`PROJECT_SPEC.md` 是否已记录映射和兼容原因。
- 前端 API 类型是否需要同步。
- 权限编码是否初始化并接入拦截。
- 写操作是否记录审计。
- `mvn -DskipTests compile` 是否通过。
