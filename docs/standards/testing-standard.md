# Testing Standard

## Baseline Commands

后端：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-api.ps1
```

前端：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-web.ps1
```

全量：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verify-all.ps1
```

## Minimum Verification

后端改动：

- 编译通过。
- DTO 校验编译通过。
- 涉及 SQL 时启动后端或检查兼容初始化器。

前端改动：

- `pnpm build` 通过。
- 表单规则和 API 类型编译通过。

权限改动：

- admin 可操作。
- operator/viewer 按权限限制。
- 修改角色权限后重新登录或刷新用户信息。

接口改动：

- 更新 `.http` 冒烟测试。
- 记录 token 获取方式。
- 验证成功和失败场景。

## Manual Testing

不能自动验证的页面交互必须写入验收清单，例如：

- 抽屉开关。
- 删除确认。
- 权限按钮是否隐藏。
- 通知角标。
- 个人中心头像预览。

## Automated Testing Baseline

- 后端单元测试统一使用 JUnit 5 与 Mockito；执行 `mvn verify`，不得只执行编译。
- JaCoCo 在 `verify` 阶段生成 `apps/api-server/target/site/jacoco/jacoco.xml`，新增后端核心逻辑必须补充正向、异常或权限分支测试。
- SonarQube 扫描必须在 `mvn verify` 后执行，以导入真实 Surefire 与 JaCoCo 报告。扫描令牌仅可临时使用，禁止写入代码、脚本、文档和提交记录。
- 黑盒接口基线使用 `docs/testing/api-blackbox-test.ps1`，结果写入 `docs/testing/results/api-blackbox-result.json`。脚本不得执行会污染演示数据库的成功写操作。
- 测试 2.0 使用 `docs/testing/api-blackbox-test-2.0.ps1`，测试账号密码必须由 `DIAGNOSIS_TEST_PASSWORD` 环境变量提供；结果写入 `docs/testing/results/*-2.0.json`。2.0 脚本可扩展只读查询、非法参数和权限拦截用例，但同样不得执行成功写操作或成功下载审计。
- 页面集成验收清单维护在 `docs/testing/frontend-integration-checklist.md`；至少验证登录、菜单或路由、关键页面数据加载和权限边界。
- 测试完成后更新 `docs/testing/测试实施与质量报告.md`、`PROJECT_SPEC.md` 与 `.ai-context/ai-test/`。质量门禁通过不等于没有风险，漏洞、热点、覆盖率和重复率必须如实记录。

## Windows 本机部署验证

- 部署改动必须先运行 `deploy/windows/build.ps1`，再运行 `deploy/windows/start.ps1`。
- 必须依次验证后端 `http://127.0.0.1:18080/api/health`、前端 `http://localhost` 和 Nginx 代理 `http://localhost/api/health` 均返回 HTTP 200。
- 必须使用 `docs/testing/api-blackbox-test.ps1 -BaseUrl 'http://localhost/api'` 经 Nginx 回归核心接口和权限边界，结果另存为部署结果文件。
- 部署验收完成后更新 `docs/testing/windows-local-deployment-report.md`、`PROJECT_SPEC.md` 和 `.ai-context/ai-deploy/`；不得记录 `.env` 中的秘密信息。

## Security Startup Rules

- 禁止在 Java 代码、SQL 演示数据、配置默认值或文档中保留可直接使用的管理员密码或 JWT 密钥。
- 管理员引导只能通过环境变量显式开启，且仅允许创建不存在的账号；启动过程不得重置既有用户密码、状态或角色。
- JWT 密钥必须由部署环境提供并至少 32 位；开发脚本生成的临时密钥不得用于长期部署。
- 启动兼容 SQL 的表名、列名和 DDL 必须来自代码内固定白名单，禁止拼接任何外部输入。
