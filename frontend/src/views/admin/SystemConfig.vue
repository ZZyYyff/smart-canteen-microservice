<template>
  <div class="admin-system-config">
    <h2 class="page-title mb-20">系统信息</h2>

    <el-row :gutter="16">
      <!-- 环境信息 -->
      <el-col :xs="24" :md="12" class="mb-16">
        <div class="card">
          <h3 class="mb-16">运行环境</h3>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="网关地址">
              <el-tag size="small">{{ apiBaseUrl || 'Vite 代理' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="WebSocket">
              <el-tag size="small">{{ wsBaseUrl }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="MySQL">
              <el-tag size="small" type="info">localhost:3307</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Redis">
              <el-tag size="small" type="info">localhost:6379</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Nacos">
              <el-tag size="small" type="info">localhost:8848</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前角色">
              <el-tag size="small" type="danger">ADMIN</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="登录账号">
              {{ authStore.userInfo?.nickname || '--' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>

      <!-- 微服务模块 -->
      <el-col :xs="24" :md="12" class="mb-16">
        <div class="card">
          <h3 class="mb-16">微服务模块</h3>
          <el-table :data="services" size="small" style="width:100%">
            <el-table-column prop="name" label="服务" width="140" />
            <el-table-column label="端口" width="60">
              <template #default="{ row }">{{ row.port }}</template>
            </el-table-column>
            <el-table-column prop="desc" label="说明" min-width="160" />
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL
const wsBaseUrl = import.meta.env.VITE_WS_BASE_URL

const services = [
  { name: 'gateway-service', port: 8080, desc: '网关入口，JWT 认证，路由转发' },
  { name: 'user-service', port: 9001, desc: '用户注册、登录、个人信息管理' },
  { name: 'menu-service', port: 9002, desc: '菜品管理、每日菜单、库存操作' },
  { name: 'order-service', port: 9003, desc: '下单、接单、订单状态流转' },
  { name: 'pickup-service', port: 9004, desc: '窗口管理、排队叫号、核销、大屏推送' },
]
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 700; }
</style>
