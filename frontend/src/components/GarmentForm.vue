<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import {
  CATEGORIES,
  CATEGORY_LABELS,
  COLORS,
  COLOR_HEX,
  COLOR_LABELS,
  CONDITION_LABELS,
  GARMENT_CONDITIONS,
  GARMENT_STATUSES,
  SEASONS,
  SEASON_LABELS,
  STATUS_LABELS,
  SUBCATEGORY_LABELS,
  subcategoriesFor,
  type Category,
  type Color,
  type GarmentCondition,
  type GarmentCreateRequest,
  type GarmentStatus,
  type GarmentUpdateRequest,
  type Season,
  type Subcategory
} from '@/types/api'
import { useLocations } from '@/composables/useLocations'
import { fieldErrorFor, humanizeFieldName } from '@/utils/errors'

type LocationSelection = 'none' | 'new' | number

interface FormState {
  name: string
  description: string
  size: string
  category: Category
  subcategory: Subcategory | null
  color: Color | null
  brand: string
  condition: GarmentCondition
  status: GarmentStatus
  season: Season
  salePrice: string | number | null
  purchasePrice: string | number | null
  notes: string
  locationSelection: LocationSelection
  newLocationName: string
}

interface Props {
  initial?: Partial<GarmentUpdateRequest>
  submitLabel: string
  loading?: boolean
  error?: unknown
}

const props = withDefaults(defineProps<Props>(), {
  initial: () => ({}),
  loading: false,
  error: undefined
})

const emit = defineEmits<{
  submit: [payload: GarmentCreateRequest]
  cancel: []
}>()

function emptyState(): FormState {
  return {
    name: '',
    description: '',
    size: '',
    category: 'TOP',
    subcategory: null,
    color: null,
    brand: '',
    condition: 'GOOD',
    status: 'WARDROBE',
    season: 'SUMMER',
    salePrice: '',
    purchasePrice: '',
    notes: '',
    locationSelection: 'none',
    newLocationName: ''
  }
}

const form = reactive<FormState>(emptyState())

const { locations } = useLocations()

function hydrate() {
  const initialLocationId = props.initial.locationId ?? null
  const initialLocationName = props.initial.locationName ?? ''
  Object.assign(form, emptyState(), {
    name: props.initial.name ?? '',
    description: props.initial.description ?? '',
    size: props.initial.size ?? '',
    category: props.initial.category ?? 'TOP',
    subcategory: (props.initial.subcategory ?? null) as Subcategory | null,
    color: (props.initial.color ?? null) as Color | null,
    brand: props.initial.brand ?? '',
    condition: props.initial.condition ?? 'GOOD',
    status: props.initial.status ?? 'WARDROBE',
    season: props.initial.season ?? 'SUMMER',
    salePrice: props.initial.salePrice != null ? String(props.initial.salePrice) : '',
    purchasePrice: props.initial.purchasePrice != null ? String(props.initial.purchasePrice) : '',
    notes: props.initial.notes ?? '',
    locationSelection: initialLocationId != null
      ? initialLocationId
      : (initialLocationName ? 'new' : 'none'),
    newLocationName: initialLocationId != null ? '' : initialLocationName
  })
}

watch(() => props.initial, hydrate, { immediate: true, deep: true })

// Reset subcategory when category changes to an incompatible parent.
watch(
  () => form.category,
  (newCat) => {
    if (form.subcategory && !subcategoriesFor(newCat).includes(form.subcategory)) {
      form.subcategory = null
    }
  }
)

const errorByField = (field: string): string | undefined => {
  return fieldErrorFor(props.error, field)
}

const formErrorMessage = computed(() => {
  const err = props.error
  if (!err) return null
  if (err instanceof Error) return err.message
  return null
})

const statusOptions = computed(() =>
  GARMENT_STATUSES.filter((s) => s !== 'SOLD')
)

const availableSubcategories = computed<readonly Subcategory[]>(() =>
  subcategoriesFor(form.category)
)

function toNumberOrNull(value: unknown): number | null {
  if (value === null || value === undefined) return null
  const str = String(value).trim()
  if (!str) return null
  const n = Number(str.replace(',', '.'))
  if (!Number.isFinite(n)) return null
  return n
}

