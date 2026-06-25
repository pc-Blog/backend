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
| 部署 | Maven 3.9+ → WAR 包 → Tomcat 10 |

---

## 前置要求

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| JDK | >= 21 | 必需 |
| Maven | >= 3.9 | 构建工具 |
| PostgreSQL | 16+ | 数据库 |
| Redis | 7+ | 缓存 |
| MinIO | 8.5.2+ | 对象存储（图片/文件） |

---

## 快速启动

### 1. 克隆项目

```bash
git clone https://github.com/pc-Blog/springBoot.git
cd Blog
```

### 2. 初始化数据库

```bash
psql -U postgres -c "CREATE DATABASE blog;"
psql -U postgres -d blog -f data/init.sql
```

### 3. 配置参数

配置文件位于 `src/main/resources/args.yaml`，按实际环境修改：

```yaml
postgresql:
  url: "localhost"      # 数据库地址
  port: 5432            # 数据库端口
  username: postgres    # 数据库用户名
  password: 123456      # 数据库密码

redis:
  url: "localhost"      # Redis 地址
  port: 6379            # Redis 端口
  password: 123456      # Redis 密码

minio:
  clientPoint: http://localhost:19090  # MinIO 访问地址
  accessKey: minioadmin                # MinIO 密钥
  secretKey: minioadmin
  bucket: blog                         # 存储桶名称

internet:
  PORT: 8080           # 服务端口

jwt:
  secret: 【必填】JWT 签名密钥（从 git 历史清理，请轮换后回填）  # JWT 密钥（生产环境请修改）
  expiration: 604800   # Token 过期时间（秒，默认 7 天）
```

> **注意**：`args.yaml` 供 Docker 环境使用（数据库等走容器内网），本地开发使用 `args-dev.yaml`，其中所有地址默认指向 `localhost`。

### 4. 构建并启动

```bash
# 打包 WAR
mvn clean package -DskipTests

# 直接启动（内嵌 Tomcat）
java -jar target/ROOT.war

# 或部署到外置 Tomcat（推荐生产）
# 将 target/ROOT.war 复制到 Tomcat webapps 目录
```

启动后访问：`http://localhost:8080`

---

## 项目结构

```
Blog/
├── src/main/
│   ├── java/blog/
│   │   ├── common/          # 通用工具类、统一响应体（PageDTO、PageVO、Result）
│   │   ├── config/          # Spring 配置（Security、CORS、MinIO、Jackson、MyBatis-Plus）
│   │   ├── controller/      # RESTful API 控制器（17 个）
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # MyBatis-Plus 实体类（17 张表）
│   │   ├── exception/       # 全局异常处理
│   │   ├── mapper/          # MyBatis-Plus Mapper 接口
│   │   ├── service/         # 业务接口 + impl 实现
│   │   ├── util/            # 工具类（JWT、MinIO、分页）
│   │   └── vo/              # 视图对象（文章/项目详情、列表等）
│   └── resources/
│       ├── application.yaml           # 主配置
│       ├── args.yaml                  # Docker 环境参数
│       ├── args-dev.yaml              # 本地开发参数
│       └── logback-spring.xml         # 日志配置
├── data/
│   ├── init.sql                       # 统一建表脚本（17 张表）
│   └── migration/                     # 增量迁移脚本
├── pom.xml                            # Maven 依赖配置
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

---

## 部署架构

生产环境通过 Docker Compose 统一编排（与前端共用）：

```
nginx:80       → 反向代理
  ├→ next:3000      前端（Next.js）
  └→ tomcat:18016   后端（本项目的 WAR 包）
redis:6379          缓存
postgres:5432       数据库
```

---

## 相关项目

- [pc-Blog/next](https://github.com/pc-Blog/next) — 前端博客系统（Next.js 16）

---

## License

MIT
