<template>
  <div class="student-home">
    <!-- Greeting -->
    <div class="home-hero mb-24">
      <div>
        <h1 class="home-hero__title">{{ authStore.userInfo?.nickname || '同学' }}，今天吃什么？</h1>
        <p class="home-hero__sub">看看今日菜单，挑一份喜欢的</p>
      </div>
      <div class="home-hero__actions">
        <el-button type="primary" size="large" round @click="$router.push('/student/menu')">
          去点餐
        </el-button>
      </div>
    </div>

    <!-- Stats -->
    <div class="home-stats mb-24">
      <div class="stat-item" @click="$router.push('/student/menu')">
        <div class="stat-item__icon" style="background:#FDF4EF;color:#C75B39">
          <el-icon :size="18"><Food /></el-icon>
        </div>
        <div class="stat-item__body">
          <div class="stat-item__value">{{ dishCount }}</div>
          <div class="stat-item__label">今日菜品</div>
        </div>
      </div>
      <div class="stat-item" @click="$router.push('/student/orders')">
        <div class="stat-item__icon" style="background:#F5F0EB;color:#6B5E53">
          <el-icon :size="18"><List /></el-icon>
        </div>
        <div class="stat-item__body">
          <div class="stat-item__value">{{ orderCount }}</div>
          <div class="stat-item__label">我的订单</div>
        </div>
      </div>
      <div v-if="waitingCount > 0" class="stat-item stat-item--alert" @click="$router.push('/student/orders')">
        <div class="stat-item__icon" style="background:#FEF5EC;color:#E87D2F">
          <el-icon :size="18"><TakeawayBox /></el-icon>
        </div>
        <div class="stat-item__body">
          <div class="stat-item__value text-warning">{{ waitingCount }}</div>
          <div class="stat-item__label">待取餐</div>
        </div>
      </div>
      <div class="stat-item" @click="$router.push('/student/cart')">
        <div class="stat-item__icon" style="background:#FCF4F4;color:#D44637">
          <el-icon :size="18"><ShoppingCart /></el-icon>
        </div>
        <div class="stat-item__body">
          <div class="stat-item__value">{{ cartStore.totalCount }}</div>
          <div class="stat-item__label">购物车</div>
        </div>
      </div>
    </div>

    <!-- Waiting alerts -->
    <div v-if="waitingOrders.length > 0" class="card waiting-section mb-24">
      <div class="flex-between mb-12">
        <h3 class="text-warning" style="font-size:15px;font-weight:600">待取餐提醒</h3>
        <el-button size="small" text type="primary" @click="$router.push('/student/orders')">查看全部</el-button>
      </div>
      <div v-for="order in waitingOrders" :key="order.id" class="waiting-item" @click="$router.push(`/student/orders/${order.id}`)">
        <div class="waiting-item__no">#{{ order.pickupNo }}</div>
        <div class="waiting-item__info">
          <div class="waiting-item__code">取餐码 {{ order.pickupCode }}</div>
          <OrderStatusTag :status="order.status" />
        </div>
        <el-icon class="waiting-item__arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- Quick links -->
    <div class="card">
      <h3 style="font-size:15px;font-weight:600;margin-bottom:12px">快捷入口</h3>
      <div class="quick-links">
        <div class="quick-link" @click="$router.push('/student/menu')">
          <div class="quick-link__icon" style="background:var(--primary-lighter);color:var(--primary)">
            <el-icon :size="20"><Food /></el-icon>
          </div>
          <span>浏览菜单</span>
        </div>
        <div class="quick-link" @click="$router.push('/student/orders')">
          <div class="quick-link__icon" style="background:#F5F0EB;color:#6B5E53">
            <el-icon :size="20"><List /></el-icon>
          </div>
          <span>查看订单</span>
        </div>
        <div class="quick-link" @click="$router.push('/student/profile')">
          <div class="quick-link__icon" style="background:#F5F0EB;color:#6B5E53">
            <el-icon :size="20"><User /></el-icon>
          </div>
          <span>个人中心</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Food, List, TakeawayBox, ShoppingCart, ArrowRight, User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { getTodayMenu } from '@/api/menu'
import { getMyOrders } from '@/api/order'
import type { OrderVO } from '@/types/api'
import OrderStatusTag from '@/components/OrderStatusTag.vue'

const authStore = useAuthStore()
const cartStore = useCartStore()

const dishCount = ref(0)
const orderCount = ref(0)
const waitingOrders = ref<OrderVO[]>([])
const waitingCount = computed(() => waitingOrders.value.length)

async function loadData() {
  try {
    const menus = await getTodayMenu()
    let total = 0
    for (const m of menus) total += m.dishes.filter((d) => d.status === 'ON_SALE').length
    dishCount.value = total
  } catch { dishCount.value = 0 }
  try {
    const orders = await getMyOrders()
    orderCount.value = orders.length
    waitingOrders.value = orders.filter((o) => o.status === 'WAIT_PICKUP')
  } catch { orderCount.value = 0 }
}

onMounted(loadData)
</script>

<style scoped>
/* Hero */
.home-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}
.home-hero__title {
  font-family: 'ZCOOL KuaiLe', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 26px;
  font-weight: 800;
  color: var(--text-heading);
  line-height: 1.3;
}
.home-hero__sub {
  margin-top: 4px;
  font-size: 14px;
  color: var(--text-muted);
}

/* Stats */
.home-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.15s;
}
.stat-item:hover {
  box-shadow: var(--shadow);
  transform: translateY(-1px);
}
.stat-item--alert {
  border-color: var(--warning);
  background: #FFFBF5;
}
.stat-item__icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-item__value {
  font-family: 'ZCOOL KuaiLe', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--text-heading);
}
.stat-item__label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 1px;
}

/* Waiting */
.waiting-section {
  border-color: var(--warning);
  border-width: 2px;
}

.waiting-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
  transition: background 0.1s;
}
.waiting-item:last-child { border-bottom: none; }
.waiting-item:hover { background: var(--primary-lighter); margin: 0 -20px; padding: 12px 20px; border-radius: 6px; }
.waiting-item__no {
  font-family: 'ZCOOL KuaiLe', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 28px;
  font-weight: 700;
  color: var(--warning);
  min-width: 60px;
}
.waiting-item__info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}
.waiting-item__code {
  font-size: 14px;
  color: var(--text);
  font-weight: 500;
}
.waiting-item__arrow {
  color: var(--text-muted);
  font-size: 16px;
}

/* Quick links */
.quick-links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.quick-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: var(--bg);
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: var(--text);
  transition: background 0.15s;
}
.quick-link:hover { background: var(--primary-lighter); }
.quick-link__icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 640px) {
  .home-stats { grid-template-columns: repeat(2, 1fr); }
  .quick-links { grid-template-columns: 1fr; }
}
</style>
