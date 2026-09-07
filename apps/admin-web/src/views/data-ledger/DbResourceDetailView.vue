<template>
  <section class="db-detail-page" v-loading="loading">
    <button class="detail-back" type="button" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      <span>{{ resource?.name || '分布式数据库详情' }}</span>
    </button>

    <template v-if="resource">
      <section class="monitor-config-panel">
        <div><span>Prometheus 监控</span><strong>{{ monitoringStatusText(monitoring.status) }}</strong><small v-if="monitoring.lastErrorMessage">{{ monitoring.lastErrorMessage }}</small><small v-else-if="monitoring.lastCheckedAt">最近检查：{{ formatTime(monitoring.lastCheckedAt) }}</small></div>
        <div class="monitor-config-actions"><el-button v-if="canManageMonitoring" @click="openMonitoringConfig">监控配置</el-button><el-button v-if="canManageMonitoring && monitoring.prometheusBaseUrl" type="primary" :loading="monitoringTesting" @click="testMonitoring">测试连接</el-button></div>
      </section>
      <section class="detail-summary db-summary">
        <div class="summary-item"><span>服务器名称</span><strong>{{ resource.name }}</strong></div>
        <div class="summary-item"><span>公网IP</span><strong>{{ resource.publicIp }}</strong></div>
        <div class="summary-item"><span>内网IP</span><strong>{{ resource.privateIp || '-' }}</strong></div>
        <div class="summary-item"><span>创建人</span><strong>{{ resource.ownerName }}</strong></div>
      </section>

      <section class="metric-cards">
        <article class="metric-card"><span>CPU利用率</span><strong>{{ displayNumber(metrics.cpuUsage) }}<small>%</small></strong></article>
        <article class="metric-card"><span>内存使用占比</span><strong>{{ displayNumber(metrics.memoryUsage) }}<small>%</small></strong></article>
        <article class="metric-card"><span>磁盘IO</span><strong>{{ metrics.diskIops ?? '-' }}<small v-if="metrics.diskIops !== undefined">IOPS</small></strong></article>
        <article class="metric-card"><span>当前连接数</span><strong>{{ metrics.currentConnections ?? '-' }}<small v-if="metrics.currentConnections !== undefined">个</small></strong></article>
      </section>

      <section class="detail-panel db-content-panel">
        <div class="panel-toolbar tab-toolbar">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="服务器指标" name="metrics" />
            <el-tab-pane label="服务器日志" name="logs" />
          </el-tabs>
          <el-button
            type="primary"
            :icon="Download"
            :disabled="activeTab === 'logs' && selectedLogIds.length === 0"
            :loading="downloading"
            @click="downloadActive"
          >
            {{ activeTab === 'metrics' ? '下载指标' : `下载${selectedLogIds.length ? `(${selectedLogIds.length})` : ''}` }}
          </el-button>
        </div>

        <div class="range-toolbar">
          <span>时间范围：</span>
          <el-button v-for="item in ranges" :key="item.value" size="small" :type="range === item.value ? 'primary' : 'default'" @click="changeRange(item.value)">
            {{ item.label }}
          </el-button>
        </div>

        <div v-if="activeTab === 'metrics'" class="chart-grid" v-loading="metricsLoading">
          <el-alert v-if="metricError" class="metric-alert" type="warning" :closable="false" show-icon :title="metricError" />
          <template v-if="metrics.points.length > 0">
            <article class="chart-card">
              <h2>CPU利用率</h2>
              <svg class="line-chart" viewBox="0 0 360 180" role="img" aria-label="CPU利用率趋势">
                <line v-for="y in [30, 70, 110, 150]" :key="y" x1="42" x2="338" :y1="y" :y2="y" class="chart-gridline" />
                <polyline :points="linePoints('cpuUsage')" class="cpu-line" fill="none" />
                <text v-for="item in axisLabels" :key="item.label" :x="item.x" y="173" class="chart-label" text-anchor="middle">{{ item.label }}</text>
              </svg>
            </article>
            <article class="chart-card">
              <h2>内存使用占比</h2>
              <svg class="line-chart" viewBox="0 0 360 180" role="img" aria-label="内存使用占比趋势">
                <line v-for="y in [30, 70, 110, 150]" :key="y" x1="42" x2="338" :y1="y" :y2="y" class="chart-gridline" />
                <polyline :points="linePoints('memoryUsage')" class="memory-line" fill="none" />
                <text v-for="item in axisLabels" :key="item.label" :x="item.x" y="173" class="chart-label" text-anchor="middle">{{ item.label }}</text>
              </svg>
            </article>
            <article class="chart-card">
              <h2>当前连接数</h2>
              <svg class="line-chart" viewBox="0 0 360 180" role="img" aria-label="当前连接数趋势">
                <line v-for="y in [30, 70, 110, 150]" :key="y" x1="42" x2="338" :y1="y" :y2="y" class="chart-gridline" />
                <polyline :points="linePoints('currentConnections')" class="connection-line" fill="none" />
                <text v-for="item in axisLabels" :key="item.label" :x="item.x" y="173" class="chart-label" text-anchor="middle">{{ item.label }}</text>
              </svg>
            </article>
            <article class="chart-card cache-chart-card">
              <h2>缓存命中率</h2>
              <div class="donut-wrap">
                <svg viewBox="0 0 180 150" role="img" aria-label="缓存命中率">
                  <circle cx="75" cy="76" r="44" pathLength="100" class="donut-base" />
                  <circle cx="75" cy="76" r="44" pathLength="100" class="donut-value" :stroke-dasharray="cacheDasharray" />
                  <text x="75" y="82" text-anchor="middle" class="donut-number">{{ displayPercent(metrics.cacheHitRate) }}</text>
                </svg>
                <div class="cache-legend"><span><i class="hit-dot" />命中</span><span><i class="miss-dot" />未命中</span></div>
              </div>
            </article>
          </template>
          <el-empty v-else-if="!metricsLoading && !metricError" class="metric-empty" description="暂无指标数据" />
        </div>

        <div v-else class="log-content" v-loading="logsLoading">
          <el-table v-if="logs.length > 0" :data="logs" row-key="id" stripe class="detail-table" @selection-change="handleLogSelection">
            <el-table-column type="selection" width="52" />
            <el-table-column label="日志时间" min-width="190"><template #default="{ row }">{{ formatTime(row.loggedAt) }}</template></el-table-column>
            <el-table-column label="级别" width="120"><template #default="{ row }"><el-tag :type="logType(row.logLevel)" round>{{ row.logLevel }}</el-tag></template></el-table-column>
            <el-table-column prop="logContent" label="日志内容" min-width="560" show-overflow-tooltip />
          </el-table>
          <el-empty v-else-if="!logsLoading" description="当前时间范围暂无服务器日志" />
          <div v-if="logTotal > 0" class="log-pagination">
            <el-pagination v-model:current-page="logPageNo" :page-size="10" layout="total, prev, pager, next" :total="logTotal" @change="loadLogs" />
          </div>
        </div>
      </section>
    </template>
    <el-empty v-else-if="!loading" description="分布式数据库不存在或已删除" />

    <el-dialog v-model="monitoringConfigVisible" title="监控配置" width="620px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="Prometheus API 地址" required><el-input v-model="monitoringForm.prometheusBaseUrl" placeholder="https://prometheus.customer.example" /></el-form-item>
        <el-form-item label="数据库类型" required><el-select v-model="monitoringForm.databaseType" style="width:100%"><el-option label="MySQL" value="MYSQL" /></el-select></el-form-item>
        <el-form-item label="node_exporter 目标（job）" required><el-input v-model="monitoringForm.nodeJob" placeholder="node-exporter" /></el-form-item>
        <el-form-item label="node_exporter 目标（instance）" required><el-input v-model="monitoringForm.nodeInstance" placeholder="10.0.0.10:9100" /></el-form-item>
        <el-form-item label="mysqld_exporter 目标（job）" required><el-input v-model="monitoringForm.databaseJob" placeholder="mysqld-exporter" /></el-form-item>
        <el-form-item label="mysqld_exporter 目标（instance）" required><el-input v-model="monitoringForm.databaseInstance" placeholder="10.0.0.10:9104" /></el-form-item>
        <el-form-item label="服务端凭据引用（可选）"><el-input v-model="monitoringForm.prometheusAuthRef" placeholder="仅填写服务端已配置的引用，不填写 token" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="monitoringConfigVisible = false">取消</el-button><el-button type="primary" :loading="monitoringSaving" @click="saveMonitoring">保存配置</el-button></template>
    </el-dialog>

    <el-dialog v-model="downloadVisible" title="下载指标数据" width="392px" class="metric-download-dialog" destroy-on-close>
      <div class="download-form">
        <section class="download-section">
          <div class="download-section-label">时间范围</div>
          <div class="date-range-fields">
            <el-date-picker v-model="downloadStartDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" :clearable="false" />
            <span class="date-range-separator">–</span>
            <el-date-picker v-model="downloadEndDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" :clearable="false" />
          </div>
          <div class="date-shortcuts">
            <el-button v-for="shortcut in dateShortcuts" :key="shortcut.label" size="small" plain @click="applyDateShortcut(shortcut.days)">{{ shortcut.label }}</el-button>
          </div>
        </section>
        <section class="download-section metric-download-section">
          <div class="metric-select-toolbar">
            <el-button link type="primary" @click="selectAllDownloadMetrics">全选</el-button>
            <el-button link type="primary" @click="selectedDownloadMetrics = []">清空</el-button>
          </div>
          <el-checkbox-group v-model="selectedDownloadMetrics" class="metric-checkboxes">
            <el-checkbox v-for="item in downloadMetrics" :key="item.value" :label="item.value">{{ item.label }}</el-checkbox>
          </el-checkbox-group>
        </section>
      </div>
      <template #footer>
        <el-button class="download-cancel-button" @click="downloadVisible = false">取消</el-button>
        <el-button class="download-confirm-button" type="primary" :loading="downloading" :disabled="!selectedDownloadMetrics.length" @click="confirmMetricDownload">确认下载</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ArrowLeft, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  downloadLedgerFile,
  getDbInstanceLogs,
  getDbMetrics,
  getDbResourceDetail,
  getDbMonitoringConfig,
  saveDbMonitoringConfig,
  testDbMonitoringConfig,
  type DbInstanceLogItem,
  type DbMonitoringConfig,
  type DbMetricsOverview,
  type DbResourceItem,
  type MetricRange
} from '@/api/modules/ledger'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const canManageMonitoring = computed(() => userStore.hasPermission('ledger:database:update'))
const resource = ref<DbResourceItem | null>(null)
const monitoring = ref<DbMonitoringConfig>({ status: 'UNCONFIGURED' })
const monitoringConfigVisible = ref(false)
const monitoringSaving = ref(false)
const monitoringTesting = ref(false)
const monitoringForm = ref({ prometheusBaseUrl: '', prometheusAuthRef: '', databaseType: 'MYSQL', nodeJob: '', nodeInstance: '', databaseJob: '', databaseInstance: '' })
const metrics = ref<DbMetricsOverview>({ points: [] })
const logs = ref<DbInstanceLogItem[]>([])
const loading = ref(false)
const metricsLoading = ref(false)
const metricError = ref('')
const logsLoading = ref(false)
const downloading = ref(false)
const downloadVisible = ref(false)
const downloadStartDate = ref('')
const downloadEndDate = ref('')
const selectedDownloadMetrics = ref<string[]>([])
const downloadMetrics = [
  { label: 'CPU利用率', value: 'cpuUsage' },
  { label: '内存占比', value: 'memoryUsage' },
  { label: '缓存命中率', value: 'cacheHitRate' }
]
const dateShortcuts = [
  { label: '最近7天', days: 7 },
  { label: '最近30天', days: 30 },
  { label: '最近90天', days: 90 },
  { label: '本月', days: 0 }
]
const activeTab = ref<'metrics' | 'logs'>('metrics')
const range = ref<MetricRange>('1D')
const logPageNo = ref(1)
const logTotal = ref(0)
const selectedLogIds = ref<string[]>([])
const ranges: Array<{ label: string; value: MetricRange }> = [
  { label: '1小时', value: '1H' }, { label: '3小时', value: '3H' }, { label: '6小时', value: '6H' }, { label: '12小时', value: '12H' },
  { label: '1天', value: '1D' }, { label: '3天', value: '3D' }, { label: '7天', value: '7D' }, { label: '14天', value: '14D' }
]

