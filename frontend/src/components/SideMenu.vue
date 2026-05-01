<template>
  <el-menu
    :default-active="activeMenu"
    router
    :collapse="collapsed"
    class="side-menu"
    background-color="#FFFFFF"
    text-color="#6B7280"
    active-text-color="#0D9488"
  >
    <template v-for="item in menuItems" :key="item.path">
      <el-menu-item v-if="!item.children" :index="item.path">
        <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
        <span>{{ item.title }}</span>
      </el-menu-item>
      <el-sub-menu v-else :index="item.path">
        <template #title>
          <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </template>
        <el-menu-item
          v-for="child in item.children"
          :key="child.path"
          :index="child.path"
        >
          {{ child.title }}
        </el-menu-item>
      </el-sub-menu>
    </template>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

export interface MenuItem {
  path: string
  title: string
  icon?: unknown
  children?: MenuItem[]
}

const props = withDefaults(defineProps<{
  menuItems: MenuItem[]
  collapsed?: boolean
}>(), {
  collapsed: false,
})

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.side-menu {
  border-right: none;
  flex: 1;
  padding-top: 8px;
}
</style>
