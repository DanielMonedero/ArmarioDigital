<script setup lang="ts">
import { computed, ref } from 'vue'
import * as locationsApi from '@/api/locations'
import type { Location } from '@/types/api'
import { useLocations } from '@/composables/useLocations'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import EmptyState from '@/components/EmptyState.vue'
import ErrorState from '@/components/ErrorState.vue'
import LoadingState from '@/components/LoadingState.vue'
import { describeError } from '@/utils/errors'
import { useToastStore } from '@/stores/toast'

const toast = useToastStore()
const { locations, loading, error, refresh } = useLocations()

const newName = ref('')
const creating = ref(false)
const editingId = ref<number | null>(null)
const editingName = ref('')
const savingEdit = ref(false)
const confirmDeleteId = ref<number | null>(null)
const deleting = ref(false)

const confirmDeleteLocation = computed<Location | null>(() =>
  confirmDeleteId.value == null
    ? null
    : locations.value.find((l) => l.id === confirmDeleteId.value) ?? null
)

async function createLocation() {
  const name = newName.value.trim()
  if (!name) return
  creating.value = true
  try {
    await locationsApi.createLocation({ name })
    newName.value = ''
    toast.success('Ubicación creada')
    await refresh()
  } catch (err) {
    toast.error(describeError(err))
  } finally {
    creating.value = false
  }
}

function startEdit(loc: Location) {
  editingId.value = loc.id
  editingName.value = loc.name
}

function cancelEdit() {
  editingId.value = null
  editingName.value = ''
}

async function saveEdit() {
  if (editingId.value == null) return
  const name = editingName.value.trim()
  if (!name) return
  savingEdit.value = true
  try {
    await locationsApi.updateLocation(editingId.value, { name })
    toast.success('Ubicación renombrada')
    editingId.value = null
    editingName.value = ''
    await refresh()
  } catch (err) {
    toast.error(describeError(err))
  } finally {
    savingEdit.value = false
  }
}

function askDelete(id: number) {
  confirmDeleteId.value = id
}

async function confirmDelete() {
  const id = confirmDeleteId.value
  if (id == null) return
  deleting.value = true
  try {
    await locationsApi.deleteLocation(id)
    toast.success('Ubicación eliminada')
    confirmDeleteId.value = null
    await refresh()
  } catch (err) {
    toast.error(describeError(err))
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <section class="page">
    <header class="page__header">
      <div class="page__title">
        <h1>Ubicaciones</h1>
        <p class="page__subtitle">
          Cajas, perchas, bolsas de venta… cada prenda vive en una ubicación para encontrarla rápido.
        </p>
      </div>
    </header>

    <form class="locations__create card" @submit.prevent="createLocation">
      <label class="field__label" for="loc-name">Nueva ubicación</label>
      <div class="locations__create-row">
        <input
          id="loc-name"
          v-model="newName"
          class="input"
          placeholder="Armario grande, bolsa de venta 1…"
          maxlength="120"
          :disabled="creating"
        />
        <button type="submit" class="btn btn--primary" :disabled="creating || !newName.trim()">
          {{ creating ? 'Creando…' : 'Crear' }}
        </button>
      </div>
    </form>

    <LoadingState v-if="loading && locations.length === 0" label="Cargando ubicaciones…" />

    <ErrorState
      v-else-if="error"
      title="No se pudieron cargar las ubicaciones"
      :message="describeError(error)"
      @retry="refresh"
    />

    <EmptyState
      v-else-if="locations.length === 0"
      icon="box"
      label="Aún no tienes ubicaciones"
      description="Crea la primera arriba (por ejemplo «Armario grande») y asígnala a cada prenda al crearla o editarla."
    />

    <ul v-else class="locations__list">
      <li v-for="loc in locations" :key="loc.id" class="locations__item card">
        <div class="locations__item-main">
          <template v-if="editingId === loc.id">
            <input
              v-model="editingName"
              class="input"
              maxlength="120"
              :disabled="savingEdit"
              @keydown.enter.prevent="saveEdit"
              @keydown.esc.prevent="cancelEdit"
            />
          </template>
          <template v-else>
            <h3 class="locations__name">{{ loc.name }}</h3>
            <p class="locations__meta subtle">
              {{ loc.garmentCount }}
              {{ loc.garmentCount === 1 ? 'prenda' : 'prendas' }}
            </p>
          </template>
        </div>

        <div class="locations__actions">
          <template v-if="editingId === loc.id">
            <button
              type="button"
              class="btn btn--ghost btn--sm"
              :disabled="savingEdit"
              @click="cancelEdit"
            >
              Cancelar
            </button>
            <button
              type="button"
              class="btn btn--primary btn--sm"
              :disabled="savingEdit || !editingName.trim()"
              @click="saveEdit"
            >
              {{ savingEdit ? 'Guardando…' : 'Guardar' }}
            </button>
          </template>
          <template v-else>
            <button type="button" class="btn btn--ghost btn--sm" @click="startEdit(loc)">
              Renombrar
            </button>
            <button
              type="button"
              class="btn btn--danger btn--sm"
              :disabled="loc.garmentCount > 0"
              :title="loc.garmentCount > 0 ? 'Reasigna o elimina las prendas antes' : ''"
              @click="askDelete(loc.id)"
            >
              Eliminar
            </button>
          </template>
        </div>
      </li>
    </ul>

    <ConfirmDialog
      :open="confirmDeleteId != null"
      variant="danger"
      title="Eliminar ubicación"
      :message="confirmDeleteLocation ? `Vas a eliminar «${confirmDeleteLocation.name}». Esta acción no se puede deshacer.` : ''"
      confirm-label="Eliminar"
      :loading="deleting"
      @confirm="confirmDelete"
      @cancel="confirmDeleteId = null"
    />
  </section>
</template>

<style scoped>
.locations__create {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-4);
}

.locations__create-row {
  display: flex;
  gap: var(--space-2);
}

.locations__create-row .input {
  flex: 1;
}

.locations__list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.locations__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
}

.locations__item-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.locations__name {
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
  color: var(--color-text);
}

.locations__meta {
  font-size: 0.85rem;
}

.locations__actions {
  display: flex;
  gap: var(--space-2);
  flex-shrink: 0;
}
</style>
