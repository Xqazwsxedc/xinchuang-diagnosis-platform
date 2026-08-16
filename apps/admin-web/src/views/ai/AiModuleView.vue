<template>
  <section class="page ai-page">
    <div class="page-header">
      <div>
        <h1>{{ pageTitle }}</h1>
        <p>{{ pageDescription }}</p>
      </div>
      <el-button v-if="showCreateButton" type="primary" :icon="Plus" @click="openCreateDrawer">{{ createText }}</el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query" class="ai-filter">
        <el-form-item label="名称搜索">
          <el-input v-model="query.keyword" clearable :placeholder="keywordPlaceholder" />
        </el-form-item>
        <el-form-item v-if="pageKind === 'result'" label="处理状态">
          <el-select v-model="query.processStatus" clearable placeholder="全部状态" style="width: 150px">
            <el-option label="未处理" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="误报" value="FALSE_POSITIVE" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="pageKind === 'result' && moduleKey !== 'optimization'" label="风险等级">
          <el-select v-model="query.level" clearable placeholder="全部等级" style="width: 150px">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="严重" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="ai-table-card">
      <template v-if="pageKind === 'knowledge'">
        <el-table :data="knowledgeRecords" stripe v-loading="loading" row-key="id">
          <el-table-column prop="knowledgeCode" label="知识ID" width="170" />
          <el-table-column prop="knowledgeName" label="知识名称" min-width="260" show-overflow-tooltip />
          <el-table-column prop="knowledgeType" label="知识类型" width="130">
            <template #default="{ row }">
              <el-tag type="primary" round>{{ knowledgeTypeText(row.knowledgeType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" round>{{ row.status === 'ENABLED' ? '启用' : '禁用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="最新修改时间" width="180">
            <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="210" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link :icon="View" @click="openKnowledgeDetail(row)">详情</el-button>
              <el-button v-if="canUpdate" type="primary" link :icon="Edit" @click="openKnowledgeEdit(row)">编辑</el-button>
              <el-button v-if="canDelete" type="danger" link :icon="Delete" @click="removeKnowledge(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <template v-else-if="pageKind === 'task'">
        <el-table :data="taskRecords" stripe v-loading="loading" row-key="id">
          <el-table-column prop="taskName" label="任务名称" min-width="230" show-overflow-tooltip />
          <el-table-column prop="targetNameSnapshot" label="预测/检测对象" min-width="190" show-overflow-tooltip />
          <el-table-column prop="modelNameSnapshot" label="AI模型" min-width="180" show-overflow-tooltip />
          <el-table-column label="任务次数/结果数量" width="150">
            <template #default="{ row }">
              <el-button type="primary" link @click="openExecutionDrawer(row)">{{ row.executionCount || 0 }}/{{ row.resultCount || 0 }}</el-button>
            </template>
          </el-table-column>
          <el-table-column prop="lastExecTime" label="上一次执行时间" width="180">
            <template #default="{ row }">{{ formatTime(row.lastExecTime) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" round>{{ row.status === 'ENABLED' ? '启用' : '禁用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="270" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link :icon="View" @click="openTaskDetail(row)">详情</el-button>
              <el-button v-if="canUpdate" type="primary" link :icon="Edit" @click="openTaskEdit(row)">编辑</el-button>
              <el-button v-if="canExecute" type="success" link :icon="VideoPlay" @click="runTask(row)">执行</el-button>
              <el-button v-if="canDelete" type="danger" link :icon="Delete" @click="removeTask(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <template v-else>
        <div v-if="moduleKey === 'fault'" class="result-cards" v-loading="loading">
          <article v-for="item in faultRecords" :key="item.id" class="result-card">
            <div class="result-card-main">
              <div class="result-title">
                <span class="code-chip">{{ item.reportCode }}</span>
                <h3>{{ item.targetNameSnapshot }}</h3>
              </div>
              <div class="result-meta">
                <span>告警时间：{{ formatTime(item.alertTime) }}</span>
                <span>指标类别：{{ item.indicatorCategory || '-' }}</span>
                <span>故障类型：{{ item.faultType }}</span>
                <el-tag :type="levelTag(item.riskLevel)" round>{{ levelText(item.riskLevel) }}</el-tag>
                <el-tag :type="processTag(item.processStatus)" round>{{ processText(item.processStatus) }}</el-tag>
              </div>
            </div>
            <el-button type="primary" :icon="View" @click="openReportDetail(item)">查看详情</el-button>
          </article>
        </div>

        <el-table v-else :data="reportRecords" stripe v-loading="loading" row-key="id">
          <el-table-column :prop="moduleKey === 'defect' ? 'defectCode' : 'reportCode'" label="编号" width="170" />
          <el-table-column prop="targetNameSnapshot" label="对象" min-width="180" show-overflow-tooltip />
          <el-table-column :prop="moduleKey === 'defect' ? 'defectType' : 'optimizationType'" label="类型" min-width="170" show-overflow-tooltip />
          <el-table-column :prop="moduleKey === 'defect' ? 'defectProblem' : 'optimizationProblem'" label="问题描述" min-width="260" show-overflow-tooltip />
          <el-table-column v-if="moduleKey === 'defect'" prop="severityLevel" label="严重等级" width="120">
            <template #default="{ row }"><el-tag :type="levelTag(row.severityLevel)" round>{{ levelText(row.severityLevel) }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="processStatus" label="处理状态" width="120">
            <template #default="{ row }"><el-tag :type="processTag(row.processStatus)" round>{{ processText(row.processStatus) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link :icon="View" @click="openReportDetail(row)">详情</el-button>
              <el-button v-if="canUpdate" type="primary" link :icon="Edit" @click="openReportEdit(row)">编辑</el-button>
              <el-button v-if="canDelete" type="danger" link :icon="Delete" @click="removeReport(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <div class="table-footer">
        <el-pagination
          v-model:current-page="query.pageNo"
          v-model:page-size="query.pageSize"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @change="loadData"
        />
      </div>
    </el-card>

    <el-drawer v-model="formVisible" :title="drawerTitle" size="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-position="top">
        <template v-if="pageKind === 'knowledge'">
          <el-form-item label="知识编码" required>
            <el-input v-model="form.knowledgeCode" maxlength="100" placeholder="例如 FAULT-KB-001" />
          </el-form-item>
          <el-form-item label="知识名称" required>
            <el-input v-model="form.knowledgeName" maxlength="150" placeholder="请输入知识名称" />
          </el-form-item>
          <el-form-item label="知识内容" required>
            <el-input v-model="form.knowledgeContent" type="textarea" :rows="8" placeholder="请按 Figma 原型录入知识描述、样本数据和建议" />
          </el-form-item>
          <el-form-item label="附件名称">
            <el-input v-model="form.attachmentNames" placeholder="多个附件可用逗号分隔，当前阶段仅保存名称" />
          </el-form-item>
        </template>

        <template v-else-if="pageKind === 'task'">
          <el-form-item label="任务编码" required>
            <el-input v-model="form.taskCode" maxlength="100" placeholder="例如 TASK-FAULT-001" />
          </el-form-item>
          <el-form-item label="任务名称" required>
            <el-input v-model="form.taskName" maxlength="150" placeholder="请输入任务名称" />
          </el-form-item>
          <el-form-item label="任务对象" required>
            <div class="inline-fields">
              <el-select v-model="form.targetType" placeholder="对象类型">
                <el-option label="业务系统" value="BUSINESS_SYSTEM" />
                <el-option label="分布式数据库" value="DB_RESOURCE" />
              </el-select>
              <el-select v-model="form.targetId" filterable placeholder="请选择具体对象">
                <el-option v-for="item in targetOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="AI模型">
            <el-select v-model="form.modelId" filterable clearable placeholder="请选择AI模型" style="width: 100%">
              <el-option v-for="item in modelOptions" :key="item.id" :label="item.modelName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="指标配置">
            <el-select v-model="metricSelections" multiple filterable collapse-tags collapse-tags-tooltip placeholder="请选择指标" style="width: 100%">
              <el-option v-for="metric in metricOptions" :key="metric" :label="metric" :value="metric" />
            </el-select>
          </el-form-item>
          <el-form-item label="定时规则">
            <el-input v-model="form.scheduleRule" type="textarea" :rows="4" placeholder='例如 [{"time":"08:00","repeatType":"daily"}]' />
          </el-form-item>
          <el-form-item label="任务参数 JSON">
            <el-input v-model="form.paramJson" type="textarea" :rows="4" placeholder="当前阶段作为数据壳保存" />
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="结果编码" required>
            <el-input v-model="form.code" maxlength="100" placeholder="请输入编号" />
          </el-form-item>
          <el-form-item label="目标对象" required>
            <div class="inline-fields">
              <el-select v-model="form.targetType" placeholder="对象类型">
                <el-option label="业务系统" value="BUSINESS_SYSTEM" />
                <el-option label="分布式数据库" value="DB_RESOURCE" />
              </el-select>
              <el-select v-model="form.targetId" filterable placeholder="请选择具体对象">
                <el-option v-for="item in targetOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item :label="resultTypeLabel" required>
            <el-input v-model="form.resultType" />
          </el-form-item>
          <el-form-item :label="problemLabel" required>
            <el-input v-model="form.problem" maxlength="300" />
          </el-form-item>
          <el-form-item v-if="moduleKey !== 'optimization'" label="风险/严重等级">
            <el-select v-model="form.level" style="width: 100%">
              <el-option label="低" value="LOW" />
              <el-option label="中" value="MEDIUM" />
              <el-option label="高" value="HIGH" />
              <el-option label="严重" value="CRITICAL" />
            </el-select>
          </el-form-item>
          <el-form-item label="置信度">
            <el-input-number v-model="form.confidence" :min="0" :max="100" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item v-if="moduleKey === 'optimization'" label="问题 SQL">
            <el-input v-model="form.sqlText" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item :label="suggestionLabel">
            <el-input v-model="form.suggestion" type="textarea" :rows="5" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="formVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">提交保存</el-button>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="detailVisible" :title="detailTitle" size="640px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item v-for="item in detailItems" :key="item.label" :label="item.label">
          <template v-if="item.tag">
            <el-tag :type="item.tag.type" round>{{ item.value }}</el-tag>
          </template>
          <template v-else>{{ item.value || '-' }}</template>
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="pageKind === 'result' && detail" class="handle-actions">
        <el-button v-if="canHandle" type="success" @click="handleResult('CONFIRMED')">确认</el-button>
        <el-button v-if="canHandle" type="warning" @click="handleResult('FALSE_POSITIVE')">误报</el-button>
        <el-button v-if="canHandle" type="info" @click="handleResult('CLOSED')">关闭</el-button>
      </div>
    </el-drawer>

    <el-drawer v-model="executionVisible" title="任务执行记录" size="720px">
      <el-table :data="executionRecords" stripe>
        <el-table-column prop="taskNameSnapshot" label="任务名称" min-width="180" />
        <el-table-column prop="executionStatus" label="执行状态" width="120" />
        <el-table-column prop="triggerType" label="触发方式" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column prop="resultCount" label="结果数" width="90" />
      </el-table>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Delete, Edit, Plus, RefreshLeft, Search, VideoPlay, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAiTask,
  createDefectReport,
  createFaultReport,
  createKnowledge,
  createOptimizationReport,
  deleteAiTask,
  deleteDefectReport,
  deleteFaultReport,
  deleteKnowledge,
  deleteOptimizationReport,
  executeAiTask,
  getAiModels,
  getAiTasks,
  getDefectReports,
  getFaultReports,
  getKnowledge,
  getOptimizationReports,
  getTaskExecutions,
  handleDefectReport,
  handleFaultReport,
  handleOptimizationReport,
  updateAiTask,
  updateDefectReport,
  updateFaultReport,
  updateKnowledge,
  updateOptimizationReport,
  type AiModelItem,
  type AiReportItem,
  type AiTaskItem,
  type AiTaskType,
  type DefectReportItem,
  type FaultReportItem,
  type KnowledgeItem,
  type KnowledgeType,
  type OptimizationReportItem,
  type ProcessStatus,
  type TaskExecRecordItem
} from '@/api/modules/ai'
import { getBusinessSystems, getDbResources } from '@/api/modules/ledger'
import { useUserStore } from '@/stores/user'

type ModuleKey = 'fault' | 'defect' | 'optimization'
type PageKind = 'knowledge' | 'task' | 'result'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const formVisible = ref(false)
const detailVisible = ref(false)
const executionVisible = ref(false)
const drawerMode = ref<'create' | 'edit'>('create')
const editingId = ref('')
const total = ref(0)
const knowledgeRecords = ref<KnowledgeItem[]>([])
const taskRecords = ref<AiTaskItem[]>([])
const faultRecords = ref<FaultReportItem[]>([])
const defectRecords = ref<DefectReportItem[]>([])
const optimizationRecords = ref<OptimizationReportItem[]>([])
const executionRecords = ref<TaskExecRecordItem[]>([])
const modelOptions = ref<AiModelItem[]>([])
const businessOptions = ref<{ id: string; name: string }[]>([])
const dbOptions = ref<{ id: string; name: string }[]>([])
const detail = ref<Record<string, any> | null>(null)
const metricSelections = ref<string[]>([])
const optionsLoaded = ref(false)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  processStatus: '' as ProcessStatus | '',
  level: '' as '' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
})

