<template>
  <div class="order-detail" v-loading="loading">
    <div class="flex-between mb-16">
      <h2 class="page-title">订单详情 #{{ order?.id }}</h2>
      <el-button v-if="order?.status === 'CREATED'" type="danger" :loading="cancelling" @click="handleCancel">
        取消订单
      </el-button>
    </div>

    <template v-if="order">
      <!-- 状态卡片 -->
      <div class="card mb-16">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单状态">
            <OrderStatusTag :status="order.status" />
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ order.createdAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">
            <strong style="color:#EF4444">¥{{ order.totalAmount ?? '0.00' }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="取餐号">
            <template v-if="order.pickupNo">
              <strong style="font-size:20px">{{ order.pickupNo }}</strong>
            </template>
            <span v-else class="text-secondary">-</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 订单明细 -->
      <div class="card mb-16">
        <h3 class="mb-16">菜品明细</h3>
        <el-table :data="order.items" style="width:100%">
          <el-table-column prop="dishName" label="菜品" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">¥{{ row.price ?? '0.00' }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">¥{{ ((row.price ?? 0) * (row.quantity ?? 0)).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 取餐提醒 -->
      <div v-if="order.status === 'WAIT_PICKUP'" class="pickup-alert card mb-16">
        <div class="pickup-alert__header">
          <el-icon :size="28"><Bell /></el-icon>
          <span>请前往窗口取餐</span>
        </div>
        <p class="pickup-alert__info">取餐号：<strong>{{ order.pickupNo }}</strong></p>
        <p class="pickup-alert__info">取餐码：<strong>{{ order.pickupCode }}</strong></p>
        <div class="pickup-alert__qrcode" v-if="order.pickupCode">
          <qrcode-vue :value="order.pickupCode" :size="160" level="M" />
        </div>
        <p class="pickup-alert__hint">请向取餐窗口工作人员出示取餐号或扫描取餐码</p>
      </div>

      <!-- 已完成也展示取餐码 -->
      <div v-else-if="order.pickupCode && order.status !== 'WAIT_PICKUP'" class="card mb-16">
        <h3 class="mb-16">取餐码</h3>
        <p class="text-secondary">取餐号：<strong>{{ order.pickupNo }}</strong></p>
        <p class="text-secondary">取餐码：<strong>{{ order.pickupCode }}</strong></p>
        <div style="margin-top:12px">
          <qrcode-vue :value="order.pickupCode" :size="120" level="M" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import QrcodeVue from 'qrcode.vue'
import { Bell } from '@element-plus/icons-vue'
import { getOrderById, cancelOrder } from '@/api/order'
import type { OrderVO } from '@/types/api'
import OrderStatusTag from '@/components/OrderStatusTag.vue'

const route = useRoute()
const router = useRouter()

const order = ref<OrderVO | null>(null)
const loading = ref(true)
const cancelling = ref(false)

onMounted(async () => {
  try {
    order.value = await getOrderById(Number(route.params.id))
  } finally {
    loading.value = false
  }
})

async function handleCancel() {
  if (!order.value) return
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    cancelling.value = true
    order.value = await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
  } catch {
    // cancelled or error
  } finally {
    cancelling.value = false
  }
}
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 700;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pickup-alert {
  border: 2px solid #F97316;
  background: #FFF7ED;
}

.pickup-alert__header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: #F97316;
  margin-bottom: 16px;
}

.pickup-alert__info {
  font-size: 16px;
  margin-bottom: 8px;
}

.pickup-alert__qrcode {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}

.pickup-alert__hint {
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
