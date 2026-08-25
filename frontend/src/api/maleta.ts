import { apiRequest } from '@/api/client'
import type { MaletaItem } from '@/types/api'

export function listMaleta() {
  return apiRequest<MaletaItem[]>('/api/maleta')
}

export function addToMaleta(garmentId: number) {
  return apiRequest<MaletaItem>(`/api/maleta/${garmentId}`, {
    method: 'POST'
  })
}

export function removeFromMaleta(garmentId: number) {
  return apiRequest<void>(`/api/maleta/${garmentId}`, {
    method: 'DELETE'
  })
}

export function clearMaleta() {
  return apiRequest<void>('/api/maleta', {
    method: 'DELETE'
  })
}