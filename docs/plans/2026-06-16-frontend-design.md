# ArmPro-Server 前端实现计划

## 项目概述

为ArmPro-Server添加完整的前端系统，包括管理后台和用户自助平台。

## 技术栈

- **前端框架:** Vue 3 + Vite
- **UI组件库:** Element Plus
- **状态管理:** Pinia
- **路由:** Vue Router 4
- **HTTP客户端:** Axios
- **图表库:** ECharts
- **部署方式:** 静态文件部署（Nginx）

## 后端适配

在现有Netty服务器基础上添加HTTP REST API层，使用Spring Boot或独立的HTTP服务器。

---

## 实现阶段

### 阶段一：项目初始化（30分钟）

#### 任务1.1：创建Vue 3项目
- 使用Vite创建Vue 3项目
- 配置TypeScript支持
- 配置ESLint和Prettier
- **输出:** `frontend/` 目录

#### 任务1.2：安装依赖
- 安装Element Plus
- 安装Vue Router、Pinia、Axios
- 安装ECharts
- **输出:** package.json配置完成

#### 任务1.3：项目结构搭建
```
frontend/
├── src/
│   ├── api/          # API请求封装
│   ├── assets/       # 静态资源
│   ├── components/   # 公共组件
│   ├── layouts/      # 布局组件
│   ├── router/       # 路由配置
│   ├── stores/       # 状态管理
│   ├── utils/        # 工具函数
│   ├── views/        # 页面视图
│   │   ├── admin/    # 管理后台页面
│   │   └── user/     # 用户平台页面
│   ├── App.vue
│   └── main.ts
├── public/
├── index.html
├── vite.config.ts
└── package.json
```

---

### 阶段二：后端HTTP API层（60分钟）

#### 任务2.1：添加Spring Boot Web依赖
- 在build.gradle中添加spring-boot-starter-web
- 配置HTTP服务器端口（如8080）
- **输出:** HTTP服务器可启动

#### 任务2.2：创建REST Controller
- 复用现有SocketController的业务逻辑
- 创建HttpController类
- 实现统一的响应格式
- **输出:** HTTP API接口

#### 任务2.3：实现用户相关API
```
POST /api/user/login          # 账号登录
POST /api/user/register       # 账号注册
GET  /api/user/info           # 获取用户信息
POST /api/user/change-password # 修改密码
POST /api/user/recharge       # 卡密充值
```

#### 任务2.4：实现应用管理API
```
GET    /api/soft/list         # 获取应用列表
GET    /api/soft/detail       # 获取应用详情
POST   /api/soft/save         # 保存应用配置
DELETE /api/soft/delete       # 删除应用
GET    /api/soft/module       # 获取模块配置
POST   /api/soft/module       # 保存模块配置
```

#### 任务2.5：实现卡密管理API
```
GET    /api/card/list         # 获取卡密列表
POST   /api/card/generate     # 生成卡密
DELETE /api/card/delete       # 删除卡密
POST   /api/card/update       # 更新卡密
```

#### 任务2.6：实现任务管理API
```
GET    /api/task/info         # 获取任务信息
POST   /api/task/stop         # 终止任务
```

#### 任务2.7：实现系统管理API
```
GET    /api/system/notices    # 获取公告列表
GET    /api/system/versions   # 获取版本列表
GET    /api/system/config     # 获取系统配置
```

#### 任务2.8：实现统计API
```
GET    /api/statistics/overview  # 获取统计概览
GET    /api/statistics/chart     # 获取图表数据
```

---

### 阶段三：前端基础架构（45分钟）

#### 任务3.1：配置路由
- 创建路由配置文件
- 实现路由守卫（登录验证）
- 配置路由懒加载
- **输出:** router/index.ts

#### 任务3.2：配置状态管理
- 创建用户store
- 创建应用store
- 创建系统store
- **输出:** stores/目录

#### 任务3.3：封装API请求
- 创建Axios实例
- 实现请求拦截器（添加token）
- 实现响应拦截器（处理错误）
- 封装各个模块的API请求
- **输出:** api/目录

#### 任务3.4：创建布局组件
- 管理后台布局（侧边栏+顶部导航+内容区）
- 用户平台布局（顶部导航+内容区）
- **输出:** layouts/目录

#### 任务3.5：创建公共组件
- 页面头部组件
- 侧边栏组件
- 表格组件
- 表单组件
- 图表组件
- **输出:** components/目录

---

### 阶段四：用户平台页面（60分钟）

#### 任务4.1：登录页面
- 账号密码登录
- 第三方登录（预留）
- 记住登录状态
- **输出:** views/user/Login.vue

#### 任务4.2：注册页面
- 用户名、密码、邮箱注册
- 表单验证
- **输出:** views/user/Register.vue

#### 任务4.3：用户首页
- 用户信息展示
- 今日任务统计
- 应用数量统计
- 快捷操作入口
- **输出:** views/user/Dashboard.vue

#### 任务4.4：应用列表页面
- 应用列表表格
- 分页查询
- 搜索过滤
- 操作按钮（查看、编辑、删除）
- **输出:** views/user/SoftList.vue

#### 任务4.5：应用详情页面
- 应用基本信息
- 功能开关配置
- 统计图表展示
- **输出:** views/user/SoftDetail.vue

#### 任务4.6：应用配置页面
- 远程公告配置
- 单码验证配置
- 更新配置
- 自定义模块配置
- 广告配置
- **输出:** views/user/SoftConfig.vue

#### 任务4.7：卡密管理页面
- 卡密列表
- 生成卡密
- 批量操作（删除、冻结）
- **输出:** views/user/CardManage.vue

