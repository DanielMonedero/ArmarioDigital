<script setup lang="ts">
import { computed } from 'vue'
import { formatStatus } from '@/utils/format'
import type { GarmentStatus } from '@/types/api'

interface Props {
  status: GarmentStatus
  size?: 'sm' | 'md'
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md'
})

const variant = computed(() => {
  switch (props.status) {
    case 'WARDROBE':
      return 'badge--wardrobe'
    case 'FOR_SALE':
      return 'badge--for-sale'
    case 'SOLD':
      return 'badge--sold'
  }
})

const label = computed(() => formatStatus(props.status))
</script>

<template>
  <span class="badge" :class="[variant, `badge--${size}`]">{{ label }}</span>
</template>

<style scoped>
.badge--sm {
  font-size: 0.7rem;
  padding: 2px 8px;
}
</style>
