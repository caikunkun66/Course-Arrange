@echo off
echo 测试MySQL数据库连接...
echo.
echo 请输入MySQL root密码（配置文件中是: 123456）
mysql -u root -p123456 -e "SELECT 'MySQL连接成功!' as Result; SHOW DATABASES;"
pause

