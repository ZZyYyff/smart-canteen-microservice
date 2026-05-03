<template>
  <div class="dish-detail" v-loading="loading">
    <el-button text @click="$router.back()" class="mb-16">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>

    <template v-if="dish">
      <el-row :gutter="24">
        <!-- 图片 -->
        <el-col :xs="24" :md="10">
          <div class="dish-detail__image">
            <img v-if="dish.imageUrl" :src="dish.imageUrl" :alt="dish.name" />
            <div v-else class="dish-detail__placeholder">🍽️</div>
          </div>
        </el-col>

        <!-- 信息 -->
        <el-col :xs="24" :md="14">
          <div class="card" style="height:100%">
            <h1 class="dish-detail__name">{{ dish.name }}</h1>

            <div class="dish-detail__price mb-16">¥{{ dish.price ?? '0.00' }}</div>

            <div class="mb-16">
              <el-tag
                :type="dish.status === 'ON_SALE' ? 'success' : 'info'"
                size="default"
                class="mb-8"
              >
                {{ dish.statusDesc }}
              </el-tag>
              <el-tag v-if="dish.lowStock && dish.stock > 0" type="warning" size="default" class="mb-8" style="margin-left:8px">
                库存紧张
              </el-tag>
              <el-tag v-if="dish.stock === 0" type="danger" size="default" class="mb-8" style="margin-left:8px">
                已售罄
              </el-tag>
            </div>

            <el-descriptions :column="1" border class="mb-16">
              <el-descriptions-item label="库存">{{ dish.stock }}</el-descriptions-item>
              <el-descriptions-item label="预警阈值">{{ dish.warningStock || 0 }}</el-descriptions-item>
              <el-descriptions-item label="上架时间">{{ dish.createdAt || '-' }}</el-descriptions-item>
            </el-descriptions>

            <p v-if="dish.description" class="dish-detail__desc mb-20">{{ dish.description }}</p>
            <p v-else class="text-secondary mb-20">暂无描述</p>

            <el-button
              type="primary"
              size="large"
              :disabled="!canAddToCart"
              @click="handleAddToCart"
              style="width:100%"
            >
              {{ addToCartText }}
            </el-button>
          </div>
        </el-col>
      </el-row>
    </template>

    <el-empty v-else-if="!loading" description="菜品不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getDishById } from '@/api/menu'
import { useCartStore } from '@/stores/cart'
import type { DishVO } from '@/types/api'

const route = useRoute()
const cartStore = useCartStore()
const dish = ref<DishVO | null>(null)
const loading = ref(true)

const canAddToCart = computed(() => {
  return dish.value && dish.value.status === 'ON_SALE' && dish.value.stock > 0
})

const addToCartText = computed(() => {
  if (!dish.value) return '加入购物车'
  if (dish.value.stock === 0) return '已售罄'
  if (dish.value.status !== 'ON_SALE') return '已下架'
  return '加入购物车'
})

function handleAddToCart() {
  if (!dish.value || !canAddToCart.value) return
  cartStore.addToCart({
    dishId: dish.value.id,
    dishName: dish.value.name,
    price: dish.value.price,
  })
  ElMessage.success(`已加入购物车：${dish.value.name}`)
}

onMounted(async () => {
  try {
    const id = Number(route.params.id)
    dish.value = await getDishById(id)
  } catch {
    dish.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dish-detail__image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #ECFDF5 0%, #A7F3D0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.dish-detail__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dish-detail__placeholder {
  font-size: 80px;
}

.dish-detail__name {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
}

.dish-detail__price {
  font-size: 28px;
  font-weight: 700;
  color: #EF4444;
}

.dish-detail__desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
}

.mb-8 {
  margin-bottom: 8px;
}

.mb-16 {
  margin-bottom: 16px;
}

.mb-20 {
  margin-bottom: 20px;
}
</style>
