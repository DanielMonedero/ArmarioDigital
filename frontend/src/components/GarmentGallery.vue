<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { GarmentImage } from '@/types/api'
import { imageUrl } from '@/api/client'

interface Props {
  images: GarmentImage[]
  editable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  editable: false
})

const emit = defineEmits<{
  delete: [imageId: number]
  reorder: [orderedIds: number[]]
}>()

const sorted = computed(() =>
  [...props.images].sort((a, b) => {
    if (a.sortOrder !== b.sortOrder) return a.sortOrder - b.sortOrder
    return a.id - b.id
  })
)

const selectedIndex = ref(0)

watch(
  sorted,
  () => {
    if (selectedIndex.value >= sorted.value.length) {
      selectedIndex.value = 0
    }
  },
  { immediate: true }
)

const selected = computed(() => sorted.value[selectedIndex.value] ?? null)

function selectIndex(i: number) {
  selectedIndex.value = i
}

function moveLeft() {
  if (selectedIndex.value > 0) selectedIndex.value -= 1
}

function moveRight() {
  if (selectedIndex.value < sorted.value.length - 1) selectedIndex.value += 1
}

function requestDelete(imageId: number) {
  emit('delete', imageId)
}

const dragIndex = ref<number | null>(null)
const overIndex = ref<number | null>(null)

function onDragStart(index: number, event: DragEvent) {
  if (!props.editable) return
  dragIndex.value = index
  event.dataTransfer?.setData('text/plain', String(index))
  if (event.dataTransfer) event.dataTransfer.effectAllowed = 'move'
}

function onDragOver(index: number, event: DragEvent) {
  if (!props.editable) return
  event.preventDefault()
  overIndex.value = index
  if (event.dataTransfer) event.dataTransfer.dropEffect = 'move'
}

function onDrop(index: number, event: DragEvent) {
  if (!props.editable) return
  event.preventDefault()
  const from = dragIndex.value
  dragIndex.value = null
  overIndex.value = null
  if (from === null || from === index) return
  const reordered = sorted.value.slice()
  const [moved] = reordered.splice(from, 1)
  reordered.splice(index, 0, moved)
  emit('reorder', reordered.map((i) => i.id))
}
</script>

<template>
  <div class="gallery" :class="{ 'gallery--empty': sorted.length === 0 }">
    <div class="gallery__main">
      <template v-if="selected">
        <img
          :src="imageUrl(images[0]?.garmentId ?? 0, selected.id)"
          :alt="selected.originalFilename"
        />
        <button
          v-if="editable"
          type="button"
          class="gallery__delete"
          @click="requestDelete(selected.id)"
          aria-label="Eliminar imagen"
        >
          Eliminar
        </button>
        <div v-if="sorted.length > 1" class="gallery__nav">
          <button
            type="button"
            class="gallery__nav-btn"
            :disabled="selectedIndex === 0"
            @click="moveLeft"
            aria-label="Imagen anterior"
          >
            ‹
          </button>
          <span class="gallery__counter">
            {{ selectedIndex + 1 }} / {{ sorted.length }}
          </span>
          <button
            type="button"
            class="gallery__nav-btn"
            :disabled="selectedIndex >= sorted.length - 1"
            @click="moveRight"
            aria-label="Imagen siguiente"
          >
            ›
          </button>
        </div>
      </template>
      <div v-else class="gallery__placeholder">
        <p>Aún no hay fotografías para esta prenda.</p>
      </div>
    </div>

    <div v-if="sorted.length > 1" class="gallery__thumbs" role="list">
      <button
        v-for="(image, index) in sorted"
        :key="image.id"
        type="button"
        class="gallery__thumb"
        :class="{
          'gallery__thumb--active': index === selectedIndex,
          'gallery__thumb--over': index === overIndex
        }"
        :draggable="editable"
        :aria-label="`Imagen ${index + 1}`"
        :aria-current="index === selectedIndex"
        @click="selectIndex(index)"
        @dragstart="onDragStart(index, $event)"
        @dragover="onDragOver(index, $event)"
        @drop="onDrop(index, $event)"
        @dragend="overIndex = null"
      >
        <img
          :src="imageUrl(images[0]?.garmentId ?? 0, image.id)"
          :alt="image.originalFilename"
        />
        <span v-if="index === 0" class="gallery__thumb-tag">Principal</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.gallery {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.gallery__main {
  position: relative;
  background: var(--color-surface-muted);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  aspect-ratio: 3 / 4;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gallery__main img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.gallery__placeholder {
  color: var(--color-text-muted);
  font-size: 0.95rem;
}

.gallery__delete {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.95);
  color: var(--color-danger);
  border: 1px solid var(--color-border);
  padding: 6px 12px;
  font-size: 0.82rem;
  border-radius: var(--radius-pill);
}

.gallery__delete:hover {
  background: var(--color-danger-soft);
}

.gallery__nav {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  background: rgba(255, 255, 255, 0.9);
  padding: 4px 8px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--color-border);
}

.gallery__nav-btn {
  background: transparent;
  border: none;
  font-size: 1.1rem;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  color: var(--color-text);
}

.gallery__nav-btn:hover:not(:disabled) {
  background: var(--color-surface-muted);
}

.gallery__counter {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  min-width: 42px;
  text-align: center;
}

.gallery__thumbs {
  display: flex;
  gap: var(--space-2);
  overflow-x: auto;
  padding-bottom: 4px;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
}

.gallery__thumb {
  position: relative;
  flex: 0 0 84px;
  width: 84px;
  height: 100px;
  padding: 0;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-surface-muted);
  cursor: pointer;
  scroll-snap-align: start;
  transition: border-color var(--transition-fast);
}

.gallery__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.gallery__thumb--active {
  border-color: var(--color-primary);
}

.gallery__thumb--over {
  border-color: var(--color-accent);
}

.gallery__thumb-tag {
  position: absolute;
  bottom: 4px;
  left: 4px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--color-text);
  font-size: 0.65rem;
  padding: 2px 6px;
  border-radius: var(--radius-pill);
}

.gallery--empty .gallery__main {
  aspect-ratio: 4 / 3;
}
</style>
