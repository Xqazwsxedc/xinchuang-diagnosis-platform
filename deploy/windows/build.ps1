. "$PSScriptRoot\_common.ps1"

Import-DeploymentEnvironment
Assert-DeploymentEnvironment
Initialize-DeploymentDirectories

# 部署站点必须是当前构建的完整快照，避免旧 hash 资源残留导致页面回退到旧版本。
if (Test-Path -LiteralPath $Script:SiteDir) {
    Get-ChildItem -LiteralPath $Script:SiteDir -Force | Remove-Item -Recurse -Force
}

$maven = Get-MavenCommand
$pnpm = Get-PnpmCommand
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
$env:MAVEN_HOME = 'E:\maven\apache-maven-3.9.16'
$env:Path = "C:\Users\ASUS\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin;$env:MAVEN_HOME\bin;$env:JAVA_HOME\bin;$env:Path"

Push-Location $Script:ApiServerDir
try {
    & $maven clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw 'Backend package build failed.' }
} finally {
    Pop-Location
}

Push-Location $Script:AdminWebDir
try {
    & $pnpm build
    if ($LASTEXITCODE -ne 0) { throw 'Frontend production build failed.' }
} finally {
    Pop-Location
}

Copy-Item -LiteralPath (Join-Path $Script:ApiServerDir 'target\diagnosis-platform-0.0.1-SNAPSHOT.jar') -Destination (Join-Path $Script:RuntimeDir 'diagnosis-platform.jar') -Force
Copy-Item -Path (Join-Path $Script:AdminWebDir 'dist\*') -Destination $Script:SiteDir -Recurse -Force
Write-Host "Deployment artifacts prepared in $Script:RuntimeDir"
