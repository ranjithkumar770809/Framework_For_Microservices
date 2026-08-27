@echo off
title ShopEase Microservices Launcher
color 0E
echo ====================================================================
echo     Launching ShopEase 2-Microservice Architecture
echo ====================================================================
echo.
echo [1/3] Starting Product Service (Port 8081)...
start "Product Service :8081" cmd /k "run-product-service.bat"

echo [2/3] Waiting 5 seconds before launching Order Service...
timeout /t 5 /nobreak >nul

echo [3/3] Starting Order Service (Port 8082)...
start "Order Service :8082" cmd /k "run-order-service.bat"

echo.
echo [DONE] Both microservices launched in separate windows!
echo Opening Frontend in your default browser...
start "" "%~dp0frontend\index.html"

pause
