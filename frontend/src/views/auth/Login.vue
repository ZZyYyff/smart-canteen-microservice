<template>
  <div class="login-container">
    <!-- Left: brand panel -->
    <div class="login-brand">
      <div class="login-brand__inner">
        <div class="login-brand__logo">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="14" fill="rgba(255,255,255,0.2)"/>
            <path d="M16 20h16M16 28h16M20 16v16M28 16v16" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
            <circle cx="24" cy="22" r="3" fill="#fff" opacity="0.6"/>
          </svg>
        </div>
        <h1 class="login-brand__title">智能食堂</h1>
        <p class="login-brand__subtitle">点餐与取餐微服务系统</p>
        <div class="login-brand__features">
          <span>在线点餐</span>
          <span class="dot">·</span>
          <span>扫码取餐</span>
          <span class="dot">·</span>
          <span>实时叫号</span>
        </div>
      </div>
      <div class="login-brand__footer">
        <p>Spring Cloud + Vue 3 全栈课设项目</p>
      </div>
    </div>

    <!-- Right: form panel -->
    <div class="login-form-panel">
      <div class="login-form-card">
        <h2 class="login-form__title">登录</h2>

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
              placeholder="手机号或学工号"
              size="large"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
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
          <router-link to="/register">注册新账号</router-link>
        </p>
      </div>
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
}

/* ===== Left Brand Panel ===== */
.login-brand {
  flex: 0 0 44%;
  background:
    radial-gradient(ellipse at 20% 80%, rgba(199, 91, 57, 0.6) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(242, 156, 86, 0.4) 0%, transparent 50%),
    linear-gradient(160deg, #3A2015 0%, #5C3523 40%, #7A4A30 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-brand::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 2px,
      rgba(255, 255, 255, 0.015) 2px,
      rgba(255, 255, 255, 0.015) 4px
    );
  pointer-events: none;
}

.login-brand__inner {
  text-align: center;
  color: #FBF7F2;
  position: relative;
  z-index: 1;
}

.login-brand__logo {
  margin: 0 auto 24px;
  display: flex;
  justify-content: center;
}

.login-brand__title {
  font-family: 'ZCOOL KuaiLe', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 38px;
  font-weight: 900;
  letter-spacing: 0.06em;
  margin-bottom: 8px;
  color: #FBF7F2;
}

.login-brand__subtitle {
  font-size: 15px;
  color: rgba(251, 247, 242, 0.65);
  margin-bottom: 32px;
  letter-spacing: 0.08em;
}

.login-brand__features {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 13px;
  color: rgba(251, 247, 242, 0.5);
}

.login-brand__features .dot {
  opacity: 0.4;
}

.login-brand__footer {
  position: absolute;
  bottom: 28px;
  font-size: 12px;
  color: rgba(251, 247, 242, 0.3);
  text-align: center;
}

/* ===== Right Form Panel ===== */
.login-form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  padding: 40px;
}

.login-form-card {
  width: 380px;
  max-width: 100%;
}

.login-form__title {
  font-family: 'ZCOOL KuaiLe', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-heading);
  margin-bottom: 32px;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  letter-spacing: 0.06em;
  margin-top: 4px;
}

.login-register {
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 20px;
}

.login-register a {
  color: var(--primary);
  font-weight: 600;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }
  .login-brand {
    flex: 0 0 auto;
    padding: 48px 20px;
  }
  .login-brand__title {
    font-size: 28px;
  }
  .login-brand__footer {
    display: none;
  }
  .login-form-panel {
    padding: 32px 20px;
  }
}
</style>
