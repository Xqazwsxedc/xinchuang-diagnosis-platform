<template>
  <section class="page alert-page">
    <div class="page-header">
      <div>
        <h1>故障智能预警</h1>
        <p>查看 AI 引擎生成的故障预警并完成处置</p>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query" class="alert-filter">
        <el-form-item label="故障ID/对象">
          <el-input v-model="query.keyword" clearable placeholder="请输入故障ID或预测对象" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="query.processStatus" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="误报" value="FALSE_POSITIVE" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="query.riskLevel" clearable placeholder="全部等级" style="width: 130px">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="严重" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="预测对象类型">
          <el-select v-model="query.targetType" clearable placeholder="全部对象" style="width: 150px">
            <el-option label="业务系统" value="BUSINESS_SYSTEM" />
            <el-option label="分布式数据库" value="DB_RESOURCE" />
          </el-select>
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="records" stripe row-key="id">
        <el-table-column prop="reportCode" label="故障ID" width="185" />
        <el-table-column prop="targetNameSnapshot" label="预测对象" min-width="230" show-overflow-tooltip />
        <el-table-column prop="indicatorCategory" label="指标类别" min-width="150" show-overflow-tooltip />
        <el-table-column prop="faultType" label="预测故障类型" min-width="210" show-overflow-tooltip />
        <el-table-column prop="alertTime" label="告警时间" width="180">
          <template #default="{ row }">{{ formatTime(row.alertTime) }}</template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="105">
          <template #default="{ row }"><el-tag :type="riskType(row.riskLevel)" effect="light">{{ riskText(row.riskLevel) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="processStatus" label="处理状态" width="105">
          <template #default="{ row }"><el-tag :type="statusType(row.processStatus)" effect="light">{{ statusText(row.processStatus) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="145" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="openDetail(row.id)">查看详情</el-button>
            <el-button v-if="canDelete" type="danger" link :icon="Delete" title="删除预警" @click="removeByRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :total="total" background layout="total, sizes, prev, pager, next" :page-sizes="[10, 20, 50]" @current-change="loadData" @size-change="loadData" />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="预警详情" size="584px" class="alert-drawer" @open="resetDrawerScroll" @opened="resetDrawerScroll" @closed="detail = null">
      <template v-if="detail">
        <div class="alert-detail">
          <div class="detail-field">
            <div class="section-label">故障ID</div>
            <div class="readonly-field">{{ detail.reportCode || '-' }}</div>
          </div>
          <div class="detail-field">
            <div class="section-label">预测对象</div>
            <div class="readonly-field">{{ detail.targetNameSnapshot || '-' }}</div>
          </div>
          <div class="detail-field">
            <div class="section-label">告警时间</div>
            <div class="readonly-field">{{ formatTime(detail.alertTime) }}</div>
          </div>
          <div class="detail-field">
            <div class="section-label">指标类别</div>
            <div class="readonly-field">{{ detail.indicatorCategory || '-' }}</div>
          </div>
          <div class="detail-field">
            <div class="section-label">预测故障类型</div>
            <div class="readonly-field">{{ detail.faultType || '-' }}</div>
          </div>
          <div class="detail-field">
            <div class="section-label">预计发生时间</div>
            <div class="readonly-field">{{ expectedTime(detail) }}</div>
          </div>

          <div class="detail-section confidence-section">
            <div class="section-label">预测置信度</div>
            <div class="confidence-row">
              <el-progress :percentage="confidenceValue(detail.confidence)" :show-text="false" :stroke-width="7" />
              <span>{{ confidenceValue(detail.confidence).toFixed(1) }}%</span>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-label">指标走势</div>
            <div class="trend-box">
              <svg viewBox="0 0 500 212" role="img" aria-label="指标走势">
                <line v-for="line in trendGrid" :key="line.y" x1="50" :y1="line.y" x2="460" :y2="line.y" stroke="#d8dee9" stroke-dasharray="3 4" />
                <text v-for="line in trendGrid" :key="`label-${line.y}`" x="5" :y="line.y + 4" fill="#94a3b8" font-size="12">{{ line.label }}</text>
                <polyline :points="trendPoints" fill="none" stroke="#ff6b1a" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" />
                <circle v-for="point in trendDots" :key="point.x" :cx="point.x" :cy="point.y" r="3.5" fill="#ff6b1a" />
                <text v-for="point in trendDots" :key="`time-${point.x}`" :x="point.x" y="202" text-anchor="middle" fill="#94a3b8" font-size="11">{{ point.time }}</text>
              </svg>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-label">预测说明</div>
            <div class="readonly-text">{{ detail.forecastDescription || '-' }}</div>
          </div>
          <div class="detail-section">
            <div class="section-label">处理建议</div>
            <div class="readonly-text">{{ detail.handleSuggestion || '-' }}</div>
          </div>

          <div class="detail-section history-section">
            <div class="section-label">处理记录</div>
            <div class="process-summary"><el-tag :type="statusType(detail.processStatus)" effect="light">{{ statusText(detail.processStatus) }}</el-tag><span v-if="detail.handledAt">{{ detail.handledByName || '系统用户' }} · {{ formatTime(detail.handledAt) }}</span></div>
            <el-empty v-if="!detail.histories?.length" description="暂无处理记录" :image-size="54" />
            <div v-else class="history-list">
              <div v-for="item in detail.histories" :key="`${item.createdAt}-${item.afterStatus}`" class="history-item">
                <div><el-tag size="small" :type="statusType(item.afterStatus)">{{ statusText(item.afterStatus) }}</el-tag><span>{{ item.operatorName || '系统' }}</span></div>
                <time>{{ formatTime(item.createdAt) }}</time>
                <p>{{ item.operationRemark || '-' }}</p>
              </div>
            </div>
          </div>

        </div>
      </template>
      <template #footer>
        <div class="alert-drawer-footer">
          <el-button type="success" round :disabled="!canTransition('CONFIRMED')" @click="handleStatus('CONFIRMED')">确认</el-button>
          <el-button type="warning" round :disabled="!canTransition('FALSE_POSITIVE')" @click="handleStatus('FALSE_POSITIVE')">误报</el-button>
          <el-button type="primary" round :disabled="!canTransition('CLOSED')" @click="handleStatus('CLOSED')">关闭</el-button>
        </div>
      </template>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { Delete, RefreshLeft, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteFaultAlert, getFaultAlertDetail, getFaultAlerts, handleFaultAlert, type FaultAlertDetail, type FaultAlertItem, type FaultAlertQuery, type FaultAlertStatus } from '@/api/modules/ai'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const records = ref<FaultAlertItem[]>([])