const resourceId = () => String(route.params.id || '')
const cacheDasharray = computed(() => {
  const value = Math.max(0, Math.min(100, Number(metrics.value.cacheHitRate || 0)))
  return `${value} ${100 - value}`
})
const axisLabels = computed(() => {
  const points = metrics.value.points
  if (points.length === 0) return [] as Array<{ label: string; x: number }>
  const indices = [...new Set([0, Math.floor((points.length - 1) / 3), Math.floor((points.length - 1) * 2 / 3), points.length - 1])]
  return indices.map((index) => ({ label: formatChartTime(points[index].sampledAt), x: chartX(index, points.length) }))
})
async function loadPage() {
  loading.value = true
  try {
    resource.value = await getDbResourceDetail(resourceId())
    try { monitoring.value = await getDbMonitoringConfig(resourceId()) } catch { monitoring.value = { status: 'UNCONFIGURED' } }
    await Promise.all([loadMetrics(), loadLogs()])
  } finally {
    loading.value = false
  }
}

function openMonitoringConfig() {
  const node = monitoring.value.nodeTargetSelector || {}
  const database = monitoring.value.databaseTargetSelector || {}
  monitoringForm.value = {
    prometheusBaseUrl: monitoring.value.prometheusBaseUrl || '',
    prometheusAuthRef: '',
    databaseType: monitoring.value.databaseType || 'MYSQL',
    nodeJob: node.job || '', nodeInstance: node.instance || '',
    databaseJob: database.job || '', databaseInstance: database.instance || ''
  }
  monitoringConfigVisible.value = true
}

