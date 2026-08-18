# AI Test Compact

职责：维护验收清单、接口冒烟测试、权限测试和回归风险清单。

必须先读：

- `PROJECT_SPEC.md`
- `.ai-context/shared/verification-baseline.md`
- `docs/standards/testing-standard.md`
- `docs/context/project-baseline.md`

当前重点：基座、系统管理、数据台账、AI 数据壳的一期验收。

系统管理最近验证基线：

- `admin` 可查询系统管理模块；`operator`、`viewer` 访问用户管理接口返回 HTTP 403。
- `viewer` 访问已授权数据大屏返回 HTTP 200。
- 非法系统配置类型必须返回 `code = 400` 且不落库。

数据台账最近验证基线：

- `viewer` 查询数据台账为 HTTP `200`，直接调用业务系统新增、删除均为 HTTP `403`。
- `operator`、`admin` 对空保存请求返回业务码 `400` 且不落库；非法台账状态查询返回业务码 `400`。
- 启动后必须检查 `viewer` 未获得任何 `ledger:business:*` 或 `ledger:database:*` 写权限。
- 详情页接口：`admin`、`operator`、`viewer` 查询业务数据、数据库指标和日志均返回业务码 `200`；非法 `range` 返回业务码 `400`。
- 下载：业务数据、指标、日志返回 `text/csv`，并核对 `report_export_log` 产生 `BUSINESS_DATA`、`DB_METRICS`、`DB_LOG` 成功记录。

测试自动化基线（2026-08-04）：

