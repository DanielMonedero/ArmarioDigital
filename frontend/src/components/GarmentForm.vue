<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import {
  CATEGORIES,
  CATEGORY_LABELS,
  CONDITION_LABELS,
  GARMENT_CONDITIONS,
  GARMENT_STATUSES,
  STATUS_LABELS,
  type Category,
  type GarmentCondition,
  type GarmentCreateRequest,
  type GarmentStatus,
  type GarmentUpdateRequest
} from '@/types/api'
import { fieldErrorFor, humanizeFieldName } from '@/utils/errors'

interface FormState {
  name: string
  description: string
  size: string
  category: Category
  color: string
  brand: string
  condition: GarmentCondition
  status: GarmentStatus
  salePrice: string | number | null
  purchasePrice: string | number | null
  notes: string
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
    category: 'T_SHIRT',
    color: '',
    brand: '',
    condition: 'GOOD',
    status: 'WARDROBE',
    salePrice: '',
    purchasePrice: '',
    notes: ''
  }
}

const form = reactive<FormState>(emptyState())

function hydrate() {
  Object.assign(form, emptyState(), {
    name: props.initial.name ?? '',
    description: props.initial.description ?? '',
    size: props.initial.size ?? '',
    category: props.initial.category ?? 'T_SHIRT',
    color: props.initial.color ?? '',
    brand: props.initial.brand ?? '',
    condition: props.initial.condition ?? 'GOOD',
    status: props.initial.status ?? 'WARDROBE',
    salePrice: props.initial.salePrice != null ? String(props.initial.salePrice) : '',
    purchasePrice: props.initial.purchasePrice != null ? String(props.initial.purchasePrice) : '',
    notes: props.initial.notes ?? ''
  })
}

watch(() => props.initial, hydrate, { immediate: true, deep: true })

const errorByField = (field: string): string | undefined => {
  const direct = fieldErrorFor(props.error, field)
  if (direct) return direct
  // Backend uses "salePrice" — we keep the same key but show a friendlier label.
  return undefined
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
  const payload: GarmentCreateRequest = {
    name: form.name.trim(),
    description: form.description.trim() || null,
    size: form.size.trim(),
    category: form.category,
    color: form.color.trim() || null,
    brand: form.brand.trim() || null,
    condition: form.condition,
    status: form.status,
    salePrice: toNumberOrNull(form.salePrice),
    purchasePrice: toNumberOrNull(form.purchasePrice),
    notes: form.notes.trim() || null
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
        <label class="field__label" for="g-color">Color</label>
        <input
          id="g-color"
          v-model="form.color"
          class="input"
          :class="{ 'input--error': errorByField('color') }"
          maxlength="60"
        />
        <span v-if="errorByField('color')" class="field__error">{{ errorByField('color') }}</span>
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
</style>
