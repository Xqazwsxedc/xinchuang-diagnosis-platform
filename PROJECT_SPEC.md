# 项目说明书

## 1. 项目目标

### 技术交流图：AI 故障预测与 API 分层（2026-08-21）

- 新增 `docs/diagrams/ai-fault-prediction-flow-figma.svg`，用于说明故障预测的任务配置、执行受理、未来引擎处理、结果回写和人工处置闭环。
- 新增 `docs/diagrams/ai-api-layered-flow-figma.svg`，用于说明 Vue/Axios、Controller、Service、Mapper、MySQL 的 API 分层链路；两图明确标注真实模型推理和引擎回调仍为后续接入点，不得误述为当前已实现能力。
- 新增 `docs/tech-sharing/AI故障预测调用流程与API服务技术交流.md`，用于技术交流的“调用流程与功能模块”“API 服务、接口设计及核心代码示例”两部分；文档包含接口契约、核心代码来源、权限边界和后续引擎适配建议。

### 1.0.2 测试 3.0 基线（2026-08-13）

- 3.0 已覆盖系统管理、认证/个人中心、数据台账、故障预测、缺陷检测、性能优化与权限边界；后端 `mvn verify` 为 82/82 通过，前端生产构建通过，真实登录态黑盒为 63/63 通过。
- 3.0 首轮发现默认 `operator` 错误拥有 `ai:defect:delete`、`ai:optimization:delete`，已从 `SystemBootstrapInitializer` 的默认权限集合移除并完成全量回归；自定义角色不会被该初始化同步覆盖。
- JaCoCo 行覆盖率为 21.99%（1,110/5,048），报告位于 `apps/api-server/target/site/jacoco/`。覆盖率仍不足以代表生产级全覆盖，后续优先补隔离环境的成功写入、附件物理文件、状态处理和浏览器页面交互。
- 3.0 交付文档：`docs/testing/测试实施与质量报告-3.0.md`、`测试用例执行明细-3.0.md`、`测试代码与证据路径索引-3.0.md`；黑盒原始结果为 `docs/testing/results/api-blackbox-result-3.0.json`。SonarQube 服务本轮未启动，未声明通过 Sonar 门禁。

### 1.0.3 Prometheus 指标接入（2026-08-17）

### 1.0.4 数据库指标下载弹窗（2026-08-18）

- 数据库资源详情的“下载指标”改为先打开原型风格下载弹窗，选择日期范围和 CPU/内存/缓存命中率后再下载。
- 保留 `/ledger/db-resources/{id}/metrics/download` 及旧 `range` 参数兼容；新增 `startDate`、`endDate`、`metrics` 可选参数。日期按 Asia/Shanghai 自然日处理，最大跨度 90 天。
- 后端指标编码采用白名单并按选择执行 Prometheus query_range，CSV 只包含所选指标列；空选择、非法日期和未知指标拒绝，仍执行导出审计和监控接入校验。

- 数据库资源详情的 CPU、内存、磁盘 IOPS、MySQL 当前连接数、InnoDB 缓存命中率已改为 `Vue -> Spring Boot -> Prometheus HTTP API` 实时查询。原 `/api/ledger/db-resources/{id}/metrics` 与 CSV 下载路径保持不变，日志链路、台账 CRUD 和 MySQL 表结构不变。
- 后端新增 `monitoring/prometheus` 配置、查询模板、HTTP 客户端、范围解析与安全响应解析；PromQL 集中管理。支持 `1H/3H/6H/12H/1D/3D/7D/14D`、即时卡片、区间趋势和 CSV 下载；`NaN/Inf`、空结果、非 success、网络/HTTP 异常会得到安全处理。
- 前端详情页增加“当前连接数”卡片与趋势图，监控服务异常时只在指标区域提示，不将旧 `db_instance_metric` 演示数据伪装为实时数据。
- `MONITORING_PROMETHEUS_*`、`MONITORING_TARGETS_*` 已加入 `application.yml`、开发环境导入白名单和部署模板。本机 `.env` 仅写入用户确认的单机 Docker `instance` 映射；基础地址仍需由用户在本机配置，不在源码、模板或文档中记录真实地址。
- 修复：PromQL 的 `{instance=...}` 标签选择器曾被 Spring URI 模板错误解析为变量，导致数据库详情指标接口返回 HTTP 500 和“系统繁忙”。`PrometheusHttpClient` 已改为先对查询参数进行百分号编码再创建 URI，并增加带标签选择器的本地 HTTP 回归测试。
- 本轮验证：后端 `mvn test` 92/92 通过（含 PromQL 标签选择器 URI 编码回归）；前端 `scripts/build-web.ps1` 通过。修复前的真实登录态指标接口已复现 HTTP 500；重新构建并重启部署后，同一资源的 `range=1D` 查询返回业务码 200、14 个趋势点及 CPU/当前连接数实时值，真实 HTTP 指标链路已通过该场景验收。

### 1.0.4 Prometheus 监控接入流程调整

- 旧的安装会话、一次性令牌、安装回调、安装命令生成和 exporter 安装入口已停用。
- 当前阶段保留真实 Prometheus HTTP 指标链路、指标下载、日志、台账 CRUD、权限和审计能力。
- 客户自配 Prometheus 资源级配置已实现：每个数据库资源独立保存 Prometheus 地址、凭据引用、数据库类型和 node/database 两组 selector；旧全局 target 不参与新查询，也不自动迁移。
- MySQL 的 node selector 查询 CPU、内存、磁盘 IOPS，database selector 查询当前连接数和缓存命中率；配置保存、连接测试和详情页展示已闭环。
- 已加入服务端 SSRF allowlist、精确 selector 校验、凭据脱敏和重定向禁止。部署环境必须显式配置允许的域名或 CIDR；真实 Prometheus 手工联调仍待执行。

本项目目标是基于 Figma Make 原型，从零实现一个企业级可落地的“信创数据库智能故障诊断及修复平台”。

一期目标聚焦基础框架和核心模块骨架：

- 后端采用 Spring Boot 3 + Java 17 + MyBatis-Plus + MySQL。
- 前端采用 Vue 3 + Vite + TypeScript + Element Plus + Pinia + Vue Router + Axios。
- 数据库采用 MySQL。
- 实现企业级工程基础能力：统一返回结构、统一异常处理、参数校验、分页查询、CORS、日志、基础权限预留。
- 前端尽量还原 Figma 原型的后台布局、菜单、表格、抽屉表单、状态标签和交互风格。
- 后端接口必须真实连接 MySQL，不使用纯 Mock 作为最终实现。
- AI故障预测、AI缺陷检测、AI性能优化三部分的一期目标为保留界面、数据结构和接口壳；真实模型、算法检测、自动修复能力暂不实现。

### 1.0.1 最近界面调整

- 系统管理的“角色权限管理”页面保持左侧角色列表、右侧权限配置的双栏布局。
- 右侧“菜单权限 / 按钮权限 / 数据权限”使用横向等分的全宽页签；当前选中的页签以蓝色整块背景展示，避免仅文字区域着色。
- 业务系统详情的业务数据表无记录时，隐藏表格内默认英文 `No Data`，仅保留下方统一的“暂无业务数据记录”空状态。
- 数据台账列表“更新时间”统一以审计字段 `updated_at` 为准；修复 MyBatis-Plus 更新填充不覆盖旧值的问题后，业务系统、分布式数据库及其他继承 `BaseEntity` 的实体修改后均会显示真实最后修改时间。

### 1.1 项目分期规划

当前建议将项目分为四期推进：

一期：企业级基础平台与核心业务闭环。

- 目标：形成可运行、可演示、可继续扩展的企业后台系统底座。
- 已完成重点：项目骨架、登录认证、JWT、用户管理、角色管理、菜单权限树、按钮权限、角色数据权限字段、个人中心自助维护、数据台账、数据大屏。
- 继续完善重点：AI 三大模块的数据壳页面和基础 CRUD、指标采样、日志采集和后续生产化能力。
- 一期不做：真实 AI 模型推理、自动诊断、自动修复、真实指标采集引擎。

二期：AI 数据壳与运维业务流程闭环。

- 目标：补齐故障预测、缺陷检测、性能优化三大模块的业务数据闭环，即任务配置、执行记录、结果报告、知识库、处理状态流转。
- 重点模块：`ai_model`、`ai_task`、`task_exec_record`、`kb_instance`、`fault_report`、`defect_report`、`opt_report`、`result_status_log`、`result_knowledge_ref`。
- 交付效果：即使暂不接入真实 AI，也能完整演示任务配置、结果生成、确认/误报/关闭、知识沉淀和报表导出流程。

三期：真实采集、诊断与智能分析能力。

- 目标：从“业务数据壳”升级为“真实运维诊断平台”。
- 重点模块：指标采集、日志采集、慢 SQL 采集、执行计划分析、规则诊断、模型服务调用、告警生成、趋势分析。
- 交付效果：数据大屏和 AI 结果不再依赖人工录入或演示数据，而是基于真实采集数据生成。

四期：生产化、自动化修复与高可用治理。

- 目标：达到企业生产环境可部署、可运维、可审计、可扩展。
- 重点模块：自动化修复流程、审批流、回滚策略、租户/组织隔离、细粒度数据权限、WebSocket 实时推送、部署脚本、监控告警、测试体系、性能优化和安全加固。
- 交付效果：具备生产上线基础，支持企业级运维治理。

## 2. 环境配置

### 2.1 项目路径

工作区路径：

```text
E:\vibe work\springboot work
```

当前项目结构：

```text
springboot work/
├── .ai-context/             # Vibe Coding AI 角色协作上下文
├── apps/
│   ├── api-server/          # Spring Boot 后端，已由 backend 迁移
│   └── admin-web/           # Vue 3 前端，已由 frontend 迁移
├── docs/                    # 需求上下文、规范、模块文档、验收材料
│   ├── 数据库设计说明书_v1(1).docx # 企业级数据库设计蓝图
│   ├── context/             # Word 需求拆解后的 Vibe Coding 上下文
│   ├── standards/           # 后端、前端、API、数据库、权限、测试、文档规范
│   ├── modules/             # 系统基座和系统管理模块说明
│   ├── project-structure-diagram-bw.svg # 当前项目结构黑白图
│   └── tech-sharing/        # 开发技术分享沉淀
├── start-frontend.bat       # Windows 双击启动前端脚本，当前指向 apps/admin-web
├── start-all.bat            # Windows 双击一键启动后端和前端
├── deploy/windows/          # Windows 本机部署脚本、Nginx 配置和本机运行产物
├── README.md                # 快速启动说明
├── PROJECT_SPEC.md          # 项目说明书，后续每轮对话结束必须更新
├── scripts/                 # 统一启动、构建、验证脚本目录，含 README.md
└── .gitignore
```

结构版迁移说明：

- 用户已确认采用结构版，将后端迁移为 `apps/api-server`，前端迁移为 `apps/admin-web`。
- 当前已完成第 5 步：创建 `apps/`、`scripts/`，迁移前后端基线验证通过。
- 当前已完成第 6 步：`backend/` 已迁移为 `apps/api-server/`。
- 第 6 步迁移后验证：在 `apps/api-server/` 执行 `mvn -DskipTests compile` 通过。
- 当前已完成第 7 步：`frontend/` 已迁移为 `apps/admin-web/`，`start-frontend.bat` 和文档路径已同步。
- 第 7 步迁移后验证：在 `apps/admin-web/` 执行 `pnpm build` 通过。
- 第 7 步迁移后依赖处理：迁移目录后旧 `node_modules` 内 pnpm junction 仍指向旧 `frontend/`，已清理并在 `apps/admin-web/` 下离线重建依赖。
- 第 7 步新增 pnpm 构建审批文件：`apps/admin-web/pnpm-workspace.yaml`，用于允许 `esbuild`、`@parcel/watcher`、`vue-demi` 执行安装脚本。
- 当前已完成第 8 步：新增统一启动、构建和全量验证脚本。
- 第 8 步统一脚本验证：`scripts/build-api.ps1`、`scripts/build-web.ps1`、`scripts/verify-all.ps1` 均通过。
- 当前已完成第 9 步：新增结构迁移交接文档 `docs/context/enterprise-structure-handoff.md` 和脚本说明 `scripts/README.md`。
- 当前已完成第 10 步：已执行最终全量验证，`scripts/verify-all.ps1` 通过。
- 本轮新增一键启动入口：`start-all.bat` 和 `scripts/dev-all.ps1`，用于双击后分别打开后端、前端启动窗口。
- 一键启动脚本验证：`scripts/dev-all.ps1 -DryRun` 通过，所有 `scripts/*.ps1` 语法检查通过。
- 本轮重绘当前项目结构图：`docs/project-structure-diagram-bw.svg`，采用黑白结构总览图，重点表达一键启动入口、`apps` 前后端应用区、`docs` 文档规范区、`.ai-context` Vibe Coding 协作上下文和 `scripts` 验证闭环。
- 后续工程结构变更必须保持后端编译和前端构建均可通过，任何一步失败都停止。

### 2.1.1 Vibe Coding 企业级上下文结构

当前已新增：

```text
.ai-context/
├── shared/
│   └── feature-change-workflow.md # 新增/修改功能时的 Vibe Coding Harness 流程
├── ai-project-manager/
├── ai-frontend/
├── ai-backend/
├── ai-test/
└── ai-deploy/

docs/
├── context/
│   ├── project-baseline.md
│   ├── enterprise-structure-handoff.md
│   └── requirements/
│       └── database-design.md
├── standards/
├── modules/
│   ├── base-platform/
│   └── system-management/
└── tech-sharing/
```

角色说明：

- `ai-project-manager`：需求、范围、计划、验收和文档维护。
- `ai-frontend`：Vue 管理端、API 类型、路由、状态、页面体验。
- `ai-backend`：Spring Boot API、数据库、权限、服务逻辑和审计。
- `ai-test`：接口冒烟、权限验收、回归清单。
- `ai-deploy`：启动、构建、环境变量和后续结构迁移脚本。

每个角色目录均包含独立 `compact.md`，便于后续 Vibe Coding 接力。

新增或修改功能时，用户只需要在当前 Codex 主对话提出需求，不需要分别创建多个对话或手动给不同 AI 角色分配任务。当前执行流程已固化到 `.ai-context/shared/feature-change-workflow.md`：先由项目经理视角确认需求边界、风险和验收标准，再按后端、前端、测试、部署视角实施和验证，最后统一更新 `PROJECT_SPEC.md`、`docs/` 和 `.ai-context/`，并剔除过时内容。

### 2.2 后端环境

要求：

- JDK 17
- Maven 3.8+
- MySQL 8.x

当前环境检测结果：

- 已检测到 JDK 17 路径：`C:\Program Files\Java\jdk-17`
- 用户已安装 Maven：`E:\maven\apache-maven-3.9.16`
- 系统默认 `java` 命令指向损坏的 Oracle javapath，需要修复 PATH 或显式设置 `JAVA_HOME`
- 当前可通过临时设置 `MAVEN_HOME=E:\maven\apache-maven-3.9.16` 使用 Maven；用户本机后端已成功运行过
- 用户已在 PowerShell 中设置 `JAVA_HOME` 后执行 `mvn spring-boot:run`，实际报错为“无法将 mvn 项识别为 cmdlet、函数、脚本文件或可运行程序的名称”，确认当前阻塞点是 Maven 未安装或未配置 PATH
- 用户在 IDE 中运行 `DiagnosisPlatformApplication` 时出现 `java: 错误: 不支持发行版本 5`，判断为 IDE/Maven 编译级别未使用 Java 17，已在 `apps/api-server/pom.xml` 显式配置 `maven-compiler-plugin` 的 `<release>17</release>`
- 用户编译 `MybatisPlusConfig.java` 时出现 `找不到符号 PaginationInnerInterceptor`，判断为 MyBatis-Plus 3.5.9 分页插件依赖拆分导致，已在 `apps/api-server/pom.xml` 补充 `mybatis-plus-jsqlparser` 依赖

数据库初始化：

```sql
CREATE DATABASE xc_diagnosis DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

数据库边界：

- `xc_diagnosis` 是平台自身的业务数据库，用于存放本系统的用户、权限、数据台账、指标采样、日志记录、告警、AI 任务、知识库、执行记录等平台数据。
- 一期默认只使用这一个平台数据库。
- “分布式数据库资源”模块中登记的数据库是被监控、被诊断、被优化的外部业务数据库实例；它们作为台账和后续连接目标存在，不等同于平台自身数据库。
- 后续如需要采集外部数据库指标或日志，应通过连接配置、采集任务或代理服务写入 `xc_diagnosis` 中的采样/日志/告警表，不建议把平台元数据分散存放到多个库。

后端默认配置：

```yaml
server:
  port: 8080
  servlet:
    context-path: /api
```

数据库连接可通过环境变量覆盖：

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
```

本地敏感配置约定：

- 后端 `application.yml` 不保留数据库密码默认值，统一从 `DB_PASSWORD` 环境变量读取。
- Windows 本地开发可在 `deploy/windows/.env` 配置数据库连接信息；`scripts/dev-api.ps1` 会自动加载该文件。
- `deploy/windows/.env` 已加入 `.gitignore`，不得提交到远程仓库。

后端启动命令：

```powershell
cd "E:\vibe work\springboot work\apps\api-server"
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run
```

如果提示 `mvn` 无法识别，需要先安装 Maven 或配置 Maven：

```powershell
mvn -version
```

