<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as outfitsApi from '@/api/outfits'
import GarmentCard from '@/components/GarmentCard.vue'
import LoadingState from '@/components/LoadingState.vue'
import ErrorState from '@/components/ErrorState.vue'
import EmptyState from '@/components/EmptyState.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import type { OutfitItem, SavedOutfit, Season } from '@/types/api'
import { CATEGORY_LABELS, SEASON_LABELS, SEASONS } from '@/types/api'
import { describeError } from '@/utils/errors'
import { useToastStore } from '@/stores/toast'
import { onMounted } from 'vue'
import { imageUrl } from '@/api/client'

const MAX_VISIBLE_THUMBS = 5

function thumbUrl(item: OutfitItem): string | null {
  if (item.garmentId == null || !item.coverImageId) return null
  const imageId = Number(item.coverImageId)
  if (!Number.isFinite(imageId)) return null
  return imageUrl(item.garmentId, imageId)
}

const router = useRouter()
const toast = useToastStore()

const season = ref<Season>('SUMMER')
const includeOuterwear = ref(true)
const includeAccessories = ref(true)
const generating = ref(false)
const generateError = ref<unknown>(null)
const currentOutfit = ref<OutfitItem[]>([])

const savedOutfits = ref<SavedOutfit[]>([])
const loadingSaved = ref(false)
const errorSaved = ref<unknown>(null)

const saveName = ref('')
const savingOutfit = ref(false)
const confirmDeleteId = ref<number | null>(null)
const deleting = ref(false)

async function loadSaved() {
  loadingSaved.value = true
  errorSaved.value = null
  try {
    savedOutfits.value = await outfitsApi.listSavedOutfits()
  } catch (err) {
    errorSaved.value = err
  } finally {
    loadingSaved.value = false
  }
}

onMounted(loadSaved)

async function generate() {
  generating.value = true
  generateError.value = null
  try {
    currentOutfit.value = await outfitsApi.generateOutfit({
      season: season.value,
      includeOuterwear: includeOuterwear.value,
      includeAccessories: includeAccessories.value
    })
    saveName.value = ''
  } catch (err) {
    generateError.value = err
    currentOutfit.value = []
  } finally {
    generating.value = false
  }
}

async function saveCurrent() {
  const name = saveName.value.trim()
  if (!name || currentOutfit.value.length === 0) return
  savingOutfit.value = true
  try {
    const ids = currentOutfit.value
      .map((i) => i.garmentId)
      .filter((id): id is number => id != null)
    const saved = await outfitsApi.saveOutfit({
      name,
      season: season.value,
      includeOuterwear: includeOuterwear.value,
      includeAccessories: includeAccessories.value,
      garmentIds: ids
    })
    toast.success(`«${saved.name}» guardado`)
    saveName.value = ''
    currentOutfit.value = []
    await loadSaved()
  } catch (err) {
    toast.error(describeError(err))
  } finally {
    savingOutfit.value = false
  }
}

function discardCurrent() {
  currentOutfit.value = []
  generateError.value = null
}

async function askDelete(id: number) {
  confirmDeleteId.value = id
}

async function confirmDelete() {
  const id = confirmDeleteId.value
  if (id == null) return
  deleting.value = true
  try {
    await outfitsApi.deleteSavedOutfit(id)
    toast.success('Conjunto eliminado')
    confirmDeleteId.value = null
    await loadSaved()
  } catch (err) {
    toast.error(describeError(err))
  } finally {
    deleting.value = false
  }
}

function openSaved(id: number) {
  void router.push({ name: 'outfit-detail', params: { id } })
}

const currentCount = computed(() => currentOutfit.value.length)
</script>

