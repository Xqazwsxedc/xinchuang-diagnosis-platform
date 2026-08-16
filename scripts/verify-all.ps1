. "$PSScriptRoot\_common.ps1"

Write-Host "[verify-all] Building API server..."
& "$PSScriptRoot\build-api.ps1"
if ($LASTEXITCODE -ne 0) {
  exit $LASTEXITCODE
}

Write-Host "[verify-all] Building admin web..."
& "$PSScriptRoot\build-web.ps1"
if ($LASTEXITCODE -ne 0) {
  exit $LASTEXITCODE
}

Write-Host "[verify-all] All checks passed."
