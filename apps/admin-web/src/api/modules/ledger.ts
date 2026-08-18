import axios from 'axios'
import http, { type PageResult } from '@/api/http'

export interface LedgerQuery {
  pageNo: number
  pageSize: number
  keyword?: string
  name?: string
  ownerName?: string
  status?: LedgerStatus | ''
}

export type LedgerStatus = 'NORMAL' | 'WARNING' | 'DANGER' | 'OFFLINE'

export interface BusinessSystemItem {
  id: string
  name: string
  databaseId: string
  databaseName?: string
  databaseIds?: string[]
  databaseNames?: string[]
  ownerName: string
  status: LedgerStatus
  accessUrl?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface DbResourceItem {
  id: string
  name: string
  publicIp: string
  privateIp?: string
  ownerName: string
  status: LedgerStatus
  monitoringStatus?: 'CONNECTED' | 'NOT_CONNECTED'
  createdAt?: string
  updatedAt?: string
}

export interface DbMonitoringConfig {
  prometheusBaseUrl?: string
  credentialsConfigured?: boolean
  nodeTargetSelector?: Record<string, string>
  databaseTargetSelector?: Record<string, string>
  databaseType?: string
  status: 'UNCONFIGURED' | 'TESTING' | 'HEALTHY' | 'ABNORMAL'
  lastCheckedAt?: string
  lastErrorMessage?: string
}

export interface BusinessSystemPayload {
  name: string
  databaseId: string
  databaseIds: string[]
  ownerName: string
  status: LedgerStatus
  accessUrl?: string
  remark?: string
}

export interface DbResourcePayload {
  name: string
  publicIp: string
  privateIp?: string
  ownerName: string
  status: LedgerStatus
}

export type MetricRange = '1H' | '3H' | '6H' | '12H' | '1D' | '3D' | '7D' | '14D'

export interface BusinessDataRecordItem {
  id: string
  collectedAt: string
  totalSizeBytes: number
  collectStatus: 'SUCCESS' | 'FAILED'
  fileName: string
  recordSummary?: string
}

export interface DbMetricPoint {
  sampledAt: string
  cpuUsage?: number
  memoryUsage?: number
  diskIops?: number
  currentConnections?: number
  cacheHitRate?: number
}

export interface DbMetricsOverview {
  cpuUsage?: number
  memoryUsage?: number
  diskIops?: number
  currentConnections?: number
  cacheHitRate?: number
  points: DbMetricPoint[]
}

export interface DbInstanceLogItem {
  id: string
  loggedAt: string
  logLevel: 'INFO' | 'WARN' | 'ERROR'
  logContent: string
}

export interface LedgerDownload {
  fileName: string
  blob: Blob
}

export function getBusinessSystems(params: LedgerQuery) {
  return http.get('/ledger/business-systems', { params }) as unknown as Promise<PageResult<BusinessSystemItem>>
}

export function getBusinessSystemDetail(id: string) {
  return http.get(`/ledger/business-systems/${id}`) as unknown as Promise<BusinessSystemItem>
}

export function createBusinessSystem(data: BusinessSystemPayload) {
  return http.post('/ledger/business-systems', data) as unknown as Promise<BusinessSystemItem>
}

export function updateBusinessSystem(id: string, data: BusinessSystemPayload) {
  return http.put(`/ledger/business-systems/${id}`, data) as unknown as Promise<BusinessSystemItem>
}

export function deleteBusinessSystem(id: string) {
  return http.delete(`/ledger/business-systems/${id}`) as unknown as Promise<void>
}

export function getBusinessDataRecords(id: string) {
  return http.get(`/ledger/business-systems/${id}/data-records`) as unknown as Promise<BusinessDataRecordItem[]>
}

export function getDbResources(params: LedgerQuery) {
  return http.get('/ledger/db-resources', { params }) as unknown as Promise<PageResult<DbResourceItem>>
}

export function getDbResourceDetail(id: string) {
  return http.get(`/ledger/db-resources/${id}`) as unknown as Promise<DbResourceItem>
}

export function createDbResource(data: DbResourcePayload) {
  return http.post('/ledger/db-resources', data) as unknown as Promise<DbResourceItem>
}

export function updateDbResource(id: string, data: DbResourcePayload) {
  return http.put(`/ledger/db-resources/${id}`, data) as unknown as Promise<DbResourceItem>
}

export function deleteDbResource(id: string) {
  return http.delete(`/ledger/db-resources/${id}`) as unknown as Promise<void>
}

export function getDbMonitoringConfig(id: string) {
  return http.get(`/ledger/db-resources/${id}/monitoring-config`) as unknown as Promise<DbMonitoringConfig>
}

export function saveDbMonitoringConfig(id: string, data: {
  prometheusBaseUrl: string
  prometheusAuthRef?: string
  nodeTargetSelector: Record<string, string>
  databaseTargetSelector: Record<string, string>
  databaseType: string
}) {
  return http.put(`/ledger/db-resources/${id}/monitoring-config`, data) as unknown as Promise<DbMonitoringConfig>
}

export function testDbMonitoringConfig(id: string) {
  return http.post(`/ledger/db-resources/${id}/monitoring-config/test`) as unknown as Promise<DbMonitoringConfig>
}

export function getDbMetrics(id: string, range: MetricRange) {
  return http.get(`/ledger/db-resources/${id}/metrics`, { params: { range } }) as unknown as Promise<DbMetricsOverview>
}

export function getDbInstanceLogs(id: string, params: { pageNo: number; pageSize: number; range: MetricRange; logLevel?: string }) {
  return http.get(`/ledger/db-resources/${id}/logs`, { params }) as unknown as Promise<PageResult<DbInstanceLogItem>>
}

export async function downloadLedgerFile(path: string, params?: Record<string, string | number | undefined>): Promise<LedgerDownload> {
  const response = await axios.get(path, {
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    params,
    responseType: 'blob',
    headers: { Authorization: `Bearer ${localStorage.getItem('token') || ''}` }
  })
  if (String(response.headers['content-type'] || '').includes('application/json')) {
    const errorBody = JSON.parse(await (response.data as Blob).text()) as { message?: string }
    throw new Error(errorBody.message || '下载失败')
  }
  const disposition = response.headers['content-disposition'] || ''
  const encodedFileName = disposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1]
  return {
    fileName: encodedFileName ? decodeURIComponent(encodedFileName) : 'ledger-export.csv',
    blob: response.data
  }
}
