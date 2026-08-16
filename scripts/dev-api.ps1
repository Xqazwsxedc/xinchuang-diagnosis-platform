. "$PSScriptRoot\_common.ps1"

Import-LocalRuntimeEnvironment
Initialize-JavaMavenEnv
$mvn = Get-MavenCommand

if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET)) {
  $bytes = [byte[]]::new(32)
  [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
  $env:JWT_SECRET = [Convert]::ToBase64String($bytes)
  Write-Host "[dev-api] Generated a temporary JWT_SECRET for this development process."
}

Set-Location -LiteralPath $Script:ApiServerDir
Invoke-Checked $mvn "spring-boot:run"
