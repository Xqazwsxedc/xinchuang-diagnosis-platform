<#
3.0 全量接口黑盒回归。

设计原则：在本机演示库中仅执行登录、查询、详情、筛选、参数拒绝和权限拒绝；
不执行会写入、删除或改变通知状态的成功请求。这样可验证对外契约且不污染演示数据。
密码只从参数或 DIAGNOSIS_TEST_PASSWORD 环境变量读取，结果文件绝不记录密码或令牌。
#>
param(
    [string]$BaseUrl = 'http://localhost:8080/api',
    [string]$ResultPath = "$PSScriptRoot/results/api-blackbox-result-3.0.json",
    [string]$TestPassword = $env:DIAGNOSIS_TEST_PASSWORD
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($TestPassword)) { throw '请先设置 DIAGNOSIS_TEST_PASSWORD。' }
$results = [System.Collections.Generic.List[object]]::new()

function Save-Result([string]$Status, [string]$ErrorMessage = $null) {
    $dir = Split-Path -Parent $ResultPath
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    [pscustomobject]@{
        status = $Status; version = '3.0'; startedAt = $(if ($results.Count) { $results[0].executedAt } else { $null })
        finishedAt = (Get-Date).ToString('o'); total = $results.Count
        passed = @($results | Where-Object passed).Count; failed = @($results | Where-Object { -not $_.passed }).Count
        error = $ErrorMessage; cases = $results
    } | ConvertTo-Json -Depth 12 | Set-Content -Encoding utf8 $ResultPath
}

function Invoke-Case {
    param([string]$Id, [string]$Name, [string]$Method, [string]$Path, [int]$ExpectedStatus = 200, [string]$Token = $null, [object]$Body = $null)
    $headers = @{}
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $args = @{ Uri = "$BaseUrl$Path"; Method = $Method; Headers = $headers; SkipHttpErrorCheck = $true }
    if ($null -ne $Body) { $args.ContentType = 'application/json'; $args.Body = $Body | ConvertTo-Json -Depth 10 -Compress }
    $response = Invoke-WebRequest @args
    $passed = $response.StatusCode -eq $ExpectedStatus
    $results.Add([pscustomobject]@{ id=$Id; name=$Name; method=$Method; path=$Path; expectedStatus=$ExpectedStatus; actualStatus=$response.StatusCode; passed=$passed; executedAt=(Get-Date).ToString('o') })
    if (-not $passed) { throw "$Id $Name：预期 HTTP $ExpectedStatus，实际 HTTP $($response.StatusCode)。" }
    if ([string]::IsNullOrWhiteSpace($response.Content)) { return $null }
    return $response.Content | ConvertFrom-Json
}

function Assert-Code([object]$Response, [int]$Expected, [string]$Id) {
    if ($null -eq $Response -or $Response.code -ne $Expected) { throw "$Id：预期业务码 $Expected，实际 $($Response.code)。" }
}
function Get-Token([string]$Username) {
    $body = Invoke-Case "BB30-AUTH-$Username" "$Username 登录" 'POST' '/auth/login' 200 $null @{ username=$Username; password=$TestPassword }
    Assert-Code $body 200 "BB30-AUTH-$Username"
    if ([string]::IsNullOrWhiteSpace($body.data.token)) { throw "$Username 登录未返回令牌。" }
    return $body.data.token
}
function Get-FirstId([object]$Page) { if ($Page -and $Page.data -and @($Page.data.records).Count -gt 0) { return @($Page.data.records)[0].id }; return $null }
function Invoke-ReadCases([array]$Cases, [string]$Token) {
    foreach ($case in $Cases) { $body = Invoke-Case $case.Id $case.Name 'GET' $case.Path 200 $Token; Assert-Code $body 200 $case.Id }
}

