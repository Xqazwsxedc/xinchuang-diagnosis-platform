<template>
  <section class="page task-page">
    <div class="page-header">
      <h1>缺陷检测任务配置</h1>
      <el-button v-if="canCreate" type="primary" :icon="Plus" @click="openCreate">新增检测任务</el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="任务名称"><el-input v-model="query.keyword" clearable placeholder="任务名称" /></el-form-item>
        <el-form-item label="创建者"><el-input v-model="query.creatorName" clearable placeholder="创建者" /></el-form-item>
        <el-form-item label="创建日期"><el-date-picker v-model="query.createdDate" type="date" value-format="YYYY-MM-DD" clearable placeholder="请选择日期" /></el-form-item>
        <el-form-item label="AI检测模型"><el-select v-model="query.modelId" clearable placeholder="全部"><el-option v-for="item in models" :key="item.id" :label="item.modelName" :value="item.id" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="search">查询</el-button><el-button :icon="RefreshLeft" @click="reset">重置</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="records" stripe row-key="id" empty-text="暂无数据">
        <el-table-column prop="taskName" label="任务名称" min-width="210" show-overflow-tooltip />
        <el-table-column prop="creatorName" label="创建者" width="120"><template #default="{ row }">{{ row.creatorName || '-' }}</template></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="175"><template #default="{ row }">{{ formatTime(row.createdAt) }}</template></el-table-column>
        <el-table-column prop="targetNameSnapshot" label="检测对象" min-width="190" show-overflow-tooltip />
        <el-table-column prop="modelNameSnapshot" label="AI检测模型" min-width="170" show-overflow-tooltip />
        <el-table-column prop="lastExecTime" label="上一次检测时间" width="175"><template #default="{ row }">{{ formatTime(row.lastExecTime) }}</template></el-table-column>
        <el-table-column label="任务次数 / 缺陷数量" width="160"><template #default="{ row }">{{ row.executionCount || 0 }} / {{ row.resultCount || 0 }}</template></el-table-column>
        <el-table-column label="操作" width="290" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="openDetail(row)">查看</el-button>
            <el-button v-if="canUpdate" type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="canExecute" type="success" link :icon="VideoPlay" @click="execute(row)">立即检测</el-button>
            <el-button v-if="canDelete" type="danger" link :icon="Delete" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer"><el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" :total="total" @change="load" /></div>
    </el-card>

    <el-drawer v-model="formVisible" :title="editingId ? '编辑检测任务' : '新增检测任务'" size="580px" destroy-on-close @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="任务名称" prop="taskName"><el-input v-model="form.taskName" maxlength="150" show-word-limit placeholder="请输入任务名称" /></el-form-item>
        <el-form-item label="检测对象" prop="targetId">
          <div class="target-row"><el-select v-model="form.targetType" @change="form.targetId = ''"><el-option label="业务系统" value="BUSINESS_SYSTEM" /><el-option label="分布式数据库" value="DB_RESOURCE" /></el-select><el-select v-model="form.targetId" filterable placeholder="请选择具体对象"><el-option v-for="item in targetOptions" :key="item.id" :label="item.name" :value="item.id" /></el-select></div>
        </el-form-item>
        <el-form-item label="AI检测模型" prop="modelId"><el-select v-model="form.modelId" filterable placeholder="请选择启用的缺陷检测模型" style="width:100%"><el-option v-for="item in models" :key="item.id" :label="item.modelName" :value="item.id" /></el-select><div class="model-tip">{{ selectedModel?.description || '轻量级模型速度快，适合快速检测；深度模型准确度高，适合全面分析' }}</div></el-form-item>
        <el-form-item label="定时检测设置" prop="schedules" required>
          <div class="schedule-label" v-if="form.schedules.length">已添加 {{ form.schedules.length }} 条定时规则：</div>
          <div v-for="(item, index) in form.schedules" :key="item.id" class="schedule-row"><span><el-icon><Clock /></el-icon>{{ scheduleText(item) }}</span><el-button link type="danger" :icon="Delete" aria-label="删除定时规则" @click="form.schedules.splice(index, 1)" /></div>
          <div class="schedule-builder">
            <div class="schedule-builder-title">添加定时规则</div>
            <label class="schedule-field"><span>时间</span><input v-model="scheduleDraft.time" type="time" /></label>
            <label class="schedule-field"><span>检测范围</span><el-select v-model="scheduleDraft.detectionRange"><el-option v-for="item in detectionRanges" :key="item.value" :label="item.label" :value="item.value" /></el-select></label>
            <div class="schedule-field"><span>重复周期</span><el-radio-group v-model="scheduleDraft.repeatType" class="repeat-type-control"><el-radio-button label="daily">每日</el-radio-button><el-radio-button label="weekly">每周</el-radio-button></el-radio-group></div>
            <el-checkbox-group v-if="scheduleDraft.repeatType === 'weekly'" v-model="scheduleDraft.weekDays" class="week-days"><el-checkbox v-for="day in weekDays" :key="day" :label="day">{{ day }}</el-checkbox></el-checkbox-group>
            <el-button plain type="primary" :icon="Plus" class="add-schedule-button" @click="addSchedule">+ 添加此定时规则</el-button>
          </div>
        </el-form-item>
        <el-form-item label="创建者"><el-input :model-value="userStore.realName || userStore.username || '-'" disabled /><div class="model-tip">创建者信息自动填充，不可修改</div></el-form-item>
      </el-form>
      <template #footer><div class="drawer-footer"><el-button @click="formVisible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="save">{{ editingId ? '保存更新' : '提交保存' }}</el-button></div></template>
    </el-drawer>

    <el-drawer v-model="detailVisible" title="查看检测任务详情" size="580px" destroy-on-close class="detail-drawer">
      <div v-if="detail" class="detail-form">
        <div class="detail-field"><label>任务名称</label><el-input :model-value="detail.taskName || '-'" disabled /></div>
        <div class="detail-field"><label>检测对象</label><el-input :model-value="`${detail.targetType === 'DB_RESOURCE' ? '分布式数据库' : '业务系统'} / ${detail.targetNameSnapshot || '-'}`" disabled /></div>
        <div class="detail-field"><label>创建者</label><el-input :model-value="detail.creatorName || '-'" disabled /></div>
        <div class="detail-field"><label>创建时间</label><el-input :model-value="formatTime(detail.createdAt)" disabled /></div>
        <div class="detail-field"><label>任务次数</label><el-input :model-value="`${detail.executionCount || 0} 次`" disabled /></div>
        <div class="detail-field"><label>上一次检测时间</label><el-input :model-value="formatTime(detail.lastExecTime)" disabled /></div>
        <div class="detail-field"><label>AI检测模型</label><el-input :model-value="detail.modelNameSnapshot || '-'" disabled /></div>
        <div class="detail-field schedule-detail-field"><label>定时规则</label><div v-if="detailSchedules.length" class="detail-schedule-list"><div v-for="item in detailSchedules" :key="item.id" class="detail-schedule-row"><el-icon><Clock /></el-icon><span>{{ scheduleText(item) }}</span></div></div><el-input v-else model-value="-" disabled /></div>
      </div>
      <template #footer><div class="drawer-footer"><el-button type="primary" @click="detailVisible = false">关闭</el-button></div></template>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { Clock, Delete, Edit, Plus, RefreshLeft, Search, VideoPlay, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { createAiTask, deleteAiTask, executeAiTask, getAiModels, getAiTaskDetail, getAiTasks, updateAiTask, type AiModelItem, type AiTaskItem, type AiTaskType, type TargetType } from '@/api/modules/ai'
