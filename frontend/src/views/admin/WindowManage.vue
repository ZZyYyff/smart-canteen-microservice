<template>
  <div class="admin-window-manage">
    <div class="flex-between mb-20">
      <div>
        <h2 class="page-title">取餐窗口管理</h2>
        <p class="page-subtitle">管理食堂取餐窗口，支持启用和停用</p>
      </div>
      <el-button type="primary" @click="openCreate">新增窗口</el-button>
    </div>

    <!-- 窗口列表 -->
    <div class="card">
      <el-empty v-if="!loading && windows.length === 0" description="暂无取餐窗口" />
      <el-table v-else :data="windows" v-loading="loading" style="width:100%">
        <el-table-column label="编号" width="80">
          <template #default="{ row }">#{{ row.id }}</template>
        </el-table-column>
        <el-table-column prop="name" label="窗口名称" min-width="140" />
        <el-table-column prop="location" label="位置" min-width="140">
          <template #default="{ row }">{{ row.location || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">{{ row.createdAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 'ACTIVE'"
              size="small"
              type="success"
              text
              @click="handleEnable(row.id)"
            >
              启用
            </el-button>
            <el-button
              v-else
              size="small"
              type="warning"
              text
              @click="handleDisable(row.id)"
            >
              停用
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增取餐窗口" width="460px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="窗口名称" prop="name">
          <el-input v-model="form.name" placeholder="如：1号窗口" maxlength="50" clearable />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="如：一楼食堂东侧（选填）" maxlength="100" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { listWindows, createWindow, enableWindow, disableWindow } from '@/api/pickup'
import type { PickupWindowVO } from '@/types/api'

// ========== 列表 ==========
const windows = ref<PickupWindowVO[]>([])
const loading = ref(true)

async function loadWindows() {
  loading.value = true
  try { windows.value = await listWindows() } catch { windows.value = [] } finally { loading.value = false }
}

// ========== 新增 ==========
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  location: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入窗口名称', trigger: 'blur' }],
}

function openCreate() {
  form.name = ''
  form.location = ''
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function handleCreate() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return
  submitting.value = true
  try {
    await createWindow({ name: form.name, location: form.location || undefined })
    ElMessage.success('窗口创建成功')
    dialogVisible.value = false
    await loadWindows()
  } catch { /* interceptor handles */ } finally { submitting.value = false }
}

// ========== 启用 / 停用 ==========
async function handleEnable(id: number) {
  try {
    await enableWindow(id)
    ElMessage.success('窗口已启用')
    await loadWindows()
  } catch { /* interceptor handles */ }
}

async function handleDisable(id: number) {
  try {
    await ElMessageBox.confirm('停用后该窗口将无法用于下单和取餐，确定继续？', '停用确认', { type: 'warning' })
    await disableWindow(id)
    ElMessage.success('窗口已停用')
    await loadWindows()
  } catch { /* cancelled or error */ }
}

onMounted(loadWindows)
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 700; }
.flex-between { display: flex; align-items: center; justify-content: space-between; }
</style>