try {
    # 基线：健康检查、匿名拒绝与三种内置角色的真实登录。
    Invoke-Case 'BB30-BASE-01' '健康检查' 'GET' '/health' 200 | Out-Null
    Invoke-Case 'BB30-BASE-02' '匿名访问仪表盘被拒绝' 'GET' '/dashboard/overview' 401 | Out-Null
    Invoke-Case 'BB30-BASE-03' '匿名访问个人信息被拒绝' 'GET' '/auth/me' 401 | Out-Null
    $admin = Get-Token 'admin'; $operator = Get-Token 'operator'; $viewer = Get-Token 'viewer'
    $me = Invoke-Case 'BB30-AUTH-04' '管理员当前用户信息' 'GET' '/auth/me' 200 $admin; Assert-Code $me 200 'BB30-AUTH-04'
    $profile = Invoke-Case 'BB30-AUTH-05' '管理员个人中心资料' 'GET' '/auth/profile' 200 $admin; Assert-Code $profile 200 'BB30-AUTH-05'

    # 系统管理、仪表盘和个人通知的全部只读入口。
    Invoke-ReadCases @(
        @{Id='BB30-SYS-01';Name='仪表盘概览';Path='/dashboard/overview'},
        @{Id='BB30-SYS-02';Name='用户列表';Path='/system/users?pageNo=1&pageSize=10'},
        @{Id='BB30-SYS-03';Name='角色列表';Path='/system/roles?pageNo=1&pageSize=10'},
        @{Id='BB30-SYS-04';Name='菜单树';Path='/system/menus/tree'},
        @{Id='BB30-SYS-05';Name='系统配置列表';Path='/system/configs?pageNo=1&pageSize=10'},
        @{Id='BB30-SYS-06';Name='字典类型列表';Path='/system/dict/types?pageNo=1&pageSize=10'},
        @{Id='BB30-SYS-07';Name='字典项列表';Path='/system/dict/items?pageNo=1&pageSize=10'},
        @{Id='BB30-SYS-08';Name='登录日志';Path='/system/logs/login?pageNo=1&pageSize=10'},
        @{Id='BB30-SYS-09';Name='操作日志';Path='/system/logs/operation?pageNo=1&pageSize=10'},
        @{Id='BB30-SYS-10';Name='安全登录日志';Path='/system/security/login-logs?pageNo=1&pageSize=10'},
        @{Id='BB30-SYS-11';Name='通知列表';Path='/notifications?pageNo=1&pageSize=10'},
        @{Id='BB30-SYS-12';Name='未读通知数';Path='/notifications/unread-count'}
    ) $admin

    # 数据台账：列表、详情、指标、日志、搜索和参数校验。
    $businessPage = Invoke-Case 'BB30-LEDGER-01' '业务系统列表' 'GET' '/ledger/business-systems?pageNo=1&pageSize=10' 200 $admin; Assert-Code $businessPage 200 'BB30-LEDGER-01'
    $dbPage = Invoke-Case 'BB30-LEDGER-02' '分布式数据库列表' 'GET' '/ledger/db-resources?pageNo=1&pageSize=10' 200 $admin; Assert-Code $dbPage 200 'BB30-LEDGER-02'
    Invoke-ReadCases @(
        @{Id='BB30-LEDGER-03';Name='业务系统名称筛选';Path='/ledger/business-systems?pageNo=1&pageSize=10&name=%E8%B4%A2%E5%8A%A1'},
        @{Id='BB30-LEDGER-04';Name='数据库负责人筛选';Path='/ledger/db-resources?pageNo=1&pageSize=10&ownerName=%E7%AE%A1%E7%90%86%E5%91%98'}
    ) $admin
    $businessId = Get-FirstId $businessPage; $dbId = Get-FirstId $dbPage
    if ($businessId) { Invoke-ReadCases @(@{Id='BB30-LEDGER-05';Name='业务系统详情';Path="/ledger/business-systems/$businessId"}, @{Id='BB30-LEDGER-06';Name='业务数据记录';Path="/ledger/business-systems/$businessId/data-records"}) $admin }
    if ($dbId) { Invoke-ReadCases @(@{Id='BB30-LEDGER-07';Name='数据库详情';Path="/ledger/db-resources/$dbId"}, @{Id='BB30-LEDGER-08';Name='数据库一日指标';Path="/ledger/db-resources/$dbId/metrics?range=1D"}, @{Id='BB30-LEDGER-09';Name='数据库一日日志';Path="/ledger/db-resources/$dbId/logs?range=1D&pageNo=1&pageSize=10"}) $admin }
    if ($dbId) { $invalidMetrics = Invoke-Case 'BB30-LEDGER-10' '非法指标时间范围被拒绝' 'GET' "/ledger/db-resources/$dbId/metrics?range=30D" 200 $admin; Assert-Code $invalidMetrics 400 'BB30-LEDGER-10' }

    # 三类 AI 模块共用的模型、知识库、任务、执行记录与报表读取契约。
    Invoke-ReadCases @(
        @{Id='BB30-AI-01';Name='AI 总览';Path='/ai/overview'},
        @{Id='BB30-AI-02';Name='模型管理列表';Path='/ai/models?pageNo=1&pageSize=10'},
        @{Id='BB30-AI-03';Name='故障知识库隔离查询';Path='/ai/knowledge?knowledgeType=FAULT&pageNo=1&pageSize=10'},
        @{Id='BB30-AI-04';Name='缺陷知识库隔离查询';Path='/ai/knowledge?knowledgeType=DEFECT&pageNo=1&pageSize=10'},
        @{Id='BB30-AI-05';Name='优化知识库隔离查询';Path='/ai/knowledge?knowledgeType=OPTIMIZATION&pageNo=1&pageSize=10'},
        @{Id='BB30-AI-06';Name='通用任务列表';Path='/ai/tasks?pageNo=1&pageSize=10'},
        @{Id='BB30-AI-07';Name='故障预测任务列表';Path='/ai/fault-prediction/tasks?pageNo=1&pageSize=10'},
        @{Id='BB30-AI-08';Name='故障智能预警列表';Path='/ai/fault-prediction/alerts?pageNo=1&pageSize=10'},
        @{Id='BB30-AI-09';Name='缺陷检测任务列表';Path='/ai/tasks?pageNo=1&pageSize=10&taskType=DEFECT_DETECTION'},
        @{Id='BB30-AI-10';Name='性能优化任务列表';Path='/ai/tasks?pageNo=1&pageSize=10&taskType=PERFORMANCE_OPTIMIZATION'},
        @{Id='BB30-AI-11';Name='缺陷检测报表列表';Path='/ai/defect-reports?pageNo=1&pageSize=10'},
        @{Id='BB30-AI-12';Name='性能优化建议列表';Path='/ai/optimization-reports?pageNo=1&pageSize=10'},
        @{Id='BB30-AI-13';Name='故障报表兼容列表';Path='/ai/fault-reports?pageNo=1&pageSize=10'},
        @{Id='BB30-AI-14';Name='任务执行记录列表';Path='/ai/tasks/executions?pageNo=1&pageSize=10'}
    ) $admin
    $invalidTaskDate = Invoke-Case 'BB30-AI-15' '非法任务创建日期被拒绝' 'GET' '/ai/tasks?pageNo=1&pageSize=10&createdDate=2026-99-99' 200 $admin; Assert-Code $invalidTaskDate 400 'BB30-AI-15'
    $invalidAlert = Invoke-Case 'BB30-AI-16' '非法预警状态被拒绝' 'GET' '/ai/fault-prediction/alerts?pageNo=1&pageSize=10&processStatus=UNKNOWN' 200 $admin; Assert-Code $invalidAlert 400 'BB30-AI-16'

    # 使用列表实际返回的演示数据验证详情；为空即为演示数据覆盖缺口，在文档中如实列出。
    $defectPage = Invoke-Case 'BB30-AI-17' '缺陷报表详情前置列表' 'GET' '/ai/defect-reports?pageNo=1&pageSize=10' 200 $admin; Assert-Code $defectPage 200 'BB30-AI-17'
    $optPage = Invoke-Case 'BB30-AI-18' '优化建议详情前置列表' 'GET' '/ai/optimization-reports?pageNo=1&pageSize=10' 200 $admin; Assert-Code $optPage 200 'BB30-AI-18'
    $faultTasks = Invoke-Case 'BB30-AI-19' '故障任务详情前置列表' 'GET' '/ai/fault-prediction/tasks?pageNo=1&pageSize=10' 200 $admin; Assert-Code $faultTasks 200 'BB30-AI-19'
    $defectId = Get-FirstId $defectPage; $optId = Get-FirstId $optPage; $faultTaskId = Get-FirstId $faultTasks
    if ($defectId) { Invoke-ReadCases @(@{Id='BB30-AI-20';Name='缺陷报表详情';Path="/ai/defect-reports/$defectId"}) $admin }
    if ($optId) { Invoke-ReadCases @(@{Id='BB30-AI-21';Name='性能优化建议详情';Path="/ai/optimization-reports/$optId"}) $admin }
    if ($faultTaskId) { Invoke-ReadCases @(@{Id='BB30-AI-22';Name='故障任务配置详情';Path="/ai/fault-prediction/tasks/$faultTaskId"}) $admin }

    # 权限边界：viewer 只读，operator 不得删除，且所有拒绝请求不产生数据变化。
    Invoke-ReadCases @(
        @{Id='BB30-PERM-01';Name='查看者台账列表';Path='/ledger/business-systems?pageNo=1&pageSize=10'},
        @{Id='BB30-PERM-02';Name='查看者故障知识库';Path='/ai/knowledge?knowledgeType=FAULT&pageNo=1&pageSize=10'},
        @{Id='BB30-PERM-03';Name='查看者缺陷报表';Path='/ai/defect-reports?pageNo=1&pageSize=10'},
        @{Id='BB30-PERM-04';Name='查看者性能优化建议';Path='/ai/optimization-reports?pageNo=1&pageSize=10'}
    ) $viewer
    foreach ($case in @(
        @{Id='BB30-PERM-05';Name='查看者不能新增用户';Method='POST';Path='/system/users'},
        @{Id='BB30-PERM-06';Name='查看者不能新增业务系统';Method='POST';Path='/ledger/business-systems'},
        @{Id='BB30-PERM-07';Name='查看者不能新增故障任务';Method='POST';Path='/ai/fault-prediction/tasks'},
        @{Id='BB30-PERM-08';Name='查看者不能处理预警';Method='PATCH';Path='/ai/fault-prediction/alerts/1/handle'},
        @{Id='BB30-PERM-09';Name='操作员不能删除故障任务';Method='DELETE';Path='/ai/fault-prediction/tasks/1'},
        @{Id='BB30-PERM-10';Name='操作员不能删除缺陷报表';Method='DELETE';Path='/ai/defect-reports/1'},
        @{Id='BB30-PERM-11';Name='操作员不能删除优化建议';Method='DELETE';Path='/ai/optimization-reports/1'}
    )) { $token = if ($case.Id -like '*10' -or $case.Id -like '*11' -or $case.Id -like '*09') { $operator } else { $viewer }; Invoke-Case $case.Id $case.Name $case.Method $case.Path 403 $token @{} | Out-Null }

    Save-Result 'PASSED'
    Write-Host "API 黑盒 3.0 通过：$($results.Count) 条。结果：$ResultPath"
} catch {
    Save-Result 'FAILED' $_.Exception.Message
    throw
}
