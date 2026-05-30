<template>
  <div class="dish-card" @click="$emit('click')">
    <div class="dish-card__image">
      <img v-if="imageUrl" :src="imageUrl" :alt="name" />
      <div v-else class="dish-card__placeholder">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.35">
          <path d="M12 2a7 7 0 0 1 7 7c0 5.25-7 13-7 13S5 14.25 5 9a7 7 0 0 1 7-7z"/>
          <circle cx="12" cy="9" r="2.5"/>
        </svg>
      </div>
    </div>
    <div class="dish-card__body">
      <div class="dish-card__name">{{ name || '未命名菜品' }}</div>
      <div class="dish-card__footer">
        <span class="dish-card__price">{{ price != null ? price : '0.00' }}</span>
        <div class="dish-card__tags">
          <span v-if="outOfStock" class="tag tag--soldout">售罄</span>
          <span v-else-if="lowStock" class="tag tag--low">库存紧张</span>
          <span v-else class="dish-card__stock">{{ stock ?? '-' }} 份</span>
        </div>
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

defineEmits<{ click: [] }>()
</script>

<style scoped>
.dish-card {
  background: var(--surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--border);
  transition: box-shadow 0.2s, transform 0.2s;
}
.dish-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}

.dish-card__image {
  width: 100%;
  aspect-ratio: 4/3;
  overflow: hidden;
  background: var(--primary-lighter);
}
.dish-card__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dish-card__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
}

.dish-card__body {
  padding: 14px 16px;
}

.dish-card__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-heading);
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dish-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dish-card__price {
  font-family: 'ZCOOL KuaiLe', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 18px;
  font-weight: 700;
  color: var(--price);
}

.dish-card__price::before {
  content: '¥';
  font-size: 12px;
  font-weight: 500;
}

.dish-card__stock {
  font-size: 11px;
  color: var(--text-muted);
}

.dish-card__tags {
  display: flex;
  align-items: center;
}

.tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
}
.tag--soldout {
  background: #FEF2F2;
  color: var(--price);
}
.tag--low {
  background: #FEF5EC;
  color: var(--warning);
}
</style>
