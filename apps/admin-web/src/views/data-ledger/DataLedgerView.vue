<template>
  <section class="page ledger-page">
    <div class="page-header">
      <div>
        <h1>数据可视化台账</h1>
        <p>管理业务系统与分布式数据库资源，沉淀智能诊断、日志采集和大屏监控的数据基础。</p>
      </div>
      <el-button v-if="canCreateAnyLedger" type="primary" :icon="Plus" @click="openCreateDrawer">新增</el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query" class="ledger-filter">
        <el-form-item label="名称搜索">
          <el-input v-model="query.name" clearable placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="query.ownerName" clearable placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="警告" value="WARNING" />
            <el-option label="危险" value="DANGER" />
            <el-option label="离线" value="OFFLINE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="ledger-table-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="业务系统列表" name="business" />
        <el-tab-pane label="分布式数据库列表" name="database" />
      </el-tabs>

      <el-table :data="records" stripe v-loading="loading" row-key="id">
        <el-table-column prop="name" label="名称" min-width="240" />
        <el-table-column prop="ownerName" label="负责人" width="140" />
        <el-table-column v-if="activeTab === 'business'" prop="databaseName" label="关联分布式数据库" min-width="220">
          <template #default="{ row }">
            <div v-if="businessDatabaseNames(row).length" class="database-tags">
              <el-tag v-for="name in businessDatabaseNames(row)" :key="name" type="info" round>{{ name }}</el-tag>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column v-if="activeTab === 'database'" prop="publicIp" label="公网IP" width="160" />
        <el-table-column v-if="activeTab === 'database'" prop="privateIp" label="内网IP" width="160">
          <template #default="{ row }">
            <span>{{ row.privateIp || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="当前数据状况" width="150">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" round>{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">
            <span>{{ formatTime(row.updatedAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="openDetail(row)">查看详情</el-button>
            <el-button v-if="canUpdateActiveLedger" type="primary" link :icon="Edit" @click="openEditDrawer(row)">编辑</el-button>
            <el-button v-if="canDeleteActiveLedger" type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
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

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="520px" destroy-on-close class="ledger-form-drawer">
      <el-tabs
        v-if="drawerMode === 'create'"
        v-model="formType"
        class="resource-type-tabs"
        stretch
        @tab-change="handleFormTypeChange"
      >
        <el-tab-pane label="业务系统" name="business" :disabled="!canCreateBusiness" />
        <el-tab-pane label="分布式数据库" name="database" :disabled="!canCreateDatabase" />
      </el-tabs>

      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top">
        <template v-if="formType === 'business'">
          <el-form-item label="业务系统名称" prop="name">
            <el-input v-model="form.name" maxlength="100" placeholder="请输入业务系统名称" />
          </el-form-item>
          <el-form-item label="关联分布式数据库" prop="databaseIds">
            <el-select
              v-model="form.databaseIds"
              multiple
              filterable
              collapse-tags
              collapse-tags-tooltip
              placeholder="请选择一个或多个分布式数据库"
              style="width: 100%"
            >
              <el-option v-for="item in dbOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="创建人" prop="ownerName">
            <el-input v-model="form.ownerName" maxlength="64" placeholder="请输入创建人" />
          </el-form-item>
          <el-form-item label="访问地址" prop="accessUrl">
            <el-input v-model="form.accessUrl" maxlength="255" placeholder="请输入访问地址" />
          </el-form-item>
          <el-form-item label="备注说明" prop="remark">
            <el-input v-model="form.remark" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请输入备注说明" />
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="分布式数据库名称" prop="name">
            <el-input v-model="form.name" maxlength="100" placeholder="请输入分布式数据库名称" />
          </el-form-item>
          <el-form-item label="公网IP" prop="publicIp">
            <el-input v-model="form.publicIp" placeholder="例如：58.48.100.10" />
          </el-form-item>
          <el-form-item label="内网IP" prop="privateIp">
            <el-input v-model="form.privateIp" placeholder="例如：192.168.1.10" />
          </el-form-item>
          <el-form-item label="创建人" prop="ownerName">
            <el-input v-model="form.ownerName" maxlength="64" placeholder="请输入创建人" />
          </el-form-item>
        </template>
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
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, RefreshLeft, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  createBusinessSystem,
  createDbResource,
  deleteBusinessSystem,
  deleteDbResource,
  getBusinessSystemDetail,
  getBusinessSystems,
  getDbResourceDetail,
  getDbResources,
  updateBusinessSystem,
  updateDbResource,
  type BusinessSystemItem,
  type BusinessSystemPayload,
  type DbResourceItem,
  type DbResourcePayload,
  type LedgerStatus
} from '@/api/modules/ledger'
import { useUserStore } from '@/stores/user'

type ActiveTab = 'business' | 'database'
type LedgerRecord = BusinessSystemItem | DbResourceItem

const activeTab = ref<ActiveTab>('business')
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const canCreateBusiness = computed(() => userStore.hasPermission('ledger:business:create'))
const canCreateDatabase = computed(() => userStore.hasPermission('ledger:database:create'))
const canCreateAnyLedger = computed(() => canCreateBusiness.value || canCreateDatabase.value)
const canUpdateActiveLedger = computed(() => activeTab.value === 'business'
  ? userStore.hasPermission('ledger:business:update')
  : userStore.hasPermission('ledger:database:update'))
const canDeleteActiveLedger = computed(() => activeTab.value === 'business'
  ? userStore.hasPermission('ledger:business:delete')
  : userStore.hasPermission('ledger:database:delete'))
const loading = ref(false)
const submitLoading = ref(false)
const records = ref<LedgerRecord[]>([])
const total = ref(0)
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  ownerName: '',
  status: '' as LedgerStatus | ''
})

const drawerVisible = ref(false)
const drawerMode = ref<'create' | 'edit'>('create')
const formType = ref<ActiveTab>('business')
const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()
const dbOptions = ref<DbResourceItem[]>([])
const form = reactive({
  name: '',
  databaseIds: [] as string[],
  ownerName: '',
  status: 'NORMAL' as LedgerStatus,
  accessUrl: '',
  remark: '',
  publicIp: '',
  privateIp: ''
})

const ipv4Pattern = /^((25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)\.){3}(25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)$/

const drawerTitle = computed(() => {
  if (drawerMode.value === 'create') return '新增'
  const action = '编辑'
  return `${action}${formType.value === 'business' ? '业务系统' : '分布式数据库'}`
})

const formRules = computed<FormRules>(() => {
  const common: FormRules = {
    name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
    ownerName: [{ required: true, message: '请输入创建人', trigger: 'blur' }]
  }
  if (formType.value === 'business') {
    return {
      ...common,
      databaseIds: [{ required: true, type: 'array', min: 1, message: '请选择至少一个关联分布式数据库', trigger: 'change' }]
    }
  }
  return {
    ...common,
    publicIp: [
      { required: true, message: '请输入公网IP', trigger: 'blur' },
      { pattern: ipv4Pattern, message: '公网IP格式不正确', trigger: 'blur' }
    ],
    privateIp: [{ pattern: new RegExp(`^$|${ipv4Pattern.source}`), message: '内网IP格式不正确', trigger: 'blur' }]
  }
})

function statusType(status?: string) {
  if (status === 'NORMAL') return 'success'
  if (status === 'WARNING') return 'warning'
  if (status === 'OFFLINE') return 'info'
  return 'danger'
}

function statusText(status?: string) {
  const map: Record<string, string> = { NORMAL: '正常', WARNING: '警告', DANGER: '危险', OFFLINE: '离线' }
  return status ? map[status] || status : '-'
}

function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

function businessDatabaseNames(row?: Partial<BusinessSystemItem & DbResourceItem> | null) {
  if (!row) return []
  if ('databaseNames' in row && row.databaseNames?.length) return row.databaseNames
  if ('databaseName' in row && row.databaseName) return [row.databaseName]
  return []
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      name: query.name || undefined,
      ownerName: query.ownerName || undefined,
      status: query.status || undefined
    }
    const page = activeTab.value === 'business'
      ? await getBusinessSystems(params)
      : await getDbResources(params)
    records.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

async function loadDbOptions() {
  const page = await getDbResources({ pageNo: 1, pageSize: 200 })
  dbOptions.value = page.records
}

function handleSearch() {
  query.pageNo = 1
  loadData()
}

function reset() {
  query.name = ''
  query.ownerName = ''
  query.status = ''
  query.pageNo = 1
  loadData()
}

function handleTabChange() {
  query.pageNo = 1
  loadData()
}

function resetForm() {
  form.name = ''
  form.databaseIds = []
  form.ownerName = ''
  form.status = 'NORMAL'
  form.accessUrl = ''
  form.remark = ''
  form.publicIp = ''
  form.privateIp = ''
  editingId.value = null
}

async function openCreateDrawer() {
  drawerMode.value = 'create'
  formType.value = activeTab.value === 'business' && canCreateBusiness.value
    ? 'business'
    : 'database'
  resetForm()
  if (formType.value === 'business') await loadDbOptions()
  drawerVisible.value = true
}

async function handleFormTypeChange() {
  if (drawerMode.value !== 'create') return
  resetForm()
  if (formType.value === 'business') await loadDbOptions()
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEditDrawer(row: LedgerRecord) {
  drawerMode.value = 'edit'
  formType.value = activeTab.value
  editingId.value = row.id
  if (formType.value === 'business') {
    await loadDbOptions()
    const item = await getBusinessSystemDetail(row.id)
    form.name = item.name
    form.databaseIds = item.databaseIds?.length ? item.databaseIds : (item.databaseId ? [item.databaseId] : [])
    form.ownerName = item.ownerName
    form.status = item.status
    form.accessUrl = item.accessUrl || ''
    form.remark = item.remark || ''
  } else {
    const item = await getDbResourceDetail(row.id)
    form.name = item.name
    form.publicIp = item.publicIp
    form.privateIp = item.privateIp || ''
    form.ownerName = item.ownerName
    form.status = item.status
  }
  drawerVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formType.value === 'business') {
      const payload: BusinessSystemPayload = {
        name: form.name,
        databaseId: form.databaseIds[0] || '',
        databaseIds: form.databaseIds,
        ownerName: form.ownerName,
        status: form.status,
        accessUrl: form.accessUrl,
        remark: form.remark
      }
      if (drawerMode.value === 'create') {
        await createBusinessSystem(payload)
      } else if (editingId.value) {
        await updateBusinessSystem(editingId.value, payload)
      }
    } else {
      const payload: DbResourcePayload = {
        name: form.name,
        publicIp: form.publicIp,
        privateIp: form.privateIp,
        ownerName: form.ownerName,
        status: form.status
      }
      if (drawerMode.value === 'create') {
        await createDbResource(payload)
      } else if (editingId.value) {
        await updateDbResource(editingId.value, payload)
      }
    }
    ElMessage.success('保存成功')
    drawerVisible.value = false
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: LedgerRecord) {
  await ElMessageBox.confirm(`确定要删除「${row.name}」吗？此操作不可撤销。`, '确认删除', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  })
  if (activeTab.value === 'business') {
    await deleteBusinessSystem(row.id)
  } else {
    await deleteDbResource(row.id)
  }
  ElMessage.success('删除成功')
  await loadData()
}

function openDetail(row: LedgerRecord) {
  router.push({
    name: activeTab.value === 'business' ? 'BusinessSystemDetail' : 'DbResourceDetail',
    params: { id: row.id },
    query: { tab: activeTab.value }
  })
}

onMounted(() => {
  if (route.query.tab === 'database') activeTab.value = 'database'
  loadData()
})
</script>

<style scoped>
.ledger-page :deep(.el-card) {
  border-radius: 8px;
}

.ledger-filter :deep(.el-form-item) {
  margin-bottom: 0;
}

.ledger-table-card {
  min-height: 520px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.ledger-form-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 20px 24px 12px;
  color: var(--el-text-color-primary);
  font-size: 18px;
  font-weight: 600;
}

.ledger-form-drawer :deep(.el-drawer__body) {
  padding: 0 24px 24px;
}

.resource-type-tabs {
  margin-bottom: 24px;
}

.resource-type-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.resource-type-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.resource-type-tabs :deep(.el-tabs__item) {
  height: 44px;
  font-size: 15px;
}

.ledger-form-drawer :deep(.el-form-item) {
  margin-bottom: 22px;
}

.ledger-form-drawer :deep(.el-form-item__label) {
  padding-bottom: 8px;
  font-size: 14px;
  line-height: 20px;
}

.ledger-form-drawer :deep(.el-drawer__footer) {
  border-top: 1px solid var(--el-border-color-lighter);
  padding: 16px 24px;
}
</style>
