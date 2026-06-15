# ArmPro Server Frontend

ArmPro Server 前端管理平台，基于 Vue 3 + TypeScript + Vite 构建。

## 技术栈

- **框架**: Vue 3 (Composition API + `<script setup>`)
- **构建工具**: Vite 8
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **图表**: ECharts 6
- **HTTP 请求**: Axios
- **语言**: TypeScript

## 安装依赖

```bash
npm install
```

## 开发环境

```bash
npm run dev
```

启动后访问 http://localhost:3000 ，API 请求会自动代理到后端 http://localhost:8080 。

## 生产环境构建

```bash
npm run build
```

构建产物输出到 `dist` 目录。

## 预览生产构建

```bash
npm run preview
```

## 项目结构

```
frontend/
├── public/                  # 静态资源（不经过构建处理）
├── src/
│   ├── api/                 # API 接口定义
│   │   ├── index.ts         # 通用接口
│   │   ├── user.ts          # 用户端接口
│   │   └── admin.ts         # 管理端接口
│   ├── assets/              # 静态资源（经过构建处理）
│   ├── components/          # 公共组件
│   │   ├── Chart/
│   │   │   └── index.vue    # ECharts 图表组件
│   │   └── Common/
│   │       └── Pagination.vue  # 分页组件
│   ├── layouts/             # 布局组件
│   │   ├── MainLayout.vue   # 主布局（管理后台/首页）
│   │   └── UserLayout.vue   # 用户平台布局
│   ├── router/              # 路由配置
│   │   └── index.ts
│   ├── stores/              # Pinia 状态管理
│   │   └── user.ts          # 用户状态
│   ├── utils/               # 工具函数
│   │   ├── auth.ts          # 认证工具（Token 管理、登录状态检查）
│   │   └── request.ts       # Axios 请求封装
│   ├── views/               # 页面视图
│   │   ├── HomeView.vue
│   │   ├── LoginView.vue
│   │   ├── NotFoundView.vue
│   │   ├── user/            # 用户平台页面
│   │   └── admin/           # 管理后台页面
│   ├── App.vue              # 根组件
│   ├── main.ts              # 入口文件
│   ├── style.css            # 全局样式
│   └── env.d.ts             # 环境变量类型声明
├── .env.development         # 开发环境变量
├── .env.production          # 生产环境变量
├── index.html               # HTML 入口
├── vite.config.ts           # Vite 配置
├── tsconfig.json            # TypeScript 配置
└── package.json             # 项目依赖
```

## 环境变量

| 变量名 | 说明 | 开发环境 | 生产环境 |
|--------|------|----------|----------|
| `VITE_API_BASE_URL` | API 基础地址 | `/api` | `/api` |
| `VITE_APP_TITLE` | 应用标题 | ArmPro Server | ArmPro Server |
| `VITE_USE_MOCK` | 是否启用 Mock | false | false |
