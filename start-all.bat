@echo off
cd /d "%~dp0"
echo Starting Diagnosis Platform...
powershell -ExecutionPolicy Bypass -File "%~dp0scripts\dev-all.ps1"
echo.
echo Startup commands have been sent. Check the API and Web PowerShell windows.
echo API health: http://localhost:8080/api/health
echo Admin web:  http://localhost:5173
pause