const form = reactive<Record<string, any>>({})

const metricOptions = ['CPU', '内存', '磁盘IO', '网络带宽', '数据库连接数', 'SQL响应时长', '锁等待次数', '缓存命中率', '业务请求时延', '连接池使用率']

const moduleKey = computed<ModuleKey>(() => {
  if (route.path.startsWith('/defect-detection')) return 'defect'
  if (route.path.startsWith('/performance')) return 'optimization'
  return 'fault'
})

const pageKind = computed<PageKind>(() => {
  if (route.path.endsWith('/task')) return 'task'
  if (route.path.endsWith('/alert') || route.path.endsWith('/report') || route.path.endsWith('/suggestion')) return 'result'
  return 'knowledge'
})

const taskType = computed<AiTaskType>(() => {
  if (moduleKey.value === 'defect') return 'DEFECT_DETECTION'
  if (moduleKey.value === 'optimization') return 'PERFORMANCE_OPTIMIZATION'
  return 'FAULT_PREDICTION'
})

const knowledgeType = computed<KnowledgeType>(() => {
  if (moduleKey.value === 'defect') return 'DEFECT'
  if (moduleKey.value === 'optimization') return 'OPTIMIZATION'
  return 'FAULT'
})

const pageTitle = computed(() => {
  const map = {
    fault: { knowledge: '故障知识库管理', task: '故障预测任务配置', result: '故障智能预警' },
    defect: { knowledge: '缺陷知识库管理', task: '缺陷检测任务配置', result: '缺陷检测报表' },
    optimization: { knowledge: '优化知识库管理', task: '性能优化任务配置', result: '性能优化建议' }
  }
  return map[moduleKey.value][pageKind.value]
})
const pageDescription = computed(() => '基于 Figma 原型实现的数据壳页面，支持真实 MySQL CRUD、分页筛选和状态处理。')
const createText = computed(() => pageKind.value === 'knowledge' ? `新增${knowledgeTypeText(knowledgeType.value)}知识` : pageKind.value === 'task' ? '新增任务' : '新增结果')
const keywordPlaceholder = computed(() => pageKind.value === 'task' ? '搜索任务名称/编码' : pageKind.value === 'knowledge' ? '搜索知识名称/编码' : '搜索编号/对象/问题')
const drawerTitle = computed(() => `${drawerMode.value === 'create' ? '新增' : '编辑'}${pageTitle.value}`)
const detailTitle = computed(() => `${pageTitle.value}详情`)
const resultTypeLabel = computed(() => moduleKey.value === 'fault' ? '故障类型' : moduleKey.value === 'defect' ? '缺陷类型' : '优化类型')
const problemLabel = computed(() => moduleKey.value === 'fault' ? '预测说明' : moduleKey.value === 'defect' ? '缺陷问题' : '性能问题')
const suggestionLabel = computed(() => moduleKey.value === 'defect' ? '修复建议' : moduleKey.value === 'optimization' ? '优化建议' : '处理建议')
const reportRecords = computed<AiReportItem[]>(() => moduleKey.value === 'defect' ? defectRecords.value : optimizationRecords.value)
const targetOptions = computed(() => form.targetType === 'DB_RESOURCE' ? dbOptions.value : businessOptions.value)

