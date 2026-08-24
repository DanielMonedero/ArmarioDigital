// Types mirror the backend's Java records and enums exactly.
// Field names, enum values and JSON shapes come from the Spring Boot DTOs.

export type GarmentStatus = 'WARDROBE' | 'FOR_SALE' | 'SOLD'

export type GarmentCondition = 'NEW' | 'LIKE_NEW' | 'GOOD' | 'USED'

export type Category =
  | 'TOP'
  | 'SWEATER'
  | 'OUTERWEAR'
  | 'BOTTOM'
  | 'SKIRT'
  | 'DRESS'
  | 'SHOES'
  | 'ACCESSORIES'
  | 'OTHER'

export type Subcategory =
  // TOP
  | 'T_SHIRT'
  | 'SHIRT'
  | 'POLO'
  | 'TANK_TOP'
  | 'BLOUSE'
  // SWEATER
  | 'SWEATER'
  | 'HOODIE'
  | 'CARDIGAN'
  // OUTERWEAR
  | 'JACKET'
  | 'COAT'
  | 'BLAZER'
  | 'VEST'
  // BOTTOM
  | 'JEANS'
  | 'CHINOS'
  | 'DRESS_PANTS'
  | 'JOGGERS'
  | 'LINEN_PANTS'
  | 'SHORTS'
  | 'LEGGINGS'
  // SKIRT
  | 'MINI_SKIRT'
  | 'MIDI_SKIRT'
  | 'MAXI_SKIRT'
  // DRESS
  | 'SHORT_DRESS'
  | 'LONG_DRESS'
  // SHOES
  | 'SNEAKERS'
  | 'BOOTS'
  | 'SANDALS'
  | 'HEELED'
  | 'FLATS'
  // ACCESSORIES
  | 'BELT'
  | 'BAG'
  | 'HAT'
  | 'SCARF'
  // OTHER
  | 'OTHER'

export type Color =
  | 'WHITE'
  | 'BLACK'
  | 'GRAY'
  | 'BEIGE'
  | 'RED'
  | 'ORANGE'
  | 'YELLOW'
  | 'GREEN'
  | 'BLUE'
  | 'MULTICOLOR'

export type Season = 'SUMMER' | 'WINTER'

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
  'TOP',
  'SWEATER',
  'OUTERWEAR',
  'BOTTOM',
  'SKIRT',
  'DRESS',
  'SHOES',
  'ACCESSORIES',
  'OTHER'
] as const

export const COLORS: readonly Color[] = [
  'WHITE',
  'BLACK',
  'GRAY',
  'BEIGE',
  'RED',
  'ORANGE',
  'YELLOW',
  'GREEN',
  'BLUE',
  'MULTICOLOR'
] as const

export const SEASONS: readonly Season[] = ['SUMMER', 'WINTER'] as const

// Map of category → allowed subcategories. Mirrors the backend enum.
export const SUBCATEGORY_BY_CATEGORY: Record<Category, readonly Subcategory[]> = {
  TOP: ['T_SHIRT', 'SHIRT', 'POLO', 'TANK_TOP', 'BLOUSE'],
  SWEATER: ['SWEATER', 'HOODIE', 'CARDIGAN'],
  OUTERWEAR: ['JACKET', 'COAT', 'BLAZER', 'VEST'],
  BOTTOM: ['JEANS', 'CHINOS', 'DRESS_PANTS', 'JOGGERS', 'LINEN_PANTS', 'SHORTS', 'LEGGINGS'],
  SKIRT: ['MINI_SKIRT', 'MIDI_SKIRT', 'MAXI_SKIRT'],
  DRESS: ['SHORT_DRESS', 'LONG_DRESS'],
  SHOES: ['SNEAKERS', 'BOOTS', 'SANDALS', 'HEELED', 'FLATS'],
  ACCESSORIES: ['BELT', 'BAG', 'HAT', 'SCARF'],
  OTHER: ['OTHER']
}

export function subcategoriesFor(category: Category): readonly Subcategory[] {
  return SUBCATEGORY_BY_CATEGORY[category]
}

