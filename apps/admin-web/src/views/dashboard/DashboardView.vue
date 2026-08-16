<template>
  <section class="dashboard-page" v-loading="loading">
    <div class="dashboard-header">
      <div>
        <h1>数据大屏</h1>
        <p>汇总业务系统、分布式数据库、异常分布与实时告警，支撑信创数据库运行态势研判。</p>
      </div>
      <div class="dashboard-actions">
        <el-switch v-model="autoRefresh" active-text="自动刷新" />
        <el-button type="primary" :icon="Refresh" @click="loadData">刷新</el-button>
      </div>
    </div>

    <div class="dashboard-grid">
      <div class="screen-card">
        <span>健康评分</span>
        <strong :class="overview.healthScore >= 80 ? 'success' : 'warning'">{{ overview.healthScore }}</strong>
      </div>
      <div class="screen-card">
        <span>业务系统数量</span>
        <strong>{{ overview.businessSystemCount }}</strong>
      </div>
      <div class="screen-card">
        <span>分布式数据库数量</span>
        <strong>{{ overview.databaseCount }}</strong>
      </div>
      <div class="screen-card">
        <span>异常数量</span>
        <strong class="warning">{{ overview.abnormalCount }}</strong>
      </div>
    </div>

    <div class="dashboard-content">
      <div class="screen-panel trend-panel">
        <div class="panel-title">
          <h2>近 24 小时趋势</h2>
          <span>业务系统 / 数据库 / 异常</span>
        </div>
        <div class="trend-chart">
          <div v-for="point in overview.trend" :key="point.time" class="trend-column">
            <div class="trend-bars">
              <span class="bar business" :style="{ height: barHeight(point.businessSystemCount) }" />
              <span class="bar database" :style="{ height: barHeight(point.databaseCount) }" />
              <span class="bar abnormal" :style="{ height: barHeight(point.abnormalCount) }" />
            </div>
            <small>{{ point.time }}</small>
          </div>
        </div>
        <div class="legend">
          <span><i class="business-dot" />业务系统</span>
          <span><i class="database-dot" />数据库</span>
          <span><i class="abnormal-dot" />异常</span>
        </div>
      </div>

      <div class="screen-panel status-panel">
        <div class="panel-title">
          <h2>状态分布</h2>
          <span>当前资源健康分层</span>
        </div>
        <div class="status-list">
          <div v-for="item in overview.statusDistribution" :key="item.status" class="status-item">
            <div class="status-meta">
              <el-tag :type="statusType(item.status)" round>{{ item.label }}</el-tag>
              <strong>{{ item.count }}</strong>
            </div>
            <el-progress :percentage="statusPercent(item.count)" :show-text="false" :stroke-width="8" />
          </div>
        </div>
      </div>
    </div>

    <div class="screen-panel alert-panel">
      <div class="panel-title">
        <h2>实时告警列表</h2>
        <span>来自业务系统和分布式数据库台账状态</span>
      </div>
      <el-empty v-if="overview.alerts.length === 0" description="暂无告警" />
      <div v-else class="alert-list">
        <div v-for="item in overview.alerts" :key="`${item.sourceType}-${item.sourceId}`" class="alert-row">
          <el-tag :type="statusType(item.level)" round>{{ statusText(item.level) }}</el-tag>
          <div>
            <strong>{{ item.sourceName }}</strong>
            <p>{{ item.message }}</p>
          </div>
          <span>{{ formatTime(item.updatedAt) }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getDashboardOverview, type DashboardOverview } from '@/api/modules/dashboard'
import type { LedgerStatus } from '@/api/modules/ledger'

const loading = ref(false)
const autoRefresh = ref(false)
let timer: number | undefined

const overview = reactive<DashboardOverview>({
  businessSystemCount: 0,
  databaseCount: 0,
  normalCount: 0,
  abnormalCount: 0,
  healthScore: 100,
  statusDistribution: [],
  trend: [],
  alerts: []
})

const maxTrendValue = computed(() => {
  const values = overview.trend.flatMap((item) => [item.businessSystemCount, item.databaseCount, item.abnormalCount])
  return Math.max(1, ...values)
})

const totalStatusCount = computed(() => overview.statusDistribution.reduce((sum, item) => sum + item.count, 0))

async function loadData() {
  loading.value = true
  try {
    const data = await getDashboardOverview()
    Object.assign(overview, data)
  } finally {
    loading.value = false
  }
}

function barHeight(value: number) {
  return `${Math.max(8, Math.round((value / maxTrendValue.value) * 120))}px`
}

function statusPercent(count: number) {
  if (totalStatusCount.value === 0) return 0
  return Math.round((count / totalStatusCount.value) * 100)
}

function statusType(status?: string) {
  if (status === 'NORMAL') return 'success'
  if (status === 'WARNING') return 'warning'
  if (status === 'OFFLINE') return 'info'
  return 'danger'
}

function statusText(status?: LedgerStatus) {
  const map: Record<string, string> = { NORMAL: '正常', WARNING: '警告', DANGER: '危险', OFFLINE: '离线' }
  return status ? map[status] || status : '-'
}

function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

watch(autoRefresh, (enabled) => {
  if (timer) window.clearInterval(timer)
  timer = enabled ? window.setInterval(loadData, 30000) : undefined
})

onMounted(loadData)

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
.dashboard-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.dashboard-actions :deep(.el-switch__label) {
  color: rgba(255, 255, 255, 0.88);
  font-weight: 600;
}

.dashboard-actions :deep(.el-switch__label.is-active) {
  color: #ffffff;
}
</style>
