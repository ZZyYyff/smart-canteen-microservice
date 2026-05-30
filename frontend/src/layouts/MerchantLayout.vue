<template>
  <div class="merchant-layout">
    <!-- Sidebar -->
    <aside class="merchant-sidebar">
      <div class="merchant-sidebar__brand">
        <svg width="24" height="24" viewBox="0 0 48 48" fill="none">
          <rect width="48" height="48" rx="12" fill="rgba(255,255,255,0.15)"/>
          <path d="M14 18h20M14 24h20M14 30h12" stroke="#FBF7F2" stroke-width="2.5" stroke-linecap="round"/>
        </svg>
        <span>商家中心</span>
      </div>
      <nav class="merchant-sidebar__nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="merchant-sidebar__link"
        >
          <el-icon :size="16"><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </router-link>
      </nav>
      <div class="merchant-sidebar__footer">
        <span class="merchant-sidebar__user">{{ authStore.userInfo?.nickname || '商家用户' }}</span>
        <el-button text size="small" style="color:rgba(251,247,242,0.5)" @click="handleLogout">退出</el-button>
      </div>
    </aside>

    <!-- Main -->
    <main class="merchant-main">
      <header class="merchant-header">
        <h1 class="merchant-header__title">{{ pageTitle }}</h1>
      </header>
      <div class="merchant-content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { House, Food, List, TakeawayBox, Calendar } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const menuItems = [
  { path: '/merchant/home', title: '首页', icon: House },
  { path: '/merchant/menu', title: '菜品管理', icon: Food },
  { path: '/merchant/orders', title: '订单处理', icon: List },
  { path: '/merchant/pickup', title: '叫号核销', icon: TakeawayBox },
  { path: '/merchant/daily-menu', title: '每日菜单', icon: Calendar },
]

const pageTitle = computed(() => {
  const item = menuItems.find((m) => m.path === route.path)
  return item?.title || '商家后台'
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.merchant-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg);
}

/* Sidebar */
.merchant-sidebar {
  position: fixed; left: 0; top: 0; bottom: 0;
  width: var(--sidebar-width);
  background: var(--sidebar-bg);
  display: flex; flex-direction: column;
  z-index: 100;
}

.merchant-sidebar__brand {
  display: flex; align-items: center; gap: 10px;
  padding: 20px 18px;
  font-family: 'ZCOOL KuaiLe', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 16px; font-weight: 700;
  color: var(--sidebar-text-active);
  border-bottom: 1px solid rgba(255,255,255,0.08);
}

.merchant-sidebar__nav {
  flex: 1;
  padding: 10px 10px;
  display: flex; flex-direction: column;
  gap: 2px;
}

.merchant-sidebar__link {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
  color: var(--sidebar-text);
  transition: all 0.15s;
  text-decoration: none;
}

.merchant-sidebar__link:hover {
  background: rgba(255,255,255,0.06);
  color: var(--sidebar-text-active);
}

.merchant-sidebar__link.router-link-active {
  background: var(--sidebar-active-bg);
  color: var(--sidebar-text-active);
  font-weight: 600;
}

.merchant-sidebar__footer {
  padding: 14px 18px;
  border-top: 1px solid rgba(255,255,255,0.08);
  display: flex; align-items: center;
  justify-content: space-between;
}

.merchant-sidebar__user {
  font-size: 12px;
  color: rgba(251,247,242,0.5);
}

/* Main */
.merchant-main {
  margin-left: var(--sidebar-width);
  flex: 1;
  display: flex; flex-direction: column;
  min-height: 100vh;
}

.merchant-header {
  position: sticky; top: 0; z-index: 40;
  background: rgba(251, 247, 242, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border);
  padding: 0 28px;
  height: 52px;
  display: flex; align-items: center;
}

.merchant-header__title {
  font-family: 'ZCOOL KuaiLe', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 17px;
  font-weight: 700;
  color: var(--text-heading);
}

.merchant-content {
  flex: 1;
  padding: 24px 32px 60px;
}

/* Page transition */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.12s ease;
}
.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}
</style>
