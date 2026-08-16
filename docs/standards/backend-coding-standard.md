# Backend Coding Standard

## Stack

- Spring Boot 3
- Java 17
- MyBatis-Plus
- MySQL
- Jakarta Validation

## Layering

后端遵循：

```text
Controller -> Service Interface -> Service Impl -> Mapper -> Entity
```

规则：

- Controller 只负责 HTTP 入参、鉴权后的调用和统一返回。
- Service Impl 承载业务规则、事务、校验和审计。
- Mapper 只做 MyBatis-Plus 数据访问。
- Entity 映射数据库表。
- DTO 用于入参。
- VO 用于出参。

## API Response

- 所有业务接口返回 `ApiResponse`。
- 分页接口返回 `PageResponse`。
- 业务失败抛 `BusinessException`。
- 不在 Controller 中拼接不统一的响应结构。

## Validation

- DTO 必须使用 Jakarta Validation。
- 枚举类字符串必须用 `@Pattern` 或后端白名单校验。
- 用户输入字段必须限制长度。
- 写入数据库前必须做业务唯一性、关联存在性和状态合法性校验。

## Transaction

- 涉及多表写入必须使用 `@Transactional(rollbackFor = Exception.class)`。
- 例如用户与角色关系、角色与菜单关系、业务系统与数据库关系。

## Audit

- 新增、编辑、删除、状态变更、重置密码、个人资料修改等写操作必须记录操作日志。
- 登录成功和失败必须记录登录日志。
- 安全相关事件可生成通知。

## Compatibility

- 已有数据库升级优先用兼容初始化器补字段。
- 不直接删除已上线字段。
- 不为了重命名破坏现有 API 和前端调用。

## Verification

后端改动后至少运行：

```powershell
mvn -DskipTests compile
```

