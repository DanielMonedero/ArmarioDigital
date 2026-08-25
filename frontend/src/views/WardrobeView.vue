<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useGarmentList } from '@/composables/useGarmentList'
import type { Filters } from '@/composables/filters'
import GarmentGrid from '@/components/GarmentGrid.vue'
import GarmentFilters from '@/components/GarmentFilters.vue'
import EmptyState from '@/components/EmptyState.vue'
import ErrorState from '@/components/ErrorState.vue'
import LoadingState from '@/components/LoadingState.vue'
import UiPagination from '@/components/UiPagination.vue'
import WardrobeStatsPanel from '@/components/WardrobeStatsPanel.vue'
import { fetchWardrobeStats } from '@/api/garments'
import type { WardrobeStats } from '@/types/api'
import { describeError } from '@/utils/errors'

const { items, page, totalPages, totalElements, loading, error, params, isEmpty, load, setPage } =
  useGarmentList({ status: 'WARDROBE' })

const stats = ref<WardrobeStats | null>(null)
const statsLoading = ref(false)

async function refreshStats() {
  statsLoading.value = true
  try {
    stats.value = await fetchWardrobeStats()
  } catch {
    // Non-fatal: keep the page functional even if stats fail to load.
    stats.value = null
  } finally {
    statsLoading.value = false
  }
}

onMounted(refreshStats)
// Refresh after the user creates / edits / deletes garments.
watch(items, refreshStats)

const filters = computed({
  get: (): Filters => ({
    search: params.search ?? '',
    category: params.category ?? '',
    subcategory: params.subcategory ?? '',
    color: params.color ?? '',
    season: params.season ?? '',
    condition: params.condition ?? '',
    garmentSize: params.garmentSize ?? '',
    brand: params.brand ?? '',
    locationId: params.locationId != null ? String(params.locationId) : ''
  }),
  set: (value: Filters) => {
    params.search = value.search || undefined
    params.category = (value.category || undefined) as typeof params.category
    params.subcategory = (value.subcategory || undefined) as typeof params.subcategory
    params.color = (value.color || undefined) as typeof params.color
    params.season = (value.season || undefined) as typeof params.season
    params.condition = (value.condition || undefined) as typeof params.condition
    params.garmentSize = value.garmentSize || undefined
    params.brand = value.brand || undefined
    params.locationId = value.locationId ? Number(value.locationId) : undefined
  }
})
</script>

<template>
  <section class="page">
    <header class="page__header">
      <div class="page__title">
        <h1>Mi armario</h1>
        <p class="page__subtitle">Tus prendas guardadas y listas para usar.</p>
      </div>
      <div class="page__actions">
        <RouterLink :to="{ name: 'garment-new' }" class="btn btn--primary">
          Añadir prenda
        </RouterLink>
      </div>
    </header>

    <WardrobeStatsPanel :stats="stats" :loading="statsLoading" />

    <GarmentFilters
      v-model="filters"
      :result-count="items.length"
      :total-count="totalElements"
    />

    <LoadingState v-if="loading" label="Cargando prendas…" />

    <ErrorState
      v-else-if="error"
      :title="'No se pudieron cargar las prendas'"
      :message="describeError(error)"
      @retry="load(0)"
    />

    <template v-else-if="isEmpty">
      <EmptyState
        icon="shirt"
        label="Tu armario está vacío"
        description="Empieza añadiendo tu primera prenda para tener un control claro de lo que tienes."
      >
        <template #action>
          <RouterLink :to="{ name: 'garment-new' }" class="btn btn--primary">
            Añadir la primera
          </RouterLink>
        </template>
      </EmptyState>
    </template>

    <template v-else>
      <GarmentGrid :garments="items" />
      <UiPagination :page="page" :total-pages="totalPages" @change="setPage" />
    </template>
  </section>
</template>
