<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as garmentApi from '@/api/garments'
import GarmentForm from '@/components/GarmentForm.vue'
import LoadingState from '@/components/LoadingState.vue'
import ErrorState from '@/components/ErrorState.vue'
import type { Garment, GarmentUpdateRequest } from '@/types/api'
import { describeError } from '@/utils/errors'
import { useToastStore } from '@/stores/toast'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()

const garment = ref<Garment | null>(null)
const loading = ref(false)
const fetchError = ref<unknown>(null)
const submitting = ref(false)
const submitError = ref<unknown>(null)

const id = Number(route.params.id)

async function load() {
  loading.value = true
  fetchError.value = null
  try {
    garment.value = await garmentApi.getGarment(id)
  } catch (err) {
    fetchError.value = err
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, load)
onMounted(load)

async function submit(payload: GarmentUpdateRequest) {
  submitting.value = true
  submitError.value = null
  try {
    const updated = await garmentApi.updateGarment(id, payload)
    toast.success('Cambios guardados')
    await router.push({ name: 'garment-detail', params: { id: updated.id } })
  } catch (err) {
    submitError.value = err
  } finally {
    submitting.value = false
  }
}

function cancel() {
  router.push({ name: 'garment-detail', params: { id } })
}
</script>

<template>
  <section class="page">
    <RouterLink :to="{ name: 'garment-detail', params: { id } }" class="back-link">← Volver</RouterLink>

    <header class="page__header">
      <div class="page__title">
        <h1>Editar prenda</h1>
      </div>
    </header>

    <LoadingState v-if="loading" label="Cargando datos…" />

    <ErrorState
      v-else-if="fetchError || !garment"
      :title="'No se pudo cargar la prenda'"
      :message="describeError(fetchError)"
      @retry="load"
    />

    <GarmentForm
      v-else
      :initial="{
        name: garment.name,
        description: garment.description ?? '',
        size: garment.size,
        category: garment.category,
        subcategory: garment.subcategory,
        color: garment.color,
        brand: garment.brand ?? '',
        condition: garment.condition,
        status: garment.status,
        season: garment.season,
        salePrice: garment.salePrice ?? null,
        purchasePrice: garment.purchasePrice ?? null,
        notes: garment.notes ?? '',
        locationId: garment.locationId,
        locationName: garment.locationName ?? ''
      }"
      submit-label="Guardar cambios"
      :loading="submitting"
      :error="submitError"
      @submit="submit"
      @cancel="cancel"
    />
  </section>
</template>

<style scoped>
.back-link {
  display: inline-flex;
  align-items: center;
  font-size: 0.88rem;
  color: var(--color-text-muted);
  margin-bottom: var(--space-3);
}
</style>
