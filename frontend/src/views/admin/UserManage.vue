<template>
  <div class="admin-user-manage">
    <h2 class="page-title mb-20">用户管理</h2>

    <!-- 搜索和筛选 -->
    <el-card class="mb-16">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-input v-model="query.keyword" placeholder="搜索手机号/学工号/昵称" clearable
            @clear="handleSearch" @keyup.enter="handleSearch">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-col>
        <el-col :span="5">
          <el-select v-model="query.filterRole" placeholder="角色筛选" clearable @change="handleSearch" style="width:100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="商家" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-select v-model="query.status" placeholder="状态筛选" clearable @change="handleSearch" style="width:100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="已禁用" value="DISABLED" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 用户列表 -->
    <el-card>
      <el-table :data="tableData" v-loading="loading" stripe border style="width:100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="studentNo" label="学工号" width="110" />
        <el-table-column prop="nickname" label="昵称" min-width="100" />
        <el-table-column label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)" size="small">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'NORMAL' ? 'success' : 'danger'" size="small">
              {{ row.status === 'NORMAL' ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170" />
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'DISABLED'" type="success" size="small"
              :disabled="row.id === authStore.userInfo?.id"
              @click="handleEnable(row)">启用</el-button>
            <el-button v-if="row.status === 'NORMAL'" type="warning" size="small"
              :disabled="row.id === authStore.userInfo?.id"
              @click="handleDisable(row)">禁用</el-button>
            <el-dropdown trigger="click" style="margin-left:8px"
              @command="(role:string) => handleRoleChange(row, role)">
              <el-button size="small">改角色 <el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="STUDENT" :disabled="row.role === 'STUDENT'">学生</el-dropdown-item>
                  <el-dropdown-item command="MERCHANT" :disabled="row.role === 'MERCHANT'">商家</el-dropdown-item>
                  <el-dropdown-item command="ADMIN"
                    :disabled="row.role === 'ADMIN' || row.id === authStore.userInfo?.id">管理员</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-16 flex-right">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getAdminUserList, enableUser, disableUser, updateUserRole } from '@/api/user'
import type { UserVO } from '@/types/api'

const authStore = useAuthStore()
const loading = ref(false)
const total = ref(0)
const tableData = ref<UserVO[]>([])
const query = reactive({
  keyword: '',
  filterRole: '',
  status: '',
  page: 1,
  size: 10,
})

function roleTagType(role: string) {
  return { STUDENT: '', MERCHANT: 'warning', ADMIN: 'danger' }[role] || ''
}
function roleLabel(role: string) {
  return { STUDENT: '学生', MERCHANT: '商家', ADMIN: '管理员' }[role] || role
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getAdminUserList({ ...query })
    tableData.value = res.list
    total.value = res.total
  } catch { /* interceptor handles error */ }
  finally { loading.value = false }
}

function handleSearch() { query.page = 1; fetchData() }
function handleReset() {
  query.keyword = ''; query.filterRole = ''; query.status = ''; query.page = 1; fetchData()
}

async function handleEnable(row: UserVO) {
  try { await enableUser(row.id); ElMessage.success('已启用'); fetchData() } catch {}
}

async function handleDisable(row: UserVO) {
  try {
    await ElMessageBox.confirm(`确定禁用「${row.nickname || row.phone}」吗？`, '确认', { type: 'warning' })
    await disableUser(row.id)
    ElMessage.success('已禁用')
    fetchData()
  } catch {}
}

async function handleRoleChange(row: UserVO, role: string) {
  if (row.id === authStore.userInfo?.id) { ElMessage.warning('不能修改自己的角色'); return }
  try { await updateUserRole(row.id, role); ElMessage.success('角色已修改'); fetchData() } catch {}
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 700; }
.flex-right { display: flex; justify-content: flex-end; }
</style>
