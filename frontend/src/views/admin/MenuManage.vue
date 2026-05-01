<template>
  <div class="admin-menu-manage">
    <div class="flex-between mb-20">
      <h2 class="page-title">全局菜单管理</h2>
      <el-button type="primary" @click="openCreateDialog">新增菜品</el-button>
    </div>

    <div class="card">
      <el-form :inline="true" class="mb-16">
        <el-form-item label="名称">
          <el-input v-model="filterName" placeholder="搜索菜品" clearable @input="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width:120px" @change="loadData">
            <el-option label="上架中" value="ON_SALE" />
            <el-option label="已下架" value="OFF_SALE" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="dishes" v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="菜品名称" min-width="140" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="库存" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.lowStock ? '#F97316' : '', fontWeight: row.lowStock ? 700 : 400 }">
              {{ row.stock }}
            </span>
            <el-tag v-if="row.lowStock" type="warning" size="small" style="margin-left:4px">预警</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ON_SALE' ? 'success' : 'info'" size="small">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'OFF_SALE'"
              size="small"
              text
              type="success"
              @click="handleOnSale(row.id)"
            >
              上架
            </el-button>
            <el-button
              v-if="row.status === 'ON_SALE'"
              size="small"
              text
              type="warning"
              @click="handleOffSale(row.id)"
            >
              下架
            </el-button>
            <el-popconfirm title="确定删除该菜品？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" text type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingDish ? '编辑菜品' : '新增菜品'"
      width="520px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="菜品名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="菜品描述（选填）" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.imageUrl" placeholder="图片 URL（选填）" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="预警阈值">
          <el-input-number v-model="form.warningStock" :min="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ editingDish ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { listDishes, createDish, updateDish, deleteDish, onSaleDish, offSaleDish } from '@/api/menu'
import type { DishVO } from '@/types/api'

const dishes = ref<DishVO[]>([])
const loading = ref(true)
const dialogVisible = ref(false)
const editingDish = ref<DishVO | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()

const filterName = ref('')
const filterStatus = ref('')

const form = reactive({
  name: '',
  price: 0.01,
  description: '',
  imageUrl: '',
  stock: 0,
  warningStock: 0,
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  price: [{ required: true, message: '价格必须大于 0', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    const params: { name?: string; status?: string } = {}
    if (filterName.value) params.name = filterName.value
    if (filterStatus.value) params.status = filterStatus.value
    dishes.value = await listDishes(params)
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingDish.value = null
  form.name = ''
  form.price = 0.01
  form.description = ''
  form.imageUrl = ''
  form.stock = 0
  form.warningStock = 0
  dialogVisible.value = true
}

function openEditDialog(dish: DishVO) {
  editingDish.value = dish
  form.name = dish.name
  form.price = dish.price
  form.description = dish.description || ''
  form.imageUrl = dish.imageUrl || ''
  form.stock = dish.stock
  form.warningStock = dish.warningStock || 0
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const data = {
      name: form.name,
      price: form.price,
      description: form.description || undefined,
      imageUrl: form.imageUrl || undefined,
      stock: form.stock,
      warningStock: form.warningStock || undefined,
    }
    if (editingDish.value) {
      await updateDish(editingDish.value.id, data)
      ElMessage.success('修改成功')
    } else {
      await createDish(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch { /* */ } finally {
    saving.value = false
  }
}

async function handleOnSale(id: number) {
  try {
    await onSaleDish(id)
    ElMessage.success('上架成功')
    await loadData()
  } catch { /* */ }
}

async function handleOffSale(id: number) {
  try {
    await offSaleDish(id)
    ElMessage.success('下架成功')
    await loadData()
  } catch { /* */ }
}

async function handleDelete(id: number) {
  try {
    await deleteDish(id)
    ElMessage.success('删除成功')
    await loadData()
  } catch { /* */ }
}

onMounted(loadData)
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 700;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
