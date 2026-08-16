<template>
  <section class="page knowledge-page">
    <div class="page-header">
      <h1>缺陷知识库管理</h1>
      <el-button v-if="canCreate" type="primary" :icon="Plus" @click="openCreateDrawer">新增缺陷知识</el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query" class="knowledge-filter">
        <el-form-item label="ID搜索"><el-input v-model="query.knowledgeId" clearable placeholder="知识ID" /></el-form-item>
        <el-form-item label="名称搜索"><el-input v-model="query.knowledgeName" clearable placeholder="知识名称" /></el-form-item>
        <el-form-item label="创建者搜索"><el-input v-model="query.creatorName" clearable placeholder="创建者" /></el-form-item>
        <el-form-item label="创建时间"><el-date-picker v-model="query.createdDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" /></el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="records" stripe row-key="id" empty-text="暂无数据">
        <el-table-column prop="knowledgeCode" label="知识ID" width="190" />
        <el-table-column prop="knowledgeName" label="知识名称" min-width="290" show-overflow-tooltip />
        <el-table-column prop="creatorName" label="创建者" width="150"><template #default="{ row }">{{ row.creatorName || '-' }}</template></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180"><template #default="{ row }">{{ formatTime(row.createdAt) }}</template></el-table-column>
        <el-table-column prop="updatedAt" label="最新修改时间" width="180"><template #default="{ row }">{{ formatTime(row.updatedAt) }}</template></el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canUpdate" type="primary" link :icon="Edit" @click="openEditDrawer(row)">编辑</el-button>
            <el-button v-if="canDelete" type="danger" link :icon="Delete" @click="removeKnowledge(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" :total="total" @change="loadData" />
      </div>
    </el-card>

    <el-drawer v-model="drawerVisible" :title="drawerMode === 'create' ? '新增缺陷知识' : '编辑缺陷知识'" size="620px" destroy-on-close @closed="resetDrawer">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="知识名称" prop="knowledgeName"><el-input v-model="form.knowledgeName" maxlength="150" show-word-limit placeholder="请输入知识名称" /></el-form-item>
        <el-form-item label="知识内容" prop="knowledgeContent"><el-input v-model="form.knowledgeContent" type="textarea" :rows="8" maxlength="10000" show-word-limit placeholder="请详细描述缺陷现象、影响和处理建议..." /></el-form-item>
        <el-form-item label="附件上传" prop="attachments">
          <div class="attachment-area">
            <div v-if="existingAttachments.length" class="attachment-list">
              <div v-for="item in existingAttachments" :key="item.id" class="attachment-row">
                <div class="attachment-name"><el-icon><Paperclip /></el-icon><span>{{ item.originalFilename }}</span><small>{{ formatSize(item.fileSize) }}</small></div>
                <div class="attachment-actions">
                  <el-button v-if="item.previewable" type="primary" link @click="previewAttachment(item)">预览</el-button>
                  <el-button type="primary" link @click="downloadAttachment(item)">下载</el-button>
                  <el-button v-if="canUpdate" type="danger" link @click="removeExistingAttachment(item.id)">删除</el-button>
                </div>
              </div>
            </div>
            <el-upload v-if="remainingFileSlots > 0" class="knowledge-upload" v-model:file-list="newFiles" drag multiple :auto-upload="false" :show-file-list="false" :limit="remainingFileSlots" accept=".jpg,.jpeg,.png,.gif,.webp,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.md,.zip,.rar,.7z" :on-change="validateNewFile" :on-exceed="handleExceed">
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="upload-title">点击上传附件</div>
              <div class="upload-tip">支持图片、PDF、文档、压缩包等格式</div>
            </el-upload>
            <div v-if="newFiles.length" class="pending-file-list" aria-label="待上传附件">
              <div v-for="item in newFiles" :key="item.uid" class="pending-file-row">
                <div class="attachment-name"><el-icon><Paperclip /></el-icon><span>{{ item.name }}</span><small>{{ item.raw ? formatSize(item.raw.size) : '' }}</small></div>
                <el-button type="danger" link :icon="Delete" aria-label="移除待上传附件" @click="removeNewFile(item.uid)">删除</el-button>
              </div>
            </div>
            <div class="upload-limit-tip">单个文件不超过 20MB，最多 10 个附件</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer><div class="drawer-footer"><el-button @click="drawerVisible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="submitForm">{{ drawerMode === 'create' ? '提交保存' : '保存更新' }}</el-button></div></template>
    </el-drawer>

    <el-dialog v-model="previewVisible" title="图片预览" width="760px" destroy-on-close @closed="revokePreviewUrl">
      <img v-if="previewUrl" :src="previewUrl" class="preview-image" alt="附件预览" />
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { Delete, Edit, Paperclip, Plus, RefreshLeft, Search, UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type UploadFile, type UploadUserFile } from 'element-plus'
import { createDefectKnowledge, deleteKnowledge, fetchKnowledgeAttachment, getDefectKnowledge, getDefectKnowledgeDetail, type DefectKnowledgeQuery, type FaultKnowledgeItem, type KnowledgeAttachmentItem, updateDefectKnowledge } from '@/api/modules/ai'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const total = ref(0)
const records = ref<FaultKnowledgeItem[]>([])
const drawerVisible = ref(false)
const drawerMode = ref<'create' | 'edit'>('create')
const editingId = ref('')
const formRef = ref<FormInstance>()
const existingAttachments = ref<KnowledgeAttachmentItem[]>([])
const newFiles = ref<UploadUserFile[]>([])
const previewVisible = ref(false)
const previewUrl = ref('')
const query = reactive<DefectKnowledgeQuery>({ pageNo: 1, pageSize: 10, knowledgeId: '', knowledgeName: '', creatorName: '', createdDate: '', knowledgeType: 'DEFECT' })
const form = reactive({ knowledgeName: '', knowledgeContent: '' })
const rules = {
  knowledgeName: [{ required: true, message: '请输入知识名称', trigger: 'blur' }],
  knowledgeContent: [{ required: true, message: '请输入知识内容', trigger: 'blur' }]
}
const canCreate = computed(() => userStore.hasPermission('ai:knowledge:create'))
const canUpdate = computed(() => userStore.hasPermission('ai:knowledge:update'))
const canDelete = computed(() => userStore.hasPermission('ai:knowledge:delete'))
const remainingFileSlots = computed(() => Math.max(0, 10 - existingAttachments.value.length))

