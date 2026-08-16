<#
Runs the 2.0 regression first, then verifies 2.1 read contracts and permission boundaries.
Passwords are accepted only through a parameter/environment variable and are never written to results.
Successful write scenarios intentionally remain outside this production-like regression script.
#>
param(
    [string]$BaseUrl = 'http://localhost:8080/api',
    [string]$ResultPath = "$PSScriptRoot/results/api-blackbox-result-2.1.json",
    [string]$TestPassword = $env:DIAGNOSIS_TEST_PASSWORD
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($TestPassword)) { throw 'Set DIAGNOSIS_TEST_PASSWORD before running black-box 2.1.' }
$results = [System.Collections.Generic.List[object]]::new()

function Invoke-Case {
    param([string]$Id, [string]$Name, [string]$Method, [string]$Path, [int]$ExpectedStatus, [string]$Token, [object]$Body)
    $headers = @{}
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $args = @{ Uri = "$BaseUrl$Path"; Method = $Method; Headers = $headers; SkipHttpErrorCheck = $true }
    if ($null -ne $Body) { $args.ContentType = 'application/json'; $args.Body = $Body | ConvertTo-Json -Depth 10 -Compress }
    $response = Invoke-WebRequest @args
    $passed = $response.StatusCode -eq $ExpectedStatus
    $results.Add([pscustomobject]@{ id=$Id; name=$Name; method=$Method; path=$Path; expectedStatus=$ExpectedStatus; actualStatus=$response.StatusCode; passed=$passed; executedAt=(Get-Date).ToString('o') })
    if (-not $passed) { throw "$Id $Name failed: expected HTTP $ExpectedStatus but received $($response.StatusCode)." }
    if ([string]::IsNullOrWhiteSpace($response.Content)) { return $null }
    return $response.Content | ConvertFrom-Json
}

function Assert-Code([object]$Body, [int]$Code, [string]$Id) { if ($Body.code -ne $Code) { throw "$Id expected business code $Code but received $($Body.code)." } }
function Get-Token([string]$Username) { $body = Invoke-Case "BB21-AUTH-$Username" "$Username 登录" 'POST' '/auth/login' 200 $null @{ username=$Username; password=$TestPassword }; Assert-Code $body 200 "BB21-AUTH-$Username"; return $body.data.token }

try {
    # Retain every prior 2.0 check as the non-negotiable regression baseline.
    $baselinePath = Join-Path $PSScriptRoot 'results/api-blackbox-result-2.1-baseline.json'
    & (Join-Path $PSScriptRoot 'api-blackbox-test-2.0.ps1') -BaseUrl $BaseUrl -ResultPath $baselinePath -TestPassword $TestPassword
    $baseline = Get-Content -Raw $baselinePath | ConvertFrom-Json
    foreach ($case in $baseline.cases) { $results.Add($case) }

    $admin = Get-Token 'admin'; $viewer = Get-Token 'viewer'; $operator = Get-Token 'operator'
    foreach ($case in @(
        @{ Id='BB21-SYS-01'; Name='Admin notification page'; Path='/notifications?pageNo=1&pageSize=10' },
        @{ Id='BB21-SYS-02'; Name='Admin unread notification count'; Path='/notifications/unread-count' },
        @{ Id='BB21-AI-01'; Name='Admin dedicated fault-task page'; Path='/ai/fault-prediction/tasks?pageNo=1&pageSize=10' },
        @{ Id='BB21-AI-02'; Name='Invalid fault-task create date rejected'; Path='/ai/fault-prediction/tasks?pageNo=1&pageSize=10&createdDate=2026-99-99'; ExpectedCode=400 },
        @{ Id='BB21-AI-03'; Name='Admin dedicated fault-alert page'; Path='/ai/fault-prediction/alerts?pageNo=1&pageSize=10' },
        @{ Id='BB21-AI-04'; Name='Invalid fault-alert status rejected'; Path='/ai/fault-prediction/alerts?pageNo=1&pageSize=10&processStatus=UNKNOWN'; ExpectedCode=400 },
        @{ Id='BB21-AI-05'; Name='Admin fault knowledge page'; Path='/ai/knowledge?knowledgeType=FAULT&pageNo=1&pageSize=10' },
        @{ Id='BB21-LEDGER-01'; Name='Business-system name filter'; Path='/ledger/business-systems?pageNo=1&pageSize=10&name=%E8%B4%A2%E5%8A%A1' },
        @{ Id='BB21-LEDGER-02'; Name='Database owner filter'; Path='/ledger/db-resources?pageNo=1&pageSize=10&ownerName=%E7%AE%A1%E7%90%86%E5%91%98' }
    )) {
        $body = Invoke-Case $case.Id $case.Name 'GET' $case.Path 200 $admin $null
        $expectedCode = if ($null -eq $case.ExpectedCode) { 200 } else { [int]$case.ExpectedCode }
        Assert-Code $body $expectedCode $case.Id
    }
    foreach ($case in @(
        @{ Id='BB21-VIEW-01'; Name='Viewer cannot create business system'; Method='POST'; Path='/ledger/business-systems' },
        @{ Id='BB21-VIEW-02'; Name='Viewer cannot create database resource'; Method='POST'; Path='/ledger/db-resources' },
        @{ Id='BB21-VIEW-03'; Name='Viewer cannot create fault task'; Method='POST'; Path='/ai/fault-prediction/tasks' },
        @{ Id='BB21-VIEW-04'; Name='Viewer cannot handle fault alert'; Method='PATCH'; Path='/ai/fault-prediction/alerts/1/handle' },
        @{ Id='BB21-OPER-01'; Name='Operator cannot delete fault task'; Method='DELETE'; Path='/ai/fault-prediction/tasks/1' },
        @{ Id='BB21-OPER-02'; Name='Operator cannot delete fault alert'; Method='DELETE'; Path='/ai/fault-prediction/alerts/1' }
    )) {
        $token = if ($case.Id -like '*OPER*') { $operator } else { $viewer }
        Invoke-Case $case.Id $case.Name $case.Method $case.Path 403 $token @{} | Out-Null
    }

    $dir = Split-Path -Parent $ResultPath; New-Item -ItemType Directory -Force -Path $dir | Out-Null
    [pscustomobject]@{ status='PASSED'; version='2.1'; startedAt=$results[0].executedAt; finishedAt=(Get-Date).ToString('o'); total=$results.Count; passed=@($results | Where-Object passed).Count; failed=0; cases=$results } | ConvertTo-Json -Depth 10 | Set-Content -Encoding utf8 $ResultPath
    Write-Host "API black-box 2.1 passed: $($results.Count) cases. Result: $ResultPath"
} catch {
    $dir = Split-Path -Parent $ResultPath; New-Item -ItemType Directory -Force -Path $dir | Out-Null
    [pscustomobject]@{ status='FAILED'; version='2.1'; finishedAt=(Get-Date).ToString('o'); total=$results.Count; passed=@($results | Where-Object passed).Count; failed=1; error=$_.Exception.Message; cases=$results } | ConvertTo-Json -Depth 10 | Set-Content -Encoding utf8 $ResultPath
    throw
}