// Human-readable labels so the UI never shows enum names directly.
export const CATEGORY_LABELS: Record<Category, string> = {
  TOP: 'Parte de arriba',
  SWEATER: 'Jersey / sudadera',
  OUTERWEAR: 'Abrigo / chaqueta',
  BOTTOM: 'Pantalón',
  SKIRT: 'Falda',
  DRESS: 'Vestido',
  SHOES: 'Zapatos',
  ACCESSORIES: 'Accesorios',
  OTHER: 'Otro'
}

export const SUBCATEGORY_LABELS: Record<Subcategory, string> = {
  // TOP
  T_SHIRT: 'Camiseta',
  SHIRT: 'Camisa',
  POLO: 'Polo',
  TANK_TOP: 'Camiseta tirantes',
  BLOUSE: 'Blusa',
  // SWEATER
  SWEATER: 'Jersey',
  HOODIE: 'Sudadera',
  CARDIGAN: 'Cárdigan',
  // OUTERWEAR
  JACKET: 'Chaqueta',
  COAT: 'Abrigo',
  BLAZER: 'Blazer',
  VEST: 'Chaleco',
  // BOTTOM
  JEANS: 'Vaqueros',
  CHINOS: 'Chinos',
  DRESS_PANTS: 'Pantalón de vestir',
  JOGGERS: 'Pantalón de chándal',
  LINEN_PANTS: 'Pantalón de lino',
  SHORTS: 'Pantalón corto',
  LEGGINGS: 'Mallas',
  // SKIRT
  MINI_SKIRT: 'Mini falda',
  MIDI_SKIRT: 'Falda midi',
  MAXI_SKIRT: 'Falda larga',
  // DRESS
  SHORT_DRESS: 'Vestido corto',
  LONG_DRESS: 'Vestido largo',
  // SHOES
  SNEAKERS: 'Zapatillas',
  BOOTS: 'Botas',
  SANDALS: 'Sandalias',
  HEELED: 'Tacón',
  FLATS: 'Bailarinas',
  // ACCESSORIES
  BELT: 'Cinturón',
  BAG: 'Bolso',
  HAT: 'Sombrero',
  SCARF: 'Bufanda',
  // OTHER
  OTHER: 'Otro'
}

export const COLOR_LABELS: Record<Color, string> = {
  WHITE: 'Blanco',
  BLACK: 'Negro',
  GRAY: 'Gris',
  BEIGE: 'Beige',
  RED: 'Rojo',
  ORANGE: 'Naranja',
  YELLOW: 'Amarillo',
  GREEN: 'Verde',
  BLUE: 'Azul',
  MULTICOLOR: 'Multicolor'
}

// Hex values for UI swatches.
export const COLOR_HEX: Record<Color, string> = {
  WHITE: '#f4f4f4',
  BLACK: '#1a1a1a',
  GRAY: '#8a8a8a',
  BEIGE: '#e3d5b8',
  RED: '#c83a3a',
  ORANGE: '#e8843a',
  YELLOW: '#e8c53a',
  GREEN: '#5fa84a',
  BLUE: '#3a78c8',
  MULTICOLOR: 'linear-gradient(135deg, #c83a3a 0 25%, #e8c53a 25% 50%, #5fa84a 50% 75%, #3a78c8 75%)'
}

export const SEASON_LABELS: Record<Season, string> = {
  SUMMER: 'Verano',
  WINTER: 'Invierno'
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
  subcategory: Subcategory | null
  color: Color | null
  brand: string | null
  condition: GarmentCondition
  status: GarmentStatus
  season: Season
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
  subcategory: Subcategory | null
  color: Color | null
  brand: string | null
  condition: GarmentCondition
  status: GarmentStatus
  season: Season
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
  subcategory?: Subcategory | null
  color?: Color | null
  brand?: string | null
  condition: GarmentCondition
  status: GarmentStatus
  season: Season
  salePrice?: number | null
  purchasePrice?: number | null
  notes?: string | null
}

export interface GarmentUpdateRequest {
  name: string
  description?: string | null
  size: string
  category: Category
  subcategory?: Subcategory | null
  color?: Color | null
  brand?: string | null
  condition: GarmentCondition
  status: GarmentStatus
  season: Season
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
  subcategory?: Subcategory
  color?: Color
  season?: Season
  garmentSize?: string
  brand?: string
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
  | 'INVALID_SUBCATEGORY'
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
