@echo off
echo 正在重启前端服务...
echo.
cd /d %~dp0
call npm run dev

