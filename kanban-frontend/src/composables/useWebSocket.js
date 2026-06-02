import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let stompClient = null
const isConnected = ref(false)
const subscriptions = new Map()

export function useWebSocket() {
  function connect() {
    if (stompClient?.connected) return

    const token = localStorage.getItem('kanban_token')
    stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => { isConnected.value = true },
      onDisconnect: () => { isConnected.value = false }
    })
    stompClient.activate()
  }

  function disconnect() {
    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
      isConnected.value = false
      subscriptions.clear()
    }
  }

  function subscribeToBoard(boardId, callback) {
    const dest = `/topic/board/${boardId}`
    if (!stompClient?.connected) {
      // Defer subscription until connected
      const checkInterval = setInterval(() => {
        if (stompClient?.connected) {
          clearInterval(checkInterval)
          const sub = stompClient.subscribe(dest, callback)
          subscriptions.set(dest, sub)
        }
      }, 500)
      return
    }
    if (subscriptions.has(dest)) {
      subscriptions.get(dest).unsubscribe()
    }
    const sub = stompClient.subscribe(dest, callback)
    subscriptions.set(dest, sub)
  }

  function unsubscribeFromBoard(boardId) {
    const dest = `/topic/board/${boardId}`
    if (subscriptions.has(dest)) {
      subscriptions.get(dest).unsubscribe()
      subscriptions.delete(dest)
    }
  }

  return { connect, disconnect, isConnected, subscribeToBoard, unsubscribeFromBoard }
}
