<script setup lang="ts">
interface Props {
  title?: string
  message?: string
}

withDefaults(defineProps<Props>(), {
  title: 'No se pudo cargar la información',
  message: 'Comprueba tu conexión e inténtalo de nuevo.'
})

defineEmits<{ retry: [] }>()
</script>

<template>
  <div class="error-state">
    <div class="error-state__icon" aria-hidden="true">
      <svg viewBox="0 0 48 48" width="44" height="44" fill="none">
        <circle cx="24" cy="24" r="18" stroke="currentColor" stroke-width="1.5" />
        <path
          d="M24 16v10"
          stroke="currentColor"
          stroke-width="1.5"
          stroke-linecap="round"
        />
        <circle cx="24" cy="32" r="1.4" fill="currentColor" />
      </svg>
    </div>
    <h3 class="error-state__title">{{ title }}</h3>
    <p class="error-state__message">{{ message }}</p>
    <button class="btn btn--ghost" type="button" @click="$emit('retry')">
      <slot name="action">Reintentar</slot>
    </button>
  </div>
</template>

<style scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-6) var(--space-4);
  text-align: center;
  color: var(--color-text-muted);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
}

.error-state__icon {
  color: var(--color-danger);
  margin-bottom: var(--space-3);
}

.error-state__title {
  color: var(--color-text);
  margin-bottom: 6px;
}

.error-state__message {
  font-size: 0.92rem;
  margin-bottom: var(--space-4);
}
</style>
