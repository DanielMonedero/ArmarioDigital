import { apiRequest } from '@/api/client'
import type { GarmentImage, ImageOrderRequest } from '@/types/api'

export function uploadImage(garmentId: number, file: File) {
  const form = new FormData()
  form.append('file', file)
  return apiRequest<GarmentImage>(`/api/garments/${garmentId}/images`, {
    method: 'POST',
    formData: form
  })
}

export function deleteImage(garmentId: number, imageId: number) {
  return apiRequest<void>(`/api/garments/${garmentId}/images/${imageId}`, {
    method: 'DELETE'
  })
}

export function reorderImages(garmentId: number, payload: ImageOrderRequest) {
  return apiRequest<GarmentImage[]>(`/api/garments/${garmentId}/images/order`, {
    method: 'PUT',
    body: payload
  })
}
