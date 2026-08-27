# ==============================================================================
# Helper Script: Checks Java & Downloads Apache Maven if not present
# ==============================================================================

Write-Host "Checking Java and Maven environment..." -ForegroundColor Cyan

# 1. Check Java
try {
    $javaVer = & java -version 2>&1
    Write-Host "[OK] Java is installed:" -ForegroundColor Green
    Write-Host ($javaVer | Out-String)
} catch {
    Write-Host "[ERROR] Java JDK is not detected in PATH. Please install Java 17+." -ForegroundColor Red
}

# 2. Check Maven
$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnCmd) {
    Write-Host "[OK] Maven is already installed at: $($mvnCmd.Source)" -ForegroundColor Green
} else {
    Write-Host "[INFO] Maven is not in system PATH. Downloading portable Maven 3.9.6..." -ForegroundColor Yellow
    $toolsDir = Join-Path $PSScriptRoot "tools"
    $mavenDir = Join-Path $toolsDir "apache-maven-3.9.6"
    $zipFile = Join-Path $toolsDir "maven.zip"

    if (-not (Test-Path $toolsDir)) {
        New-Item -ItemType Directory -Path $toolsDir | Out-Null
    }

    if (-not (Test-Path (Join-Path $mavenDir "bin\mvn.cmd"))) {
        Write-Host "Downloading Apache Maven..." -ForegroundColor Cyan
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip" -OutFile $zipFile
        Write-Host "Extracting Maven..." -ForegroundColor Cyan
        Expand-Archive -Path $zipFile -DestinationPath $toolsDir -Force
        Remove-Item $zipFile -Force
    }

    $env:PATH = "$mavenDir\bin;$env:PATH"
    Write-Host "[OK] Portable Maven configured at $mavenDir\bin" -ForegroundColor Green
}
