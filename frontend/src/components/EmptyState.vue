<script setup lang="ts">
interface Props {
  label?: string
  description?: string
  icon?: 'shirt' | 'tag' | 'check' | 'plus'
}

withDefaults(defineProps<Props>(), {
  label: 'Nada por aquí todavía',
  description: '',
  icon: 'shirt'
})
</script>

<template>
  <div class="empty">
    <div class="empty__icon" aria-hidden="true">
      <svg v-if="icon === 'shirt'" viewBox="0 0 48 48" width="48" height="48" fill="none">
        <path
          d="M16 8l4-4h8l4 4 8 4-4 8-4-2v24H16V18l-4 2-4-8 8-4z"
          stroke="currentColor"
          stroke-width="1.5"
          stroke-linejoin="round"
        />
      </svg>
      <svg v-else-if="icon === 'tag'" viewBox="0 0 48 48" width="48" height="48" fill="none">
        <path
          d="M22 6h16v16L24 36 12 24 22 6z"
          stroke="currentColor"
          stroke-width="1.5"
          stroke-linejoin="round"
        />
        <circle cx="30" cy="14" r="2" stroke="currentColor" stroke-width="1.5" />
      </svg>
      <svg v-else-if="icon === 'check'" viewBox="0 0 48 48" width="48" height="48" fill="none">
        <circle cx="24" cy="24" r="18" stroke="currentColor" stroke-width="1.5" />
        <path
          d="M16 24l6 6 12-12"
          stroke="currentColor"
          stroke-width="1.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
      <svg v-else viewBox="0 0 48 48" width="48" height="48" fill="none">
        <path
          d="M24 12v24M12 24h24"
          stroke="currentColor"
          stroke-width="1.5"
          stroke-linecap="round"
        />
      </svg>
    </div>
    <h3 class="empty__title">{{ label }}</h3>
    <p v-if="description" class="empty__description">{{ description }}</p>
    <div v-if="$slots.action" class="empty__action">
      <slot name="action" />
    </div>
  </div>
</template>

<style scoped>
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-7) var(--space-4);
  text-align: center;
  color: var(--color-text-muted);
}

.empty__icon {
  color: var(--color-border-strong);
  margin-bottom: var(--space-4);
}

.empty__title {
  color: var(--color-text);
  font-size: 1.1rem;
  margin-bottom: 6px;
}

.empty__description {
  max-width: 360px;
  font-size: 0.92rem;
  color: var(--color-text-muted);
  margin-bottom: var(--space-4);
}

.empty__action {
  display: flex;
  gap: var(--space-2);
}
</style>
