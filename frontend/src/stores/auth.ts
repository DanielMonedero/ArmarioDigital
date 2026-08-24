import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import * as authApi from '@/api/auth'
import { ApiError } from '@/api/client'
import type { UserResponse } from '@/types/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserResponse | null>(null)
  const status = ref<'idle' | 'loading' | 'authenticated' | 'anonymous'>('idle')
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => status.value === 'authenticated')

  async function bootstrap() {
    status.value = 'loading'
    try {
      user.value = await authApi.me()
      status.value = 'authenticated'
    } catch (err) {
      // The backend's entry point returns 401 with an empty body, which
      // ApiError normalizes to UNAUTHORIZED. Anything 4xx here means the
      // user simply isn't logged in.
      if (err instanceof ApiError && (err.status === 401 || err.status === 403 || err.status === 404)) {
        user.value = null
        status.value = 'anonymous'
      } else if (err instanceof ApiError) {
        user.value = null
        status.value = 'anonymous'
      } else {
        status.value = 'anonymous'
        user.value = null
      }
    }
  }

  async function login(username: string, password: string) {
    status.value = 'loading'
    error.value = null
    try {
      const response = await authApi.login({ username, password })
      user.value = response.user
      status.value = 'authenticated'
    } catch (err) {
      status.value = 'anonymous'
      user.value = null
      if (err instanceof ApiError) {
        error.value =
          err.code === 'BAD_CREDENTIALS' ? 'Usuario o contraseña incorrectos' : err.message
      } else {
        error.value = 'No se pudo iniciar sesión. Inténtalo de nuevo.'
      }
      throw err
    }
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // Even if the backend call fails we want to drop the local session.
    }
    user.value = null
    status.value = 'anonymous'
  }

  function clearError() {
    error.value = null
  }

  return {
    user,
    status,
    error,
    isAuthenticated,
    bootstrap,
    login,
    logout,
    clearError
  }
})