当前可用的临时 Maven 环境命令：

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
$env:MAVEN_HOME="E:\maven\apache-maven-3.9.16"
$env:Path="$env:MAVEN_HOME\bin;$env:JAVA_HOME\bin;$env:Path"
mvn -version
```

若该命令不可用，任选一种方式处理：

- 安装 Apache Maven 3.8+，并把 Maven 的 `bin` 目录加入系统 PATH。
- 如果本机已有 Maven，例如 `D:\apache-maven-3.9.9`，临时执行：

```powershell
$env:MAVEN_HOME="D:\apache-maven-3.9.9"
$env:Path="$env:MAVEN_HOME\bin;$env:JAVA_HOME\bin;$env:Path"
mvn -version
mvn spring-boot:run
```

- 使用 IntelliJ IDEA / Eclipse / Spring Tools 的内置 Maven 打开 `apps/api-server/pom.xml` 后运行 Spring Boot 启动类。

健康检查：

```bash
curl http://localhost:8080/api/health
```

### 2.3 前端环境

要求：

- Node.js 18+
- pnpm

当前环境说明：

- 系统 PATH 未检测到全局 `node` / `npm`
- Codex 内置 Node 可用：`C:\Users\ASUS\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe`
- Codex 内置 pnpm 可用路径可能因运行时版本不同而变化，当前检测到可用路径：`C:\Users\ASUS\.cache\codex-runtimes\codex-primary-runtime\dependencies\bin\fallback\pnpm.cmd`
- 用户在 PowerShell 中直接执行 `pnpm dev` 时出现“无法将 pnpm 项识别为 cmdlet、函数、脚本文件或可运行程序的名称”，确认当前阻塞点是 pnpm 未安装或未配置 PATH
- 已更新 `apps/admin-web/run-dev.ps1`，优先使用 Codex 内置 `pnpm.cmd`，并兼容 `bin\fallback\pnpm.cmd` 启动前端
- 用户已确认前端成功运行

前端启动命令：

```powershell
cd "E:\vibe work\springboot work\apps\admin-web"
pnpm dev
```

如果系统 PATH 没有 Node，可先临时加入 Codex 内置 Node：

```powershell
$env:Path="C:\Users\ASUS\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin;$env:Path"
pnpm dev
```

如果系统 PATH 没有 pnpm，可直接运行：

```powershell
cd "E:\vibe work\springboot work\apps\admin-web"
.\run-dev.ps1
```

也可以直接双击项目根目录下的脚本启动前端：

```text
E:\vibe work\springboot work\start-frontend.bat
```

当前已新增一键启动整个平台的脚本：

```text
E:\vibe work\springboot work\start-all.bat
```

双击后会分别打开后端和前端 PowerShell 窗口：

- 后端：`scripts/dev-api.ps1`
- 前端：`scripts/dev-web.ps1`

当前前端开发服务器已成功启动过：

```text
http://localhost:5173
```

前端构建验证已通过：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-web.ps1
```