const canCreate = computed(() => userStore.hasAnyPermission(createPermissions.value))
const canUpdate = computed(() => userStore.hasAnyPermission(updatePermissions.value))
const canDelete = computed(() => userStore.hasAnyPermission(deletePermissions.value))
const canHandle = computed(() => userStore.hasAnyPermission(handlePermissions.value))
const canExecute = computed(() => userStore.hasPermission('ai:task:execute'))
const showCreateButton = computed(() => canCreate.value)
const createPermissions = computed(() => pageKind.value === 'knowledge' ? ['ai:knowledge:create'] : pageKind.value === 'task' ? ['ai:task:create'] : [`ai:${moduleKey.value}:create`])
const updatePermissions = computed(() => pageKind.value === 'knowledge' ? ['ai:knowledge:update'] : pageKind.value === 'task' ? ['ai:task:update'] : [`ai:${moduleKey.value}:update`])
const deletePermissions = computed(() => pageKind.value === 'knowledge' ? ['ai:knowledge:delete'] : pageKind.value === 'task' ? ['ai:task:delete'] : [`ai:${moduleKey.value}:delete`])
const handlePermissions = computed(() => [`ai:${moduleKey.value}:handle`])

const detailItems = computed(() => {
  if (!detail.value) return []
  if (pageKind.value === 'knowledge') {
    return [
      { label: '知识编码', value: detail.value.knowledgeCode },
      { label: '知识名称', value: detail.value.knowledgeName },
      { label: '知识类型', value: knowledgeTypeText(detail.value.knowledgeType) },
      { label: '知识内容', value: detail.value.knowledgeContent },
      { label: '附件名称', value: detail.value.attachmentNames },
      { label: '更新时间', value: formatTime(detail.value.updatedAt) }
    ]
  }
  if (pageKind.value === 'task') {
    return [
      { label: '任务编码', value: detail.value.taskCode },
      { label: '任务名称', value: detail.value.taskName },
      { label: '对象', value: detail.value.targetNameSnapshot },
      { label: 'AI模型', value: detail.value.modelNameSnapshot },
      { label: '执行次数', value: detail.value.executionCount || 0 },
      { label: '指标配置', value: detail.value.metricCodes },
      { label: '定时规则', value: detail.value.scheduleRule }
    ]
  }
  const status = detail.value.processStatus
  return [
    { label: '编号', value: detail.value.reportCode || detail.value.defectCode },
    { label: '对象', value: detail.value.targetNameSnapshot },
    { label: resultTypeLabel.value, value: detail.value.faultType || detail.value.defectType || detail.value.optimizationType },
    { label: problemLabel.value, value: detail.value.forecastDescription || detail.value.defectProblem || detail.value.optimizationProblem },
    { label: suggestionLabel.value, value: detail.value.handleSuggestion || detail.value.repairSuggestion || detail.value.optimizationContent },
    { label: '处理状态', value: processText(status), tag: { type: processTag(status) } }
  ]
})