<template>
  <section class="page">
    <header class="page__header">
      <div class="page__title">
        <h1>Conjuntos</h1>
        <p class="page__subtitle">
          Genera un conjunto aleatorio a partir de tu armario o consulta los que ya tienes guardados.
        </p>
      </div>
    </header>

    <section class="generator card">
      <h2 class="generator__title">Generador aleatorio</h2>

      <div class="generator__controls">
        <div class="field">
          <label class="field__label" for="gen-season">Temporada</label>
          <select id="gen-season" v-model="season" class="select">
            <option v-for="s in SEASONS" :key="s" :value="s">{{ SEASON_LABELS[s] }}</option>
          </select>
        </div>

        <label class="generator__toggle">
          <input v-model="includeOuterwear" type="checkbox" />
          <span>Incluir capa exterior (chaqueta, abrigo…)</span>
        </label>

        <label class="generator__toggle">
          <input v-model="includeAccessories" type="checkbox" />
          <span>Incluir accesorios (cinturón, bolso…)</span>
        </label>

        <button
          type="button"
          class="btn btn--primary generator__btn"
          :disabled="generating"
          @click="generate"
        >
          {{ generating ? 'Generando…' : 'Generar conjunto' }}
        </button>
      </div>

      <p class="generator__hint subtle">
        Si tienes vestidos, pueden sustituir al conjunto de arriba + abajo.
      </p>
    </section>

    <section v-if="currentOutfit.length > 0 || generateError" class="generator__result">
      <LoadingState v-if="generating" label="Buscando prendas al azar…" />

      <ErrorState
        v-else-if="generateError"
        title="No se pudo generar el conjunto"
        :message="describeError(generateError)"
        @retry="generate"
      />

      <template v-else>
        <header class="generator__result-head">
          <h3>Tu conjunto ({{ currentCount }} prendas)</h3>
          <button type="button" class="btn btn--ghost btn--sm" @click="discardCurrent">
            Descartar
          </button>
        </header>

        <div class="generator__grid">
          <GarmentCard
            v-for="item in currentOutfit"
            :key="item.garmentId ?? item.name"
            :garment="item.garment!"
          />
        </div>

        <form class="generator__save card" @submit.prevent="saveCurrent">
          <label class="field__label" for="save-name">Guardar como…</label>
          <div class="generator__save-row">
            <input
              id="save-name"
              v-model="saveName"
              class="input"
              placeholder="Look casual viernes"
              maxlength="160"
              :disabled="savingOutfit"
            />
            <button
              type="submit"
              class="btn btn--primary"
              :disabled="savingOutfit || !saveName.trim()"
            >
              {{ savingOutfit ? 'Guardando…' : 'Guardar' }}
            </button>
          </div>
        </form>
      </template>
    </section>

    <section class="saved">
      <h2 class="saved__title">Conjuntos guardados</h2>

      <LoadingState v-if="loadingSaved && savedOutfits.length === 0" label="Cargando conjuntos…" />

      <ErrorState
        v-else-if="errorSaved"
        title="No se pudieron cargar los conjuntos"
        :message="describeError(errorSaved)"
        @retry="loadSaved"
      />

      <EmptyState
        v-else-if="savedOutfits.length === 0"
        icon="sparkles"
        label="Aún no has guardado ningún conjunto"
        description="Genera uno con el formulario de arriba y guárdalo si te gusta."
      />

      <ul v-else class="saved__list">
        <li v-for="outfit in savedOutfits" :key="outfit.id" class="saved__item card">
          <div
            class="saved__thumbs"
            :aria-label="`${outfit.items.length} prendas en este conjunto`"
          >
            <span
              v-for="item in outfit.items.slice(0, MAX_VISIBLE_THUMBS)"
              :key="(item.garmentId ?? item.name) + '-thumb'"
              class="saved__thumb"
              :title="`${CATEGORY_LABELS[item.category]} · ${item.name}`"
            >
              <img
                v-if="thumbUrl(item)"
                :src="thumbUrl(item)!"
                :alt="item.name"
                loading="lazy"
              />
              <span v-else class="saved__thumb-fallback" aria-hidden="true">
                {{ item.name.charAt(0).toUpperCase() }}
              </span>
            </span>
            <span
              v-if="outfit.items.length > MAX_VISIBLE_THUMBS"
              class="saved__thumb saved__thumb--more"
              :title="`+${outfit.items.length - MAX_VISIBLE_THUMBS} prendas más`"
            >
              +{{ outfit.items.length - MAX_VISIBLE_THUMBS }}
            </span>
          </div>

          <button class="saved__open" type="button" @click="openSaved(outfit.id)">
            <span class="saved__name">{{ outfit.name }}</span>
            <span class="saved__meta subtle">
              {{ outfit.items.length }} prendas · {{ SEASON_LABELS[outfit.season] }}
            </span>
          </button>
          <button
            type="button"
            class="btn btn--danger btn--sm"
            @click="askDelete(outfit.id)"
          >
            Eliminar
          </button>
        </li>
      </ul>
    </section>

    <ConfirmDialog
      :open="confirmDeleteId != null"
      variant="danger"
      title="Eliminar conjunto"
      message="¿Seguro que quieres eliminar este conjunto guardado?"
      confirm-label="Eliminar"
      :loading="deleting"
      @confirm="confirmDelete"
      @cancel="confirmDeleteId = null"
    />
  </section>
</template>

<style scoped>
.generator {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-4);
}

.generator__title {
  margin: 0;
  font-size: 1.05rem;
}

.generator__controls {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  align-items: flex-end;
}

.generator__controls .field {
  min-width: 140px;
}

.generator__toggle {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 0.92rem;
  color: var(--color-text);
  cursor: pointer;
  padding-bottom: 8px;
}

.generator__btn {
  margin-left: auto;
}

.generator__hint {
  font-size: 0.85rem;
  margin: 0;
}

.generator__result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: var(--space-4) 0 var(--space-2);
}

.generator__result-head h3 {
  margin: 0;
  font-size: 1rem;
}

.generator__grid {
  display: grid;
  gap: var(--space-3);
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (min-width: 600px) {
  .generator__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (min-width: 960px) {
  .generator__grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

.generator__save {
  margin-top: var(--space-3);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-4);
}

.generator__save-row {
  display: flex;
  gap: var(--space-2);
}

.generator__save-row .input {
  flex: 1;
}

.saved {
  margin-top: var(--space-5);
}

.saved__title {
  font-size: 1.05rem;
  margin: 0 0 var(--space-3);
}

.saved__list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.saved__item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
}

.saved__thumbs {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.saved__thumb {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-surface-muted);
  border: 2px solid var(--color-surface);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--color-text-muted);
  margin-left: -8px;
  flex-shrink: 0;
}

.saved__thumb:first-child {
  margin-left: 0;
}

.saved__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.saved__thumb-fallback {
  line-height: 1;
}

.saved__thumb--more {
  background: var(--color-border-strong);
  color: var(--color-surface);
  font-size: 0.72rem;
}

.saved__open {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  background: transparent;
  border: 0;
  padding: 0;
  cursor: pointer;
  text-align: left;
  flex: 1;
  min-width: 0;
  color: inherit;
}

.saved__name {
  font-weight: 600;
  color: var(--color-text);
}

.saved__meta {
  font-size: 0.85rem;
}
</style>
