import type { GarmentCondition, GarmentStatus, Category } from '@/types/api'
import { CATEGORY_LABELS, CONDITION_LABELS, STATUS_LABELS } from '@/types/api'

export function formatCategory(value: Category): string {
  return CATEGORY_LABELS[value] ?? value
}

export function formatCondition(value: GarmentCondition): string {
  return CONDITION_LABELS[value] ?? value
}

export function formatStatus(value: GarmentStatus): string {
  return STATUS_LABELS[value] ?? value
}

const currencyFormatter = new Intl.NumberFormat('es-ES', {
  style: 'currency',
  currency: 'EUR',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

export function formatPrice(value: number | string | null | undefined): string {
  if (value === null || value === undefined || value === '') return '—'
  const n = typeof value === 'string' ? Number(value) : value
  if (!Number.isFinite(n)) return '—'
  return currencyFormatter.format(n)
}

const dateFormatter = new Intl.DateTimeFormat('es-ES', {
  day: '2-digit',
  month: 'short',
  year: 'numeric'
})

export function formatDate(value: string | null | undefined): string {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return '—'
  return dateFormatter.format(d)
}

const dateTimeFormatter = new Intl.DateTimeFormat('es-ES', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit'
})

export function formatDateTime(value: string | null | undefined): string {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return '—'
  return dateTimeFormatter.format(d)
}
