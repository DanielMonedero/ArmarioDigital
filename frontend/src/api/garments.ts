import { apiRequest } from '@/api/client'
import type {
  Garment,
  GarmentCreateRequest,
  GarmentListParams,
  GarmentStatus,
  GarmentUpdateRequest,
  PageResponse,
  GarmentSummary,
  WardrobeStats
} from '@/types/api'

export function listGarments(params: GarmentListParams = {}) {
  return apiRequest<PageResponse<GarmentSummary>>('/api/garments', {
    query: {
      status: params.status,
      category: params.category,
      subcategory: params.subcategory,
      color: params.color,
      season: params.season,
      locationId: params.locationId,
      garmentSize: params.garmentSize,
      brand: params.brand,
      condition: params.condition,
      search: params.search,
      page: params.page,
      size: params.size,
      sort: params.sort
    }
  })
}

export function fetchWardrobeStats() {
  return apiRequest<WardrobeStats>('/api/garments/stats')
}

export function getGarment(id: number) {
  return apiRequest<Garment>(`/api/garments/${id}`)
}

export function createGarment(payload: GarmentCreateRequest) {
  return apiRequest<Garment>('/api/garments', {
    method: 'POST',
    body: payload
  })
}

export function updateGarment(id: number, payload: GarmentUpdateRequest) {
  return apiRequest<Garment>(`/api/garments/${id}`, {
    method: 'PUT',
    body: payload
  })
}

export function deleteGarment(id: number) {
  return apiRequest<void>(`/api/garments/${id}`, { method: 'DELETE' })
}

export function putForSale(id: number) {
  return apiRequest<Garment>(`/api/garments/${id}/put-for-sale`, { method: 'POST' })
}

export function moveToWardrobe(id: number) {
  return apiRequest<Garment>(`/api/garments/${id}/move-to-wardrobe`, { method: 'POST' })
}

export function markAsSold(id: number) {
  return apiRequest<Garment>(`/api/garments/${id}/mark-as-sold`, { method: 'POST' })
}

export type { GarmentStatus }
