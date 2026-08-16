. "$PSScriptRoot\_common.ps1"

Import-DeploymentEnvironment
Assert-DeploymentEnvironment

$healthUrl = "http://127.0.0.1:$env:DEPLOY_API_PORT/api/health"
foreach ($item in @(
    @{ Name = 'Nginx application'; Url = 'http://127.0.0.1/' },
    @{ Name = 'Deployment API health'; Url = $healthUrl }
)) {
    try {
        $response = Invoke-WebRequest -Uri $item.Url -UseBasicParsing -TimeoutSec 5
        Write-Host "$($item.Name): HTTP $($response.StatusCode)"
    } catch {
        Write-Host "$($item.Name): unavailable ($($_.Exception.Message))"
        exit 1
    }
}
