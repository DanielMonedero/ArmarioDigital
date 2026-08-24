import { computed, reactive, ref, watch } from 'vue'
import type {
  Category,
  GarmentCondition,
  GarmentListParams,
  GarmentStatus,
  PageResponse,
  GarmentSummary,
  Season,
  Subcategory
} from '@/types/api'
import { listGarments } from '@/api/garments'

interface UseGarmentListOptions {
  status?: GarmentStatus
  defaultSort?: string
  pageSize?: number
}

export function useGarmentList(options: UseGarmentListOptions = {}) {
  const status = options.status
  const pageSize = options.pageSize ?? 24
  const defaultSort = options.defaultSort ?? 'createdAt,desc'

  const items = ref<GarmentSummary[]>([])
  const page = ref(0)
  const totalElements = ref(0)
  const totalPages = ref(0)
  const loading = ref(false)
  const error = ref<unknown>(null)
  const sort = ref(defaultSort)

  const params = reactive<Omit<GarmentListParams, 'page' | 'size' | 'sort' | 'status'>>({
    search: undefined,
    category: undefined,
    subcategory: undefined,
    color: undefined,
    season: undefined,
    garmentSize: undefined,
    brand: undefined,
    condition: undefined
  })

  let activeRequest = 0

  async function load(targetPage = page.value) {
    const requestId = ++activeRequest
    loading.value = true
    error.value = null
    page.value = targetPage
    try {
      const response: PageResponse<GarmentSummary> = await listGarments({
        ...params,
        status,
        page: targetPage,
        size: pageSize,
        sort: sort.value
      })
      if (requestId !== activeRequest) return
      items.value = response.content
      totalElements.value = response.totalElements
      totalPages.value = response.totalPages
    } catch (err) {
      if (requestId !== activeRequest) return
      error.value = err
      items.value = []
      totalElements.value = 0
      totalPages.value = 0
    } finally {
      if (requestId === activeRequest) {
        loading.value = false
      }
    }
  }

  function setPage(newPage: number) {
    if (newPage < 0 || newPage >= totalPages.value) return
    void load(newPage)
  }

  function reloadFromFirstPage() {
    void load(0)
  }

  // When category changes, subcategory becomes invalid (each sub belongs to a
  // single parent). Reset it so the next request doesn't filter by an orphan.
  watch(
    () => params.category,
    (newCat) => {
      if (params.subcategory && newCat) {
        if (!subcategoryBelongsTo(params.subcategory, newCat)) {
          params.subcategory = undefined
        }
      }
    }
  )

  watch(params, reloadFromFirstPage, { deep: true })
  void load(0)

  const isEmpty = computed(() => !loading.value && items.value.length === 0)

  return {
    items,
    page,
    totalElements,
    totalPages,
    loading,
    error,
    sort,
    params,
    isEmpty,
    load,
    setPage,
    reloadFromFirstPage
  }
}

const SUBCATEGORY_PARENT: Record<Subcategory, Category> = {
  // TOP
  T_SHIRT: 'TOP',
  SHIRT: 'TOP',
  POLO: 'TOP',
  TANK_TOP: 'TOP',
  BLOUSE: 'TOP',
  // SWEATER
  SWEATER: 'SWEATER',
  HOODIE: 'SWEATER',
  CARDIGAN: 'SWEATER',
  // OUTERWEAR
  JACKET: 'OUTERWEAR',
  COAT: 'OUTERWEAR',
  BLAZER: 'OUTERWEAR',
  VEST: 'OUTERWEAR',
  // BOTTOM
  JEANS: 'BOTTOM',
  CHINOS: 'BOTTOM',
  DRESS_PANTS: 'BOTTOM',
  JOGGERS: 'BOTTOM',
  LINEN_PANTS: 'BOTTOM',
  SHORTS: 'BOTTOM',
  LEGGINGS: 'BOTTOM',
  // SKIRT
  MINI_SKIRT: 'SKIRT',
  MIDI_SKIRT: 'SKIRT',
  MAXI_SKIRT: 'SKIRT',
  // DRESS
  SHORT_DRESS: 'DRESS',
  LONG_DRESS: 'DRESS',
  // SHOES
  SNEAKERS: 'SHOES',
  BOOTS: 'SHOES',
  SANDALS: 'SHOES',
  HEELED: 'SHOES',
  FLATS: 'SHOES',
  // ACCESSORIES
  BELT: 'ACCESSORIES',
  BAG: 'ACCESSORIES',
  HAT: 'ACCESSORIES',
  SCARF: 'ACCESSORIES',
  // OTHER
  OTHER: 'OTHER'
}

function subcategoryBelongsTo(sub: Subcategory, cat: Category): boolean {
  return SUBCATEGORY_PARENT[sub] === cat
}
