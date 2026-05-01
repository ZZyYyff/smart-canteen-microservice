<template>
  <div class="student-menu">
    <div class="flex-between mb-20">
      <div>
        <h2 class="page-title">今日菜单</h2>
        <p class="text-secondary" v-if="menuDate">{{ menuDate }}</p>
      </div>
      <el-badge :value="cartStore.totalCount" :hidden="!cartStore.totalCount">
        <el-button @click="$router.push('/student/cart')">
          <el-icon><ShoppingCart /></el-icon>
          购物车
        </el-button>
      </el-badge>
    </div>

    <el-empty v-if="!loading && allDishes.length === 0" description="今日暂无菜品" />

    <div v-for="menu in menus" :key="menu.id" class="menu-section mb-20">
      <h3 class="menu-section__title">
        {{ menu.mealPeriodDesc }}
        <span class="text-secondary" style="font-size:13px;font-weight:400">
          {{ menu.startTime }} - {{ menu.endTime }}
        </span>
      </h3>
      <el-row :gutter="16">
        <el-col v-for="dish in menu.dishes" :key="dish.id" :xs="12" :sm="8" :md="6" :lg="4" style="margin-bottom:16px">
          <DishCard
            :name="dish.name"
            :price="dish.price"
            :image-url="dish.imageUrl"
            :stock="dish.stock"
            :low-stock="dish.lowStock"
            :out-of-stock="dish.stock === 0"
            @click="handleAddToCart(dish)"
          />
          <el-button
            size="small"
            type="primary"
            plain
            style="width:100%;margin-top:4px"
            :disabled="dish.stock === 0 || dish.status !== 'ON_SALE'"
            @click="handleAddToCart(dish)"
          >
            {{ dish.stock === 0 ? '已售罄' : dish.status !== 'ON_SALE' ? '已下架' : '加入购物车' }}
          </el-button>
        </el-col>
      </el-row>
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

function handleAddToCart(dish: DishVO) {
  cartStore.addToCart({
    dishId: dish.id,
    dishName: dish.name,
    price: dish.price,
  })
  ElMessage.success(`已加入购物车：${dish.name}`)
}

onMounted(async () => {
  try {
    menus.value = await getTodayMenu()
    for (const m of menus.value) {
      allDishes.value.push(...m.dishes)
    }
    if (menus.value.length > 0) {
      menuDate.value = menus.value[0].menuDate
    }
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

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.menu-section__title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--primary);
}
</style>
