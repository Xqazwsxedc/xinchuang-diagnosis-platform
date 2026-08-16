. "$PSScriptRoot\_common.ps1"

$packageDir = Join-Path $PSScriptRoot 'packages'
$archive = Join-Path $packageDir 'nginx-1.28.0.zip'
$downloadUrl = 'https://nginx.org/download/nginx-1.28.0.zip'

if (Test-Path -LiteralPath $Script:NginxExe) {
    Write-Host "Nginx is already installed: $Script:NginxRoot"
    return
}

New-Item -ItemType Directory -Force -Path $packageDir | Out-Null
if (-not (Test-Path -LiteralPath $archive) -or (Get-Item -LiteralPath $archive).Length -lt 1000000) {
    Remove-Item -LiteralPath $archive -Force -ErrorAction SilentlyContinue
    Start-BitsTransfer -Source $downloadUrl -Destination $archive
}
if ((Get-Item -LiteralPath $archive).Length -lt 1000000) {
    throw 'Nginx download produced an incomplete archive.'
}
Expand-Archive -LiteralPath $archive -DestinationPath (Join-Path $Script:ProjectRoot 'tools') -Force
if (-not (Test-Path -LiteralPath $Script:NginxExe)) {
    throw 'Nginx extraction did not produce the expected executable.'
}
Write-Host "Nginx installed: $Script:NginxRoot"
