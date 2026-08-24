<script setup lang="ts">
import { useToastStore } from '@/stores/toast'

const toast = useToastStore()
</script>

<template>
  <div v-if="toast.toasts.length" class="toast-stack" role="region" aria-live="polite">
    <div
      v-for="t in toast.toasts"
      :key="t.id"
      class="toast"
      :class="{
        'toast--success': t.kind === 'success',
        'toast--error': t.kind === 'error',
        'toast--info': t.kind === 'info'
      }"
      role="status"
      @click="toast.dismiss(t.id)"
    >
      {{ t.message }}
    </div>
  </div>
</template>
