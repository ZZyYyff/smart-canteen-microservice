<template>
  <div class="admin-system-config">
    <h2 class="page-title mb-20">系统配置</h2>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12" class="mb-16">
        <div class="card">
          <h3 class="mb-16">环境信息</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="网关地址">
              <el-tag>{{ apiBaseUrl }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="WebSocket 地址">
              <el-tag>{{ wsBaseUrl }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前角色">
              <el-tag type="danger">ADMIN · 管理员</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="登录账号">
              {{ authStore.userInfo?.nickname || '管理员' }}
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              {{ authStore.userInfo?.phone || '--' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>

      <el-col :xs="24" :md="12" class="mb-16">
        <div class="card">
          <h3 class="mb-16">服务模块</h3>
          <div v-for="svc in services" :key="svc.name" class="service-item">
            <div class="service-item__header">
              <span class="service-item__name">{{ svc.name }}</span>
              <el-tag size="small" type="info">:{{ svc.port }}</el-tag>
            </div>
            <p class="service-item__desc">{{ svc.desc }}</p>
            <p class="service-item__path">路由：<code>{{ svc.path }}</code></p>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="card mb-16">
      <h3 class="mb-16">系统功能清单</h3>
      <el-row :gutter="16">
        <el-col v-for="f in features" :key="f.title" :xs="24" :sm="12" :md="8" class="mb-16">
          <div class="feature-card">
            <div class="feature-card__icon">
              <el-icon :size="20"><component :is="f.icon" /></el-icon>
            </div>
            <div class="feature-card__title">{{ f.title }}</div>
            <div class="feature-card__desc">{{ f.desc }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="card">
      <h3 class="mb-16">关于本系统</h3>
      <p class="text-secondary" style="line-height:1.8">
        智能食堂点餐与取餐微服务系统，基于 Spring Cloud 微服务架构。
        包含用户服务、菜品菜单服务、订单服务、取餐排队服务五大模块，
        通过 Spring Cloud Gateway 统一入口，Nacos 服务注册发现，
        OpenFeign 服务间调用，WebSocket 大屏实时推送。
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { User, Food, List, TakeawayBox, Setting } from '@element-plus/icons-vue'

const authStore = useAuthStore()

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL
const wsBaseUrl = import.meta.env.VITE_WS_BASE_URL

const services = [
  { name: 'gateway-service', port: 8080, path: '/api/**', desc: '统一网关入口，JWT 认证，路由转发，限流' },
  { name: 'user-service', port: 9001, path: '/api/users/**', desc: '用户注册、登录、Token 刷新、个人信息管理' },
  { name: 'menu-service', port: 9002, path: '/api/menus/**', desc: '菜品 CRUD、每日菜单管理、库存操作' },
  { name: 'order-service', port: 9003, path: '/api/orders/**', desc: '下单、接单、订单状态流转' },
  { name: 'pickup-service', port: 9004, path: '/api/pickup/**', desc: '取餐窗口管理、排队叫号、核销、大屏推送' },
]

const features = [
  { icon: User, title: '用户管理', desc: '学生/商家/管理员三种角色，JWT 双 Token 认证' },
  { icon: Food, title: '菜品管理', desc: '菜品增删改查、上下架、库存预警' },
  { icon: List, title: '订单管理', desc: '下单扣库存、订单状态流转 CREATED→COMPLETED' },
  { icon: TakeawayBox, title: '取餐排队', desc: '窗口叫号、取餐码核销、排队队列管理' },
  { icon: Setting, title: '大屏推送', desc: 'WebSocket 实时推送叫号消息到食堂大屏' },
  { icon: Setting, title: '微服务架构', desc: 'Spring Cloud Gateway + Nacos + OpenFeign' },
]
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 700;
}

.service-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}

.service-item:last-child {
  border-bottom: none;
}

.service-item__header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.service-item__name {
  font-weight: 600;
  font-size: 14px;
}

.service-item__desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 2px;
}

.service-item__path {
  font-size: 12px;
  color: var(--text-secondary);
}

.service-item__path code {
  background: #F3F4F6;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 12px;
}

.feature-card {
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  height: 100%;
}

.feature-card__icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #ECFDF5;
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.feature-card__title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
}

.feature-card__desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
}
</style>
