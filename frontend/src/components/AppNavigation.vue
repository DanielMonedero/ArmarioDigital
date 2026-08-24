<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const toast = useToastStore()

const menuOpen = ref(false)

const navItems = [
  { name: 'wardrobe', label: 'Mi armario' },
  { name: 'for-sale', label: 'En venta' },
  { name: 'sold', label: 'Vendidas' }
] as const

async function handleLogout() {
  await auth.logout()
  toast.info('Sesión cerrada')
  await router.push({ name: 'login' })
}

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function closeMenu() {
  menuOpen.value = false
}
</script>

<template>
  <nav class="nav" :class="{ 'nav--open': menuOpen }" aria-label="Principal">
    <button
      class="nav__toggle"
      type="button"
      :aria-expanded="menuOpen"
      aria-controls="primary-menu"
      @click="toggleMenu"
    >
      <span class="sr-only">Menú</span>
      <span class="nav__toggle-bar" aria-hidden="true"></span>
      <span class="nav__toggle-bar" aria-hidden="true"></span>
      <span class="nav__toggle-bar" aria-hidden="true"></span>
    </button>

    <ul id="primary-menu" class="nav__list" role="list">
      <li v-for="item in navItems" :key="item.name">
        <RouterLink
          :to="{ name: item.name }"
          class="nav__link"
          :class="{ 'nav__link--active': route.name === item.name }"
          @click="closeMenu"
        >
          {{ item.label }}
        </RouterLink>
      </li>
      <li class="nav__user" v-if="auth.user">
        <span class="nav__user-name">{{ auth.user.displayName || auth.user.username }}</span>
        <button class="btn btn--ghost btn--sm" type="button" @click="handleLogout">
          Salir
        </button>
      </li>
    </ul>
  </nav>
</template>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.nav__list {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  list-style: none;
}

.nav__link {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: var(--radius-pill);
  font-size: 0.92rem;
  color: var(--color-text-muted);
  transition: background var(--transition-fast), color var(--transition-fast);
}

.nav__link:hover {
  background: var(--color-surface-muted);
  color: var(--color-text);
  text-decoration: none;
}

.nav__link--active {
  background: var(--color-primary);
  color: var(--color-primary-text);
}

.nav__link--active:hover {
  background: var(--color-primary-hover);
  color: var(--color-primary-text);
}

.nav__user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-left: var(--space-3);
  padding-left: var(--space-3);
  border-left: 1px solid var(--color-border);
}

.nav__user-name {
  font-size: 0.85rem;
  color: var(--color-text-muted);
  max-width: 160px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav__toggle {
  display: none;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
  padding: 0;
}

.nav__toggle-bar {
  display: block;
  width: 18px;
  height: 1.5px;
  background: var(--color-text);
  border-radius: 2px;
}

@media (max-width: 720px) {
  .nav__toggle {
    display: inline-flex;
  }

  .nav__list {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    flex-direction: column;
    align-items: stretch;
    background: var(--color-surface);
    border-bottom: 1px solid var(--color-border);
    padding: var(--space-3) var(--space-4);
    gap: var(--space-1);
    transform: translateY(-10px);
    opacity: 0;
    pointer-events: none;
    transition: opacity var(--transition-base), transform var(--transition-base);
  }

  .nav--open .nav__list {
    transform: translateY(0);
    opacity: 1;
    pointer-events: auto;
  }

  .nav__link {
    padding: 12px 14px;
    border-radius: var(--radius-md);
  }

  .nav__user {
    border-left: none;
    border-top: 1px solid var(--color-border);
    margin-left: 0;
    padding-left: 0;
    padding-top: var(--space-3);
    margin-top: var(--space-2);
    flex-direction: column;
    align-items: flex-start;
  }

  .nav__user-name {
    max-width: none;
  }
}
</style>
