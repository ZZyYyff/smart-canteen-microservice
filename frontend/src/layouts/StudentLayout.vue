<template>
  <div class="app-layout" style="flex-direction:column">
    <div class="app-topnav">
      <div class="app-topnav__brand">🍽️ 智能食堂</div>
      <div class="app-topnav__links">
        <router-link to="/student/home">首页</router-link>
        <router-link to="/student/menu">今日菜单</router-link>
        <router-link to="/student/cart">购物车</router-link>
        <router-link to="/student/orders">我的订单</router-link>
        <router-link to="/student/profile">个人中心</router-link>
      </div>
      <div class="app-topnav__right">
        <span class="text-secondary">{{ authStore.userInfo?.nickname || '同学' }}</span>
        <el-button text type="danger" @click="handleLogout">退出</el-button>
      </div>
    </div>
    <div class="app-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>
