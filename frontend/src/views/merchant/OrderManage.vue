<template>
  <div class="merchant-order-manage">
    <h2 class="page-title mb-20">订单处理</h2>

    <div class="card">
      <el-tabs v-model="activeStatus" @tab-change="loadOrders">
        <el-tab-pane label="待接单" name="CREATED" />
        <el-tab-pane label="已接单" name="ACCEPTED" />
        <el-tab-pane label="制作中" name="COOKING" />
        <el-tab-pane label="待取餐" name="WAIT_PICKUP" />
        <el-tab-pane label="已完成" name="COMPLETED" />
      </el-tabs>

      <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无订单" />
      <el-table v-else :data="filteredOrders" v-loading="loading" style="width:100%">
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
        <el-table-column label="菜品" min-width="200">
          <template #default="{ row }">
            <span v-for="(item, idx) in row.items" :key="idx">
              {{ item.dishName }}×{{ item.quantity }}<template v-if="idx < row.items.length - 1">、</template>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="取餐号" width="80">
          <template #default="{ row }">{{ row.pickupNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="下单时间" width="170">
          <template #default="{ row }">{{ row.createdAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'CREATED'"
              type="primary"
              size="small"
              @click="handleAccept(row.id)"
            >
              接单
            </el-button>
            <el-button
              v-if="row.status === 'ACCEPTED'"
              type="warning"
              size="small"
              @click="handleCooking(row.id)"
            >
              开始制作
            </el-button>
            <el-button
              v-if="row.status === 'COOKING'"
              type="success"
              size="small"
              @click="handleReady(row.id)"
            >
              制作完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMerchantPendingOrders, getMyOrders, acceptOrder, cookingOrder, readyOrder } from '@/api/order'
import type { OrderVO } from '@/types/api'
import OrderStatusTag from '@/components/OrderStatusTag.vue'

const allOrders = ref<OrderVO[]>([])
const loading = ref(true)
const activeStatus = ref('CREATED')

const filteredOrders = computed(() =>
  allOrders.value.filter((o) => o.status === activeStatus.value),
)

async function loadOrders() {
  loading.value = true
  try {
    // 使用 getMerchantPendingOrders 加载全部在途订单
    const pending = await getMerchantPendingOrders()
    // 同时加载自己的订单（商家作为用户也会有订单记录）
    try {
      const myOrders = await getMyOrders()
      // 合并去重
      const ids = new Set(pending.map((o) => o.id))
      for (const o of myOrders) {
        if (!ids.has(o.id)) {
          pending.push(o)
        }
      }
    } catch { /* ignore */ }
    allOrders.value = pending
  } finally {
    loading.value = false
  }
}

async function handleAccept(id: number) {
  try {
    await acceptOrder(id)
    ElMessage.success('已接单')
    await loadOrders()
  } catch { /* */ }
}

async function handleCooking(id: number) {
  try {
    await cookingOrder(id)
    ElMessage.success('开始制作')
    await loadOrders()
  } catch { /* */ }
}

async function handleReady(id: number) {
  try {
    await readyOrder(id)
    ElMessage.success('备餐完成，等待取餐')
    await loadOrders()
  } catch { /* */ }
}

onMounted(loadOrders)
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 700;
}
</style>
