param(
  [switch] $DryRun
)

. "$PSScriptRoot\_common.ps1"

$apiScript = Join-Path $PSScriptRoot "dev-api.ps1"
$webScript = Join-Path $PSScriptRoot "dev-web.ps1"

if (-not (Test-Path -LiteralPath $apiScript)) {
  throw "API startup script not found: $apiScript"
}

if (-not (Test-Path -LiteralPath $webScript)) {
  throw "Web startup script not found: $webScript"
}

$apiArgs = "-NoExit -ExecutionPolicy Bypass -File `"$apiScript`""
$webArgs = "-NoExit -ExecutionPolicy Bypass -File `"$webScript`""

if ($DryRun) {
  Write-Host "[dev-all] Project root: $Script:ProjectRoot"
  Write-Host "[dev-all] API server directory: $Script:ApiServerDir"
  Write-Host "[dev-all] Admin web directory: $Script:AdminWebDir"
  Write-Host "[dev-all] API command: powershell $apiArgs"
  Write-Host "[dev-all] Web command: powershell $webArgs"
  Write-Host "[dev-all] Dry run passed."
  return
}

Write-Host "[dev-all] Starting API server in a new PowerShell window..."
Start-Process -FilePath "powershell" -ArgumentList $apiArgs -WorkingDirectory $Script:ProjectRoot

Start-Sleep -Seconds 3

Write-Host "[dev-all] Starting admin web in a new PowerShell window..."
Start-Process -FilePath "powershell" -ArgumentList $webArgs -WorkingDirectory $Script:ProjectRoot

Write-Host "[dev-all] Startup windows opened."
Write-Host "[dev-all] API health: http://localhost:8080/api/health"
Write-Host "[dev-all] Admin web: http://localhost:5173"
