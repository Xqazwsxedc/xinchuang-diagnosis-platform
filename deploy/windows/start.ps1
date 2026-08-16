. "$PSScriptRoot\_common.ps1"

Import-DeploymentEnvironment
Assert-DeploymentEnvironment
Initialize-DeploymentDirectories

$jar = Join-Path $Script:RuntimeDir 'diagnosis-platform.jar'
if (-not (Test-Path -LiteralPath $jar)) {
    throw "Deployment package not found: $jar. Run build.ps1 first."
}
if (-not (Test-Path -LiteralPath (Join-Path $Script:SiteDir 'index.html'))) {
    throw 'Frontend deployment files not found. Run build.ps1 first.'
}
if (-not (Test-Path -LiteralPath $Script:NginxExe)) {
    throw "Nginx was not installed at $Script:NginxRoot. Run install-nginx.ps1 first."
}
if (-not (Test-LocalPortAvailable ([int]$env:DEPLOY_API_PORT))) {
    throw "Deployment API port $env:DEPLOY_API_PORT is already in use."
}
if (-not (Test-LocalPortAvailable 80)) {
    throw 'Port 80 is already in use.'
}

$java = Get-JavaCommand
$apiLog = Join-Path $Script:LogsDir 'api.log'
$apiErrorLog = Join-Path $Script:LogsDir 'api-error.log'
$env:SERVER_PORT = $env:DEPLOY_API_PORT
$apiProcess = Start-Process -FilePath $java -ArgumentList "-jar `"$jar`"" -WorkingDirectory $Script:RuntimeDir -WindowStyle Hidden -RedirectStandardOutput $apiLog -RedirectStandardError $apiErrorLog -PassThru
$apiProcess.Id | Set-Content -Encoding ascii (Join-Path $Script:RuntimeDir 'api.pid')

$healthUrl = "http://127.0.0.1:$env:DEPLOY_API_PORT/api/health"
$deadline = (Get-Date).AddSeconds(60)
do {
    try {
        $health = Invoke-WebRequest -Uri $healthUrl -UseBasicParsing -TimeoutSec 3
        if ($health.StatusCode -eq 200) { break }
    } catch {
        Start-Sleep -Seconds 2
    }
} while ((Get-Date) -lt $deadline)

if ($null -eq $health -or $health.StatusCode -ne 200) {
    Stop-Process -Id $apiProcess.Id -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath (Join-Path $Script:RuntimeDir 'api.pid') -Force -ErrorAction SilentlyContinue
    throw "Deployment API did not become healthy. See $apiErrorLog"
}

$nginxConfiguration = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'nginx.conf') -Raw
$nginxConfiguration.Replace('__DEPLOY_API_PORT__', $env:DEPLOY_API_PORT) | Set-Content -LiteralPath (Join-Path $Script:NginxRoot 'conf\nginx.conf') -Encoding ascii
& $Script:NginxExe -p $Script:NginxRoot -c conf/nginx.conf
if ($LASTEXITCODE -ne 0) {
    Stop-Process -Id $apiProcess.Id -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath (Join-Path $Script:RuntimeDir 'api.pid') -Force -ErrorAction SilentlyContinue
    throw 'Nginx failed to start.'
}

Write-Host 'Deployment started.'
Write-Host 'Application: http://localhost'
Write-Host "API health: $healthUrl"
