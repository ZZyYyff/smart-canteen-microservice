<template>
  <div class="login-container">
    <div class="login-card card">
      <h1 class="login-title">智能食堂点餐与取餐系统</h1>
      <p class="login-subtitle">欢迎回来，请登录您的账号</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="account">
          <el-input
            v-model="form.account"
            placeholder="请输入手机号或学工号"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <p class="login-register">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  account: '',
  password: '',
})

const rules: FormRules = {
  account: [{ required: true, message: '请输入手机号或学工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const ROLE_HOME: Record<string, string> = {
  STUDENT: '/student/home',
  MERCHANT: '/merchant/home',
  ADMIN: '/admin/home',
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login(form.account, form.password)
    ElMessage.success('登录成功')
    const target = ROLE_HOME[authStore.role] || '/student/home'
    router.push(target)
  } catch {
    // error already shown by request interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ECFDF5 0%, #F0FDFA 50%, #CCFBF1 100%);
  padding: 20px;
}

.login-card {
  width: 400px;
  max-width: 100%;
  padding: 40px;
}

.login-title {
  font-size: 20px;
  font-weight: 700;
  text-align: center;
  color: var(--text);
  margin-bottom: 6px;
}

.login-subtitle {
  text-align: center;
  color: var(--text-secondary);
  margin-bottom: 28px;
  font-size: 14px;
}

.login-btn {
  width: 100%;
}

.login-register {
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
}

.login-register a {
  color: var(--primary);
  font-weight: 500;
}
</style>
