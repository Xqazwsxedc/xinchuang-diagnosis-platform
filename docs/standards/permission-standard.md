# Permission Standard

## Current Model

当前权限模型：

```text
sys_user -> sys_user_role -> sys_role -> sys_role_menu -> sys_menu.permission_code
```

能力：

- 动态菜单。
- 按钮权限。
- 后端接口权限编码拦截。
- admin 超级管理员兜底。
- 角色数据权限字段。

## Permission Code

权限编码格式建议：

```text
module:resource:action
```

示例：

- `system:user:view`
- `system:user:create`
- `system:role:assign-menu`
- `system:security:view`
- `ledger:business:update`
- `ai:task:execute`

## Role Data Scope

当前枚举：

- `ALL`
- `DEPARTMENT`
- `SELF`
- `CUSTOM`

规则：

- `CUSTOM` 必须填写 `data_scope_config`。
- 非 `CUSTOM` 保存时清空 `data_scope_config`。
- 当前字段用于记录角色数据边界，后续业务查询过滤需按模块继续落地。

## Frontend Rules

- 路由使用 `meta.permission`。
- 按钮按权限编码显示隐藏。
- 菜单从后端返回的菜单树渲染。
- 系统管理侧栏只渲染五个正式入口；历史直达路由不能因为保留兼容而重新成为菜单项。

## Backend Rules

- 新增接口必须评估是否需要权限编码。
- 修改拦截规则后必须更新菜单权限初始化。
- `GET /system/security/login-logs` 必须使用 `system:security:view`，不能借用审计日志权限，以确保安全入口的前后端权限链路一致。
- 权限不足返回 403。