function onSubmit(event: Event) {
  event.preventDefault()
  const locationId = typeof form.locationSelection === 'number' ? form.locationSelection : null
  const locationName =
    form.locationSelection === 'new' ? form.newLocationName.trim() || null : null
  const payload: GarmentCreateRequest = {
    name: form.name.trim(),
    description: form.description.trim() || null,
    size: form.size.trim(),
    category: form.category,
    subcategory: form.subcategory,
    color: form.color,
    brand: form.brand.trim() || null,
    condition: form.condition,
    status: form.status,
    season: form.season,
    salePrice: toNumberOrNull(form.salePrice),
    purchasePrice: toNumberOrNull(form.purchasePrice),
    notes: form.notes.trim() || null,
    locationId,
    locationName
  }
  emit('submit', payload)
}
</script>

<template>
  <form class="garment-form" novalidate @submit="onSubmit">
    <div class="garment-form__grid">
      <div class="field">
        <label class="field__label" for="g-name">Nombre</label>
        <input
          id="g-name"
          v-model="form.name"
          class="input"
          :class="{ 'input--error': errorByField('name') }"
          maxlength="160"
          required
        />
        <span v-if="errorByField('name')" class="field__error">{{ errorByField('name') }}</span>
      </div>

      <div class="field">
        <label class="field__label" for="g-size">Talla</label>
        <input
          id="g-size"
          v-model="form.size"
          class="input"
          :class="{ 'input--error': errorByField('size') }"
          placeholder="M, 38, 42 EU…"
          maxlength="40"
          required
        />
        <span v-if="errorByField('size')" class="field__error">{{ errorByField('size') }}</span>
      </div>

      <div class="field">
        <label class="field__label" for="g-category">Categoría</label>
        <select id="g-category" v-model="form.category" class="select">
          <option v-for="c in CATEGORIES" :key="c" :value="c">{{ CATEGORY_LABELS[c] }}</option>
        </select>
      </div>

      <div class="field">
        <label class="field__label" for="g-subcategory">Subcategoría</label>
        <select
          id="g-subcategory"
          v-model="form.subcategory"
          class="select"
          :class="{ 'input--error': errorByField('subcategory') }"
        >
          <option :value="null">Sin subcategoría</option>
          <option v-for="s in availableSubcategories" :key="s" :value="s">
            {{ SUBCATEGORY_LABELS[s] }}
          </option>
        </select>
        <span v-if="errorByField('subcategory')" class="field__error">{{ errorByField('subcategory') }}</span>
      </div>

      <div class="field">
        <label class="field__label">Color</label>
        <div class="color-picker" role="radiogroup" aria-label="Color">
          <button
            v-for="c in COLORS"
            :key="c"
            type="button"
            role="radio"
            :aria-checked="form.color === c"
            :title="COLOR_LABELS[c]"
            class="color-picker__swatch"
            :class="{ 'color-picker__swatch--active': form.color === c }"
            :style="{ background: COLOR_HEX[c] }"
            @click="form.color = form.color === c ? null : c"
          />
        </div>
        <span class="color-picker__label subtle">
          {{ form.color ? COLOR_LABELS[form.color] : 'Sin color' }}
        </span>
      </div>

      <div class="field">
        <label class="field__label">Temporada</label>
        <div class="season-toggle" role="radiogroup" aria-label="Temporada">
          <button
            v-for="s in SEASONS"
            :id="`g-season-${s}`"
            :key="s"
            type="button"
            role="radio"
            :aria-checked="form.season === s"
            class="season-toggle__btn"
            :class="{ 'season-toggle__btn--active': form.season === s }"
            @click="form.season = s"
          >
            {{ SEASON_LABELS[s] }}
          </button>
        </div>
        <span v-if="errorByField('season')" class="field__error">{{ errorByField('season') }}</span>
      </div>

      <div class="field">
        <label class="field__label" for="g-condition">Estado de la prenda</label>
        <select id="g-condition" v-model="form.condition" class="select">
          <option v-for="c in GARMENT_CONDITIONS" :key="c" :value="c">{{ CONDITION_LABELS[c] }}</option>
        </select>
      </div>

      <div class="field">
        <label class="field__label" for="g-status">Situación</label>
        <select
          id="g-status"
          v-model="form.status"
          class="select"
          :class="{ 'input--error': errorByField('status') }"
        >
          <option v-for="s in statusOptions" :key="s" :value="s">{{ STATUS_LABELS[s] }}</option>
        </select>
        <span v-if="errorByField('status')" class="field__error">{{ errorByField('status') }}</span>
      </div>

      <div class="field">
        <label class="field__label" for="g-brand">Marca</label>
        <input
          id="g-brand"
          v-model="form.brand"
          class="input"
          :class="{ 'input--error': errorByField('brand') }"
          maxlength="120"
        />
        <span v-if="errorByField('brand')" class="field__error">{{ errorByField('brand') }}</span>
      </div>

      <div class="field">
        <label class="field__label" for="g-location">Ubicación</label>
        <select
          id="g-location"
          v-model="form.locationSelection"
          class="select"
        >
          <option value="none">Sin ubicación</option>
          <option value="new">+ Crear nueva ubicación…</option>
          <option v-for="loc in locations" :key="loc.id" :value="loc.id">
            {{ loc.name }} ({{ loc.garmentCount }})
          </option>
        </select>
        <input
          v-if="form.locationSelection === 'new'"
          v-model="form.newLocationName"
          class="input"
          maxlength="120"
          placeholder="Nombre de la nueva ubicación"
        />
        <span v-if="errorByField('locationId') || errorByField('locationName')" class="field__error">
          {{ errorByField('locationId') || errorByField('locationName') }}
        </span>
      </div>

      <div class="field">
        <label class="field__label" for="g-sale">Precio de venta (€)</label>
        <input
          id="g-sale"
          v-model="form.salePrice"
          class="input"
          :class="{ 'input--error': errorByField('salePrice') }"
          inputmode="decimal"
          type="number"
          step="0.01"
          min="0"
          placeholder="0,00"
        />
        <span v-if="errorByField('salePrice')" class="field__error">{{ errorByField('salePrice') }}</span>
        <span v-else class="field__hint">Obligatorio si la pones en venta</span>
      </div>

      <div class="field">
        <label class="field__label" for="g-purchase">Precio de compra (€)</label>
        <input
          id="g-purchase"
          v-model="form.purchasePrice"
          class="input"
          :class="{ 'input--error': errorByField('purchasePrice') }"
          inputmode="decimal"
          type="number"
          step="0.01"
          min="0"
          placeholder="0,00"
        />
        <span v-if="errorByField('purchasePrice')" class="field__error">{{ errorByField('purchasePrice') }}</span>
      </div>

      <div class="field garment-form__full">
        <label class="field__label" for="g-description">Descripción</label>
        <textarea
          id="g-description"
          v-model="form.description"
          class="textarea"
          rows="3"
          maxlength="4000"
          placeholder="Opcional. Si la dejas vacía, se generará automáticamente al poner la prenda en venta."
        ></textarea>
      </div>

      <div class="field garment-form__full">
        <label class="field__label" for="g-notes">Notas personales</label>
        <textarea
          id="g-notes"
          v-model="form.notes"
          class="textarea"
          rows="3"
          maxlength="4000"
          placeholder="Detalles internos, ubicación, recordatorios…"
        ></textarea>
      </div>
    </div>

    <div v-if="formErrorMessage" class="garment-form__error">
      {{ formErrorMessage }}
    </div>

    <div class="garment-form__actions">
      <button type="button" class="btn btn--ghost" @click="emit('cancel')">Cancelar</button>
      <button type="submit" class="btn btn--primary" :disabled="loading">
        {{ loading ? 'Guardando…' : submitLabel }}
      </button>
    </div>

    <span class="sr-only" aria-live="polite">{{ humanizeFieldName('form') }}</span>
  </form>
