<script setup lang="ts">
interface Props {
  label?: string
  inline?: boolean
}

withDefaults(defineProps<Props>(), {
  label: '',
  inline: false
})
</script>

<template>
  <div v-if="inline" class="loading loading--inline" role="status" aria-live="polite">
    <span class="loading__spinner" aria-hidden="true"></span>
    <span v-if="label" class="loading__label">{{ label }}</span>
    <span class="sr-only">{{ label || 'Cargando' }}</span>
  </div>
  <div v-else class="loading" role="status" aria-live="polite">
    <span class="loading__spinner loading__spinner--lg" aria-hidden="true"></span>
    <span v-if="label" class="loading__label">{{ label }}</span>
    <span class="sr-only">{{ label || 'Cargando' }}</span>
  </div>
</template>

<style scoped>
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-7) var(--space-4);
  gap: var(--space-3);
  color: var(--color-text-muted);
}

.loading--inline {
  padding: var(--space-3) 0;
  flex-direction: row;
}

.loading__spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.loading__spinner--lg {
  width: 28px;
  height: 28px;
  border-width: 3px;
}

.loading__label {
  font-size: 0.9rem;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .loading__spinner {
    animation-duration: 1.6s;
  }
}
</style>
