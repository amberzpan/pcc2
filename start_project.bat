@echo off
chcp 65001 >nul
setlocal

set "ROOT=E:\vscode\code\pcc2"
set "BACKEND_DIR=%ROOT%\backend"
set "FRONTEND_DIR=%ROOT%\frontend"
set "MAVEN_CMD=mvn"
set "NPM_CMD=npm"
if "%DB_PASSWORD%"=="" set "DB_PASSWORD=hp197027"

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
start "Backend" cmd /k "cd /d %BACKEND_DIR% && set DB_PASSWORD=%DB_PASSWORD% && %MAVEN_CMD% spring-boot:run"

set "BACKEND_READY=0"
for /l %%i in (1,1,45) do (
  netstat -ano | findstr ":9090" | findstr LISTENING >nul 2>&1
  if not errorlevel 1 (
    set "BACKEND_READY=1"
    goto :backend_ready
  )
  timeout /t 1 /nobreak >nul
)

:backend_ready
if "%BACKEND_READY%"=="1" (
  echo       Backend listening on 9090
) else (
  echo       [WARN] Backend startup timed out, please check backend terminal logs.
)
echo.

echo [3/3] Starting frontend...
start "Frontend" cmd /k "cd /d %FRONTEND_DIR% && %NPM_CMD% run dev -- --host"

set "FRONTEND_READY=0"
for /l %%i in (1,1,35) do (
  netstat -ano | findstr ":5173" | findstr LISTENING >nul 2>&1
  if not errorlevel 1 (
    set "FRONTEND_READY=1"
    goto :frontend_ready
  )
  timeout /t 1 /nobreak >nul
)

:frontend_ready
if "%FRONTEND_READY%"=="1" (
  echo       Frontend listening on 5173
) else (
  echo       [WARN] Frontend startup timed out, please check frontend terminal logs.
)
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

endlocal
