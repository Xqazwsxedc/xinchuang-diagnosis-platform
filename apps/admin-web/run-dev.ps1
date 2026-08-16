$ErrorActionPreference = "Stop"

$bundledNode = "$env:USERPROFILE\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin"
$bundledPnpm = "$env:USERPROFILE\.cache\codex-runtimes\codex-primary-runtime\dependencies\bin\pnpm.cmd"
$fallbackPnpm = "$env:USERPROFILE\.cache\codex-runtimes\codex-primary-runtime\dependencies\bin\fallback\pnpm.cmd"
$env:COREPACK_ENABLE_PROJECT_SPEC = "0"
$env:npm_config_manage_package_manager_versions = "false"

if (Test-Path $bundledNode) {
  $env:Path = "$bundledNode;$env:Path"
}

if (Test-Path $bundledPnpm) {
  & $bundledPnpm dev
} elseif (Test-Path $fallbackPnpm) {
  & $fallbackPnpm dev
} else {
  pnpm dev
}
