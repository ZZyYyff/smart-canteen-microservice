<template>
  <div class="admin-user-manage">
    <h2 class="page-title mb-20">用户管理</h2>

    <!-- 提示：后端暂未提供用户列表接口 -->
    <el-alert
      title="后端暂未提供用户列表接口"
      type="warning"
      description="当前后端 user-service 仅提供注册、登录、个人信息查询接口，无用户列表/筛选/管理接口。以下为静态占位展示。"
      show-icon
      :closable="false"
      class="mb-16"
    />

    <div class="card mb-16">
      <h3 class="mb-16">当前管理员信息</h3>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户 ID">{{ authStore.userInfo?.id }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ authStore.userInfo?.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ authStore.userInfo?.phone }}</el-descriptions-item>
        <el-descriptions-item label="学工号">{{ authStore.userInfo?.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag type="danger" size="small">管理员</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag type="success" size="small">{{ authStore.userInfo?.status === 'NORMAL' ? '正常' : '已禁用' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ authStore.userInfo?.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="card">
      <h3 class="mb-16">用户列表（占位）</h3>

      <el-row :gutter="16" class="mb-16">
        <el-col :span="6">
          <el-select v-model="filterRole" placeholder="角色筛选" clearable style="width:100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="商家" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width:100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="已禁用" value="DISABLED" />
          </el-select>
        </el-col>
      </el-row>

      <el-empty description="后端暂未提供用户列表接口，此处为占位">
        <template #image>
          <el-icon :size="64" color="#D1D5DB"><User /></el-icon>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { User } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 筛选条件（前端占位，后端无对应接口）
const filterRole = ref('')
const filterStatus = ref('')
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 700;
}
</style>
