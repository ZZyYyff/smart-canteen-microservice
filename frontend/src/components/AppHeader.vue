<template>
  <div class="app-header">
    <div class="app-header__left">
      <slot name="left">
        <span>{{ title }}</span>
      </slot>
    </div>
    <div class="app-header__right">
      <slot name="right">
        <span class="text-secondary">{{ subtitle }}</span>
        <el-button text @click="$emit('logout')">退出登录</el-button>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title?: string
  subtitle?: string
}>(), {
  title: '',
  subtitle: '',
})

defineEmits<{
  logout: []
}>()
</script>
