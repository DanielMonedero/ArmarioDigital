import type { Category, Color, GarmentCondition, Season, Subcategory } from '@/types/api'

export interface Filters {
  search: string
  category: Category | ''
  subcategory: Subcategory | ''
  color: Color | ''
  season: Season | ''
  condition: GarmentCondition | ''
  garmentSize: string
  brand: string
}

export const emptyFilters = (): Filters => ({
  search: '',
  category: '',
  subcategory: '',
  color: '',
  season: '',
  condition: '',
  garmentSize: '',
  brand: ''
})
