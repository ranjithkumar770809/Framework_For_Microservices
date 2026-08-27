@echo off
title Product Service (Port 8081)
color 0A
echo ===================================================
echo   Starting Product Service on Port 8081...
echo ===================================================
cd product-service
if exist mvnw.cmd (
    call mvnw.cmd spring-boot:run
) else (
    mvn spring-boot:run
)
pause
