@echo off
echo ========================================
echo 清除前端缓存并重启服务
echo ========================================
echo.

cd /d %~dp0

echo [1/3] 正在清除 node_modules/.cache 目录...
if exist "node_modules\.cache" (
    rmdir /s /q "node_modules\.cache"
    echo 缓存已清除
) else (
    echo 缓存目录不存在
)

echo.
echo [2/3] 正在清除 dist 目录...
if exist "dist" (
    rmdir /s /q "dist"
    echo dist 目录已清除
) else (
    echo dist 目录不存在
)

echo.
echo [3/3] 正在启动前端服务...
echo ========================================
echo 请在浏览器中按 Ctrl+Shift+Delete 清除浏览器缓存
echo 或按 Ctrl+F5 强制刷新页面
echo ========================================
echo.

call npm run dev

