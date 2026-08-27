@echo off
title Order Service (Port 8082)
color 0B
echo ===================================================
echo   Starting Order Service on Port 8082...
echo ===================================================
cd order-service
if exist mvnw.cmd (
    call mvnw.cmd spring-boot:run
) else (
    mvn spring-boot:run
)
pause
