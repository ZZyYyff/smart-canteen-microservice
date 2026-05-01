<template>
  <div class="student-home">
    <h2 class="page-title">你好，{{ authStore.userInfo?.nickname || '同学' }}</h2>
    <p class="page-subtitle">欢迎使用智能食堂点餐系统</p>

    <el-row :gutter="16" class="mb-20">
      <el-col :xs="12" :sm="6">
        <StatCard title="今日菜品" :value="dishCount" :icon="Food" bg-color="#ECFDF5" icon-color="#0D9488" />
      </el-col>
      <el-col :xs="12" :sm="6">
        <StatCard title="我的订单" :value="orderCount" :icon="List" bg-color="#EFF6FF" icon-color="#3B82F6" />
      </el-col>
      <el-col :xs="12" :sm="6">
        <StatCard title="待取餐" :value="waitingCount" :icon="TakeawayBox" bg-color="#FFF7ED" icon-color="#F97316" />
      </el-col>
      <el-col :xs="12" :sm="6">
        <StatCard title="购物车" :value="cartStore.totalCount" :icon="ShoppingCart" bg-color="#FEF2F2" icon-color="#EF4444" />
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="24">
        <div class="card">
          <div class="flex-between mb-16">
            <h3>快捷操作</h3>
          </div>
          <el-space wrap>
            <el-button type="primary" @click="$router.push('/student/menu')">去点餐</el-button>
            <el-button @click="$router.push('/student/orders')">查看订单</el-button>
            <el-button @click="$router.push('/student/cart')">
              购物车
              <el-badge v-if="cartStore.totalCount" :value="cartStore.totalCount" class="cart-badge" />
            </el-button>
          </el-space>
        </div>
      </el-col>
    </el-row>

    <el-row v-if="waitingOrders.length" :gutter="16" style="margin-top:16px">
      <el-col :span="24">
        <div class="card">
          <h3 class="mb-16" style="color:#F97316">⏳ 待取餐提醒</h3>
          <div v-for="order in waitingOrders" :key="order.id" class="waiting-item">
            <span>订单 #{{ order.id }}</span>
            <OrderStatusTag :status="order.status" />
            <span class="text-secondary">取餐号：<strong>{{ order.pickupNo }}</strong></span>
            <span class="text-secondary">取餐码：<strong>{{ order.pickupCode }}</strong></span>
            <el-button size="small" text type="primary" @click="$router.push(`/student/orders/${order.id}`)">
              详情
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Food, List, TakeawayBox, ShoppingCart } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { getTodayMenu } from '@/api/menu'
import { getMyOrders } from '@/api/order'
import type { OrderVO } from '@/types/api'
import StatCard from '@/components/StatCard.vue'
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
    for (const m of menus) {
      total += m.dishes.filter((d) => d.status === 'ON_SALE').length
    }
    dishCount.value = total
  } catch {
    dishCount.value = 0
  }

  try {
    const orders = await getMyOrders()
    orderCount.value = orders.length
    waitingOrders.value = orders.filter((o) => o.status === 'WAIT_PICKUP')
  } catch {
    orderCount.value = 0
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 4px;
}

.page-subtitle {
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cart-badge {
  margin-left: 4px;
}

.waiting-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
}

.waiting-item:last-child {
  border-bottom: none;
}
</style>
