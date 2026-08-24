import { onMounted, ref } from 'vue'
import * as locationsApi from '@/api/locations'
import type { Location } from '@/types/api'

// Shared across components so we don't refetch the same list on every mount.
const cache = ref<Location[]>([])
const loading = ref(false)
const error = ref<unknown>(null)
let inflight: Promise<void> | null = null

async function load(force = false): Promise<void> {
  if (!force && cache.value.length > 0) return
  if (inflight) return inflight
  loading.value = true
  error.value = null
  inflight = locationsApi
    .listLocations()
    .then((list) => {
      cache.value = list
    })
    .catch((err) => {
      error.value = err
    })
    .finally(() => {
      loading.value = false
      inflight = null
    })
  return inflight
}

export function useLocations() {
  onMounted(() => {
    void load()
  })

  return {
    locations: cache,
    loading,
    error,
    refresh: () => load(true)
  }
}
