import { defineStore } from 'pinia'
import type { AuthMenuItem } from '@/api/modules/auth'

interface UserState {
  token: string
  id: string
  username: string
  realName: string
  avatarUrl: string
  department: string
  phone: string
  email: string
  roles: string[]
  menus: AuthMenuItem[]
  permissions: string[]
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('token') || '',
    id: localStorage.getItem('userId') || '',
    username: localStorage.getItem('username') || '',
    realName: localStorage.getItem('realName') || '',
    avatarUrl: localStorage.getItem('avatarUrl') || '',
    department: localStorage.getItem('department') || '',
    phone: localStorage.getItem('phone') || '',
    email: localStorage.getItem('email') || '',
    roles: JSON.parse(localStorage.getItem('roles') || '[]'),
    menus: JSON.parse(localStorage.getItem('menus') || '[]'),
    permissions: JSON.parse(localStorage.getItem('permissions') || '[]')
  }),
  getters: {
    hasAnyRole: (state) => (roles: string[]) => roles.some((role) => state.roles.includes(role)),
    hasPermission: (state) => (permission: string) => state.roles.includes('admin') || state.permissions.includes(permission),
    hasAnyPermission: (state) => (permissions: string[]) => state.roles.includes('admin') || permissions.some((permission) => state.permissions.includes(permission))
  },
  actions: {
    setSession(payload: UserState) {
      this.token = payload.token
      this.id = payload.id
      this.username = payload.username
      this.realName = payload.realName
      this.avatarUrl = payload.avatarUrl
      this.department = payload.department
      this.phone = payload.phone
      this.email = payload.email
      this.roles = payload.roles
      this.menus = payload.menus
      this.permissions = payload.permissions
      localStorage.setItem('token', payload.token)
      localStorage.setItem('userId', payload.id)
      localStorage.setItem('username', payload.username)
      localStorage.setItem('realName', payload.realName)
      localStorage.setItem('avatarUrl', payload.avatarUrl)
      localStorage.setItem('department', payload.department)
      localStorage.setItem('phone', payload.phone)
      localStorage.setItem('email', payload.email)
      localStorage.setItem('roles', JSON.stringify(payload.roles))
      localStorage.setItem('menus', JSON.stringify(payload.menus))
      localStorage.setItem('permissions', JSON.stringify(payload.permissions))
    },
    setUser(payload: Omit<UserState, 'token'>) {
      this.id = payload.id
      this.username = payload.username
      this.realName = payload.realName
      this.avatarUrl = payload.avatarUrl
      this.department = payload.department
      this.phone = payload.phone
      this.email = payload.email
      this.roles = payload.roles
      this.menus = payload.menus
      this.permissions = payload.permissions
      localStorage.setItem('userId', payload.id)
      localStorage.setItem('username', payload.username)
      localStorage.setItem('realName', payload.realName)
      localStorage.setItem('avatarUrl', payload.avatarUrl)
      localStorage.setItem('department', payload.department)
      localStorage.setItem('phone', payload.phone)
      localStorage.setItem('email', payload.email)
      localStorage.setItem('roles', JSON.stringify(payload.roles))
      localStorage.setItem('menus', JSON.stringify(payload.menus))
      localStorage.setItem('permissions', JSON.stringify(payload.permissions))
    },
    logout() {
      this.token = ''
      this.id = ''
      this.username = ''
      this.realName = ''
      this.avatarUrl = ''
      this.department = ''
      this.phone = ''
      this.email = ''
      this.roles = []
      this.menus = []
      this.permissions = []
      localStorage.clear()
    }
  }
})
