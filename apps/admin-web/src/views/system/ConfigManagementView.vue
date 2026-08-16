<template>
  <section class="page config-page">
    <div v-if="!embedded" class="page-header">
      <div>
        <h1>系统配置</h1>
        <p>维护系统级参数配置，为平台名称、刷新间隔、安全策略和后续业务参数提供统一管理入口。</p>
      </div>
      <el-button v-if="userStore.hasPermission('system:config:create')" type="primary" :icon="Plus" @click="openDrawer('create')">新增配置</el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query" class="config-filter">
        <el-form-item label="配置键">
          <el-input v-model="query.configKey" clearable placeholder="请输入配置键" />
        </el-form-item>
        <el-form-item label="配置名称">
          <el-input v-model="query.configName" clearable placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.configType" clearable placeholder="全部类型" style="width: 140px">
            <el-option label="文本" value="TEXT" />
            <el-option label="数字" value="NUMBER" />
            <el-option label="布尔" value="BOOLEAN" />
            <el-option label="JSON" value="JSON" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="config-table-card">
      <el-table :data="records" stripe v-loading="loading" row-key="id">
        <el-table-column prop="configKey" label="配置键" min-width="190" />
        <el-table-column prop="configName" label="配置名称" min-width="160" />
        <el-table-column prop="configType" label="类型" width="100">
          <template #default="{ row }">{{ configTypeText(row.configType) }}</template>
        </el-table-column>
        <el-table-column prop="configValue" label="配置值" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">{{ row.configValue || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" round>{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="userStore.hasPermission('system:config:update')" type="primary" link :icon="Edit" @click="openDrawer('edit', row)">编辑</el-button>
            <el-button v-if="userStore.hasPermission('system:config:status')" type="warning" link :icon="SwitchButton" @click="toggleStatus(row)">
              {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
            <el-button v-if="userStore.hasPermission('system:config:delete')" type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="form.configKey" maxlength="100" placeholder="例如：SYSTEM_NAME" />
        </el-form-item>
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="form.configName" maxlength="100" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置类型" prop="configType">
          <el-select v-model="form.configType" style="width: 100%">
            <el-option label="文本" value="TEXT" />
            <el-option label="数字" value="NUMBER" />
            <el-option label="布尔" value="BOOLEAN" />
            <el-option label="JSON" value="JSON" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置值">
          <el-switch
            v-if="form.configType === 'BOOLEAN'"
            v-model="booleanValue"
            active-text="true"
            inactive-text="false"
          />
          <el-input-number
            v-else-if="form.configType === 'NUMBER'"
            v-model="numberValue"
            style="width: 100%"
          />
          <el-input
            v-else-if="form.configType === 'JSON'"
            v-model="form.configValue"
            type="textarea"
            :rows="8"
            placeholder='例如：{"enabled":true}'
          />
          <el-input v-else v-model="form.configValue" maxlength="1000" placeholder="请输入配置值" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">提交保存</el-button>
        </div>
      </template>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Delete, Edit, Plus, RefreshLeft, Search, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createConfig,
  deleteConfig,
  getConfigs,
  updateConfig,
  updateConfigStatus,
  type ConfigItem,
  type ConfigPayload,
  type ConfigType,
  type UserStatus
} from '@/api/modules/system'
import { useUserStore } from '@/stores/user'

withDefaults(defineProps<{ embedded?: boolean }>(), {
  embedded: false
})

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const records = ref<ConfigItem[]>([])
const total = ref(0)
const drawerVisible = ref(false)
const drawerMode = ref<'create' | 'edit'>('create')
const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  configKey: '',
  configName: '',
  configType: '' as ConfigType | '',
  status: '' as UserStatus | ''
})

const form = reactive<ConfigPayload>({
  configKey: '',
  configName: '',
  configValue: '',
  configType: 'TEXT',
  status: 'ENABLED',
  remark: ''
})

const drawerTitle = computed(() => drawerMode.value === 'create' ? '新增系统配置' : '编辑系统配置')
const booleanValue = computed({
  get: () => form.configValue === 'true',
  set: (value: boolean) => {
    form.configValue = value ? 'true' : 'false'
  }
})
const numberValue = computed({
  get: () => Number(form.configValue || 0),
  set: (value: number | undefined) => {
    form.configValue = String(value ?? 0)
  }
})

const formRules: FormRules = {
  configKey: [
    { required: true, message: '请输入配置键', trigger: 'blur' },
    { pattern: /^[A-Z][A-Z0-9_]*$/, message: '配置键需使用大写字母、数字和下划线，并以字母开头', trigger: 'blur' }
  ],
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  configType: [{ required: true, message: '请选择配置类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

watch(() => form.configType, (type) => {
  if (type === 'BOOLEAN' && !['true', 'false'].includes(form.configValue || '')) {
    form.configValue = 'false'
  }
  if (type === 'NUMBER' && Number.isNaN(Number(form.configValue || 0))) {
    form.configValue = '0'
  }
})

async function loadData() {
  loading.value = true
  try {
    const page = await getConfigs({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      configKey: query.configKey || undefined,
      configName: query.configName || undefined,
      configType: query.configType || undefined,
      status: query.status || undefined
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
  query.configKey = ''
  query.configName = ''
  query.configType = ''
  query.status = ''
  query.pageNo = 1
  loadData()
}

function resetForm() {
  editingId.value = null
  form.configKey = ''
  form.configName = ''
  form.configValue = ''
  form.configType = 'TEXT'
  form.status = 'ENABLED'
  form.remark = ''
}

function openDrawer(mode: 'create' | 'edit', row?: ConfigItem) {
  drawerMode.value = mode
  resetForm()
  if (row) {
    editingId.value = row.id
    form.configKey = row.configKey
    form.configName = row.configName
    form.configValue = row.configValue || ''
    form.configType = row.configType
    form.status = row.status
    form.remark = row.remark || ''
  }
  drawerVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (drawerMode.value === 'create') {
      await createConfig(form)
    } else if (editingId.value) {
      await updateConfig(editingId.value, form)
    }
    ElMessage.success('保存成功')
    drawerVisible.value = false
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

async function toggleStatus(row: ConfigItem) {
  const nextStatus: UserStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  await updateConfigStatus(row.id, nextStatus)
  ElMessage.success(`${statusText(nextStatus)}成功`)
  await loadData()
}

async function handleDelete(row: ConfigItem) {
  await ElMessageBox.confirm(`确定要删除配置「${row.configName}」吗？`, '确认删除', { type: 'warning' })
  await deleteConfig(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

function statusText(status: UserStatus) {
  return status === 'ENABLED' ? '启用' : '禁用'
}

function configTypeText(type: ConfigType) {
  const map: Record<ConfigType, string> = { TEXT: '文本', NUMBER: '数字', BOOLEAN: '布尔', JSON: 'JSON' }
  return map[type] || type
}

function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

onMounted(loadData)
</script>

<style scoped>
.config-page :deep(.el-card) {
  border-radius: 8px;
}

.config-filter :deep(.el-form-item) {
  margin-bottom: 0;
}

.config-table-card {
  min-height: 560px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
