import type { ApiErrorCode, ErrorResponse } from '@/types/api'

const baseUrl = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '')

export class ApiError extends Error {
  readonly status: number
  readonly code: ApiErrorCode | string
  readonly path: string
  readonly fieldErrors?: Record<string, string>
  readonly timestamp?: string

  constructor(
    status: number,
    code: ApiErrorCode | string,
    message: string,
    path: string,
    fieldErrors?: Record<string, string>,
    timestamp?: string
  ) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.path = path
    this.fieldErrors = fieldErrors
    this.timestamp = timestamp
  }

  get isUnauthorized(): boolean {
    return this.status === 401
  }

  get isForbidden(): boolean {
    return this.status === 403
  }

  get isNotFound(): boolean {
    return this.status === 404
  }

  get isConflict(): boolean {
    return this.status === 409
  }

  get isValidation(): boolean {
    return this.status === 400 && this.code === 'VALIDATION_ERROR'
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  query?: Record<string, string | number | undefined | null>
  formData?: FormData
  signal?: AbortSignal
  // Allow binary responses (e.g. image download).
  responseType?: 'json' | 'blob'
  headers?: Record<string, string>
}

function buildUrl(path: string, query?: RequestOptions['query']): string {
  const url = baseUrl + path
  if (!query) return url
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (value === undefined || value === null || value === '') continue
    params.set(key, String(value))
  }
  const qs = params.toString()
  return qs ? `${url}?${qs}` : url
}

async function parseError(response: Response, fallbackPath: string): Promise<ApiError> {
  let body: ErrorResponse | null = null
  try {
    body = (await response.json()) as ErrorResponse
  } catch {
    // body may be empty (e.g. the 401 entry point writes no body).
  }
  if (body && typeof body.status === 'number' && typeof body.error === 'string') {
    return new ApiError(
      body.status,
      body.error,
      body.message || response.statusText,
      body.path || fallbackPath,
      body.fieldErrors,
      body.timestamp
    )
  }
  return new ApiError(
    response.status,
    response.status === 401 ? 'UNAUTHORIZED' : 'INTERNAL_ERROR',
    response.statusText || 'Request failed',
    fallbackPath
  )
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, query, formData, signal, responseType = 'json', headers } = options

  const init: RequestInit = {
    method,
    credentials: 'include',
    signal
  }

  const finalHeaders: Record<string, string> = { ...(headers ?? {}) }

  if (formData) {
    init.body = formData
    // The browser sets the multipart boundary automatically.
  } else if (body !== undefined) {
    init.body = JSON.stringify(body)
    finalHeaders['Content-Type'] = 'application/json'
  }

  if (Object.keys(finalHeaders).length > 0) {
    init.headers = finalHeaders
  }

  const response = await fetch(buildUrl(path, query), init)

  if (!response.ok) {
    throw await parseError(response, path)
  }

  if (response.status === 204) {
    return undefined as T
  }

  if (responseType === 'blob') {
    return (await response.blob()) as T
  }

  if (response.headers.get('content-length') === '0') {
    return undefined as T
  }

  // Guard against empty responses for endpoints that return 200 with no body.
  const text = await response.text()
  if (!text) return undefined as T
  try {
    return JSON.parse(text) as T
  } catch {
    return text as unknown as T
  }
}

export function imageUrl(garmentId: number, imageId: number): string {
  return `${baseUrl}/api/garments/${garmentId}/images/${imageId}`
}
