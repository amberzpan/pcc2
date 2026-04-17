@echo off
chcp 65001 >nul
echo ========================================
echo   Social Platform Startup Script
echo ========================================
echo.

echo [1/3] Cleanup old processes...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":9090" ^| findstr LISTENING') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| findstr LISTENING') do taskkill /F /PID %%a >nul 2>&1
timeout /t 2 /nobreak >nul
echo       Done
echo.

echo [2/3] Starting backend...
start "Backend" cmd /k "cd /d E:\vscode\code\pcc2\backend && C:\maven\apache-maven-3.9.14\bin\mvn.cmd spring-boot:run"
echo       Please wait for "Started SocialApplication"...
echo.

echo [3/3] Starting frontend...
timeout /t 12 /nobreak >nul
start "Frontend" cmd /k "cd /d E:\vscode\code\pcc2\frontend && E:\Node\npm.cmd run dev"
echo       Starting frontend...
echo.

timeout /t 5 /nobreak >nul
echo.
echo ========================================
echo   Startup Complete!
echo ========================================
echo.
echo   Backend: http://localhost:9090
echo   Frontend: http://localhost:5173
echo.
echo   Please open in browser: http://localhost:5173
echo.
pause
