import { ApiError } from '@/api/client'

const fieldMessages: Record<string, string> = {
  name: 'Nombre',
  description: 'Descripción',
  size: 'Talla',
  category: 'Categoría',
  condition: 'Estado',
  status: 'Estado',
  color: 'Color',
  brand: 'Marca',
  salePrice: 'Precio de venta',
  purchasePrice: 'Precio de compra',
  notes: 'Notas',
  username: 'Usuario',
  password: 'Contraseña',
  displayName: 'Nombre visible'
}

const messageByCode: Record<string, string> = {
  BAD_CREDENTIALS: 'Usuario o contraseña incorrectos',
  USERNAME_TAKEN: 'Ese nombre de usuario ya está en uso',
  LAST_IMAGE: 'Una prenda debe tener al menos una imagen',
  MISSING_IMAGE: 'Sube al menos una imagen antes de ponerla en venta',
  MISSING_PRICE: 'Indica un precio de venta antes de ponerla en venta',
  INVALID_TRANSITION: 'Esta transición de estado no está permitida',
  INVALID_STATUS: 'Estado no permitido',
  UNSUPPORTED_CONTENT_TYPE: 'Formato de imagen no soportado',
  EMPTY_FILE: 'Selecciona un archivo',
  PAYLOAD_TOO_LARGE: 'La imagen es demasiado grande',
  UNSUPPORTED_MEDIA_TYPE: 'Tipo de archivo no soportado',
  NOT_FOUND: 'No se ha encontrado el recurso',
  FORBIDDEN: 'No tienes permiso para hacer esto',
  UNAUTHORIZED: 'Tu sesión ha caducado',
  DUPLICATE_IDS: 'Las imágenes deben aparecer solo una vez',
  INCOMPLETE_LIST: 'La lista debe incluir todas las imágenes',
  INVALID_IMAGE_ID: 'La imagen no pertenece a esta prenda'
}

export function humanizeFieldName(field: string): string {
  if (fieldMessages[field]) return fieldMessages[field]
  // Best-effort fallback for unknown fields: "salePrice" -> "Sale price".
  const spaced = field.replace(/([A-Z])/g, ' $1').toLowerCase().trim()
  return spaced.charAt(0).toUpperCase() + spaced.slice(1)
}

export function describeError(err: unknown): string {
  if (err instanceof ApiError) {
    return messageByCode[err.code] ?? err.message
  }
  if (err instanceof Error) return err.message
  return 'Ha ocurrido un error inesperado'
}

export function fieldErrorFor(
  err: unknown,
  field: string
): string | undefined {
  if (err instanceof ApiError && err.fieldErrors) {
    return err.fieldErrors[field]
  }
  return undefined
}
