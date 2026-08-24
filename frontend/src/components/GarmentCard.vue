<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import type { GarmentSummary } from '@/types/api'
import { COLOR_HEX, COLOR_LABELS, SEASON_LABELS } from '@/types/api'
import { formatPrice } from '@/utils/format'
import { imageUrl } from '@/api/client'
import StatusBadge from './StatusBadge.vue'

interface Props {
  garment: GarmentSummary
}

const props = defineProps<Props>()

const cover = computed(() => {
  if (!props.garment.coverImageId) return null
  const imageId = Number(props.garment.coverImageId)
  if (!Number.isFinite(imageId)) return null
  return imageUrl(props.garment.id, imageId)
})

const price = computed(() => {
  if (props.garment.status !== 'FOR_SALE') return null
  return formatPrice(props.garment.salePrice)
})

const subtitleParts = computed(() =>
  [props.garment.brand, props.garment.size, props.garment.locationName].filter(Boolean)
)
</script>

<template>
  <RouterLink
    :to="{ name: 'garment-detail', params: { id: garment.id } }"
    class="card-link"
    :aria-label="`Ver detalle de ${garment.name}`"
  >
    <article class="garment-card">
      <div class="garment-card__media">
        <img
          v-if="cover"
          :src="cover"
          :alt="`Foto de ${garment.name}`"
          loading="lazy"
        />
        <div v-else class="garment-card__placeholder" aria-hidden="true">
          <svg viewBox="0 0 48 48" width="42" height="42" fill="none">
            <rect x="6" y="10" width="36" height="28" rx="4" stroke="currentColor" stroke-width="1.5" />
            <circle cx="16" cy="20" r="3" stroke="currentColor" stroke-width="1.5" />
            <path d="M10 34l10-10 8 8 6-6 8 8" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round" />
          </svg>
        </div>
        <div v-if="garment.status !== 'WARDROBE'" class="garment-card__status">
          <StatusBadge :status="garment.status" size="sm" />
        </div>
        <div class="garment-card__badges">
          <span
            v-if="garment.color"
            class="garment-card__color"
            :title="COLOR_LABELS[garment.color]"
            :style="{ background: COLOR_HEX[garment.color] }"
          />
          <span class="garment-card__season" :title="SEASON_LABELS[garment.season]">
            {{ garment.season === 'SUMMER' ? '☀' : '❄' }}
          </span>
        </div>
      </div>
      <div class="garment-card__body">
        <h3 class="garment-card__title">{{ garment.name }}</h3>
        <p v-if="subtitleParts.length" class="garment-card__subtitle">
          {{ subtitleParts.join(' · ') }}
        </p>
        <p v-if="price" class="garment-card__price">{{ price }}</p>
      </div>
    </article>
  </RouterLink>
</template>

<style scoped>
.card-link {
  display: block;
  text-decoration: none;
  color: inherit;
  border-radius: var(--radius-lg);
}

.card-link:hover {
  text-decoration: none;
}

.garment-card {
  display: flex;
  flex-direction: column;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: transform var(--transition-base), box-shadow var(--transition-base),
    border-color var(--transition-base);
  height: 100%;
}

.card-link:hover .garment-card,
.card-link:focus-visible .garment-card {
  transform: translateY(-2px);
  box-shadow: var(--shadow-soft);
  border-color: var(--color-border-strong);
}

.garment-card__media {
  position: relative;
  aspect-ratio: 3 / 4;
  background: var(--color-surface-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.garment-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.garment-card__placeholder {
  color: var(--color-border-strong);
}

.garment-card__status {
  position: absolute;
  top: 10px;
  left: 10px;
}

.garment-card__badges {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.85);
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 0.85rem;
  backdrop-filter: blur(2px);
}

.garment-card__color {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid var(--color-border);
  display: inline-block;
}

.garment-card__season {
  line-height: 1;
}

.garment-card__body {
  padding: var(--space-3) var(--space-4) var(--space-4);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.garment-card__title {
  font-size: 0.98rem;
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.garment-card__subtitle {
  font-size: 0.83rem;
  color: var(--color-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.garment-card__price {
  margin-top: 6px;
  font-weight: 600;
  color: var(--color-accent);
  font-size: 0.95rem;
}
</style>
