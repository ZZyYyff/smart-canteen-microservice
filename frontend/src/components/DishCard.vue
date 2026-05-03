<template>
  <div class="dish-card" @click="$emit('click')">
    <div class="dish-card__image">
      <img v-if="imageUrl" :src="imageUrl" :alt="name" />
      <div v-else class="dish-card__placeholder">
        <span>🍽️</span>
      </div>
    </div>
    <div class="dish-card__body">
      <div class="dish-card__name">{{ name || '未命名菜品' }}</div>
      <div class="dish-card__footer">
        <span class="dish-card__price">{{ price != null ? price : '0.00' }}</span>
        <el-tag v-if="outOfStock" type="danger" size="small">已售罄</el-tag>
        <el-tag v-else-if="lowStock" type="warning" size="small">库存紧张</el-tag>
        <span v-else class="text-secondary" style="font-size:12px">库存 {{ stock ?? '-' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  name: string
  price: number
  imageUrl?: string
  stock?: number
  lowStock?: boolean
  outOfStock?: boolean
}>()

defineEmits<{
  click: []
}>()
</script>

<style scoped>
.dish-card__placeholder {
  width: 100%;
  height: 100%;
  background: #ECFDF5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
}
</style>
