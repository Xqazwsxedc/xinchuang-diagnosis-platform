import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/layouts/MainLayout.vue'
import { FALLBACK_ROUTE, resolveHomeRoute } from '@/utils/navigation'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue')
  },
  {
    path: '/',
    component: MainLayout,
    redirect: () => {
      const userStore = useUserStore()
      return resolveHomeRoute(userStore.menus)
    },
    children: [
      {
        path: 'data-ledger/visualization',
        name: 'DataLedger',
        component: () => import('@/views/data-ledger/DataLedgerView.vue'),
        meta: { title: '数据可视化台账', permission: 'ledger:visualization:view' }
      },
      {
        path: 'data-ledger/business-systems/:id',
        name: 'BusinessSystemDetail',
        component: () => import('@/views/data-ledger/BusinessSystemDetailView.vue'),
        meta: { title: '业务系统详情', permission: 'ledger:visualization:view' }
      },
      {
        path: 'data-ledger/db-resources/:id',
        name: 'DbResourceDetail',
        component: () => import('@/views/data-ledger/DbResourceDetailView.vue'),
        meta: { title: '分布式数据库详情', permission: 'ledger:visualization:view' }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '数据大屏', permission: 'dashboard:view' }
      },
      {
        path: 'fault-prediction/knowledge',
        name: 'FaultKnowledge',
        component: () => import('@/views/ai/FaultKnowledgeView.vue'),
        meta: { title: '故障知识库管理', permission: 'fault-prediction:knowledge:view' }
      },
      {
        path: 'fault-prediction/task',
        name: 'FaultTask',
        component: () => import('@/views/ai/FaultTaskConfigView.vue'),
        meta: { title: '故障预测任务配置', permission: 'fault-prediction:task:view' }
      },
      {
        path: 'fault-prediction/alert',
        name: 'FaultAlert',
        component: () => import('@/views/ai/FaultAlertView.vue'),
        meta: { title: '故障智能预警', permission: 'fault-prediction:alert:view' }
      },
        {
          path: 'defect-detection/knowledge',
          name: 'DefectKnowledge',
          component: () => import('@/views/ai/DefectKnowledgeView.vue'),
          meta: { title: '缺陷知识库管理', permission: 'defect-detection:knowledge:view' }
        },
        {
          path: 'defect-detection/task',
          name: 'DefectTask',
          component: () => import('@/views/ai/DefectTaskConfigView.vue'),
          meta: { title: '缺陷检测任务配置', permission: 'defect-detection:task:view' }
        },
      {
        path: 'defect-detection/report',
        name: 'DefectReport',
        component: () => import('@/views/ai/DefectReportView.vue'),
        meta: { title: '缺陷检测报表', permission: 'defect-detection:report:view' }
      },
      {
        path: 'performance/knowledge',
        name: 'OptimizationKnowledge',
        component: () => import('@/views/ai/OptimizationKnowledgeView.vue'),
        meta: { title: '优化知识库管理', permission: 'performance:knowledge:view' }
      },
      {
        path: 'performance/task',
        name: 'PerformanceTask',
        component: () => import('@/views/ai/OptimizationTaskConfigView.vue'),
        meta: { title: '性能优化任务配置', permission: 'performance:task:view' }
      },
      {
        path: 'performance/suggestion',
        name: 'OptimizationSuggestion',
        component: () => import('@/views/ai/OptimizationSuggestionView.vue'),
        meta: { title: '性能优化建议', permission: 'performance:suggestion:view' }
      },
      {
        path: 'system/users',
        name: 'SystemUsers',
        component: () => import('@/views/system/UserManagementView.vue'),
        meta: { title: '用户账号管理', permission: 'system:user:view' }
      },
      {
        path: 'system/roles',
        name: 'SystemRoles',
        component: () => import('@/views/system/RolePermissionView.vue'),
        meta: { title: '角色权限管理', permission: 'system:role:view' }
      },
      {
        path: 'system/menus',
        name: 'SystemMenus',
        component: () => import('@/views/system/MenuManagementView.vue'),
        meta: { title: '菜单权限管理', permission: 'system:menu:view' }
      },
      {
        path: 'system/logs',
        name: 'SystemLogs',
        component: () => import('@/views/system/AuditLogView.vue'),
        meta: { title: '日志审计', permission: 'system:audit-log:view' }
      },
      {
        path: 'system/audit',
        name: 'SystemAudit',
        component: () => import('@/views/system/AuditLogView.vue'),
        meta: { title: '日志审计管理', permission: 'system:audit-log:view' }
      },
      {
        path: 'system/dicts',
        name: 'SystemDicts',
        component: () => import('@/views/system/DictManagementView.vue'),
        meta: { title: '字典管理', permission: 'system:dict:view' }
      },
      {
        path: 'system/configs',
        name: 'SystemConfigs',
        component: () => import('@/views/system/ConfigManagementView.vue'),
        meta: { title: '系统配置', permission: 'system:config:view' }
      },
      {
        path: 'system/config',
        name: 'SystemBaseConfig',
        component: () => import('@/views/system/SystemBaseConfigView.vue'),
        meta: { title: '系统基础配置', permission: 'system:config:view' }
      },
      {
        path: 'system/security',
        name: 'SystemSecurity',
        component: () => import('@/views/system/SecurityLoginControlView.vue'),
        meta: { title: '安全登录管控', permission: 'system:security:view' }
      },
      {
        path: 'system/:page',
        name: 'System',
        component: () => import('@/views/PlaceholderView.vue'),
        meta: { title: '系统管理', permission: 'system:view' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/ProfileView.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/NotificationsView.vue'),
        meta: { title: '消息通知' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.token) {
    return '/login'
  }
  if (to.path === '/login' && userStore.token) {
    return resolveHomeRoute(userStore.menus)
  }
  const permission = to.meta.permission as string | undefined
  if (permission && !userStore.hasPermission(permission)) {
    const homeRoute = resolveHomeRoute(userStore.menus)
    return homeRoute !== to.path ? homeRoute : FALLBACK_ROUTE
  }
  return true
})

export default router
