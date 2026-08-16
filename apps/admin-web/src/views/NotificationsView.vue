<template>
  <section class="page notification-page">
    <div class="page-header">
      <div>
        <h1>消息通知</h1>
        <p>查看系统通知、告警通知和安全提醒，及时处理平台运行过程中的关键消息。</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Check" @click="handleReadAll">全部已读</el-button>
        <el-button :icon="Delete" type="danger" plain @click="handleClearRead">清空已读</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query" class="notification-filter">
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable placeholder="标题或内容" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.noticeType" clearable placeholder="全部类型" style="width: 140px">
            <el-option label="系统通知" value="SYSTEM" />
            <el-option label="告警通知" value="ALERT" />
            <el-option label="任务通知" value="TASK" />
            <el-option label="安全通知" value="SECURITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.readStatus" clearable placeholder="全部状态" style="width: 130px">
            <el-option label="未读" value="UNREAD" />
            <el-option label="已读" value="READ" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="query.priority" clearable placeholder="全部优先级" style="width: 140px">
            <el-option label="低" value="LOW" />
            <el-option label="普通" value="NORMAL" />
            <el-option label="高" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="notification-list-card">
      <el-empty v-if="!loading && records.length === 0" description="暂无通知" />
      <div v-else v-loading="loading" class="notification-list">
        <article v-for="item in records" :key="item.id" class="notification-card" :class="{ unread: item.readStatus === 'UNREAD' }">
          <div class="notification-main" @click="openDetail(item)">
            <div class="notification-title">
              <el-badge is-dot :hidden="item.readStatus === 'READ'" />
              <el-tag :type="noticeTypeTag(item.noticeType)" round>{{ noticeTypeText(item.noticeType) }}</el-tag>
              <strong>{{ item.title }}</strong>
              <el-tag :type="priorityTag(item.priority)" size="small" round>{{ priorityText(item.priority) }}</el-tag>
            </div>
            <p>{{ item.content || '-' }}</p>
            <span>{{ formatTime(item.createdAt) }}</span>
          </div>
          <div class="notification-actions">
            <el-button v-if="item.readStatus === 'UNREAD'" type="primary" link :icon="Check" @click="handleRead(item)">已读</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(item)">删除</el-button>
          </div>
        </article>
      </div>

      <div class="table-footer">
        <el-pagination
          v-model:current-page="query.pageNo"
          v-model:page-size="query.pageSize"
          layout="total, sizes, prev, pager, next"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          @change="loadData"
        />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="通知详情" size="520px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="noticeTypeTag(detail.noticeType)" round>{{ noticeTypeText(detail.noticeType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="priorityTag(detail.priority)" round>{{ priorityText(detail.priority) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="阅读状态">{{ detail.readStatus === 'UNREAD' ? '未读' : '已读' }}</el-descriptions-item>
        <el-descriptions-item label="通知时间">{{ formatTime(detail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="阅读时间">{{ formatTime(detail.readAt) }}</el-descriptions-item>
        <el-descriptions-item label="关联业务">{{ detail.businessType || '-' }} {{ detail.businessId || '' }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ detail.content || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Check, Delete, RefreshLeft, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  clearReadNotifications,
  deleteNotification,
  getNotificationDetail,
  getNotifications,
  markAllNotificationsRead,
  markNotificationRead,
  type NotificationItem,
  type NoticePriority,
  type NoticeReadStatus,
  type NoticeType
} from '@/api/modules/notification'
import { useNotificationStore } from '@/stores/notification'

const loading = ref(false)
const notificationStore = useNotificationStore()
const records = ref<NotificationItem[]>([])
const total = ref(0)
const detailVisible = ref(false)
const detail = ref<NotificationItem | null>(null)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  noticeType: '' as NoticeType | '',
  priority: '' as NoticePriority | '',
  readStatus: '' as NoticeReadStatus | ''
})

async function loadData() {
  loading.value = true
  try {
    const page = await getNotifications({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      noticeType: query.noticeType || undefined,
      priority: query.priority || undefined,
      readStatus: query.readStatus || undefined
    })
    records.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNo = 1
  loadData()
}

function reset() {
  query.keyword = ''
  query.noticeType = ''
  query.priority = ''
  query.readStatus = ''
  query.pageNo = 1
  loadData()
}

async function openDetail(item: NotificationItem) {
  detail.value = await getNotificationDetail(item.id)
  detailVisible.value = true
  if (item.readStatus === 'UNREAD') {
    await markNotificationRead(item.id)
    await loadData()
    await notificationStore.refreshUnreadCount()
  }
}

async function handleRead(item: NotificationItem) {
  await markNotificationRead(item.id)
  ElMessage.success('已标记为已读')
  await loadData()
  await notificationStore.refreshUnreadCount()
}

async function handleReadAll() {
  await markAllNotificationsRead()
  ElMessage.success('已全部标记为已读')
  await loadData()
  await notificationStore.refreshUnreadCount()
}

async function handleDelete(item: NotificationItem) {
  await ElMessageBox.confirm(`确定要删除通知「${item.title}」吗？`, '确认删除', { type: 'warning' })
  await deleteNotification(item.id)
  ElMessage.success('删除成功')
  await loadData()
  await notificationStore.refreshUnreadCount()
}

async function handleClearRead() {
  await ElMessageBox.confirm('确定要清空所有已读通知吗？', '确认清空', { type: 'warning' })
  await clearReadNotifications()
  ElMessage.success('已清空已读通知')
  await loadData()
  await notificationStore.refreshUnreadCount()
}

function noticeTypeText(type: NoticeType) {
  const map: Record<NoticeType, string> = { SYSTEM: '系统通知', ALERT: '告警通知', TASK: '任务通知', SECURITY: '安全通知' }
  return map[type] || type
}

function noticeTypeTag(type: NoticeType) {
  const map: Record<NoticeType, 'primary' | 'danger' | 'warning' | 'info'> = { SYSTEM: 'primary', ALERT: 'danger', TASK: 'warning', SECURITY: 'info' }
  return map[type] || 'info'
}

function priorityText(priority: NoticePriority) {
  const map: Record<NoticePriority, string> = { LOW: '低', NORMAL: '普通', HIGH: '高' }
  return map[priority] || priority
}

function priorityTag(priority: NoticePriority) {
  const map: Record<NoticePriority, 'info' | 'primary' | 'danger'> = { LOW: 'info', NORMAL: 'primary', HIGH: 'danger' }
  return map[priority] || 'info'
}

function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

onMounted(async () => {
  await loadData()
  await notificationStore.refreshUnreadCount()
})
</script>

<style scoped>
.page-actions {
  display: flex;
  gap: 12px;
}

.notification-filter :deep(.el-form-item) {
  margin-bottom: 0;
}

.notification-list-card {
  min-height: 560px;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 360px;
}

.notification-card {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: #fff;
}

.notification-card.unread {
  border-color: #a8d0ff;
  background: #f7fbff;
}

.notification-main {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.notification-title strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-main p {
  margin: 0 0 8px;
  color: var(--app-text-secondary);
}

.notification-main span {
  color: var(--app-text-secondary);
  font-size: 13px;
}

.notification-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  gap: 8px;
}
</style>