const detail = ref<FaultAlertDetail | null>(null)
const detailVisible = ref(false)
const total = ref(0)
const query = reactive<FaultAlertQuery>({ pageNo: 1, pageSize: 10, keyword: '', processStatus: undefined, riskLevel: undefined, targetType: undefined })
const canDelete = computed(() => userStore.hasPermission('ai:fault:delete'))

const trend = computed(() => {
  if (!detail.value?.trendJson) return []
  try {
    const value = JSON.parse(detail.value.trendJson)
    return Array.isArray(value) ? value.filter((item) => typeof item?.value === 'number').slice(-8) : []
  } catch { return [] }
})
const trendDots = computed(() => {
  const values = trend.value
  if (!values.length) return []
  const min = Math.min(...values.map((item) => item.value))
  const max = Math.max(...values.map((item) => item.value))
  const range = max - min || 1
  return values.map((item, index) => ({ time: item.time || '', x: 50 + (410 / Math.max(values.length - 1, 1)) * index, y: 170 - ((item.value - min) / range) * 140 }))
})
const trendPoints = computed(() => trendDots.value.map((point) => `${point.x},${point.y}`).join(' '))
const trendGrid = [{ y: 28, label: '95' }, { y: 69, label: '89' }, { y: 110, label: '83' }, { y: 151, label: '78' }]

