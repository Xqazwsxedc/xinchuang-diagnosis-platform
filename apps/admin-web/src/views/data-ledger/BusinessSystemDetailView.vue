<template>
  <section class="ledger-detail-page" v-loading="loading">
    <button class="detail-back" type="button" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      <span>{{ business?.name || '业务系统详情' }}</span>
    </button>

    <template v-if="business">
      <section class="detail-summary business-summary">
        <div class="summary-item">
          <span>业务系统名称</span>
          <strong>{{ business.name }}</strong>
        </div>
        <div class="summary-item">
          <span>关联的分布式数据库</span>
          <strong>{{ business.databaseNames?.join('、') || business.databaseName || '-' }}</strong>
        </div>
        <div class="summary-item">
          <span>创建人</span>
          <strong>{{ business.ownerName }}</strong>
        </div>
        <div class="summary-item">
          <span>访问地址</span>
          <a v-if="business.accessUrl" :href="business.accessUrl" target="_blank" rel="noreferrer">{{ business.accessUrl }}</a>
          <strong v-else>-</strong>
        </div>
        <div class="summary-item">
          <span>当前数据状况</span>
          <strong class="status-value" :class="statusClass(business.status)"><i />{{ statusText(business.status) }}</strong>
        </div>
        <div class="summary-item">
          <span>备注说明</span>
          <strong>{{ business.remark || '-' }}</strong>
        </div>
      </section>

      <section class="detail-panel business-record-panel">
        <div class="panel-toolbar">
          <span class="panel-tab">业务数据库</span>
          <el-button
            type="primary"
            :icon="Download"
            :disabled="selectedRecordIds.length === 0"
            :loading="downloading === 'selected'"
            @click="downloadSelected"
          >
            下载{{ selectedRecordIds.length ? `(${selectedRecordIds.length})` : '' }}
          </el-button>
        </div>
        <el-table :data="records" v-loading="recordsLoading" row-key="id" stripe class="detail-table" @selection-change="handleRecordSelection">
          <el-table-column type="selection" width="52" />
          <el-table-column label="收集日期" min-width="170">
            <template #default="{ row }">{{ datePart(row.collectedAt) }}</template>
          </el-table-column>
          <el-table-column label="收集时间" min-width="160">
            <template #default="{ row }">{{ timePart(row.collectedAt) }}</template>
          </el-table-column>
          <el-table-column label="总大小" min-width="150">
            <template #default="{ row }">{{ formatSize(row.totalSizeBytes) }}</template>
          </el-table-column>
          <el-table-column label="收集状态" min-width="160">
            <template #default="{ row }">
              <span class="collect-status" :class="row.collectStatus === 'SUCCESS' ? 'success' : 'failed'">
                {{ row.collectStatus === 'SUCCESS' ? '成功' : '失败' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="180">
            <template #default="{ row }">
              <el-button type="primary" :icon="Download" :loading="downloading === row.id" @click="downloadRecord(row.id)">下载</el-button>
            </template>
          </el-table-column>
          <template #empty><span aria-hidden="true" /></template>
        </el-table>
        <el-empty v-if="!recordsLoading && records.length === 0" description="暂无业务数据记录" />
      </section>
    </template>
    <el-empty v-else-if="!loading" description="业务系统不存在或已删除" />
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ArrowLeft, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  downloadLedgerFile,
  getBusinessDataRecords,
  getBusinessSystemDetail,
  type BusinessDataRecordItem,
  type BusinessSystemItem
} from '@/api/modules/ledger'

const route = useRoute()
const router = useRouter()
const business = ref<BusinessSystemItem | null>(null)
const records = ref<BusinessDataRecordItem[]>([])
const loading = ref(false)
const recordsLoading = ref(false)
const downloading = ref<string | 'selected' | null>(null)
const selectedRecordIds = ref<string[]>([])

const businessId = () => String(route.params.id || '')

async function loadPage() {
  loading.value = true
  recordsLoading.value = true
  try {
    const [businessDetail, recordList] = await Promise.all([
      getBusinessSystemDetail(businessId()),
      getBusinessDataRecords(businessId())
    ])
    business.value = businessDetail
    records.value = recordList
    selectedRecordIds.value = []
  } finally {
    loading.value = false
    recordsLoading.value = false
  }
}

async function downloadSelected() {
  if (selectedRecordIds.value.length === 0) return
  await download(`/ledger/business-systems/${businessId()}/data-records/download`, 'selected', {
    recordIds: selectedRecordIds.value.join(',')
  })
}

async function downloadRecord(recordId: string) {
  await download(`/ledger/business-systems/${businessId()}/data-records/${recordId}/download`, recordId)
}

async function download(path: string, key: string | 'selected', params?: Record<string, string>) {
  downloading.value = key
  try {
    const file = await downloadLedgerFile(path, params)
    triggerDownload(file.fileName, file.blob)
    ElMessage.success('下载已开始')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '下载失败，请稍后重试')
  } finally {
    downloading.value = null
  }
}

