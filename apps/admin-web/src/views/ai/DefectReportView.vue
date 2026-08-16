<template>
  <section class="page report-page">
    <div class="page-header"><h1>缺陷检测报表</h1><div class="header-actions"><el-button @click="selectAll">全选</el-button><el-button type="primary" :disabled="!selectedIds.length" :icon="Download" @click="exportSelected">导出</el-button></div></div>
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query" class="report-filter">
        <el-form-item label="ID搜索"><el-input v-model="query.defectCode" clearable placeholder="缺陷ID" /></el-form-item>
        <el-form-item label="名称搜索"><el-input v-model="query.taskName" clearable placeholder="任务名称" /></el-form-item>
        <el-form-item label="创建者"><el-input v-model="query.creatorName" clearable placeholder="创建者" /></el-form-item>
        <el-form-item label="结果确认"><el-select v-model="query.processStatus" clearable placeholder="全部"><el-option label="未核对" value="PENDING" /><el-option label="确认" value="CONFIRMED" /><el-option label="误报" value="FALSE_POSITIVE" /><el-option label="已关闭" value="CLOSED" /></el-select></el-form-item>
        <el-form-item class="filter-actions"><el-button type="primary" :icon="Search" @click="search">查询</el-button><el-button :icon="RefreshLeft" @click="reset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" class="table-card">
      <el-table ref="tableRef" v-loading="loading" :data="records" stripe row-key="id" empty-text="暂无数据" @selection-change="handleSelection">
        <el-table-column type="selection" width="58" /><el-table-column prop="defectCode" label="缺陷ID" width="130" /><el-table-column prop="taskNameSnapshot" label="缺陷检查任务名称" min-width="220" show-overflow-tooltip /><el-table-column prop="creatorName" label="创建者" width="120"><template #default="{ row }">{{ row.creatorName || '-' }}</template></el-table-column><el-table-column prop="createdAt" label="创建时间" width="175"><template #default="{ row }">{{ formatTime(row.createdAt) }}</template></el-table-column><el-table-column prop="defectProblem" label="缺陷问题" min-width="270" show-overflow-tooltip /><el-table-column prop="processStatus" label="检测结果确认" width="140"><template #default="{ row }"><el-tag :type="statusType(row.processStatus)" round>{{ statusText(row.processStatus) }}</el-tag></template></el-table-column><el-table-column label="操作" width="150" fixed="right"><template #default="{ row }"><el-button type="primary" link :icon="View" @click="openDetail(row)">查看详情</el-button></template></el-table-column>
      </el-table>
      <div class="table-footer"><el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" :total="total" @change="load" /></div>
    </el-card>

    <el-dialog v-model="detailVisible" title="缺陷检测报表详情" width="780px" class="report-detail-dialog" destroy-on-close>
      <template v-if="detail">
        <div class="summary-grid"><div><label>任务名称</label><strong>{{ detail.taskNameSnapshot || '-' }}</strong></div><div><label>创建者</label><strong>{{ detail.creatorName || '-' }}</strong></div><div><label>创建时间</label><strong>{{ formatTime(detail.createdAt) }}</strong></div><div><label>检测对象</label><strong>{{ detail.targetType === 'DB_RESOURCE' ? '分布式数据库' : '业务系统' }} / {{ detail.targetNameSnapshot || '-' }}</strong></div></div>
        <article class="detail-section problem-section"><h3>缺陷问题</h3><p>{{ detail.defectProblem || '-' }}</p></article>
        <article class="detail-section data-section"><h3>缺陷数据</h3><pre>{{ detail.defectData || '-' }}</pre></article>
        <article class="detail-section description-section"><h3>缺陷描述</h3><p>{{ detail.defectDescription || '-' }}</p></article>
      </template>
      <template #footer><div class="dialog-footer"><el-button @click="detailVisible = false">关闭</el-button><el-button type="primary" :icon="Download" @click="detail && exportReports([detail.id])">导出报表</el-button></div></template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Download, RefreshLeft, Search, View } from '@element-plus/icons-vue'
import { ElMessage, type TableInstance } from 'element-plus'
import { exportDefectReports, getDefectReportDetail, getDefectReports, type DefectReportItem, type ProcessStatus } from '@/api/modules/ai'

