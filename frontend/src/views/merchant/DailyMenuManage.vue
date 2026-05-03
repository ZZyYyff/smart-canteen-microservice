<template>
  <div class="daily-menu-manage">
    <div class="flex-between mb-20">
      <div>
        <h2 class="page-title">每日菜单管理</h2>
        <p class="page-subtitle">创建并管理每日售卖菜单</p>
      </div>
      <el-button type="primary" @click="openCreate">新建每日菜单</el-button>
    </div>

    <!-- 筛选 -->
    <div class="card mb-16">
      <el-form :inline="true" :model="query" size="default">
        <el-form-item label="日期">
          <el-date-picker v-model="query.menuDate" type="date" placeholder="全部"
            value-format="YYYY-MM-DD" clearable style="width:170px" />
        </el-form-item>
        <el-form-item label="餐段">
          <el-select v-model="query.mealPeriod" placeholder="全部" clearable style="width:130px">
            <el-option label="早餐" value="BREAKFAST" />
            <el-option label="午餐" value="LUNCH" />
            <el-option label="晚餐" value="DINNER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">查询</el-button>
          <el-button @click="doReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 列表 -->
    <div class="card">
      <el-empty v-if="!loading && menus.length === 0" description="暂无每日菜单" />
      <el-table v-else :data="menus" v-loading="loading" style="width:100%">
        <el-table-column label="日期" width="120">
          <template #default="{ row }">{{ row.menuDate }}</template>
        </el-table-column>
        <el-table-column label="餐段" width="80">
          <template #default="{ row }">{{ row.mealPeriodDesc || row.mealPeriod }}</template>
        </el-table-column>
        <el-table-column label="售卖时间" min-width="180">
          <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
        </el-table-column>
        <el-table-column label="菜品数" width="80" align="center">
          <template #default="{ row }">{{ row.dishes?.length || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '生效中' : row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="openView(row)">查看菜品</el-button>
            <el-button size="small" text type="warning" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="菜单日期" prop="menuDate">
          <el-date-picker v-model="form.menuDate" type="date" placeholder="请选择日期"
            value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="餐段" prop="mealPeriod">
          <el-select v-model="form.mealPeriod" placeholder="请选择餐段" style="width:100%">
            <el-option label="早餐" value="BREAKFAST" />
            <el-option label="午餐" value="LUNCH" />
            <el-option label="晚餐" value="DINNER" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker v-model="form.startTime" placeholder="售卖开始时间"
            format="HH:mm" value-format="HH:mm:00" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker v-model="form.endTime" placeholder="售卖结束时间"
            format="HH:mm" value-format="HH:mm:00" style="width:100%" />
        </el-form-item>
        <el-form-item label="选择菜品" prop="selectedIds">
          <el-table :data="dishList" v-loading="dishLoading" max-height="280"
            style="width:100%" ref="dishTableRef" @selection-change="onSelect">
            <el-table-column type="selection" width="50" :selectable="canSelect" />
            <el-table-column prop="name" label="菜品" min-width="140" />
            <el-table-column label="价格" width="80">
              <template #default="{ row: d }">¥{{ d.price ?? '0.00' }}</template>
            </el-table-column>
            <el-table-column label="库存" width="70" prop="stock" />
            <el-table-column label="状态" width="80">
              <template #default="{ row: d }">
                <el-tag size="small" :type="d.status === 'ON_SALE' ? 'success' : 'info'">{{ d.statusDesc }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-8 text-secondary" style="font-size:13px">
            已选 {{ form.selectedIds.length }} 个菜品（仅可选"上架中"且"库存>0"的菜品）
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看菜品弹窗 -->
    <el-dialog v-model="viewVisible" title="菜单菜品" width="500px">
      <el-empty v-if="viewDishes.length === 0" description="暂未关联菜品" />
      <el-table v-else :data="viewDishes" style="width:100%">
        <el-table-column prop="name" label="菜品名称" min-width="140" />
        <el-table-column label="价格" width="80">
          <template #default="{ row }">¥{{ row.price ?? '0.00' }}</template>
        </el-table-column>
        <el-table-column label="库存" width="70" prop="stock" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'ON_SALE' ? 'success' : 'info'">{{ row.statusDesc }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getDailyMenuList, createDailyMenu, updateDailyMenu, deleteDailyMenu, listDishes } from '@/api/menu'
import type { DailyMenuVO, DishVO } from '@/types/api'

// ========== 列表 & 筛选 ==========
const menus = ref<DailyMenuVO[]>([])
const loading = ref(true)
const query = reactive({ menuDate: '', mealPeriod: '' })

async function loadList() {
  loading.value = true
  try {
    const p: Record<string, string> = {}
    if (query.menuDate) p.menuDate = query.menuDate
    if (query.mealPeriod) p.mealPeriod = query.mealPeriod
    menus.value = await getDailyMenuList(p)
  } catch { menus.value = [] } finally { loading.value = false }
}

function doSearch() { loadList() }
function doReset() {
  query.menuDate = ''
  query.mealPeriod = ''
  loadList()
}

// ========== 新建 / 编辑 ==========
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref<number | null>(null)
const dialogTitle = computed(() => editingId.value ? '编辑每日菜单' : '新建每日菜单')
const formRef = ref<FormInstance>()
const dishTableRef = ref()
const dishList = ref<DishVO[]>([])
const dishLoading = ref(false)

const form = reactive({
  menuDate: '',
  mealPeriod: '' as string,
  startTime: '',
  endTime: '',
  selectedIds: [] as number[],
})

const validateEndTime = (_r: unknown, v: string, cb: (e?: Error) => void) => {
  if (form.startTime && v && v <= form.startTime) cb(new Error('结束时间必须晚于开始时间'))
  else cb()
}
const validateDishes = (_r: unknown, _v: unknown, cb: (e?: Error) => void) => {
  if (form.selectedIds.length === 0) cb(new Error('至少选择一个菜品'))
  else cb()
}

const rules: FormRules = {
  menuDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  mealPeriod: [{ required: true, message: '请选择餐段', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    { validator: validateEndTime, trigger: 'change' },
  ],
  selectedIds: [{ validator: validateDishes, trigger: 'change' }],
}

function canSelect(d: DishVO): boolean {
  return d.status === 'ON_SALE' && d.stock > 0
}

function onSelect(rows: DishVO[]) {
  form.selectedIds = rows.map((r) => r.id)
  formRef.value?.validateField('selectedIds')
}

async function openCreate() {
  editingId.value = null
  form.menuDate = ''
  form.mealPeriod = ''
  form.startTime = ''
  form.endTime = ''
  form.selectedIds = []
  dialogVisible.value = true
  await loadDishes()
  await nextTick()
  dishTableRef.value?.clearSelection()
}

async function openEdit(row: DailyMenuVO) {
  editingId.value = row.id
  form.menuDate = row.menuDate
  form.mealPeriod = row.mealPeriod
  form.startTime = row.startTime
  form.endTime = row.endTime
  form.selectedIds = (row.dishes || []).map((d: DishVO) => d.id)
  dialogVisible.value = true
  await loadDishes()
  await nextTick()
  // 回显已选菜品
  dishList.value.forEach((d) => {
    if (form.selectedIds.includes(d.id)) {
      dishTableRef.value?.toggleRowSelection(d, true)
    }
  })
}

async function loadDishes() {
  dishLoading.value = true
  try { dishList.value = await listDishes() } catch { dishList.value = [] } finally { dishLoading.value = false }
}

function resetForm() {
  formRef.value?.resetFields()
  form.selectedIds = []
  editingId.value = null
}

async function handleSubmit() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return
  submitting.value = true
  try {
    const data = {
      menuDate: form.menuDate,
      mealPeriod: form.mealPeriod,
      startTime: form.startTime,
      endTime: form.endTime,
      dishIds: form.selectedIds,
    }
    if (editingId.value) {
      await updateDailyMenu(editingId.value, data)
      ElMessage.success('编辑成功')
    } else {
      await createDailyMenu(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadList()
  } catch { /* interceptor handles error */ } finally { submitting.value = false }
}

// ========== 删除 ==========
async function handleDelete(row: DailyMenuVO) {
  try {
    await ElMessageBox.confirm(`确定要删除 ${row.menuDate} 的${row.mealPeriodDesc || row.mealPeriod}菜单吗？此操作不可撤销。`, '删除确认', { type: 'warning' })
    await deleteDailyMenu(row.id)
    ElMessage.success('删除成功')
    await loadList()
  } catch { /* cancelled or error */ }
}

// ========== 查看 ==========
const viewVisible = ref(false)
const viewDishes = ref<DishVO[]>([])

function openView(row: DailyMenuVO) {
  viewDishes.value = row.dishes || []
  viewVisible.value = true
}

onMounted(loadList)
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 700; }
.flex-between { display: flex; align-items: center; justify-content: space-between; }
</style>
