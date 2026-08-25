import { onMounted, ref } from 'vue'
import * as maletaApi from '@/api/maleta'
import type { MaletaItem } from '@/types/api'

const cache = ref<MaletaItem[]>([])
const loading = ref(false)
const error = ref<unknown>(null)
let inflight: Promise<void> | null = null

async function load(force = false): Promise<void> {
  if (!force && cache.value.length > 0) return
  if (inflight) return inflight
  loading.value = true
  error.value = null
  inflight = maletaApi
    .listMaleta()
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

export function useMaleta() {
  onMounted(() => {
    void load()
  })

  async function refresh(): Promise<void> {
    await load(true)
  }

  async function add(garmentId: number): Promise<void> {
    await maletaApi.addToMaleta(garmentId)
    await load(true)
  }

  async function remove(garmentId: number): Promise<void> {
    await maletaApi.removeFromMaleta(garmentId)
    await load(true)
  }

  async function clear(): Promise<void> {
    await maletaApi.clearMaleta()
    cache.value = []
  }

  function contains(garmentId: number): boolean {
    return cache.value.some((item) => item.garmentId === garmentId)
  }

  return {
    items: cache,
    loading,
    error,
    refresh,
    add,
    remove,
    clear,
    contains
  }
}