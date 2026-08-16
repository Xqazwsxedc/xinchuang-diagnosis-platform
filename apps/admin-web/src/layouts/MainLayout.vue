<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <div class="brand">
        <el-icon class="brand-icon"><Coin /></el-icon>
        <span>信创数据库智能故障诊断及修复平台</span>
      </div>
      <div class="header-actions">
        <el-input class="global-search" placeholder="全局搜索" clearable />
        <el-badge v-if="notificationStore.unreadCount > 0" :value="notificationBadgeValue" class="notification-badge">
          <el-button :icon="Bell" circle @click="$router.push('/notifications')" />
        </el-badge>
        <el-button v-else :icon="Bell" circle @click="$router.push('/notifications')" />
        <el-dropdown>
          <el-button class="user-button">
            <el-avatar :src="userStore.avatarUrl" :size="24">{{ userInitial }}</el-avatar>
            {{ userStore.realName || '管理员' }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="app-body">
      <el-aside width="240px" class="app-sidebar">
        <el-menu :default-active="$route.path" router class="side-menu">
          <DynamicMenuItem v-for="menu in visibleMenus" :key="menu.id" :menu="menu" />
        </el-menu>
      </el-aside>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import {
  ArrowDown,
  Bell,
  Coin
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'
import { getCurrentUser, logoutApi } from '@/api/modules/auth'
import { computed, onMounted } from 'vue'
import DynamicMenuItem from '@/components/DynamicMenuItem.vue'

const router = useRouter()
const userStore = useUserStore()
const notificationStore = useNotificationStore()
const visibleMenus = computed(() =>
  userStore.menus.filter((item) => item.menuType === 'MENU' && item.visible === 1 && item.status === 'ENABLED')
)
const notificationBadgeValue = computed(() => notificationStore.unreadCount > 99 ? '99+' : notificationStore.unreadCount)
const userInitial = computed(() => (userStore.realName || userStore.username || '管').slice(0, 1))

async function handleLogout() {
  await logoutApi().catch(() => undefined)
  notificationStore.clear()
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  refreshCurrentSession()
  notificationStore.refreshUnreadCount().catch(() => undefined)
})

async function refreshCurrentSession() {
  try {
    const user = await getCurrentUser()
    userStore.setUser({
      id: user.id,
      username: user.username,
      realName: user.realName,
      avatarUrl: user.avatarUrl || '',
      department: user.department || '',
      phone: user.phone || '',
      email: user.email || '',
      roles: user.roles,
      menus: user.menus,
      permissions: user.permissions
    })
  } catch {
    notificationStore.clear()
    userStore.logout()
    router.push('/login')
  }
}
</script>