function resetForm() {
  Object.keys(form).forEach((key) => delete form[key])
  metricSelections.value = []
  form.status = 'ENABLED'
  form.processStatus = 'PENDING'
  form.targetType = 'BUSINESS_SYSTEM'
  form.level = 'MEDIUM'
  form.confidence = 80
}

function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

function knowledgeTypeText(value?: string) {
  const map: Record<string, string> = { FAULT: '故障', DEFECT: '缺陷', OPTIMIZATION: '优化' }
  return value ? map[value] || value : '-'
}

function levelText(value?: string) {
  const map: Record<string, string> = { LOW: '低', MEDIUM: '中', HIGH: '高', CRITICAL: '严重' }
  return value ? map[value] || value : '-'
}

function levelTag(value?: string) {
  if (value === 'LOW') return 'info'
  if (value === 'MEDIUM') return 'warning'
  return 'danger'
}

function processText(value?: string) {
  const map: Record<string, string> = { PENDING: '未处理', CONFIRMED: '已确认', FALSE_POSITIVE: '误报', CLOSED: '已关闭' }
  return value ? map[value] || value : '-'
}

function processTag(value?: string) {
  if (value === 'CONFIRMED') return 'success'
  if (value === 'FALSE_POSITIVE') return 'warning'
  if (value === 'CLOSED') return 'info'
  return 'danger'
}

