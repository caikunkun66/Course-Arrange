#!/bin/bash
set -e
# 备份文件自带 CREATE DATABASE，容器里库已经由 MYSQL_DATABASE 建好，去掉这三行再导入
sed '1,3d' /sql/schema.sql | mysql --default-character-set=utf8mb4 -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"