- 测试 2.0 已新增业务系统服务、数据库资源服务和台账请求校验白盒测试；`mvn verify` 共 35 项通过（9 个测试类，0 失败/错误），JaCoCo 行覆盖率为 10.61%，报告为 `docs/testing/测试实施与质量报告-2.0.md`。JaCoCo XML 路径为 `apps/api-server/target/site/jacoco/jacoco.xml`。
- SonarQube 项目键为 `xinchuang-diagnosis-platform`，扫描必须在单测后执行，并导入 JaCoCo/Surefire 报告。1.0 基线（22 项单测、质量门禁 `OK`）见 `docs/testing/sonarqube-quality-report-2026-08-04.md`；测试 2.0 已将单测扩展至 35 项、JaCoCo 行覆盖率 10.61%，但 SonarQube 2.0 提交因本机认证返回 401 暂未完成，不得把 1.0 指标冒充 2.0 指标。
- 管理员初始化仅允许 `BOOTSTRAP_ADMIN_ENABLED=true` 且配置 `BOOTSTRAP_ADMIN_PASSWORD` 时创建不存在账号；已有管理员绝不重置密码或状态。开发脚本会临时生成 JWT 密钥，部署必须设置至少 32 位 `JWT_SECRET`。
- 接口黑盒脚本为 `docs/testing/api-blackbox-test.ps1`，只读查询及无权限写拦截验证共 14 项；结果为 `docs/testing/results/api-blackbox-result.json`。
- 接口黑盒 2.0 脚本为 `docs/testing/api-blackbox-test-2.0.ps1`，经部署 Nginx 验证 50/50 通过，覆盖系统管理、AI 查询、台账详情、非法参数及 admin/operator/viewer 权限边界；结果为 `docs/testing/results/deployment-api-blackbox-result-2.0.json`。脚本从 `DIAGNOSIS_TEST_PASSWORD` 获取测试账号密码。
- 页面集成基线为 `docs/testing/frontend-integration-checklist.md`，覆盖管理员登录、系统管理五项动态菜单、角色权限、台账详情和安全登录日志。
- 测试用例设计总表为 `docs/testing/测试用例设计说明-2.0.md`，已逐项列明 35 个白盒与 50 个黑盒用例；后续新增用例必须先更新该表、再执行测试、最后更新结果报告。
- 2026-08-04 新增 `FaultKnowledgeServiceImplTest`，覆盖知识名称必填、附件必填、详情不存在，后端全量为 38/38 通过。后续需在独立测试账号下补充 multipart 上传、图片预览、下载、逻辑删除及 admin/operator/viewer 黑盒用例，避免污染演示库。
- 2026-08-04 知识库回归新增历史记录 `created_by = null` 的列表和详情白盒用例，要求接口返回 200/正常 VO，创建者为空，不得因空 Map 查询触发 500；异常日志仅用于服务端排查，不得向客户端泄露堆栈。
- 本次执行结果：`mvn test` 共 40/40 通过、0 失败/错误；使用管理员登录态调用开发版故障知识库列表返回 HTTP 200、业务码 200、2 条记录。后续该场景为必跑回归项。
- 2026-08-04 附件上传回归：验证新增抽屉中真实上传控件可见；管理员 multipart 上传 1 个 Markdown 附件、受鉴权下载及逻辑删除均通过。前端 `pnpm build` 通过；后续调整上传控件时必须同时回归选择文件、移除待上传文件、格式/大小限制和保存后附件记录。
- 2026-08-05 故障预测任务配置：新增 `AiManagementServiceImplFaultTaskTest` 5 项，覆盖模型必选、每周规则星期必选、人工执行生成 `PENDING` 且无结束时间、创建者批量回填、非法创建日期；后端 `mvn verify` 45/45 通过，JaCoCo 行覆盖率 15.81%（732/4629），前端 `pnpm build` 通过。后续黑盒需使用独立任务数据验证 admin/operator/viewer CRUD 边界、对象与模型真实存在性、详情与预警跳转，不得把 `PENDING` 当作成功预测。
- 2026-08-05 专用故障任务接口收口回归：后端 `mvn verify` 49/49 通过，JaCoCo 行覆盖率 17.37%（826/4756）；健康检查 200，未登录专用列表 401，前端构建通过。由于 `DIAGNOSIS_TEST_PASSWORD` 未设置，admin/operator/viewer 登录态 CRUD、立即受理和页面视觉回归未执行，待独立测试凭据与可用浏览器会话补测。
- 2026-08-05 故障智能预警：新增 3 项白盒，覆盖非法状态跳转、确认预警写处理日志、空备注拒绝；全量后端 52/52 通过、JaCoCo 18.04%（879/4872），前端构建通过，隔离实例专用接口未登录 401。后续必须补 admin/operator/viewer 的处理/删除权限、终态拒绝处理、详情历史和原型抽屉视觉验收；未设置 `DIAGNOSIS_TEST_PASSWORD` 前不得标记为通过。
- 2026-08-06 前端预警详情回归点：打开任一详情时首屏必须依次可见故障 ID、预测对象、告警时间、指标类别、预测故障类型、预计发生时间，随后才是置信度和趋势；关闭并再次打开后仍必须从顶部开始。`scripts/build-web.ps1` 通过，这不替代登录态浏览器回归。
- 2026-08-06 角色权限页前端回归点：`/system/roles` 首屏为左侧角色列表、右侧权限配置；切换角色需回显已选菜单，菜单/按钮页签保存后必须调用原菜单授权接口并补齐父级；数据权限页保存需保留 `CUSTOM` 非空校验。viewer 的保存按钮必须禁用。前端生产构建通过，登录态交互待独立测试账号补测。
- 2026-08-06 故障任务保存回归：确认旧演示定时规则缺少预测范围时，页面编辑回显为 4 小时且保存请求不含 `undefined`；确认定时设置布局包含规则列表、时间、预测范围、每日/每周选择和创建者只读字段。后端全量 `mvn test` 52/52 通过，前端生产构建通过；登录态浏览器视觉和真实更新仍需测试凭据补验。
- 2026-08-11 新增故障任务专用编辑回归：请求不携带 `taskCode` 时，更新实体必须继承原编码并成功进入通用更新服务；`FaultPredictionTaskServiceImplTest` 增至 3 项，后端全量 `mvn test` 53/53 通过。

