<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ApiError } from '@/api/client'
import * as authApi from '@/api/auth'
import { describeError } from '@/utils/errors'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref<string | null>(null)
const showCreate = ref(false)
const newUser = ref({ username: '', password: '', displayName: '' })
const creating = ref(false)
const createError = ref<string | null>(null)

async function submitLogin() {
  errorMessage.value = null
  loading.value = true
  try {
    await auth.login(username.value.trim(), password.value)
    const redirect = (route.query.redirect as string | undefined) ?? '/wardrobe'
    await router.push(redirect)
  } catch (err) {
    errorMessage.value = err instanceof ApiError && err.code === 'BAD_CREDENTIALS'
      ? 'Usuario o contraseña incorrectos'
      : describeError(err)
  } finally {
    loading.value = false
  }
}

async function submitCreate() {
  createError.value = null
  creating.value = true
  try {
    await authApi.createUser({
      username: newUser.value.username.trim(),
      password: newUser.value.password,
      displayName: newUser.value.displayName.trim() || undefined
    })
    showCreate.value = false
    newUser.value = { username: '', password: '', displayName: '' }
    username.value = newUser.value.username
    errorMessage.value = null
    errorMessage.value = null
  } catch (err) {
    createError.value = describeError(err)
  } finally {
    creating.value = false
  }
}

const enableRegistration = computed(() => import.meta.env.DEV)
</script>

<template>
  <div class="login">
    <div class="login__card">
      <div class="login__brand">
        <span class="login__brand-mark">·</span>
        <span class="login__brand-name">Armario</span>
      </div>
      <h1 class="login__title">Bienvenida de nuevo</h1>
      <p class="login__subtitle">Inicia sesión para gestionar tu inventario personal.</p>

      <form class="login__form" @submit.prevent="submitLogin">
        <div class="field">
          <label class="field__label" for="login-username">Usuario</label>
          <input
            id="login-username"
            v-model="username"
            class="input"
            autocomplete="username"
            required
          />
        </div>
        <div class="field">
          <label class="field__label" for="login-password">Contraseña</label>
          <input
            id="login-password"
            v-model="password"
            class="input"
            type="password"
            autocomplete="current-password"
            required
          />
        </div>
        <p v-if="errorMessage" class="field__error">{{ errorMessage }}</p>
        <button class="btn btn--primary btn--block" type="submit" :disabled="loading">
          {{ loading ? 'Entrando…' : 'Entrar' }}
        </button>
      </form>

      <div v-if="enableRegistration" class="login__register">
        <button
          type="button"
          class="btn btn--ghost btn--block"
          @click="showCreate = !showCreate"
        >
          {{ showCreate ? 'Cancelar alta' : 'Crear cuenta' }}
        </button>

        <form v-if="showCreate" class="login__register-form" @submit.prevent="submitCreate">
          <div class="field">
            <label class="field__label" for="register-username">Usuario</label>
            <input
              id="register-username"
              v-model="newUser.username"
              class="input"
              minlength="3"
              maxlength="64"
              required
            />
          </div>
          <div class="field">
            <label class="field__label" for="register-displayName">Nombre visible</label>
            <input
              id="register-displayName"
              v-model="newUser.displayName"
              class="input"
              maxlength="120"
            />
          </div>
          <div class="field">
            <label class="field__label" for="register-password">Contraseña (mín. 8)</label>
            <input
              id="register-password"
              v-model="newUser.password"
              class="input"
              type="password"
              minlength="8"
              maxlength="200"
              required
            />
          </div>
          <p v-if="createError" class="field__error">{{ createError }}</p>
          <button class="btn btn--primary btn--block" type="submit" :disabled="creating">
            {{ creating ? 'Creando…' : 'Crear cuenta' }}
          </button>
          <p class="subtle">
            Solo disponible en modo desarrollo cuando el backend tiene activado
            <code>APP_AUTH_ALLOW_USER_CREATION=true</code>.
          </p>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login {
  width: 100%;
  max-width: 420px;
}

.login__card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  padding: var(--space-6);
  box-shadow: var(--shadow-strong);
}

.login__brand {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-family: var(--font-serif);
  font-size: 1.1rem;
  margin-bottom: var(--space-5);
}

.login__brand-mark {
  color: var(--color-accent);
  font-size: 1.4rem;
  line-height: 1;
}

.login__title {
  font-size: 1.5rem;
  margin-bottom: var(--space-2);
}

.login__subtitle {
  color: var(--color-text-muted);
  margin-bottom: var(--space-5);
}

.login__form {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.login__register {
  margin-top: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.login__register-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  background: var(--color-surface-muted);
  padding: var(--space-4);
  border-radius: var(--radius-md);
}
</style>
