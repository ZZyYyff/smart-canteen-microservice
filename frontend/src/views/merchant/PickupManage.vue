<template>
  <div class="merchant-pickup-manage">
    <h2 class="page-title mb-20">取餐叫号与核销</h2>

    <el-row :gutter="16">
      <!-- 左侧：窗口列表与叫号 -->
      <el-col :xs="24" :md="14">
        <div class="card mb-16">
          <h3 class="mb-16">选择窗口</h3>
          <el-select
            v-model="selectedWindowId"
            placeholder="请选择取餐窗口"
            style="width:100%"
            size="large"
            @change="loadQueue"
          >
            <el-option
              v-for="w in windows"
              :key="w.id"
              :label="w.name + (w.location ? ' - ' + w.location : '')"
              :value="w.id"
            >
              <span>{{ w.name }}</span>
              <el-tag size="small" :type="w.status === 'ACTIVE' ? 'success' : 'info'" style="margin-left:8px">
                {{ w.statusDesc }}
              </el-tag>
            </el-option>
          </el-select>

          <!-- 当前叫号 -->
          <div v-if="currentCalled" class="current-call card" style="margin-top:16px;background:#FFF7ED;border:2px solid #F97316">
            <div class="current-call__label">当前叫号</div>
            <div class="current-call__number">{{ currentCalled.pickupNo }}</div>
            <div class="current-call__info">{{ currentCalled.windowName }}</div>
            <el-button type="primary" size="large" style="margin-top:12px" @click="handleCallNext">
              叫下一号
            </el-button>
          </div>
          <div v-else-if="selectedWindowId" class="text-center" style="padding:24px 0">
            <el-button type="primary" size="large" @click="handleCallNext">叫下一号</el-button>
          </div>
          <div v-else class="text-center text-secondary" style="padding:24px 0">
            请先选择窗口
          </div>
        </div>

        <!-- 等待队列 -->
        <div v-if="selectedWindowId" class="card">
          <h3 class="mb-16">等待队列（{{ waitingQueue.length }}）</h3>
          <el-empty v-if="waitingQueue.length === 0" description="暂无等待" />
          <el-table v-else :data="waitingQueue" style="width:100%">
            <el-table-column prop="pickupNo" label="取餐号" width="80">
              <template #default="{ row }">
                <strong>{{ row.pickupNo }}</strong>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === 'WAITING' ? 'warning' : 'info'">
                  {{ row.statusDesc }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="queueTime" label="排队时间" min-width="160" />
          </el-table>
        </div>
      </el-col>

      <!-- 右侧：核销 -->
      <el-col :xs="24" :md="10">
        <div class="card">
          <h3 class="mb-16">取餐核销</h3>
          <el-form :model="verifyForm" label-position="top">
            <el-form-item label="取餐号">
              <el-input-number
                v-model="verifyForm.pickupNo"
                :min="1"
                placeholder="请输入取餐号"
                style="width:100%"
                size="large"
              />
            </el-form-item>
            <el-form-item label="取餐码">
              <el-input
                v-model="verifyForm.pickupCode"
                placeholder="请输入取餐码"
                size="large"
                clearable
                @keyup.enter="handleVerify"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                style="width:100%"
                :loading="verifying"
                :disabled="!verifyForm.pickupNo || !verifyForm.pickupCode"
                @click="handleVerify"
              >
                确认核销
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listWindows, getWindowQueue, callNext, verifyPickup } from '@/api/pickup'
import type { PickupWindowVO, PickupQueueVO } from '@/types/api'

const windows = ref<PickupWindowVO[]>([])
const selectedWindowId = ref<number | null>(null)
const waitingQueue = ref<PickupQueueVO[]>([])
const currentCalled = ref<PickupQueueVO | null>(null)
const verifying = ref(false)

const verifyForm = reactive({
  pickupNo: null as number | null,
  pickupCode: '',
})

onMounted(async () => {
  try {
    windows.value = await listWindows()
  } catch { /* */ }
})

async function loadQueue() {
  if (!selectedWindowId.value) return
  try {
    waitingQueue.value = await getWindowQueue(selectedWindowId.value)
  } catch { /* */ }
}

async function handleCallNext() {
  if (!selectedWindowId.value) return
  try {
    const result = await callNext(selectedWindowId.value)
    currentCalled.value = result
    ElMessage.success(`叫号成功：${result.pickupNo} 号`)
    await loadQueue()
  } catch { /* */ }
}

async function handleVerify() {
  if (!verifyForm.pickupNo || !verifyForm.pickupCode) {
    ElMessage.warning('请输入取餐号和取餐码')
    return
  }
  verifying.value = true
  try {
    const result = await verifyPickup({
      pickupNo: verifyForm.pickupNo,
      pickupCode: verifyForm.pickupCode,
    })
    ElMessage.success(`核销成功：${result.pickupNo} 号`)
    verifyForm.pickupNo = null
    verifyForm.pickupCode = ''
    if (currentCalled.value && currentCalled.value.pickupNo === result.pickupNo) {
      currentCalled.value = null
    }
    await loadQueue()
  } catch { /* */ } finally {
    verifying.value = false
  }
}
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 700;
}

.current-call {
  text-align: center;
  padding: 24px;
}

.current-call__label {
  font-size: 14px;
  color: #F97316;
  margin-bottom: 8px;
}

.current-call__number {
  font-size: 64px;
  font-weight: 700;
  color: #F97316;
  line-height: 1;
}

.current-call__info {
  font-size: 15px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.text-center {
  text-align: center;
}
</style>
