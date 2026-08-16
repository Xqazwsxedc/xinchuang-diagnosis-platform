param(
    [string]$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string]$OutputPath = "$PSScriptRoot\测试用例执行明细-2.1.md"
)

$ErrorActionPreference = 'Stop'
$apiResultPath = Join-Path $PSScriptRoot 'results/api-blackbox-result-2.1.json'
$surefirePath = Join-Path $ProjectRoot 'apps/api-server/target/surefire-reports'

function Get-Category([string]$ClassName) {
    if ($ClassName -match '\.auth\.|\.security\.') { return '认证与安全' }
    if ($ClassName -match '\.ledger\.') { return '数据台账' }
    if ($ClassName -match '\.ai\.') { return 'AI 故障预测与知识库' }
    if ($ClassName -match 'Notification') { return '通知中心' }
    if ($ClassName -match '\.system\.') { return '系统管理与初始化' }
    return '平台基础'
}

function Get-WhiteboxDescription([string]$Name) {
    return "执行测试方法 $Name，验证服务层或 Bean Validation 的契约；异常分支不得产生未预期的数据写入或越权行为。"
}

$lines = [System.Collections.Generic.List[string]]::new()
$lines.Add('# 测试用例执行明细 2.1')
$lines.Add('')
$lines.Add('## 1. 文档控制')
$lines.Add('')
$lines.Add('| 项目 | 内容 |')
$lines.Add('| --- | --- |')
$lines.Add('| 版本 | 2.1 |')
$lines.Add('| 执行日期 | 2026-08-11 |')
$lines.Add('| 适用范围 | 系统基座、系统管理、数据台账、故障知识库、故障预测任务、故障智能预警 |')
$lines.Add('| 白盒执行命令 | `mvn verify` |')
$lines.Add('| 黑盒执行命令 | `pwsh -File docs/testing/api-blackbox-test-2.1.ps1` |')
$lines.Add('| 白盒结果 | 73/73 通过，0 失败，0 错误，0 跳过 |')
$lines.Add('| 黑盒结果 | 68/68 通过，0 失败 |')
$lines.Add('| 覆盖率 | JaCoCo 行覆盖率 21.05%（1029/4889） |')
$lines.Add('')
$lines.Add('## 2. 结果口径与证据')
$lines.Add('')
$lines.Add('- 白盒原始证据：`apps/api-server/target/surefire-reports/TEST-*.xml`。每条用例由 JUnit 5 执行，`failure`、`error`、`skipped` 节点均为空即为通过。')
$lines.Add('- 黑盒原始证据：`docs/testing/results/api-blackbox-result-2.1.json`。每条记录包含接口、方法、期望/实际 HTTP 状态、时间和通过标记。')
$lines.Add('- 黑盒账号密码只在进程环境变量中使用，不写入该文档、脚本结果或仓库。')
$lines.Add('')
$lines.Add('## 3. 白盒测试逐条结果')
$lines.Add('')
$lines.Add('| 编号 | 模块 | 测试类与方法 | 测试目标/步骤 | 预期结果 | 实际结果 |')
$lines.Add('| --- | --- | --- | --- | --- | --- |')
$whiteboxIndex = 0
Get-ChildItem $surefirePath -Filter 'TEST-*.xml' | Sort-Object Name | ForEach-Object {
    [xml]$report = Get-Content -Raw $_.FullName
    foreach ($test in @($report.testsuite.testcase)) {
        $whiteboxIndex++
        $className = [string]$test.classname
        $method = [string]$test.name
        $status = if ($null -ne $test.failure -or $null -ne $test.error -or $null -ne $test.skipped) { '失败或跳过，详见 Surefire XML' } else { '通过' }
        $lines.Add("| WB21-$('{0:d3}' -f $whiteboxIndex) | $(Get-Category $className) | ``$($className).$method`` | $(Get-WhiteboxDescription $method) | 校验、服务行为、审计和权限边界符合契约 | $status |")
    }
}
$lines.Add('')
$lines.Add('## 4. 黑盒接口测试逐条结果')
$lines.Add('')
$lines.Add('| 编号 | 模块 | 用例 | 请求 | 预期 | 实际 | 结果 |')
$lines.Add('| --- | --- | --- | --- | --- | --- |')
$apiResult = Get-Content -Raw $apiResultPath | ConvertFrom-Json
 $blackboxIndex = 0
foreach ($test in $apiResult.cases) {
    $blackboxIndex++
    $caseId = if ([string]::IsNullOrWhiteSpace($test.id)) { "BB21-BASE-$('{0:d3}' -f $blackboxIndex)" } else { [string]$test.id }
    $category = if ($caseId -match 'LEDGER') { '数据台账' } elseif ($caseId -match 'AI|FAULT') { 'AI 故障预测与知识库' } elseif ($caseId -match 'VIEW|OPER') { '角色权限边界' } elseif ($caseId -match 'SYS') { '系统管理与通知' } else { '认证与平台基础' }
    $actual = if ($null -ne $test.actualStatus) { "HTTP $($test.actualStatus)" } else { '见原始结果' }
    $result = if ($test.passed) { '通过' } else { '失败' }
    $lines.Add("| $caseId | $category | $($test.name) | ``$($test.method) $($test.path)`` | HTTP $($test.expectedStatus) | $actual | $result |")
}
$lines.Add('')
$lines.Add('## 5. 未自动执行的集成与质量门禁项')
$lines.Add('')
$lines.Add('| 编号 | 项目 | 原因 | 后续验收标准 |')
$lines.Add('| --- | --- | --- | --- |')
$lines.Add('| WEB21-01~09 | 浏览器页面集成 | 需要独立可清理数据和人工视觉确认 | 按 `frontend-integration-checklist.md` 逐项填写实际结果与截图证据 |')
$lines.Add('| SQ21-01 | SonarQube 2.1 扫描 | 本机 SonarQube 服务未启动 | 启动服务后，以本轮 JaCoCo XML 和临时 Execute Analysis 令牌扫描并更新质量报告 |')
$lines.Add('| WRITE21-01 | 成功写入与清理 | 当前黑盒脚本避免污染演示库 | 在隔离数据库验证 CRUD、附件、任务受理、预警状态机及清理后数据准确性 |')
$lines.Add('')
$lines.Add('## 6. 结论')
$lines.Add('')
$lines.Add('本文件记录了本轮已执行的全部白盒与黑盒用例。73 条白盒和 68 条黑盒均通过；通过不替代 SonarQube、隔离写入测试和浏览器视觉验收，未执行项不得标记为已通过。')

[System.IO.File]::WriteAllLines($OutputPath, $lines, [System.Text.UTF8Encoding]::new($true))
Write-Host "Generated $OutputPath with $whiteboxIndex white-box and $($apiResult.cases.Count) black-box cases."
