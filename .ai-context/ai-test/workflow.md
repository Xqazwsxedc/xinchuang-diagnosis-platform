# Workflow

1. 根据需求和模块文档列出验收项。
2. 区分自动验证、接口验证、页面手工验证。
3. 覆盖 admin、operator、viewer 三类角色。
4. 新增后端核心逻辑先补 JUnit/Mockito 单元测试，再执行 `mvn verify` 生成 JaCoCo 报告。
5. 在真实覆盖率报告生成后执行 SonarQube 扫描；风险不得因为质量门禁通过而省略。
6. 新增接口必须加入冒烟或 `api-blackbox-test.ps1`，并包含成功、未登录或无权限、参数错误等适用场景。
7. 涉及页面、路由或动态菜单时，更新页面集成验收清单并进行真实浏览器验证。
8. 更新测试报告、测试规范、`PROJECT_SPEC.md` 和本 AI 上下文。
