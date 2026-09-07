$ErrorActionPreference = 'Stop'

$Script:DeployRoot = Split-Path -Parent $PSScriptRoot
$Script:ProjectRoot = Split-Path -Parent $Script:DeployRoot
$Script:ApiServerDir = Join-Path $Script:ProjectRoot 'apps\api-server'
$Script:AdminWebDir = Join-Path $Script:ProjectRoot 'apps\admin-web'
$Script:RuntimeDir = Join-Path $PSScriptRoot 'runtime'
$Script:SiteDir = Join-Path $Script:RuntimeDir 'site'
$Script:LogsDir = Join-Path $Script:RuntimeDir 'logs'
$Script:EnvFile = Join-Path $PSScriptRoot '.env'
$Script:NginxRoot = Join-Path $Script:ProjectRoot 'tools\nginx-1.28.0'
$Script:NginxExe = Join-Path $Script:NginxRoot 'nginx.exe'

function Import-DeploymentEnvironment {
    if (-not (Test-Path -LiteralPath $Script:EnvFile)) {
        throw "Deployment environment file not found: $Script:EnvFile. Copy deployment.env.template to .env and set the values."
    }
    foreach ($line in Get-Content -LiteralPath $Script:EnvFile) {
        if ($line -match '^\s*#' -or [string]::IsNullOrWhiteSpace($line)) {
            continue
        }
        if ($line -notmatch '^([A-Z0-9_]+)=(.*)$') {
            throw "Invalid deployment environment line: $line"
        }
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
    }
}

function Assert-DeploymentEnvironment {
    foreach ($name in 'DB_HOST', 'DB_PORT', 'DB_NAME', 'DB_USERNAME', 'DB_PASSWORD', 'DEPLOY_API_PORT', 'JWT_SECRET') {
        $value = [Environment]::GetEnvironmentVariable($name, 'Process')
        if ([string]::IsNullOrWhiteSpace($value) -or $value.StartsWith('REPLACE_')) {
            throw "Deployment environment variable $name must be configured in $Script:EnvFile."
        }
    }
    if ($env:JWT_SECRET.Length -lt 32) {
        throw 'JWT_SECRET must contain at least 32 characters.'
    }
}

function Initialize-DeploymentDirectories {
    foreach ($directory in $Script:RuntimeDir, $Script:SiteDir, $Script:LogsDir) {
        New-Item -ItemType Directory -Force -Path $directory | Out-Null
    }
}

function Get-JavaCommand {
    foreach ($javaHome in @($env:JAVA_HOME, [Environment]::GetEnvironmentVariable('JAVA_HOME', 'User'), 'E:\Java\jdk-17', 'C:\Program Files\Java\jdk-17')) {
        if ($javaHome) {
            $knownJava = Join-Path $javaHome 'bin\java.exe'
            if (Test-Path -LiteralPath $knownJava) {
                return $knownJava
            }
        }
    }
    $java = Get-Command java -ErrorAction SilentlyContinue
    if ($null -eq $java) {
        throw 'JDK 17 was not found.'
    }
    return $java.Source
}

function Get-MavenCommand {
    $knownMaven = 'E:\maven\apache-maven-3.9.16\bin\mvn.cmd'
    if (Test-Path -LiteralPath $knownMaven) {
        return $knownMaven
    }
    $maven = Get-Command mvn -ErrorAction SilentlyContinue
    if ($null -eq $maven) {
        throw 'Maven was not found.'
    }
    return $maven.Source
}

function Get-PnpmCommand {
    $knownPnpm = 'C:\Users\ASUS\.cache\codex-runtimes\codex-primary-runtime\dependencies\bin\fallback\pnpm.cmd'
    if (Test-Path -LiteralPath $knownPnpm) {
        return $knownPnpm
    }
    $pnpm = Get-Command pnpm -ErrorAction SilentlyContinue
    if ($null -eq $pnpm) {
        throw 'pnpm was not found.'
    }
    return $pnpm.Source
}

function Test-LocalPortAvailable([int]$Port) {
    return $null -eq (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1)
}
