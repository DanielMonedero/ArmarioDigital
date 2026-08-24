import type { Category, GarmentCondition } from '@/types/api'

export interface Filters {
  search: string
  category: Category | ''
  condition: GarmentCondition | ''
  garmentSize: string
  brand: string
  color: string
}

export const emptyFilters = (): Filters => ({
  search: '',
  category: '',
  condition: '',
  garmentSize: '',
  brand: '',
  color: ''
})
