<template>
  <section class="page dict-page">
    <div v-if="!embedded" class="page-header">
      <div>
        <h1>字典管理</h1>
        <p>维护系统基础枚举、状态标签和下拉选项，为后续业务配置和页面展示提供统一来源。</p>
      </div>
      <el-button v-if="userStore.hasPermission('system:dict:create')" type="primary" :icon="Plus" @click="openTypeDrawer('create')">新增字典类型</el-button>
    </div>

    <div class="dict-layout">
      <el-card shadow="never" class="dict-type-card">
        <el-form :inline="true" :model="typeQuery" class="dict-filter">
          <el-form-item label="编码">
            <el-input v-model="typeQuery.dictCode" clearable placeholder="字典编码" />
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model="typeQuery.dictName" clearable placeholder="字典名称" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="searchTypes">查询</el-button>
            <el-button :icon="RefreshLeft" @click="resetTypes">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="typeRecords" stripe v-loading="typeLoading" row-key="id" highlight-current-row @current-change="selectType">
          <el-table-column prop="dictCode" label="字典编码" min-width="160" />
          <el-table-column prop="dictName" label="字典名称" min-width="140" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" round>{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="230" fixed="right">
            <template #default="{ row }">
              <el-button v-if="userStore.hasPermission('system:dict:update')" type="primary" link :icon="Edit" @click.stop="openTypeDrawer('edit', row)">编辑</el-button>
              <el-button v-if="userStore.hasPermission('system:dict:status')" type="warning" link :icon="SwitchButton" @click.stop="toggleTypeStatus(row)">
                {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
              </el-button>
              <el-button v-if="userStore.hasPermission('system:dict:delete')" type="danger" link :icon="Delete" @click.stop="handleDeleteType(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="table-footer">
          <el-pagination
            v-model:current-page="typeQuery.pageNo"
            v-model:page-size="typeQuery.pageSize"
            layout="total, prev, pager, next"
            :total="typeTotal"
            @change="loadTypes"
          />
        </div>
      </el-card>

      <el-card shadow="never" class="dict-item-card">
        <div class="item-header">
          <div>
            <h2>{{ selectedType ? selectedType.dictName : '字典项' }}</h2>
            <span>{{ selectedType ? selectedType.dictCode : '请先选择左侧字典类型' }}</span>
          </div>
          <el-button
            v-if="selectedType && userStore.hasPermission('system:dict:create')"
            type="primary"
            :icon="Plus"
            @click="openItemDrawer('create')"
          >
            新增字典项
          </el-button>
        </div>

        <el-form :inline="true" :model="itemQuery" class="dict-filter">
          <el-form-item label="值">
            <el-input v-model="itemQuery.itemValue" clearable placeholder="字典项值" />
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model="itemQuery.itemLabel" clearable placeholder="显示名称" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" :disabled="!selectedType" @click="searchItems">查询</el-button>
            <el-button :icon="RefreshLeft" :disabled="!selectedType" @click="resetItems">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="itemRecords" stripe v-loading="itemLoading" row-key="id">
          <el-table-column prop="itemValue" label="字典值" min-width="140" />
          <el-table-column prop="itemLabel" label="显示名称" min-width="140" />
          <el-table-column prop="tagType" label="标签" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.tagType" :type="tagType(row.tagType)" round>{{ row.tagType }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" round>{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="230" fixed="right">
            <template #default="{ row }">
              <el-button v-if="userStore.hasPermission('system:dict:update')" type="primary" link :icon="Edit" @click="openItemDrawer('edit', row)">编辑</el-button>
              <el-button v-if="userStore.hasPermission('system:dict:status')" type="warning" link :icon="SwitchButton" @click="toggleItemStatus(row)">
                {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
              </el-button>
              <el-button v-if="userStore.hasPermission('system:dict:delete')" type="danger" link :icon="Delete" @click="handleDeleteItem(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="table-footer">
          <el-pagination
            v-model:current-page="itemQuery.pageNo"
            v-model:page-size="itemQuery.pageSize"
            layout="total, prev, pager, next"
            :total="itemTotal"
            @change="loadItems"
          />
        </div>
      </el-card>
    </div>

    <el-drawer v-model="typeDrawerVisible" :title="typeDrawerTitle" size="520px" destroy-on-close>
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-position="top">
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="typeForm.dictCode" maxlength="100" placeholder="例如：HEALTH_STATUS" />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" maxlength="100" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="typeForm.status" style="width: 100%">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="typeDrawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitType">提交保存</el-button>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="itemDrawerVisible" :title="itemDrawerTitle" size="520px" destroy-on-close>
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-position="top">
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="itemForm.dictCode" disabled />
        </el-form-item>
        <el-form-item label="字典值" prop="itemValue">
          <el-input v-model="itemForm.itemValue" maxlength="100" placeholder="例如：NORMAL" />
        </el-form-item>
        <el-form-item label="显示名称" prop="itemLabel">
          <el-input v-model="itemForm.itemLabel" maxlength="100" placeholder="例如：正常" />
        </el-form-item>
        <el-form-item label="标签类型">
          <el-select v-model="itemForm.tagType" clearable placeholder="请选择标签类型" style="width: 100%">
            <el-option label="primary" value="primary" />
            <el-option label="success" value="success" />
            <el-option label="warning" value="warning" />
            <el-option label="danger" value="danger" />
            <el-option label="info" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="itemForm.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="itemForm.status" style="width: 100%">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="itemForm.remark" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="itemDrawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitItem">提交保存</el-button>
        </div>
      </template>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, RefreshLeft, Search, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createDictItem,
  createDictType,
  deleteDictItem,
  deleteDictType,
  getDictItems,
  getDictTypes,
  updateDictItem,
  updateDictItemStatus,
  updateDictType,
  updateDictTypeStatus,
  type DictItem,
  type DictItemPayload,
  type DictTypeItem,
  type DictTypePayload,
  type UserStatus
} from '@/api/modules/system'
import { useUserStore } from '@/stores/user'

withDefaults(defineProps<{ embedded?: boolean }>(), {
  embedded: false
})

const userStore = useUserStore()
const typeLoading = ref(false)
const itemLoading = ref(false)
const submitLoading = ref(false)
const typeRecords = ref<DictTypeItem[]>([])
const itemRecords = ref<DictItem[]>([])
const selectedType = ref<DictTypeItem | null>(null)
const typeTotal = ref(0)
const itemTotal = ref(0)
const typeDrawerVisible = ref(false)
const itemDrawerVisible = ref(false)
const typeMode = ref<'create' | 'edit'>('create')
const itemMode = ref<'create' | 'edit'>('create')
const editingTypeId = ref<string | null>(null)
const editingItemId = ref<string | null>(null)
const typeFormRef = ref<FormInstance>()
const itemFormRef = ref<FormInstance>()

const typeQuery = reactive({ pageNo: 1, pageSize: 10, dictCode: '', dictName: '', status: '' as UserStatus | '' })
const itemQuery = reactive({ pageNo: 1, pageSize: 10, itemValue: '', itemLabel: '', status: '' as UserStatus | '' })
const typeForm = reactive<DictTypePayload>({ dictCode: '', dictName: '', status: 'ENABLED', remark: '' })
const itemForm = reactive<DictItemPayload>({ dictCode: '', itemValue: '', itemLabel: '', tagType: '', sortOrder: 0, status: 'ENABLED', remark: '' })

const typeDrawerTitle = computed(() => typeMode.value === 'create' ? '新增字典类型' : '编辑字典类型')
const itemDrawerTitle = computed(() => itemMode.value === 'create' ? '新增字典项' : '编辑字典项')

const typeRules: FormRules = {
  dictCode: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    { pattern: /^[A-Z][A-Z0-9_]*$/, message: '字典编码需使用大写字母、数字和下划线，并以字母开头', trigger: 'blur' }
  ],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const itemRules: FormRules = {
  dictCode: [{ required: true, message: '请选择字典类型', trigger: 'change' }],
  itemValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
  itemLabel: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

async function loadTypes() {
  typeLoading.value = true
  try {
    const page = await getDictTypes({
      pageNo: typeQuery.pageNo,
      pageSize: typeQuery.pageSize,
      dictCode: typeQuery.dictCode || undefined,
      dictName: typeQuery.dictName || undefined,
      status: typeQuery.status || undefined
    })
    typeRecords.value = page.records
    typeTotal.value = page.total
    if (!selectedType.value && page.records.length > 0) {
      await selectType(page.records[0])
    }
  } finally {
    typeLoading.value = false
  }
}

async function loadItems() {
  if (!selectedType.value) {
    itemRecords.value = []
    itemTotal.value = 0
    return
  }
  itemLoading.value = true
  try {
    const page = await getDictItems({
      pageNo: itemQuery.pageNo,
      pageSize: itemQuery.pageSize,
      dictCode: selectedType.value.dictCode,
      itemValue: itemQuery.itemValue || undefined,
      itemLabel: itemQuery.itemLabel || undefined,
      status: itemQuery.status || undefined
    })
    itemRecords.value = page.records
    itemTotal.value = page.total
  } finally {
    itemLoading.value = false
  }
}

async function selectType(row?: DictTypeItem) {
  if (!row) return
  selectedType.value = row
  itemQuery.pageNo = 1
  await loadItems()
}

function searchTypes() {
  typeQuery.pageNo = 1
  selectedType.value = null
  loadTypes()
}

function resetTypes() {
  typeQuery.dictCode = ''
  typeQuery.dictName = ''
  typeQuery.status = ''
  typeQuery.pageNo = 1
  selectedType.value = null
  loadTypes()
}

function searchItems() {
  itemQuery.pageNo = 1
  loadItems()
}

function resetItems() {
  itemQuery.itemValue = ''
  itemQuery.itemLabel = ''
  itemQuery.status = ''
  itemQuery.pageNo = 1
  loadItems()
}

function openTypeDrawer(mode: 'create' | 'edit', row?: DictTypeItem) {
  typeMode.value = mode
  editingTypeId.value = row?.id || null
  typeForm.dictCode = row?.dictCode || ''
  typeForm.dictName = row?.dictName || ''
  typeForm.status = row?.status || 'ENABLED'
  typeForm.remark = row?.remark || ''
  typeDrawerVisible.value = true
}

function openItemDrawer(mode: 'create' | 'edit', row?: DictItem) {
  if (!selectedType.value) return
  itemMode.value = mode
  editingItemId.value = row?.id || null
  itemForm.dictCode = selectedType.value.dictCode
  itemForm.itemValue = row?.itemValue || ''
  itemForm.itemLabel = row?.itemLabel || ''
  itemForm.tagType = row?.tagType || ''
  itemForm.sortOrder = row?.sortOrder ?? 0
  itemForm.status = row?.status || 'ENABLED'
  itemForm.remark = row?.remark || ''
  itemDrawerVisible.value = true
}

async function submitType() {
  await typeFormRef.value?.validate()
  submitLoading.value = true
  try {
    if (typeMode.value === 'create') {
      await createDictType(typeForm)
    } else if (editingTypeId.value) {
      await updateDictType(editingTypeId.value, typeForm)
    }
    ElMessage.success('保存成功')
    typeDrawerVisible.value = false
    selectedType.value = null
    await loadTypes()
  } finally {
    submitLoading.value = false
  }
}

async function submitItem() {
  await itemFormRef.value?.validate()
  submitLoading.value = true
  try {
    if (itemMode.value === 'create') {
      await createDictItem(itemForm)
    } else if (editingItemId.value) {
      await updateDictItem(editingItemId.value, itemForm)
    }
    ElMessage.success('保存成功')
    itemDrawerVisible.value = false
    await loadItems()
  } finally {
    submitLoading.value = false
  }
}

async function toggleTypeStatus(row: DictTypeItem) {
  const nextStatus: UserStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  await updateDictTypeStatus(row.id, nextStatus)
  ElMessage.success(`${statusText(nextStatus)}成功`)
  await loadTypes()
}

async function toggleItemStatus(row: DictItem) {
  const nextStatus: UserStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  await updateDictItemStatus(row.id, nextStatus)
  ElMessage.success(`${statusText(nextStatus)}成功`)
  await loadItems()
}

async function handleDeleteType(row: DictTypeItem) {
  await ElMessageBox.confirm(`确定要删除字典类型「${row.dictName}」吗？存在字典项时不能删除。`, '确认删除', { type: 'warning' })
  await deleteDictType(row.id)
  ElMessage.success('删除成功')
  if (selectedType.value?.id === row.id) selectedType.value = null
  await loadTypes()
}

async function handleDeleteItem(row: DictItem) {
  await ElMessageBox.confirm(`确定要删除字典项「${row.itemLabel}」吗？`, '确认删除', { type: 'warning' })
  await deleteDictItem(row.id)
  ElMessage.success('删除成功')
  await loadItems()
}

function statusText(status: UserStatus) {
  return status === 'ENABLED' ? '启用' : '禁用'
}

function tagType(value?: string) {
  return ['success', 'warning', 'danger', 'info', 'primary'].includes(value || '') ? value as 'success' | 'warning' | 'danger' | 'info' | 'primary' : 'info'
}

onMounted(loadTypes)
</script>

<style scoped>
.dict-layout {
  display: grid;
  grid-template-columns: minmax(520px, 0.95fr) minmax(560px, 1.05fr);
  gap: 16px;
  align-items: start;
}

.dict-page :deep(.el-card) {
  border-radius: 8px;
}

.dict-filter :deep(.el-form-item) {
  margin-bottom: 12px;
}

.dict-type-card,
.dict-item-card {
  min-height: 600px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.item-header h2 {
  margin: 0 0 4px;
  font-size: 18px;
}

.item-header span {
  color: var(--app-text-secondary);
  font-size: 13px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 1180px) {
  .dict-layout {
    grid-template-columns: 1fr;
  }
}
</style>
