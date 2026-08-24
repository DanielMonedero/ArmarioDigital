import { apiRequest } from '@/api/client'
import type { Location, LocationCreateRequest } from '@/types/api'

export function listLocations() {
  return apiRequest<Location[]>('/api/locations')
}

export function createLocation(payload: LocationCreateRequest) {
  return apiRequest<Location>('/api/locations', {
    method: 'POST',
    body: payload
  })
}

export function updateLocation(id: number, payload: LocationCreateRequest) {
  return apiRequest<Location>(`/api/locations/${id}`, {
    method: 'PUT',
    body: payload
  })
}

export function deleteLocation(id: number) {
  return apiRequest<void>(`/api/locations/${id}`, { method: 'DELETE' })
}
