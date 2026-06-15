import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: '首页' },
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录' },
  },
  // ========== 用户平台路由 ==========
  {
    path: '/user/login',
    name: 'UserLogin',
    component: () => import('@/views/user/Login.vue'),
    meta: { title: '用户登录' },
  },
  {
    path: '/user/register',
    name: 'UserRegister',
    component: () => import('@/views/user/Register.vue'),
    meta: { title: '用户注册' },
  },
  {
    path: '/user',
    name: 'UserLayout',
    component: () => import('@/layouts/UserLayout.vue'),
    redirect: '/user/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'UserDashboard',
        component: () => import('@/views/user/Dashboard.vue'),
        meta: { title: '控制台' },
      },
      {
        path: 'soft-list',
        name: 'SoftList',
        component: () => import('@/views/user/SoftList.vue'),
        meta: { title: '应用管理' },
      },
      {
        path: 'soft-detail/:id',
        name: 'SoftDetail',
        component: () => import('@/views/user/SoftDetail.vue'),
        meta: { title: '应用详情' },
      },
      {
        path: 'soft-config/:id',
        name: 'SoftConfig',
        component: () => import('@/views/user/SoftConfig.vue'),
        meta: { title: '应用配置' },
      },
      {
        path: 'card-manage',
        name: 'CardManage',
        component: () => import('@/views/user/CardManage.vue'),
        meta: { title: '卡密管理' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心' },
      },
    ],
  },
  // ========== 管理后台路由 ==========
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '控制台', icon: 'Odometer' },
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', icon: 'User' },
      },
      {
        path: 'softs',
        name: 'AdminSofts',
        component: () => import('@/views/admin/SoftManage.vue'),
        meta: { title: '应用管理', icon: 'Grid' },
      },
      {
        path: 'cards',
        name: 'AdminCards',
        component: () => import('@/views/admin/CardManage.vue'),
        meta: { title: '卡密管理', icon: 'Ticket' },
      },
      {
        path: 'tasks',
        name: 'AdminTasks',
        component: () => import('@/views/admin/TaskManage.vue'),
        meta: { title: '任务管理', icon: 'List' },
      },
      {
        path: 'config',
        name: 'AdminConfig',
        component: () => import('@/views/admin/SystemConfig.vue'),
        meta: { title: '系统配置', icon: 'Setting' },
      },
      {
        path: 'notices',
        name: 'AdminNotices',
        component: () => import('@/views/admin/NoticeManage.vue'),
        meta: { title: '公告管理', icon: 'Bell' },
      },
      {
        path: 'versions',
        name: 'AdminVersions',
        component: () => import('@/views/admin/VersionManage.vue'),
        meta: { title: '版本管理', icon: 'Upload' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: '404' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || 'ArmPro Server'

  // 检查是否需要登录认证
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some((record) => record.meta.requiresAdmin)
  const token = localStorage.getItem('token')

  if (requiresAuth && !token) {
    // 未登录，跳转到登录页
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (requiresAdmin) {
    // 检查是否为管理员（可根据实际需求调整）
    // 这里简单检查token是否存在，实际项目中应检查用户角色
    if (!token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }

  next()
})

export default router