项目级全量验证已通过：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verify-all.ps1
```

## 3. 数据库模块（数据库变更必读）

> 维护规则：以后任何涉及数据库表、字段、索引、枚举、初始化数据、实体映射、接口入参出参的变更，都必须先阅读 `docs/数据库设计说明书_v1(1).docx`、`docs/context/requirements/database-design.md`、本模块和 `docs/standards/database-standard.md`，再修改数据库；变更完成后必须同步更新本模块，剔除过时内容，保证这里始终是项目数据库设计的最新准绳。

### 3.1 数据库总体口径

当前项目数据库设计由三层准绳共同维护：

- 企业级数据库设计蓝图：`docs/数据库设计说明书_v1(1).docx`。
- 可检索摘要和执行约束：`docs/context/requirements/database-design.md`。
- 当前已落地数据库：`apps/api-server/src/main/resources/schema.sql`、`data.sql`、后端实体和已运行功能。

数据库设计原则：

- 后续新增或修改数据库能力必须优先对齐 `docs/数据库设计说明书_v1(1).docx`，不得只按临时页面字段随意建表。
- 已经在代码中真实落地并通过验证的表，不为了追求文档命名一致而强行改名。
- Word 设计说明书作为完整企业版蓝图，用于指导后续扩展。
- 当前项目已多出的功能和表必须保留，尤其是登录认证、用户管理、角色管理、菜单权限树、按钮权限、动态菜单和后端权限校验。
- 后续新增表和字段优先使用数据库设计说明书中的表名、字段名、关系和字典编码。
- 如果 Word 文档中的表和当前项目表含义一致但名称不同，必须在本模块记录映射关系和兼容原因；不得继续扩大命名差异。
- 如果说明书内部存在重复表述或字段名差异，必须先在计划中说明并让用户确认，不能静默选择。
- 不建议一次性替换现有数据库结构，应采用兼容迁移：新增字段、新增关联表、保留旧字段过渡、代码稳定后再考虑清理冗余字段。
- 本轮已重新学习并沉淀数据库设计说明书：新增 `docs/context/requirements/database-design.md`，更新 `docs/standards/database-standard.md`、`.ai-context/shared/change-rules.md`、`.ai-context/shared/source-map.md`、`.ai-context/ai-backend/compact.md` 和 `.ai-context/ai-backend/workflow.md`，后续后端和数据库开发必须严格执行。

### 3.2 当前已落地数据库

当前项目已经落地并被功能使用的表如下：

| 当前表名 | 业务含义 | 当前状态 | 对应 Word 设计 |
| --- | --- | --- | --- |
| `ledger_db_resource` | 分布式数据库资源台账 | 已实现 CRUD，数据大屏统计使用 | `db_instance` |
| `ledger_business_system` | 业务系统台账 | 已实现 CRUD，数据大屏统计使用 | `bus_system` |
| `ledger_business_system_db` | 业务系统与数据库资源关联 | 已实现业务系统关联多个数据库 | `bus_system_db` |
| `sys_user` | 系统用户 | 已实现登录、用户管理、禁用、重置密码、头像字段、个人资料自助修改 | `sys_user` |
| `sys_role` | 系统角色 | 已实现角色管理、启用禁用、角色分配、数据权限范围字段 | `sys_role` |
| `sys_user_role` | 用户角色关系 | 已实现用户分配角色 | `sys_user_role` |
| `sys_menu` | 菜单、页面、按钮权限 | 已实现菜单权限树、动态菜单、按钮权限 | `sys_menu_permission` |
| `sys_role_menu` | 角色菜单/按钮权限关系 | 已实现角色分配菜单权限 | `sys_role_menu` |
| `login_log` | 登录日志 | 已实现登录成功/失败记录和分页查询 | `login_log` |
| `operation_log` | 操作日志 | 已实现核心模块成功操作记录和分页查询 | `operation_log` |
| `sys_dict_type` | 字典类型 | 已实现 CRUD、状态管理、初始化数据 | `sys_dict_type` |
| `sys_dict_item` | 字典项 | 已实现 CRUD、状态管理、启用项查询、初始化数据 | `sys_dict_item` |
| `sys_config` | 系统配置 | 已实现 CRUD、状态管理、按配置键查询、初始化数据 | `sys_config` |
| `system_notification` | 系统通知 | 已实现个人通知查询、未读数、已读、删除和真实通知生成 | 说明书扩展表 |

当前表设计特点：

- 所有主键统一使用 `BIGINT`，后端采用 MyBatis-Plus 雪花 ID。
- 后端已经配置 Long 转 String，避免前端 JavaScript 精度丢失。
- 通用字段采用：`created_at`、`updated_at`、`created_by`、`updated_by`、`deleted`。
- 启用/禁用类状态当前使用字符串枚举，例如 `ENABLED`、`DISABLED`。
- 健康状态当前使用字符串枚举：`NORMAL`、`WARNING`、`DANGER`、`OFFLINE`。
- 当前没有强制建立真实外键，主要通过业务逻辑维护关联，便于演示环境和后续兼容迁移。

### 3.3 当前表结构要点

#### 3.3.1 `ledger_db_resource`

用途：保存分布式数据库或被监控数据库实例的基础台账。

当前核心字段：

- `id`：数据库资源 ID。
- `name`：数据库名称。
- `public_ip`：公网 IP。
- `private_ip`：内网 IP。
- `owner_name`：负责人名称。
- `status`：健康状态，当前取值为 `NORMAL`、`WARNING`、`DANGER`、`OFFLINE`。
- `created_at`、`updated_at`、`created_by`、`updated_by`、`deleted`：通用审计字段。

当前索引：

- `idx_db_resource_name`
- `idx_db_resource_owner`
- `idx_db_resource_status`
- `idx_db_resource_deleted`

后续建议增强字段：

- `db_code`：数据库编码，建议唯一。
- `db_type`：数据库类型，如 MySQL、PostgreSQL、Oracle、达梦、人大金仓等。
- `db_version`：数据库版本。
- `host`：主机地址或域名。
- `port`：端口。
- `connection_url`：连接串或服务名，不保存明文账号密码。
- `environment`：环境，建议取值 `DEV`、`TEST`、`UAT`、`PROD`。
- `remark`：备注。

注意：数据库账号、密码等敏感凭据不要直接放入该表，后续如要连接真实数据库，应单独设计加密凭据表或接入配置中心/密钥管理。

#### 3.3.2 `ledger_business_system`

用途：保存业务系统台账。

当前核心字段：

- `id`：业务系统 ID。
- `name`：业务系统名称。
- `database_id`：默认/主数据库资源 ID，用于兼容早期单关联数据和大屏统计。
- `owner_name`：负责人名称。
- `status`：健康状态，当前取值为 `NORMAL`、`WARNING`、`DANGER`、`OFFLINE`。
- `access_url`：访问地址。
- `remark`：备注。
- `created_at`、`updated_at`、`created_by`、`updated_by`、`deleted`：通用审计字段。

当前索引：

- `idx_business_name`
- `idx_business_owner`
- `idx_business_status`
- `idx_business_database`
- `idx_business_deleted`

后续建议增强字段：

- `system_code`：业务系统编码，建议唯一。
- `owner_user_id`：负责人用户 ID。
- `dept_id`：所属部门 ID。
- `enabled` 或继续沿用 `status` 区分启停与健康状态时需谨慎，避免语义混乱。

当前关联规则：

- Word 文档建议业务系统与数据库实例为多对多关系。
- 当前项目已新增 `ledger_business_system_db` 作为真实多数据库关联表。
- 新增/编辑业务系统时，前端可选择一个或多个分布式数据库。
- 后端保存时会把第一个数据库同步写入 `ledger_business_system.database_id`，作为默认/主数据库兼容字段。
- 后端更新业务系统关联数据库时，会先物理删除该业务系统旧关联记录，再插入最新关联，避免逻辑删除记录与唯一索引产生冲突。
- 查询详情和列表时优先读取 `ledger_business_system_db`，如果旧数据没有关联表记录，则回退读取 `database_id`。
- 删除数据库资源时会同时检查 `ledger_business_system.database_id` 和 `ledger_business_system_db.db_resource_id`，已被业务系统关联则禁止删除。

#### 3.3.2.1 `ledger_business_system_db`

用途：保存业务系统和分布式数据库资源的多对多关联。

当前核心字段：

- `id`：关联记录 ID。
- `business_system_id`：业务系统 ID。
- `db_resource_id`：数据库资源 ID。
- `relation_type`：关系类型，当前保存逻辑为第一个数据库写 `PRIMARY`，其余数据库写 `STANDBY`；预留 `LOG`、`ANALYSIS` 等扩展类型。
- `created_at`、`updated_at`、`created_by`、`updated_by`、`deleted`：通用审计字段。

当前索引与约束：

- `uk_business_system_db_relation`：`business_system_id + db_resource_id + relation_type + deleted`。
- `idx_business_system_db_system`
- `idx_business_system_db_resource`
- `idx_business_system_db_deleted`

后续建议：

- 若需要在前端显式维护主库、备库、日志库、分析库，应把 `relation_type` 做成可编辑字段，并增加排序字段 `sort_order`。
- 当前不建议删除 `ledger_business_system.database_id`，它仍承担兼容默认数据库字段职责。

#### 3.3.3 `sys_user`

用途：保存平台用户账号、登录信息和用户管理数据。

当前核心字段：

- `id`
- `username`
- `password_hash`
- `real_name`
- `avatar_url`：头像地址，支持 http(s) 图片地址或 `data:image` base64。
- `department`
- `phone`
- `email`
- `status`：`ENABLED`、`DISABLED`
- `remark`
- 通用审计字段

当前能力：

- BCrypt 密码存储。
- 默认管理员 `admin / password` 初始化。
- 用户分页、搜索、新增、编辑、启用禁用、重置密码、删除。
- 用户管理支持维护头像地址。
- 个人中心支持当前登录用户查看和自助修改个人资料。
- 个人中心支持当前登录用户校验原密码后自助修改密码。
- 登录时校验禁用用户不允许登录。

后续建议增强字段：

- `dept_id`：所属部门，替代或补充当前文本型 `department`。
- `password_updated_at`：密码更新时间。
- `last_login_at`：最近登录时间。
- `failed_login_count`：连续登录失败次数。
- `locked_until`：锁定截止时间。

#### 3.3.4 `sys_role`

用途：保存系统角色。

当前核心字段：

- `id`
- `role_code`
- `role_name`
- `data_scope`：数据权限范围，当前取值 `ALL`、`DEPARTMENT`、`SELF`、`CUSTOM`。
- `data_scope_config`：自定义数据权限配置，仅 `CUSTOM` 时保留；非 `CUSTOM` 保存时置空。
- `status`：`ENABLED`、`DISABLED`
- `remark`
- 通用审计字段

当前预置角色：

- `admin`：系统管理员。
- `operator`：运维操作员。
- `viewer`：只读查看员。

后续建议：

- 保留当前 `status` 字段，不改成 Word 文档中的 `enabled`，因为当前前后端都已按状态枚举实现。
- 可新增 `role_desc` 或继续复用 `remark`。
- 当前已按需求文档在 `sys_role` 上落地角色级数据权限字段，用于管理界面配置和权限边界记录。
- 后续如需要对部门、业务系统、数据库资源做多目标精细授权，可在不破坏现有字段的前提下再扩展 `sys_role_data_scope` 明细表。

#### 3.3.5 `sys_user_role`

用途：保存用户与角色的多对多关系。

当前核心字段：

- `id`
- `user_id`
- `role_id`
- 通用审计字段

当前设计与 Word 文档略有差异：

- Word 文档建议使用联合主键 `(user_id, role_id)`。
- 当前项目使用独立雪花 ID，并通过索引辅助查询。
- 建议保留当前独立 ID 风格，后续可补充唯一约束或业务层去重。

#### 3.3.6 `sys_menu`

用途：保存目录、菜单、页面和按钮权限，用于左侧菜单渲染、按钮显示和后端接口鉴权。

当前核心字段：

- `id`
- `parent_id`
- `menu_type`：当前取值 `MENU`、`BUTTON`
- `menu_name`
- `permission_code`
- `route_path`
- `component_path`
- `icon`
- `sort_order`
- `visible`
- `status`
- `remark`
- 通用审计字段

当前能力：

- 菜单权限树。
- 动态左侧菜单。
- 按钮权限控制。
- 后端接口通过 `permission_code` 校验。
- admin 角色拥有超级管理员兜底能力。

与 Word 文档映射：

- 当前 `sys_menu` 等价于 Word 文档中的 `sys_menu_permission`。
- 不建议改名为 `sys_menu_permission`，因为当前代码、接口、前端页面已经使用 `sys_menu`。

后续建议增强字段：

- `menu_type` 扩展为 `DIR`、`MENU`、`BUTTON`、`API`。
- `api_path`：接口路径。
- `http_method`：请求方法，如 GET、POST、PUT、PATCH、DELETE。
- 如后续实现 API 权限可视化管理，应优先扩展当前表，而不是新增重复权限表。

#### 3.3.7 `sys_role_menu`

用途：保存角色拥有的菜单、按钮和接口权限。

当前核心字段：

- `id`
- `role_id`
- `menu_id`
- 通用审计字段

当前能力：

- 角色管理页面可分配菜单和按钮权限。
- 保存角色权限时会自动补齐父级菜单。
- 获取用户菜单树时会自动补齐祖先节点，避免菜单树断裂。

后续建议：

- 可补充唯一约束或业务层继续保证 `(role_id, menu_id, deleted)` 不重复。
- 如果后续增加 API 权限类型，本表无需变化。

#### 3.3.8 `login_log`

用途：记录用户登录成功和失败情况，支撑安全审计。

当前核心字段：

- `id`
- `user_id`：用户 ID，账号不存在时为空。
- `username`：登录账号。
- `login_time`：登录时间。
- `login_ip`：登录 IP。
- `device_info`：浏览器/客户端信息。
- `login_status`：`SUCCESS`、`FAILED`。
- `failure_reason`：失败原因。
- 通用审计字段。

当前写入范围：

- 登录成功记录 `SUCCESS`。
- 账号不存在记录 `FAILED`，失败原因为“账号不存在”。
- 密码错误记录 `FAILED`，失败原因为“密码错误”。
- 用户禁用记录 `FAILED`，失败原因为“用户已禁用”。

当前查询能力：

- `GET /api/system/logs/login`
- 支持按账号、登录状态、开始时间、结束时间分页查询。

#### 3.3.9 `operation_log`

用途：记录用户在系统内的关键操作，支撑审计追踪。

当前核心字段：

- `id`
- `operator_id`：操作人 ID。
- `operator_name_snapshot`：操作人姓名快照。
- `module_name`：模块名称。
- `operation_type`：操作类型。
- `target_type`：操作对象类型。
- `target_id`：操作对象 ID。
- `operation_content`：操作内容摘要。
- `operation_result`：`SUCCESS`、`FAILED`。
- `error_message`：失败原因，当前预留。
- `request_ip`：请求 IP。
- `user_agent`：客户端信息。
- 通用审计字段。

当前写入范围：

- 数据台账：业务系统新增、编辑、删除；分布式数据库新增、编辑、删除。
- 用户管理：新增、编辑、启用/禁用、重置密码、删除。
- 角色管理：新增、编辑、启用/禁用、删除、分配菜单权限。
- 菜单权限管理：新增、编辑、启用/禁用、删除。

当前查询能力：

- `GET /api/system/logs/operation`
- 支持按操作人、模块、操作类型、操作结果、开始时间、结束时间分页查询。

当前限制：

- 本阶段先记录成功操作。
- 失败操作、参数脱敏、全量 AOP 审计可在后续增强。

#### 3.3.10 `sys_dict_type`

用途：维护系统字典类型，例如健康状态、启用状态、登录状态、操作类型和 AI 任务类型。

当前核心字段：

- `id`
- `dict_code`：字典编码，唯一。
- `dict_name`：字典名称。
- `status`：`ENABLED`、`DISABLED`。
- `remark`
- 通用审计字段。

当前能力：

- 分页查询。
- 新增、编辑、启用/禁用、删除。
- 字典编码唯一校验。
- 删除存在字典项的字典类型时会提示不能删除。
- 操作写入 `operation_log`。

#### 3.3.11 `sys_dict_item`

用途：维护具体字典项，用于前端状态标签、下拉选项和后续业务配置。

当前核心字段：

- `id`
- `dict_code`：所属字典编码。
- `item_value`：字典项值。
- `item_label`：显示名称。
- `tag_type`：前端标签类型。
- `sort_order`：排序。
- `status`：`ENABLED`、`DISABLED`。
- `remark`
- 通用审计字段。

当前能力：

- 按字典编码分页查询。
- 新增、编辑、启用/禁用、删除。
- 查询指定字典编码下的启用字典项：`GET /api/system/dict/items/enabled/{dictCode}`。
- 同一字典编码下字典项值唯一。
- 操作写入 `operation_log`。

当前初始化字典：

- `HEALTH_STATUS`
- `ENABLE_STATUS`
- `LOGIN_STATUS`
- `OPERATION_TYPE`
- `TASK_TYPE`

#### 3.3.12 `sys_config`

用途：维护系统级参数配置，例如系统名称、Token 有效期、数据大屏刷新秒数、默认密码等。

当前核心字段：

- `id`
- `config_key`：配置键，唯一。
- `config_name`：配置名称。
- `config_value`：配置值。
- `config_type`：`TEXT`、`NUMBER`、`BOOLEAN`、`JSON`。
- `status`：`ENABLED`、`DISABLED`。
- `remark`
- 通用审计字段。

当前能力：

- 分页查询。
- 新增、编辑、启用/禁用、删除。
- 按配置键查询：`GET /api/system/configs/key/{configKey}`。
- 配置键唯一校验。
- 操作写入 `operation_log`。

当前初始化配置：

- `SYSTEM_NAME`
- `TOKEN_EXPIRE_SECONDS`
- `DASHBOARD_REFRESH_SECONDS`
- `DEFAULT_PASSWORD`

当前限制：

- 本阶段先提供管理能力，尚未强制替换现有业务中的静态配置读取逻辑。
- 后续如果让 Token 有效期、默认密码等配置动态生效，需要逐项评估安全和缓存策略。

#### 3.3.13 `system_notification`

用途：保存站内通知，支撑消息通知页面和顶部铃铛未读数量。

当前核心字段：

- `id`
- `receiver_id`：接收用户 ID。
- `receiver_name_snapshot`：接收人姓名快照。
- `title`：通知标题。
- `content`：通知内容。
- `notice_type`：`SYSTEM`、`ALERT`、`TASK`、`SECURITY`。
- `priority`：`LOW`、`NORMAL`、`HIGH`。
- `read_status`：`UNREAD`、`READ`。
- `read_at`：阅读时间。
- `business_type`：关联业务类型。
- `business_id`：关联业务 ID。
- 通用审计字段。

当前索引：

- `idx_notification_receiver_read`
- `idx_notification_receiver_time`
- `idx_notification_type`
- `idx_notification_priority`
- `idx_notification_business`
- `idx_notification_deleted`

当前能力：

- 当前登录用户分页查询自己的通知。
- 查询当前登录用户未读数量。
- 查看通知详情。
- 单条标记已读。
- 全部标记已读。
- 删除单条通知。
- 清空已读通知。
- 通知操作写入 `operation_log`。

当前通知生成范围：

- 后端启动时为 admin 初始化演示通知，若 admin 已有通知则不重复生成。
- 业务系统状态变为 `WARNING`、`DANGER`、`OFFLINE` 时，给管理员生成告警通知。
- 分布式数据库状态变为 `WARNING`、`DANGER`、`OFFLINE` 时，给管理员生成告警通知。
- 用户被禁用时，给被操作用户生成安全通知。
- 用户密码被重置时，给被操作用户生成安全通知。

当前字典扩展：

- `NOTICE_TYPE`
- `NOTICE_PRIORITY`
- `NOTICE_READ_STATUS`
- `OPERATION_TYPE` 已追加 `READ`、`READ_ALL`、`CLEAR_READ`

当前限制：

- 本阶段为站内通知，不包含 WebSocket/SSE 实时推送。
- 通知中心只展示当前登录用户自己的通知，不提供后台全量通知管理。
- 登录失败暂不主动通知用户，避免账号枚举和攻击噪音。

### 3.4 Word 设计说明书中的完整数据库蓝图

Word 设计中的主业务链路：

```text
数据台账
→ 指标采集
→ AI任务配置
→ 定时执行
→ 生成故障预警 / 缺陷报表 / 优化建议
→ 知识库检索或沉淀
→ 人工确认 / 标记误报 / 关闭
→ 数据大屏展示
```

Word 设计中的模块划分：

| 模块 | 对应 UI | 设计表 |
| --- | --- | --- |
| 用户权限模块 | 用户账号管理、角色权限管理、个人中心 | `sys_dept`、`sys_user`、`sys_role`、`sys_user_role`、`sys_menu_permission`、`sys_role_menu`、`sys_role_data_scope` |
| 数据台账模块 | 数据可视化台账、业务系统/分布式数据库新增编辑 | `bus_system`、`db_instance`、`bus_system_db` |
| AI任务模块 | 故障预测任务配置、缺陷检测任务配置、性能优化任务配置 | `ai_model`、`ai_task`、`task_exec_record` |
| 知识库模块 | 故障知识库、缺陷知识库、优化知识库 | `kb_instance` |
| 结果管理模块 | 故障智能预警、缺陷检测报表、性能优化建议及详情页 | `fault_report`、`defect_report`、`opt_report` |
| 审计与配置模块 | 日志审计管理、系统基础配置、安全登录管控 | `result_status_log`、`operation_log`、`login_log`、`report_export_log`、`sys_dict_type`、`sys_dict_item`、`sys_config`、`sys_login_policy` |

### 3.5 当前项目与 Word 表名映射

| Word 表名 | 当前项目建议 | 处理策略 |
| --- | --- | --- |
| `sys_dept` | `sys_dept` | 未实现，后续新增 |
| `sys_user` | `sys_user` | 已实现，已补头像和个人资料维护字段，后续可补部门 ID、登录安全字段 |
| `sys_role` | `sys_role` | 已实现，保留当前 `status` 设计，已补角色数据权限字段 |
| `sys_user_role` | `sys_user_role` | 已实现，保留独立 ID |
| `sys_menu_permission` | `sys_menu` | 已实现，不改名，后续增强字段 |
| `sys_role_menu` | `sys_role_menu` | 已实现，后续可补唯一约束 |
| `sys_role_data_scope` | `sys_role.data_scope` / `sys_role.data_scope_config` | 当前先用角色表字段承载数据权限范围；后续如做多目标细粒度授权再新增明细表 |
| `bus_system` | `ledger_business_system` | 已实现，不改名，后续补业务编码/部门/负责人用户 |
| `db_instance` | `ledger_db_resource` | 已实现，不改名，后续补数据库类型/版本/端口/环境 |
| `bus_system_db` | `ledger_business_system_db` | 已实现，当前按项目命名保留 |
| `ai_model` | `ai_model` | 已实现一期数据壳 |
| `ai_task` | `ai_task` | 已实现一期数据壳 |
| `task_exec_record` | `task_exec_record` | 已实现一期数据壳 |
| `kb_instance` | `kb_instance` | 已实现一期数据壳 |
| `result_knowledge_ref` | `result_knowledge_ref` | 已实现一期数据壳知识引用关系 |
| `fault_report` | `fault_report` | 已实现一期数据壳 |
| `fault_alert_metric_trend` | `fault_alert_metric_trend` | 未实现，后续新增 |
| `defect_report` | `defect_report` | 已实现一期数据壳 |
| `optimization_report` | `opt_report` | 已实现一期数据壳，采用 Word 前文的 `opt_report` 命名 |
| `result_status_log` | `result_status_log` | 已实现一期数据壳状态流转记录 |
| `operation_log` | `operation_log` | 已实现 |
| `login_log` | `login_log` | 已实现 |
| `report_export_log` | `report_export_log` | 未实现，后续新增 |
| `sys_dict_type` | `sys_dict_type` | 已实现 |
| `sys_dict_item` | `sys_dict_item` | 已实现 |
| `sys_config` | `sys_config` | 已实现 |
| `sys_login_policy` | `sys_login_policy` | 未实现，后续新增 |

### 3.6 后续建议新增表设计摘要

#### 3.6.1 组织与数据权限

`sys_dept`：部门表。

建议字段：

- `id` 或 `dept_id`
- `parent_id`
- `dept_code`
- `dept_name`
- `sort_order`
- `status` 或 `enabled`
- 通用审计字段

项目建议：

- 为保持当前项目风格，主键建议仍叫 `id`。
- 顶级部门建议 `parent_id = 0`，与当前菜单树风格一致；如果未来使用真实外键，则顶级部门可改为 NULL。

当前实现：角色数据权限先落在 `sys_role.data_scope` 和 `sys_role.data_scope_config`。

当前取值：

- `ALL`：全部数据。
- `DEPARTMENT`：本部门数据。
- `SELF`：本人数据。
- `CUSTOM`：自定义数据范围。

当前规则：

- 角色新增、编辑页面可配置数据权限。
- 后端 `RoleSaveRequest` 校验数据权限枚举只能为 `ALL`、`DEPARTMENT`、`SELF`、`CUSTOM`。
- 当 `data_scope = CUSTOM` 时，`data_scope_config` 必填。
- 当 `data_scope != CUSTOM` 时，保存时清空 `data_scope_config`，避免遗留配置造成权限边界误读。

后续扩展：如需要对部门、业务系统、数据库资源做多目标细粒度授权，再新增 `sys_role_data_scope` 角色数据权限明细表。

建议字段：

- `id`
- `role_id`
- `scope_type`：建议沿用当前已落地枚举 `ALL`、`DEPARTMENT`、`SELF`、`CUSTOM`
- `target_type`：`DEPT`、`BUSINESS_SYSTEM`、`DB_RESOURCE`
- `target_id`
- `scope_key`
- `target_name_snapshot`
- 通用审计字段

用途：

- 支撑角色只能看全局、部门、本人的业务系统或指定数据库资源。

#### 3.6.2 审计与安全

`operation_log`：操作日志。

建议字段：

- `id`
- `operator_id`
- `operator_name_snapshot`
- `module_name`
- `operation_type`
- `target_type`
- `target_id`
- `operation_content`
- `operation_result`
- `error_message`
- `request_ip`
- `user_agent`
- `created_at`

`login_log`：登录日志。

建议字段：

- `id`
- `user_id`
- `username`
- `login_time`
- `login_ip`
- `device_info`
- `login_status`
- `failure_reason`

说明：

- 登录失败时可能没有匹配到用户，所以 `user_id` 必须允许为空。

`report_export_log`：报告导出日志。

建议字段：

- `id`
- `report_type`
- `report_id`
- `file_name`
- `file_url`
- `export_status`
- `failure_reason`
- `exported_by`
- `exported_at`

`sys_login_policy`：安全登录策略。

建议字段：

- `id`
- `policy_key`
- `policy_value`
- `policy_name`
- `policy_desc`
- `status`
- `updated_by`
- 通用审计字段

#### 3.6.3 字典与配置

`sys_dict_type`：字典类型。

建议字段：

- `id`
- `dict_code`
- `dict_name`
- `status`
- 通用审计字段

`sys_dict_item`：字典项。

建议字段：

- `id`
- `dict_code`
- `item_value`
- `item_label`
- `sort_order`
- `status`
- 通用审计字段

`sys_config`：系统配置。

建议字段：

- `id`
- `config_key`
- `config_value`
- `config_name`
- `config_desc`
- `status`
- `updated_by`
- 通用审计字段

#### 3.6.4 台账多对多增强

`ledger_business_system_db`：业务系统与数据库资源关联表。

当前已落地字段：

- `id`
- `business_system_id`
- `db_resource_id`
- `relation_type`：`PRIMARY`、`STANDBY`、`LOG`、`ANALYSIS`
- 通用审计字段

当前已落地约束：

- 唯一约束：`business_system_id + db_resource_id + relation_type + deleted`
- 索引：`business_system_id`、`db_resource_id`

当前实现状态：

- 已新增关联表。
- 后端新增、编辑业务系统时会写入关联表。
- 前端业务系统表单已支持多选分布式数据库。
- 列表和详情已支持展示多个关联数据库。
- 存量 `ledger_business_system.database_id` 数据可在没有关联表记录时作为回退展示。
- 稳定后再决定是否增加一次性数据迁移脚本，把存量 `database_id` 批量写入关联表；当前代码已具备兼容读取能力。
- 关联表更新时采用物理删除旧关系再重建新关系；业务系统和数据库资源主表仍使用逻辑删除。

#### 3.6.5 AI任务与模型

`ai_model`：AI 模型表。

建议字段：

- `id`
- `model_code`
- `model_name`
- `model_type`
- `model_version`
- `provider`
- `description`
- `status`
- 通用审计字段

`ai_task`：AI 任务主表。

建议字段：

- `id`
- `task_code`
- `task_name`
- `task_type`：`FAULT_PREDICTION`、`DEFECT_DETECTION`、`PERFORMANCE_OPTIMIZATION`
- `model_id`
- `target_type`：`BUSINESS_SYSTEM`、`DB_RESOURCE`
- `target_id`
- `target_name_snapshot`
- `param_json`
- `schedule_rule`
- `status`
- `last_exec_time`
- `next_exec_time`
- 通用审计字段

`task_exec_record`：任务执行记录表。

建议字段：

- `id`
- `task_id`
- `task_type`
- `execution_status`：`RUNNING`、`SUCCESS`、`FAILED`
- `trigger_type`：`MANUAL`、`SCHEDULED`
- `start_time`
- `end_time`
- `input_start_time`
- `input_end_time`
- `result_count`
- `error_message`
- `created_at`

#### 3.6.6 知识库与结果

`kb_instance`：统一知识库。

建议字段：

- `id`
- `knowledge_code`
- `knowledge_name`
- `knowledge_type`：`FAULT`、`DEFECT`、`OPTIMIZATION`
- `knowledge_content`
- `source_type`：`MANUAL`、`RESULT_DEPOSIT`
- `source_result_type`
- `source_result_id`
- `status`
- `version`
- 通用审计字段

`result_knowledge_ref`：结果与知识引用关系。

建议字段：

- `id`
- `result_type`
- `result_id`
- `knowledge_id`
- `ref_type`：`REFERENCED`、`DEPOSITED`
- `relevance_score`
- `created_at`

`fault_report`：故障预警结果。

建议字段：

- `id`
- `report_code` 或 `alert_code`
- `task_id`
- `execution_id`
- `target_type`
- `target_id`
- `target_name_snapshot`
- `alert_time`
- `indicator_category`
- `fault_type`
- `risk_level`
- `expected_start_time`
- `expected_end_time`
- `confidence`
- `forecast_description`
- `handle_suggestion`
- `process_status`
- `handled_by`
- `handled_at`
- 通用审计字段

`fault_alert_metric_trend`：故障指标趋势快照。

建议字段：

- `id`
- `alert_id`
- `metric_code`
- `metric_value`
- `point_time`
- `created_at`

`defect_report`：缺陷检测结果。

建议字段：

- `id`
- `defect_code`
- `task_id`
- `execution_id`
- `task_name_snapshot`
- `target_type`
- `target_id`
- `target_name_snapshot`
- `defect_type`
- `severity_level`
- `defect_problem`
- `defect_data`
- `defect_description`
- `repair_suggestion`
- `process_status`
- `handled_by`
- `handled_at`
- 通用审计字段

`opt_report`：性能优化建议。

建议字段：

- `id`
- `report_code`
- `task_id`
- `execution_id`
- `task_name_snapshot`
- `target_type`
- `target_id`
- `target_name_snapshot`
- `optimization_type`
- `optimization_problem`
- `sql_text`
- `sql_fingerprint`
- `execution_plan_json`
- `slow_sql_metrics_json`
- `optimization_content`
- `confidence`
- `process_status`
- `executed_sql`
- `executed_by`
- `executed_at`
- `execute_result`
- `before_metrics_json`
- `after_metrics_json`
- `handled_by`
- `handled_at`
- 通用审计字段

`result_status_log`：结果状态流转日志。

建议字段：

- `id`
- `business_type`：`FAULT_REPORT`、`DEFECT_REPORT`、`OPT_REPORT`
- `business_id`
- `old_status`
- `new_status`
- `operator_id`
- `operator_name_snapshot`
- `remark`
- `created_at`

### 3.7 字典枚举建议

后续业务表建议保存编码，前端或服务端通过字典项转换中文展示。

| 字典编码 | 字典名称 | 建议取值 |
| --- | --- | --- |
| `TASK_TYPE` | 任务类型 | `FAULT_PREDICTION`、`DEFECT_DETECTION`、`PERFORMANCE_OPTIMIZATION` |
| `PROCESS_STATUS` | 处理状态 | `PENDING`、`CONFIRMED`、`FALSE_ALARM`、`EXECUTED`、`CLOSED` |
| `KNOWLEDGE_TYPE` | 知识类型 | `FAULT`、`DEFECT`、`OPTIMIZATION` |
| `AI_MODEL_TYPE` | AI模型类型 | `FAULT`、`DEFECT`、`OPTIMIZATION`、`DEEP_LLM`、`LIGHTWEIGHT`、`SQL_OPTIMIZATION` |
| `HEALTH_STATUS` | 健康状态 | `NORMAL`、`WARNING`、`DANGER`、`OFFLINE` |
| `EXECUTION_STATUS` | 执行状态 | `RUNNING`、`SUCCESS`、`FAILED` |
| `TRIGGER_TYPE` | 任务触发方式 | `MANUAL`、`SCHEDULED` |
| `DATA_SCOPE_TYPE` | 数据权限范围 | `ALL`、`DEPARTMENT`、`SELF`、`CUSTOM` |
| `ENVIRONMENT` | 运行环境 | `DEV`、`TEST`、`UAT`、`PROD` |

当前项目已使用的状态：

- 账号/角色/菜单启用状态：`ENABLED`、`DISABLED`
- 台账健康状态：`NORMAL`、`WARNING`、`DANGER`、`OFFLINE`
- 角色数据权限范围：`ALL`、`DEPARTMENT`、`SELF`、`CUSTOM`

### 3.8 数据库演进优先级

建议后续按以下顺序推进数据库：

1. 日志审计与安全：`operation_log`、`login_log` 已完成；后续继续补 `sys_login_policy`
2. 字典与配置：`sys_dict_type`、`sys_dict_item`、`sys_config` 已完成
3. 组织与数据权限：角色数据权限字段已完成；后续补 `sys_dept` 和可选的 `sys_role_data_scope` 明细表
4. 台账增强：`ledger_business_system_db`，支持业务系统和数据库多对多
5. AI任务基础：`ai_model`、`ai_task`、`task_exec_record` 已完成一期数据壳，后续增强真实执行和调度能力
6. 知识库：`kb_instance`、`result_knowledge_ref` 已完成一期数据壳，后续增强检索和沉淀规则
7. 结果管理：`fault_report`、`defect_report`、`opt_report`、`result_status_log` 已完成一期数据壳，后续增强真实诊断结果来源
8. 指标采集与趋势：后续补指标定义、指标采样、故障指标趋势快照

每次数据库变更验收要求：

- 更新 `schema.sql`。
- 如涉及默认数据，更新 `data.sql` 或启动初始化器。
- 新增/修改后端实体、Mapper、DTO、VO、Service、Controller。
- 更新权限菜单和按钮权限，必要时补充 `sys_menu` 初始化。
- 更新前端 API 类型和页面表单。
- 运行后端编译：`mvn -DskipTests compile`。
- 如涉及前端，运行前端构建：`pnpm build`。
- 更新本数据库模块，删除过时描述。

## 4. 已经完成内容

### 4.1 Figma 原型读取与范围整理

已读取 Figma Make 原型源码和设计文档，确认系统为 PC 后台管理应用。

已整理出的一级模块：

- 数据台账
- 数据大屏
- AI故障预测
- AI缺陷检测
- AI性能优化
- 系统管理
- 登录、个人中心、消息通知

已确认一期范围：

- 完整实现项目基础框架
- 完成核心模块的页面、数据结构、接口壳
- AI 三大模块只实现界面和数据闭环，不实现真实 AI 能力

### 4.1.1 Vibe Coding 企业级上下文基座

已完成第 0 到第 4 步：

第 0 步：基线冻结。

- 已读取当前 `PROJECT_SPEC.md`。
- 已读取 Word 需求文档并提取关键需求。
- 已记录当前项目结构到 `docs/context/project-baseline.md`。
- 后端基线编译通过：`mvn -DskipTests compile`。
- 前端基线构建通过：`pnpm build`。
- 已记录前端构建中的非阻断 Sass/Rollup/chunk 警告。

第 1 步：AI 角色上下文。

- 已新增 `.ai-context/shared`。
- 已新增 `.ai-context/ai-project-manager`。
- 已新增 `.ai-context/ai-frontend`。
- 已新增 `.ai-context/ai-backend`。
- 已新增 `.ai-context/ai-test`。
- 已新增 `.ai-context/ai-deploy`。
- 每个角色已包含 `compact.md`、`responsibility.md`、`workflow.md`、`handoff-checklist.md`。

第 2 步：需求上下文化。

- 已新增 `docs/context/requirements/index.md`。
- 已新增 `base-platform.md`、`system-management.md`、`data-ledger.md`、`ai-modules.md`、`non-functional.md`。
- 已按“已实现 / 部分实现 / 未实现 / 暂不实现”记录需求状态。

第 3 步：代码规范上下文化。

- 已新增后端编码规范。
- 已新增前端编码规范。
- 已新增 API 契约规范。
- 已新增数据库规范。
- 已新增权限规范。
- 已新增测试规范。
- 已新增文档维护规范。

第 4 步：模块说明沉淀。

- 已新增 `docs/modules/base-platform`，覆盖认证、权限、审计日志、字典配置、通知。
- 已新增 `docs/modules/system-management`，覆盖用户管理、角色管理、菜单管理、个人中心。
- 已新增 `docs/tech-sharing/README.md`，作为后续技术分享沉淀入口。

当前边界：

- 已完成后端目录迁移：`backend/` -> `apps/api-server/`。
- 已完成前端目录迁移：`frontend/` -> `apps/admin-web/`。
- 已更新前后端相关文档路径；前端启动脚本已指向 `apps/admin-web/`。
- 已重建 `apps/admin-web/node_modules`，避免 pnpm junction 继续指向旧 `frontend/` 路径。
- 已新增统一脚本：
  - `scripts/dev-all.ps1`
  - `scripts/dev-api.ps1`
  - `scripts/build-api.ps1`
  - `scripts/dev-web.ps1`
  - `scripts/build-web.ps1`
  - `scripts/verify-all.ps1`
- 已将 `start-frontend.bat` 改为调用 `scripts/dev-web.ps1`。
- 已新增 `start-all.bat`，用于双击一键启动后端和前端。
- 第 8 步已逐项验证：后端构建脚本、前端构建脚本、全量验证脚本均通过。
- 已新增 `docs/context/enterprise-structure-handoff.md`，记录结构迁移后的运行入口、交接边界、统一命令和注意事项。
- 已新增 `scripts/README.md`，记录项目级脚本职责和使用方式。
- 第 10 步最终验证：`scripts/verify-all.ps1` 通过。

### 4.2 后端项目骨架

已创建后端目录：

```text
apps/api-server/
```

已完成内容：

- `pom.xml`
- Spring Boot 启动类
- `application.yml`
- 统一返回结构 `ApiResponse`
- 分页返回结构 `PageResponse`
- 分页查询参数 `PageQuery`
- 业务异常 `BusinessException`
- 全局异常处理 `GlobalExceptionHandler`
- 基础实体 `BaseEntity`
- 通用状态枚举
- CORS 配置
- MyBatis-Plus 分页配置
- MyBatis-Plus 自动填充配置
- 系统用户实体骨架
- 数据台账业务系统实体、Mapper、Service、Controller
- 分布式数据库资源实体、Mapper、Service、Controller
- 健康检查接口
- 登录接口壳
- 数据台账真实 CRUD 接口
- AI 模块概览接口壳

当前后端示例接口：

```text
GET  /api/health
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
GET  /api/ledger/business-systems
GET  /api/ledger/business-systems/{id}
POST /api/ledger/business-systems
PUT  /api/ledger/business-systems/{id}
DELETE /api/ledger/business-systems/{id}
GET  /api/ledger/db-resources
GET  /api/ledger/db-resources/{id}
POST /api/ledger/db-resources
PUT  /api/ledger/db-resources/{id}
DELETE /api/ledger/db-resources/{id}
GET  /api/dashboard/overview
GET  /api/system/users
GET  /api/system/users/{id}
POST /api/system/users
PUT  /api/system/users/{id}
PATCH /api/system/users/{id}/status
PATCH /api/system/users/{id}/password
DELETE /api/system/users/{id}
GET  /api/system/users/roles
GET  /api/auth/profile
PUT  /api/auth/profile
PATCH /api/auth/password
GET  /api/ai/overview
```

### 4.3 数据库与初始化脚本

已完成：

- `apps/api-server/src/main/resources/schema.sql`
- `apps/api-server/src/main/resources/data.sql`
- `application.yml` 已开启 `spring.sql.init.mode: always`
- 数据台账一期表：
  - `ledger_db_resource`
  - `ledger_business_system`
  - `ledger_business_system_db`
- 系统权限基础表：
  - `sys_user`
  - `sys_role`
  - `sys_user_role`
  - `sys_user.avatar_url`
  - `sys_role.data_scope`
  - `sys_role.data_scope_config`
- 菜单权限树基础表：
  - `sys_menu`
  - `sys_role_menu`
- 审计、字典、配置、通知和 AI 数据壳表已按一期范围补齐：
  - `login_log`
  - `operation_log`
  - `sys_dict_type`
  - `sys_dict_item`
  - `sys_config`
  - `system_notification`
  - `ai_model`
  - `ai_task`
  - `task_exec_record`
  - `kb_instance`
  - `fault_report`
  - `defect_report`
  - `opt_report`
  - `result_status_log`
- 通用字段：`id`、`created_at`、`updated_at`、`created_by`、`updated_by`、`deleted`
- 状态枚举：`NORMAL`、`WARNING`、`DANGER`、`OFFLINE`
- 索引：名称、负责人、状态、逻辑删除、业务系统关联数据库 ID
- 基础实体 `BaseEntity` 已显式使用 MyBatis-Plus `IdType.ASSIGN_ID` 主键策略
- 已新增 Jackson 全局配置，将后端 `Long/long` 序列化为字符串，避免前端 JavaScript 数字精度丢失导致详情、编辑、删除查询不到数据
- 初始化演示数据已通过 `INSERT IGNORE` 写入，避免重复插入固定 ID 数据
- 已预置角色：`admin`、`operator`、`viewer`
- 已预置默认管理员账号：`admin / password`
- 已结合用户提供的《数据库设计说明书_v1(1).docx》完成数据库设计评审：
  - 说明书中的总体链路为“数据台账 → 指标采集 → AI任务配置 → 定时执行 → 故障预警/缺陷报表/优化建议 → 人工处理 → 数据大屏”，与当前项目方向一致。
  - 说明书中的完整表体系适合作为二期/完整版本蓝图，当前一期已落地的 `ledger_business_system`、`ledger_db_resource`、`ledger_business_system_db`、`sys_user`、`sys_role`、`sys_user_role`、`sys_menu`、`sys_role_menu` 需要保留。
  - 当前项目已经实现菜单权限树和按钮权限，虽与说明书中的 `sys_menu_permission` 命名不同，但业务含义一致；短期不建议改名，以免破坏现有代码。
  - 当前项目的数据台账已升级为业务系统与数据库实例多对多关系，并保留 `ledger_business_system.database_id` 作为默认数据库兼容字段。
  - 后续数据库演进优先级建议：优先补部门组织、安全登录策略、报表导出日志、真实指标采样和更细粒度数据权限明细。

### 4.4 前端项目骨架

已创建前端目录：

```text
apps/admin-web/
```

已完成内容：

- Vite + Vue 3 + TypeScript 项目结构
- Element Plus 接入
- Element Plus 已由全量注册优化为当前项目组件按需注册，避免首屏引入未使用组件和全量样式
- Pinia 用户状态
- Vue Router 路由
- Axios 统一封装
- API 模块示例：认证、数据台账
- 主布局 `MainLayout`
- 登录页
- 数据台账完整 CRUD 页面
- 数据大屏页面骨架
- AI 模块数据壳页面和系统管理真实页面
- 个人中心页，支持头像、姓名、部门、手机号、邮箱维护和当前用户自助改密
- 消息通知页
- 统一主题样式 `theme.scss`
- 前端构建验证通过
- 本轮前端性能优化已完成：
  - `apps/admin-web/src/main.ts` 从全量 `ElementPlus` 注册改为当前使用组件按需注册，并改为按需引入 Element Plus 组件样式。
  - `apps/admin-web/vite.config.ts` 增加 Vue、Axios、Element 图标的稳定 vendor 分包，改善缓存和主入口解析压力。
  - `apps/admin-web/src/views/ai/AiModuleView.vue` 缓存 AI 模型、业务系统、数据库选项，路由切换时不再重复拉取三组选项接口。
  - 构建体积对比：主 JS 从约 `1,102.62 kB / gzip 365.51 kB` 降至约 `544.65 kB / gzip 177.41 kB`；主 CSS 从约 `362.42 kB / gzip 49.13 kB` 降至约 `209.86 kB / gzip 28.62 kB`。
  - 当前仍有 Vite chunk 超 500 kB 提示，但已接近阈值且不阻断；继续拆分需引入更细的 Element Plus 自动按需插件或进一步页面级组件拆分。
- 前端开发服务成功启动
- 已优化 `apps/admin-web/run-dev.ps1`，在系统未配置 pnpm 时可使用 Codex 内置 pnpm 启动；已兼容 `dependencies\bin\pnpm.cmd` 和 `dependencies\bin\fallback\pnpm.cmd`
- 用户已确认前端成功运行
- 数据台账页面已接入真实 Axios API：
  - 业务系统列表、搜索、分页、新增、编辑、删除、详情
  - 分布式数据库列表、搜索、分页、新增、编辑、删除、详情
  - 抽屉表单、详情抽屉、状态标签、删除确认、加载状态
- 前端台账模块的 `id`、`databaseId` 已改为字符串类型，与后端 Long 转字符串保持一致，避免雪花 ID 超过 JavaScript 安全整数范围

### 4.5 数据大屏模块

已完成：

- 后端新增 `GET /api/dashboard/overview` 聚合统计接口。
- 后端从真实台账表实时汇总：
  - 业务系统数量
  - 分布式数据库数量
  - 正常数量
  - 异常数量
  - 健康评分
  - 状态分布
  - 近 24 小时趋势
  - 实时告警列表
- 告警列表当前基于业务系统和分布式数据库的非正常状态生成：
  - `WARNING`：警告
  - `DANGER`：危险
  - `OFFLINE`：离线
- 前端新增 `apps/admin-web/src/api/modules/dashboard.ts`。
- 前端 `DashboardView.vue` 已接入真实接口，完成统计卡片、趋势柱状图、状态分布、实时告警列表和手动/自动刷新交互。
- 前端构建验证通过：`pnpm build`。
- 后端编译验证通过：`mvn -DskipTests compile`。
- 根据浏览器注释完成两处 UI 细节修正：
  - 数据大屏页“自动刷新”开关文字在深蓝背景下改为高对比白色，提高可读性。
  - 角色权限管理表格操作列加宽，并将“详情 / 编辑 / 分配权限 / 禁用 / 删除”按钮固定为同一行显示，避免删除按钮换行。
  - 前端构建验证通过：`pnpm build`。
- 已修复菜单权限变更后的登录加载问题：
  - 登录成功后不再固定跳转 `/data-ledger/visualization`，而是根据后端返回的菜单树跳转到当前用户第一个可访问页面。
  - 已登录访问 `/login` 和权限不足重定向时，也统一使用当前用户可访问首页，避免权限不足时反复跳转导致页面加载异常。
  - 角色权限树保存时会自动补齐已选权限的父级菜单。
  - 后端返回用户菜单和保存角色菜单权限时会自动补齐祖先节点，避免数据库中存在子权限但缺少父菜单导致菜单树断裂。
  - 后端编译验证通过：`mvn -DskipTests compile`。
  - 前端构建验证通过：`pnpm build`。

### 4.6 登录认证与用户权限基础模块

已完成：

- 后端引入 `spring-security-crypto`，使用 BCrypt 存储和校验密码。
- 后端实现轻量 JWT：
  - `POST /api/auth/login`
  - `POST /api/auth/logout`
  - `GET /api/auth/me`
- JWT 配置位于 `application.yml`：
  - `security.jwt.secret`
  - `security.jwt.expire-seconds`
- 后端新增 JWT 拦截器：
  - `/auth/login`、`/health`、`/actuator/**` 放行
  - 其他业务接口默认需要 token
  - token 过期、无效、用户禁用返回 HTTP 401
- 后端已新增基础 RBAC 接口级权限拦截：
  - `viewer`：只允许访问查询类 `GET` 接口，不能新增、编辑、删除台账和其他业务数据
  - `operator`：可执行普通业务写操作，例如数据台账新增、编辑、删除
  - `admin`：拥有系统管理和业务写操作权限
  - `/system/**` 用户管理、角色管理接口仅允许 `admin` 访问
  - 权限不足返回 HTTP 403 和“当前账号没有操作权限”
- 菜单权限树第一阶段已完成：
  - 新增 `sys_menu`、`sys_role_menu` 表结构
  - 新增菜单实体、角色菜单关系实体、Mapper
  - 新增菜单树查询接口：`GET /api/system/menus/tree`
  - 新增菜单详情、新增、编辑、启用/禁用、删除接口
  - 新增角色菜单权限查询接口：`GET /api/system/roles/{id}/menus`
  - 新增角色菜单权限保存接口：`PUT /api/system/roles/{id}/menus`
  - `GET /api/auth/me` 已返回当前用户菜单树 `menus` 和权限编码列表 `permissions`
  - 启动初始化器已兜底初始化当前系统菜单、按钮权限和 admin/operator/viewer 默认授权；三个预置角色会精确同步到项目定义的默认权限集，以清除历史残留越权授权，自定义角色不受影响
  - 数据库、后端权限基础、动态权限拦截、前端动态菜单和菜单权限管理页面均已完成
- 菜单权限树第二阶段已完成：
  - `JwtAuthInterceptor` 已从固定角色规则升级为动态权限编码校验。
  - 通过请求方法和接口路径映射 `permission_code`。
  - 用户权限来自 `sys_user_role -> sys_role_menu -> sys_menu.permission_code`。
  - `admin` 角色保留超级管理员兜底能力。
  - 非 admin 用户必须拥有对应 `permission_code` 才能访问接口。
  - 权限不足返回 HTTP 403 和“当前账号没有操作权限”。
  - 已补齐系统管理按钮权限初始化：
    - `system:user:create`
    - `system:user:update`
    - `system:user:status`
    - `system:user:reset-password`
    - `system:user:delete`
    - `system:role:create`
    - `system:role:update`
    - `system:role:status`
    - `system:role:delete`
    - `system:role:assign-menu`
    - `system:menu:create`
    - `system:menu:update`
    - `system:menu:status`
    - `system:menu:delete`
  - 后端编译验证通过：`mvn -DskipTests compile`。
- 后端实现用户管理：
  - 用户列表分页查询
  - 按账号、姓名、状态搜索
  - 新增用户
  - 编辑用户
  - 启用/禁用用户
  - 重置密码
  - 删除用户
  - 用户名唯一校验
  - 用户角色分配
  - 用户头像地址维护
  - 账号、手机号、邮箱、头像地址、密码、状态等字段参数校验
- 后端实现角色基础能力：
  - `sys_role`
  - `sys_user_role`
  - 预置 `admin`、`operator`、`viewer`
  - 菜单权限和按钮权限已通过 `sys_menu`、`sys_role_menu` 和动态权限拦截生效
  - 数据权限范围已通过 `data_scope`、`data_scope_config` 字段落地
- 后端实现角色管理独立 CRUD：
  - 分页查询
  - 按角色编码、角色名称、状态搜索
  - 新增角色
  - 编辑角色
  - 启用/禁用角色
  - 删除角色
  - 查看角色详情
  - 角色编码唯一校验
  - 数据权限枚举校验
  - 自定义数据权限必填校验
  - 已分配给用户的角色不允许删除
  - 新增、编辑、状态变更、删除记录后端操作日志
- 后端实现当前用户个人中心接口：
  - `GET /api/auth/profile`：查看当前登录用户个人资料
  - `PUT /api/auth/profile`：当前登录用户自助修改头像、姓名、部门、手机号、邮箱
  - `PATCH /api/auth/password`：当前登录用户校验原密码后自助修改密码
  - 个人资料修改和改密均写入操作日志
  - 改密成功后生成安全通知
- 前端登录能力已升级：
  - 登录成功保存 token 和用户信息
  - Axios 自动携带 token
  - HTTP 401 自动清理登录状态并跳转 `/login`
  - 路由守卫：未登录不能访问后台，已登录访问 `/login` 自动跳首页
  - 顶部用户菜单显示当前用户
  - 顶部显示当前用户头像，头像为空时显示用户名/姓名首字
  - 退出登录调用后端接口并清理本地状态
  - 基于后端返回的菜单树和权限编码做页面与按钮可见性控制
  - 前端登录用户状态已保存 `menus`、`permissions`、`avatarUrl`、`department`、`phone`、`email`
- 前端用户管理页面已完成：
  - 列表、搜索、分页
  - 新增、编辑
  - 启用/禁用
  - 重置密码
  - 删除
  - 角色分配
  - 头像地址维护
  - 手机号、邮箱、头像地址、密码等表单校验
- 前端角色管理页面已完成：
  - 路由 `/system/roles`
  - 列表、搜索、分页
  - 新增、编辑
  - 启用/禁用
  - 删除
  - 详情抽屉
  - 数据权限范围配置
  - 自定义数据权限范围填写和校验
  - 分配菜单/按钮权限抽屉
- 前端个人中心页面已完成：
  - 当前用户个人资料展示
  - 当前用户头像、姓名、部门、手机号、邮箱编辑保存
  - 当前用户自助修改密码
  - 保存资料后刷新本地登录用户状态
  - 头像地址、手机号、邮箱、密码规则表单校验
- 前端构建验证通过：`pnpm build`。
- 后端编译验证通过：`mvn -DskipTests compile`。

### 4.7 工程辅助文件

已完成：

- `README.md`
- `.gitignore`
- `apps/api-server/run-dev.ps1`
- `apps/admin-web/run-dev.ps1`
- `start-frontend.bat`
- `PROJECT_SPEC.md`

### 4.8 日志审计模块

已完成：

- 数据库新增 `login_log` 登录日志表。
- 数据库新增 `operation_log` 操作日志表。
- 后端新增登录日志和操作日志实体、Mapper、DTO、VO、Service。
- 后端新增日志查询接口：
  - `GET /api/system/logs/login`
  - `GET /api/system/logs/operation`
- 后端已接入登录日志写入：
  - 登录成功
  - 账号不存在
  - 密码错误
  - 用户禁用
- 后端已接入核心操作日志写入：
  - 数据台账：业务系统新增、编辑、删除；分布式数据库新增、编辑、删除。
  - 用户管理：新增、编辑、启用/禁用、重置密码、删除。
  - 角色管理：新增、编辑、启用/禁用、删除、分配菜单权限。
  - 菜单权限管理：新增、编辑、启用/禁用、删除。
- 后端新增权限编码：`system:audit-log:view`。
- 后端启动初始化器新增菜单：系统管理 / 日志审计。
- 后端权限拦截规则已接入：非 admin 用户需要拥有 `system:audit-log:view` 才能访问日志接口。
- 前端新增日志审计页面：`apps/admin-web/src/views/system/AuditLogView.vue`。
- 前端新增路由：`/system/logs`。
- 前端系统 API 已新增登录日志、操作日志分页查询方法。
- 日志审计页面使用 Tabs 区分：
  - 登录日志
  - 操作日志
- 登录日志支持按账号、状态、时间范围查询和分页。
- 操作日志支持按操作人、模块、操作类型、结果、时间范围查询和分页。

阶段验证结果：

- 阶段1：新增日志表、实体、Mapper、基础服务后，后端 `mvn -DskipTests compile` 通过。
- 阶段2：接入登录日志和操作日志写入后，后端 `mvn -DskipTests compile` 通过。
- 阶段3：新增日志查询接口和菜单权限初始化后，后端 `mvn -DskipTests compile` 通过。
- 阶段4：新增前端日志审计页面、路由和 API 后，前端 `pnpm build` 通过。

### 4.9 字典管理与系统配置模块

已完成：

- 数据库新增 `sys_dict_type` 字典类型表。
- 数据库新增 `sys_dict_item` 字典项表。
- 数据库新增 `sys_config` 系统配置表。
- 后端新增字典类型、字典项、系统配置实体、Mapper、DTO、VO、Service。
- 后端新增字典管理接口：
  - `GET /api/system/dict/types`
  - `GET /api/system/dict/types/{id}`
  - `POST /api/system/dict/types`
  - `PUT /api/system/dict/types/{id}`
  - `PATCH /api/system/dict/types/{id}/status`
  - `DELETE /api/system/dict/types/{id}`
  - `GET /api/system/dict/items`
  - `GET /api/system/dict/items/enabled/{dictCode}`
  - `GET /api/system/dict/items/{id}`
  - `POST /api/system/dict/items`
  - `PUT /api/system/dict/items/{id}`
  - `PATCH /api/system/dict/items/{id}/status`
  - `DELETE /api/system/dict/items/{id}`
- 后端新增系统配置接口：
  - `GET /api/system/configs`
  - `GET /api/system/configs/{id}`
  - `GET /api/system/configs/key/{configKey}`
  - `POST /api/system/configs`
  - `PUT /api/system/configs/{id}`
  - `PATCH /api/system/configs/{id}/status`
  - `DELETE /api/system/configs/{id}`
- 后端新增权限编码：
  - `system:dict:view`
  - `system:dict:create`
  - `system:dict:update`
  - `system:dict:status`
  - `system:dict:delete`
  - `system:config:view`
  - `system:config:create`
  - `system:config:update`
  - `system:config:status`
  - `system:config:delete`
- 后端启动初始化器新增菜单：
  - 系统管理 / 字典管理
  - 系统管理 / 系统配置
- 后端启动初始化器新增默认字典：
  - `HEALTH_STATUS`
  - `ENABLE_STATUS`
  - `LOGIN_STATUS`
  - `OPERATION_TYPE`
  - `TASK_TYPE`
- 后端启动初始化器新增默认配置：
  - `SYSTEM_NAME`
  - `TOKEN_EXPIRE_SECONDS`
  - `DASHBOARD_REFRESH_SECONDS`
  - `DEFAULT_PASSWORD`
- 字典管理、系统配置新增/编辑/状态变更/删除操作已写入操作日志。
- 前端新增字典管理页面：`apps/admin-web/src/views/system/DictManagementView.vue`。
- 前端新增系统配置页面：`apps/admin-web/src/views/system/ConfigManagementView.vue`。
- 前端新增路由：
  - `/system/dicts`
  - `/system/configs`
- 前端系统 API 已新增字典管理和系统配置相关请求方法。

阶段验证结果：

- 阶段1：新增字典/配置表、实体、Mapper、DTO、VO、Service 后，后端 `mvn -DskipTests compile` 通过。
- 阶段2：新增后端 CRUD 接口、权限初始化和操作日志接入后，后端 `mvn -DskipTests compile` 通过。
- 阶段3：新增前端字典管理 API、路由和页面后，前端 `pnpm build` 通过。
- 阶段4：新增前端系统配置页面后，前端 `pnpm build` 通过。

### 4.10 通知中心真实数据模块

已完成：

- 数据库新增 `system_notification` 系统通知表。
- 后端新增通知实体、Mapper、DTO、VO、Service。
- 后端新增通知中心接口：
  - `GET /api/notifications`
  - `GET /api/notifications/unread-count`
  - `GET /api/notifications/{id}`
  - `PATCH /api/notifications/{id}/read`
  - `PATCH /api/notifications/read-all`
  - `DELETE /api/notifications/{id}`
  - `DELETE /api/notifications/read`
- 通知接口默认需要登录，但不额外要求系统管理权限。
- 通知接口通过 `UserContext` 只返回当前登录用户自己的通知。
- 通知操作已接入操作日志：
  - 单条已读
  - 全部已读
  - 删除通知
  - 清空已读
- 后端启动初始化器新增通知相关字典：
  - `NOTICE_TYPE`
  - `NOTICE_PRIORITY`
  - `NOTICE_READ_STATUS`
- 后端启动初始化器为 admin 初始化演示通知，已有通知时不重复生成。
- 后端已接入通知生成场景：
  - 业务系统状态为 `WARNING`、`DANGER`、`OFFLINE` 时通知管理员。
  - 分布式数据库状态为 `WARNING`、`DANGER`、`OFFLINE` 时通知管理员。
  - 用户被禁用时通知被操作用户。
  - 用户密码被重置时通知被操作用户。
- 前端新增通知 API 模块：`apps/admin-web/src/api/modules/notification.ts`。
- 前端 `NotificationsView.vue` 已从静态卡片改为真实接口数据：
  - 分页查询
  - 关键字搜索
  - 类型筛选
  - 已读/未读筛选
  - 优先级筛选
  - 详情抽屉
  - 单条标记已读
  - 全部标记已读
  - 删除通知
  - 清空已读通知
- 前端新增 `useNotificationStore`，保存和刷新未读通知数量。
- 顶部铃铛已接入真实未读数量，未读为 0 时隐藏角标。
- 已修复顶部通知角标显示 0 的问题：当前改为未读数量大于 0 时才挂载 `el-badge`，没有未读通知时只显示普通铃铛按钮。

阶段验证结果：

- 阶段1：新增通知表、实体、Mapper、DTO、VO、Service 和字典项后，后端 `mvn -DskipTests compile` 通过。
- 阶段2：新增通知接口、个人数据隔离和操作日志后，后端 `mvn -DskipTests compile` 通过。
- 阶段3：接入通知生成场景后，后端 `mvn -DskipTests compile` 通过。
- 阶段4：改造通知中心页面接真实接口后，前端 `pnpm build` 通过。
- 阶段5：顶部铃铛接入未读数后，前端 `pnpm build` 通过。
- 角标修复验证：前端 `pnpm build` 通过。

## 5. 待实现清单

### 5.0 一期收尾盘点

当前一期主体功能已经基本完成，收尾阶段不建议继续扩张大功能，应重点做联调、数据补齐、文档沉淀和风险修复。

一期已具备的核心能力：

- 企业后台基础框架：Spring Boot 3、Vue 3、MySQL、统一返回、异常处理、分页、CORS、基础日志。
- 登录认证：JWT、BCrypt、登录/退出、当前用户、401 跳转、路由守卫。
- 用户权限：用户管理、角色管理、角色数据权限配置、菜单权限树、按钮权限、后端接口权限拦截、个人中心自助维护。
- 数据台账：业务系统、分布式数据库 CRUD；业务系统支持关联多个数据库。
- 数据大屏：概览统计、状态分布、近 24 小时趋势、告警列表。
- 日志审计：登录日志、操作日志。
- 字典配置：字典管理、系统配置。
- 通知中心：真实通知数据、未读数、已读、删除、清空已读；角标为 0 时不显示。
- AI 数据壳：AI 故障预测、AI 缺陷检测、AI 性能优化的基础表、接口、权限、前端页面和 CRUD 已完成第一版。

一期收尾必须完成：

1. 全量重启联调。
   - 重启后端，让 `schema.sql` 新增表、`SystemBootstrapInitializer` 新增菜单权限生效。
   - 重新登录前端，刷新本地菜单和权限缓存。
   - 逐页验证新增 AI 菜单是否显示、接口是否有权限。

2. 演示数据补齐。
   - 当前台账已有演示数据。
   - AI 模型、AI 任务、知识库、故障预警、缺陷报表、优化建议初始化演示数据已补齐。
   - 演示数据已放入 `apps/api-server/src/main/resources/data.sql`，使用固定 ID 和 `INSERT IGNORE`，避免重复插入。
   - AI 演示数据的编码、名称、内容、备注等均显式包含“演示”或 `DEMO` 标识，避免和真实业务数据混淆。
   - 本轮后端验证通过：`mvn -DskipTests compile`。

3. 接口验收清单。
   - 已完成一期基础接口测试与验收清单：`docs/一期基础接口测试与验收清单.md`。
   - 已完成一期接口冒烟测试脚本：`docs/一期接口冒烟测试.http`。
   - 覆盖登录、退出、当前用户。
   - 覆盖数据台账业务系统 CRUD、数据库 CRUD、多数据库关联。
   - 覆盖数据大屏基础统计。
   - 覆盖用户、角色、菜单权限管理。
   - 覆盖登录日志、操作日志。
   - 覆盖字典、系统配置。
   - 覆盖通知中心。
   - 覆盖 AI 知识库、任务、执行记录、三类结果 CRUD 和状态处理。

4. 权限验收清单。
   - `admin`：全部可见、全部可操作。
   - `operator`：可维护台账和 AI 数据壳，不应进入系统管理敏感页面。
   - `viewer`：只读查看，不应出现新增、编辑、删除、处理、执行按钮。
   - 角色数据权限：角色新增/编辑时必须可配置 `全部数据 / 本部门数据 / 本人数据 / 自定义数据`，选择自定义时必须填写范围。
   - 个人中心：普通登录用户只能通过 `/auth/profile` 和 `/auth/password` 修改自己的资料和密码，不能借个人中心修改他人账号。
   - 修改角色权限后，需要重新登录或刷新用户信息再验证。

5. 前端 UI 收口。
   - 重点检查 AI 三大模块页面与 Figma 原型的一致性：标题、筛选区、表格、右侧抽屉、状态标签、按钮位置。
   - 检查移动/窄屏下表格操作列、抽屉表单、顶部按钮是否拥挤。
   - 修复已发现的可见 UI 问题，例如通知角标 0 已修复。

6. 文档与交付材料。
   - README 快速启动已补充一期验收材料入口。
   - 一期验收清单已输出：`docs/一期基础接口测试与验收清单.md`。
   - 默认账号密码、数据库配置位置、脚本位置已写入验收清单。
   - 核心接口清单已写入验收清单和冒烟测试脚本。
   - 输出一期不做内容边界：真实 AI 推理、真实采集、自动修复、生产级部署暂不包含。

建议一期可选增强：

- 为 AI 模块补更细的 DTO/VO，减少直接返回实体字段。
- 为 AI 模块补统一状态字典项，例如 `AI_PROCESS_STATUS`、`AI_RISK_LEVEL`、`AI_TASK_STATUS`。
- 增加一键初始化演示数据脚本。
- 整理一期演示路线：登录 → 数据大屏 → 数据台账 → AI 数据壳 → 权限切换 → 日志审计。

### 5.1 数据库设计与初始化

- 数据台账一期表结构和演示数据已完成。
- 系统用户、角色、菜单权限、角色数据权限字段、登录日志、操作日志、字典、配置、通知中心和 AI 数据壳表结构已完成。
- 后续仍需覆盖安全登录策略和真实指标采样。
- 基础菜单、角色、用户、AI 演示数据已补齐。
- 结合用户数据库设计说明书，后续建议按兼容演进方式补齐以下表：
  - 组织与数据权限：角色数据权限字段已完成；后续补 `sys_dept` 和可选的 `sys_role_data_scope` 明细表
  - 审计与安全：`operation_log`、`login_log` 已完成；后续补 `report_export_log`、`sys_login_policy`
  - 字典与配置：`sys_dict_type`、`sys_dict_item`、`sys_config` 已完成
  - 台账增强：`ledger_business_system_db` 已完成，后续可继续增强关系类型、排序和批量迁移脚本
  - AI 任务与模型：`ai_model`、`ai_task`、`task_exec_record` 已完成一期数据壳
  - 知识库与结果：`kb_instance`、`result_knowledge_ref`、`fault_report`、`defect_report`、`opt_report`、`result_status_log` 已完成一期数据壳
  - 指标采集：后续需补充指标定义、指标采样、故障指标趋势等表，支撑数据大屏和 AI 结果详情可回溯。

### 5.2 后端真实接口

- 数据台账业务系统与分布式数据库真实 CRUD 已完成。
- 数据台账已支持一个业务系统关联多个分布式数据库。
- 使用 MyBatis-Plus 实现分页查询。
- 已增加数据台账参数 DTO、响应 VO、参数校验、状态枚举校验。
- 已修复数据台账详情、编辑、删除时报“业务系统不存在 / 分布式数据库不存在”的 ID 精度问题：后端 Long 返回字符串，前端按字符串传递 ID。
- 系统权限、数据大屏、AI 数据壳等模块真实接口已补齐一期范围。
- 登录日志、操作日志已完成；后续可继续补报表导出日志和更细粒度接口审计。

### 5.3 系统权限

- 用户管理 CRUD 已完成。
- 角色基础数据结构已完成。
- 用户角色分配已完成。
- 登录接口已从数据库校验账号密码。
- JWT 登录认证已完成。
- 菜单权限、按钮权限和接口级动态权限拦截已完成。
- 角色数据权限配置已完成，当前落在 `sys_role.data_scope` 和 `sys_role.data_scope_config`。
- 当前用户个人中心自助修改资料和密码已完成。
- 角色管理独立 CRUD 页面已完成。
- 基础 RBAC 权限拦截已完成，角色不再只是标签；后端会真实限制不同角色的接口操作范围。

### 5.11 角色管理页面规划

“系统管理 / 角色权限管理”页面已实现，本节保留为当前能力说明和验收口径。

一期角色管理范围：

- 复用已创建的 `sys_role` 表。
- 新增角色列表页面：
  - 分页查询
  - 按角色编码、角色名称、状态搜索
  - 新增角色
  - 编辑角色
  - 启用/禁用角色
  - 删除角色
  - 查看角色详情
- 角色字段：
  - 角色编码 `role_code`
  - 角色名称 `role_name`
  - 数据权限 `data_scope`
  - 自定义数据权限配置 `data_scope_config`
  - 状态 `status`
  - 备注 `remark`
  - 创建时间 `created_at`
  - 更新时间 `updated_at`
- 角色状态枚举：
  - `ENABLED`：启用
  - `DISABLED`：禁用
- 权限能力：
  - 菜单权限、按钮权限已通过权限树分配。
  - 后端接口已按权限编码动态拦截。
  - 页面按钮已按权限编码显示/隐藏。
  - 数据权限已支持 `ALL`、`DEPARTMENT`、`SELF`、`CUSTOM` 四类范围。
  - 选择 `CUSTOM` 时必须填写自定义数据范围；非 `CUSTOM` 保存时清空自定义范围。
- 业务约束：
  - 角色编码唯一。
  - 已分配给用户的角色删除时应给出提示，建议先禁止删除，避免用户角色关系异常。
  - 预置角色 `admin`、`operator`、`viewer` 建议允许编辑名称/备注/状态，但删除需谨慎；一期可统一按“已分配不可删除”规则处理。
- 前端实现：
  - 路由 `/system/roles`
  - 页面文件 `apps/admin-web/src/views/system/RoleManagementView.vue`
  - API 文件复用或扩展 `apps/admin-web/src/api/modules/system.ts`
  - UI 风格与用户管理、数据台账保持一致：筛选表单、表格、分页、抽屉表单、状态标签、删除确认。
- 后端实现：
  - `SysRoleController`
  - `SysRoleService`
  - `RolePageQuery`
  - `RoleSaveRequest`
  - `RoleStatusRequest`
  - `RoleVO`
  - 接口统一返回 `ApiResponse`
  - 分页统一返回 `PageResponse`
- 验收标准：
  - 登录后可进入“系统管理 / 角色权限管理”。
  - 可查询、分页、新增、编辑、启用/禁用、删除角色。
  - 可配置角色数据权限范围。
  - 选择自定义数据权限时，不填写自定义范围应阻止保存并提示。
  - 角色编码重复时返回清晰错误。
  - 删除已分配用户的角色时返回清晰错误。
  - 用户管理页面的角色下拉能同步展示角色管理中新增或修改后的启用角色。
  - 前端构建通过。
  - 后端 Maven 编译通过。

### 5.12 菜单权限树当前状态

菜单权限树已完成数据库、后端接口、动态权限拦截、前端动态菜单和角色授权页面。

第二阶段：后端接口权限动态生效，已完成。

第三阶段：前端动态菜单和权限配置页面，已完成。

- Pinia 已保存并持久化 `menus` 和 `permissions`。
- 登录成功后保存后端返回的菜单树和权限编码。
- 左侧菜单已从固定写死改为根据后端菜单树动态渲染。
- 路由守卫已按 `permission_code` 控制，直接输入 URL 也会被前端拦截。
- 数据台账新增、编辑、删除按钮已按权限编码控制：
  - `ledger:business:create`
  - `ledger:business:update`
  - `ledger:business:delete`
  - `ledger:database:create`
  - `ledger:database:update`
  - `ledger:database:delete`
- 角色管理页面已增加“分配权限”抽屉，可勾选菜单和按钮权限并保存到角色。
- 新增菜单权限管理页面 `/system/menus`：
  - 菜单树展示
  - 新增菜单/按钮
  - 编辑菜单/按钮
  - 启用/禁用
  - 删除
  - 配置权限编码、路由、图标、排序、显示状态
- 前端构建验证通过：`pnpm build`。
- 后端编译验证通过：`mvn -DskipTests compile`。

- 修改角色权限后重新登录或刷新权限后生效。
- 修改角色权限时，如果只取消按钮权限，例如取消 `ledger:business:update` 或 `ledger:database:update`，应保留对应页面查看权限 `ledger:visualization:view`，否则用户会失去整个台账页面访问入口；当前前端和后端已增加父级菜单补齐与可访问首页跳转兜底。

### 5.4 数据台账

- 业务系统 CRUD 已完成。
- 分布式数据库资源 CRUD 已完成。
- 业务系统已支持关联多个分布式数据库，新增/编辑表单为多选，列表和详情使用标签展示多个数据库。
- 删除分布式数据库时已同时检查旧单关联字段和新多对多关联表，已被业务系统关联时禁止删除。
- 台账列表筛选、分页、状态标签已完成。
- 台账详情抽屉已完成。
- 本轮后端编译验证通过：`mvn -DskipTests compile`。
- 本轮前端构建验证通过：`pnpm build`。
- 后续需在用户本机 MySQL 环境重启后端后完成多数据库关联联调验证。
- 指标采样数据接口。
- 日志采集记录、历史日志、日志详情接口。

### 5.5 数据大屏

- 概览统计接口已完成。
- 实时告警列表已完成。
- 近 24 小时趋势已完成。
- 状态分布已完成。
- 前端已实现 30 秒自动刷新开关。
- 后续可接入真实服务器、数据库、业务指标采样表。
- 后续可接入 WebSocket 推送。

### 5.6 AI故障预测

一期只做数据壳：

- 故障知识库 CRUD。
- 故障预测任务 CRUD。
- 任务调度规则维护。
- 执行记录列表。
- 故障预警列表与详情。
- 确认/误报处理。

暂不实现：

- 真实故障预测模型。
- 模型服务调用。
- 自动诊断和自动修复。

### 5.7 AI缺陷检测

一期只做数据壳：

- 缺陷知识库 CRUD。
- 缺陷检测任务 CRUD。
- 任务执行看板数据。
- 缺陷报表列表与详情。
- 确认/误报处理。

暂不实现：

- 真实日志异常检测。
- 真实代码/架构/兼容性/安全检测算法。

### 5.8 AI性能优化

一期只做数据壳：

- 优化知识库 CRUD。
- 性能优化任务 CRUD。
- DB 执行情况报表。
- 性能画像详情。
- 优化建议列表与详情。
- 确认/误报处理。

暂不实现：

- 自动 SQL 修复。
- 真实执行计划分析。
- 自动执行优化动作。

### 5.13 AI 三大模块数据壳实施计划

本阶段目标：实现 AI故障预测、AI缺陷检测、AI性能优化的一期数据壳页面和基础 CRUD。真实模型推理、自动检测、自动修复暂不实现，但数据库、接口、权限、页面和流程要按企业级项目可扩展方式落地。

前端设计约束：

- AI 三大模块前端必须参考 Figma Make 原型：`https://www.figma.com/make/9LjKaVBmaB3UvcrvTJuOXq/根据文档生成应用`。
- 已读取 Figma Make 设计上下文，源文件包含：
  - `src/app/App.tsx`
  - `src/app/components/layout/AppLayout.tsx`
  - `src/app/pages/fault-prediction/KnowledgeBase.tsx`
  - `src/app/pages/fault-prediction/TaskConfig.tsx`
  - `src/app/pages/fault-prediction/AlertList.tsx`
  - 后续实现缺陷检测和性能优化时，需继续读取对应 Figma 源码文件再编码。
- 当前项目技术栈是 Vue 3 + Element Plus，不能直接复制 Figma 中的 React + Tailwind 代码；必须把原型中的布局、信息架构、交互和视觉语言转换为当前项目组件风格。
- 视觉基准为 Figma 原型的白蓝商务后台风格：浅色背景、白色内容卡片、蓝色主按钮、轻量阴影、8-12px 圆角、紧凑表格、右侧抽屉、状态标签。
- 页面结构基准：
  - AI故障预测：`/fault-prediction/knowledge`、`/fault-prediction/task`、`/fault-prediction/alert`
  - AI缺陷检测：`/defect-detection/knowledge`、`/defect-detection/task`、`/defect-detection/report`
  - AI性能优化：`/performance/knowledge`、`/performance/task`、`/performance/suggestion`
- 交互基准：
  - 知识库页面：筛选区、表格、分页、新增/编辑抽屉、真实附件上传、下载、图片预览和逻辑删除确认。
  - 任务配置页面：筛选区、任务表格、查看/编辑抽屉、预测/检测/优化对象选择、AI 模型选择、指标多选、定时规则配置、手动执行入口。
  - 结果页面：筛选区、状态标签、详情抽屉、确认/误报/关闭等状态处理，故障预警页面可保留指标趋势图壳。

第一阶段：数据库与通用 AI 底座。

- 新增 `ai_model`：AI 模型配置表，用于预留模型编码、名称、类型、版本、供应方、状态。
- 新增 `ai_task`：AI 任务主表，统一承载故障预测、缺陷检测、性能优化三类任务。
- 新增 `task_exec_record`：任务执行记录表，用于展示手动/定时执行历史。
- 新增 `kb_instance`：统一知识库表，按 `FAULT`、`DEFECT`、`OPTIMIZATION` 区分知识类型。
- 新增 `result_status_log`：结果状态流转日志，记录确认、误报、关闭、已处理等操作。
- 新增 `result_knowledge_ref`：结果与知识库引用/沉淀关系。
- 所有表沿用当前项目规范：`BIGINT` 雪花 ID、通用审计字段、逻辑删除、字符串枚举、业务索引、不强制真实外键。

第二阶段：后端通用能力。

- 新增 `ai` 模块分层结构：`entity`、`mapper`、`dto`、`vo`、`service`、`controller`。
- 实现 AI 模型 CRUD。
- 实现 AI 任务 CRUD、启用/禁用、手动执行占位接口。
- 实现任务执行记录分页查询。
- 实现统一知识库 CRUD。
- 实现结果状态流转通用服务。
- 接入统一返回结构、统一异常处理、参数校验、分页查询、操作日志、基础权限编码。

第三阶段：故障预测数据壳。

- 新增 `fault_report`：故障预警结果表。
- 后端接口支持故障预警列表、详情、新增、编辑、删除、状态处理。
- 前端新增“AI故障预测”页面，建议包含：
  - 故障预测任务
  - 故障知识库
  - 执行记录
  - 故障预警结果
- 页面使用表格、筛选、分页、抽屉详情、抽屉表单、风险等级标签、处理状态标签。

第四阶段：缺陷检测数据壳。

- 新增 `defect_report`：缺陷检测结果表。
- 后端接口支持缺陷报表列表、详情、新增、编辑、删除、状态处理。
- 前端新增“AI缺陷检测”页面，建议包含：
  - 缺陷检测任务
  - 缺陷知识库
  - 执行记录
  - 缺陷检测报表
- 页面重点展示缺陷类型、严重等级、问题描述、修复建议、处理状态。

第五阶段：性能优化数据壳。

- 新增 `opt_report`：性能优化建议表。
- 后端接口支持优化建议列表、详情、新增、编辑、删除、状态处理。
- 前端新增“AI性能优化”页面，建议包含：
  - 性能优化任务
  - 优化知识库
  - 执行记录
  - 优化建议
- 页面重点展示优化类型、问题 SQL、优化建议、置信度、执行结果、处理状态。

第六阶段：菜单权限与验收。

- 初始化菜单：
  - AI故障预测
  - AI缺陷检测
  - AI性能优化
- 初始化权限编码：
  - `ai:fault:view/create/update/delete/handle`
  - `ai:defect:view/create/update/delete/handle`
  - `ai:optimization:view/create/update/delete/handle`
  - `ai:task:view/create/update/delete/execute`
  - `ai:knowledge:view/create/update/delete`
- 前端路由接入动态菜单。
- 页面按钮按权限编码显示/隐藏。
- 后端接口按权限编码拦截。
- 每阶段完成后分别执行后端 `mvn -DskipTests compile` 和前端 `pnpm build`，并说明页面联调验证方法。

当前完成状态：

- 第一阶段已完成：
  - `schema.sql` 已新增 `ai_model`、`ai_task`、`task_exec_record`、`kb_instance`、`fault_report`、`defect_report`、`opt_report`、`result_status_log`、`result_knowledge_ref`。
  - 后端已新增上述表对应的实体和 Mapper。
  - 后端编译验证通过：`mvn -DskipTests compile`。
- 第二阶段已完成：
  - 后端新增 AI 管理 Service：`AiManagementService`、`AiManagementServiceImpl`。
  - 后端新增 AI 模型接口：`/api/ai/models`。
  - 后端新增 AI 知识库接口：`/api/ai/knowledge`。
  - 后端新增 AI 任务接口：`/api/ai/tasks`。
  - 后端新增任务执行接口：`POST /api/ai/tasks/{id}/execute`。
  - 后端新增执行记录接口：`GET /api/ai/tasks/executions`。
  - 后端新增故障预测结果接口：`/api/ai/fault-reports`。
  - 后端新增缺陷检测结果接口：`/api/ai/defect-reports`。
  - 后端新增性能优化结果接口：`/api/ai/optimization-reports`。
  - 结果接口已支持处理状态流转：`PATCH /handle`，并写入 `result_status_log` 和操作日志。
  - 后端编译验证通过：`mvn -DskipTests compile`。
- 第三至第五阶段的前端数据壳已完成第一版：
  - 新增前端 API：`apps/admin-web/src/api/modules/ai.ts`。
  - 新增复用页面：`apps/admin-web/src/views/ai/AiModuleView.vue`。
  - 已接入 9 个 Figma 路由：
    - `/fault-prediction/knowledge`
    - `/fault-prediction/task`
    - `/fault-prediction/alert`
    - `/defect-detection/knowledge`
    - `/defect-detection/task`
    - `/defect-detection/report`
    - `/performance/knowledge`
    - `/performance/task`
    - `/performance/suggestion`
  - 页面已实现筛选、分页、表格/预警卡片、右侧抽屉表单、详情抽屉、删除确认、任务手动执行、结果确认/误报/关闭。
  - 前端构建验证通过：`pnpm build`。
- 第六阶段已完成基础权限接入：
  - `JwtAuthInterceptor` 已新增 AI 模块 URL 权限规则。
  - `SystemBootstrapInitializer` 已新增 AI 模块按钮权限初始化。
  - `operator` 默认拥有 AI 数据壳维护权限。
  - `viewer` 默认拥有 AI 页面查看和基础查询权限。

本轮后续联调注意事项：

- 后端需要重启，让 `schema.sql` 自动创建新增 AI 表，让 `SystemBootstrapInitializer` 写入新增菜单和按钮权限。
- 用户需要重新登录，刷新本地保存的菜单和权限。
- 当前页面为数据壳 CRUD，任务执行只生成一条执行记录，不调用真实 AI 模型。
- 当前前端按 Figma 原型还原核心结构，但使用 Element Plus 组件承载，后续可继续按单页面细化视觉细节。

### 5.9 前端页面完善

- 数据台账页面已按 Figma 后台风格完成一期还原。
- 后续基于 Figma 原型继续还原其他业务页面。
- 使用 Element Plus 表格、分页、抽屉、弹窗、标签、表单校验。
- 数据台账 Axios API 调用已完成。
- 完善加载状态、空状态、错误状态。
- 完善路由标题、菜单选中、面包屑。

### 5.10 验证与运行

- 安装并配置 Maven。
- 解决当前 `mvn` 无法识别的问题。
- 在 IDE 中将 Project SDK、Module SDK、Language level 设置为 JDK 17，并重新导入 Maven 项目，解决发行版本 5 编译问题。
- 刷新 Maven 依赖，确认 `mybatis-plus-jsqlparser` 下载成功，解决 `PaginationInnerInterceptor` 找不到符号问题。
- 后端执行 `mvn spring-boot:run`。
- 用户已确认后端成功运行。
- 后端连接 MySQL 验证数据台账接口。
- 前端已由用户确认成功运行。
- 前端构建验证已通过：`pnpm build`。
- 前后端联调数据台账列表、搜索、分页、新增、编辑、删除、详情。
- 登录认证、用户管理模块需重启后端后验证。
- 补充基础单元测试或接口测试。

## 6. 约束条件

### 6.1 技术栈约束

后端必须使用：

- Spring Boot 3
- Java 17
- MyBatis-Plus
- MySQL

前端必须使用：

- Vue 3
- Vite
- TypeScript
- Element Plus
- Pinia
- Vue Router
- Axios

### 6.2 实现范围约束

- 不直接一次性写完整系统，必须按阶段推进。
- 每个阶段完成后，需要说明如何运行、如何验证。
- AI 三大模块一期不实现真实 AI 算法，只保留界面和数据结构。
- 后端接口最终必须真实连接 MySQL，不以 Mock 作为最终交付。
- 前端 UI 尽量还原 Figma 原型的布局、菜单、表格、抽屉表单、状态标签和交互。

### 6.3 工程规范约束

- 后端必须包含统一返回结构。
- 后端必须包含统一异常处理。
- 后端必须包含参数校验。
- 后端必须支持分页查询。
- 后端必须配置 CORS。
- 后端必须保留基础权限扩展点。
- 后端实体应包含创建时间、更新时间、创建人、更新人、逻辑删除等通用字段。
- 前端 API 请求必须通过 Axios 封装。
- 前端状态管理必须使用 Pinia。
- 前端路由必须通过 Vue Router 统一管理。

### 6.4 文档维护约束

从本轮开始，后续每轮对话结束时必须更新本文档：

- 把刚刚沟通确定的内容写入文档。
- 剔除过时内容。
- 更新已完成内容。
- 更新待实现清单。
- 保持文档可作为后续工作的最新上下文来源。

后续回答格式约束：

- 正式输出内容放在回答前面。
- 每轮结束仍必须更新本文档，但回复中不再展示完整版《项目说明书》，除非用户明确要求。
- 文档内容使用 Markdown 格式。

Vibe Coding 上下文维护约束：

- 涉及需求范围时，同步更新 `docs/context/requirements`。
- 涉及代码规范时，同步更新 `docs/standards`。
- 涉及基座或系统管理模块时，同步更新 `docs/modules/base-platform` 或 `docs/modules/system-management`。
- 涉及角色协作方式时，同步更新 `.ai-context` 中对应角色的 `compact.md` 或流程文档。
- 前端结构已完成迁移；后续文档中运行代码路径应统一使用 `apps/admin-web/`，仅历史基线可保留旧 `frontend/`。

## 7. 注意事项

- 当前 Git 根目录可能在 `E:\vibe work`，而项目在 `E:\vibe work\springboot work`。执行 Git 命令时可能出现 ownership / safe.directory 提示。
- 当前系统 `java` 命令默认路径异常，应优先使用 `C:\Program Files\Java\jdk-17\bin\java.exe` 或修复 PATH。
- 用户已确认后端成功运行。
- 第四步新增后端代码后，需要重启后端，使 `schema.sql`、`data.sql` 初始化并加载最新接口。
- 第六步新增后端依赖 `spring-security-crypto`，首次启动需要 Maven 下载依赖；默认账号为 `admin / password`。
- 已使用 `E:\maven\apache-maven-3.9.16` 验证后端 `mvn -DskipTests compile` 构建成功。
- 如果 IDE 运行时报 `不支持发行版本 5`，需要确认 IDE 使用 JDK 17 编译，并刷新 Maven；项目 `pom.xml` 已显式指定 Maven compiler release 17。
- 如果 IDE 运行时报 `PaginationInnerInterceptor` 找不到符号，需要刷新 Maven 依赖；项目 `pom.xml` 已补充 `mybatis-plus-jsqlparser`。
- 前端依赖已经安装，`pnpm build` 已验证通过。
- 第七步迁移前端目录后，已清理旧 `node_modules` 并在 `apps/admin-web/` 下离线重建依赖；迁移后 `pnpm build` 已验证通过。
- 当前 `apps/admin-web/pnpm-workspace.yaml` 记录 pnpm v11 的允许构建依赖：`esbuild`、`@parcel/watcher`、`vue-demi`。
- 第八步已新增统一脚本：`scripts/dev-api.ps1`、`scripts/build-api.ps1`、`scripts/dev-web.ps1`、`scripts/build-web.ps1`、`scripts/verify-all.ps1`。
- 第八步验证结果：`scripts/build-api.ps1`、`scripts/build-web.ps1`、`scripts/verify-all.ps1` 均通过；后端构建在沙箱内可能因 Maven 依赖解析被拦，需要提权执行。
- 第九步已新增结构交接文档 `docs/context/enterprise-structure-handoff.md` 和脚本说明 `scripts/README.md`。
- 第十步最终验证结果：`scripts/verify-all.ps1` 通过；后端 Maven 编译成功，前端 Vite 构建成功。
- 本轮新增一键启动脚本：`start-all.bat`、`scripts/dev-all.ps1`；`scripts/dev-all.ps1 -DryRun` 已验证通过。
- 本轮性能排查与优化：
  - 前端入口已从全量 Element Plus 引入改为按需组件和样式引入。
  - Vite 已拆分 `vue-vendor`、`http-vendor`、`element-icons` 缓存分包。
  - AI 数据壳页面已缓存模型、业务系统、数据库选项，减少路由切换重复接口请求。
  - 后端 `BusinessSystemServiceImpl.page` 已改为批量加载业务系统关联数据库和数据库名称，避免分页列表 N+1 查询。
  - 验证结果：`scripts/verify-all.ps1` 通过；后端 Maven 编译成功，前端 Vite 构建成功。
- 本轮系统管理模块代码打磨：
  - 范围覆盖用户、角色、菜单、字典、配置、审计日志、通知和个人中心；未修改既有接口路径、入参出参结构和核心业务流程。
  - 用户分页已批量加载角色关系与角色信息，消除逐用户角色查询的 N+1 路径；菜单祖先补齐改为一次加载菜单映射，并拦截不存在父级、自身父级和循环父子关系。
  - 通知全部已读、清空已读改为批量数据库操作；字典类型改名同步字典项改为批量更新；系统管理写操作补齐事务边界和审计一致性。
  - 用户、角色管理前端补齐按钮权限显示，与后端 `system:user:*`、`system:role:*` 权限编码一致；后端权限拦截仍为最终安全边界。
  - 配置、字典、菜单 DTO 已补类型、状态、长度和可见性边界校验；非法配置类型接口验证返回 `code = 400` 且未写入数据。
  - 本轮验证：后端 `scripts/build-api.ps1` 通过，前端 `scripts/build-web.ps1` 通过；健康检查、管理员登录、当前用户、用户分页、角色分页、菜单树、通知列表均成功；`operator`、`viewer` 访问用户管理接口均为 HTTP 403，`viewer` 访问已授权数据大屏为 HTTP 200。详见 `docs/modules/system-management/code-quality-optimization.md`。
- 本轮数据台账模块代码打磨：
  - 范围覆盖业务系统、分布式数据库资源、关联关系的后端服务和台账前端页面；未修改现有接口路径、请求/响应结构、表映射及关联关系的既有替换语义。
  - 已清理前端未使用表单字段，数据库选项改为打开业务系统抽屉时按需加载；后端写操作补齐事务，对象查询统一复用，并保持业务系统分页关联数据批量加载。
  - DTO 已补充关联 ID、关联数量、文本长度、状态白名单和严格 IPv4 校验。空保存请求、非法状态查询均返回业务码 `400` 且未写入数据。
  - 修复启动初始化器只追加授权而遗留越权菜单的权限一致性问题：`admin`、`operator`、`viewer` 三个预置角色在启动时精确同步默认授权，自定义角色不受影响；恢复了误删的“财务管理系统”及其主数据库关联。
  - 本轮验证：后端 Maven 编译通过，前端构建通过；独立后端实例健康检查通过；`viewer` 台账查询 HTTP `200`，新增、删除 HTTP `403`；`operator`、`admin` 的空保存请求均返回业务码 `400`。打磨记录已统一为中文，详见 `docs/modules/data-ledger/code-quality-optimization.md`。
- 本轮数据台账详情页与演示链路：
  - 业务系统、分布式数据库列表的“查看详情”已由抽屉改为独立详情路由，既有 CRUD 接口、字段和写权限保持兼容。
  - 新增 `bus_system_data_record`、`db_instance_metric`、`db_instance_log` 三张详情数据表和演示初始化数据；新表按数据库设计蓝图命名，逻辑关联既有 `ledger_business_system`、`ledger_db_resource` 主表，不扩大 `ledger_*` 命名差异。
  - 新增业务数据、数据库指标、数据库日志查询和 CSV 下载接口；`report_export_log` 已落地并兼容扩展 `BUSINESS_DATA`、`DB_METRICS`、`DB_LOG` 三类台账导出审计，下载同时写操作日志。
  - 数据库详情支持 `1小时` 至 `14天` 时间范围、CPU/内存趋势、磁盘 IOPS、缓存命中率和服务器日志；当前为可运行的数据库演示数据，真实连接与采集任务仍在后续范围。
  - 本轮验证：后端编译、前端构建、独立实例健康检查通过；`admin/operator/viewer` 查询详情均返回业务码 `200`，非法时间范围返回业务码 `400`；业务数据、指标、日志 CSV 下载均返回 `text/csv` 且审计成功落库。详见 `docs/modules/data-ledger/detail-pages.md`。
  - 详情返回列表已补齐来源页签恢复：列表跳转详情时传递 `tab` 查询参数，业务系统和分布式数据库详情的标题返回均恢复至对应列表页签；运行中的前端页面已完成两条返回链路回归，前端生产构建通过。
  - 已按 Figma Make 原型 `DataDetail.tsx` 统一两类详情页 UI：40px 内容区、浅蓝信息栏、12px 面板圆角、分段页签、交替表格行和窄屏单列布局。业务数据与服务器日志的顶部下载按钮改为勾选后启用，分别通过可选 `recordIds`、`logIds` 参数提交；后端限制单次最多 100 条，并重新校验资源归属、日志时间范围和 ID 合法性，原全量和单条下载接口保持兼容。隔离实例已验证单项 CSV 精确导出 1 条记录。
  - 数据库详情“服务器指标 / 服务器日志”页签的选中蓝色背景内文字已改为 flex 水平、垂直居中，消除 Element Plus 默认行高造成的文字偏移。
- 第四步新增前端代码后，`pnpm build` 已再次验证通过；构建过程中仅出现 Sass/Rollup 第三方依赖警告和 chunk 体积提示，无阻断错误。
- 数据台账联调时如详情、编辑、删除提示“不存在”，应优先检查是否为 Long 雪花 ID 被前端当 number 处理造成的精度丢失；当前项目已通过 Jackson Long 转字符串和前端 string ID 修复。
- 用户已完成数据台账联调测试：
  - 业务系统：查询、分页、新增、编辑、查看详情、删除均测试成功。
  - 分布式数据库：查询、分页、新增、编辑、查看详情、删除均测试成功。
  - 删除已被业务系统关联的数据库时提示不能删除，符合预期业务规则。
- 前端开发服务器曾成功启动在 `http://localhost:5173`。
- 如果 PowerShell 直接执行 `pnpm dev` 报 `pnpm` 无法识别，可在 `apps/admin-web` 目录执行 `.\run-dev.ps1`；当前脚本已兼容 Codex 内置 fallback pnpm 路径，或可安装并配置全局 pnpm。
- 为方便 Windows 启动前端，已新增项目根目录 `start-frontend.bat`，可双击启动前端开发服务。
- 用户已确认前端成功运行。
- `apps/admin-web/node_modules`、`apps/admin-web/dist`、`apps/api-server/target` 已加入 `.gitignore`。
- 如果后续重新打开终端，可能需要重新把 Node 或 JDK 加入 PATH。
- 设计文档中的部分页面在 Figma 原型中存在源码但未挂菜单，后续实现时需要统一菜单与路由。
- 本轮系统管理菜单与 Figma 原型对齐：动态侧栏的“系统管理”固定为用户账号管理、角色权限管理、日志审计管理、系统基础配置、安全登录管控五项，已通过真实管理员登录返回菜单树验证为 5 项。
  - 菜单权限管理已收纳到 `/system/roles` 的“菜单权限”页签；字典管理和系统配置已收纳到 `/system/config` 的对应页签。`/system/menus`、`/system/logs`、`/system/dicts`、`/system/configs` 保留兼容直达路由，但不再显示于侧栏。
  - 新增 `/system/security` 页面和 `GET /api/system/security/login-logs` 只读接口，使用 `system:security:view` 独立鉴权并复用真实登录日志数据，未创建假配置或复制审计查询逻辑。
  - `SystemBootstrapInitializer` 现在同步更新内置菜单结构。为符合 `sys_menu.permission_code` 唯一约束，字典和系统配置保留原有权限编码所属菜单 ID，只调整其层级、名称和路由，避免启动迁移时产生重复编码。
  - 本轮验证：后端 `mvn -DskipTests compile` 通过，前端 `pnpm build` 通过；18081 独立实例启动成功，管理员登录返回 5 项系统管理菜单，安全登录日志接口返回 `code = 200`、共 59 条记录。临时实例已停止，不影响现有 8080 服务。
- 本轮补充动态菜单会话同步：已确认运行中的 8080 登录接口实际返回 5 项新系统管理菜单；截图显示旧六项的原因是浏览器 localStorage 缓存未在后台布局启动时刷新。`MainLayout.vue` 现会调用 `/api/auth/me` 覆盖本地菜单、权限和用户资料，刷新页面即可自动获取最新动态菜单；会话失效时清理缓存并返回登录页。
- 本轮已建立可复现测试基线，执行顺序固定为：JUnit/Mockito 白盒测试与 JaCoCo 覆盖率 -> SonarQube 代码质量扫描 -> 黑盒接口测试 -> 前后端页面集成测试。后续功能开发必须遵循该顺序，并同步更新 `docs/testing/测试实施与质量报告.md`、`docs/standards/testing-standard.md`、`.ai-context/ai-test/`。
  - 1.0 历史基线：后端 `mvn verify` 已通过，JUnit 11 项，失败 0、错误 0；JaCoCo XML 已生成，项目行覆盖率 6.26%。当前测试结果以测试 2.0 记录为准。
  - 本机 SonarQube 9.9.6 已完成真实扫描：176 个生产 Java 文件、3 个测试 Java 文件，质量门禁 OK，覆盖率 5.8%，代码异味 66，重复行密度 20.1%，Bug 1、漏洞 3、安全热点 1。报告与风险必须如实保留，质量门禁 OK 不等于可直接生产上线。
  - 黑盒接口脚本 `docs/testing/api-blackbox-test.ps1` 已通过 14 项，覆盖登录、未认证 401、系统管理菜单、管理员查询、viewer/operator 权限边界；脚本避免成功写操作，结果保存在 `docs/testing/results/api-blackbox-result.json`。
  - 页面集成已验证管理员登录、系统管理五项动态菜单、角色权限管理、业务系统查看详情和安全登录管控页面；前端 `pnpm build` 通过。完整材料见 `docs/testing/测试实施与质量报告.md` 和 `docs/testing/frontend-integration-checklist.md`。
- 本轮已完成报告中高优先级 Bug 与安全风险整改：
  - 移除 `data.sql` 中的管理员弱密码种子及对既有管理员的覆盖更新；`SystemBootstrapInitializer` 仅在 `BOOTSTRAP_ADMIN_ENABLED=true` 且提供 `BOOTSTRAP_ADMIN_PASSWORD` 时创建不存在的管理员，永不重置既有密码或重新启用既有账号。历史 `DEFAULT_PASSWORD=password` 配置在启动时会安全清空。已有演示数据库的管理员密码不会被自动改写，重启后必须在个人中心完成一次密码修改。
  - `JWT_SECRET` 不再内置默认值，少于 32 位时后端拒绝启动；`scripts/dev-api.ps1` 会仅为当前开发进程生成随机密钥，长期部署必须显式配置密钥。
  - 数据库兼容初始化器已改为固定白名单 DDL，消除动态 SQL 热点；`LedgerDownload` 保持 record 对外结构，并改为字节内容比较与防御性复制，消除数组相等性 Bug。
  - 1.0 历史记录：已新增安全初始化、JWT 密钥、DDL 和下载对象测试，后端 `mvn verify` 通过 22 项测试；隔离 18081 实例启动和黑盒接口 14 项回归通过。
  - 1.0 历史 SonarQube 记录：质量门禁 OK，新增代码覆盖率 81.3%，全项目覆盖率 8.4%，Bugs 0、Vulnerabilities 0、Security Hotspots 0、Code Smells 65。完整记录见 `docs/testing/sonarqube-quality-report-2026-08-04.md`；当前测试 2.0 尚未形成新的 SonarQube 指标。
  - 已完成测试 2.0：新增 13 项白盒测试，`mvn verify` 为 35/35 通过，JaCoCo 行覆盖率由 6.26% 提升至 10.61%；新增独立黑盒脚本 `docs/testing/api-blackbox-test-2.0.ps1`，在 Nginx 部署入口 `http://localhost/api` 执行 50/50 通过，覆盖系统管理、AI 查询、台账详情、参数校验和 admin/operator/viewer 权限边界。黑盒脚本不保存密码，使用 `DIAGNOSIS_TEST_PASSWORD` 环境变量。SonarQube 2.0 复扫因本机扫描认证返回 401 暂未执行，必须由有权限的临时令牌恢复，不得绕过或重置认证。详见 `docs/testing/测试实施与质量报告-2.0.md`。
  - 已新增 `docs/testing/测试用例设计说明-2.0.md`：按白盒/黑盒、认证安全/系统管理/数据台账/AI 数据壳/角色权限模块完整列明 35 个白盒与 50 个黑盒用例的前置条件、操作、预期结果和后续补测方向，作为后续测试扩展与课程验收的统一用例依据。
  - 本轮已同步测试 2.0 结果至 `.ai-context/ai-project-manager`、`ai-backend`、`ai-test`、`ai-deploy` 与 `shared` 基线；前端本轮未修改代码或新增页面测试，`ai-frontend` 无内容变化。企业级工作流和文档维护规范已升级为硬性要求：每轮结束前必须检查全部角色上下文，受影响即更新，未受影响也记录，无需用户提醒。
- 本轮完成 Windows 本机初步部署：
  - 架构为 `浏览器 -> Nginx:80 -> Vue 静态文件`，`/api` 由 Nginx 代理至 `Spring Boot:18080`，后端连接本机 `MySQL80:3306`；部署端口与开发端口 `5173`、`8080` 隔离。
  - 已新增 `deploy/windows/` 的 Nginx 安装、构建、启动、停止、状态检查脚本及 `.env` 模板。真实数据库密码和 JWT 密钥仅存于被 Git 忽略的本机 `.env`，Nginx 代理端口从 `DEPLOY_API_PORT` 生成，避免配置漂移。
  - 本机构建已通过，`http://localhost`、`http://localhost/api/health` 和后端 `http://127.0.0.1:18080/api/health` 均返回 HTTP 200；经 Nginx 的黑盒接口回归 14/14 通过。已实测停止后清理 PID 文件和 `80`/`18080` 端口，再启动后恢复健康。详见 `deploy/windows/README.md`、`docs/testing/windows-local-deployment-report.md` 和 `docs/testing/results/deployment-api-blackbox-result.json`。
- 本轮完成 AI 故障知识库管理企业化实现：
  - `/fault-prediction/knowledge` 已从通用 AI 页面拆为 Figma 对齐专用页面：ID、名称、创建者、创建日期筛选，知识 ID/名称/创建者/创建时间/最新修改时间列表，右侧新增编辑抽屉与分页。
  - 真实附件能力已落地：新增 `kb_attachment` 记录真实文件元数据；知识仍统一使用 `kb_instance`。多附件上限 10 个，单个最大 20MB，服务端白名单、随机文件名、受控本地目录、图片内容校验、鉴权预览和下载均已实现。
  - 知识名称要求未删除记录唯一，知识编码由后端自动生成；编辑保存按保留附件清单执行逻辑删除，知识删除同时逻辑删除附件，物理文件保留供后续清理和恢复。原 JSON CRUD 接口保持兼容，新页面使用 multipart 表单接口。
  - 默认权限已对齐：`admin` 全权；`operator` 可查看/新增/编辑/下载但不可删除；`viewer` 仅查看/下载。启动初始化会收回预置 `operator` 的旧删除权限。
  - 本轮验证：后端全量 JUnit 38/38 通过，前端生产构建通过，Windows 本机部署已重新构建并启动，`http://localhost` 与 `http://127.0.0.1:18080/api/health` 均为 HTTP 200。模块说明见 `docs/modules/ai-fault-prediction/knowledge-base.md`。
- 本轮修复 `start-all.bat` 开发启动 500：`scripts/dev-api.ps1` 现会通过 `scripts/_common.ps1` 安全读取本机 `deploy/windows/.env` 中非空且非模板值的数据库、JWT、知识附件目录变量，不读取部署端口，也不把任何密码写入仓库。已用相同开发链路验证 `http://127.0.0.1:8080/api/health` 返回 HTTP 200，受保护知识库接口在未登录状态返回预期 HTTP 401 而非 500；临时验证进程已停止。
- 本轮修复故障知识库历史数据兼容问题：`kb_instance.created_by` 为空的旧演示记录在列表和详情映射时显示空创建者，不再因对 `Map.of()` 使用空键查询而触发 HTTP 500；`GlobalExceptionHandler` 保留未处理异常的服务端错误日志。新增列表/详情白盒回归测试，后续不得假定历史审计字段一定非空。
- 本轮完成故障知识库附件上传前端闭环：补齐按需注册遗漏的 `ElUpload` 及样式，上传区与 Figma 原型对齐为蓝色虚线拖拽面板；支持文件类型选择过滤、待上传文件队列、移除、附件数量上限和大小提示。后端 multipart 上传、受鉴权下载和逻辑删除接口保持不变。前端生产构建通过，开发版已实测真实上传、下载、逻辑删除均成功。
- 本轮完成故障预测任务配置：
  - `/fault-prediction/task` 已由通用 AI 页面替换为 Figma 对齐的专用任务配置页，支持任务名称、创建者、创建日期、模型筛选，任务详情、创建、编辑、逻辑删除和立即执行。
  - 抽屉支持真实业务系统/分布式数据库对象、已启用的故障预测模型、固定 22 项指标、多条每日/每周定时规则及 `1/2/4/12/24` 小时预测范围；对象类型切换会清空已选对象。
  - `ai_task` 仍是唯一任务主表，`schedule_rule` 保存 JSON 规则数组，`param_json` 保留未来 AI 引擎对接信息。故障预测的立即执行新增 `task_exec_record`，状态为 `PENDING`、结束时间为空，明确表示“已受理，等待 AI 引擎处理”，不伪造模型成功或预警结果。
  - 后端在保存时校验模型必选、模型存在且启用、模型类型、目标对象、指标、调度规则、任务编码；列表创建者使用批量用户查询回填，兼容历史空创建人。原有 `/ai/tasks` 接口路径、既有字段和其他 AI 模块业务逻辑保持兼容，仅新增可选的 `modelId`、`creatorName`、`createdDate` 查询参数。
  - 默认权限：`admin` 全权，`operator` 可查看/新增/编辑/立即执行但不可删除，`viewer` 仅查看；三角色当前均可查看全部任务。
  - 新增模块说明 `docs/modules/ai-fault-prediction/task-config.md`；后端 `mvn verify` 45/45 通过，JaCoCo 行覆盖率 15.81%，前端生产构建通过。真实 AI 推理、调度触发和预警产出仍为下一阶段引擎接入范围。
- 本轮补齐故障预测任务配置第 5-7 步：
  - 新增专用故障任务 DTO/VO、Service 和 Controller，接口为 `GET /ai/fault-prediction/tasks`、`GET /ai/fault-prediction/tasks/{id}`、`POST/PUT/DELETE /ai/fault-prediction/tasks/{id}`、`POST /ai/fault-prediction/tasks/{id}/execute`；旧 `/ai/tasks` 接口继续保留兼容。
  - 清理 `FaultPredictionTaskServiceImpl` 未使用的 `AiTaskMapper` 依赖，并为专用列表根路径增加精确 `GET` 权限规则；未修改既有任务表、请求响应兼容字段或核心业务语义。
  - 后端 `mvn verify` 结果为 12 个测试类、49/49 通过，失败 0、错误 0，JaCoCo 行覆盖率 17.37%（826/4756）；前端 `scripts/build-web.ps1` 构建通过。
  - 运行时验证：开发后端连接本机 MySQL 启动成功，`GET /api/health` 返回 200，未登录访问专用任务列表返回 401。登录态 CRUD、三角色写权限和浏览器视觉对照因本机未设置 `DIAGNOSIS_TEST_PASSWORD` 暂未执行，不能标记为通过；真实 AI 推理、调度触发和预警生成仍是后续引擎接入范围。
- 本轮完成故障智能预警企业化改造：
  - `/fault-prediction/alert` 已由通用 AI 页面替换为专用预警页面，列表支持故障 ID/对象、状态、风险等级、对象类型筛选；详情抽屉按原型实现故障信息、预测时间、置信度、趋势、说明、建议、状态及处理历史。
  - `fault_report` 保持唯一预警主表，`result_status_log` 保持唯一人工处理审计表；新增 `/api/ai/fault-prediction/alerts` 专用查询、详情、处理、删除接口，旧 `/api/ai/fault-reports` 保留兼容并供未来引擎接入。
  - 人工端无新增、编辑预警入口。状态机由服务层强制：`PENDING -> CONFIRMED/FALSE_POSITIVE`、`CONFIRMED -> CLOSED`，终态禁止再次处理；专用处理要求填写备注。
  - 权限已对齐：admin 可全部操作，operator 可查看和处理但不可删除，viewer 只读。启动初始化会收回 operator 的预警新增、编辑、删除权限。
  - 后端全量 `mvn verify` 52/52 通过，JaCoCo 行覆盖率 18.04%（879/4872）；前端生产构建通过；隔离实例健康检查 200、未登录专用列表 401。登录态三角色黑盒和浏览器视觉验收仍待测试密码，真实 AI 引擎写入仍留待后续接入。
- 本轮预警抽屉视觉修正：按原型重构为 584px 固定宽度，固定标题栏、独立滚动正文和固定底部操作栏；正文按预测对象、告警时间、指标类别、预测故障类型、预计发生时间、置信度、指标走势、预测说明、处理建议顺序展示，底部固定显示“确认 / 误报 / 关闭”。按钮按状态和权限禁用，后端状态机不变。前端 `scripts/build-web.ps1` 再次通过。
- 2026-08-06 预警抽屉首屏修复：详情接口已完整返回故障 ID、预测对象、告警时间、指标类别和预测故障类型；前端移除运行时字符串模板组件，改为页面内显式只读字段，确保字段稳定渲染在置信度和趋势图之前。抽屉在打开和动画结束时均重置正文滚动位置，避免再次打开时停留在后半段。前端 `scripts/build-web.ps1` 类型检查和生产构建通过；Figma MCP 因账号调用额度限制无法继续读取源码，登录态实际视觉验收仍待补。
- 2026-08-06 更新项目级工作流图：`docs/diagrams/vibe-coding-harness-workflow.png`（同时保留 `docs/diagrams/vibe-coding-harness-workflow.svg` 源文件）升级为五步详细拆解版。每一阶段均标明执行流程、输入、检查点、产出和关联技术栈：需求阶段使用 Markdown/Figma/数据库设计，规划阶段使用 Codex/.ai-context/Git，实现阶段使用 Spring Boot/MyBatis/MySQL/Vue，质量阶段使用 JUnit/Mockito/Playwright/SonarQube，部署阶段使用 Maven/Spring Boot Jar/Nginx/MySQL；验证失败必须回到对应阶段修复，随后再进入文档收口和下一轮迭代。
- 2026-08-06 角色权限管理页面完成 Figma 风格重构：`/system/roles` 改为左侧角色列表、右侧菜单/按钮/数据权限配置的固定双栏布局，保留新增、编辑、删除、菜单树父级补齐和数据范围保存等既有接口及权限边界；补齐 `ElRadio` 按需注册。前端生产构建通过，登录态视觉和角色写操作回归待独立测试账号补测。
- 2026-08-06 故障预测任务配置兼容性与原型布局修复：根因是早期演示任务的 `schedule_rule` 缺少 `predictionWindowHours`，编辑时前端显示 `undefined`，保存触发 DTO 必填校验并被统一显示为“请求参数不合法”。`FaultTaskConfigView.vue` 现对历史规则补齐默认 4 小时并规范化时间、周期、星期和规则 ID；`FaultTaskSaveRequest` 同步提供 4 小时服务端默认值，`data.sql` 演示规则补齐完整字段。定时预测设置按原型改为已添加规则列表 + 添加规则卡片，字段纵向全宽排列：时间、预测时间范围、每日/每周分段选择、添加按钮，创建者移动到末尾并标记只读。`GlobalExceptionHandler` 对校验异常返回首个具体校验消息。后端 `mvn test` 52/52 通过，前端 `scripts/build-web.ps1` 通过；接口和数据库结构未改变。
- 2026-08-06 定时预测设置宽度二次修正：为 `schedule-form-item` 显式设置表单内容区 `width: 100%`、`min-width: 0`，规则列表和添加规则卡片不再被 Element Plus 表单布局压缩成窄列；前端 `scripts/build-web.ps1` 再次通过。
- 2026-08-11 修复故障预测任务编辑时“任务编码不能为空”：专用 `FaultTaskSaveRequest` 按契约不包含也不应暴露 `taskCode`，但 `FaultPredictionTaskServiceImpl.update()` 之前重新构造任务实体时遗漏继承已有编码，触发通用任务服务的编码必填校验。现更新链路先读取原任务，再把原 `taskCode` 写入更新实体；新增白盒回归用例，验证前端不传编码的专用编辑请求仍可更新。后端全量 `mvn test` 53/53 通过；对外接口字段与前端请求结构不变。
- 2026-08-11 数据可视化台账“新增”已按 Figma 原型改为单一抽屉：通过顶部“业务系统 / 分布式数据库”页签选择资源表单，固定底部“取消 / 提交保存”。业务系统保留多数据库关联，数据库保留 IPv4 校验；原型未展示的健康状态不再显示，但仍按既有契约以新增默认 `NORMAL`、编辑保留原值提交。新增页签按 `ledger:business:create`、`ledger:database:create` 控制，编辑锁定原资源类型。未修改后端 API、数据库表、权限模型或部署结构；`scripts/build-web.ps1` 类型检查与生产构建通过，登录态新增保存回归待可写测试数据环境补测。
- 2026-08-11 完成测试 2.1 首轮交付：新增 20 条系统管理、AI 契约与通知服务白盒用例，后端 `mvn verify` 73/73 通过，JaCoCo 行覆盖率 21.05%（1029/4889）；前端生产构建通过。黑盒 `api-blackbox-test-2.1.ps1` 继承 2.0 的 50 条基线并扩展至 68 条，68/68 通过，逐条结果保存在 `docs/testing/results/api-blackbox-result-2.1.json`。SonarQube 当前未启动，成功写入和浏览器集成仍待隔离测试数据环境；完整报告为 `docs/testing/测试实施与质量报告-2.1.md`。
- 2026-08-11 新增测试资产索引 `docs/testing/测试代码与证据路径索引-2.1.md`：逐一对应 16 个白盒测试类、黑盒 1.0/2.0/2.1 脚本、Surefire、JaCoCo、构建脚本、页面清单及全部 2.1 交付文档，便于复跑和课程验收取证。
- 2026-08-11 修复数据台账分布式数据库详情演示数据为空：根因是 `db_instance_metric`、`db_instance_log` 的初始化时间停留在 2026-07-29，而页面默认查询最近 1 天；既有记录因 `INSERT IGNORE` 不会重新执行 `NOW()`。已仅刷新内置演示资源 `1900000000000000001` 的 15 条指标和关联 3 条日志时间戳，不修改用户台账或业务数据。真实接口回归：最近 1 天指标 15 条，CPU 45.2%、内存 67.8%、磁盘 1250 IOPS、缓存命中率 85%，日志 3 条，均返回业务码 200。
- 2026-08-11 AI 故障预测专项测试完成首轮：定向白盒覆盖知识库、任务、预警、执行记录和请求校验共 27/27 通过；新增完成执行结束时间和非故障任务边界测试。专项黑盒复用 2.1 已通过结果，覆盖知识库/专用任务/预警查询、非法筛选及 viewer/operator 拒绝写操作。交付 `docs/testing/ai-fault-prediction-test-cases-2.1.md`、`ai-fault-prediction-test-report-2.1.md`、`ai-fault-prediction-test-code-paths-2.1.md`；成功写入、附件物理文件与浏览器页面验收仍需隔离数据环境。
- 2026-08-11 AI 专项测试代码注释补齐：任务服务、专用任务服务、预警服务、知识库服务和 AI 请求校验测试均补充类级边界说明及方法级业务意图注释；注释修改后定向 JUnit 仍为 27/27 通过，未改变生产代码。
- 2026-08-11 按项目中文规范修正 AI 专项测试注释：五个测试类的类级、方法级注释已全部改为中文；复跑定向 JUnit 27/27 通过，构建失败信息仅为 PowerShell 工作目录下附加 `rg` 检查路径错误，不属于 Java 编译或测试失败。
- 2026-08-11 缺陷知识库管理完成专用页面升级：`/defect-detection/knowledge` 已从通用 `AiModuleView.vue` 切换为 `DefectKnowledgeView.vue`，实现原型范围内的列表筛选、分页、新增/编辑抽屉、删除确认和附件上传交互。附件上传、已有附件保留/删除、图片预览、文件下载和知识逻辑删除复用故障知识库的真实 `kb_instance`/`kb_attachment` 链路；所有缺陷新增/编辑请求固定提交 `knowledgeType=DEFECT`，列表固定按 `DEFECT` 查询，防止故障知识串入缺陷模块。前端 `vue-tsc --noEmit` 与生产构建通过；模块说明见 `docs/modules/ai-defect-detection/knowledge-base.md`。登录态写操作和浏览器集成回归待测试账号及可写环境补验。
- 2026-08-11 缺陷知识库后端类型细节修正：自动编码按知识类型生成 `FAULT-KB-`、`DEFECT-KB-`、`OPT-KB-` 前缀，新增/编辑/删除审计文案按类型标识；未修改对外接口、数据库结构或附件存储规则。后端 `mvn test` 75/75 通过。
- 2026-08-11 完成 AI 缺陷检测任务配置首轮改造：`/defect-detection/task` 已从 `AiModuleView.vue` 占位页切换为 `DefectTaskConfigView.vue`，实现原型范围内的列表筛选、分页、新增/编辑抽屉、详情抽屉、立即检测和删除。抽屉包含任务名称、检测对象、启用的缺陷检测模型、每日/每周定时规则、检测范围和创建者只读信息；任务继续使用 `ai_task` 与通用 AI 任务接口，固定 `taskType=DEFECT_DETECTION`。后端补充缺陷定时规则校验，立即检测改为创建 `task_exec_record(PENDING)` 等待未来引擎，不伪造成功结果。前端生产构建通过，后端 `mvn test` 77/77 通过；模块说明见 `docs/modules/ai-defect-detection/task-config.md`。Figma MCP 源码读取因 View seat 调用额度限制未完成，登录态页面和黑盒接口回归待可写测试环境补验。
- 2026-08-11 缺陷检测任务详情抽屉按用户补充原型修正：改为 580px 纵向只读表单，字段顺序固定为任务名称、检测对象、创建者、创建时间、任务次数、上一次检测时间、AI 检测模型、定时规则；使用禁用输入框、规则卡片、独立滚动正文和底部固定“关闭”按钮，移除原型没有的详情执行记录表格。详情接口和后端数据契约不变，前端生产构建再次通过。
- 2026-08-13 完成 AI 缺陷检测报表首轮改造：`/defect-detection/report` 已由通用 `AiModuleView.vue` 切换为 `DefectReportView.vue`，按原型实现缺陷 ID、任务名称、创建者、结果确认筛选，列表全选/多选、查看详情和批量导出。详情改为居中报告弹窗，展示任务摘要、缺陷问题、缺陷数据和缺陷描述，底部提供关闭和单份导出。后端保持 `/api/ai/defect-reports` 兼容并新增缺陷 ID、任务名称、创建者筛选、创建者批量回填及 `/api/ai/defect-reports/export` CSV 导出；导出限制 100 条、校验记录有效性、复用 `ai:defect:view` 并写审计。前端生产构建通过，后端 `mvn test` 77/77 通过；模块说明见 `docs/modules/ai-defect-detection/report-management.md`。登录态筛选和导出黑盒仍待测试环境补验。
- 2026-08-13 缺陷检测报表契约与回归修正：前端报表页原将“缺陷 ID”筛选以 `defectId` 发送，后端 `AiPageQuery` 实际接收 `defectCode`，导致筛选条件被忽略；现已统一 `AiQuery.defectCode` 与页面请求参数。新增空导出清单拒绝、创建者批量回填及 CSV 核心字段断言，后端 `mvn test` 79/79 通过，前端 `scripts/build-web.ps1` 通过。未修改数据库结构、既有接口入参出参或核心业务逻辑；登录态导出权限与页面交互仍需独立测试环境补验。
- 2026-08-13 完成 AI 性能优化知识库首轮专用页面改造：`/performance/knowledge` 已由通用 `AiModuleView.vue` 切换为 `OptimizationKnowledgeView.vue`，按原型实现知识 ID、名称、创建者、创建时间筛选，列表分页、编辑、删除、新增及新增/编辑抽屉。前端固定提交 `knowledgeType=OPTIMIZATION`，附件上传、保留、逻辑删除、图片预览和其他格式下载复用既有真实知识库链路；未新增表、文件存储机制或后端接口，`scripts/build-web.ps1` 通过。模块说明见 `docs/modules/ai-performance-optimization/knowledge-base.md`；登录态写操作与页面视觉验收待独立可写环境补验。
- 2026-08-13 完成 AI 性能优化任务配置和建议页面专用化：`/performance/task` 切换为 `OptimizationTaskConfigView.vue`，固定 `PERFORMANCE_OPTIMIZATION` 任务类型，新增模型/对象/每日每周规则抽屉、`OPT-TASK-` 服务端编码及 `PENDING` 等待优化引擎执行语义；`/performance/suggestion` 切换为 `OptimizationSuggestionView.vue`，按原型实现建议 ID、任务名称、创建者、状态筛选，多选/全选、真实 CSV 导出和居中详情处理弹窗。`opt_report` 仍为唯一建议事实表，后端补任务名称、创建者筛选、创建者批量回填和最多 100 条导出校验审计，未改变既有接口语义或数据库结构。模块说明见 `docs/modules/ai-performance-optimization/task-config.md` 与 `suggestion-management.md`。
- 2026-08-13 性能优化任务与建议最终验证：修复通用任务更新在前端不回传内部 `taskCode` 时会触发编码必填校验的问题，更新链路改为继承原编码；新增对应白盒回归。后端 `mvn test` 82/82 通过，前端 `scripts/build-web.ps1` 通过。构建仍只有既有第三方 Rollup/Sass 和大分包提示；登录态写入、建议导出和权限黑盒待独立可写环境补验。
- 2026-08-18 修复数据库资源详情页空状态和中等宽度错位：日志为空时不挂载 Element Plus 表格，仅显示“当前时间范围暂无服务器日志”；指标无趋势点时仅显示“暂无指标数据”，不渲染四图和缓存圆环；721-1300px 下摘要/指标卡片改为两列，日志表由内容区横向滚动承载。仅涉及前端 `DbResourceDetailView.vue` 与文档上下文，后端、数据库、部署结构不变；待 `scripts/build-web.ps1` 验证。