const loading = ref(false); const total = ref(0); const records = ref<DefectReportItem[]>([]); const selectedIds = ref<string[]>([]); const detailVisible = ref(false); const detail = ref<DefectReportItem | null>(null); const tableRef = ref<TableInstance>()
const query = reactive({ pageNo: 1, pageSize: 10, defectCode: '', taskName: '', creatorName: '', processStatus: '' as ProcessStatus | '' })
function formatTime(value?: string) { return value ? value.replace('T', ' ').slice(0, 16) : '-' }
function statusText(value?: string) { return ({ PENDING: '未核对', CONFIRMED: '确认', FALSE_POSITIVE: '误报', CLOSED: '已关闭' } as Record<string, string>)[value || ''] || '-' }
function statusType(value?: string) { return value === 'CONFIRMED' ? 'success' : value === 'FALSE_POSITIVE' ? 'warning' : value === 'CLOSED' ? 'info' : 'info' }
async function load() { loading.value = true; try { const page = await getDefectReports({ pageNo: query.pageNo, pageSize: query.pageSize, defectCode: query.defectCode || undefined, taskName: query.taskName || undefined, creatorName: query.creatorName || undefined, processStatus: query.processStatus || undefined }); records.value = page.records; total.value = page.total; selectedIds.value = [] } finally { loading.value = false } }
function search() { query.pageNo = 1; load() }
function reset() { query.defectCode = ''; query.taskName = ''; query.creatorName = ''; query.processStatus = ''; query.pageNo = 1; load() }
function handleSelection(rows: DefectReportItem[]) { selectedIds.value = rows.map(item => item.id) }
function selectAll() { records.value.forEach(row => tableRef.value?.toggleRowSelection(row, true)) }
async function openDetail(row: DefectReportItem) { detail.value = await getDefectReportDetail(row.id); detailVisible.value = true }
async function exportSelected() { await exportReports(selectedIds.value) }
async function exportReports(ids: string[]) { if (!ids.length) return; const response = await exportDefectReports(ids); const link = document.createElement('a'); link.href = URL.createObjectURL(response.data); link.download = '缺陷检测报表.csv'; link.click(); URL.revokeObjectURL(link.href); ElMessage.success('导出成功') }
load()
</script>

<style scoped>
.report-page{min-width:0}.page-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:20px}.page-header h1{margin:0;font-size:22px;color:#1f2937;font-weight:600}.header-actions{display:flex;gap:10px}.filter-card{margin-bottom:16px;border-radius:8px}.report-filter:deep(.el-form-item){margin-right:18px;margin-bottom:0}.report-filter:deep(.el-input){width:150px}.report-filter:deep(.el-select){width:150px}.table-footer{display:flex;justify-content:flex-end;padding:16px 0 0}.report-detail-dialog:deep(.el-dialog){border-radius:14px;overflow:hidden}.report-detail-dialog:deep(.el-dialog__body){padding:0 22px 20px}.summary-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px 48px;padding:18px 14px 22px;background:#fbfcfe;border-radius:12px}.summary-grid label{display:block;margin-bottom:8px;color:#9aabc1;font-size:13px}.summary-grid strong{color:#25324a;font-size:14px;font-weight:600}.detail-section{margin-top:20px;padding:20px 22px;border:1px solid #dce6f5;border-radius:14px;box-shadow:0 5px 16px rgba(36,89,166,.07)}.detail-section h3{margin:0 0 18px;padding-left:11px;border-left:4px solid #2f6fe4;color:#1e293b;font-size:18px}.detail-section p{margin:0;color:#334155;font-size:14px;line-height:1.8}.data-section h3{border-left-color:#ff7a00}.detail-section pre{margin:0;padding:15px 16px;overflow:auto;border-radius:12px;background:#1d1f24;color:#e7ebf2;font:13px/1.65 Consolas,monospace;white-space:pre-wrap}.dialog-footer{display:flex;justify-content:flex-end;gap:12px}.dialog-footer:deep(.el-button--primary){border-radius:10px}@media(max-width:720px){.summary-grid{grid-template-columns:1fr}.report-filter:deep(.el-form-item){margin-bottom:12px}}
</style>
