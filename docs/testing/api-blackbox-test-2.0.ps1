param(
    [string]$BaseUrl = 'http://localhost:8080/api',
    [string]$ResultPath = "$PSScriptRoot/results/api-blackbox-result-2.0.json",
    [string]$TestPassword = $env:DIAGNOSIS_TEST_PASSWORD
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($TestPassword)) {
    throw 'Set DIAGNOSIS_TEST_PASSWORD for the local test accounts before running this script.'
}
$results = [System.Collections.Generic.List[object]]::new()

function Invoke-ApiRequest {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Path,
        [int]$ExpectedStatus,
        [string]$Token,
        [object]$Body
    )
    $headers = @{}
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $parameters = @{ Uri = "$BaseUrl$Path"; Method = $Method; Headers = $headers; SkipHttpErrorCheck = $true }
    if ($null -ne $Body) {
        $parameters.ContentType = 'application/json'
        $parameters.Body = $Body | ConvertTo-Json -Depth 8 -Compress
    }
    $response = Invoke-WebRequest @parameters
    $passed = $response.StatusCode -eq $ExpectedStatus
    $results.Add([pscustomobject]@{
        name = $Name; method = $Method; path = $Path; expectedStatus = $ExpectedStatus
        actualStatus = $response.StatusCode; passed = $passed; executedAt = (Get-Date).ToString('o')
    })
    if (-not $passed) { throw "$Name failed: expected HTTP $ExpectedStatus but received $($response.StatusCode)." }
    if ([string]::IsNullOrWhiteSpace($response.Content)) { return $null }
    return $response.Content | ConvertFrom-Json
}

function Get-Token([string]$Username) {
    $response = Invoke-ApiRequest -Name "$Username login" -Method 'POST' -Path '/auth/login' -ExpectedStatus 200 -Body @{ username = $Username; password = $TestPassword }
    if ($response.code -ne 200 -or [string]::IsNullOrWhiteSpace($response.data.token)) { throw "$Username login did not return a usable token." }
    return $response.data.token
}

function Assert-BusinessSuccess($Response, [string]$Name) {
    if ($null -eq $Response -or $Response.code -ne 200) { throw "$Name did not return business code 200." }
}

function Assert-BusinessCode($Response, [int]$ExpectedCode, [string]$Name) {
    if ($null -eq $Response -or $Response.code -ne $ExpectedCode) {
        throw "$Name did not return business code $ExpectedCode."
    }
}

