# Verification Baseline

基线日期：2026-07-19。

迁移更新日期：2026-07-20。

测试 2.0 更新日期：2026-08-05。

- 后端上一轮 `mvn verify`：9 个测试类、35 项通过、0 失败、0 错误；JaCoCo 行覆盖率 10.61%。故障预测任务配置补充后，当前结果为 12 个测试类、49 项通过、0 失败、0 错误；JaCoCo 行覆盖率 17.37%（826/4756）。
- 部署版黑盒 2.0：`docs/testing/api-blackbox-test-2.0.ps1` 经 `http://localhost/api` 执行 50/50 通过；结果为 `docs/testing/results/deployment-api-blackbox-result-2.0.json`。
- 用例设计依据：`docs/testing/测试用例设计说明-2.0.md`；脚本从 `DIAGNOSIS_TEST_PASSWORD` 环境变量读取测试账号密码，禁止写入密码。
- SonarQube 2.0 尚未扫描：本机扫描认证返回 401，等待具备 `Execute Analysis` 权限的临时令牌；不得将 1.0 SonarQube 指标用于 2.0 结论。

第 0 步验证结果：

- 后端基线编译通过：`mvn -DskipTests compile`
- 前端基线构建通过：`pnpm build`

第 5、6、7、8、9、10 步验证结果：

- 已创建 `apps/` 和 `scripts/`。
- 已将 `backend/` 迁移为 `apps/api-server/`。
- 后端迁移后编译通过：`mvn -DskipTests compile`。
- 已将 `frontend/` 迁移为 `apps/admin-web/`。
- 前端迁移后构建通过：`pnpm build`。
- 前端迁移后已清理旧 `node_modules` 并在 `apps/admin-web/` 下离线重建依赖，避免 pnpm junction 指向旧 `frontend/`。
- 已新增 `apps/admin-web/pnpm-workspace.yaml`，允许 `esbuild`、`@parcel/watcher`、`vue-demi` 执行安装脚本。
- 已新增统一脚本：`scripts/dev-api.ps1`、`scripts/build-api.ps1`、`scripts/dev-web.ps1`、`scripts/build-web.ps1`、`scripts/verify-all.ps1`。
- 第 8 步验证：`scripts/build-api.ps1` 通过，`scripts/build-web.ps1` 通过，`scripts/verify-all.ps1` 通过。
- 已新增结构交接文档：`docs/context/enterprise-structure-handoff.md`。
- 已新增脚本说明：`scripts/README.md`。
- 第 10 步最终验证：`scripts/verify-all.ps1` 通过。
- 已新增一键启动入口：`start-all.bat` 和 `scripts/dev-all.ps1`，`scripts/dev-all.ps1 -DryRun` 通过。
- 本轮性能优化验证：`scripts/verify-all.ps1` 通过。
- 前端构建体积优化后主 JS 约 `544.65 kB / gzip 177.41 kB`，主 CSS 约 `209.86 kB / gzip 28.62 kB`。
- 后端业务系统分页已批量加载关联数据库，避免列表页 N+1 查询。
- 2026-07-21 已重新学习 `docs/数据库设计说明书_v1(1).docx`，新增 `docs/context/requirements/database-design.md`，并将数据库设计蓝图要求写入 `PROJECT_SPEC.md`、`docs/standards/database-standard.md` 和 `.ai-context`；本轮为文档和上下文变更，未改业务代码。
- 2026-07-27 系统管理模块代码打磨完成：后端用户分页、菜单树、字典更新、通知批量操作完成性能与一致性优化；用户、角色、菜单、字典、配置、通知写操作补齐事务边界；用户和角色页面补齐按钮权限显示。后端编译、前端构建、管理员接口、非法参数及 `admin/operator/viewer` 权限回归均通过。
- 2026-07-27 数据台账模块代码打磨完成：请求边界校验、写操作事务、对象存在性复用与前端按需加载已补齐，分页关联数据保持批量加载；后端编译、前端构建和全量验证通过。独立临时实例验证 `viewer` 台账查询 HTTP `200`、台账写操作 HTTP `403`，非法保存/非法状态查询返回业务码 `400` 且未写入数据。
- 2026-07-29 数据台账详情页完成：业务系统详情包含真实演示业务数据记录及 CSV 下载；数据库详情包含指标卡、时间范围、趋势、缓存命中率、日志和下载。隔离实例验证后端编译、前端构建、`admin/operator/viewer` 详情查询、非法时间范围、三类 CSV 下载和 `report_export_log` 审计均通过。
- 2026-07-29 详情页返回列表回归通过：业务系统和分布式数据库列表进入详情后，点击详情标题均返回对应来源页签；前端生产构建通过。
- 2026-07-29 详情页 Figma 原型对齐完成：两类详情页 UI 已按 Figma Make `DataDetail.tsx` 收敛；业务记录和服务器日志选中下载使用后端 `recordIds`、`logIds` 重新校验范围。隔离后端验证单项 CSV 均只导出 1 条数据，后端编译、前端构建和浏览器页面验收通过。
- 2026-08-04 故障知识库管理：后端全量 `mvn test` 为 38/38 通过，前端 `pnpm build` 通过；本机部署重新构建且 Nginx 首页、后端健康接口均为 HTTP 200。Vite 在沙箱中仍需提权写临时配置文件。
- 2026-08-04 故障知识库启动后列表 500 已修复：原因是旧 `kb_instance` 演示记录 `created_by` 为空，VO 映射对不可变空 Map 进行空键查询；列表和详情现均兼容空创建人，并新增对应白盒回归用例。修复后需重新执行后端全量测试及开发版知识库列表接口回归。
- 2026-08-04 知识库兼容修复验证完成：后端 `mvn test` 共 40/40 通过、0 失败/错误；开发版 8080 健康检查为 HTTP 200，以管理员登录态请求故障知识库列表为 HTTP 200、业务码 200、返回 2 条记录。临时 18081 定位实例及其日志文件均已清理，5173 前端未停止。
- 2026-08-04 知识库附件上传前端闭环完成：补齐 `ElUpload` 按需注册与样式，浏览器验收新增抽屉的上传面板可点击且与原型一致；前端构建通过。开发版实际 multipart 上传、受鉴权下载、逻辑删除均通过，回归记录已逻辑删除。
- 2026-08-05 故障预测任务配置完成：后端新增专用故障任务 DTO/VO/Controller、模型/调度/指标校验、`PENDING` 人工受理记录和创建者批量回填；前端新增 Figma 对齐任务配置专用页。后端 `mvn verify` 49/49 通过，JaCoCo 行覆盖率 17.37%（826/4756），前端 `scripts/build-web.ps1` 通过；运行时健康检查 200，未登录专用列表 401。尚未进行需写入演示库的任务 CRUD 黑盒回归，也未接入真实 AI 引擎；本机没有 `DIAGNOSIS_TEST_PASSWORD`，登录态黑盒待补测。
- 2026-08-05 故障智能预警完成：专用预警页面和详情抽屉已替代通用页，`fault_report + result_status_log` 通过专用 `/ai/fault-prediction/alerts` 接口提供查询、详情、处理和删除；处理状态机和备注由后端强制。后端 `mvn verify` 52/52 通过，JaCoCo 行覆盖率 18.04%（879/4872），前端构建通过；隔离实例健康检查 200、未登录专用列表 401。登录态三角色黑盒和浏览器视觉验收待 `DIAGNOSIS_TEST_PASSWORD`。
- 2026-08-05 预警抽屉视觉复核：实现已改为 584px 固定标题/正文滚动/固定底栏，并固定呈现“确认 / 误报 / 关闭”按钮；前端生产构建再次通过。需在登录态浏览器会话按原型截图进行最终视觉验收。
- 2026-08-06 预警抽屉首屏回归：故障 ID、预测对象、告警时间、指标类别、预测故障类型和预计发生时间已改用 `FaultAlertView.vue` 中的显式 DOM 结构，并在抽屉 `open`、`opened` 及数据加载后复位正文滚动；`scripts/build-web.ps1` 通过。Figma MCP 本轮达到账号 View 调用额度，未能取得新源码；仍需登录态浏览器视觉验收。

