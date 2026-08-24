<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as garmentApi from '@/api/garments'
import * as imageApi from '@/api/images'
import { ApiError } from '@/api/client'
import type { Garment, GarmentUpdateRequest, ImageOrderRequest } from '@/types/api'
import {
  formatCategory,
  formatCondition,
  formatDate,
  formatPrice,
  formatStatus
} from '@/utils/format'
import { describeError } from '@/utils/errors'
import { useToastStore } from '@/stores/toast'
import GarmentGallery from '@/components/GarmentGallery.vue'
import GarmentImageUploader from '@/components/GarmentImageUploader.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import LoadingState from '@/components/LoadingState.vue'
import ErrorState from '@/components/ErrorState.vue'
import StatusBadge from '@/components/StatusBadge.vue'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()

const id = computed(() => Number(route.params.id))

const garment = ref<Garment | null>(null)
const loading = ref(false)
const error = ref<unknown>(null)
const mutating = ref(false)
const confirmDelete = ref(false)
const confirmSoldAction = ref(false)
const confirmWardrobeAction = ref(false)

async function load() {
  loading.value = true
  error.value = null
  try {
    garment.value = await garmentApi.getGarment(id.value)
  } catch (err) {
    error.value = err
    garment.value = null
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, load, { immediate: true })

async function copyDescription() {
  if (!garment.value?.description) return
  try {
    await navigator.clipboard.writeText(garment.value.description)
    toast.success('Descripción copiada al portapapeles')
  } catch {
    toast.error('No se pudo copiar la descripción')
  }
}

async function putForSale() {
  if (!garment.value) return
  mutating.value = true
  try {
    garment.value = await garmentApi.putForSale(id.value)
    toast.success('Prenda puesta en venta')
  } catch (err) {
    toast.error(describeError(err))
  } finally {
    mutating.value = false
  }
}

async function moveToWardrobe() {
  if (!garment.value) return
  mutating.value = true
  try {
    garment.value = await garmentApi.moveToWardrobe(id.value)
    toast.success('Prenda devuelta al armario')
  } catch (err) {
    toast.error(describeError(err))
  } finally {
    mutating.value = false
    confirmWardrobeAction.value = false
  }
}

async function markAsSold() {
  if (!garment.value) return
  mutating.value = true
  try {
    garment.value = await garmentApi.markAsSold(id.value)
    toast.success('Prenda marcada como vendida')
  } catch (err) {
    toast.error(describeError(err))
  } finally {
    mutating.value = false
    confirmSoldAction.value = false
  }
}

async function deleteGarment() {
  if (!garment.value) return
  mutating.value = true
  try {
    await garmentApi.deleteGarment(id.value)
    toast.success('Prenda eliminada')
    confirmDelete.value = false
    await router.push({ name: 'wardrobe' })
  } catch (err) {
    toast.error(describeError(err))
    confirmDelete.value = false
  } finally {
    mutating.value = false
  }
}

async function deleteImage(imageId: number) {
  if (!garment.value) return
  try {
    await imageApi.deleteImage(id.value, imageId)
    garment.value = {
      ...garment.value,
      images: garment.value.images.filter((i) => i.id !== imageId)
    }
    toast.success('Imagen eliminada')
  } catch (err) {
    toast.error(describeError(err))
  }
}

async function reorderImages(orderedIds: number[]) {
  if (!garment.value) return
  const payload: ImageOrderRequest = { imageIds: orderedIds }
  try {
    const updated = await imageApi.reorderImages(id.value, payload)
    garment.value = { ...garment.value, images: updated }
    toast.success('Orden actualizado')
  } catch (err) {
    toast.error(describeError(err))
  }
}

const canEdit = computed(() => garment.value?.status !== 'SOLD')
const canDelete = computed(() => !!garment.value)
const canPutForSale = computed(
  () => garment.value?.status === 'WARDROBE' && (garment.value.images?.length ?? 0) > 0
)
const canMoveToWardrobe = computed(
  () => garment.value?.status === 'FOR_SALE' || garment.value?.status === 'SOLD'
)
const canMarkAsSold = computed(() => garment.value?.status === 'FOR_SALE')
</script>

<template>
  <section class="page">
    <RouterLink :to="{ name: 'wardrobe' }" class="back-link">← Volver</RouterLink>

    <LoadingState v-if="loading" label="Cargando prenda…" />

    <ErrorState
      v-else-if="error"
      :title="'No se pudo cargar la prenda'"
      :message="error instanceof ApiError && error.isNotFound
        ? 'Esta prenda no existe o ya no está disponible.'
        : describeError(error)"
      @retry="load"
    />

    <template v-else-if="garment">
      <header class="page__header">
        <div class="page__title">
          <h1>{{ garment.name }}</h1>
          <p class="page__subtitle">
            <StatusBadge :status="garment.status" />
            <span class="muted"> · {{ formatCategory(garment.category) }}</span>
            <span v-if="garment.brand" class="muted"> · {{ garment.brand }}</span>
            <span v-if="garment.size" class="muted"> · Talla {{ garment.size }}</span>
          </p>
        </div>
        <div class="page__actions">
          <RouterLink
            v-if="canEdit"
            :to="{ name: 'garment-edit', params: { id: garment.id } }"
            class="btn btn--ghost"
          >
            Editar
          </RouterLink>
          <button
            v-if="canPutForSale"
            type="button"
            class="btn btn--primary"
            :disabled="mutating"
            @click="putForSale"
          >
            Poner en venta
          </button>
          <button
            v-if="canMarkAsSold"
            type="button"
            class="btn btn--primary"
            :disabled="mutating"
            @click="confirmSoldAction = true"
          >
            Marcar como vendida
          </button>
          <button
            v-if="canMoveToWardrobe"
            type="button"
            class="btn btn--ghost"
            :disabled="mutating"
            @click="confirmWardrobeAction = true"
          >
            Volver al armario
          </button>
          <button
            v-if="canDelete"
            type="button"
            class="btn btn--danger"
            :disabled="mutating"
            @click="confirmDelete = true"
          >
            Eliminar
          </button>
        </div>
      </header>

      <div class="detail">
        <div class="detail__media">
          <GarmentGallery
            :images="garment.images"
            editable
            @delete="deleteImage"
            @reorder="reorderImages"
          />
          <div class="detail__uploader">
            <GarmentImageUploader :garment-id="garment.id" @uploaded="load" />
          </div>
        </div>

        <aside class="detail__info card">
          <section v-if="garment.status === 'FOR_SALE'" class="detail__description">
            <header class="detail__section-head">
              <h2>Descripción para la venta</h2>
              <button
                type="button"
                class="btn btn--sm btn--ghost"
                :disabled="!garment.description"
                @click="copyDescription"
              >
                Copiar
              </button>
            </header>
            <p v-if="garment.description" class="detail__description-text">
              {{ garment.description }}
            </p>
            <p v-else class="muted">
              Pulsa <em>Poner en venta</em> para que el backend genere la descripción.
            </p>
          </section>

          <section class="detail__prices">
            <div v-if="garment.salePrice != null" class="detail__price">
              <span class="subtle">Precio de venta</span>
              <strong>{{ formatPrice(garment.salePrice) }}</strong>
            </div>
            <div v-if="garment.purchasePrice != null" class="detail__price">
              <span class="subtle">Precio de compra</span>
              <strong>{{ formatPrice(garment.purchasePrice) }}</strong>
            </div>
            <div v-if="garment.soldAt" class="detail__price">
              <span class="subtle">Vendida el</span>
              <strong>{{ formatDate(garment.soldAt) }}</strong>
            </div>
          </section>

          <section class="detail__attributes">
            <dl>
              <div>
                <dt>Estado de la prenda</dt>
                <dd>{{ formatCondition(garment.condition) }}</dd>
              </div>
              <div v-if="garment.color">
                <dt>Color</dt>
                <dd>{{ garment.color }}</dd>
              </div>
              <div v-if="garment.brand">
                <dt>Marca</dt>
                <dd>{{ garment.brand }}</dd>
              </div>
              <div>
                <dt>Situación</dt>
                <dd>{{ formatStatus(garment.status) }}</dd>
              </div>
              <div>
                <dt>Añadida</dt>
                <dd>{{ formatDate(garment.createdAt) }}</dd>
              </div>
              <div v-if="garment.updatedAt !== garment.createdAt">
                <dt>Actualizada</dt>
                <dd>{{ formatDate(garment.updatedAt) }}</dd>
              </div>
            </dl>
          </section>

          <section v-if="garment.notes" class="detail__notes">
            <h3>Notas</h3>
            <p>{{ garment.notes }}</p>
          </section>
        </aside>
      </div>
    </template>

    <ConfirmDialog
      :open="confirmDelete"
      variant="danger"
      title="Eliminar prenda"
      message="Se eliminarán también todas las fotografías asociadas. Esta acción no se puede deshacer."
      confirm-label="Eliminar"
      @confirm="deleteGarment"
      @cancel="confirmDelete = false"
    />
    <ConfirmDialog
      :open="confirmSoldAction"
      title="Marcar como vendida"
      message="La prenda pasará al estado Vendida y conservará el precio y la descripción."
      confirm-label="Marcar vendida"
      @confirm="markAsSold"
      @cancel="confirmSoldAction = false"
    />
    <ConfirmDialog
      :open="confirmWardrobeAction"
      title="Volver al armario"
      message="La prenda volverá a estar disponible en tu armario. Si estaba vendida, se eliminará la fecha de venta."
      confirm-label="Volver al armario"
      @confirm="moveToWardrobe"
      @cancel="confirmWardrobeAction = false"
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

.detail {
  display: grid;
  gap: var(--space-5);
  grid-template-columns: 1fr;
}

@media (min-width: 900px) {
  .detail {
    grid-template-columns: minmax(0, 1.4fr) minmax(280px, 1fr);
    align-items: start;
  }
}

.detail__media {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.detail__uploader {
  margin-top: var(--space-3);
}

.detail__info {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.detail__section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}

.detail__description-text {
  background: var(--color-surface-muted);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  white-space: pre-wrap;
  line-height: 1.55;
}

.detail__prices {
  display: grid;
  gap: var(--space-2);
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
}

.detail__price {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail__price strong {
  font-size: 1.05rem;
}

.detail__attributes dl {
  display: grid;
  gap: var(--space-2);
  margin: 0;
}

.detail__attributes div {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: var(--space-2);
}

.detail__attributes div:last-child {
  border-bottom: none;
}

.detail__attributes dt {
  color: var(--color-text-muted);
  font-size: 0.88rem;
}

.detail__attributes dd {
  margin: 0;
  font-size: 0.92rem;
  color: var(--color-text);
}

.detail__notes h3 {
  font-size: 0.95rem;
  margin-bottom: var(--space-2);
}

.detail__notes p {
  color: var(--color-text-muted);
  white-space: pre-wrap;
}
</style>
