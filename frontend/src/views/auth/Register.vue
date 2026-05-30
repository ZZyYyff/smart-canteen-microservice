<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="register-title">注册账号</h2>
      <p class="register-subtitle">填写信息完成注册</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" size="large" clearable />
        </el-form-item>

        <el-form-item prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="学工号" size="large" clearable />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称（选填）" size="large" clearable />
        </el-form-item>

        <el-form-item prop="role">
          <el-radio-group v-model="form.role">
            <el-radio-button value="STUDENT">学生</el-radio-button>
            <el-radio-button value="MERCHANT">商家</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（6-20位）" size="large" show-password />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" size="large" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="handleRegister">
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
  background:
    radial-gradient(ellipse at 30% 10%, rgba(199, 91, 57, 0.06) 0%, transparent 60%),
    radial-gradient(ellipse at 70% 90%, rgba(242, 156, 86, 0.05) 0%, transparent 60%),
    var(--bg);
  padding: 20px;
}

.register-card {
  width: 420px;
  max-width: 100%;
  padding: 40px 36px;
  background: var(--surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border);
}

.register-title {
  font-family: 'ZCOOL KuaiLe', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 26px;
  font-weight: 700;
  text-align: center;
  color: var(--text-heading);
  margin-bottom: 4px;
}

.register-subtitle {
  text-align: center;
  color: var(--text-muted);
  margin-bottom: 28px;
  font-size: 14px;
}

.register-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  letter-spacing: 0.06em;
  margin-top: 4px;
}

.register-login {
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 20px;
}
.register-login a {
  color: var(--primary);
  font-weight: 600;
}
</style>
