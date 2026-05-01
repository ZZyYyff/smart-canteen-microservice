<template>
  <div class="student-profile">
    <h2 class="page-title mb-20">个人中心</h2>

    <div class="card mb-16">
      <h3 class="mb-16">基本信息</h3>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户 ID">{{ authStore.userInfo?.id }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ authStore.userInfo?.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ authStore.userInfo?.phone }}</el-descriptions-item>
        <el-descriptions-item label="学工号">{{ authStore.userInfo?.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag size="small">{{ roleText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ authStore.userInfo?.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="card">
      <h3 class="mb-16">修改信息</h3>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        style="max-width:420px"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入新昵称" clearable />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入新手机号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { updateCurrentUser } from '@/api/user'

const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const saving = ref(false)

const roleText = computed(() => {
  const map: Record<string, string> = { STUDENT: '学生', MERCHANT: '商家', ADMIN: '管理员' }
  return map[authStore.role] || authStore.role
})

const form = reactive({
  nickname: authStore.userInfo?.nickname || '',
  phone: authStore.userInfo?.phone || '',
})

const rules: FormRules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const params: Record<string, string> = {}
    if (form.nickname) params.nickname = form.nickname
    if (form.phone) params.phone = form.phone
    if (Object.keys(params).length === 0) {
      ElMessage.warning('没有需要修改的内容')
      return
    }
    await updateCurrentUser(params)
    await authStore.refreshUserInfo()
    ElMessage.success('修改成功')
  } catch {
    // error shown by interceptor
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 700;
}
</style>
