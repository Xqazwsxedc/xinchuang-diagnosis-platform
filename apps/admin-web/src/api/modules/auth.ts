import http from '@/api/http'

export interface AuthMenuItem {
  id: string
  parentId: string
  menuType: 'MENU' | 'BUTTON'
  menuName: string
  permissionCode?: string
  routePath?: string
  componentPath?: string
  icon?: string
  sortOrder: number
  visible: number
  status: 'ENABLED' | 'DISABLED'
  children: AuthMenuItem[]
}

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  user: LoginUser
}

export interface LoginUser {
  id: string
  username: string
  realName: string
  avatarUrl?: string
  department?: string
  phone?: string
  email?: string
  status: 'ENABLED' | 'DISABLED'
  roles: string[]
  menus: AuthMenuItem[]
  permissions: string[]
}

export function loginApi(payload: LoginPayload) {
  return http.post('/auth/login', payload) as unknown as Promise<LoginResult>
}

export function getCurrentUser() {
  return http.get('/auth/me') as unknown as Promise<LoginUser>
}

export function logoutApi() {
  return http.post('/auth/logout') as unknown as Promise<void>
}

export interface ProfilePayload {
  realName: string
  avatarUrl?: string
  department?: string
  phone?: string
  email?: string
}

export interface ChangePasswordPayload {
  oldPassword: string
  newPassword: string
}

export function getProfile() {
  return http.get('/auth/profile') as unknown as Promise<ProfilePayload & { id: string; username: string; roles: unknown[] }>
}

export function updateProfile(data: ProfilePayload) {
  return http.put('/auth/profile', data) as unknown as Promise<ProfilePayload & { id: string; username: string; roles: unknown[] }>
}

export function changePassword(data: ChangePasswordPayload) {
  return http.patch('/auth/password', data) as unknown as Promise<void>
}
