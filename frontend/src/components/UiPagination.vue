<script setup lang="ts">
interface Props {
  page: number
  totalPages: number
}

const props = defineProps<Props>()
const emit = defineEmits<{ change: [page: number] }>()

const canPrev = () => props.page > 0
const canNext = () => props.page < props.totalPages - 1

function goto(page: number) {
  emit('change', page)
}
</script>

<template>
  <nav v-if="totalPages > 1" class="pagination" aria-label="Paginación">
    <button
      type="button"
      class="btn btn--ghost btn--sm"
      :disabled="!canPrev()"
      @click="goto(page - 1)"
    >
      Anterior
    </button>
    <span class="pagination__indicator subtle">
      Página {{ page + 1 }} de {{ totalPages }}
    </span>
    <button
      type="button"
      class="btn btn--ghost btn--sm"
      :disabled="!canNext()"
      @click="goto(page + 1)"
    >
      Siguiente
    </button>
  </nav>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  margin-top: var(--space-5);
}

.pagination__indicator {
  font-size: 0.85rem;
}
</style>
