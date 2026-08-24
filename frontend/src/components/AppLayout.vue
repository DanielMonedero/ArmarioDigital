<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppNavigation from '@/components/AppNavigation.vue'

const auth = useAuthStore()
const route = useRoute()

const isPublic = computed(() => route.meta.public === true)
const isBlankLayout = computed(() => route.meta.layout === 'blank')
</script>

<template>
  <div v-if="isPublic && isBlankLayout" class="layout layout--blank">
    <slot />
  </div>
  <div v-else class="layout">
    <header v-if="auth.isAuthenticated" class="layout__header">
      <div class="container layout__header-inner">
        <RouterLink :to="{ name: 'wardrobe' }" class="brand">
          <span class="brand__mark" aria-hidden="true">·</span>
          <span class="brand__name">Armario</span>
        </RouterLink>
        <AppNavigation />
      </div>
    </header>
    <main class="layout__main">
      <div class="container">
        <slot />
      </div>
    </main>
    <footer class="layout__footer">
      <div class="container">
        <span class="subtle">Inventario personal de ropa</span>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.layout--blank {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-5);
  background: linear-gradient(180deg, #fafaf7 0%, #f3f1ec 100%);
}

.layout__header {
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  z-index: 30;
  backdrop-filter: saturate(140%) blur(6px);
}

.layout__header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  min-height: var(--header-height);
}

.layout__main {
  flex: 1;
}

.layout__footer {
  border-top: 1px solid var(--color-border);
  padding: var(--space-4) 0;
  background: var(--color-surface);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-family: var(--font-serif);
  font-size: 1.15rem;
  letter-spacing: -0.01em;
}

.brand:hover {
  text-decoration: none;
}

.brand__mark {
  font-size: 1.6rem;
  line-height: 1;
  color: var(--color-accent);
}

@media (max-width: 600px) {
  .layout__header-inner {
    flex-wrap: wrap;
    min-height: 56px;
    padding-top: 6px;
    padding-bottom: 6px;
  }
}
</style>
