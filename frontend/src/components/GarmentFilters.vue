<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  CATEGORIES,
  CATEGORY_LABELS,
  COLOR_HEX,
  COLOR_LABELS,
  COLORS,
  CONDITION_LABELS,
  GARMENT_CONDITIONS,
  SEASON_LABELS,
  SEASONS,
  SUBCATEGORY_LABELS,
  subcategoriesFor,
  type Category,
  type Color,
  type GarmentCondition,
  type Season,
  type Subcategory
} from '@/types/api'
import type { Filters } from '@/composables/filters'
import { emptyFilters } from '@/composables/filters'

interface Props {
  modelValue: Filters
  resultCount?: number
  totalCount?: number
}

const props = withDefaults(defineProps<Props>(), {
  resultCount: 0,
  totalCount: 0
})

const emit = defineEmits<{
  'update:modelValue': [value: Filters]
  reset: []
}>()

const search = ref(props.modelValue.search)
const panelOpen = ref(false)

const local = computed<Filters>({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

function update<K extends keyof Filters>(key: K, value: Filters[K]) {
  const next: Filters = { ...local.value, [key]: value }
  // Reset subcategory when category changes so we don't keep an orphan filter.
  if (key === 'category') {
    next.subcategory = ''
  }
  emit('update:modelValue', next)
}

function commitSearch() {
  update('search', search.value.trim())
}

function reset() {
  search.value = ''
  emit('update:modelValue', emptyFilters())
  emit('reset')
}

const availableSubcategories = computed<readonly Subcategory[]>(() => {
  const cat = local.value.category as Category | ''
  return cat ? subcategoriesFor(cat) : []
})

const activeCount = computed(() => {
  const v = local.value
  return [
    v.search,
    v.category,
    v.subcategory,
    v.color,
    v.season,
    v.condition,
    v.garmentSize,
    v.brand
  ].filter(Boolean).length
})
</script>

<template>
  <section class="filters">
    <form class="filters__row" role="search" @submit.prevent="commitSearch">
      <div class="filters__search">
        <svg
          class="filters__search-icon"
          viewBox="0 0 24 24"
          width="16"
          height="16"
          fill="none"
          aria-hidden="true"
        >
          <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.5" />
          <path d="M20 20l-3.5-3.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
        </svg>
        <input
          v-model="search"
          class="input filters__search-input"
          type="search"
          placeholder="Buscar por nombre, marca o descripción"
          aria-label="Buscar"
          @keydown.enter.prevent="commitSearch"
          @blur="commitSearch"
        />
      </div>

      <button
        type="button"
        class="btn btn--ghost btn--sm filters__toggle"
        :aria-expanded="panelOpen"
        @click="panelOpen = !panelOpen"
      >
        Filtros<span v-if="activeCount" class="filters__count">{{ activeCount }}</span>
      </button>

      <button
        v-if="activeCount"
        type="button"
        class="btn btn--ghost btn--sm"
        @click="reset"
      >
        Limpiar
      </button>

      <p class="filters__results subtle">
        {{ resultCount }} resultado<span v-if="resultCount !== 1">s</span>
        <span v-if="totalCount > resultCount"> de {{ totalCount }}</span>
      </p>
    </form>

    <div v-if="panelOpen" class="filters__panel">
      <div class="field">
        <label class="field__label" for="filter-category">Categoría</label>
        <select
          id="filter-category"
          class="select"
          :value="local.category"
          @change="update('category', ($event.target as HTMLSelectElement).value as Category | '')"
        >
          <option value="">Todas</option>
          <option v-for="c in CATEGORIES" :key="c" :value="c">{{ CATEGORY_LABELS[c] }}</option>
        </select>
      </div>

      <div class="field">
        <label class="field__label" for="filter-subcategory">Subcategoría</label>
        <select
          id="filter-subcategory"
          class="select"
          :value="local.subcategory"
          :disabled="!local.category"
          @change="update('subcategory', ($event.target as HTMLSelectElement).value as Subcategory | '')"
        >
          <option value="">{{ local.category ? 'Cualquiera' : 'Elige categoría' }}</option>
          <option v-for="s in availableSubcategories" :key="s" :value="s">
            {{ SUBCATEGORY_LABELS[s] }}
          </option>
        </select>
      </div>

      <div class="field">
        <label class="field__label" for="filter-color">Color</label>
        <select
          id="filter-color"
          class="select"
          :value="local.color"
          @change="update('color', ($event.target as HTMLSelectElement).value as Color | '')"
        >
          <option value="">Cualquiera</option>
          <option v-for="c in COLORS" :key="c" :value="c">
            {{ COLOR_LABELS[c] }}
          </option>
        </select>
        <span v-if="local.color" class="color-swatch" aria-hidden="true">
          <span class="color-swatch__dot" :style="{ background: COLOR_HEX[local.color as Color] }" />
          <span class="color-swatch__label">{{ COLOR_LABELS[local.color as Color] }}</span>
        </span>
      </div>

      <div class="field">
        <label class="field__label" for="filter-season">Temporada</label>
        <select
          id="filter-season"
          class="select"
          :value="local.season"
          @change="update('season', ($event.target as HTMLSelectElement).value as Season | '')"
        >
          <option value="">Cualquiera</option>
          <option v-for="s in SEASONS" :key="s" :value="s">{{ SEASON_LABELS[s] }}</option>
        </select>
      </div>

      <div class="field">
        <label class="field__label" for="filter-condition">Estado</label>
        <select
          id="filter-condition"
          class="select"
          :value="local.condition"
          @change="update('condition', ($event.target as HTMLSelectElement).value as GarmentCondition | '')"
        >
          <option value="">Cualquiera</option>
          <option v-for="c in GARMENT_CONDITIONS" :key="c" :value="c">{{ CONDITION_LABELS[c] }}</option>
        </select>
      </div>

      <div class="field">
        <label class="field__label" for="filter-size">Talla</label>
        <input
          id="filter-size"
          class="input"
          :value="local.garmentSize"
          maxlength="40"
          placeholder="M, 38, 42 EU…"
          @input="update('garmentSize', ($event.target as HTMLInputElement).value)"
        />
      </div>

      <div class="field">
        <label class="field__label" for="filter-brand">Marca</label>
        <input
          id="filter-brand"
          class="input"
          :value="local.brand"
          maxlength="120"
          @input="update('brand', ($event.target as HTMLInputElement).value)"
        />
      </div>
    </div>
  </section>
</template>

<style scoped>
.filters {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.filters__row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.filters__search {
  position: relative;
  flex: 1 1 220px;
  min-width: 200px;
}

.filters__search-icon {
  position: absolute;
  top: 50%;
  left: 12px;
  transform: translateY(-50%);
  color: var(--color-text-subtle);
  pointer-events: none;
}

.filters__search-input {
  padding-left: 34px;
}

.filters__toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.filters__count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--color-primary);
  color: var(--color-primary-text);
  font-size: 0.7rem;
}

.filters__results {
  margin-left: auto;
}

.filters__panel {
  display: grid;
  gap: var(--space-3);
  grid-template-columns: 1fr;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-4);
}

@media (min-width: 600px) {
  .filters__panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (min-width: 900px) {
  .filters__panel {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

.color-swatch {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  font-size: 0.82rem;
  color: var(--color-text-muted);
}

.color-swatch__dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 1px solid var(--color-border);
  display: inline-block;
}
</style>
