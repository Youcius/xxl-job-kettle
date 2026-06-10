# xxl-job-kettle · Linux Server 部署手册

> 目标：在 Ubuntu 22.04+ 服务器上一键部署 **xxl-job-admin (with Vue 3 SPA) + executor + Kettle 9.1 + MySQL**，做到 GLUE_SHELL 调起 Kettle `.ktr` 端到端跑通。

---

## 📦 部署前准备

| 组件 | 最低版本 | 用途 | 备注 |
|------|----------|------|------|
| Linux | Ubuntu 22.04 LTS | 主机系统 | 也支持 20.04 / Debian 11 |
| Docker Engine | 24.0+ | 容器运行时 | `docker --version` |
| Docker Compose | v2.20+ | 编排 | `docker compose version` |
| Kettle 9.1 | pdi-ce-9.1.0.0-324 | ETL 引擎 | 装在 **主机** `/opt/pdi/data-integration` |
| JDK 8 (Temurin) | 1.8.0_482 | Kettle 9.1 官方 JRE | 装在 **主机** `/opt/pdi/jdk8` |
| 端口 | 18081, 13306 | 暴露给客户端 | admin / MySQL |

### 1. 安装 Docker

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
newgrp docker
```

### 2. 安装 Kettle 9.1（**关键 — 必须装在主机，不在容器**）

生产 Kettle 9.1 装在 `/opt/pdi/data-integration`，JDK 8 装在 `/opt/pdi/jdk8`。本仓库的 `deploy/DEPLOY.md` 兼容这个目录结构。

```bash
ls /opt/pdi/data-integration/pan.sh   # Kettle 9.1 主目录
ls /opt/pdi/jdk8/bin/java              # JDK 8 (Temurin 1.8.0_482)
```

如果你的 Kettle 装在别的位置，按下面改 `docker-compose.yml` 的 volume mount 和 `--xxl.kettle.path` 即可。

> 📌 **为什么 Kettle 不进容器？** Kettle 9.1 + Karaf 启动慢、依赖 `.kettle` 配置目录 + 客户端 GUI（Spoon），与容器化思路冲突。装在主机后，admin 和 executor 用 volume 挂载共享，**两边看到的文件路径完全一致**。

### 3. 创建 Kettle 运行日志目录

```bash
sudo mkdir -p /opt/pdi/logs
sudo chown -R 1000:1000 /opt/pdi/logs   # 让容器内 app 用户能写
```

### 4. 准备构建产物

admin 镜像构建时需要 `server/xxl-job-admin/xxl-job-admin-2.5.0.jar` 位于 Docker build context。这个 jar 是构建/部署产物，不作为仓库源码提交。Vue 3 SPA 由 `web/dist` 通过 compose 挂载到 `/app-ui`。

```bash
cd /path/to/xxl-job-kettle
mvn -f server -pl xxl-job-admin -am -DskipTests package
npm --prefix web install
npm --prefix web run build

cp server/xxl-job-admin/target/xxl-job-admin-2.5.0.jar \
   server/xxl-job-admin/xxl-job-admin-2.5.0.jar
```

### 5. 初始化 MySQL 表

`deploy/docker-compose.yml` 已挂载 `../server/xxl-job-admin/src/main/resources/sql` 到 `/docker-entrypoint-initdb.d/`，**首次启动** MySQL 会自动执行 `tables_xxl_job.sql`（建 16 张 admin 表 + 4 张 kettle 集成表）。

> ⚠️ **如果 MySQL 容器已经启动过**，volume 里有数据就不会再执行初始化脚本。**首次部署请先 `docker compose down -v` 清掉 volume**。

---

## 🚀 一键部署

```bash
cd /path/to/xxl-job-kettle/deploy
docker compose up -d --build

# 等 MySQL 初始化 + admin 启动完成（约 60-90s）
docker compose logs -f admin | grep "Started XxlJobAdminApplication"
```

启动顺序：
1. `mysql` (healthcheck 通过)
2. `admin`（连接 MySQL，建表校验，启动 Tomcat）
3. `executor`（连 admin 注册 RPC）

### 验证

```bash
# 1. 进程都健康
docker compose ps

