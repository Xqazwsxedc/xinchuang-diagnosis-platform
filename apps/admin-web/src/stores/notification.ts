import { defineStore } from 'pinia'
import { getUnreadCount } from '@/api/modules/notification'

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    unreadCount: 0
  }),
  actions: {
    async refreshUnreadCount() {
      this.unreadCount = await getUnreadCount()
    },
    clear() {
      this.unreadCount = 0
    }
  }
})
