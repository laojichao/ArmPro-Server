# ArmPro-Server 前端项目总结

## 项目概述

为ArmPro-Server添加了完整的前端系统，包括管理后台和用户自助平台。

## 技术栈

- **前端框架:** Vue 3.5 + TypeScript 6.0
- **构建工具:** Vite 8.0
- **UI组件库:** Element Plus 2.14
- **状态管理:** Pinia 3.0
- **路由:** Vue Router 4.6
- **HTTP客户端:** Axios 1.18
- **图表库:** ECharts 6.1

## 项目结构

```
ArmPro-Server/
├── frontend/                          # 前端项目
│   ├── src/
│   │   ├── api/                       # API请求封装
│   │   │   ├── user.ts                # 用户端API
│   │   │   └── admin.ts               # 管理端API
│   │   ├── assets/                    # 静态资源
│   │   ├── components/                # 公共组件
│   │   │   ├── Chart/index.vue        # ECharts图表组件
│   │   │   └── Common/Pagination.vue  # 分页组件
│   │   ├── composables/               # 组合式函数
│   │   │   └── useDarkMode.ts         # 暗黑模式
│   │   ├── layouts/                   # 布局组件
│   │   │   ├── MainLayout.vue         # 主布局
│   │   │   └── UserLayout.vue         # 用户平台布局
│   │   ├── router/                    # 路由配置
│   │   │   └── index.ts
│   │   ├── stores/                    # 状态管理
│   │   │   └── user.ts                # 用户状态
│   │   ├── utils/                     # 工具函数
│   │   │   ├── auth.ts                # 认证工具
│   │   │   └── request.ts             # Axios封装
│   │   ├── views/                     # 页面视图
│   │   │   ├── admin/                 # 管理后台页面
│   │   │   │   ├── Dashboard.vue      # 管理首页
│   │   │   │   ├── UserManage.vue     # 用户管理
│   │   │   │   ├── SoftManage.vue     # 应用管理
│   │   │   │   ├── CardManage.vue     # 卡密管理
│   │   │   │   ├── TaskManage.vue     # 任务管理
│   │   │   │   ├── SystemConfig.vue   # 系统配置
│   │   │   │   ├── NoticeManage.vue   # 公告管理
│   │   │   │   └── VersionManage.vue  # 版本管理
│   │   │   └── user/                  # 用户平台页面
│   │   │       ├── Login.vue          # 登录页
│   │   │       ├── Register.vue       # 注册页
│   │   │       ├── Dashboard.vue      # 用户首页
│   │   │       ├── SoftList.vue       # 应用列表
│   │   │       ├── SoftDetail.vue     # 应用详情
│   │   │       ├── SoftConfig.vue     # 应用配置
│   │   │       ├── CardManage.vue     # 卡密管理
│   │   │       └── Profile.vue        # 个人中心
│   │   ├── App.vue                    # 根组件
│   │   └── main.ts                    # 入口文件
│   ├── .env.development               # 开发环境配置
│   ├── .env.production                # 生产环境配置
│   ├── index.html                     # HTML入口
│   ├── package.json                   # 依赖配置
│   ├── tsconfig.json                  # TypeScript配置
│   └── vite.config.ts                 # Vite配置
├── src/
│   └── main/java/armadillo/
│       ├── config/
│       │   ├── CorsConfig.java        # 跨域配置
│       │   └── SpringBootApp.java     # Spring Boot启动类
│       ├── controller/
│       │   ├── SocketController.java  # 现有TCP接口
│       │   ├── HttpController.java    # HTTP API接口
│       │   └── AdminController.java   # 管理端API
│       └── result/
│           └── ApiResponse.java       # 统一响应类
└── build.gradle                       # 构建配置
```

## 启动命令

### 前端开发模式
```bash
cd D:\WorkSpace\IdeaProjects\ArmPro-Server\frontend
npm run dev
```
访问 http://localhost:3000

### 前端生产构建
```bash
cd D:\WorkSpace\IdeaProjects\ArmPro-Server\frontend
npm run build
```
构建产物在 `frontend/dist/` 目录

### 后端启动
```bash
cd D:\WorkSpace\IdeaProjects\ArmPro-Server
gradle run
```
- Netty TCP服务器: 默认端口
- Spring Boot HTTP服务器: 8080端口

## API接口

### 用户端API (/api/user/)
- POST /api/user/login - 用户登录
- POST /api/user/register - 用户注册
- GET /api/user/info - 获取用户信息
- POST /api/user/change-password - 修改密码
- POST /api/user/recharge - 卡密充值

### 应用管理API (/api/soft/)
- GET /api/soft/list - 获取应用列表
- GET /api/soft/detail - 获取应用详情
- POST /api/soft/save - 保存应用配置
- DELETE /api/soft/delete - 删除应用

### 卡密管理API (/api/card/)
- GET /api/card/list - 获取卡密列表
- POST /api/card/generate - 生成卡密
- DELETE /api/card/delete - 删除卡密

### 任务管理API (/api/task/)
- GET /api/task/info - 获取任务信息
- POST /api/task/stop - 终止任务

### 系统管理API (/api/system/)
- GET /api/system/notices - 获取公告列表
- GET /api/system/versions - 获取版本列表

### 管理端API (/api/admin/)
- GET /api/admin/dashboard - 管理首页数据
- GET /api/admin/users - 用户列表
- GET /api/admin/softs - 应用列表
- GET /api/admin/cards - 卡密列表
- GET /api/admin/tasks - 任务列表
- GET /api/admin/config - 系统配置
- POST /api/admin/config - 保存系统配置
- GET /api/admin/notices - 公告列表
- POST /api/admin/notices - 发布公告
- GET /api/admin/versions - 版本列表
- POST /api/admin/versions - 发布版本

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

### 其他特性
- 响应式布局
- 暗黑模式支持
- 中文界面
- Token认证
- 路由守卫
- 错误处理

## 部署说明

### 开发环境
1. 启动后端服务（Netty + Spring Boot）
2. 启动前端开发服务器
3. 访问 http://localhost:3000

### 生产环境
1. 构建前端：`npm run build`
2. 将 `frontend/dist/` 部署到Nginx
3. 配置Nginx反向代理到后端8080端口

### Nginx配置示例
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

## 注意事项

1. 确保后端MySQL、Redis服务正常运行
2. 配置正确的数据库连接信息
3. 配置七牛云或本地上传参数
4. 生产环境建议使用HTTPS
5. 定期备份数据库

## 后续优化建议

1. 添加单元测试
2. 添加E2E测试
3. 优化打包体积
4. 添加PWA支持
5. 添加多语言支持
6. 优化移动端体验
