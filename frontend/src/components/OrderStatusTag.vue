<template>
  <el-tag :type="tagType" size="small" disable-transitions>
    {{ statusText }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const STATUS_MAP: Record<string, { type: 'info' | 'warning' | 'success' | 'danger' | ''; text: string }> = {
  CREATED: { type: 'info', text: '待接单' },
  ACCEPTED: { type: 'warning', text: '已接单' },
  COOKING: { type: 'warning', text: '制作中' },
  WAIT_PICKUP: { type: '', text: '待取餐' },
  COMPLETED: { type: 'success', text: '已完成' },
  CANCELLED: { type: 'danger', text: '已取消' },
}

const props = defineProps<{
  status?: string
}>()

const entry = computed(() => {
  if (!props.status) return null
  return STATUS_MAP[props.status] ?? null
})

const tagType = computed(() => entry.value?.type ?? 'info')
const statusText = computed(() => entry.value?.text ?? '未知状态')
</script>