function handleRecordSelection(selectedRecords: BusinessDataRecordItem[]) {
  selectedRecordIds.value = selectedRecords.map((record) => record.id)
}

function triggerDownload(fileName: string, blob: Blob) {
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.href = url
  link.download = fileName
  link.click()
  URL.revokeObjectURL(url)
}

function goBack() {
  router.push({ name: 'DataLedger', query: { tab: 'business' } })
}

function datePart(value: string) {
  return value.replace('T', ' ').slice(0, 10)
}

function timePart(value: string) {
  return value.replace('T', ' ').slice(11, 19)
}

function formatSize(bytes: number) {
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

function statusText(status: string) {
  return ({ NORMAL: '正常', WARNING: '警告', DANGER: '危险', OFFLINE: '离线' } as Record<string, string>)[status] || status
}

function statusClass(status: string) {
  return `is-${status.toLowerCase()}`
}

onMounted(loadPage)
</script>

<style scoped>
.ledger-detail-page {
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

.detail-back .el-icon {
  color: #0d1b38;
  font-size: 20px;
}

.detail-summary {
  display: grid;
  margin-top: 24px;
  padding: 24px;
  border: 1px solid #b8caf4;
  border-radius: 12px;
  background: #eef4ff;
  box-shadow: 0 6px 18px rgba(30, 64, 175, 0.08);
}

.business-summary {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px 40px;
}

.summary-item {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-item > span {
  color: #94a5c6;
  font-size: 13px;
}

.summary-item strong,
.summary-item a {
  min-width: 0;
  overflow-wrap: anywhere;
  color: #071d48;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.45;
}

.summary-item a {
  color: #001e64;
  text-decoration: none;
}

.status-value {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.status-value i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #00b42a;
}

.status-value.is-warning i { background: #ff7d00; }
.status-value.is-danger i { background: #f53f3f; }
.status-value.is-offline i { background: #86909c; }

.detail-panel {
  margin-top: 24px;
  overflow: hidden;
  border: 1px solid #dce7fa;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 6px 18px rgba(30, 64, 175, 0.08);
}

.panel-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 56px;
  padding: 8px 16px 8px 8px;
  border-bottom: 1px solid #dce7fa;
}

.panel-tab {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 22px;
  border-radius: 8px;
  color: #fff;
  background: #1e40af;
  font-size: 14px;
  font-weight: 700;
}

.panel-toolbar :deep(.el-button) {
  min-width: 68px;
  border-radius: 8px;
  font-weight: 700;
}

.detail-table :deep(th.el-table__cell) {
  height: 42px;
  color: #071d48;
  background: #f1f5f9;
  font-weight: 700;
}

.detail-table :deep(td.el-table__cell) {
  height: 58px;
  color: #0d2450;
}

.collect-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 44px;
  padding: 3px 10px;
  border: 1px solid;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.collect-status.success {
  border-color: #b7f0c8;
  color: #00a63a;
  background: #effcf3;
}

.collect-status.failed {
  border-color: #ffd1d1;
  color: #f53f3f;
  background: #fff4f4;
}

@media (max-width: 1100px) {
  .business-summary { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 720px) {
  .ledger-detail-page { padding: 24px 16px 32px; }
  .business-summary { grid-template-columns: 1fr; }
  .panel-toolbar { align-items: flex-start; gap: 12px; flex-direction: column; }
  .panel-toolbar :deep(.el-button) { align-self: flex-end; }
}
</style>