function formatTime(value?: string) { return value ? value.replace('T', ' ').slice(0, 16) : '-' }
function formatSize(value: number) { return value < 1024 * 1024 ? `${Math.ceil(value / 1024)} KB` : `${(value / (1024 * 1024)).toFixed(1)} MB` }
async function loadData() {
  loading.value = true
  try {
    const page = await getDefectKnowledge({ ...query, knowledgeId: query.knowledgeId || undefined, knowledgeName: query.knowledgeName || undefined, creatorName: query.creatorName || undefined, createdDate: query.createdDate || undefined })
    records.value = page.records
    total.value = page.total
  } finally { loading.value = false }
}
function handleSearch() { query.pageNo = 1; loadData() }
function resetSearch() { query.knowledgeId = ''; query.knowledgeName = ''; query.creatorName = ''; query.createdDate = ''; query.pageNo = 1; loadData() }
function openCreateDrawer() { drawerMode.value = 'create'; drawerVisible.value = true }
async function openEditDrawer(row: FaultKnowledgeItem) {
  const detail = await getDefectKnowledgeDetail(row.id)
  if (detail.knowledgeType !== 'DEFECT') { ElMessage.error('知识类型不匹配，无法编辑'); return }
  drawerMode.value = 'edit'; editingId.value = row.id; form.knowledgeName = detail.knowledgeName; form.knowledgeContent = detail.knowledgeContent; existingAttachments.value = detail.attachments || []; drawerVisible.value = true
}
function resetDrawer() { editingId.value = ''; form.knowledgeName = ''; form.knowledgeContent = ''; existingAttachments.value = []; newFiles.value = []; formRef.value?.clearValidate() }
function removeExistingAttachment(id: string) { existingAttachments.value = existingAttachments.value.filter(item => item.id !== id) }
function removeNewFile(uid?: number) { if (uid != null) newFiles.value = newFiles.value.filter(item => item.uid !== uid) }
function handleExceed() { ElMessage.warning('每条知识最多保留10个附件') }
function validateNewFile(file: UploadFile) {
  const raw = file.raw
  if (!raw) return
  const extension = raw.name.split('.').pop()?.toLowerCase() || ''
  const allowed = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'md', 'zip', 'rar', '7z']
  if (!allowed.includes(extension) || raw.size > 20 * 1024 * 1024) {
    newFiles.value = newFiles.value.filter(item => item.uid !== file.uid)
    ElMessage.error(!allowed.includes(extension) ? '附件格式不受支持' : '单个附件不能超过20MB')
  }
}
function buildPayload() {
  const data = new FormData()
  data.append('knowledgeName', form.knowledgeName.trim())
  data.append('knowledgeContent', form.knowledgeContent.trim())
  data.append('knowledgeType', 'DEFECT')
  existingAttachments.value.forEach(item => data.append('retainedAttachmentIds', item.id))
  newFiles.value.forEach(item => { if (item.raw) data.append('files', item.raw) })
  return data
}
async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!existingAttachments.value.length && !newFiles.value.length) { ElMessage.warning('请至少上传一个附件'); return }
  submitting.value = true
  try {
    const payload = buildPayload()
    if (drawerMode.value === 'create') await createDefectKnowledge(payload)
    else await updateDefectKnowledge(editingId.value, payload)
    ElMessage.success('保存成功'); drawerVisible.value = false; await loadData()
  } finally { submitting.value = false }
}
async function removeKnowledge(row: FaultKnowledgeItem) {
  await ElMessageBox.confirm(`确定删除「${row.knowledgeName}」吗？删除后可由后续清理与恢复流程处理。`, '确认删除', { type: 'warning' })
  await deleteKnowledge(row.id); ElMessage.success('删除成功'); await loadData()
}
async function previewAttachment(item: KnowledgeAttachmentItem) {
  const response = await fetchKnowledgeAttachment(item.id, true)
  previewUrl.value = URL.createObjectURL(response.data); previewVisible.value = true
}
function revokePreviewUrl() { if (previewUrl.value) URL.revokeObjectURL(previewUrl.value); previewUrl.value = '' }
async function downloadAttachment(item: KnowledgeAttachmentItem) {
  const response = await fetchKnowledgeAttachment(item.id)
  const link = document.createElement('a'); link.href = URL.createObjectURL(response.data); link.download = item.originalFilename; link.click(); URL.revokeObjectURL(link.href)
}
loadData()
</script>

