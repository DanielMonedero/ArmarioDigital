import { apiRequest } from '@/api/client'
import type {
  CreateUserRequest,
  LoginRequest,
  LoginResponse,
  UserResponse
} from '@/types/api'

export interface PublicConfig {
  allowUserCreation: boolean
}

export function fetchPublicConfig() {
  return apiRequest<PublicConfig>('/api/config')
}

export function login(payload: LoginRequest) {
  return apiRequest<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: payload
  })
}

export function logout() {
  return apiRequest<void>('/api/auth/logout', { method: 'POST' })
}

export function me() {
  return apiRequest<UserResponse>('/api/auth/me')
}

export function createUser(payload: CreateUserRequest) {
  return apiRequest<UserResponse>('/api/auth/users', {
    method: 'POST',
    body: payload
  })
}