import { getBusinessSystems, getDbResources } from '@/api/modules/ledger'
import { useUserStore } from '@/stores/user'

type Schedule = { id: string; time: string; repeatType: 'daily' | 'weekly'; weekDays: string[]; detectionRange: string }
const detectionRanges = [{ label: '只检测前一天的', value: 'PREVIOUS_DAY' }, { label: '只检测当天的', value: 'CURRENT_DAY' }, { label: '检测前两天的', value: 'PREVIOUS_TWO_DAYS' }]
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const userStore = useUserStore(); const loading = ref(false); const submitting = ref(false); const total = ref(0); const records = ref<AiTaskItem[]>([]); const models = ref<AiModelItem[]>([]); const systems = ref<{id:string;name:string}[]>([]); const databases = ref<{id:string;name:string}[]>([]); const formVisible = ref(false); const detailVisible = ref(false); const editingId = ref(''); const detail = ref<AiTaskItem | null>(null); const formRef = ref<FormInstance>()
const query = reactive({ pageNo: 1, pageSize: 10, keyword: '', creatorName: '', createdDate: '', modelId: '' })
const form = reactive({ taskCode: '', taskName: '', taskType: 'DEFECT_DETECTION', targetType: 'BUSINESS_SYSTEM' as TargetType, targetId: '', modelId: '', schedules: [] as Schedule[] })
const scheduleDraft = reactive({ time: '08:00', repeatType: 'daily' as 'daily' | 'weekly', weekDays: [] as string[], detectionRange: 'PREVIOUS_DAY' })
const rules = { taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }], targetId: [{ required: true, message: '请选择检测对象', trigger: 'change' }], modelId: [{ required: true, message: '请选择AI检测模型', trigger: 'change' }] }
const canCreate = computed(() => userStore.hasPermission('ai:task:create')); const canUpdate = computed(() => userStore.hasPermission('ai:task:update')); const canDelete = computed(() => userStore.hasPermission('ai:task:delete')); const canExecute = computed(() => userStore.hasPermission('ai:task:execute')); const targetOptions = computed(() => form.targetType === 'DB_RESOURCE' ? databases.value : systems.value); const selectedModel = computed(() => models.value.find(item => item.id === form.modelId)); const detailSchedules = computed(() => parseSchedules(detail.value?.scheduleRule))
function formatTime(value?: string) { return value ? value.replace('T', ' ').slice(0, 19) : '-' }
function parseSchedules(value?: string): Schedule[] { try { const parsed = JSON.parse(value || '[]'); return Array.isArray(parsed) ? parsed.map((item, index) => ({ id: item.id || `rule-${index}`, time: item.time || '08:00', repeatType: item.repeatType === 'weekly' ? 'weekly' : 'daily', weekDays: Array.isArray(item.weekDays) ? item.weekDays : [], detectionRange: item.detectionRange || 'PREVIOUS_DAY' })) : [] } catch { return [] } }
function scheduleText(item: Schedule) { const range = detectionRanges.find(option => option.value === item.detectionRange)?.label || detectionRanges[0].label; return item.repeatType === 'daily' ? `每日 ${item.time}，${range}` : `每周 ${item.weekDays.join('、')} ${item.time}，${range}` }
async function loadOptions() { const [modelPage, systemPage, dbPage] = await Promise.all([getAiModels({ pageNo: 1, pageSize: 200, modelType: 'DEFECT_DETECTION', status: 'ENABLED' }), getBusinessSystems({ pageNo: 1, pageSize: 200 }), getDbResources({ pageNo: 1, pageSize: 200 })]); models.value = modelPage.records; systems.value = systemPage.records.map(item => ({ id: item.id, name: item.name })); databases.value = dbPage.records.map(item => ({ id: item.id, name: item.name })) }
async function load() { loading.value = true; try { const page = await getAiTasks({ ...query, taskType: 'DEFECT_DETECTION' }); records.value = page.records; total.value = page.total } finally { loading.value = false } }
function search() { query.pageNo = 1; load() }
function reset() { query.keyword = ''; query.creatorName = ''; query.createdDate = ''; query.modelId = ''; query.pageNo = 1; load() }
function resetForm() { editingId.value = ''; form.taskCode = ''; form.taskName = ''; form.taskType = 'DEFECT_DETECTION'; form.targetType = 'BUSINESS_SYSTEM'; form.targetId = ''; form.modelId = ''; form.schedules = []; scheduleDraft.time = '08:00'; scheduleDraft.repeatType = 'daily'; scheduleDraft.weekDays = []; scheduleDraft.detectionRange = 'PREVIOUS_DAY'; formRef.value?.clearValidate() }
function openCreate() { resetForm(); formVisible.value = true }
function openEdit(row: AiTaskItem) { editingId.value = row.id; form.taskCode = row.taskCode; form.taskName = row.taskName; form.targetType = row.targetType; form.targetId = row.targetId; form.modelId = row.modelId || ''; form.schedules = parseSchedules(row.scheduleRule); formVisible.value = true }
async function openDetail(row: AiTaskItem) { detailVisible.value = true; try { detail.value = await getAiTaskDetail(row.id) } catch { detailVisible.value = false } }
function addSchedule() { if (scheduleDraft.repeatType === 'weekly' && !scheduleDraft.weekDays.length) { ElMessage.warning('每周规则至少选择一个星期'); return } if (form.schedules.some(item => item.time === scheduleDraft.time && item.repeatType === scheduleDraft.repeatType && item.weekDays.join(',') === scheduleDraft.weekDays.join(','))) { ElMessage.warning('该定时规则已存在'); return } form.schedules.push({ id: `rule-${Date.now()}`, time: scheduleDraft.time, repeatType: scheduleDraft.repeatType, weekDays: [...scheduleDraft.weekDays], detectionRange: scheduleDraft.detectionRange }) }
async function save() { const valid = await formRef.value?.validate().catch(() => false); if (!valid) return; if (!form.schedules.length) { ElMessage.warning('请至少添加一条定时检测规则'); return } submitting.value = true; try { const taskType: AiTaskType = 'DEFECT_DETECTION'; const payload = { taskCode: form.taskCode, taskName: form.taskName.trim(), taskType, modelId: form.modelId, targetType: form.targetType, targetId: form.targetId, scheduleRule: JSON.stringify(form.schedules.map(({ id, ...item }) => item)), metricCodes: '', status: 'ENABLED' as const }; if (editingId.value) await updateAiTask(editingId.value, payload); else await createAiTask(payload); ElMessage.success('保存成功'); formVisible.value = false; await load() } finally { submitting.value = false } }
async function execute(row: AiTaskItem) { await ElMessageBox.confirm(`确认立即检测「${row.taskName}」吗？当前将创建检测记录，等待检测引擎处理。`, '立即检测', { type: 'warning' }); await executeAiTask(row.id); ElMessage.success('检测任务已受理，等待检测引擎处理'); await load() }
async function remove(row: AiTaskItem) { await ElMessageBox.confirm(`确认删除「${row.taskName}」吗？`, '删除任务', { type: 'warning' }); await deleteAiTask(row.id); ElMessage.success('删除成功'); await load() }
loadOptions().then(load)
</script>