# 2. admin 首页
curl -sI http://localhost:18081/xxl-job-admin/

# 3. executor 注册成功
docker compose logs executor | grep -E "started|register"

# 4. Kettle 9.1 路径可读，jobs 目录可见
docker compose exec -u root admin ls /opt/pdi/data-integration/pan.sh
docker compose exec -u root executor ls /opt/pdi/data-integration/pan.sh
docker compose exec -u root admin ls /opt/pdi/jobs
```

打开浏览器：**http://your-server-ip:18081/xxl-job-admin/**  
默认账号：**admin / 123456**

---

## 🔧 关键配置说明（踩过的坑）

### A. `xxl.kettle.path` 与 `xxl.kettle.file.store-path` 已可分离

现在的增强版 admin jar 已修复脚本生成逻辑：

- `KettleFileServiceImpl.upload()` 仍然把上传文件写到 `xxl.kettle.file.store-path/<groupName>/<fileName>`
- `KettleScriptUtil.generateScript()` 会优先读取 `xxl.kettle.file.store-path`
  - 先读 JVM `-Dxxl.kettle.file.store-path`
  - 再读环境变量 `XXL_KETTLE_FILE_STORE_PATH`
  - 都没有时，才回退到 `xxl.kettle.path`

**推荐生产部署**：
- `xxl.kettle.path = /opt/pdi/data-integration`（Kettle 程序目录，里面必须有 `pan.sh` / `kitchen.sh`）
- `xxl.kettle.file.store-path = /opt/pdi/jobs`（任务文件目录）
- 上传的 `.ktr/.kjb` 落在 `/opt/pdi/jobs/{groupName}/xxx.ktr`

**自动建目录**：
- 上传时 `Files.createDirectories(storePath/groupName)` 会自动创建 `/opt/pdi/jobs` 和子目录
- 执行脚本也会先 `mkdir -p "$JOB_DIR"`，保证 group 子目录存在

### B. Kettle 9.1 与 JDK 8 官方版本最稳

- ✅ **JDK 8 (Temurin 1.8.0_482)** — Kettle 9.1 官方支持的版本（生产推荐）
- ✅ **JDK 11 (Temurin)** — 兼容，需要 `spoon.sh` 补丁（去掉 `-Djava.endorsed.dirs`）
- ❌ **JDK 17** — Kettle 反射 `sun.net.www.protocol.jar.JarFileFactory` 被模块系统封死，启动就崩

**生产**用 `PENTAHO_JAVA_HOME=/opt/pdi/jdk8`，executor 调 pan.sh 时用 JDK 8。

admin 用 Java 17（Spring Boot 2.7 + Vue 3 嵌入），executor 用 Java 11。**这是预期设计**。

### C. Kettle 9.1 字段值里 `${var}` 不替换

Kettle 9.1 在以下位置替换 `${var}`：
- ✅ Text File Output **filename**
- ✅ Table Input **SQL**
- ✅ Get Variable / Set Variables 步骤

**不会**替换的位置：
- ❌ Generate Rows 的 `<limit>`（行数）
- ❌ 字段的 `nullif` 默认值
- ❌ Add Constants 步骤的 nullif

如需在字段值里用参数，用 Kettle 表达式（`||`）或 Get Variable 步骤。

### D. executorParam 与 Kettle 参数

KettleScriptUtil 会把 `executorParam` 按空白切分后追加到 `pan.sh` / `kitchen.sh` 调用后面。需要变量替换时，请使用 Kettle 支持的 `-param:KEY=VAL` 形式。

实战用法：
- **简单转换**：硬编码配置跑通 ✓
- **参数化转换**：在 Kettle 转换里用 SQL / 文件名变量 ✓
- **频繁改参数**：Spoon UI 设计好 .ktr → 重新上传到 admin

### E. Kettle `${var}` 替换必须用 `pan -param:KEY=VAL` 设置

```bash
pan.sh -file=trans.ktr -param:count=5 -param:message=Hello
```

转换里**先声明**参数：
```xml
<info>
  <parameters>
    <parameter>
      <name>count</name>
      <default_value>10</default_value>
      <description>rows to generate</description>
    </parameter>
  </parameters>
