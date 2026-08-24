<script setup lang="ts">
import { ref } from 'vue'
import { uploadImage } from '@/api/images'
import { ApiError } from '@/api/client'
import { describeError } from '@/utils/errors'
import { useToastStore } from '@/stores/toast'

interface Props {
  garmentId: number
}

const props = defineProps<Props>()
const emit = defineEmits<{ uploaded: [] }>()
const toast = useToastStore()

const inputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const dragOver = ref(false)
const previews = ref<{ id: string; url: string; name: string }[]>([])

function pick() {
  inputRef.value?.click()
}

function onChange(event: Event) {
  const files = (event.target as HTMLInputElement).files
  if (files) {
    void handleFiles(Array.from(files))
  }
  if (inputRef.value) inputRef.value.value = ''
}

function onDrop(event: DragEvent) {
  event.preventDefault()
  dragOver.value = false
  const files = event.dataTransfer?.files
  if (files && files.length) {
    void handleFiles(Array.from(files))
  }
}

async function handleFiles(files: File[]) {
  uploading.value = true
  let failures = 0
  for (const file of files) {
    const id = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
    const url = URL.createObjectURL(file)
    previews.value.push({ id, url, name: file.name })
    try {
      await uploadImage(props.garmentId, file)
    } catch (err) {
      failures += 1
      toast.error(describeError(err))
      previews.value = previews.value.filter((p) => p.id !== id)
    } finally {
      URL.revokeObjectURL(url)
    }
  }
  uploading.value = false
  previews.value = []
  if (failures === 0 && files.length > 0) {
    toast.success(
      files.length === 1 ? 'Imagen subida' : `${files.length} imágenes subidas`
    )
    emit('uploaded')
  }
}
</script>

<template>
  <div
    class="uploader"
    :class="{ 'uploader--over': dragOver, 'uploader--busy': uploading }"
    @dragover.prevent="dragOver = true"
    @dragleave="dragOver = false"
    @drop="onDrop"
  >
    <input
      ref="inputRef"
      class="sr-only"
      type="file"
      accept="image/jpeg,image/png,image/webp"
      multiple
      @change="onChange"
    />
    <button type="button" class="btn btn--ghost" :disabled="uploading" @click="pick">
      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" aria-hidden="true">
        <path
          d="M12 5v14M5 12h14"
          stroke="currentColor"
          stroke-width="1.5"
          stroke-linecap="round"
        />
      </svg>
      {{ uploading ? 'Subiendo…' : 'Añadir imágenes' }}
    </button>
    <p class="uploader__hint subtle">o arrastra archivos aquí · JPEG, PNG o WebP · máx. 10 MB</p>

    <div v-if="previews.length" class="uploader__previews">
      <div v-for="p in previews" :key="p.id" class="uploader__preview">
        <img :src="p.url" :alt="p.name" />
      </div>
    </div>

    <p v-if="uploading" class="uploader__status subtle">Subiendo imágenes…</p>
  </div>
</template>

<style scoped>
.uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-5);
  border: 1.5px dashed var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  text-align: center;
  transition: border-color var(--transition-fast), background var(--transition-fast);
}

.uploader--over {
  border-color: var(--color-primary);
  background: var(--color-surface-muted);
}

.uploader--busy {
  opacity: 0.85;
}

.uploader__hint {
  font-size: 0.82rem;
}

.uploader__previews {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  justify-content: center;
  margin-top: var(--space-2);
}

.uploader__preview {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-surface-muted);
}

.uploader__preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.uploader__status {
  font-size: 0.85rem;
}
</style>
