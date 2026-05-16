# 高校综合信息管理系统

高校综合信息管理系统是信息系统实践课程结项项目，采用前后端分离架构，面向高校基础数据维护、人员管理、权限管理、课程公告、业务申请、附件管理、审批流转和日志审计等场景。

系统以统一登录认证、RBAC 权限控制、动态菜单、业务数据维护、附件管理、工作流审批和日志记录为核心，形成可演示、可扩展的 Web 信息系统。

## 技术栈

前端：

- Vue 3
- JavaScript
- Vite
- Pinia
- Vue Router
- Element Plus
- Axios
- Playwright

后端：

- Java 17
- Spring Boot 3
- Spring Security
- JWT
- MyBatis XML
- MySQL 8.0
- JUnit 5

部署：

- Ubuntu
- Nginx
- systemd
- MySQL

## 项目结构

```text
xtu_system/
├── backend/                       # Spring Boot 后端工程
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/xtu/system/
│       │   ├── common/            # 通用响应、异常、分页、工具类
│       │   ├── config/            # 安全、Web、JWT 配置
│       │   └── modules/           # 业务模块
│       └── resources/
│           ├── db/migration/      # MySQL 初始化脚本
│           └── mapper/            # MyBatis XML
├── frontend/                      # Vue 3 前端工程
│   └── src/
│       ├── api/                   # 接口封装
│       ├── components/            # 公共组件
│       ├── layout/                # 后台主布局
│       ├── router/                # 路由与权限守卫
│       ├── stores/                # 登录态、菜单、权限状态
│       └── views/                 # 页面模块
├── docs/                          # 项目计划、结项报告、数据库设计等材料
└── README.md
```

## 功能范围

结项版系统覆盖以下模块：

- 登录认证：账号密码登录、退出登录、JWT 鉴权、当前用户信息、修改密码。
- 权限体系：用户、角色、菜单、按钮权限、后端动态菜单、前端按钮级控制。
- 工作台：用户、学生、教师、课程、申请、待办等实时统计。
- 系统管理：用户管理、角色管理、菜单管理、登录日志、操作日志。
- 组织管理：部门树查询、部门下拉、新增、编辑、删除。
- 人员管理：学生管理、教师管理、导入、导出、批量删除、账号创建和解绑。
- 业务管理：课程管理、公告管理、申请管理，支持附件绑定和附件数量展示。
- 工作流：申请待办、已办、审批通过、审批驳回、流转记录。
- 附件管理：上传、下载、查询、删除，业务删除时联动清理附件。
- 审计日志：登录日志、操作日志自动记录和分页查询。

## 本地运行

### 环境要求

- JDK 17
- Maven
- Node.js 22 或兼容版本
- npm
- MySQL 8.0

### 数据库配置

默认开发库配置：

```text
地址：127.0.0.1:3306
数据库：xtu_system
账号：root
密码：123456
```

可通过环境变量覆盖：

```bash
export DB_URL='jdbc:mysql://127.0.0.1:3306/xtu_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false'
export DB_USERNAME=root
export DB_PASSWORD=123456
```

数据库初始化脚本：

```text
backend/src/main/resources/db/migration/
├── V1__init_user_module.sql
├── V2__business_modules.sql
└── V3__workflow_attachment_log.sql
```

### 启动后端

```bash
cd backend
DB_USERNAME=root DB_PASSWORD=123456 mvn spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认地址：

```text
http://localhost:5173
```

前端开发环境通过 Vite 将 `/api` 代理到后端服务。

## 默认账号

```text
管理员：admin / admin123
教师：teacher01 / teacher123
```

## 常用命令

后端编译：

```bash
cd backend
mvn -DskipTests compile
```

后端测试：

```bash
cd backend
DB_USERNAME=root DB_PASSWORD=123456 mvn test
```

前端构建：

```bash
cd frontend
npm run build
```

前端 E2E：

```bash
cd frontend
npm run test:e2e:install
npm run test:e2e
```

## 测试验收

结项验收重点覆盖：

- 管理员账号登录并访问核心页面。
- 教师账号登录后菜单和按钮权限收敛。
- 用户管理状态切换、重置密码、分配角色。
- 学生和教师新增、导入、导出、批量删除、账号创建。
- 课程、公告、申请新增、编辑、附件上传、删除联动清理。
- 申请提交、审批通过、审批驳回、流转记录查询。
- 登录日志和操作日志写入。

前端 Playwright 冒烟测试覆盖管理员核心页面访问和教师权限收敛两条关键链路。

## 部署说明

生产环境推荐使用原生部署方式：

- Nginx 托管前端静态资源。
- Nginx 反向代理 `/api` 到 Spring Boot。
- systemd 管理后端 jar。
- MySQL 保存业务数据。
- 定期备份数据库和附件目录。

## Git 协作

- 每个功能模块单独创建分支和 Pull Request。
- 每个 PR 只提交当前模块相关文件，不混入无关代码。
- 文档、前端、后端、测试、部署配置尽量分开提交。
- PR 创建后检查文件范围、提交说明和测试记录，再合并。
- 不提交 `node_modules/`、`target/`、`dist/`、上传附件和本地环境变量文件。

## 文档索引

- [项目计划书](docs/plan/project_plan.md)
- [中期与结项报告](docs/report/中期与结项报告.md)
- [数据库设计](docs/report/数据库设计.md)
