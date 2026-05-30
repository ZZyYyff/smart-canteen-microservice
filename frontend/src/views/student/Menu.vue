<template>
  <div class="student-menu">
    <!-- Header -->
    <div class="menu-header mb-24">
      <div>
        <h2 class="menu-header__title">今日菜单</h2>
        <p class="text-muted text-sm" v-if="menuDate">{{ menuDate }}</p>
      </div>
      <el-badge :value="cartStore.totalCount" :hidden="!cartStore.totalCount" :max="99">
        <el-button size="large" round @click="$router.push('/student/cart')">
          <el-icon style="margin-right:4px"><ShoppingCart /></el-icon>
          购物车
        </el-button>
      </el-badge>
    </div>

    <el-empty v-if="!loading && allDishes.length === 0" description="今日暂无菜品">
      <el-button type="primary" @click="$router.push('/student/orders')">查看订单</el-button>
    </el-empty>

    <div v-for="menu in menus" :key="menu.id" class="menu-section mb-24">
      <div class="menu-section__header">
        <h3 class="menu-section__title">{{ menu.mealPeriodDesc }}</h3>
        <span class="menu-section__time">{{ menu.startTime?.slice(0, 5) }} - {{ menu.endTime?.slice(0, 5) }}</span>
      </div>

      <div class="dish-grid">
        <div v-for="dish in menu.dishes" :key="dish.id" class="dish-grid__item">
          <DishCard
            :name="dish.name"
            :price="dish.price"
            :image-url="dish.imageUrl"
            :stock="dish.stock"
            :low-stock="dish.lowStock"
            :out-of-stock="dish.stock === 0"
            @click="$router.push(`/student/dishes/${dish.id}`)"
          />
          <div class="dish-grid__actions">
            <el-button
              size="small"
              :type="canAdd(dish) ? 'primary' : 'info'"
              :disabled="!canAdd(dish)"
              @click.stop="handleAddToCart(dish)"
              class="dish-grid__cart-btn"
            >
              {{ cartLabel(dish) }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getTodayMenu } from '@/api/menu'
import { useCartStore } from '@/stores/cart'
import type { DailyMenuVO, DishVO } from '@/types/api'
import DishCard from '@/components/DishCard.vue'

const cartStore = useCartStore()
const menus = ref<DailyMenuVO[]>([])
const allDishes = ref<DishVO[]>([])
const menuDate = ref('')
const loading = ref(true)

function canAdd(d: DishVO) { return d.status === 'ON_SALE' && d.stock > 0 }
function cartLabel(d: DishVO) {
  if (d.stock === 0) return '售罄'
  if (d.status !== 'ON_SALE') return '已下架'
  return '+ 加入购物车'
}

function handleAddToCart(dish: DishVO) {
  cartStore.addToCart({ dishId: dish.id, dishName: dish.name, price: dish.price })
  ElMessage.success(`已加入购物车：${dish.name}`)
}

onMounted(async () => {
  try {
    menus.value = await getTodayMenu()
    for (const m of menus.value) allDishes.value.push(...m.dishes)
    if (menus.value.length > 0) menuDate.value = menus.value[0].menuDate
  } finally { loading.value = false }
})
</script>

<style scoped>
.menu-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}
.menu-header__title {
  font-family: 'ZCOOL KuaiLe', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 24px;
  font-weight: 800;
  color: var(--text-heading);
}

.menu-section__header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--primary);
}
.menu-section__title {
  font-family: 'ZCOOL KuaiLe', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 17px;
  font-weight: 700;
  color: var(--text-heading);
}
.menu-section__time {
  font-size: 13px;
  color: var(--text-muted);
}

.dish-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.dish-grid__item {
  display: flex;
  flex-direction: column;
}
.dish-grid__actions {
  margin-top: 6px;
}
.dish-grid__cart-btn {
  width: 100%;
  border-radius: var(--radius);
}

@media (max-width: 768px) { .dish-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .dish-grid { grid-template-columns: 1fr; } }
</style>
