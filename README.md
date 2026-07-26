# 栏轩阁 · 后端服务

栏轩阁个人博客系统的后端 API 服务，基于 **Spring Boot 4 + Java 21**，提供文章、项目、图库、说说等内容的 RESTful API。

前端仓库：[pc-Blog/next](https://github.com/pc-Blog/next)

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Spring Boot 4.0.6 + Spring MVC |
| 语言 | Java 21 |
| 数据库 | PostgreSQL 16 |
| ORM | MyBatis-Plus 3.5.15（注解模式，无 XML 映射） |
| 缓存 | Redis (Lettuce) |
| 文件存储 | MinIO 8.5.2 |
| 安全 | Spring Security + JWT (jjwt 0.12.6) + BCrypt |
| 工具 | Lombok、FastJSON 2.51、SimpleMagic |
| 部署 | Docker（多阶段构建，镜像 ~195MB） |

---

## 前置要求

| 依赖 | 版本要求 |
|------|---------|
| Docker | 24+ |
| Docker Compose | 2.20+ |

> 本地开发还需要 JDK 21 和 Maven 3.9+

---

## 快速启动（Docker 推荐）

### 1. 克隆项目

```bash
git clone https://github.com/pc-Blog/springBoot.git
cd Blog
```

### 2. 配置参数

编辑 `src/main/resources/args.yaml`，按实际环境修改数据库、Redis、MinIO 等配置。

> Docker 环境下数据库等组件走容器内网，服务名即容器名（如 `bg-postgres`）。本地开发使用 `args-dev.yaml`。

### 3. 构建并启动

```bash
# 构建镜像
docker build -t bg-api:latest .

# 启动所有服务（PostgreSQL、Redis、MinIO、Nginx、前端）
docker compose -f docker-compose.yml up -d
```

启动后访问：`http://localhost:8080`

> **首次部署后**：PostgreSQL 需执行建表脚本（`docker exec -i bg-postgres psql -U postgres -d blog < data/init.sql`），MinIO 需在控制台 `http://localhost:19000` 创建 `blog` 存储桶。

---

## 本地开发（传统 Maven）

```bash
# 使用本地开发配置
cp src/main/resources/args-dev.yaml src/main/resources/application.yaml

# 编译
mvn clean package -DskipTests -s .mvn/settings.xml

# 启动
java -jar target/ROOT.jar
```

> `.mvn/settings.xml` 配置了阿里云 Maven 镜像加速国内下载。

---

## 项目结构

```
Blog/
├── src/main/
│   ├── java/blog/
│   │   ├── common/          # 通用工具类、统一响应体（PageDTO、PageVO、Result）
│   │   ├── config/          # Spring 配置（Security、CORS、MinIO、Jackson、MyBatis-Plus）
│   │   ├── controller/      # RESTful API 控制器
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # MyBatis-Plus 实体类
│   │   ├── exception/       # 全局异常处理
│   │   ├── mapper/          # MyBatis-Plus Mapper 接口
│   │   ├── service/         # 业务接口 + impl 实现
│   │   ├── util/            # 工具类（JWT、MinIO、分页）
│   │   └── vo/              # 视图对象
│   └── resources/
│       ├── args.yaml                  # Docker 环境参数
│       ├── args-dev.yaml              # 本地开发参数
│       └── logback-spring.xml         # 日志配置
├── data/
│   ├── init.sql                       # 建表脚本
│   └── migration/                     # 增量迁移脚本
├── .mvn/settings.xml                  # Maven 镜像配置（阿里云加速）
├── Dockerfile                         # 多阶段构建
├── docker-compose.yml                 # 服务编排
├── .dockerignore
├── pom.xml
└── README.md
```

---

## API 概览

所有接口前缀：`/api`

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `/api/auth/**` | 登录、GitHub OAuth、当前用户信息 |
| 文章 | `/api/article/**` | 文章 CRUD、公开查询、浏览量、置顶 |
| 项目 | `/api/project/**` | 项目 CRUD、公开查询 |
| 分类 | `/api/category/**` | 分类管理 |
| 标签 | `/api/tag/**` | 标签管理 |
| 评论 | `/api/comment/**` | 评论管理 |
| 说说 | `/api/chatter/**` | 说说动态 |
| 图库 | `/api/album/**`、`/api/photo/**` | 相册与照片 |
| 媒体 | `/api/media/**` | 文件上传与管理（MinIO） |
| 技能 | `/api/skill/**` | 技能管理 |
| 时间线 | `/api/timeline/**` | 学习历程 |
| 友链 | `/api/friend-link/**` | 友情链接 |
| 关于 | `/api/about/**` | 个人配置 |
| 仪表盘 | `/api/dashboard` | 站点概览数据 |
| 文学 | `/api/op/**` | 文学创作 |
| 用户 | `/api/user/**` | 用户管理 |
| 数据同步 | `/api/sync/**` | Worker 数据备份（需 ADMIN_TOKEN） |

---

## 部署架构

```
nginx:80       → 反向代理
  ├→ bg-blog:3000       前端（Next.js）
  └→ bg-api:8080         后端（本服务）
bg-redis:6379            缓存
bg-postgres:5432         数据库
bg-minio:9000/9001       对象存储
```

---

## Docker 构建说明

多阶段构建，最终镜像仅包含运行所需的 JRE 和 JAR 包，约 **195MB**。

```dockerfile
# 第一阶段：编译（Maven + JDK）
FROM maven:3.9-eclipse-temurin-21 AS build
# 第二阶段：运行（仅 JRE）
FROM eclipse-temurin:21-jre
```

首次构建因需下载 Maven 依赖较慢，后续修改仅重新编译，秒级完成。

---

## 相关项目

- [pc-Blog/next](https://github.com/pc-Blog/next) — 前端博客系统（Next.js 16）

---

## License

MIT
