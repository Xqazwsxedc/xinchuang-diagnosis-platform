$ErrorActionPreference = "Stop"

$Script:ProjectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$Script:ApiServerDir = Join-Path $Script:ProjectRoot "apps\api-server"
$Script:AdminWebDir = Join-Path $Script:ProjectRoot "apps\admin-web"

function Add-PathPrefix {
  param(
    [Parameter(Mandatory = $true)]
    [string] $Path
  )

  if (Test-Path -LiteralPath $Path) {
    $env:Path = "$Path;$env:Path"
  }
}

function Initialize-JavaMavenEnv {
  $javaHomeInvalid = (-not $env:JAVA_HOME) -or (-not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME "bin\java.exe")))
  if ($javaHomeInvalid) {
    $candidateJavaHome = "C:\Program Files\Java\jdk-17"
    if (Test-Path -LiteralPath (Join-Path $candidateJavaHome "bin\java.exe")) {
      $env:JAVA_HOME = $candidateJavaHome
    }
  }

  $mavenHomeInvalid = (-not $env:MAVEN_HOME) -or (-not (Test-Path -LiteralPath (Join-Path $env:MAVEN_HOME "bin\mvn.cmd")))
  if ($mavenHomeInvalid) {
    $candidateMavenHome = "E:\maven\apache-maven-3.9.16"
    if (Test-Path -LiteralPath (Join-Path $candidateMavenHome "bin\mvn.cmd")) {
      $env:MAVEN_HOME = $candidateMavenHome
    }
  }

  if ($env:MAVEN_HOME) {
    Add-PathPrefix -Path (Join-Path $env:MAVEN_HOME "bin")
  }

  if ($env:JAVA_HOME) {
    Add-PathPrefix -Path (Join-Path $env:JAVA_HOME "bin")
  }
}

function Get-MavenCommand {
  if ($env:MAVEN_HOME) {
    $mvnCmd = Join-Path $env:MAVEN_HOME "bin\mvn.cmd"
    if (Test-Path -LiteralPath $mvnCmd) {
      return $mvnCmd
    }
  }

  return "mvn"
}

function Import-LocalRuntimeEnvironment {
  $envFile = Join-Path $Script:ProjectRoot "deploy\windows\.env"
  if (-not (Test-Path -LiteralPath $envFile)) {
    return
  }

  $allowedNames = @(
    "DB_HOST", "DB_PORT", "DB_NAME", "DB_USERNAME", "DB_PASSWORD",
    "JWT_SECRET", "KNOWLEDGE_STORAGE_ROOT",
    "MONITORING_PROMETHEUS_BASE_URL", "MONITORING_PROMETHEUS_CONNECT_TIMEOUT",
    "MONITORING_PROMETHEUS_READ_TIMEOUT", "MONITORING_TARGETS_NODE_INSTANCE",
    "MONITORING_TARGETS_MYSQL_INSTANCE"
  )
  foreach ($line in Get-Content -LiteralPath $envFile) {
    if ($line -match '^\s*#' -or [string]::IsNullOrWhiteSpace($line)) {
      continue
    }
    if ($line -notmatch '^([A-Z0-9_]+)=(.*)$') {
      continue
    }
    $name = $matches[1]
    $value = $matches[2]
    if ($allowedNames -contains $name -and -not [string]::IsNullOrWhiteSpace($value) -and -not $value.StartsWith('REPLACE_')) {
      [Environment]::SetEnvironmentVariable($name, $value, 'Process')
    }
  }
}

function Initialize-NodePnpmEnv {
  $env:COREPACK_ENABLE_PROJECT_SPEC = "0"
  $env:npm_config_manage_package_manager_versions = "false"

  $bundledNode = Join-Path $env:USERPROFILE ".cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin"
  Add-PathPrefix -Path $bundledNode
}

function Get-PnpmCommand {
  $bundledPnpm = Join-Path $env:USERPROFILE ".cache\codex-runtimes\codex-primary-runtime\dependencies\bin\pnpm.cmd"
  $fallbackPnpm = Join-Path $env:USERPROFILE ".cache\codex-runtimes\codex-primary-runtime\dependencies\bin\fallback\pnpm.cmd"

  if (Test-Path -LiteralPath $bundledPnpm) {
    return $bundledPnpm
  }

  if (Test-Path -LiteralPath $fallbackPnpm) {
    return $fallbackPnpm
  }

  return "pnpm"
}

function Invoke-Checked {
  param(
    [Parameter(Mandatory = $true)]
    [string] $Command,

    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $Arguments
  )

  & $Command @Arguments
  if ($LASTEXITCODE -ne 0) {
    throw "Command failed with exit code $LASTEXITCODE`: $Command $($Arguments -join ' ')"
  }
}
