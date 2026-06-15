# ArmPro-Server

Arm Pro 注入器的后端服务，包含完整的前端管理系统。

## 项目简介

这是一个APK加壳/保护服务的后端系统，提供：
- APK加壳保护服务
- 用户管理系统
- 应用配置管理
- 单码验证系统
- 任务处理系统

## 技术栈

### 后端
- Java 8
- Netty (TCP Socket服务器)
- Spring Boot (HTTP API服务器)
- MyBatis (数据库ORM)
- MySQL (数据库)
- Redis (缓存)

### 前端
- Vue 3.5 + TypeScript 6.0
- Vite 8.0 (构建工具)
- Element Plus 2.14 (UI组件库)
- Pinia 3.0 (状态管理)
- Vue Router 4.6 (路由)
- ECharts 6.1 (图表库)

## 项目结构

```
ArmPro-Server/
├── frontend/                    # 前端项目
│   ├── src/
│   │   ├── api/                 # API请求封装
│   │   ├── components/          # 公共组件
│   │   ├── layouts/             # 布局组件
│   │   ├── router/              # 路由配置
│   │   ├── stores/              # 状态管理
│   │   ├── utils/               # 工具函数
│   │   └── views/               # 页面视图
│   │       ├── admin/           # 管理后台页面
│   │       └── user/            # 用户平台页面
│   ├── package.json
│   └── vite.config.ts
├── src/
│   └── main/java/armadillo/
│       ├── config/              # 配置类
│       ├── controller/          # 控制器
│       │   ├── SocketController.java  # TCP接口
│       │   ├── HttpController.java    # HTTP接口
│       │   └── AdminController.java   # 管理接口
│       └── result/              # 响应类
├── build.gradle
└── README.md
```

## 快速开始

### 环境要求
- JDK 8+
- Gradle 4.10+
- MySQL 5.7+
- Redis 5.0+
- Node.js 16+ (前端开发)

### 1. 克隆项目
```bash
git clone git@github.com:laojichao/ArmPro-Server.git
cd ArmPro-Server
```

### 2. 配置数据库
编辑 `src/main/resources/development/jdbc.properties`，配置MySQL连接信息。

### 3. 配置Redis
编辑 `src/main/resources/development/redis.properties`，配置Redis连接信息。

### 4. 启动后端服务
```bash
gradle run
```

后端服务将启动：
- Netty TCP服务器 (默认端口)
- Spring Boot HTTP服务器 (端口8080)

### 5. 启动前端开发服务器
```bash
cd frontend
npm install
npm run dev
```

前端开发服务器将启动在 http://localhost:3000

## 访问地址

- **用户平台:** http://localhost:3000/user/login
- **管理后台:** http://localhost:3000/admin/dashboard

## API接口

### 用户端API
- `POST /api/user/login` - 用户登录
- `POST /api/user/register` - 用户注册
- `GET /api/user/info` - 获取用户信息
- `POST /api/user/change-password` - 修改密码
- `POST /api/user/recharge` - 卡密充值

### 应用管理API
- `GET /api/soft/list` - 获取应用列表
- `GET /api/soft/detail` - 获取应用详情
- `POST /api/soft/save` - 保存应用配置
- `DELETE /api/soft/delete` - 删除应用

### 卡密管理API
- `GET /api/card/list` - 获取卡密列表
- `POST /api/card/generate` - 生成卡密
- `DELETE /api/card/delete` - 删除卡密

### 任务管理API
- `GET /api/task/info` - 获取任务信息
- `POST /api/task/stop` - 终止任务

### 系统管理API
- `GET /api/system/notices` - 获取公告列表
- `GET /api/system/versions` - 获取版本列表

### 管理端API
- `GET /api/admin/dashboard` - 管理首页数据
- `GET /api/admin/users` - 用户列表
- `GET /api/admin/softs` - 应用列表
- `GET /api/admin/cards` - 卡密列表
- `GET /api/admin/tasks` - 任务列表
- `GET /api/admin/config` - 系统配置
- `POST /api/admin/config` - 保存系统配置
- `GET /api/admin/notices` - 公告列表
- `POST /api/admin/notices` - 发布公告
- `GET /api/admin/versions` - 版本列表
- `POST /api/admin/versions` - 发布版本

## 功能特性

### 用户平台
- 用户登录/注册
- 应用列表查看
- 应用配置管理（远程公告、单码验证、更新、自定义模块、广告）
- 卡密管理（生成、查看、批量操作）
- 个人中心（修改密码、充值）
- 数据统计图表

### 管理后台
- 系统概览统计
- 用户管理（查看、封禁）
- 应用管理（查看、删除）
- 卡密管理（生成、查看）
- 任务管理（查看、终止）
- 系统配置
- 公告管理
- 版本管理

## 生产部署

### 1. 构建前端
```bash
cd frontend
npm run build
```

### 2. 部署前端
将 `frontend/dist/` 目录部署到Nginx或其他Web服务器。

### 3. Nginx配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 4. 启动后端服务
```bash
gradle run
```

## 相关链接

- [Arm Plus 官网](http://am-ls.cn)
- [前端项目文档](frontend/README.md)
- [API接口文档](docs/API.md)

## 许可证

本项目仅供学习交流使用。
