<template>
  <div class="app-layout app-layout--has-sidebar">
    <aside class="app-sidebar">
      <div class="sidebar-logo">⚙️ 管理后台</div>
      <SideMenu :menu-items="menuItems" />
    </aside>
    <div class="app-main">
      <AppHeader
        title="系统管理"
        subtitle="管理员"
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
import { House, User, Food, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const menuItems = [
  { path: '/admin/home', title: '首页', icon: House },
  { path: '/admin/users', title: '用户管理', icon: User },
  { path: '/admin/menus', title: '菜单管理', icon: Food },
  { path: '/admin/system', title: '系统设置', icon: Setting },
]

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>
