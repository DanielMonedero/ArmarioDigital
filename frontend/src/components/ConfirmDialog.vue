<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'

interface Props {
  open: boolean
  title: string
  message?: string
  confirmLabel?: string
  cancelLabel?: string
  variant?: 'danger' | 'default'
}

const props = withDefaults(defineProps<Props>(), {
  message: '',
  confirmLabel: 'Confirmar',
  cancelLabel: 'Cancelar',
  variant: 'default'
})

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

const dialogRef = ref<HTMLDialogElement | null>(null)

function close() {
  dialogRef.value?.close()
}

function onCancel() {
  emit('cancel')
  close()
}

function onConfirm() {
  emit('confirm')
  close()
}

function onBackdrop(event: MouseEvent) {
  if (event.target === dialogRef.value) {
    onCancel()
  }
}

function onKeydown(event: KeyboardEvent) {
  if (!props.open) return
  if (event.key === 'Escape') {
    event.preventDefault()
    onCancel()
  }
}

onMounted(() => {
  window.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
})

function syncOpen(value: boolean) {
  const dlg = dialogRef.value
  if (!dlg) return
  if (value && !dlg.open) {
    dlg.showModal()
  } else if (!value && dlg.open) {
    dlg.close()
  }
}

import { watch } from 'vue'
watch(
  () => props.open,
  (v) => syncOpen(v)
)
</script>

<template>
  <dialog
    ref="dialogRef"
    class="confirm-dialog"
    :class="{ 'confirm-dialog--danger': variant === 'danger' }"
    @click="onBackdrop"
    @close="emit('cancel')"
  >
    <div class="confirm-dialog__inner">
      <h2 class="confirm-dialog__title">{{ title }}</h2>
      <p v-if="message" class="confirm-dialog__message">{{ message }}</p>
      <div class="confirm-dialog__actions">
        <button class="btn btn--ghost" type="button" @click="onCancel">
          {{ cancelLabel }}
        </button>
        <button
          class="btn"
          :class="variant === 'danger' ? 'btn--danger' : 'btn--primary'"
          type="button"
          @click="onConfirm"
        >
          {{ confirmLabel }}
        </button>
      </div>
    </div>
  </dialog>
</template>

<style scoped>
.confirm-dialog {
  border: none;
  border-radius: var(--radius-lg);
  padding: 0;
  background: var(--color-surface);
  max-width: 440px;
  width: calc(100vw - 32px);
  box-shadow: var(--shadow-strong);
  color: var(--color-text);
}

.confirm-dialog::backdrop {
  background: rgba(20, 20, 18, 0.4);
  backdrop-filter: blur(2px);
}

.confirm-dialog__inner {
  padding: var(--space-5);
}

.confirm-dialog__title {
  font-size: 1.1rem;
  margin-bottom: var(--space-2);
}

.confirm-dialog__message {
  color: var(--color-text-muted);
  font-size: 0.95rem;
  margin-bottom: var(--space-5);
}

.confirm-dialog__actions {
  display: flex;
  gap: var(--space-2);
  justify-content: flex-end;
}

.confirm-dialog--danger .confirm-dialog__title {
  color: var(--color-danger);
}
</style>