async function saveMonitoring() {
  const form = monitoringForm.value
  if (!form.prometheusBaseUrl || !form.nodeJob || !form.nodeInstance || !form.databaseJob || !form.databaseInstance) {
    ElMessage.warning('请完整填写 Prometheus 地址和两组目标 selector')
    return
  }
  monitoringSaving.value = true
  try {
    monitoring.value = await saveDbMonitoringConfig(resourceId(), {
      prometheusBaseUrl: form.prometheusBaseUrl,
      prometheusAuthRef: form.prometheusAuthRef || undefined,
      databaseType: form.databaseType,
      nodeTargetSelector: { job: form.nodeJob, instance: form.nodeInstance },
      databaseTargetSelector: { job: form.databaseJob, instance: form.databaseInstance }
    })
    monitoringConfigVisible.value = false
    ElMessage.success('监控配置已保存，请测试连接')
  } catch (error) { ElMessage.error(error instanceof Error ? error.message : '保存监控配置失败') }
  finally { monitoringSaving.value = false }
}

async function testMonitoring() {
  monitoringTesting.value = true
  try {
    monitoring.value = await testDbMonitoringConfig(resourceId())
    ElMessage.success('Prometheus 连接和目标测试通过')
    await loadMetrics()
  } catch (error) { ElMessage.error(error instanceof Error ? error.message : 'Prometheus 连接测试失败'); await getDbMonitoringConfig(resourceId()).then(value => { monitoring.value = value }).catch(() => undefined) }
  finally { monitoringTesting.value = false }
}

