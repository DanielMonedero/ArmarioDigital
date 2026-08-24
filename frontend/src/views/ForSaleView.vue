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
  useGarmentList({ status: 'FOR_SALE' })

const filters = computed({
  get: (): Filters => ({
    search: params.search ?? '',
    category: params.category ?? '',
    condition: params.condition ?? '',
    garmentSize: params.garmentSize ?? '',
    brand: params.brand ?? '',
    color: params.color ?? ''
  }),
  set: (value: Filters) => {
    params.search = value.search || undefined
    params.category = (value.category || undefined) as typeof params.category
    params.condition = (value.condition || undefined) as typeof params.condition
    params.garmentSize = value.garmentSize || undefined
    params.brand = value.brand || undefined
    params.color = value.color || undefined
  }
})
</script>

<template>
  <section class="page">
    <header class="page__header">
      <div class="page__title">
        <h1>En venta</h1>
        <p class="page__subtitle">
          Las prendas que tienes publicadas a la venta. Aquí verás la descripción generada
          automáticamente.
        </p>
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

    <LoadingState v-if="loading" label="Cargando prendas en venta…" />

    <ErrorState
      v-else-if="error"
      :title="'No se pudieron cargar las prendas en venta'"
      :message="describeError(error)"
      @retry="load(0)"
    />

    <template v-else-if="isEmpty">
      <EmptyState
        icon="tag"
        label="Aún no tienes nada en venta"
        description="Cuando pongas una prenda a la venta aparecerá aquí con su descripción generada."
      />
    </template>

    <template v-else>
      <GarmentGrid :garments="items" />
      <UiPagination :page="page" :total-pages="totalPages" @change="setPage" />
    </template>
  </section>
</template>
