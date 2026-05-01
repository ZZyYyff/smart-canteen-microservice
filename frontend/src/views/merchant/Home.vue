<template>
  <div class="merchant-home">
    <h2 class="page-title mb-20">商家工作台</h2>

    <el-row :gutter="16" class="mb-20">
      <el-col :xs="12" :sm="6">
        <StatCard title="今日订单" :value="todayTotal" :icon="List" bg-color="#EFF6FF" icon-color="#3B82F6" />
      </el-col>
      <el-col :xs="12" :sm="6">
        <StatCard title="待接单" :value="createdCount" :icon="Clock" bg-color="#FFF7ED" icon-color="#F97316" />
      </el-col>
      <el-col :xs="12" :sm="6">
        <StatCard title="制作中" :value="cookingCount" :icon="Food" bg-color="#FEF2F2" icon-color="#EF4444" />
      </el-col>
      <el-col :xs="12" :sm="6">
        <StatCard title="待取餐" :value="waitPickupCount" :icon="TakeawayBox" bg-color="#ECFDF5" icon-color="#0D9488" />
      </el-col>
    </el-row>

    <div class="card">
      <h3 class="mb-16">最近订单</h3>
      <el-empty v-if="!loading && recentOrders.length === 0" description="暂无订单" />
      <el-table v-else :data="recentOrders" v-loading="loading" style="width:100%">
        <el-table-column label="订单号" width="80">
          <template #default="{ row }">#{{ row.id }}</template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥{{ row.totalAmount ?? '0.00' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <OrderStatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="取餐号" width="80">
          <template #default="{ row }">{{ row.pickupNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="时间" min-width="160">
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
import StatCard from '@/components/StatCard.vue'
import OrderStatusTag from '@/components/OrderStatusTag.vue'

const orders = ref<OrderVO[]>([])
const loading = ref(true)

const todayTotal = computed(() => orders.value.length)
const createdCount = computed(() => orders.value.filter((o) => o.status === 'CREATED').length)
const cookingCount = computed(() => orders.value.filter((o) => o.status === 'COOKING').length)
const waitPickupCount = computed(() => orders.value.filter((o) => o.status === 'WAIT_PICKUP').length)
const recentOrders = computed(() => orders.value.slice(0, 10))

onMounted(async () => {
  try {
    orders.value = await getMerchantPendingOrders()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 700;
}
</style>
