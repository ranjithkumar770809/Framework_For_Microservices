# ==============================================================================
# ShopEase Microservices PowerShell Launcher
# ==============================================================================

Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "     Launching ShopEase 2-Microservice Architecture (PowerShell)     " -ForegroundColor Cyan
Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host ""

$baseDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Ensure Maven is available
& "$baseDir\setup-maven.ps1"

$toolsDir = Join-Path $baseDir "tools\apache-maven-3.9.6\bin"
$mvnExec = if (Get-Command mvn -ErrorAction SilentlyContinue) { "mvn" } elseif (Test-Path "$toolsDir\mvn.cmd") { "$toolsDir\mvn.cmd" } else { "mvn" }

# 1. Start Product Service (Port 8081)
Write-Host "[1/3] Starting Product Service (Port 8081)..." -ForegroundColor Green
Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "Set-Location '$baseDir\product-service'; `$env:PATH = '$toolsDir;' + `$env:PATH; Write-Host '--- Product Service (Port 8081) ---' -ForegroundColor Green; $mvnExec spring-boot:run"

# 2. Wait for Product Service
Write-Host "[2/3] Waiting 5 seconds before launching Order Service..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# 3. Start Order Service (Port 8082)
Write-Host "[3/3] Starting Order Service (Port 8082)..." -ForegroundColor Magenta
Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "Set-Location '$baseDir\order-service'; `$env:PATH = '$toolsDir;' + `$env:PATH; Write-Host '--- Order Service (Port 8082) ---' -ForegroundColor Magenta; $mvnExec spring-boot:run"

# 4. Open Frontend
Write-Host ""
Write-Host "[DONE] Microservices launched! Opening Frontend in browser..." -ForegroundColor Cyan
Start-Process "$baseDir\frontend\index.html"

Write-Host "Both microservices are running in separate terminal windows." -ForegroundColor Green
