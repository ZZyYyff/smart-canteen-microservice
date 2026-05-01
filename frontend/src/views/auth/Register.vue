<template>
  <div class="register-container">
    <div class="register-card card">
      <h1 class="register-title">创建账号</h1>
      <p class="register-subtitle">注册后即可使用智能食堂点餐服务</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="studentNo">
          <el-input
            v-model="form.studentNo"
            placeholder="请输入学工号"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="昵称（选填）"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="role">
          <el-radio-group v-model="form.role">
            <el-radio-button value="STUDENT">学生</el-radio-button>
            <el-radio-button value="MERCHANT">商家</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（6-20位）"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <p class="register-login">
        已有账号？
        <router-link to="/login">返回登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { register as registerApi } from '@/api/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  phone: '',
  studentNo: '',
  nickname: '',
  role: 'STUDENT',
  password: '',
  confirmPassword: '',
})

const validatePassword = (_rule: unknown, value: string, callback: (err?: Error) => void) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  studentNo: [
    { required: true, message: '请输入学工号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' },
  ],
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await registerApi({
      phone: form.phone,
      studentNo: form.studentNo,
      password: form.password,
      nickname: form.nickname || undefined,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // error already shown by request interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ECFDF5 0%, #F0FDFA 50%, #CCFBF1 100%);
  padding: 20px;
}

.register-card {
  width: 440px;
  max-width: 100%;
  padding: 40px;
}

.register-title {
  font-size: 20px;
  font-weight: 700;
  text-align: center;
  color: var(--text);
  margin-bottom: 6px;
}

.register-subtitle {
  text-align: center;
  color: var(--text-secondary);
  margin-bottom: 28px;
  font-size: 14px;
}

.register-btn {
  width: 100%;
}

.register-login {
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
}

.register-login a {
  color: var(--primary);
  font-weight: 500;
}
</style>
