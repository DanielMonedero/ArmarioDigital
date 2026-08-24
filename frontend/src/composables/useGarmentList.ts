import { computed, reactive, ref, watch } from 'vue'
import type { GarmentListParams, GarmentStatus, PageResponse, GarmentSummary } from '@/types/api'
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
    garmentSize: undefined,
    brand: undefined,
    color: undefined,
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
