# BentoML time-series communication verification service

This service is intentionally not a production anomaly detection model. It uses a deterministic Z-Score calculation to verify that time-series arrays can travel through this path:

`Spring Boot -> BentoML -> fake model -> Spring Boot`

## Run

```powershell
cd apps/model-server
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe -m bentoml serve service:TimeSeriesAnomalyService --port 3000
```

The Spring Boot default endpoint is `http://127.0.0.1:3000/predict`. Override it with `AI_ENGINE_BENTOML_BASE_URL` when needed.

## Direct verification

```powershell
$body = @{
  requestId = "direct-test"
  timestamps = @(1, 2, 3, 4, 5)
  features = @{ cpu_usage = @(10, 10, 10, 10, 100) }
} | ConvertTo-Json -Depth 5

Invoke-RestMethod -Method Post -Uri http://127.0.0.1:3000/predict -ContentType application/json -Body $body
```

With the platform running, authenticated users with `ai:task:execute` can send the same payload to:

`POST /api/ai/fault-prediction/engine/verify`
