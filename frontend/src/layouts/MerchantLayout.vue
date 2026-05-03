<template>
  <div class="app-layout app-layout--has-sidebar">
    <aside class="app-sidebar">
      <div class="sidebar-logo">🏪 商家中心</div>
      <SideMenu :menu-items="menuItems" />
    </aside>
    <div class="app-main">
      <AppHeader
        title="商家后台"
        :subtitle="authStore.userInfo?.nickname || '商家用户'"
        @logout="handleLogout"
      />
      <div class="app-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import SideMenu from '@/components/SideMenu.vue'
import AppHeader from '@/components/AppHeader.vue'
import { House, Food, List, TakeawayBox, Calendar } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const menuItems = [
  { path: '/merchant/home', title: '首页', icon: House },
  { path: '/merchant/menu', title: '菜品管理', icon: Food },
  { path: '/merchant/orders', title: '订单管理', icon: List },
  { path: '/merchant/pickup', title: '叫号核销', icon: TakeawayBox },
  { path: '/merchant/daily-menu', title: '每日菜单', icon: Calendar },
]

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>
