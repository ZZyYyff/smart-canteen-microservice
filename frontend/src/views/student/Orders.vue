<template>
  <div class="student-orders">
    <h2 class="page-title mb-20">我的订单</h2>

    <el-empty v-if="!loading && orders.length === 0" description="暂无订单">
      <el-button type="primary" @click="$router.push('/student/menu')">去点餐</el-button>
    </el-empty>

    <div v-else v-loading="loading" class="card">
      <el-table :data="orders" style="width:100%" @row-click="(row: OrderVO) => $router.push(`/student/orders/${row.id}`)">
        <el-table-column prop="id" label="订单号" width="80">
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
          <template #default="{ row }">
            <template v-if="row.pickupNo">{{ row.pickupNo }}</template>
            <span v-else class="text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" min-width="160">
          <template #default="{ row }">{{ row.createdAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyOrders } from '@/api/order'
import type { OrderVO } from '@/types/api'
import OrderStatusTag from '@/components/OrderStatusTag.vue'

const orders = ref<OrderVO[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    orders.value = await getMyOrders()
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
