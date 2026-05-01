import { defineStore } from 'pinia'
import { ref } from 'vue'
import { WsClient } from '@/utils/websocket'
import type { PickupQueueVO } from '@/types/api'

interface ScreenMessage {
  type: string
  windowId: number
  windowName: string
  currentPickupNo: number
  waitingQueue: PickupQueueVO[]
  timestamp: string
  message: string
}

export const usePickupStore = defineStore('pickup', () => {
  const connected = ref(false)
  const currentCall = ref<ScreenMessage | null>(null)
  const waitingQueue = ref<PickupQueueVO[]>([])
  const lastMessage = ref('')

  let client: WsClient | null = null

  function connect() {
    if (client) return

    const base = import.meta.env.VITE_WS_BASE_URL
    const url = `${base}/ws/pickup/screen`

    client = new WsClient(url, (data) => {
      const msg = data as ScreenMessage
      if (msg.type === 'CALL') {
        currentCall.value = {
          type: msg.type,
          windowId: msg.windowId,
          windowName: msg.windowName || '',
          currentPickupNo: msg.currentPickupNo,
          waitingQueue: msg.waitingQueue || [],
          timestamp: msg.timestamp || '',
          message: msg.message || '',
        }
        waitingQueue.value = msg.waitingQueue || []
        lastMessage.value = msg.message || ''
      }
    })

    client.setStatusHandler((status) => {
      connected.value = status
    })

    client.connect()
  }

  function disconnect() {
    if (client) {
      client.disconnect()
      client = null
    }
    connected.value = false
    currentCall.value = null
    waitingQueue.value = []
  }

  return { connected, currentCall, waitingQueue, lastMessage, connect, disconnect }
})