try {
    Invoke-ApiRequest 'Health check' 'GET' '/health' 200 | Out-Null
    Invoke-ApiRequest 'Unauthenticated dashboard is rejected' 'GET' '/dashboard/overview' 401 | Out-Null
    Invoke-ApiRequest 'Unauthenticated current-user is rejected' 'GET' '/auth/me' 401 | Out-Null

    $adminToken = Get-Token 'admin'
    $adminMe = Invoke-ApiRequest 'Admin current-user data' 'GET' '/auth/me' 200 $adminToken
    Assert-BusinessSuccess $adminMe 'Admin current-user data'
    if ($adminMe.data.username -ne 'admin') { throw 'Admin current-user data is inconsistent.' }
    $systemMenu = $adminMe.data.menus | Where-Object { $_.menuName -eq '系统管理' } | Select-Object -First 1
    if ($null -eq $systemMenu -or @($systemMenu.children).Count -ne 5) { throw 'System management menu must contain five entries.' }

    foreach ($case in @(
        @{ Name = 'Admin dashboard'; Path = '/dashboard/overview' },
        @{ Name = 'Admin users'; Path = '/system/users?pageNo=1&pageSize=10' },
        @{ Name = 'Admin roles'; Path = '/system/roles?pageNo=1&pageSize=10' },
        @{ Name = 'Admin security logs'; Path = '/system/security/login-logs?pageNo=1&pageSize=10' },
        @{ Name = 'Admin config list'; Path = '/system/configs?pageNo=1&pageSize=10' },
        @{ Name = 'Admin dictionary types'; Path = '/system/dict/types?pageNo=1&pageSize=10' },
        @{ Name = 'Admin menu tree'; Path = '/system/menus/tree' },
        @{ Name = 'Admin login logs'; Path = '/system/logs/login?pageNo=1&pageSize=10' },
        @{ Name = 'Admin operation logs'; Path = '/system/logs/operation?pageNo=1&pageSize=10' },
        @{ Name = 'Admin notifications'; Path = '/notifications?pageNo=1&pageSize=10' },
        @{ Name = 'Admin unread count'; Path = '/notifications/unread-count' },
        @{ Name = 'AI overview'; Path = '/ai/overview' },
        @{ Name = 'AI models'; Path = '/ai/models?pageNo=1&pageSize=10' },
        @{ Name = 'AI knowledge'; Path = '/ai/knowledge?pageNo=1&pageSize=10' },
        @{ Name = 'AI tasks'; Path = '/ai/tasks?pageNo=1&pageSize=10' },
        @{ Name = 'AI fault reports'; Path = '/ai/fault-reports?pageNo=1&pageSize=10' },
        @{ Name = 'AI defect reports'; Path = '/ai/defect-reports?pageNo=1&pageSize=10' },
        @{ Name = 'AI optimization reports'; Path = '/ai/optimization-reports?pageNo=1&pageSize=10' }
    )) { Assert-BusinessSuccess (Invoke-ApiRequest $case.Name 'GET' $case.Path 200 $adminToken) $case.Name }

    $businessPage = Invoke-ApiRequest 'Admin business-system list' 'GET' '/ledger/business-systems?pageNo=1&pageSize=10' 200 $adminToken
    $dbPage = Invoke-ApiRequest 'Admin database-resource list' 'GET' '/ledger/db-resources?pageNo=1&pageSize=10' 200 $adminToken
    Assert-BusinessSuccess $businessPage 'Admin business-system list'
    Assert-BusinessSuccess $dbPage 'Admin database-resource list'
    $businessId = @($businessPage.data.records)[0].id
    $dbId = @($dbPage.data.records)[0].id
    if ($null -eq $businessId -or $null -eq $dbId) { throw 'Ledger demo data is missing.' }

    foreach ($case in @(
        @{ Name = 'Business-system detail'; Path = "/ledger/business-systems/$businessId" },
        @{ Name = 'Business data records'; Path = "/ledger/business-systems/$businessId/data-records" },
        @{ Name = 'Database-resource detail'; Path = "/ledger/db-resources/$dbId" },
        @{ Name = 'Database metrics 1D'; Path = "/ledger/db-resources/$dbId/metrics?range=1D" },
        @{ Name = 'Database logs 1D'; Path = "/ledger/db-resources/$dbId/logs?range=1D&pageNo=1&pageSize=10" }
    )) { Assert-BusinessSuccess (Invoke-ApiRequest $case.Name 'GET' $case.Path 200 $adminToken) $case.Name }
    Assert-BusinessCode (Invoke-ApiRequest 'Invalid metric range rejected' 'GET' "/ledger/db-resources/$dbId/metrics?range=30D" 200 $adminToken) 400 'Invalid metric range rejected'
    Assert-BusinessCode (Invoke-ApiRequest 'Invalid log level rejected' 'GET' "/ledger/db-resources/$dbId/logs?range=1D&logLevel=DEBUG" 200 $adminToken) 400 'Invalid log level rejected'
    Assert-BusinessCode (Invoke-ApiRequest 'Invalid business-system save rejected' 'POST' '/ledger/business-systems' 200 $adminToken -Body @{}) 400 'Invalid business-system save rejected'
    Assert-BusinessCode (Invoke-ApiRequest 'Invalid database-resource save rejected' 'POST' '/ledger/db-resources' 200 $adminToken -Body @{ name = ''; publicIp = '999.1.1.1'; ownerName = ''; status = 'UNKNOWN' }) 400 'Invalid database-resource save rejected'

    $viewerToken = Get-Token 'viewer'
    foreach ($case in @(
        @{ Name = 'Viewer dashboard'; Path = '/dashboard/overview' },
        @{ Name = 'Viewer business-system list'; Path = '/ledger/business-systems?pageNo=1&pageSize=10' },
        @{ Name = 'Viewer database-resource list'; Path = '/ledger/db-resources?pageNo=1&pageSize=10' },
        @{ Name = 'Viewer business detail'; Path = "/ledger/business-systems/$businessId" },
        @{ Name = 'Viewer database metrics'; Path = "/ledger/db-resources/$dbId/metrics?range=1D" },
        @{ Name = 'Viewer database logs'; Path = "/ledger/db-resources/$dbId/logs?range=1D&pageNo=1&pageSize=10" }
    )) { Assert-BusinessSuccess (Invoke-ApiRequest $case.Name 'GET' $case.Path 200 $viewerToken) $case.Name }
    Invoke-ApiRequest 'Viewer cannot create business system' 'POST' '/ledger/business-systems' 403 $viewerToken -Body @{} | Out-Null
    Invoke-ApiRequest 'Viewer cannot create database resource' 'POST' '/ledger/db-resources' 403 $viewerToken -Body @{} | Out-Null
    Invoke-ApiRequest 'Viewer cannot view users' 'GET' '/system/users?pageNo=1&pageSize=10' 403 $viewerToken | Out-Null
    Invoke-ApiRequest 'Viewer cannot view roles' 'GET' '/system/roles?pageNo=1&pageSize=10' 403 $viewerToken | Out-Null
    Invoke-ApiRequest 'Viewer cannot view system config' 'GET' '/system/configs?pageNo=1&pageSize=10' 403 $viewerToken | Out-Null

    $operatorToken = Get-Token 'operator'
    Assert-BusinessSuccess (Invoke-ApiRequest 'Operator business-system list' 'GET' '/ledger/business-systems?pageNo=1&pageSize=10' 200 $operatorToken) 'Operator business-system list'
    Invoke-ApiRequest 'Operator cannot view users' 'GET' '/system/users?pageNo=1&pageSize=10' 403 $operatorToken | Out-Null
    Invoke-ApiRequest 'Operator cannot view roles' 'GET' '/system/roles?pageNo=1&pageSize=10' 403 $operatorToken | Out-Null

    $directory = Split-Path -Parent $ResultPath
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    [pscustomobject]@{ status = 'PASSED'; startedAt = $results[0].executedAt; finishedAt = (Get-Date).ToString('o'); total = $results.Count; passed = @($results | Where-Object passed).Count; failed = 0; cases = $results } |
        ConvertTo-Json -Depth 8 | Set-Content -Encoding utf8 $ResultPath
    Write-Host "API black-box 2.0 testing passed: $($results.Count) cases. Result: $ResultPath"
} catch {
    $directory = Split-Path -Parent $ResultPath
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    [pscustomobject]@{ status = 'FAILED'; finishedAt = (Get-Date).ToString('o'); total = $results.Count; passed = @($results | Where-Object passed).Count; failed = 1; error = $_.Exception.Message; cases = $results } |
        ConvertTo-Json -Depth 8 | Set-Content -Encoding utf8 $ResultPath
    throw
}
