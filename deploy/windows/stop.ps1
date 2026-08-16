. "$PSScriptRoot\_common.ps1"

$apiPidFile = Join-Path $Script:RuntimeDir 'api.pid'
if (Test-Path -LiteralPath $apiPidFile) {
    $apiPid = (Get-Content -LiteralPath $apiPidFile -Raw).Trim()
    if ($apiPid -match '^\d+$') {
        Stop-Process -Id ([int]$apiPid) -Force -ErrorAction SilentlyContinue
    }
    Remove-Item -LiteralPath $apiPidFile -Force
}
if (Test-Path -LiteralPath $Script:NginxExe) {
    & $Script:NginxExe -p $Script:NginxRoot -s stop
}
Write-Host 'Deployment stop requested.'
