FROM mysql:8.0
COPY docker/mysql/init.sh /docker-entrypoint-initdb.d/init.sh
COPY doc/数据库备份/db_course_arrangement.sql /sql/schema.sql
RUN sed -i 's/\r$//' /docker-entrypoint-initdb.d/init.sh /sql/schema.sql \
    && chmod +x /docker-entrypoint-initdb.d/init.sh
