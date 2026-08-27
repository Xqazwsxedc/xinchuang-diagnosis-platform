$ErrorActionPreference = "Stop"

$modelServerDir = Join-Path $PSScriptRoot "..\apps\model-server"
$python = Join-Path $modelServerDir ".venv\Scripts\python.exe"

if (-not (Test-Path -LiteralPath $python)) {
  python -m venv (Join-Path $modelServerDir ".venv")
}

& $python -m pip install -r (Join-Path $modelServerDir "requirements.txt")
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Set-Location -LiteralPath $modelServerDir
& $python -m bentoml serve service:TimeSeriesAnomalyService --port 3000
