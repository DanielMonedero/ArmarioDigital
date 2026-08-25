<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import GarmentCard from '@/components/GarmentCard.vue'
import LoadingState from '@/components/LoadingState.vue'
import ErrorState from '@/components/ErrorState.vue'
import EmptyState from '@/components/EmptyState.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import { useMaleta } from '@/composables/useMaleta'
import { describeError } from '@/utils/errors'
import { useToastStore } from '@/stores/toast'

const router = useRouter()
const toast = useToastStore()
const { items, loading, error, refresh, remove, clear } = useMaleta()

const confirmRemoveId = ref<number | null>(null)
const confirmClear = ref<boolean>(false)
const removing = ref(false)
const clearing = ref(false)

const targetRemove = computed(() =>
  confirmRemoveId.value == null
    ? null
    : items.value.find((i) => i.garmentId === confirmRemoveId.value) ?? null
)

async function removeOne() {
  const id = confirmRemoveId.value
  if (id == null) return
  removing.value = true
  try {
    await remove(id)
    toast.success('Prenda sacada de la maleta')
    confirmRemoveId.value = null
  } catch (err) {
    toast.error(describeError(err))
  } finally {
    removing.value = false
  }
}

async function clearAll() {
  clearing.value = true
  try {
    await clear()
    toast.success('Maleta vaciada')
    confirmClear.value = false
  } catch (err) {
    toast.error(describeError(err))
  } finally {
    clearing.value = false
  }
}

function openGarment(garmentId: number) {
  void router.push({
    name: 'garment-detail',
    params: { id: garmentId },
    query: { readonly: '1' }
  })
}
</script>

<template>
  <section class="page">
    <header class="page__header">
      <div class="page__title">
        <h1>Maleta</h1>
        <p class="page__subtitle">
          Prendas del armario que te quieres llevar de viaje. Las prendas
          siguen en tu armario, aquí solo es un recordatorio.
        </p>
      </div>
      <div class="page__actions" v-if="items.length > 0">
        <button type="button" class="btn btn--danger" @click="confirmClear = true">
          Vaciar maleta
        </button>
      </div>
    </header>

    <LoadingState v-if="loading && items.length === 0" label="Cargando maleta…" />

    <ErrorState
      v-else-if="error && items.length === 0"
      title="No se pudo cargar la maleta"
      :message="describeError(error)"
      @retry="refresh"
    />

    <EmptyState
      v-else-if="items.length === 0"
      icon="shirt"
      label="Tu maleta está vacía"
      description="Ve a una prenda del armario y pulsa «Añadir a maleta» para empezar."
    />

    <ul v-else class="maleta__grid">
      <li
        v-for="item in items"
        :key="item.id"
        class="maleta__item"
      >
        <div class="maleta__card" @click="openGarment(item.garmentId)">
          <GarmentCard :garment="item.garment" />
          <button
            type="button"
            class="maleta__remove"
            :aria-label="`Sacar ${item.garment.name} de la maleta`"
            @click.stop="confirmRemoveId = item.garmentId"
          >
            ×
          </button>
        </div>
      </li>
    </ul>

    <ConfirmDialog
      :open="confirmRemoveId != null"
      variant="danger"
      title="Sacar prenda de la maleta"
      :message="targetRemove ? `Vas a sacar «${targetRemove.garment.name}» de la maleta. Seguirá en tu armario.` : ''"
      confirm-label="Sacar"
      :loading="removing"
      @confirm="removeOne"
      @cancel="confirmRemoveId = null"
    />

    <ConfirmDialog
      :open="confirmClear"
      variant="danger"
      title="Vaciar maleta"
      :message="`Vas a sacar las ${items.length} prendas de la maleta. Todas seguirán en tu armario.`"
      confirm-label="Vaciar"
      :loading="clearing"
      @confirm="clearAll"
      @cancel="confirmClear = false"
    />
  </section>
</template>

<style scoped>
.maleta__grid {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: var(--space-3);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (min-width: 600px) {
  .maleta__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (min-width: 960px) {
  .maleta__grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

.maleta__item {
  display: block;
}

.maleta__card {
  position: relative;
  cursor: pointer;
  border-radius: var(--radius-lg);
  transition: transform var(--transition-base);
}

.maleta__card:hover {
  transform: translateY(-2px);
}

.maleta__remove {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 0;
  background: rgba(0, 0, 0, 0.55);
  color: white;
  font-size: 1.1rem;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  transition: background var(--transition-fast);
}

.maleta__remove:hover {
  background: rgba(0, 0, 0, 0.8);
}
</style>