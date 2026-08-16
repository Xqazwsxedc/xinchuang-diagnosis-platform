<template>
  <section class="page role-page">
    <div v-if="!embedded" class="page-header">
      <div>
        <h1>角色权限管理</h1>
        <p>维护后台角色、启用状态、菜单按钮权限和数据权限范围。</p>
      </div>
      <el-button v-if="userStore.hasPermission('system:role:create')" type="primary" :icon="Plus" @click="openCreateDrawer">新增角色</el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query" class="role-filter">
        <el-form-item label="角色编码">
          <el-input v-model="query.roleCode" clearable placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="query.roleName" clearable placeholder="请输入角色名称" />
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

    <el-card shadow="never" class="role-table-card">
      <el-table :data="records" stripe v-loading="loading" row-key="id">
        <el-table-column prop="roleCode" label="角色编码" min-width="180" />
        <el-table-column prop="roleName" label="角色名称" min-width="180" />
        <el-table-column prop="dataScope" label="数据权限" width="140">
          <template #default="{ row }">{{ dataScopeText(row.dataScope) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" round>{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="权限预留说明" min-width="280">
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="430" fixed="right">
          <template #default="{ row }">
            <div class="role-actions">
              <el-button type="primary" link :icon="View" @click="openDetail(row)">详情</el-button>
              <el-button v-if="userStore.hasPermission('system:role:update')" type="primary" link :icon="Edit" @click="openEditDrawer(row)">编辑</el-button>
              <el-button v-if="userStore.hasPermission('system:role:assign-menu')" type="primary" link :icon="Connection" @click="openPermissionDrawer(row)">分配权限</el-button>
              <el-button v-if="userStore.hasPermission('system:role:status')" type="warning" link :icon="SwitchButton" @click="toggleStatus(row)">
                {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
              </el-button>
              <el-button v-if="userStore.hasPermission('system:role:delete')" type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </div>
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

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" maxlength="64" placeholder="例如：admin、operator、viewer" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" maxlength="64" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="数据权限" prop="dataScope">
          <el-select v-model="form.dataScope" style="width: 100%">
            <el-option label="全部数据" value="ALL" />
            <el-option label="本部门数据" value="DEPARTMENT" />
            <el-option label="本人数据" value="SELF" />
            <el-option label="自定义数据" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.dataScope === 'CUSTOM'" label="自定义数据范围" prop="dataScopeConfig">
          <el-input
            v-model="form.dataScopeConfig"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入部门编码、业务范围或数据规则说明"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="权限预留说明" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="可记录该角色的菜单、按钮、数据权限边界，后续将扩展为权限树配置"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">提交保存</el-button>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="detailVisible" title="角色详情" size="520px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="角色编码">{{ detail.roleCode }}</el-descriptions-item>
        <el-descriptions-item label="角色名称">{{ detail.roleName }}</el-descriptions-item>
        <el-descriptions-item label="数据权限">{{ dataScopeText(detail.dataScope) }}</el-descriptions-item>
        <el-descriptions-item label="自定义数据范围">{{ detail.dataScopeConfig || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === 'ENABLED' ? 'success' : 'info'" round>{{ statusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="权限预留说明">{{ detail.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(detail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(detail.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <el-drawer v-model="permissionVisible" :title="`分配权限 - ${permissionRoleName}`" size="560px">
      <el-tree
        ref="permissionTreeRef"
        :data="menuTree"
        node-key="id"
        show-checkbox
        default-expand-all
        :props="{ label: 'menuName', children: 'children' }"
      >
        <template #default="{ data }">
          <span class="permission-node">
            <el-tag size="small" :type="data.menuType === 'MENU' ? 'primary' : 'info'">{{ data.menuType === 'MENU' ? '菜单' : '按钮' }}</el-tag>
            <span>{{ data.menuName }}</span>
            <small v-if="data.permissionCode">{{ data.permissionCode }}</small>
          </span>
        </template>
      </el-tree>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="permissionVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitPermissions">保存权限</el-button>
        </div>
      </template>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { Connection, Delete, Edit, Plus, RefreshLeft, Search, SwitchButton, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  assignRoleMenus,
  createRole,
  deleteRole,
  getMenuTree,
  getRoleDetail,
  getRoleMenuIds,
  getRolePage,
  updateRole,
  updateRoleStatus,
  type DataScope,
  type MenuItem,
  type RoleItem,
  type RolePayload,
  type UserStatus
} from '@/api/modules/system'
import { useUserStore } from '@/stores/user'

withDefaults(defineProps<{ embedded?: boolean }>(), {
  embedded: false
})

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const records = ref<RoleItem[]>([])
const total = ref(0)
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  roleCode: '',
  roleName: '',
  status: '' as UserStatus | ''
})

const drawerVisible = ref(false)
const drawerMode = ref<'create' | 'edit'>('create')
const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<RolePayload>({
  roleCode: '',
  roleName: '',
  dataScope: 'ALL',
  dataScopeConfig: '',
  status: 'ENABLED',
  remark: ''
})

const detailVisible = ref(false)
const detail = ref<RoleItem | null>(null)
const permissionVisible = ref(false)
const permissionRoleId = ref<string | null>(null)
const permissionRoleName = ref('')
const permissionTreeRef = ref()
const menuTree = ref<MenuItem[]>([])

const drawerTitle = computed(() => drawerMode.value === 'create' ? '新增角色' : '编辑角色')

const formRules: FormRules = {
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_:-]*$/, message: '角色编码需以字母开头，只能包含字母、数字、下划线、冒号和短横线', trigger: 'blur' }
  ],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  dataScope: [{ required: true, message: '请选择数据权限', trigger: 'change' }],
  dataScopeConfig: [{ max: 500, message: '自定义数据范围最多 500 个字符', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const page = await getRolePage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      roleCode: query.roleCode || undefined,
      roleName: query.roleName || undefined,
      status: query.status || undefined
    })
    records.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

