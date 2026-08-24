// Types mirror the backend's Java records and enums exactly.
// Field names, enum values and JSON shapes come from the Spring Boot DTOs.

export type GarmentStatus = 'WARDROBE' | 'FOR_SALE' | 'SOLD'

export type GarmentCondition = 'NEW' | 'LIKE_NEW' | 'GOOD' | 'USED'

export type Category =
  | 'T_SHIRT'
  | 'SHIRT'
  | 'SWEATER'
  | 'HOODIE'
  | 'JACKET'
  | 'COAT'
  | 'DRESS'
  | 'PANTS'
  | 'JEANS'
  | 'SHORTS'
  | 'SKIRT'
  | 'SHOES'
  | 'ACCESSORIES'
  | 'OTHER'

export const GARMENT_STATUSES: readonly GarmentStatus[] = [
  'WARDROBE',
  'FOR_SALE',
  'SOLD'
] as const

export const GARMENT_CONDITIONS: readonly GarmentCondition[] = [
  'NEW',
  'LIKE_NEW',
  'GOOD',
  'USED'
] as const

export const CATEGORIES: readonly Category[] = [
  'T_SHIRT',
  'SHIRT',
  'SWEATER',
  'HOODIE',
  'JACKET',
  'COAT',
  'DRESS',
  'PANTS',
  'JEANS',
  'SHORTS',
  'SKIRT',
  'SHOES',
  'ACCESSORIES',
  'OTHER'
] as const

// Human-readable labels so the UI never shows T_SHIRT / FOR_SALE directly.
export const CATEGORY_LABELS: Record<Category, string> = {
  T_SHIRT: 'Camiseta',
  SHIRT: 'Camisa',
  SWEATER: 'Jersey',
  HOODIE: 'Sudadera',
  JACKET: 'Chaqueta',
  COAT: 'Abrigo',
  DRESS: 'Vestido',
  PANTS: 'Pantalón',
  JEANS: 'Vaqueros',
  SHORTS: 'Pantalón corto',
  SKIRT: 'Falda',
  SHOES: 'Zapatos',
  ACCESSORIES: 'Accesorios',
  OTHER: 'Otro'
}

export const CONDITION_LABELS: Record<GarmentCondition, string> = {
  NEW: 'Nuevo',
  LIKE_NEW: 'Como nuevo',
  GOOD: 'Buen estado',
  USED: 'Usado'
}

export const STATUS_LABELS: Record<GarmentStatus, string> = {
  WARDROBE: 'Armario',
  FOR_SALE: 'En venta',
  SOLD: 'Vendida'
}

export interface UserResponse {
  id: number
  username: string
  displayName: string | null
  createdAt: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  user: UserResponse
}

export interface CreateUserRequest {
  username: string
  password: string
  displayName?: string
}

export interface GarmentImage {
  id: number
  garmentId: number | null
  filename: string
  originalFilename: string
  contentType: string
  size: number
  sortOrder: number
  createdAt: string
}

export interface GarmentSummary {
  id: number
  name: string
  size: string
  category: Category
  color: string | null
  brand: string | null
  condition: GarmentCondition
  status: GarmentStatus
  salePrice: number | null
  coverImageId: string | null
  createdAt: string
  updatedAt: string
}

export interface Garment {
  id: number
  name: string
  description: string | null
  size: string
  category: Category
  color: string | null
  brand: string | null
  condition: GarmentCondition
  status: GarmentStatus
  salePrice: number | null
  purchasePrice: number | null
  notes: string | null
  soldAt: string | null
  createdAt: string
  updatedAt: string
  images: GarmentImage[]
}

export interface GarmentCreateRequest {
  name: string
  description?: string | null
  size: string
  category: Category
  color?: string | null
  brand?: string | null
  condition: GarmentCondition
  status: GarmentStatus
  salePrice?: number | null
  purchasePrice?: number | null
  notes?: string | null
}

export interface GarmentUpdateRequest {
  name: string
  description?: string | null
  size: string
  category: Category
  color?: string | null
  brand?: string | null
  condition: GarmentCondition
  status: GarmentStatus
  salePrice?: number | null
  purchasePrice?: number | null
  notes?: string | null
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface ImageOrderRequest {
  imageIds: number[]
}

export interface ErrorResponse {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors?: Record<string, string>
}

export interface GarmentListParams {
  status?: GarmentStatus
  category?: Category
  garmentSize?: string
  brand?: string
  color?: string
  condition?: GarmentCondition
  search?: string
  page?: number
  size?: number
  sort?: string
}

export type ApiErrorCode =
  | 'VALIDATION_ERROR'
  | 'BAD_REQUEST'
  | 'BAD_CREDENTIALS'
  | 'UNAUTHORIZED'
  | 'FORBIDDEN'
  | 'NOT_FOUND'
  | 'CONFLICT'
  | 'USERNAME_TAKEN'
  | 'INVALID_TRANSITION'
  | 'MISSING_IMAGE'
  | 'MISSING_PRICE'
  | 'INVALID_STATUS'
  | 'LAST_IMAGE'
  | 'UNSUPPORTED_CONTENT_TYPE'
  | 'EMPTY_FILE'
  | 'INVALID_FILENAME'
  | 'DUPLICATE_IDS'
  | 'INCOMPLETE_LIST'
  | 'INVALID_IMAGE_ID'
  | 'PAYLOAD_TOO_LARGE'
  | 'UNSUPPORTED_MEDIA_TYPE'
  | 'INTERNAL_ERROR'
