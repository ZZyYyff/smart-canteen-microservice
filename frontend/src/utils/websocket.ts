export type MessageHandler = (data: unknown) => void

export class WsClient {
  private url: string
  private ws: WebSocket | null = null
  private onMessage: MessageHandler
  private onStatusChange: ((connected: boolean) => void) | null = null
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null
  private reconnectDelay = 2000
  private intentionalClose = false

  constructor(url: string, onMessage: MessageHandler) {
    this.url = url
    this.onMessage = onMessage
  }

  setStatusHandler(handler: (connected: boolean) => void) {
    this.onStatusChange = handler
  }

  connect() {
    this.intentionalClose = false
    this.doConnect()
  }

  private doConnect() {
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return
    }

    try {
      this.ws = new WebSocket(this.url)
    } catch {
      this.scheduleReconnect()
      return
    }

    this.ws.onopen = () => {
      this.reconnectDelay = 2000
      this.onStatusChange?.(true)
    }

    this.ws.onmessage = (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data)
        this.onMessage(data)
      } catch {
        // non-JSON message, ignore
      }
    }

    this.ws.onclose = () => {
      this.onStatusChange?.(false)
      if (!this.intentionalClose) {
        this.scheduleReconnect()
      }
    }

    this.ws.onerror = () => {
      // onclose will fire after onerror
    }
  }

  private scheduleReconnect() {
    if (this.reconnectTimer) return
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this.reconnectDelay = Math.min(this.reconnectDelay * 1.5, 30000)
      this.doConnect()
    }, this.reconnectDelay)
  }

  disconnect() {
    this.intentionalClose = true
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.onStatusChange?.(false)
  }
}
