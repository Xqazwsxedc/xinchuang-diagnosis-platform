import http, { type PageResult } from '@/api/http'

export type NoticeType = 'SYSTEM' | 'ALERT' | 'TASK' | 'SECURITY'
export type NoticePriority = 'LOW' | 'NORMAL' | 'HIGH'
export type NoticeReadStatus = 'UNREAD' | 'READ'

export interface NotificationQuery {
  pageNo: number
  pageSize: number
  keyword?: string
  noticeType?: NoticeType | ''
  priority?: NoticePriority | ''
  readStatus?: NoticeReadStatus | ''
  startTime?: string
  endTime?: string
}

export interface NotificationItem {
  id: string
  receiverId: string
  receiverNameSnapshot?: string
  title: string
  content?: string
  noticeType: NoticeType
  priority: NoticePriority
  readStatus: NoticeReadStatus
  readAt?: string
  businessType?: string
  businessId?: string
  createdAt: string
}

export function getNotifications(params: NotificationQuery) {
  return http.get('/notifications', { params }) as unknown as Promise<PageResult<NotificationItem>>
}

export function getUnreadCount() {
  return http.get('/notifications/unread-count') as unknown as Promise<number>
}

export function getNotificationDetail(id: string) {
  return http.get(`/notifications/${id}`) as unknown as Promise<NotificationItem>
}

export function markNotificationRead(id: string) {
  return http.patch(`/notifications/${id}/read`) as unknown as Promise<void>
}

export function markAllNotificationsRead() {
  return http.patch('/notifications/read-all') as unknown as Promise<void>
}

export function deleteNotification(id: string) {
  return http.delete(`/notifications/${id}`) as unknown as Promise<void>
}

export function clearReadNotifications() {
  return http.delete('/notifications/read') as unknown as Promise<void>
}
