@echo off
cd /d "%~dp0"

:: 启动后端
start cmd /k "mvn spring-boot:run"

:: 启动前端
start cmd /k "cd /d %~dp0UI\coursearrange && npm run dev"

echo ===========================
echo 前后端启动命令已执行，请查看各自窗口日志。
echo ===========================
pause