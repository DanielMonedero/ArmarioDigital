import { defineStore } from 'pinia'
import { ref } from 'vue'

export type ToastKind = 'success' | 'error' | 'info'

export interface Toast {
  id: number
  kind: ToastKind
  message: string
}

let nextId = 1

export const useToastStore = defineStore('toast', () => {
  const toasts = ref<Toast[]>([])

  function show(kind: ToastKind, message: string, ttlMs = 3500) {
    const id = nextId++
    toasts.value.push({ id, kind, message })
    window.setTimeout(() => dismiss(id), ttlMs)
  }

  function dismiss(id: number) {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  function success(message: string) {
    show('success', message)
  }

  function error(message: string) {
    show('error', message)
  }

  function info(message: string) {
    show('info', message)
  }

  return { toasts, show, dismiss, success, error, info }
})