- 2026-08-06 故障预测任务编辑兼容回归：旧演示 `schedule_rule` 缺少 `predictionWindowHours` 时，前端回显默认 4 小时且保存请求不含 `undefined`；定时设置布局按原型包含规则列表、时间、预测范围、每日/每周选择和创建者只读字段。后端 `mvn test` 52/52 通过，前端 `scripts/build-web.ps1` 通过；登录态真实保存与浏览器视觉仍需测试凭据补验。
- 2026-08-06 定时设置宽度回归：`.schedule-form-item` 的表单内容区、规则列表和添加规则卡片必须保持 100% 宽度，不能被 Element Plus 压缩成窄列；前端构建通过，登录态浏览器视觉仍需实际打开抽屉确认。
- 2026-08-11 故障任务专用编辑回归：前端请求不传内部 `taskCode`，后端更新链路必须复用原编码，不得返回“任务编码不能为空”；后端 `mvn test` 53/53 通过。
- 2026-08-11 数据台账新增抽屉原型对齐：`DataLedgerView.vue` 已收敛为单一“新增”抽屉，业务系统与分布式数据库通过顶部页签切换；类型检查和前端生产构建 `scripts/build-web.ps1` 通过。未修改后端、数据库或部署脚本；尚未执行会写入演示库的登录态新增保存回归。

后端验证命令：

```powershell
cd "E:\vibe work\springboot work"
powershell -ExecutionPolicy Bypass -File .\scripts\build-api.ps1
```

前端验证命令：

```powershell
cd "E:\vibe work\springboot work"
powershell -ExecutionPolicy Bypass -File .\scripts\build-web.ps1
```

全量验证命令：

```powershell
cd "E:\vibe work\springboot work"
powershell -ExecutionPolicy Bypass -File .\scripts\verify-all.ps1
```

已知非阻断警告：

- Sass legacy JS API deprecation warning。
- Rollup 对第三方依赖 `/* #__PURE__ */` 注释位置的提示。
- Vite chunk 大小超过 500 kB 的提示。

环境注意：

- 前端构建在沙箱内可能因 Vite 写临时 `vite.config.ts.timestamp-*.mjs` 文件触发 `EPERM`，需要提权重跑。
- pnpm v11 在沙箱内写临时文件或执行 `approve-builds` 也可能触发 `EPERM`，需要提权后在项目目录内执行。
- 当前 Git 根目录可能位于父级 `E:\vibe work`，项目目录可能显示为 untracked；不要只依赖 Git 状态作为回退点。