<style scoped>
.task-page{min-width:0}.page-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:20px}.page-header h1{margin:0;font-size:22px;color:#1f2937;font-weight:600}.filter-card{margin-bottom:16px;border-radius:8px}.table-footer{display:flex;justify-content:flex-end;padding:16px 0 0}.target-row{display:flex;gap:10px;width:100%}.target-row>*:first-child{width:34%}.target-row>*:last-child{flex:1}.model-tip,.schedule-label{margin-top:6px;color:#94a3b8;font-size:12px;line-height:20px}.schedule-form-item{width:100%}.schedule-form-item:deep(.el-form-item__content){display:block;width:100%;min-width:0}.schedule-label,.schedule-row,.schedule-builder{box-sizing:border-box;width:100%}.schedule-row{display:flex;align-items:center;justify-content:space-between;margin-top:8px;padding:11px 12px;border:1px solid #bfd0fb;border-radius:10px;background:#f5f8ff;color:#1f2937}.schedule-row span{display:flex;align-items:center;gap:8px;font-size:13px;min-width:0}.schedule-builder{display:grid;gap:14px;margin-top:14px;padding:14px;border:1px solid #dbe4f5;border-radius:10px;background:#fbfcff}.schedule-builder-title{font-size:14px;font-weight:600;color:#334155}.schedule-field{display:grid;gap:8px;color:#334155;font-size:13px;font-weight:600;width:100%}.schedule-field input{box-sizing:border-box;width:100%;height:36px;padding:0 12px;border:1px solid #d7e1f1;border-radius:10px;background:#fff;color:#334155}.schedule-field:deep(.el-select){width:100%}.repeat-type-control{display:flex;width:100%}.repeat-type-control:deep(.el-radio-button){flex:1}.repeat-type-control:deep(.el-radio-button__inner){width:100%;border-radius:10px!important;box-shadow:none}.repeat-type-control:deep(.el-radio-button:first-child .el-radio-button__inner){border-left:1px solid #d7e1f1}.week-days{display:flex;flex-wrap:wrap;gap:10px;margin:0}.add-schedule-button{width:100%;margin-top:2px}.detail-drawer:deep(.el-drawer__body){padding:0 22px 88px;overflow-y:auto}.detail-drawer:deep(.el-drawer__footer){position:absolute;right:0;bottom:0;left:0;z-index:2;padding:16px 22px;background:#fff;border-top:1px solid #e8edf5}.detail-form{padding-top:4px}.detail-field{display:grid;gap:10px;margin-bottom:23px}.detail-field label{font-size:14px;line-height:20px;font-weight:600;color:#1f2937}.detail-field:deep(.el-input__wrapper){min-height:42px;padding:1px 14px;border:1px solid #dce5f4;border-radius:12px;background:#fbfcfe;box-shadow:none}.detail-field:deep(.el-input.is-disabled .el-input__inner){color:#7b8799;-webkit-text-fill-color:#7b8799}.schedule-detail-field{margin-bottom:0}.detail-schedule-list{display:grid;gap:8px}.detail-schedule-row{display:flex;align-items:center;gap:9px;min-height:42px;padding:0 14px;border:1px solid #dce5f4;border-radius:12px;background:#fbfcfe;color:#334155;font-size:13px}.detail-schedule-row .el-icon{color:#2459d6}.drawer-footer{display:flex;justify-content:flex-end;gap:12px}.drawer-footer:deep(.el-button--primary){min-width:70px;border-radius:12px}@media(max-width:720px){.target-row{flex-direction:column}.target-row>*:first-child{width:100%}}
</style>
