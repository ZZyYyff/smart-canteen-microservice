<template>
  <div class="student-layout">
    <nav class="student-nav">
      <div class="student-nav__inner">
        <router-link to="/student/home" class="student-nav__brand">
          <svg width="22" height="22" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="12" :fill="`var(--primary)`" opacity="0.12"/>
            <path d="M16 22h16M18 30h12M20 18v16M28 18v16" stroke="var(--primary)" stroke-width="2" stroke-linecap="round"/>
          </svg>
          智能食堂
        </router-link>
        <div class="student-nav__links">
          <router-link to="/student/home">首页</router-link>
          <router-link to="/student/menu">菜单</router-link>
          <router-link to="/student/cart">
            购物车
            <span v-if="cartTotal > 0" class="nav-badge">{{ cartTotal }}</span>
          </router-link>
          <router-link to="/student/orders">订单</router-link>
        </div>
        <div class="student-nav__spacer" />
        <div class="student-nav__user">
          <span class="student-nav__name">{{ authStore.userInfo?.nickname || '同学' }}</span>
          <el-button text type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </nav>
    <main class="student-main">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()
const cartTotal = computed(() => cartStore.totalCount)

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.student-layout {
  min-height: 100vh;
  background: var(--bg);
}

/* Nav */
.student-nav {
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border);
}

.student-nav__inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 52px;
  padding: 0 24px;
  gap: 24px;
}

.student-nav__brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'ZCOOL KuaiLe', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 17px;
  font-weight: 700;
  color: var(--text-heading);
  flex-shrink: 0;
}

.student-nav__links {
  display: flex;
  gap: 2px;
}

.student-nav__links a {
  position: relative;
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-muted);
  transition: color 0.15s, background 0.15s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.student-nav__links a:hover {
  color: var(--text);
  background: var(--primary-lighter);
}

.student-nav__links a.router-link-active {
  color: var(--primary);
  font-weight: 600;
  background: var(--primary-lighter);
}

.nav-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--primary);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
}

.student-nav__spacer {
  flex: 1;
}

.student-nav__user {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.student-nav__name {
  font-size: 13px;
  color: var(--text-muted);
}

/* Main */
.student-main {
  padding: 28px 24px 60px;
}

/* Page transition */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.15s ease;
}
.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}
</style>
