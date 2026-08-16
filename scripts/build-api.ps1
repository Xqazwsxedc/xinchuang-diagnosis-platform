. "$PSScriptRoot\_common.ps1"

Initialize-JavaMavenEnv
$mvn = Get-MavenCommand

Set-Location -LiteralPath $Script:ApiServerDir
Invoke-Checked $mvn "-DskipTests" "compile"