async function loadOptions(force = false) {
  if (optionsLoaded.value && !force) return
  const [models, systems, dbs] = await Promise.all([
    getAiModels({ pageNo: 1, pageSize: 200 }).catch(() => ({ records: [] })),
    getBusinessSystems({ pageNo: 1, pageSize: 200 }).catch(() => ({ records: [] })),
    getDbResources({ pageNo: 1, pageSize: 200 }).catch(() => ({ records: [] }))
  ])
  modelOptions.value = models.records
  businessOptions.value = systems.records.map((item) => ({ id: item.id, name: item.name }))
  dbOptions.value = dbs.records.map((item) => ({ id: item.id, name: item.name }))
  optionsLoaded.value = true
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      knowledgeType: pageKind.value === 'knowledge' ? knowledgeType.value : undefined,
      taskType: pageKind.value === 'task' ? taskType.value : undefined,
      processStatus: query.processStatus || undefined,
      level: query.level || undefined
    }
    if (pageKind.value === 'knowledge') {
      const page = await getKnowledge(params)
      knowledgeRecords.value = page.records
      total.value = page.total
    } else if (pageKind.value === 'task') {
      const page = await getAiTasks(params)
      taskRecords.value = page.records
      total.value = page.total
    } else if (moduleKey.value === 'fault') {
      const page = await getFaultReports(params)
      faultRecords.value = page.records
      total.value = page.total
    } else if (moduleKey.value === 'defect') {
      const page = await getDefectReports(params)
      defectRecords.value = page.records
      total.value = page.total
    } else {
      const page = await getOptimizationReports(params)
      optimizationRecords.value = page.records
      total.value = page.total
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNo = 1
  loadData()
}

