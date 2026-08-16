# Frontend Coding Standard

## Stack

- Vue 3
- Vite
- TypeScript
- Element Plus
- Pinia
- Vue Router
- Axios

## Structure

- API 类型和请求函数放在 `apps/admin-web/src/api/modules`。
- 登录用户状态放在 `apps/admin-web/src/stores/user.ts`。
- 通知状态放在 `apps/admin-web/src/stores/notification.ts`。
- 页面放在 `apps/admin-web/src/views`。
- 系统管理页面放在 `apps/admin-web/src/views/system`。

## UI Rules

- 企业后台界面保持紧凑、清晰、可扫描。
- 优先使用 Element Plus 表格、分页、抽屉、弹窗、标签和表单。
- 操作按钮应使用图标加文字，危险操作必须确认。
- 表格操作列要给足宽度，避免按钮换行。
- 表单字段必须设置 `maxlength` 和规则校验。
- 页面不写大量说明文字，用户通过控件完成任务。

## API Types

- 后端 Long ID 到前端按 `string` 处理。
- API 类型必须和后端 VO/DTO 同步。
- 新增后端字段时必须同步 API 类型、Pinia 状态和页面表单。

## Performance

- 不允许在 `main.ts` 中全量 `app.use(ElementPlus)`。
- Element Plus 组件应在 `main.ts` 中按当前项目实际使用范围注册。
- Element Plus 样式应按组件引入，避免恢复 `element-plus/dist/index.css` 全量样式。
- 大型共用页面的下拉选项、基础字典和静态选择数据应做页面级缓存，避免路由切换重复请求。
- 新增大型第三方依赖或全局组件注册后，必须运行 `scripts/build-web.ps1` 并关注构建体积变化。

## Permission

- 页面路由使用 `meta.permission`。
- 按钮显示按权限编码控制。
- 左侧菜单从后端菜单树动态渲染。
- 修改权限后需要重新登录或刷新当前用户信息。

## Verification

前端改动后至少运行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-web.ps1
```

构建包含 `vue-tsc --noEmit`，可作为类型检查。