function monitoringStatusText(status: DbMonitoringConfig['status']) {
  return ({ UNCONFIGURED: '未配置', TESTING: '测试中', HEALTHY: '正常', ABNORMAL: '异常' } as Record<string, string>)[status] || '未配置'
}


async function loadMetrics() {
  metricsLoading.value = true
  metricError.value = ''
  try {
    metrics.value = await getDbMetrics(resourceId(), range.value)
  } catch (error) {
    metrics.value = { points: [] }
    metricError.value = typeof error === 'object' && error !== null && 'message' in error
      ? String(error.message)
      : error instanceof Error ? error.message : '监控服务暂不可用，请稍后重试'
  } finally {
    metricsLoading.value = false
  }
}

async function loadLogs() {
  logsLoading.value = true
  try {
    const page = await getDbInstanceLogs(resourceId(), { pageNo: logPageNo.value, pageSize: 10, range: range.value })
    logs.value = page.records
    logTotal.value = page.total
    selectedLogIds.value = []
  } finally {
    logsLoading.value = false
  }
}

async function changeRange(value: MetricRange) {
  if (range.value === value) return
  range.value = value
  logPageNo.value = 1
  await Promise.all([loadMetrics(), loadLogs()])
}

async function handleTabChange() {
  if (activeTab.value === 'logs' && logs.value.length === 0) await loadLogs()
}

