import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getCurrentUser as getCurrentUserApi } from '@/api/user'
import type { UserVO } from '@/types/api'
import {
  getToken,
  setToken,
  removeToken,
  getRefreshToken,
  setRefreshToken,
  removeRefreshToken,
  getUserInfo,
  setUserInfo,
  removeUserInfo,
} from '@/utils/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(getToken() || '')
  const refreshToken = ref<string>(getRefreshToken() || '')
  const userInfo = ref<UserVO | null>(getUserInfo<UserVO>())

  const role = computed(() => userInfo.value?.role || '')
  const isLoggedIn = computed(() => !!token.value)

  async function login(phoneOrStudentNo: string, password: string) {
    const params: { password: string; phone?: string; studentNo?: string } = { password }
    if (/^1[3-9]\d{9}$/.test(phoneOrStudentNo)) {
      params.phone = phoneOrStudentNo
    } else {
      params.studentNo = phoneOrStudentNo
    }
    const res = await loginApi(params)
    token.value = res.token
    refreshToken.value = res.refreshToken
    userInfo.value = res.user

    setToken(res.token)
    setRefreshToken(res.refreshToken)
    setUserInfo(res.user)
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    removeToken()
    removeRefreshToken()
    removeUserInfo()
  }

  async function refreshUserInfo() {
    const res = await getCurrentUserApi()
    userInfo.value = res
    setUserInfo(res)
  }

  return {
    token,
    refreshToken,
    userInfo,
    role,
    isLoggedIn,
    login,
    logout,
    refreshUserInfo,
  }
})
