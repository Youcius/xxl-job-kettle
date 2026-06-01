# XXL-JOB Kettle

基于 [XXL-JOB](https://github.com/xuxueli/xxl-job) 的分布式任务调度平台，扩展了 **Kettle (Pentaho Data Integration)** 集成能力。

> License: GPL v3，遵循原项目开源协议。

## 功能特性

| 模块 | 功能 |
|------|------|
| 仪表盘 | 任务统计、执行器状态、最近调度日志 |
| 任务管理 | 创建/编辑/启停/触发 Cron 定时任务 |
| 调度日志 | 查看执行记录、分发状态、运行结果 |
| 执行器管理 | 注册执行器、查看在线状态 |
| 用户管理 | 系统用户增删改查、角色权限控制 |
| **Kettle 管理** | 上传 .ktr/.kjb、一键创建调度任务、版本管理 |
| 国际化 | 中 / English 切换 |

## Kettle 集成架构

```
Windows (开发)                  Linux (生产)
+------------------+           +-------------------------+
| Spoon 开发作业    |   上传    | XXL-JOB Admin           |
| 保存 .ktr/.kjb    | -------> | 执行器调用 pan.sh        |
| XXL-JOB 前端      |           | -> Kettle 引擎执行 ETL  |
+------------------+           | -> 源库 --同步--> 目标库  |
                               +-------------------------+
```

## 项目结构

```
xxl-job-kettle/
+-- server/                    后端 (Spring Boot + MyBatis)
|   +-- xxl-job-admin/         Admin 调度中心
|   +-- xxl-job-core/          核心库
|   +-- xxl-job-executor-samples/
+-- web/                       前端 (Vue 3 + Element Plus)
|   +-- src/
|       +-- views/             页面组件
|       +-- i18n/              中英文语言包
|       +-- api/               API 请求
+-- deploy/
    +-- docker-compose.yml     一键部署
    +-- nginx.conf             Nginx 配置
    +-- start-dev.bat          Windows 开发启动脚本
```

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Java 8, Spring Boot 2.7, MyBatis, MySQL |
| 前端 | Vue 3, Vite, Element Plus, Vue Router, Pinia |
| 国际化 | vue-i18n |
| 部署 | Docker + Nginx |

## 快速开始（开发环境）

```bash
# 1. 启动 MySQL
# 2. 导入表结构
mysql -u root -p < server/xxl-job-admin/src/main/resources/sql/tables_xxl_job.sql
mysql -u root -p < server/xxl-job-admin/src/main/resources/sql/tables_xxl_kettle.sql

# 3. 启动后端
cd server/xxl-job-admin
mvn spring-boot:run

# 4. 启动前端
cd web
npm install && npm run dev

# 访问 http://localhost:5173
# 默认账号: admin / 123456
```

## Docker 部署

```bash
# 1. 编译
cd server && mvn clean package -DskipTests
cd web && npm install && npm run build

# 2. 启动
cd deploy
docker-compose up -d

# 访问 http://localhost
```

## Kettle 使用流程

1. 在 Windows 上用 **Spoon** 开发 Kettle 作业(.ktr/.kjb)
2. 登录 XXL-JOB 管理后台 -> **Kettle 管理** -> 新建分组
3. 进入分组 -> 上传 .ktr/.kjb 文件
4. 点击 **一键创建调度任务** -> 自动生成 Shell 脚本任务
5. 在**任务管理**中点击 :zap: 执行一次
6. 在**调度日志**中查看执行结果

## 环境要求

- JDK 8+ (推荐 JDK 11，兼容 Kettle 9.0)
- MySQL 8.0
- Node.js 18+
- Maven 3.6+
- Kettle 9.0 (可选，生产环境需要)

## 致谢

- [XXL-JOB](https://github.com/xuxueli/xxl-job) by 许献立 (xuxueli)
- [Element Plus](https://element-plus.org/)
- [Vue I18n](https://vue-i18n.intlify.dev/)
