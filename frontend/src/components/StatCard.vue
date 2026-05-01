<template>
  <div class="stat-card">
    <div class="stat-card__icon" :style="{ background: bgColor, color: iconColor }">
      <el-icon :size="24"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-card__info">
      <div class="stat-card__value">{{ value }}</div>
      <div class="stat-card__title">{{ title }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  value: string | number
  icon: unknown
  bgColor?: string
  iconColor?: string
}>()
</script>
