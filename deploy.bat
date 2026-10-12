@echo off
cd /d "%~dp0"
docker compose up -d --build
if errorlevel 1 (
  echo 启动失败，请确认已安装并启动 Docker Desktop。
  exit /b 1
)
echo.
echo 启动完成。浏览器打开 http://localhost:8080
echo 查看日志: docker compose logs -f
echo 停止服务: docker compose down