function statusText(status: UserStatus) {
  return status === 'ENABLED' ? '启用' : '禁用'
}

function dataScopeText(scope?: DataScope) {
  const map: Record<DataScope, string> = {
    ALL: '全部数据',
    DEPARTMENT: '本部门数据',
    SELF: '本人数据',
    CUSTOM: '自定义数据'
  }
  return scope ? map[scope] || scope : '全部数据'
}

function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

function handleSearch() {
  query.pageNo = 1
  loadData()
}

function reset() {
  query.roleCode = ''
  query.roleName = ''
  query.status = ''
  query.pageNo = 1
  loadData()
}

function resetForm() {
  editingId.value = null
  form.roleCode = ''
  form.roleName = ''
  form.dataScope = 'ALL'
  form.dataScopeConfig = ''
  form.status = 'ENABLED'
  form.remark = ''
}

function openCreateDrawer() {
  drawerMode.value = 'create'
  resetForm()
  drawerVisible.value = true
}

async function openEditDrawer(row: RoleItem) {
  drawerMode.value = 'edit'
  resetForm()
  const item = await getRoleDetail(row.id)
  editingId.value = item.id
  form.roleCode = item.roleCode
  form.roleName = item.roleName
  form.dataScope = item.dataScope || 'ALL'
  form.dataScopeConfig = item.dataScopeConfig || ''
  form.status = item.status
  form.remark = item.remark || ''
  drawerVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  if (form.dataScope === 'CUSTOM' && !form.dataScopeConfig?.trim()) {
    ElMessage.warning('选择自定义数据时，请填写自定义数据范围')
    return
  }
  submitLoading.value = true
  try {
    const payload = { ...form, dataScopeConfig: form.dataScope === 'CUSTOM' ? form.dataScopeConfig : '' }
    if (drawerMode.value === 'create') {
      await createRole(payload)
    } else if (editingId.value) {
      await updateRole(editingId.value, payload)
    }
    ElMessage.success('保存成功')
    drawerVisible.value = false
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

async function toggleStatus(row: RoleItem) {
  const nextStatus: UserStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  await updateRoleStatus(row.id, nextStatus)
  ElMessage.success(`${statusText(nextStatus)}成功`)
  await loadData()
}

async function openDetail(row: RoleItem) {
  detail.value = await getRoleDetail(row.id)
  detailVisible.value = true
}

async function handleDelete(row: RoleItem) {
  await ElMessageBox.confirm(`确定要删除角色「${row.roleName}」吗？已分配给用户的角色不能删除。`, '确认删除', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  })
  await deleteRole(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

async function openPermissionDrawer(row: RoleItem) {
  permissionRoleId.value = row.id
  permissionRoleName.value = row.roleName
  const [tree, checkedIds] = await Promise.all([getMenuTree(), getRoleMenuIds(row.id)])
  menuTree.value = tree
  permissionVisible.value = true
  await nextTick()
  permissionTreeRef.value?.setCheckedKeys(checkedIds)
}

async function submitPermissions() {
  if (!permissionRoleId.value) return
  submitLoading.value = true
  try {
    const checked = permissionTreeRef.value?.getCheckedKeys(false) || []
    const halfChecked = permissionTreeRef.value?.getHalfCheckedKeys?.() || []
    await assignRoleMenus(permissionRoleId.value, normalizeSelectedMenuIds([...checked, ...halfChecked]))
    ElMessage.success('权限保存成功，用户重新登录后生效')
    permissionVisible.value = false
  } finally {
    submitLoading.value = false
  }
}

function normalizeSelectedMenuIds(ids: string[]) {
  const selected = new Set(ids.map(String))
  const menuMap = new Map<string, MenuItem>()

  function collect(nodes: MenuItem[]) {
    nodes.forEach((node) => {
      menuMap.set(String(node.id), node)
      collect(node.children || [])
    })
  }

  collect(menuTree.value)
  Array.from(selected).forEach((id) => {
    let parentId = menuMap.get(id)?.parentId
    while (parentId && String(parentId) !== '0') {
      selected.add(String(parentId))
      parentId = menuMap.get(String(parentId))?.parentId
    }
  })
  return Array.from(selected)
}

onMounted(loadData)
</script>

<style scoped>
.role-page :deep(.el-card) {
  border-radius: 8px;
}

.role-filter :deep(.el-form-item) {
  margin-bottom: 0;
}

.role-table-card {
  min-height: 520px;
}

.role-actions {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 14px;
  white-space: nowrap;
}

.role-actions :deep(.el-button) {
  margin-left: 0;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.permission-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.permission-node small {
  color: var(--app-text-secondary);
}
</style>
