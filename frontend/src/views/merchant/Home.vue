<template>
  <div class="merchant-home">
    <!-- Stats -->
    <div class="stats-grid mb-24">
      <div class="stat-card">
        <div class="stat-card__icon" style="background:#FDF4EF;color:#C75B39">
          <el-icon :size="20"><List /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__value">{{ todayTotal }}</div>
          <div class="stat-card__label">今日订单</div>
        </div>
      </div>
      <div class="stat-card stat-card--warn">
        <div class="stat-card__icon" style="background:#FEF5EC;color:#E87D2F">
          <el-icon :size="20"><Clock /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__value text-warning">{{ createdCount }}</div>
          <div class="stat-card__label">待接单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon" style="background:#FCF4F4;color:#D44637">
          <el-icon :size="20"><Food /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__value">{{ cookingCount }}</div>
          <div class="stat-card__label">制作中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon" style="background:#FDF4EF;color:var(--primary)">
          <el-icon :size="20"><TakeawayBox /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__value">{{ waitPickupCount }}</div>
          <div class="stat-card__label">待取餐</div>
        </div>
      </div>
    </div>

    <!-- Recent orders -->
    <div class="card">
      <div class="flex-between mb-16">
        <h3 style="font-size:15px;font-weight:600">最近订单</h3>
        <el-button size="small" text type="primary" @click="$router.push('/merchant/orders')">查看全部</el-button>
      </div>
      <el-empty v-if="!loading && recentOrders.length === 0" description="暂无订单" />
      <el-table v-else :data="recentOrders" v-loading="loading" style="width:100%">
        <el-table-column label="订单号" width="80">
          <template #default="{ row }">#{{ row.id }}</template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }"><span class="text-price">¥{{ row.totalAmount ?? '0.00' }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><OrderStatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="取餐号" width="80">
          <template #default="{ row }">{{ row.pickupNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="下单时间" min-width="160">
          <template #default="{ row }">{{ row.createdAt || '-' }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { List, Clock, Food, TakeawayBox } from '@element-plus/icons-vue'
import { getMerchantPendingOrders } from '@/api/order'
import type { OrderVO } from '@/types/api'
import OrderStatusTag from '@/components/OrderStatusTag.vue'

const orders = ref<OrderVO[]>([])
const loading = ref(true)

const todayTotal = computed(() => orders.value.length)
const createdCount = computed(() => orders.value.filter((o) => o.status === 'CREATED').length)
const cookingCount = computed(() => orders.value.filter((o) => o.status === 'COOKING').length)
const waitPickupCount = computed(() => orders.value.filter((o) => o.status === 'WAIT_PICKUP').length)
const recentOrders = computed(() => orders.value.slice(0, 10))

onMounted(async () => {
  try { orders.value = await getMerchantPendingOrders() } finally { loading.value = false }
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
}
.stat-card--warn {
  border-color: var(--warning);
  background: #FFFBF5;
}

.stat-card__icon {
  width: 44px; height: 44px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.stat-card__value {
  font-family: 'ZCOOL KuaiLe', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 24px; font-weight: 700;
  color: var(--text-heading); line-height: 1.2;
}

.stat-card__label {
  font-size: 12px; color: var(--text-muted); margin-top: 1px;
}

@media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
