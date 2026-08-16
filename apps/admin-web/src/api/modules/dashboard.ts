import http from '@/api/http'
import type { LedgerStatus } from '@/api/modules/ledger'

export interface StatusDistributionItem {
  status: LedgerStatus
  label: string
  count: number
}

export interface TrendPoint {
  time: string
  businessSystemCount: number
  databaseCount: number
  abnormalCount: number
}

export interface AlertItem {
  sourceType: 'BUSINESS_SYSTEM' | 'DB_RESOURCE'
  sourceId: string
  sourceName: string
  level: LedgerStatus
  message: string
  updatedAt?: string
}

export interface DashboardOverview {
  businessSystemCount: number
  databaseCount: number
  normalCount: number
  abnormalCount: number
  healthScore: number
  statusDistribution: StatusDistributionItem[]
  trend: TrendPoint[]
  alerts: AlertItem[]
}

export function getDashboardOverview() {
  return http.get('/dashboard/overview') as unknown as Promise<DashboardOverview>
}
