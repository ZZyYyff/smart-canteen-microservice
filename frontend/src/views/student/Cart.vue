<template>
  <div class="student-cart">
    <h2 class="page-title mb-20">购物车</h2>

    <el-empty v-if="cartStore.items.length === 0" description="购物车是空的">
      <el-button type="primary" @click="$router.push('/student/menu')">去点餐</el-button>
    </el-empty>

    <template v-else>
      <div class="card mb-16">
        <el-table :data="cartStore.items" style="width:100%">
          <el-table-column prop="dishName" label="菜品" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="数量" width="140">
            <template #default="{ row }">
              <el-input-number
                :model-value="row.quantity"
                :min="1"
                :max="99"
                size="small"
                @change="(v: number) => cartStore.updateQuantity(row.dishId, v!)"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="100">
            <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button size="small" type="danger" text @click="cartStore.removeFromCart(row.dishId)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="cart-footer">
          <span class="cart-total">合计：<strong>¥{{ cartStore.totalPrice.toFixed(2) }}</strong></span>
        </div>
      </div>

      <div class="card">
        <h3 class="mb-16">选择取餐窗口</h3>
        <el-select
          v-model="selectedWindowId"
          placeholder="请选择取餐窗口"
          style="width:100%"
          size="large"
        >
          <el-option
            v-for="w in activeWindows"
            :key="w.id"
            :label="w.name + (w.location ? ' - ' + w.location : '')"
            :value="w.id"
          />
        </el-select>

        <el-button
          type="primary"
          size="large"
          style="width:100%;margin-top:16px"
          :loading="submitting"
          :disabled="!selectedWindowId"
          @click="handleSubmit"
        >
          提交订单
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { createOrder } from '@/api/order'
import { listWindows } from '@/api/pickup'
import type { PickupWindowVO } from '@/types/api'

const router = useRouter()
const cartStore = useCartStore()

const windows = ref<PickupWindowVO[]>([])
const selectedWindowId = ref<number | null>(null)
const submitting = ref(false)

const activeWindows = computed(() => windows.value.filter((w) => w.status === 'ACTIVE'))

onMounted(async () => {
  try {
    windows.value = await listWindows()
    if (activeWindows.value.length === 1) {
      selectedWindowId.value = activeWindows.value[0].id
    }
  } catch {
    // ignore
  }
})

async function handleSubmit() {
  if (!selectedWindowId.value) {
    ElMessage.warning('请选择取餐窗口')
    return
  }
  submitting.value = true
  try {
    const order = await createOrder({
      windowId: selectedWindowId.value,
      items: cartStore.items.map((i) => ({
        dishId: i.dishId,
        dishName: i.dishName,
        price: i.price,
        quantity: i.quantity,
      })),
    })
    cartStore.clearCart()
    ElMessage.success('下单成功')
    router.push(`/student/orders/${order.id}`)
  } catch {
    // error shown by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 700;
}

.cart-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid var(--border);
  margin-top: 16px;
}

.cart-total {
  font-size: 18px;
}

.cart-total strong {
  color: #EF4444;
}
</style>