- 2026-08-11 数据台账新增 UI 回归：管理员或 operator 打开唯一“新增”抽屉后，应能在有权限的“业务系统 / 分布式数据库”页签间切换；切换后表单和校验必须重置。业务系统必须校验至少一条关联数据库，数据库必须校验公网 IPv4，提交分别调用既有资源接口并保持 `status = NORMAL` 默认值。编辑不得切换资源类型；viewer 不得看到新增入口。本轮仅完成前端生产构建，登录态写入回归待可写测试数据环境补测。
- 2026-08-11 测试 2.1：新增系统请求、AI 请求、通知服务白盒测试，`mvn verify` 为 73/73 通过，JaCoCo 行覆盖率 21.05%（1029/4889）；黑盒 2.1 继承 2.0 基线并扩展专用任务、预警、知识库、通知、筛选和权限，68/68 通过，逐条结果见 `docs/testing/results/api-blackbox-result-2.1.json`。SonarQube 服务未启动，页面成功写入验收待独立数据环境。
- 2026-08-11 台账详情演示数据时效性回归：内置资源的旧指标/日志时间戳落在默认 `1D` 范围外，刷新演示记录时间后，`GET /ledger/db-resources/1900000000000000001/metrics?range=1D` 返回 15 点和最新四项指标，日志接口返回 3 条，均为业务码 200。后续真实采集接入前，演示环境重置后需确认 `data.sql` 使用当前 `NOW()` 初始化。
- 2026-08-11 AI 故障预测专项回归：新增专用任务“完成必须有结束时间”和“非故障任务拒绝详情”白盒用例；故障知识库、任务、预警、执行回调和 DTO 定向白盒 27/27 通过。专项黑盒基于 2.1 已通过结果验证知识库、专用任务/预警查询、非法筛选和 viewer/operator 写权限拒绝；交付文档见 `docs/testing/ai-fault-prediction-test-*-2.1.md`。
- 2026-08-11 缺陷知识库页面切换为 `DefectKnowledgeView.vue`，复用真实附件上传/下载/图片预览链路并固定 `DEFECT` 类型；前端生产构建通过。需在登录态继续补新增、编辑保留附件、删除附件、下载、预览和逻辑删除黑盒回归。
- 2026-08-11 页面切换后的后端全量 `mvn test` 为 75/75 通过；类型编码和审计文案按知识类型改造未引入回归。
- 2026-08-11 AI 专项白盒代码注释已补齐：五个 AI 测试类增加类级测试边界和方法级业务意图说明；复跑 `mvn test` 定向 AI 用例 27/27 通过。
- 2026-08-11 缺陷检测任务配置页面完成首轮改造；前端构建通过，后端全量 `mvn test` 77/77 通过。新增白盒覆盖每周规则缺星期和立即检测 PENDING；后续重点补模型类型隔离、三角色黑盒和浏览器交互测试。
- 2026-08-13 缺陷检测报表切换到专用页面，新增缺陷 ID/任务名称/创建者筛选、详情报告弹窗和 CSV 批量导出接口；前端构建及后端 `mvn test` 77/77 通过。后续补选中清单为空/超过 100/含无效 ID、CSV UTF-8 内容、viewer 导出和页面多选导出黑盒。
- 2026-08-13 报表筛选与导出回归：修复前端 `defectId`/后端 `defectCode` 参数不一致；新增空导出清单拒绝、创建者批量回填和 CSV 核心字段白盒用例。后端 `mvn test` 79/79 通过，前端 `scripts/build-web.ps1` 通过。待独立登录态环境补测超过 100 条、无效 ID、viewer 导出权限及页面多选交互。
- 2026-08-13 性能优化知识库页面已切换为 `OptimizationKnowledgeView.vue`；专项测试需覆盖 `knowledgeType=OPTIMIZATION` 查询隔离、新增/编辑 multipart 附件、保留与删除附件、图片预览、其他格式下载、逻辑删除及三角色权限。当前仅完成前端构建，登录态可写数据回归待补测。
- 2026-08-13 性能优化任务与建议专项测试：白盒需覆盖 `OPT-TASK-` 自动编码、模型类型/定时规则校验、执行 `PENDING`、建议空/无效/超量导出、CSV 内容和创建者回填；黑盒需覆盖三角色任务 CRUD/执行边界、建议查询筛选、导出及确认/误报处理。登录态可写环境回归待补测。
- 2026-08-13 已新增性能优化任务自动编码、执行 `PENDING`、编辑保留原任务编码白盒用例，后端全量 `mvn test` 82/82 通过；前端生产构建通过。优化建议的空/无效/超量导出、CSV 内容和创建者回填仍需补充白盒与登录态黑盒。
- 2026-08-13 测试 3.0：`mvn verify` 白盒 82/82 通过，JaCoCo 行覆盖率 21.99%（1110/5048）；前端 `scripts/build-web.ps1` 通过；`api-blackbox-test-3.0.ps1` 在真实 admin/operator/viewer 登录态下 63/63 通过。脚本只执行读取、非法参数和越权拒绝，不污染演示库。
- 3.0 首轮黑盒发现 `operator` 可删除缺陷报表，根因是默认初始化角色错误包含 `ai:defect:delete` 与 `ai:optimization:delete`。已修复 `SystemBootstrapInitializer` 的 operator 默认菜单集合，完整回归后 BB30-PERM-10/11 返回 403。后续调整内置角色授权时必须回归三角色写/删除边界。
- 3.0 交付物：`docs/testing/测试实施与质量报告-3.0.md`、`测试用例执行明细-3.0.md`、`测试代码与证据路径索引-3.0.md`、`frontend-integration-checklist-3.0.md` 和 `results/api-blackbox-result-3.0.json`。SonarQube、隔离库成功写入、真实附件物理链路、浏览器视觉验收均未执行，不得标记为通过。
- 审计时间回归：`MybatisMetaObjectHandlerTest` 验证更新填充会覆盖已有旧 `updatedAt`。台账编辑验收须确认列表更新时间变为本次保存时间，而非创建时间。
- 2026-08-17 Prometheus 白盒基线：新增 `PrometheusResponseParserTest`（matrix/vector、NaN/Inf、非 success）、`PrometheusRangeResolverTest`（8 个范围映射边界）和 `DbPrometheusMetricsServiceTest`（五类指标合并与当前连接数）。后端 `mvn test` 91/91 通过，前端生产构建通过。真实 HTTP、登录态指标下载和页面视觉验收待本机 `MONITORING_PROMETHEUS_BASE_URL` 配置后补测；不得用单测替代真实 Prometheus 联调。
- 2026-08-17 真实登录态指标接口曾复现 URI 模板异常：`{instance=...}` 的 PromQL 触发 HTTP 500。已新增 `PrometheusHttpClientTest`，通过本地 HTTP Server 断言花括号百分号编码且响应可解析；后端全量 JUnit 92/92 通过，重新部署后登录态 `/ledger/db-resources/{id}/metrics?range=1D` 返回业务码 200、14 个趋势点及实时 CPU/连接数。其余七个范围、CSV 和页面视觉仍须单独验收。
- 2026-08-18 指标下载弹窗已完成前端生产构建；新增日期范围解析白盒用例，覆盖上海时区结束日包含、倒序/缺失/超90天拒绝。待 Maven 仓库网络恢复后复跑全量单测，并在真实 Prometheus 登录态验证弹窗下载、所选 CSV 列、空选择和未接入资源拒绝。
- 2026-08-18 数据库详情页空态回归要求：日志 `records=[]` 时页面不得出现 Element Plus `No Data`，只显示“当前时间范围暂无服务器日志”；指标 `points=[]` 时不得渲染四个图表或缓存圆环，只显示“暂无指标数据”。补充 721-1300px 摘要两列、日志区横向滚动的浏览器视觉检查；本轮无后端接口或数据库变更。
