<template>
  <div class="admin-home">
    <h2 class="page-title mb-20">管理后台</h2>

    <el-row :gutter="16" class="mb-20">
      <el-col :xs="12" :sm="6">
        <StatCard
          title="用户总数"
          :value="stats.userCount"
          :icon="User"
          bg-color="#EFF6FF"
          icon-color="#3B82F6"
        />
      </el-col>
      <el-col :xs="12" :sm="6">
        <StatCard
          title="菜品数量"
          :value="stats.dishCount"
          :icon="Food"
          bg-color="#ECFDF5"
          icon-color="#0D9488"
        />
      </el-col>
      <el-col :xs="12" :sm="6">
        <StatCard
          title="取餐窗口"
          :value="stats.windowCount"
          :icon="TakeawayBox"
          bg-color="#FFF7ED"
          icon-color="#F97316"
        />
      </el-col>
      <el-col :xs="12" :sm="6">
        <StatCard
          title="系统服务"
          :value="stats.serviceCount"
          :icon="Setting"
          bg-color="#FEF2F2"
          icon-color="#EF4444"
        />
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12" class="mb-16">
        <div class="card">
          <h3 class="mb-16">系统模块</h3>
          <el-table :data="modules" style="width:100%">
            <el-table-column prop="name" label="模块" width="140" />
            <el-table-column prop="desc" label="说明" />
            <el-table-column label="端口" width="70">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{ row.port }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :xs="24" :md="12" class="mb-16">
        <div class="card">
          <h3 class="mb-16">快速入口</h3>
          <el-space wrap>
            <el-button @click="$router.push('/admin/users')">
              <el-icon><User /></el-icon> 用户管理
            </el-button>
            <el-button @click="$router.push('/admin/menus')">
              <el-icon><Food /></el-icon> 菜单管理
            </el-button>
            <el-button @click="$router.push('/admin/system')">
              <el-icon><Setting /></el-icon> 系统配置
            </el-button>
            <el-button @click="$router.push('/screen/pickup')" target="_blank">
              <el-icon><Monitor /></el-icon> 取餐大屏
            </el-button>
          </el-space>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { User, Food, TakeawayBox, Setting, Monitor } from '@element-plus/icons-vue'
import { listDishes } from '@/api/menu'
import { listWindows } from '@/api/pickup'
import StatCard from '@/components/StatCard.vue'

const stats = reactive({
  userCount: '--',
  dishCount: 0,
  windowCount: 0,
  serviceCount: 5,
})

const modules = [
  { name: 'gateway-service', desc: '网关 — 统一入口、JWT 校验、路由转发', port: 8080 },
  { name: 'user-service', desc: '用户服务 — 注册、登录、用户信息管理', port: 9001 },
  { name: 'menu-service', desc: '菜品菜单服务 — 菜品 CRUD、每日菜单、库存管理', port: 9002 },
  { name: 'order-service', desc: '订单服务 — 下单、接单、状态流转', port: 9003 },
  { name: 'pickup-service', desc: '取餐服务 — 排队叫号、核销、大屏 WebSocket', port: 9004 },
]

onMounted(async () => {
  try {
    const dishes = await listDishes()
    stats.dishCount = dishes.length
  } catch { /* */ }

  try {
    const windows = await listWindows()
    stats.windowCount = windows.length
  } catch { /* */ }

  // 后端暂未提供用户列表接口，用户数量显示为占位符
})
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 700;
}
</style>
