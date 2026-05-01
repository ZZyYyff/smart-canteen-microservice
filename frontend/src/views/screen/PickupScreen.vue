<template>
  <div class="pickup-screen">
    <!-- 顶部 -->
    <header class="screen-header">
      <h1 class="screen-title">智能食堂取餐大屏</h1>
      <div class="screen-time">
        <span>{{ currentDate }}</span>
        <span class="screen-time__clock">{{ currentTime }}</span>
      </div>
    </header>

    <!-- 连接状态 -->
    <div v-if="!store.connected" class="screen-reconnect">
      <span class="reconnect-dot"></span>
      正在重连...
    </div>

    <!-- 主体 -->
    <main class="screen-body">
      <!-- 当前叫号 -->
      <section class="screen-call">
        <template v-if="store.currentCall">
          <div class="screen-call__label">当前叫号</div>
          <div class="screen-call__number">{{ store.currentCall.currentPickupNo }}</div>
          <div class="screen-call__window">{{ store.currentCall.windowName }}</div>
          <div class="screen-call__text">请到 {{ store.currentCall.windowName }} 窗口取餐</div>
        </template>
        <template v-else>
          <div class="screen-call__label">等待叫号中</div>
          <div class="screen-call__placeholder">--</div>
          <div class="screen-call__text">暂无叫号信息</div>
        </template>
      </section>

      <!-- 等待队列 -->
      <section class="screen-queue">
        <h3 class="screen-queue__title">等待队列</h3>
        <div v-if="store.waitingQueue.length === 0" class="screen-queue__empty">
          <p>暂无排队</p>
        </div>
        <div v-else class="screen-queue__list">
          <div
            v-for="item in store.waitingQueue.slice(0, 8)"
            :key="item.id"
            class="queue-item"
          >
            <span class="queue-item__no">{{ item.pickupNo }}</span>
            <span class="queue-item__window">{{ item.windowName || '窗口' + item.windowId }}</span>
            <span class="queue-item__status">{{ item.statusDesc }}</span>
          </div>
        </div>
      </section>
    </main>

    <!-- 底部 -->
    <footer class="screen-footer">
      <p>请留意叫号，凭取餐码到对应窗口取餐</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import dayjs from 'dayjs'
import { usePickupStore } from '@/stores/pickup'

const store = usePickupStore()

const currentDate = ref('')
const currentTime = ref('')

let clockTimer: ReturnType<typeof setInterval> | null = null

function updateClock() {
  const now = dayjs()
  currentDate.value = now.format('YYYY年MM月DD日 dddd')
  currentTime.value = now.format('HH:mm:ss')
}

onMounted(() => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
  store.connect()
})

onUnmounted(() => {
  if (clockTimer) {
    clearInterval(clockTimer)
    clockTimer = null
  }
  store.disconnect()
})
</script>

<style scoped>
.pickup-screen {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: #F9FAFB;
  font-size: 18px;
}

/* ===== Header ===== */
.screen-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 40px;
  border-bottom: 1px solid #374151;
  flex-shrink: 0;
}

.screen-title {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 2px;
}

.screen-time {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  font-size: 16px;
  color: #9CA3AF;
}

.screen-time__clock {
  font-size: 28px;
  font-weight: 700;
  color: #F9FAFB;
  font-variant-numeric: tabular-nums;
}

/* ===== Reconnect ===== */
.screen-reconnect {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  background: #F97316;
  color: #FFF;
  font-size: 15px;
  flex-shrink: 0;
}

.reconnect-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #FFF;
  animation: blink 1s infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

/* ===== Body ===== */
.screen-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.screen-call {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #374151;
  padding: 40px;
}

.screen-call__label {
  font-size: 22px;
  color: #9CA3AF;
  margin-bottom: 12px;
}

.screen-call__number {
  font-size: 160px;
  font-weight: 700;
  color: #F97316;
  line-height: 1;
}

.screen-call__placeholder {
  font-size: 160px;
  font-weight: 700;
  color: #374151;
  line-height: 1;
}

.screen-call__window {
  font-size: 36px;
  font-weight: 600;
  margin-top: 12px;
  color: #34D399;
}

.screen-call__text {
  font-size: 24px;
  color: #D1D5DB;
  margin-top: 16px;
}

.screen-queue {
  width: 420px;
  display: flex;
  flex-direction: column;
  padding: 30px 24px;
  flex-shrink: 0;
}

.screen-queue__title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #374151;
}

.screen-queue__empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6B7280;
  font-size: 20px;
}

.screen-queue__list {
  flex: 1;
  overflow-y: auto;
}

.queue-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 8px;
  margin-bottom: 8px;
  background: #1F2937;
  font-size: 18px;
}

.queue-item__no {
  font-size: 28px;
  font-weight: 700;
  color: #F97316;
  width: 60px;
  text-align: center;
  flex-shrink: 0;
}

.queue-item__window {
  flex: 1;
  color: #D1D5DB;
}

.queue-item__status {
  color: #FBBF24;
  font-size: 14px;
  flex-shrink: 0;
}

/* ===== Footer ===== */
.screen-footer {
  border-top: 1px solid #374151;
  padding: 16px 40px;
  text-align: center;
  color: #6B7280;
  font-size: 16px;
  flex-shrink: 0;
}
</style>