</info>
```

### F. executor 不要暴露 9999 端口

executor 只对 admin 容器讲 RPC，不需要公网访问。`docker-compose.yml` **不**映射 9999 到 host。

---

## 🛠 运维常用

### 看日志

```bash
# admin
docker compose logs -f admin

# executor
docker compose logs -f executor

# Kettle 9.1 每次执行的转换日志
ls -lt /opt/pdi/logs/  | head -20
tail -f /opt/pdi/logs/transformation_<jobid>_<timestamp>.log
```

### 重启单个服务

```bash
docker compose restart admin
docker compose restart executor
```

### 升级版本（更新 jar）

```bash
# 1. 停服务
docker compose down

# 2. 替换 admin jar 和 Vue dist（保留挂载的数据）
cp server/xxl-job-admin/target/xxl-job-admin-2.5.0.jar server/xxl-job-admin/
npm --prefix web run build

# 3. 重新构建镜像
docker compose build admin executor

# 4. 启动
docker compose up -d
```

### 清空全部（慎用）

```bash
docker compose down -v   # 会删 mysql volume + kettle 日志
```

## ✅ 上线 Checklist

- [ ] Kettle 9.1 已装到 `/opt/pdi/data-integration`
- [ ] MySQL volume 已建（首次启动会执行 `tables_xxl_job.sql`）
- [ ] `server/xxl-job-admin/xxl-job-admin-2.5.0.jar` 已由当前源码构建并复制到 Docker build context
- [ ] `web/dist` 已由当前前端源码构建
- [ ] `xxl.kettle.path=/opt/pdi/data-integration`
- [ ] `xxl.kettle.file.store-path=/opt/pdi/jobs`
- [ ] `xxl.job.executor.appname=xxl-job-executor-kettle`（与 admin group 配对）
- [ ] executor 容器 base image = `eclipse-temurin:11-jre-jammy`
- [ ] Kettle 文件上传后，调度前先在 Spoon UI 里试跑一次 `.ktr`，确认 XML 没语法错

---

## 🆘 排错速查

| 现象 | 原因 | 解法 |
|------|------|------|
| admin 起不来，连 MySQL 报 access denied | 密码错 / MySQL 没初始化 | 查 `tables_xxl_job.sql` 是否已建表 |
| admin 起不来，KettleFile upload 报权限错 | `/opt/pdi/jobs` 不可写 | `mkdir -p /opt/pdi/jobs /opt/pdi/logs && chown -R 1000:1000 /opt/pdi/jobs /opt/pdi/logs` |
| executor 注册成功，调度任务时找不到 .ktr | `/opt/pdi/jobs` 没挂载到 admin/executor，或 `XXL_KETTLE_FILE_STORE_PATH` 没配 | 检查 compose 里的 jobs volume 和环境变量 |
| 调度任务时 pan.sh 报 Java module error | executor 用了 Java 17 | 改回 Java 11 base image（已修） |
| pan.sh 报 `-Djava.endorsed.dirs not found` | Kettle 9.1 用 Java 9+ 删的选项 | 生产通过 `PENTAHO_JAVA_HOME=/opt/pdi/jdk8` 使用 JDK 8 |
| 调度任务 Kettle 字段值不替换 `${var}` | Kettle 9.1 设计限制 | 用 filename/SQL 替换，或 Get Variable 步骤 |
| 调度任务 Kettle 文件名里 `${var}` 也不替换 | `pan -param:` 没传 | 在任务参数里传 `-param:KEY=VAL` |

---

📌 **Kettle 集成 1 句话总结**：admin 把 `.ktr/.kjb` 上传到 `/opt/pdi/jobs`，executor 在 `/opt/pdi/data-integration` 里调 `pan.sh` / `kitchen.sh` 去跑这个绝对路径文件，日志写回 `/opt/pdi/logs`。