function resetSearch() {
  query.keyword = ''
  query.processStatus = ''
  query.level = ''
  query.pageNo = 1
  loadData()
}

function openCreateDrawer() {
  drawerMode.value = 'create'
  editingId.value = ''
  resetForm()
  if (pageKind.value === 'knowledge') {
    form.knowledgeType = knowledgeType.value
    form.knowledgeCode = `${knowledgeType.value}-KB-${Date.now().toString().slice(-5)}`
  } else if (pageKind.value === 'task') {
    form.taskType = taskType.value
    form.taskCode = `${taskType.value}-TASK-${Date.now().toString().slice(-5)}`
  } else {
    form.code = `${moduleKey.value.toUpperCase()}-${Date.now().toString().slice(-5)}`
  }
  formVisible.value = true
}

function openKnowledgeDetail(row: KnowledgeItem) {
  detail.value = row
  detailVisible.value = true
}

function openKnowledgeEdit(row: KnowledgeItem) {
  drawerMode.value = 'edit'
  editingId.value = row.id
  resetForm()
  Object.assign(form, row)
  formVisible.value = true
}

function openTaskDetail(row: AiTaskItem) {
  detail.value = row
  detailVisible.value = true
}

function openTaskEdit(row: AiTaskItem) {
  drawerMode.value = 'edit'
  editingId.value = row.id
  resetForm()
  Object.assign(form, row)
  metricSelections.value = row.metricCodes ? row.metricCodes.split(',') : []
  formVisible.value = true
}

function openReportDetail(row: AiReportItem) {
  detail.value = row as Record<string, any>
  detailVisible.value = true
}

function openReportEdit(row: AiReportItem) {
  drawerMode.value = 'edit'
  editingId.value = row.id
  resetForm()
  Object.assign(form, row)
  form.code = (row as any).reportCode || (row as any).defectCode
  form.resultType = (row as any).faultType || (row as any).defectType || (row as any).optimizationType
  form.problem = (row as any).forecastDescription || (row as any).defectProblem || (row as any).optimizationProblem
  form.suggestion = (row as any).handleSuggestion || (row as any).repairSuggestion || (row as any).optimizationContent
  form.level = (row as any).riskLevel || (row as any).severityLevel || 'MEDIUM'
  formVisible.value = true
}