async function downloadActive() {
  if (activeTab.value === 'metrics') {
    openMetricDownload()
    return
  }
  downloading.value = true
  try {
    const file = await downloadLedgerFile(`/ledger/db-resources/${resourceId()}/logs/download`, {
      range: range.value,
      logIds: selectedLogIds.value.join(',')
    })
    saveDownloadedFile(file)
    ElMessage.success('日志下载已开始')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '下载失败，请稍后重试')
  } finally {
    downloading.value = false
  }
}

function openMetricDownload() {
  const today = new Date()
  downloadEndDate.value = formatDateInput(today)
  const start = new Date(today)
  start.setDate(today.getDate() - 6)
  downloadStartDate.value = formatDateInput(start)
  selectedDownloadMetrics.value = downloadMetrics.map((item) => item.value)
  downloadVisible.value = true
}

function applyDateShortcut(days: number) {
  const today = new Date()
  downloadEndDate.value = formatDateInput(today)
  if (days === 0) {
    downloadStartDate.value = formatDateInput(new Date(today.getFullYear(), today.getMonth(), 1))
    return
  }
  const start = new Date(today)
  start.setDate(today.getDate() - days + 1)
  downloadStartDate.value = formatDateInput(start)
}

function selectAllDownloadMetrics() {
  selectedDownloadMetrics.value = downloadMetrics.map((item) => item.value)
}

async function confirmMetricDownload() {
  if (!downloadStartDate.value || !downloadEndDate.value) {
    ElMessage.warning('请选择日期范围')
    return
  }
  if (downloadStartDate.value > downloadEndDate.value) {
    ElMessage.warning('开始日期不能晚于结束日期')
    return
  }
  downloading.value = true
  try {
    const file = await downloadLedgerFile(`/ledger/db-resources/${resourceId()}/metrics/download`, {
      startDate: downloadStartDate.value,
      endDate: downloadEndDate.value,
      metrics: selectedDownloadMetrics.value.join(',')
    })
    saveDownloadedFile(file)
    downloadVisible.value = false
    ElMessage.success('指标下载已开始')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '下载失败，请稍后重试')
  } finally {
    downloading.value = false
  }
}

function saveDownloadedFile(file: { fileName: string; blob: Blob }) {
  const link = document.createElement('a')
  const url = URL.createObjectURL(file.blob)
  link.href = url
  link.download = file.fileName
  link.click()
  URL.revokeObjectURL(url)
}

