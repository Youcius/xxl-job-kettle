#!/bin/bash
# XXL-JOB Kettle WSL 一键部署脚本
set -e

echo "=== XXL-JOB Kettle 部署 ==="

MYSQL_USER="root"
MYSQL_DB="xxl_job"
APP_DIR="/opt/xxl-job"
FRONTEND_DIR="/opt/xxl-job/frontend"
PROJECT_DIR="/mnt/d/CodeLab/xxl-job-2.5.0"
FRONTEND_SRC="/mnt/d/CodeLab/xxl-job-frontend"

# 1. 启动 MySQL
echo "[1/5] 启动 MySQL..."
sudo service mysql start

# 2. 初始化数据库
echo "[2/5] 初始化数据库..."
sudo mysql -u root -e "CREATE DATABASE IF NOT EXISTS ${MYSQL_DB} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
sudo mysql -u root ${MYSQL_DB} < ${PROJECT_DIR}/doc/db/tables_xxl_job.sql 2>/dev/null
sudo mysql -u root ${MYSQL_DB} < ${PROJECT_DIR}/xxl-job-admin/src/main/resources/sql/tables_xxl_kettle.sql 2>/dev/null
echo "  数据库初始化完成"

# 3. 部署后端
echo "[3/5] 部署后端..."
sudo mkdir -p ${APP_DIR}
# 先把 jar 复制到 Linux 文件系统（避免跨文件系统性能问题）
sudo cp ${PROJECT_DIR}/xxl-job-admin/target/xxl-job-admin-2.5.0.jar ${APP_DIR}/

# 创建启动脚本
cat > /tmp/xxl-job-admin.service << 'SVC'
[Unit]
Description=XXL-JOB Admin
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/xxl-job
ExecStart=/usr/bin/java -jar /opt/xxl-job/xxl-job-admin-2.5.0.jar --server.port=8081
Restart=on-failure

[Install]
WantedBy=multi-user.target
SVC

sudo mv /tmp/xxl-job-admin.service /etc/systemd/system/ 2>/dev/null || true

# 直接启动（WSL 无 systemd）
echo "  启动后端（端口 8081）..."
nohup java -jar ${APP_DIR}/xxl-job-admin-2.5.0.jar --server.port=8081 \
    --spring.datasource.username=root \
    --spring.datasource.password="" \
    > ${APP_DIR}/console.log 2>&1 &
echo $! > ${APP_DIR}/pid
echo "  后端已启动，PID: $(cat ${APP_DIR}/pid)"

# 4. 部署前端
echo "[4/5] 部署前端..."
sudo mkdir -p ${FRONTEND_DIR}
sudo cp -r ${FRONTEND_SRC}/dist/* ${FRONTEND_DIR}/

# Nginx 配置
sudo tee /etc/nginx/sites-available/xxl-job > /dev/null << 'NGX'
server {
    listen 8080;
    server_name localhost;

    root /opt/xxl-job/frontend;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /xxl-job-admin/ {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        client_max_body_size 200m;
        proxy_read_timeout 300s;
    }
}
NGX

sudo rm -f /etc/nginx/sites-enabled/default
sudo ln -sf /etc/nginx/sites-available/xxl-job /etc/nginx/sites-enabled/

echo "[5/5] 重启 Nginx..."
sudo service nginx restart

echo ""
echo "=== 部署完成 ==="
echo "前端: http://localhost:8080"
echo "后端: http://localhost:8081/xxl-job-admin"
echo "日志: tail -f ${APP_DIR}/console.log"
