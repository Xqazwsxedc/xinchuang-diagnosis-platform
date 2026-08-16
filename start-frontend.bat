@echo off
cd /d "%~dp0"
powershell -ExecutionPolicy Bypass -File "%~dp0scripts\dev-web.ps1"
pause
