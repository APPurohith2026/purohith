param(
  [switch]$SkipInstall,
  [switch]$SkipBackendTests,
  [switch]$SkipE2E,
  [switch]$KeepServers
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Frontend = Join-Path $Root 'frontend'
$Backend = Join-Path $Root 'backend'
$backendProcess = $null
$frontendProcess = $null

function Step($message) { Write-Host "`n=== $message ===" -ForegroundColor Cyan }
function Require-Command($name) {
  if (-not (Get-Command $name -ErrorAction SilentlyContinue)) { throw "Required command '$name' was not found in PATH." }
}
function Wait-Url($url, $name, $seconds = 90) {
  $deadline = (Get-Date).AddSeconds($seconds)
  do {
    try {
      $r = Invoke-WebRequest -UseBasicParsing -Uri $url -TimeoutSec 3
      if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) { Write-Host "$name is ready." -ForegroundColor Green; return }
    } catch {}
    Start-Sleep -Seconds 2
  } while ((Get-Date) -lt $deadline)
  throw "$name did not become ready at $url within $seconds seconds."
}

try {
  Require-Command node; Require-Command npm; Require-Command mvn; Require-Command docker

  Step 'Start local MySQL'
  Push-Location $Root
  docker compose up -d mysql
  Pop-Location

  if (-not $SkipBackendTests) {
    Step 'Backend unit + integration tests'
    Push-Location $Backend
    mvn clean test
    if ($LASTEXITCODE -ne 0) { throw 'Backend tests failed.' }
    Pop-Location
  }

  Step 'Frontend dependencies and production build'
  Push-Location $Frontend
  if (-not $SkipInstall) {
    if (Test-Path 'package-lock.json') { npm ci } else { npm install }
    if ($LASTEXITCODE -ne 0) { throw 'npm dependency installation failed.' }
    npx playwright install chromium
    if ($LASTEXITCODE -ne 0) { throw 'Playwright Chromium installation failed.' }
  }
  npm run build
  if ($LASTEXITCODE -ne 0) { throw 'Frontend build failed.' }
  Pop-Location

  if (-not $SkipE2E) {
    Step 'Start Spring Boot for E2E'
    $backendProcess = Start-Process powershell -PassThru -WindowStyle Hidden -ArgumentList '-NoProfile','-Command',"Set-Location '$Backend'; `$env:SPRING_PROFILES_ACTIVE='local'; mvn spring-boot:run"
    Wait-Url 'http://127.0.0.1:8080/actuator/health' 'Backend' 120

    Step 'Start React/Vite for E2E'
    $frontendProcess = Start-Process powershell -PassThru -WindowStyle Hidden -ArgumentList '-NoProfile','-Command',"Set-Location '$Frontend'; npm run dev -- --host 127.0.0.1 --port 5173"
    Wait-Url 'http://127.0.0.1:5173' 'Frontend' 60

    Step 'Playwright E2E regression suite'
    Push-Location $Frontend
    npm run test:e2e
    $e2eCode = $LASTEXITCODE
    Pop-Location
    if ($e2eCode -ne 0) { throw "Playwright failed. Open frontend/playwright-report/index.html or run 'npm run test:e2e:report'." }
  }

  Step 'Verification complete'
  Write-Host 'ALL CHECKS PASSED' -ForegroundColor Green
  Write-Host 'Playwright report: frontend/playwright-report/index.html'
}
finally {
  if (-not $KeepServers) {
    if ($frontendProcess -and -not $frontendProcess.HasExited) { Stop-Process -Id $frontendProcess.Id -Force -ErrorAction SilentlyContinue }
    if ($backendProcess -and -not $backendProcess.HasExited) { Stop-Process -Id $backendProcess.Id -Force -ErrorAction SilentlyContinue }
  }
}