</template>

<style scoped>
.garment-form__grid {
  display: grid;
  gap: var(--space-4);
  grid-template-columns: 1fr;
}

@media (min-width: 600px) {
  .garment-form__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.garment-form__full {
  grid-column: 1 / -1;
}

.garment-form__error {
  margin-top: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--color-danger-soft);
  color: var(--color-danger);
  border-radius: var(--radius-md);
  font-size: 0.92rem;
}

.garment-form__actions {
  margin-top: var(--space-5);
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
}

.color-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 4px 0;
}

.color-picker__swatch {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 2px solid var(--color-border);
  cursor: pointer;
  padding: 0;
  transition: transform var(--transition-base), border-color var(--transition-base);
}

.color-picker__swatch:hover {
  transform: scale(1.08);
}

.color-picker__swatch--active {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px var(--color-primary-soft);
}

.color-picker__label {
  font-size: 0.85rem;
  margin-top: 4px;
}

.season-toggle {
  display: inline-flex;
  gap: 4px;
  background: var(--color-surface-muted);
  padding: 4px;
  border-radius: var(--radius-md);
  width: fit-content;
}

.season-toggle__btn {
  border: 0;
  background: transparent;
  padding: 6px 14px;
  border-radius: calc(var(--radius-md) - 4px);
  cursor: pointer;
  font-size: 0.92rem;
  color: var(--color-text-muted);
  transition: background var(--transition-base), color var(--transition-base);
}

.season-toggle__btn--active {
  background: var(--color-surface);
  color: var(--color-text);
  box-shadow: var(--shadow-soft);
}
</style>