async function submitForm() {
  submitLoading.value = true
  try {
    if (pageKind.value === 'knowledge') {
      const payload = { ...form, knowledgeType: knowledgeType.value }
      drawerMode.value === 'create' ? await createKnowledge(payload) : await updateKnowledge(editingId.value, payload)
    } else if (pageKind.value === 'task') {
      const payload = { ...form, taskType: taskType.value, metricCodes: metricSelections.value.join(',') }
      drawerMode.value === 'create' ? await createAiTask(payload) : await updateAiTask(editingId.value, payload)
    } else {
      await submitReport()
    }
    ElMessage.success('保存成功')
    formVisible.value = false
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

async function submitReport() {
  if (moduleKey.value === 'fault') {
    const payload = {
      reportCode: form.code,
      targetType: form.targetType,
      targetId: form.targetId,
      faultType: form.resultType,
      riskLevel: form.level,
      confidence: form.confidence,
      forecastDescription: form.problem,
      handleSuggestion: form.suggestion,
      processStatus: form.processStatus
    }
    drawerMode.value === 'create' ? await createFaultReport(payload) : await updateFaultReport(editingId.value, payload)
  } else if (moduleKey.value === 'defect') {
    const payload = {
      defectCode: form.code,
      targetType: form.targetType,
      targetId: form.targetId,
      defectType: form.resultType,
      severityLevel: form.level,
      defectProblem: form.problem,
      repairSuggestion: form.suggestion,
      processStatus: form.processStatus
    }
    drawerMode.value === 'create' ? await createDefectReport(payload) : await updateDefectReport(editingId.value, payload)
  } else {
    const payload = {
      reportCode: form.code,
      targetType: form.targetType,
      targetId: form.targetId,
      optimizationType: form.resultType,
      optimizationProblem: form.problem,
      sqlText: form.sqlText,
      optimizationContent: form.suggestion,
      confidence: form.confidence,
      processStatus: form.processStatus
    }
    drawerMode.value === 'create' ? await createOptimizationReport(payload) : await updateOptimizationReport(editingId.value, payload)
  }
}

async function removeKnowledge(row: KnowledgeItem) {
  await ElMessageBox.confirm(`确定删除「${row.knowledgeName}」吗？`, '确认删除', { type: 'warning' })
  await deleteKnowledge(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

async function removeTask(row: AiTaskItem) {
  await ElMessageBox.confirm(`确定删除「${row.taskName}」吗？`, '确认删除', { type: 'warning' })
  await deleteAiTask(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

async function removeReport(row: AiReportItem) {
  await ElMessageBox.confirm('确定删除该结果吗？', '确认删除', { type: 'warning' })
  if (moduleKey.value === 'fault') await deleteFaultReport(row.id)
  else if (moduleKey.value === 'defect') await deleteDefectReport(row.id)
  else await deleteOptimizationReport(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

async function runTask(row: AiTaskItem) {
  await ElMessageBox.confirm(`确定立即执行「${row.taskName}」吗？当前阶段会生成一条执行记录。`, '执行任务', { type: 'info' })
  await executeAiTask(row.id)
  ElMessage.success('执行成功')
  await loadData()
}

async function openExecutionDrawer(row: AiTaskItem) {
  const page = await getTaskExecutions({ pageNo: 1, pageSize: 100, taskType: row.taskType })
  executionRecords.value = page.records.filter((item) => item.taskId === row.id)
  executionVisible.value = true
}

async function handleResult(status: ProcessStatus) {
  if (!detail.value) return
  if (moduleKey.value === 'fault') await handleFaultReport(detail.value.id, status)
  else if (moduleKey.value === 'defect') await handleDefectReport(detail.value.id, status)
  else await handleOptimizationReport(detail.value.id, status)
  ElMessage.success('处理成功')
  detailVisible.value = false
  await loadData()
}

watch(() => route.path, async () => {
  query.pageNo = 1
  query.keyword = ''
  query.processStatus = ''
  query.level = ''
  await Promise.all([loadData(), loadOptions()])
})

onMounted(async () => {
  await Promise.all([loadOptions(), loadData()])
})
</script>

<style scoped>
.ai-page :deep(.el-card) {
  border-radius: 8px;
}

.ai-filter :deep(.el-form-item) {
  margin-bottom: 0;
}

.ai-table-card {
  min-height: 520px;
}

.result-cards {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.result-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.06);
}

.result-card-main {
  min-width: 0;
  flex: 1;
}

.result-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.result-title h3 {
  margin: 0;
  color: #1e40af;
  font-size: 18px;
}

.code-chip {
  padding: 3px 8px;
  border-radius: 6px;
  background: #f0f7ff;
  color: #1e40af;
  font-size: 12px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.result-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
  color: #64748b;
  font-size: 13px;
}

.inline-fields {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 12px;
  width: 100%;
}

.drawer-footer,
.handle-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.handle-actions {
  margin-top: 18px;
}
</style>