<style scoped>
.knowledge-page { min-width: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-header h1 { margin: 0; font-size: 22px; color: #1f2937; font-weight: 600; }
.filter-card, .table-card { border-radius: 8px; }
.filter-card { margin-bottom: 16px; }
.knowledge-filter :deep(.el-form-item) { margin-bottom: 0; margin-right: 18px; }
.knowledge-filter :deep(.el-input) { width: 150px; }
.knowledge-filter :deep(.el-date-editor) { width: 168px; }
.knowledge-filter .filter-actions { margin-right: 0; }
.table-footer { display: flex; justify-content: flex-end; padding: 16px 0 0; }
.attachment-area { width: 100%; }
.attachment-list, .pending-file-list { border: 1px solid var(--el-border-color-lighter); border-radius: 6px; }
.attachment-list { margin-bottom: 12px; }
.attachment-row, .pending-file-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; min-height: 42px; padding: 0 12px; border-bottom: 1px solid var(--el-border-color-lighter); }
.attachment-row:last-child, .pending-file-row:last-child { border-bottom: 0; }
.attachment-name { display: flex; align-items: center; gap: 7px; min-width: 0; color: #334155; }
.attachment-name span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.attachment-name small { color: #94a3b8; white-space: nowrap; }
.attachment-actions { flex: none; }
.knowledge-upload { display: block; width: 100%; }
.knowledge-upload :deep(.el-upload), .knowledge-upload :deep(.el-upload-dragger) { width: 100%; }
.knowledge-upload :deep(.el-upload-dragger) { box-sizing: border-box; height: 112px; padding: 20px 16px; border: 2px dashed #7c9cf5; border-radius: 12px; background: #f7f9ff; }
.knowledge-upload :deep(.el-upload-dragger:hover), .knowledge-upload :deep(.is-dragover) { border-color: #356ae6; background: #eef3ff; }
.upload-icon { margin-bottom: 4px; font-size: 28px; color: #8da0b9; }
.upload-title { color: #7186a3; font-size: 14px; font-weight: 600; line-height: 22px; }
.upload-tip, .upload-limit-tip { color: #9aa9bd; font-size: 12px; line-height: 20px; }
.upload-limit-tip { margin-top: 7px; }
.pending-file-list { margin-top: 12px; }
.drawer-footer { display: flex; justify-content: flex-end; gap: 12px; }
.preview-image { display: block; max-width: 100%; max-height: 70vh; margin: 0 auto; object-fit: contain; }
@media (max-width: 960px) { .knowledge-filter :deep(.el-form-item) { margin-bottom: 12px; } }
</style>