async function loadData() {
  loading.value = true
  try {
    const page = await getFaultAlerts({ pageNo: query.pageNo, pageSize: query.pageSize, keyword: query.keyword || undefined, processStatus: query.processStatus || undefined, riskLevel: query.riskLevel || undefined, targetType: query.targetType || undefined })
    records.value = page.records
    total.value = page.total
  } finally { loading.value = false }
}
function search() { query.pageNo = 1; loadData() }
function reset() { query.keyword = ''; query.processStatus = undefined; query.riskLevel = undefined; query.targetType = undefined; query.pageNo = 1; loadData() }
async function openDetail(id: string) {
  detail.value = await getFaultAlertDetail(id)
  detailVisible.value = true
  await nextTick()
  resetDrawerScroll()
}
function resetDrawerScroll() {
  const body = document.querySelector<HTMLElement>('.alert-drawer .el-drawer__body')
  if (body) body.scrollTop = 0
}
async function handleStatus(status: FaultAlertStatus) {
  if (!detail.value) return
  const result = await ElMessageBox.prompt('请填写本次处理备注', '处理预警', { inputPlaceholder: '请输入处理说明', inputValidator: (value) => value.trim() ? true : '处理备注不能为空', confirmButtonText: '提交', cancelButtonText: '取消' })
  detail.value = await handleFaultAlert(detail.value.id, status, result.value)
  ElMessage.success('预警处理成功')
  await loadData()
}
async function removeByRow(row: FaultAlertItem) {
  await ElMessageBox.confirm(`确定删除预警「${row.reportCode}」吗？`, '确认删除', { type: 'warning' })
  await deleteFaultAlert(row.id)
  ElMessage.success('删除成功')
  await loadData()
}
function formatTime(value?: string) { return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '-' }
function expectedTime(row: FaultAlertDetail) { if (!row.expectedStartTime) return '-'; return row.expectedEndTime ? `${formatTime(row.expectedStartTime)} ~ ${formatTime(row.expectedEndTime)}` : formatTime(row.expectedStartTime) }
function confidenceValue(value?: number) { return Math.max(0, Math.min(100, Number(value || 0))) }
function riskText(value?: string) { return ({ LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', CRITICAL: '严重' } as Record<string, string>)[value || ''] || '-' }
function riskType(value?: string) { return ({ LOW: 'success', MEDIUM: 'warning', HIGH: 'danger', CRITICAL: 'danger' } as Record<string, 'success' | 'warning' | 'danger'>)[value || ''] || 'info' }
function statusText(value?: string) { return ({ PENDING: '待处理', CONFIRMED: '已确认', FALSE_POSITIVE: '误报', CLOSED: '已关闭' } as Record<string, string>)[value || ''] || '-' }
function statusType(value?: string) { return ({ PENDING: 'danger', CONFIRMED: 'success', FALSE_POSITIVE: 'warning', CLOSED: 'info' } as Record<string, 'success' | 'warning' | 'danger' | 'info'>)[value || ''] || 'info' }
function canTransition(nextStatus: FaultAlertStatus) {
  if (!detail.value || !userStore.hasPermission('ai:fault:handle')) return false
  return (detail.value.processStatus === 'PENDING' && (nextStatus === 'CONFIRMED' || nextStatus === 'FALSE_POSITIVE'))
    || (detail.value.processStatus === 'CONFIRMED' && nextStatus === 'CLOSED')
}

onMounted(loadData)
</script>

<style scoped>
.alert-page :deep(.el-card) { border-radius: 8px; }
.alert-filter :deep(.el-form-item) { margin-bottom: 0; }
.table-card { min-height: 520px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 20px; }
.alert-drawer :deep(.el-drawer__header) { height: 74px; margin: 0; padding: 0 20px; border-bottom: 1px solid #e2e8f3; background: #f8fafc; color: #172033; font-size: 18px; font-weight: 700; }
.alert-drawer :deep(.el-drawer__body) { overflow: auto; padding: 18px 20px 0; }
.alert-drawer :deep(.el-drawer__footer) { padding: 16px 20px; border-top: 1px solid #e8edf5; background: #fff; }
.alert-detail { padding: 0 0 24px; }
.detail-field, .detail-section { margin-bottom: 22px; }
.section-label { margin-bottom: 9px; color: #172033; font-size: 14px; font-weight: 600; }
.readonly-field, .readonly-text { border: 1px solid #dce5f2; border-radius: 12px; background: #fbfcfe; color: #7a8495; font-size: 14px; line-height: 20px; }
.readonly-field { min-height: 42px; padding: 10px 14px; }
.readonly-text { min-height: 106px; padding: 13px 14px; white-space: pre-wrap; }
.confidence-row { display: flex; align-items: center; gap: 18px; }
.confidence-row :deep(.el-progress) { flex: 1; }
.confidence-row > span { min-width: 48px; color: #234bc5; font-size: 13px; text-align: right; }
.trend-box { height: 225px; border: 1px solid #dce5f2; border-radius: 12px; background: #fbfcfe; padding: 8px 10px 0; }
.trend-box svg { display: block; width: 100%; height: 100%; }
.process-summary { display: flex; align-items: center; gap: 12px; color: #8b94a4; font-size: 13px; }
.history-section { border-top: 1px solid #edf1f7; padding-top: 18px; }
.history-list { display: grid; gap: 12px; }
.history-item { border-left: 2px solid #dbe5f4; padding: 2px 0 2px 12px; color: #7a8495; font-size: 13px; }
.history-item > div { display: flex; align-items: center; gap: 9px; color: #4e596b; }
.history-item time { display: block; margin-top: 5px; font-size: 12px; }
.history-item p { margin: 6px 0 0; line-height: 1.55; }
.alert-drawer-footer { display: flex; justify-content: flex-end; gap: 10px; }
.alert-drawer-footer :deep(.el-button) { min-width: 64px; height: 38px; margin: 0; font-weight: 600; }
@media (max-width: 900px) { .alert-filter { display: grid; grid-template-columns: 1fr 1fr; } .filter-actions { grid-column: 1 / -1; } }
@media (max-width: 640px) { .alert-filter { display: block; } .alert-filter :deep(.el-form-item) { margin-bottom: 12px; } .alert-filter :deep(.el-input), .alert-filter :deep(.el-select) { width: 100% !important; } .pagination-wrap { justify-content: center; overflow-x: auto; } }
</style>
