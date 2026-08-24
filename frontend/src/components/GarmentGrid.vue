<script setup lang="ts">
import type { GarmentSummary } from '@/types/api'
import GarmentCard from './GarmentCard.vue'

interface Props {
  garments: GarmentSummary[]
}

defineProps<Props>()
</script>

<template>
  <ul class="garment-grid" role="list">
    <li v-for="g in garments" :key="g.id" class="garment-grid__item">
      <GarmentCard :garment="g" />
    </li>
  </ul>
</template>

<style scoped>
.garment-grid {
  display: grid;
  gap: var(--space-4);
  grid-template-columns: repeat(2, minmax(0, 1fr));
  list-style: none;
  padding: 0;
  margin: 0;
}

@media (min-width: 600px) {
  .garment-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (min-width: 960px) {
  .garment-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (min-width: 1280px) {
  .garment-grid {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}

.garment-grid__item {
  display: flex;
}

.garment-grid__item > * {
  flex: 1;
}
</style>
