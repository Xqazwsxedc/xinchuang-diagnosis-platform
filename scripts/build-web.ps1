. "$PSScriptRoot\_common.ps1"

Initialize-NodePnpmEnv
$pnpm = Get-PnpmCommand

Set-Location -LiteralPath $Script:AdminWebDir
Invoke-Checked $pnpm "build"
