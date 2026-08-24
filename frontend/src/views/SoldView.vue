<script setup lang="ts">
import { computed } from 'vue'
import { useGarmentList } from '@/composables/useGarmentList'
import type { Filters } from '@/composables/filters'
import GarmentGrid from '@/components/GarmentGrid.vue'
import GarmentFilters from '@/components/GarmentFilters.vue'
import EmptyState from '@/components/EmptyState.vue'
import ErrorState from '@/components/ErrorState.vue'
import LoadingState from '@/components/LoadingState.vue'
import UiPagination from '@/components/UiPagination.vue'
import { describeError } from '@/utils/errors'

const { items, page, totalPages, totalElements, loading, error, params, isEmpty, load, setPage } =
  useGarmentList({ status: 'SOLD' })

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
        <h1>Vendidas</h1>
        <p class="page__subtitle">Histórico de prendas que ya has vendido.</p>
      </div>
      <div class="page__actions">
        <RouterLink :to="{ name: 'garment-new' }" class="btn btn--primary">
          Añadir prenda
        </RouterLink>
      </div>
    </header>

    <GarmentFilters
      v-model="filters"
      :result-count="items.length"
      :total-count="totalElements"
    />

    <LoadingState v-if="loading" label="Cargando vendidas…" />

    <ErrorState
      v-else-if="error"
      :title="'No se pudieron cargar las prendas vendidas'"
      :message="describeError(error)"
      @retry="load(0)"
    />

    <template v-else-if="isEmpty">
      <EmptyState
        icon="check"
        label="Aún no has vendido nada"
        description="Las prendas que marques como vendidas aparecerán aquí."
      />
    </template>

    <template v-else>
      <GarmentGrid :garments="items" />
      <UiPagination :page="page" :total-pages="totalPages" @change="setPage" />
    </template>
  </section>
</template>
