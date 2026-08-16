$ErrorActionPreference = "Stop"

if (-not $env:JAVA_HOME) {
  $candidate = "C:\Program Files\Java\jdk-17"
  if (Test-Path $candidate) {
    $env:JAVA_HOME = $candidate
    $env:Path = "$candidate\bin;$env:Path"
  }
}

mvn spring-boot:run