#### 任务4.8：个人中心页面
- 修改密码
- 充值卡密
- 账号信息
- **输出:** views/user/Profile.vue

---

### 阶段五：管理后台页面（60分钟）

#### 任务5.1：管理后台首页
- 系统概览统计
- 用户数量、应用数量、任务数量
- 今日活跃用户趋势
- 收入统计
- **输出:** views/admin/Dashboard.vue

#### 任务5.2：用户管理页面
- 用户列表
- 搜索过滤
- 用户详情
- 封禁/解封操作
- **输出:** views/admin/UserManage.vue

#### 任务5.3：应用管理页面
- 所有应用列表
- 应用详情查看
- 强制删除应用
- **输出:** views/admin/SoftManage.vue

#### 任务5.4：卡密管理页面
- 卡密列表
- 批量生成卡密
- 卡密使用统计
- **输出:** views/admin/CardManage.vue

#### 任务5.5：任务管理页面
- 任务列表
- 任务状态监控
- 强制终止任务
- **输出:** views/admin/TaskManage.vue

#### 任务5.6：系统配置页面
- 系统参数配置
- 会员等级配置
- 上传配置
- **输出:** views/admin/SystemConfig.vue

#### 任务5.7：公告管理页面
- 公告列表
- 发布公告
- 编辑/删除公告
- **输出:** views/admin/NoticeManage.vue

#### 任务5.8：版本管理页面
- 版本列表
- 发布新版本
- 版本说明编辑
- **输出:** views/admin/VersionManage.vue

---

### 阶段六：功能完善（45分钟）

#### 任务6.1：国际化支持
- 配置vue-i18n
- 中文语言包
- 英文语言包（可选）
- **输出:** i18n配置

#### 任务6.2：主题定制
- Element Plus主题定制
- 暗黑模式支持（可选）
- **输出:** 主题配置

#### 任务6.3：权限控制
- 路由权限控制
- 按钮权限控制
- 管理员/普通用户区分
- **输出:** 权限系统

#### 任务6.4：数据可视化
- ECharts图表封装
- 用户增长趋势图
- 应用使用统计图
- 收入统计图
- **输出:** 图表组件

#### 任务6.5：错误处理
- 全局错误处理
- 网络错误提示
- 401/403处理
- **输出:** 错误处理机制

---

### 阶段七：测试与优化（30分钟）

#### 任务7.1：功能测试
- 登录注册流程测试
- 应用管理流程测试
- 卡密管理流程测试
- **输出:** 测试报告

#### 任务7.2：性能优化
- 路由懒加载
- 组件懒加载
- 图片懒加载
- 接口缓存
- **输出:** 优化完成

#### 任务7.3：打包部署
- 配置生产环境
- 打包优化
- Nginx配置示例
- **输出:** 部署文档

---

## 文件结构

```
ArmPro-Server/
├── frontend/                    # 前端项目
│   ├── src/
│   │   ├── api/
│   │   │   ├── user.ts
│   │   │   ├── soft.ts
│   │   │   ├── card.ts
│   │   │   ├── task.ts
│   │   │   └── system.ts
│   │   ├── assets/
│   │   ├── components/
│   │   │   ├── Layout/
│   │   │   ├── Chart/
│   │   │   └── Common/
│   │   ├── layouts/
│   │   │   ├── AdminLayout.vue
│   │   │   └── UserLayout.vue
│   │   ├── router/
│   │   │   └── index.ts
│   │   ├── stores/
│   │   │   ├── user.ts
│   │   │   ├── soft.ts
│   │   │   └── system.ts
│   │   ├── utils/
│   │   │   ├── request.ts
│   │   │   └── auth.ts
│   │   ├── views/
│   │   │   ├── admin/
│   │   │   │   ├── Dashboard.vue
│   │   │   │   ├── UserManage.vue
│   │   │   │   ├── SoftManage.vue
│   │   │   │   ├── CardManage.vue
│   │   │   │   ├── TaskManage.vue
│   │   │   │   ├── SystemConfig.vue
│   │   │   │   ├── NoticeManage.vue
│   │   │   │   └── VersionManage.vue
│   │   │   └── user/
│   │   │       ├── Login.vue
│   │   │       ├── Register.vue
│   │   │       ├── Dashboard.vue
│   │   │       ├── SoftList.vue
│   │   │       ├── SoftDetail.vue
│   │   │       ├── SoftConfig.vue
│   │   │       ├── CardManage.vue
│   │   │       └── Profile.vue
│   │   ├── App.vue
│   │   └── main.ts
│   ├── public/
│   ├── index.html
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── package.json
├── src/
│   └── main/java/armadillo/
│       └── controller/
│           ├── SocketController.java  # 现有TCP接口
│           └── HttpController.java    # 新增HTTP接口
└── build.gradle
```

---

## 执行模式

### 模式A：子代理驱动（推荐）
- 使用sessions_spawn并行执行各个任务
- 每个任务完成后自动审查
- 适合快速开发

### 模式B：手动执行
- 按照计划逐步执行
- 每个任务完成后手动确认
- 适合学习和理解

---

## 预计时间

- 阶段一：30分钟
- 阶段二：60分钟
- 阶段三：45分钟
- 阶段四：60分钟
- 阶段五：60分钟
- 阶段六：45分钟
- 阶段七：30分钟

**总计：约5小时**

---

## 注意事项

1. 后端HTTP API需要复用现有业务逻辑，避免代码重复
2. 前端需要处理好与TCP协议的差异（HTTP是无状态的）
3. 注意安全性：token存储、XSS防护、CSRF防护
4. 考虑性能：接口缓存、图片压缩、代码分割
