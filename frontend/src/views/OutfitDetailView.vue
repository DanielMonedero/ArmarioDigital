<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as outfitsApi from '@/api/outfits'
import GarmentCard from '@/components/GarmentCard.vue'
import LoadingState from '@/components/LoadingState.vue'
import ErrorState from '@/components/ErrorState.vue'
import type { SavedOutfit } from '@/types/api'
import { SEASON_LABELS } from '@/types/api'
import { describeError } from '@/utils/errors'

const route = useRoute()
const id = computed(() => Number(route.params.id))

const outfit = ref<SavedOutfit | null>(null)
const loading = ref(false)
const error = ref<unknown>(null)

async function load() {
  loading.value = true
  error.value = null
  try {
    outfit.value = await outfitsApi.getSavedOutfit(id.value)
  } catch (err) {
    error.value = err
    outfit.value = null
  } finally {
    loading.value = false
  }
}

watch(id, load, { immediate: true })
</script>

<template>
  <section class="page">
    <RouterLink :to="{ name: 'outfits' }" class="back-link">← Conjuntos</RouterLink>

    <LoadingState v-if="loading" label="Cargando conjunto…" />

    <ErrorState
      v-else-if="error"
      title="No se pudo cargar el conjunto"
      :message="describeError(error)"
      @retry="load"
    />

    <template v-else-if="outfit">
      <header class="page__header">
        <div class="page__title">
          <h1>{{ outfit.name }}</h1>
          <p class="page__subtitle">
            {{ outfit.items.length }} prendas · {{ SEASON_LABELS[outfit.season] }}
            <span v-if="outfit.includeOuterwear"> · con capa exterior</span>
            <span v-if="outfit.includeAccessories"> · con accesorios</span>
          </p>
        </div>
      </header>

      <div class="outfit__grid">
        <template v-for="item in outfit.items" :key="item.garmentId ?? item.name">
          <GarmentCard v-if="item.garment" :garment="item.garment" />
          <div v-else class="outfit__deleted card">
            <strong>{{ item.category }} · {{ item.name }}</strong>
            <p class="subtle">Esta prenda fue eliminada del armario.</p>
          </div>
        </template>
      </div>
    </template>
  </section>
</template>

<style scoped>
.outfit__grid {
  display: grid;
  gap: var(--space-3);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (min-width: 600px) {
  .outfit__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (min-width: 960px) {
  .outfit__grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

.outfit__deleted {
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  color: var(--color-text-muted);
  font-size: 0.92rem;
}

.outfit__deleted strong {
  color: var(--color-text);
}
</style>
