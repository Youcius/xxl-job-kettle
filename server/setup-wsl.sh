#!/bin/bash
# XXL-JOB Kettle WSL 一键部署脚本
# 在 WSL Ubuntu 终端中运行: bash /mnt/d/CodeLab/xxl-job-2.5.0/setup-wsl.sh
set -e

echo "========================================"
echo "  XXL-JOB Kettle WSL 部署"
echo "========================================"

# 0. 换阿里云镜像（提速）
echo "[0/6] 配置镜像源..."
sudo sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list 2>/dev/null || true
sudo sed -i 's/security.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list 2>/dev/null || true

# 1. 安装依赖
echo "[1/6] 安装 Java 17 + MySQL + Nginx..."
sudo apt-get update -qq
sudo apt-get install -y -qq openjdk-17-jdk-headless mysql-server nginx
echo "  依赖安装完成"

# 2. 启动 MySQL
echo "[2/6] 启动 MySQL..."
sudo service mysql start

# 初始化数据库
sudo mysql -u root -e "CREATE DATABASE IF NOT EXISTS xxl_job DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || true

# 3. 建表
echo "[3/6] 建表..."
sudo mysql -u root xxl_job < /mnt/d/CodeLab/xxl-job-2.5.0/doc/db/tables_xxl_job.sql
sudo mysql -u root xxl_job < /mnt/d/CodeLab/xxl-job-2.5.0/xxl-job-admin/src/main/resources/sql/tables_xxl_kettle.sql
echo "  数据库表创建完成"

# 4. 部署后端
echo "[4/6] 部署后端..."
sudo mkdir -p /opt/xxl-job
sudo cp /mnt/d/CodeLab/xxl-job-2.5.0/xxl-job-admin/target/xxl-job-admin-2.5.0.jar /opt/xxl-job/

# 停止旧进程
kill $(cat /opt/xxl-job/pid 2>/dev/null) 2>/dev/null || true
sleep 1

# 启动
nohup java -jar /opt/xxl-job/xxl-job-admin-2.5.0.jar \
    --server.port=8081 \
    --spring.datasource.username=root \
    > /opt/xxl-job/console.log 2>&1 &
echo $! > /opt/xxl-job/pid
echo "  后端已启动，PID: $(cat /opt/xxl-job/pid)"

# 5. 部署前端
echo "[5/6] 部署前端..."
sudo mkdir -p /opt/xxl-job/frontend
sudo cp -r /mnt/d/CodeLab/xxl-job-frontend/dist/* /opt/xxl-job/frontend/

# 6. 配置 Nginx
echo "[6/6] 配置 Nginx..."
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
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        client_max_body_size 200m;
        proxy_read_timeout 300s;
    }
}
NGX

sudo rm -f /etc/nginx/sites-enabled/default
sudo ln -sf /etc/nginx/sites-available/xxl-job /etc/nginx/sites-enabled/
sudo service nginx restart

echo ""
echo "========================================"
echo "  部署完成！"
echo "  前端: http://localhost:8080"
echo "  后端: http://localhost:8081/xxl-job-admin"
echo "  日志: tail -f /opt/xxl-job/console.log"
echo "========================================"
