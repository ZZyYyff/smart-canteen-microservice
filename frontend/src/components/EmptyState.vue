<template>
  <div class="empty-state">
    <div class="empty-state__icon">
      <el-icon :size="48"><component :is="displayIcon" /></el-icon>
    </div>
    <p class="empty-state__text">{{ description }}</p>
    <el-button v-if="actionText" type="primary" @click="$emit('action')">
      {{ actionText }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { Document } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  description?: string
  actionText?: string
  icon?: any
}>(), {
  description: '暂无数据',
  actionText: '',
})

const displayIcon = props.icon ?? Document

defineEmits<{
  action: []
}>()
</script>