function formatDateInput(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function handleLogSelection(selectedLogs: DbInstanceLogItem[]) {
  selectedLogIds.value = selectedLogs.map((log) => log.id)
}

function linePoints(metric: 'cpuUsage' | 'memoryUsage' | 'currentConnections') {
  const points = metrics.value.points.filter((point) => point[metric] !== undefined && point[metric] !== null)
  if (points.length === 0) return ''
  const max = metric === 'currentConnections'
    ? Math.max(1, ...points.map((point) => Number(point[metric])))
    : 100
  return points.map((point, index) => `${chartX(index, points.length)},${chartY(Number(point[metric]), max)}`).join(' ')
}

function chartX(index: number, count: number) {
  return count <= 1 ? 190 : 42 + index * (296 / (count - 1))
}

function chartY(value: number, max: number) {
  return 150 - Math.max(0, Math.min(max, value)) / max * 120
}

function displayNumber(value?: number) {
  return value === undefined || value === null ? '-' : Number(value).toFixed(1)
}

function displayPercent(value?: number) {
  return value === undefined || value === null ? '--' : `${Number(value).toFixed(1)}%`
}

function formatTime(value: string) {
  return value.replace('T', ' ').slice(0, 19)
}

function formatChartTime(value: string) {
  return value.replace('T', ' ').slice(11, 16)
}

function logType(level: string) {
  return level === 'INFO' ? 'success' : level === 'WARN' ? 'warning' : 'danger'
}

function goBack() {
  router.push({ name: 'DataLedger', query: { tab: 'database' } })
}

onMounted(loadPage)
</script>

<style scoped>
.db-detail-page {
  min-height: 100%;
  padding: 40px;
  color: #0d2450;
  background: #f8faff;
}

.detail-back {
  display: inline-flex;
  align-items: center;
  gap: 16px;
  min-height: 40px;
  padding: 0;
  border: 0;
  color: #102a56;
  background: transparent;
  font: inherit;
  font-size: 24px;
  font-weight: 700;
  cursor: pointer;
}

.detail-back .el-icon { color: #0d1b38; font-size: 20px; }

.detail-summary {
  display: grid;
  margin-top: 24px;
  padding: 24px;
  border: 1px solid #b8caf4;
  border-radius: 12px;
  background: #eef4ff;
  box-shadow: 0 6px 18px rgba(30, 64, 175, 0.08);
}
.monitor-config-panel { display:flex; justify-content:space-between; align-items:center; gap:16px; margin-top:24px; padding:16px 24px; border:1px solid #dce7fa; border-radius:8px; background:#fff; }.monitor-config-panel span,.monitor-config-panel small { display:block; color:#7185ad; font-size:13px; }.monitor-config-panel strong { display:block; margin:4px 0; color:#071d48; }.monitor-config-actions { display:flex; gap:8px; }

.db-summary { grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 28px; }
.summary-item { min-width: 0; display: flex; flex-direction: column; gap: 8px; }
.summary-item span { color: #94a5c6; font-size: 13px; }
.summary-item strong { overflow-wrap: anywhere; color: #071d48; font-size: 14px; font-weight: 700; }

.metric-cards { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 20px; margin-top: 24px; }
.metric-card { min-height: 112px; padding: 24px; border: 1px solid #dce7fa; border-radius: 12px; background: #fff; box-shadow: 0 6px 18px rgba(30, 64, 175, 0.08); }
.metric-card span { display: block; margin-bottom: 12px; color: #94a5c6; font-size: 13px; }
.metric-card strong { color: #1f45ba; font-size: 27px; line-height: 1; }
.metric-card small { margin-left: 5px; color: #92a6c9; font-size: 13px; }

.detail-panel { margin-top: 24px; overflow: hidden; border: 1px solid #dce7fa; border-radius: 12px; background: #fff; box-shadow: 0 6px 18px rgba(30, 64, 175, 0.08); }
.panel-toolbar { display: flex; align-items: center; justify-content: space-between; min-height: 56px; padding: 8px 16px 8px 8px; border-bottom: 1px solid #dce7fa; }
.tab-toolbar :deep(.el-tabs__header) { margin: 0; }
.tab-toolbar :deep(.el-tabs__nav-wrap::after) { display: none; }
.tab-toolbar :deep(.el-tabs__item) { display: inline-flex; align-items: center; justify-content: center; box-sizing: border-box; height: 36px; padding: 0 20px; color: #8ea0c5; font-weight: 700; line-height: normal; text-align: center; }
.tab-toolbar :deep(.el-tabs__item.is-active) { border-radius: 8px; color: #fff; background: #1e40af; }
.tab-toolbar :deep(.el-tabs__active-bar) { display: none; }
.tab-toolbar :deep(.el-button) { border-radius: 8px; font-weight: 700; }

.range-toolbar { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; min-height: 76px; padding: 20px 24px; border-bottom: 1px solid #dce7fa; color: #91a2c4; font-size: 13px; }
.range-toolbar :deep(.el-button) { min-width: 56px; margin: 0; border-color: #dce7fa; border-radius: 8px; color: #061b47; font-weight: 700; }

.chart-grid { position: relative; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 24px; padding: 24px; min-height: 286px; }
.metric-alert { grid-column: 1 / -1; }
.chart-card { min-height: 244px; padding: 24px; border: 1px solid #dce7fa; border-radius: 12px; background: #fff; }
.chart-card h2 { margin: 0; color: #061b47; font-size: 18px; }
.line-chart { width: 100%; height: 190px; margin-top: 12px; overflow: visible; }
.chart-gridline { stroke: #dce7fa; stroke-dasharray: 3 4; stroke-width: 1; }
.cpu-line, .memory-line, .connection-line { stroke-width: 3; stroke-linecap: round; stroke-linejoin: round; }
.cpu-line { stroke: #2449b8; }
.memory-line { stroke: #009a6a; }
.connection-line { stroke: #d97706; }
.chart-label { fill: #7185ad; font-size: 10px; }
.cache-chart-card { display: flex; flex-direction: column; }
.donut-wrap { display: flex; align-items: center; justify-content: center; gap: 8px; flex: 1; }
.donut-wrap svg { width: 170px; height: 148px; }
.donut-base, .donut-value { fill: none; stroke-width: 18; transform: rotate(-90deg); transform-origin: 75px 76px; }
.donut-base { stroke: #e92f2f; }
.donut-value { stroke: #2449b8; stroke-linecap: butt; }
.donut-number { fill: #2449b8; font-size: 18px; font-weight: 700; }
.cache-legend { display: flex; flex-direction: column; gap: 10px; color: #7185ad; font-size: 12px; }
.cache-legend span { display: inline-flex; align-items: center; gap: 6px; }
.cache-legend i { width: 9px; height: 9px; border-radius: 50%; }
.hit-dot { background: #2449b8; }.miss-dot { background: #e92f2f; }
.metric-empty { grid-column: 1 / -1; }

.log-content { min-width: 0; min-height: 280px; overflow-x: auto; }
.detail-table { min-width: 920px; }
.detail-table :deep(th.el-table__cell) { height: 42px; color: #071d48; background: #f1f5f9; font-weight: 700; }
.detail-table :deep(td.el-table__cell) { height: 58px; color: #0d2450; }
.log-pagination { display: flex; justify-content: flex-end; padding: 16px; }

.metric-download-dialog {
  max-width: calc(100vw - 32px);
  overflow: hidden;
  border-radius: 14px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.22);
}
.metric-download-dialog :deep(.el-dialog__header) {
  box-sizing: border-box;
  height: 74px;
  margin: 0;
  padding: 25px 22px 20px;
  border-bottom: 1px solid #e5eaf3;
}
.metric-download-dialog :deep(.el-dialog__title) {
  color: #0f1f3d;
  font-size: 20px;
  font-weight: 700;
  line-height: 28px;
}
.metric-download-dialog :deep(.el-dialog__headerbtn) {
  top: 19px;
  right: 16px;
  width: 36px;
  height: 36px;
}
.metric-download-dialog :deep(.el-dialog__close) {
  color: #172033;
  font-size: 18px;
}
.metric-download-dialog :deep(.el-dialog__body) { padding: 15px 22px 17px; }
.metric-download-dialog :deep(.el-dialog__footer) {
  padding: 20px 21px 21px;
  border-top: 1px solid #e5eaf3;
}
.download-form { width: 100%; }
.download-section + .download-section { margin-top: 20px; }
.download-section-label {
  margin-bottom: 10px;
  color: #91a0ba;
  font-size: 12px;
  font-weight: 700;
  line-height: 18px;
}
.date-range-fields { display: flex; align-items: center; gap: 7px; width: 100%; }
.date-range-fields :deep(.el-date-editor) { flex: 1; width: 0; min-width: 0; height: 34px; }
.date-range-fields :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #dce4f2 inset;
}
.date-range-fields :deep(.el-input__inner) { color: #172033; font-size: 12px; }
.date-range-fields :deep(.el-input__prefix) { color: #91a0ba; }
.date-range-separator { flex: none; color: #93a1b8; font-size: 13px; }
.date-shortcuts { display: flex; flex-wrap: wrap; gap: 7px; margin-top: 10px; }
.date-shortcuts :deep(.el-button) {
  min-width: 57px;
  height: 28px;
  margin-left: 0;
  padding: 0 10px;
  border-color: #dce4f2;
  border-radius: 10px;
  color: #172033;
  background: #fff;
  font-size: 11px;
  font-weight: 700;
}
.date-shortcuts :deep(.el-button:hover),
.date-shortcuts :deep(.el-button:focus) {
  color: #3568d4;
  border-color: #9eb9ef;
  background: #f5f8ff;
}
.metric-select-toolbar { display: flex; align-items: center; gap: 12px; height: 22px; margin-bottom: 8px; }
.metric-select-toolbar :deep(.el-button) {
  height: auto;
  margin-left: 0;
  padding: 0;
  font-size: 12px;
  font-weight: 700;
}
.metric-select-toolbar :deep(.el-button:first-child) { color: #2256c7; }
.metric-select-toolbar :deep(.el-button:last-child) { color: #91a0ba; }
.metric-checkboxes { display: flex; flex-direction: column; align-items: flex-start; gap: 10px; }
.metric-checkboxes :deep(.el-checkbox) {
  --el-checkbox-checked-bg-color: #34383f;
  --el-checkbox-checked-input-border-color: #34383f;
  --el-checkbox-checked-text-color: #172033;
  height: 22px;
  margin-right: 0;
}
.metric-checkboxes :deep(.el-checkbox__input) { height: 14px; }
.metric-checkboxes :deep(.el-checkbox__inner) { width: 14px; height: 14px; border-radius: 2px; }
.metric-checkboxes :deep(.el-checkbox__label) {
  padding-left: 9px;
  color: #172033;
  font-size: 12px;
  font-weight: 600;
}
.metric-download-dialog :deep(.download-cancel-button),
.metric-download-dialog :deep(.download-confirm-button) {
  height: 34px;
  margin-left: 8px;
  padding: 0 14px;
  border-radius: 11px;
  font-size: 12px;
  font-weight: 700;
}
.metric-download-dialog :deep(.download-cancel-button) { color: #26344d; border-color: #dce4f2; }
.metric-download-dialog :deep(.download-confirm-button) { border-color: #89a9ef; background: #89a9ef; }
.metric-download-dialog :deep(.download-confirm-button:hover),
.metric-download-dialog :deep(.download-confirm-button:focus) { border-color: #769bea; background: #769bea; }

@media (max-width: 1300px) { .db-summary, .metric-cards { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 720px) { .db-detail-page { padding: 24px 16px 32px; }.db-summary, .metric-cards, .chart-grid { grid-template-columns: 1fr; }.panel-toolbar { align-items: flex-start; gap: 12px; flex-direction: column; }.tab-toolbar :deep(.el-button) { align-self: flex-end; }.date-range-fields { flex-wrap: wrap; }.date-range-fields :deep(.el-date-editor) { flex-basis: calc(50% - 12px); }.date-shortcuts { gap: 6px; } }
</style>
