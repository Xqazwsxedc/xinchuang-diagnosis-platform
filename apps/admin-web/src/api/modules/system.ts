import http, { type PageResult } from '@/api/http'

export type UserStatus = 'ENABLED' | 'DISABLED'
export type DataScope = 'ALL' | 'DEPARTMENT' | 'SELF' | 'CUSTOM'

export interface RoleItem {
  id: string
  roleCode: string
  roleName: string
  dataScope: DataScope
  dataScopeConfig?: string
  status: UserStatus
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface MenuItem {
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
  status: UserStatus
  remark?: string
  createdAt?: string
  updatedAt?: string
  children: MenuItem[]
}

export interface UserItem {
  id: string
  username: string
  realName: string
  avatarUrl?: string
  department?: string
  phone?: string
  email?: string
  status: UserStatus
  remark?: string
  roleIds: string[]
  roles: RoleItem[]
  createdAt?: string
  updatedAt?: string
}

export interface UserQuery {
  pageNo: number
  pageSize: number
  username?: string
  realName?: string
  status?: UserStatus | ''
}

export interface UserCreatePayload {
  username: string
  password: string
  realName: string
  avatarUrl?: string
  department?: string
  phone?: string
  email?: string
  status: UserStatus
  remark?: string
  roleIds: string[]
}

export interface UserUpdatePayload {
  realName: string
  avatarUrl?: string
  department?: string
  phone?: string
  email?: string
  status: UserStatus
  remark?: string
  roleIds: string[]
}

export interface RoleQuery {
  pageNo: number
  pageSize: number
  roleCode?: string
  roleName?: string
  status?: UserStatus | ''
}

export interface RolePayload {
  roleCode: string
  roleName: string
  dataScope: DataScope
  dataScopeConfig?: string
  status: UserStatus
  remark?: string
}

export type LogStatus = 'SUCCESS' | 'FAILED'

export interface LoginLogQuery {
  pageNo: number
  pageSize: number
  username?: string
  loginStatus?: LogStatus | ''
  startTime?: string
  endTime?: string
}

export interface OperationLogQuery {
  pageNo: number
  pageSize: number
  operatorName?: string
  moduleName?: string
  operationType?: string
  operationResult?: LogStatus | ''
  startTime?: string
  endTime?: string
}

export interface LoginLogItem {
  id: string
  userId?: string
  username: string
  loginTime: string
  loginIp?: string
  deviceInfo?: string
  loginStatus: LogStatus
  failureReason?: string
}

export interface OperationLogItem {
  id: string
  operatorId?: string
  operatorNameSnapshot?: string
  moduleName: string
  operationType: string
  targetType?: string
  targetId?: string
  operationContent?: string
  operationResult: LogStatus
  errorMessage?: string
  requestIp?: string
  userAgent?: string
  createdAt: string
}

export interface DictTypeItem {
  id: string
  dictCode: string
  dictName: string
  status: UserStatus
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface DictItem {
  id: string
  dictCode: string
  itemValue: string
  itemLabel: string
  tagType?: string
  sortOrder: number
  status: UserStatus
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface DictTypeQuery {
  pageNo: number
  pageSize: number
  dictCode?: string
  dictName?: string
  status?: UserStatus | ''
}

export interface DictItemQuery {
  pageNo: number
  pageSize: number
  dictCode?: string
  itemValue?: string
  itemLabel?: string
  status?: UserStatus | ''
}

export interface DictTypePayload {
  dictCode: string
  dictName: string
  status: UserStatus
  remark?: string
}

export interface DictItemPayload {
  dictCode: string
  itemValue: string
  itemLabel: string
  tagType?: string
  sortOrder: number
  status: UserStatus
  remark?: string
}

export type ConfigType = 'TEXT' | 'NUMBER' | 'BOOLEAN' | 'JSON'

export interface ConfigItem {
  id: string
  configKey: string
  configName: string
  configValue?: string
  configType: ConfigType
  status: UserStatus
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface ConfigQuery {
  pageNo: number
  pageSize: number
  configKey?: string
  configName?: string
  configType?: ConfigType | ''
  status?: UserStatus | ''
}

export interface ConfigPayload {
  configKey: string
  configName: string
  configValue?: string
  configType: ConfigType
  status: UserStatus
  remark?: string
}

export function getUsers(params: UserQuery) {
  return http.get('/system/users', { params }) as unknown as Promise<PageResult<UserItem>>
}

export function getUserDetail(id: string) {
  return http.get(`/system/users/${id}`) as unknown as Promise<UserItem>
}

export function createUser(data: UserCreatePayload) {
  return http.post('/system/users', data) as unknown as Promise<UserItem>
}

export function updateUser(id: string, data: UserUpdatePayload) {
  return http.put(`/system/users/${id}`, data) as unknown as Promise<UserItem>
}

export function updateUserStatus(id: string, status: UserStatus) {
  return http.patch(`/system/users/${id}/status`, { status }) as unknown as Promise<void>
}

export function resetUserPassword(id: string, password: string) {
  return http.patch(`/system/users/${id}/password`, { password }) as unknown as Promise<void>
}

export function deleteUser(id: string) {
  return http.delete(`/system/users/${id}`) as unknown as Promise<void>
}

export function getRoles() {
  return http.get('/system/users/roles') as unknown as Promise<RoleItem[]>
}

export function getRolePage(params: RoleQuery) {
  return http.get('/system/roles', { params }) as unknown as Promise<PageResult<RoleItem>>
}

export function getRoleDetail(id: string) {
  return http.get(`/system/roles/${id}`) as unknown as Promise<RoleItem>
}

export function createRole(data: RolePayload) {
  return http.post('/system/roles', data) as unknown as Promise<RoleItem>
}

export function updateRole(id: string, data: RolePayload) {
  return http.put(`/system/roles/${id}`, data) as unknown as Promise<RoleItem>
}

export function updateRoleStatus(id: string, status: UserStatus) {
  return http.patch(`/system/roles/${id}/status`, { status }) as unknown as Promise<void>
}

export function deleteRole(id: string) {
  return http.delete(`/system/roles/${id}`) as unknown as Promise<void>
}

export function getRoleMenuIds(id: string) {
  return http.get(`/system/roles/${id}/menus`) as unknown as Promise<string[]>
}

export function assignRoleMenus(id: string, menuIds: string[]) {
  return http.put(`/system/roles/${id}/menus`, { menuIds }) as unknown as Promise<void>
}

export function getMenuTree() {
  return http.get('/system/menus/tree') as unknown as Promise<MenuItem[]>
}

export function getMenuDetail(id: string) {
  return http.get(`/system/menus/${id}`) as unknown as Promise<MenuItem>
}

export interface MenuPayload {
  parentId: string
  menuType: 'MENU' | 'BUTTON'
  menuName: string
  permissionCode?: string
  routePath?: string
  componentPath?: string
  icon?: string
  sortOrder: number
  visible: number
  status: UserStatus
  remark?: string
}

export function createMenu(data: MenuPayload) {
  return http.post('/system/menus', data) as unknown as Promise<MenuItem>
}

export function updateMenu(id: string, data: MenuPayload) {
  return http.put(`/system/menus/${id}`, data) as unknown as Promise<MenuItem>
}

export function updateMenuStatus(id: string, status: UserStatus) {
  return http.patch(`/system/menus/${id}/status`, { status }) as unknown as Promise<void>
}

export function deleteMenu(id: string) {
  return http.delete(`/system/menus/${id}`) as unknown as Promise<void>
}

export function getLoginLogs(params: LoginLogQuery) {
  return http.get('/system/logs/login', { params }) as unknown as Promise<PageResult<LoginLogItem>>
}

export function getSecurityLoginLogs(params: LoginLogQuery) {
  return http.get('/system/security/login-logs', { params }) as unknown as Promise<PageResult<LoginLogItem>>
}

export function getOperationLogs(params: OperationLogQuery) {
  return http.get('/system/logs/operation', { params }) as unknown as Promise<PageResult<OperationLogItem>>
}

export function getDictTypes(params: DictTypeQuery) {
  return http.get('/system/dict/types', { params }) as unknown as Promise<PageResult<DictTypeItem>>
}

export function getDictTypeDetail(id: string) {
  return http.get(`/system/dict/types/${id}`) as unknown as Promise<DictTypeItem>
}

export function createDictType(data: DictTypePayload) {
  return http.post('/system/dict/types', data) as unknown as Promise<DictTypeItem>
}

export function updateDictType(id: string, data: DictTypePayload) {
  return http.put(`/system/dict/types/${id}`, data) as unknown as Promise<DictTypeItem>
}

export function updateDictTypeStatus(id: string, status: UserStatus) {
  return http.patch(`/system/dict/types/${id}/status`, { status }) as unknown as Promise<void>
}

export function deleteDictType(id: string) {
  return http.delete(`/system/dict/types/${id}`) as unknown as Promise<void>
}

export function getDictItems(params: DictItemQuery) {
  return http.get('/system/dict/items', { params }) as unknown as Promise<PageResult<DictItem>>
}

export function getEnabledDictItems(dictCode: string) {
  return http.get(`/system/dict/items/enabled/${dictCode}`) as unknown as Promise<DictItem[]>
}

export function getDictItemDetail(id: string) {
  return http.get(`/system/dict/items/${id}`) as unknown as Promise<DictItem>
}

export function createDictItem(data: DictItemPayload) {
  return http.post('/system/dict/items', data) as unknown as Promise<DictItem>
}

export function updateDictItem(id: string, data: DictItemPayload) {
  return http.put(`/system/dict/items/${id}`, data) as unknown as Promise<DictItem>
}

export function updateDictItemStatus(id: string, status: UserStatus) {
  return http.patch(`/system/dict/items/${id}/status`, { status }) as unknown as Promise<void>
}

export function deleteDictItem(id: string) {
  return http.delete(`/system/dict/items/${id}`) as unknown as Promise<void>
}

export function getConfigs(params: ConfigQuery) {
  return http.get('/system/configs', { params }) as unknown as Promise<PageResult<ConfigItem>>
}

export function getConfigDetail(id: string) {
  return http.get(`/system/configs/${id}`) as unknown as Promise<ConfigItem>
}

export function getConfigByKey(configKey: string) {
  return http.get(`/system/configs/key/${configKey}`) as unknown as Promise<ConfigItem>
}

export function createConfig(data: ConfigPayload) {
  return http.post('/system/configs', data) as unknown as Promise<ConfigItem>
}

export function updateConfig(id: string, data: ConfigPayload) {
  return http.put(`/system/configs/${id}`, data) as unknown as Promise<ConfigItem>
}

export function updateConfigStatus(id: string, status: UserStatus) {
  return http.patch(`/system/configs/${id}/status`, { status }) as unknown as Promise<void>
}

export function deleteConfig(id: string) {
  return http.delete(`/system/configs/${id}`) as unknown as Promise<void>
}
