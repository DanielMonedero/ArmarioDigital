<script setup lang="ts">
import { computed } from 'vue'
import type { WardrobeStats, WardrobeStatsCategoryBucket } from '@/types/api'
import { CATEGORY_LABELS, SUBCATEGORY_LABELS } from '@/types/api'

interface Props {
  stats: WardrobeStats | null
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), { loading: false })

const maxCount = computed(() => {
  if (!props.stats) return 1
  return Math.max(1, ...props.stats.byCategory.map((b) => b.count))
})

function widthOf(count: number): string {
  const pct = Math.max(2, Math.round((count / maxCount.value) * 100))
  return `${pct}%`
}

function isEmpty(stats: WardrobeStats | null): boolean {
  return !!stats && stats.total === 0
}
</script>

<template>
  <section class="stats card" aria-label="Resumen del armario">
    <header class="stats__head">
      <h2 class="stats__title">Mi armario</h2>
      <div class="stats__total">
        <span class="stats__total-number">{{ stats?.total ?? 0 }}</span>
        <span class="stats__total-label">
          {{ stats?.total === 1 ? 'prenda' : 'prendas' }}
        </span>
      </div>
    </header>

    <p v-if="loading && !stats" class="stats__hint subtle">Cargando resumen…</p>

    <p v-else-if="isEmpty(stats)" class="stats__hint subtle">
      Aún no tienes prendas en el armario. Crea la primera con el botón
      «Añadir prenda».
    </p>

    <ul v-else-if="stats && stats.byCategory.length > 0" class="stats__list">
      <li
        v-for="bucket in stats.byCategory"
        :key="bucket.category"
        class="stats__row"
      >
        <div class="stats__row-head">
          <span class="stats__category">{{ CATEGORY_LABELS[bucket.category] }}</span>
          <span class="stats__count">{{ bucket.count }}</span>
        </div>
        <div class="stats__bar-track">
          <div
            class="stats__bar-fill"
            :style="{ width: widthOf(bucket.count) }"
          />
        </div>
        <ul v-if="bucket.subcategories.length > 0" class="stats__subs">
          <li v-for="sub in bucket.subcategories" :key="sub.subcategory" class="stats__sub">
            <span>{{ SUBCATEGORY_LABELS[sub.subcategory] }}</span>
            <span class="subtle">{{ sub.count }}</span>
          </li>
        </ul>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.stats {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-4);
}

.stats__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-3);
}

.stats__title {
  margin: 0;
  font-size: 1.05rem;
}

.stats__total {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
}

.stats__total-number {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1;
}

.stats__total-label {
  font-size: 0.92rem;
  color: var(--color-text-muted);
}

.stats__hint {
  margin: 0;
}

.stats__list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.stats__row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stats__row-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.stats__category {
  font-weight: 600;
  color: var(--color-text);
}

.stats__count {
  font-weight: 600;
  color: var(--color-text-muted);
  font-variant-numeric: tabular-nums;
}

.stats__bar-track {
  width: 100%;
  height: 8px;
  border-radius: 999px;
  background: var(--color-surface-muted);
  overflow: hidden;
}

.stats__bar-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 999px;
  transition: width var(--transition-base);
  min-width: 4px;
}

.stats__subs {
  list-style: none;
  padding: 0 0 0 var(--space-3);
  margin: 4px 0 0;
  display: flex;
  flex-wrap: wrap;
  gap: 4px var(--space-3);
}

.stats__sub {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  font-size: 0.82rem;
  color: var(--color-text-muted);
}
</style>