import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { guest: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { guest: true },
  },
  {
    path: '/screen',
    component: () => import('@/layouts/ScreenLayout.vue'),
    meta: { public: true },
    children: [
      { path: 'pickup', name: 'ScreenPickup', component: () => import('@/views/screen/PickupScreen.vue') },
    ],
  },
  {
    path: '/student',
    component: () => import('@/layouts/StudentLayout.vue'),
    meta: { role: 'STUDENT' },
    children: [
      { path: '', redirect: '/student/home' },
      { path: 'home', name: 'StudentHome', component: () => import('@/views/student/Home.vue') },
      { path: 'menu', name: 'StudentMenu', component: () => import('@/views/student/Menu.vue') },
      { path: 'cart', name: 'StudentCart', component: () => import('@/views/student/Cart.vue') },
      { path: 'orders', name: 'StudentOrders', component: () => import('@/views/student/Orders.vue') },
      { path: 'orders/:id', name: 'StudentOrderDetail', component: () => import('@/views/student/OrderDetail.vue') },
      { path: 'profile', name: 'StudentProfile', component: () => import('@/views/student/Profile.vue') },
      { path: 'dishes/:id', name: 'StudentDishDetail', component: () => import('@/views/student/DishDetail.vue') },
    ],
  },
  {
    path: '/merchant',
    component: () => import('@/layouts/MerchantLayout.vue'),
    meta: { role: 'MERCHANT' },
    children: [
      { path: '', redirect: '/merchant/home' },
      { path: 'home', name: 'MerchantHome', component: () => import('@/views/merchant/Home.vue') },
      { path: 'menu', name: 'MerchantMenu', component: () => import('@/views/merchant/MenuManage.vue') },
      { path: 'orders', name: 'MerchantOrders', component: () => import('@/views/merchant/OrderManage.vue') },
      { path: 'pickup', name: 'MerchantPickup', component: () => import('@/views/merchant/PickupManage.vue') },
      { path: 'daily-menu', name: 'MerchantDailyMenu', component: () => import('@/views/merchant/DailyMenuManage.vue'), meta: { role: 'MERCHANT' } },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { role: 'ADMIN' },
    children: [
      { path: '', redirect: '/admin/home' },
      { path: 'home', name: 'AdminHome', component: () => import('@/views/admin/Home.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/UserManage.vue') },
      { path: 'menus', name: 'AdminMenus', component: () => import('@/views/admin/MenuManage.vue') },
      { path: 'system', name: 'AdminSystem', component: () => import('@/views/admin/SystemConfig.vue') },
      { path: 'windows', name: 'AdminWindows', component: () => import('@/views/admin/WindowManage.vue') },
      { path: 'daily-menus', name: 'AdminDailyMenus', component: () => import('@/views/merchant/DailyMenuManage.vue') },
    ],
  },
  {
    path: '/',
    redirect: '/login',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

const ROLE_HOME: Record<string, string> = {
  STUDENT: '/student/home',
  MERCHANT: '/merchant/home',
  ADMIN: '/admin/home',
}

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // 无需鉴权的公共页面
  if (to.meta.public) {
    next()
    return
  }

  // 已登录用户访问 guest 页面（login/register），跳转对应角色首页
  // 但从根路径 / 重定向过来的不踢走，允许用户停留在登录页
  if (to.meta.guest && authStore.isLoggedIn && from.path !== '/') {
    const target = ROLE_HOME[authStore.role] || '/student/home'
    next(target)
    return
  }

  // guest 页面放行
  if (to.meta.guest) {
    next()
    return
  }

  // 需要登录的页面：未登录跳转 /login
  if (!authStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 已登录但角色不匹配
  const routeRole = to.meta.role as string | undefined
  if (routeRole && authStore.role !== routeRole) {
    const target = ROLE_HOME[authStore.role] || '/login'
    ElMessage.warning('您没有权限访问该页面，已跳转到对应首页')
    next(target)
    return
  }

  next()
})

export default router
