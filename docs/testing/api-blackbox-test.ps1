param(
    [string]$BaseUrl = 'http://localhost:8080/api',
    [string]$ResultPath = "$PSScriptRoot/results/api-blackbox-result.json"
)

$ErrorActionPreference = 'Stop'
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
    if ($Token) {
        $headers.Authorization = "Bearer $Token"
    }
    $parameters = @{ Uri = "$BaseUrl$Path"; Method = $Method; Headers = $headers; SkipHttpErrorCheck = $true }
    if ($null -ne $Body) {
        $parameters.ContentType = 'application/json'
        $parameters.Body = $Body | ConvertTo-Json -Depth 8 -Compress
    }
    $response = Invoke-WebRequest @parameters
    $passed = $response.StatusCode -eq $ExpectedStatus
    $results.Add([pscustomobject]@{
        name = $Name
        method = $Method
        path = $Path
        expectedStatus = $ExpectedStatus
        actualStatus = $response.StatusCode
        passed = $passed
        executedAt = (Get-Date).ToString('o')
    })
    if (-not $passed) {
        throw "$Name failed: expected HTTP $ExpectedStatus but received $($response.StatusCode)."
    }
    if ([string]::IsNullOrWhiteSpace($response.Content)) {
        return $null
    }
    return $response.Content | ConvertFrom-Json
}

function Get-Token {
    param([string]$Username)
    $response = Invoke-ApiRequest -Name "$Username login" -Method 'POST' -Path '/auth/login' -ExpectedStatus 200 -Body @{ username = $Username; password = 'password' }
    if ($response.code -ne 200 -or [string]::IsNullOrWhiteSpace($response.data.token)) {
        throw "$Username login did not return a usable token."
    }
    return $response.data.token
}

try {
    Invoke-ApiRequest -Name 'Health check' -Method 'GET' -Path '/health' -ExpectedStatus 200 | Out-Null
    Invoke-ApiRequest -Name 'Unauthenticated dashboard request is rejected' -Method 'GET' -Path '/dashboard/overview' -ExpectedStatus 401 | Out-Null

    $adminToken = Get-Token -Username 'admin'
    $adminMe = Invoke-ApiRequest -Name 'Admin current-user data' -Method 'GET' -Path '/auth/me' -ExpectedStatus 200 -Token $adminToken
    if ($adminMe.data.username -ne 'admin') { throw 'Admin current-user data is inconsistent.' }
    $systemMenu = $adminMe.data.menus | Where-Object { $_.menuName -eq '系统管理' } | Select-Object -First 1
    if ($null -eq $systemMenu -or @($systemMenu.children).Count -ne 5) { throw 'System management menu must contain five entries.' }

    Invoke-ApiRequest -Name 'Admin user list' -Method 'GET' -Path '/system/users?pageNo=1&pageSize=10' -ExpectedStatus 200 -Token $adminToken | Out-Null
    Invoke-ApiRequest -Name 'Admin role list' -Method 'GET' -Path '/system/roles?pageNo=1&pageSize=10' -ExpectedStatus 200 -Token $adminToken | Out-Null
    Invoke-ApiRequest -Name 'Admin security login-log list' -Method 'GET' -Path '/system/security/login-logs?pageNo=1&pageSize=10' -ExpectedStatus 200 -Token $adminToken | Out-Null
    Invoke-ApiRequest -Name 'Admin business-system ledger list' -Method 'GET' -Path '/ledger/business-systems?pageNo=1&pageSize=10' -ExpectedStatus 200 -Token $adminToken | Out-Null

    $viewerToken = Get-Token -Username 'viewer'
    Invoke-ApiRequest -Name 'Viewer business-system ledger list' -Method 'GET' -Path '/ledger/business-systems?pageNo=1&pageSize=10' -ExpectedStatus 200 -Token $viewerToken | Out-Null
    Invoke-ApiRequest -Name 'Viewer cannot create business system' -Method 'POST' -Path '/ledger/business-systems' -ExpectedStatus 403 -Token $viewerToken -Body @{} | Out-Null
    Invoke-ApiRequest -Name 'Viewer cannot view user management' -Method 'GET' -Path '/system/users?pageNo=1&pageSize=10' -ExpectedStatus 403 -Token $viewerToken | Out-Null

    $operatorToken = Get-Token -Username 'operator'
    Invoke-ApiRequest -Name 'Operator cannot view user management' -Method 'GET' -Path '/system/users?pageNo=1&pageSize=10' -ExpectedStatus 403 -Token $operatorToken | Out-Null

    $directory = Split-Path -Parent $ResultPath
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    [pscustomobject]@{
        status = 'PASSED'
        startedAt = $results[0].executedAt
        finishedAt = (Get-Date).ToString('o')
        total = $results.Count
        passed = @($results | Where-Object passed).Count
        failed = 0
        cases = $results
    } | ConvertTo-Json -Depth 6 | Set-Content -Encoding utf8 $ResultPath
    Write-Host "API black-box testing passed: $($results.Count) cases. Result: $ResultPath"
}
catch {
    $directory = Split-Path -Parent $ResultPath
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    [pscustomobject]@{
        status = 'FAILED'
        finishedAt = (Get-Date).ToString('o')
        total = $results.Count
        passed = @($results | Where-Object passed).Count
        failed = 1
        error = $_.Exception.Message
        cases = $results
    } | ConvertTo-Json -Depth 6 | Set-Content -Encoding utf8 $ResultPath
    throw
}
