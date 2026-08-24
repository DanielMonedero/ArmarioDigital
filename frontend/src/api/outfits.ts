import { apiRequest } from '@/api/client'
import type {
  OutfitGenerateRequest,
  OutfitItem,
  SavedOutfit,
  SavedOutfitCreateRequest
} from '@/types/api'

export function generateOutfit(payload: OutfitGenerateRequest) {
  return apiRequest<OutfitItem[]>('/api/outfits/generate', {
    method: 'POST',
    body: payload
  })
}

export function listSavedOutfits() {
  return apiRequest<SavedOutfit[]>('/api/outfits')
}

export function getSavedOutfit(id: number) {
  return apiRequest<SavedOutfit>(`/api/outfits/${id}`)
}

export function saveOutfit(payload: SavedOutfitCreateRequest) {
  return apiRequest<SavedOutfit>('/api/outfits', {
    method: 'POST',
    body: payload
  })
}

export function deleteSavedOutfit(id: number) {
  return apiRequest<void>(`/api/outfits/${id}`, { method: 'DELETE' })
}
