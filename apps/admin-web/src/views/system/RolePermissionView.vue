<template>
  <section class="page role-permission-page">
    <div class="role-page-title">
      <h1>角色权限管理</h1>
      <el-button v-if="canCreateRole" type="primary" :icon="Plus" @click="openCreateDrawer">新增角色</el-button>
    </div>

    <div class="role-workspace">
      <el-card shadow="never" class="role-list-panel">
        <template #header>
          <div class="panel-title">角色列表</div>
        </template>
        <div v-loading="roleLoading" class="role-list">
          <div
            v-for="role in roles"
            :key="role.id"
            role="button"
            tabindex="0"
            class="role-item"
            :class="{ active: selectedRole?.id === role.id }"
            @click="selectRole(role)"
            @keydown.enter="selectRole(role)"
          >
            <div class="role-item-main">
              <strong>{{ role.roleName }}</strong>
              <el-icon><ArrowRight /></el-icon>
            </div>
            <p>{{ role.remark || dataScopeText(role.dataScope) }}</p>
            <div class="role-item-meta">
              <span>{{ role.roleCode }}</span>
              <time>{{ formatDate(role.updatedAt || role.createdAt) }}</time>
            </div>
            <div class="role-item-actions">
              <el-button v-if="canUpdateRole" size="small" :icon="Edit" @click.stop="openEditDrawer(role)">编辑</el-button>
              <el-button v-if="canDeleteRole" size="small" type="danger" :icon="Delete" @click.stop="deleteRoleItem(role)">删除</el-button>
            </div>
          </div>
          <el-empty v-if="!roleLoading && !roles.length" description="暂无角色" :image-size="64" />
        </div>
      </el-card>

      <el-card shadow="never" class="permission-panel">
        <template #header>
          <div class="panel-title">{{ selectedRole ? `${selectedRole.roleName} - 权限配置` : '权限配置' }}</div>
        </template>
        <template v-if="selectedRole">
          <el-tabs v-model="activePermissionTab" class="permission-tabs">
            <el-tab-pane label="菜单权限" name="menu" />
            <el-tab-pane label="按钮权限" name="button" />
            <el-tab-pane label="数据权限" name="data" />
          </el-tabs>

          <div v-if="activePermissionTab !== 'data'" class="permission-tree-wrap">
            <el-tree
              ref="permissionTreeRef"
              v-loading="permissionLoading"
              :data="permissionNodes"
              node-key="id"
              show-checkbox
              default-expand-all
              :props="treeProps"
              empty-text="暂无可配置权限"
            />
          </div>

          <el-form v-else :model="dataPermissionForm" label-position="top" class="data-permission-form">
            <el-form-item label="数据范围">
              <el-radio-group v-model="dataPermissionForm.dataScope" class="scope-options">
                <el-radio label="ALL">全部数据</el-radio>
                <el-radio label="DEPARTMENT">本部门数据</el-radio>
                <el-radio label="SELF">本人数据</el-radio>
                <el-radio label="CUSTOM">自定义数据</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="dataPermissionForm.dataScope === 'CUSTOM'" label="自定义数据范围">
              <el-input v-model="dataPermissionForm.dataScopeConfig" type="textarea" :rows="5" maxlength="500" show-word-limit placeholder="请输入部门编码、业务范围或数据规则说明" />
            </el-form-item>
          </el-form>

          <div class="permission-footer">
            <el-button @click="resetPermission">重置</el-button>
            <el-button type="primary" :loading="permissionSaving" :disabled="activePermissionTab === 'data' ? !canUpdateRole : !canAssignMenu" @click="savePermission">保存权限</el-button>
          </div>
        </template>
        <el-empty v-else description="请选择角色后配置权限" :image-size="80" />
      </el-card>
    </div>

    <el-drawer v-model="roleDrawerVisible" :title="drawerTitle" size="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top">
        <el-form-item label="角色编码" prop="roleCode"><el-input v-model="form.roleCode" maxlength="64" placeholder="例如：admin、operator、viewer" /></el-form-item>
        <el-form-item label="角色名称" prop="roleName"><el-input v-model="form.roleName" maxlength="64" placeholder="请输入角色名称" /></el-form-item>
        <el-form-item label="数据权限" prop="dataScope">
          <el-select v-model="form.dataScope" style="width: 100%">
            <el-option label="全部数据" value="ALL" />
            <el-option label="本部门数据" value="DEPARTMENT" />
            <el-option label="本人数据" value="SELF" />
            <el-option label="自定义数据" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.dataScope === 'CUSTOM'" label="自定义数据范围" prop="dataScopeConfig"><el-input v-model="form.dataScopeConfig" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请输入部门编码、业务范围或数据规则说明" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-select v-model="form.status" style="width: 100%"><el-option label="启用" value="ENABLED" /><el-option label="禁用" value="DISABLED" /></el-select></el-form-item>
        <el-form-item label="权限说明"><el-input v-model="form.remark" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请输入角色职责或权限边界说明" /></el-form-item>
      </el-form>
      <template #footer><div class="drawer-footer"><el-button @click="roleDrawerVisible = false">取消</el-button><el-button type="primary" :loading="roleSaving" @click="saveRole">保存</el-button></div></template>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ArrowRight, Delete, Edit, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { assignRoleMenus, createRole, deleteRole, getMenuTree, getRoleDetail, getRoleMenuIds, getRolePage, updateRole, type DataScope, type MenuItem, type RoleItem, type RolePayload, type UserStatus } from '@/api/modules/system'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const roles = ref<RoleItem[]>([])
const selectedRole = ref<RoleItem | null>(null)
const roleLoading = ref(false)
const permissionLoading = ref(false)
const permissionSaving = ref(false)
const roleSaving = ref(false)
const activePermissionTab = ref<'menu' | 'button' | 'data'>('menu')
const menuTree = ref<MenuItem[]>([])
const selectedMenuIds = ref<string[]>([])
const permissionTreeRef = ref()
const treeProps = { label: 'menuName', children: 'children' }
const canCreateRole = computed(() => userStore.hasPermission('system:role:create'))
const canUpdateRole = computed(() => userStore.hasPermission('system:role:update'))
const canDeleteRole = computed(() => userStore.hasPermission('system:role:delete'))
const canAssignMenu = computed(() => userStore.hasPermission('system:role:assign-menu'))
const dataPermissionForm = reactive<{ dataScope: DataScope; dataScopeConfig: string }>({ dataScope: 'ALL', dataScopeConfig: '' })

const roleDrawerVisible = ref(false)
const drawerMode = ref<'create' | 'edit'>('create')
const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<RolePayload>({ roleCode: '', roleName: '', dataScope: 'ALL', dataScopeConfig: '', status: 'ENABLED', remark: '' })
const drawerTitle = computed(() => drawerMode.value === 'create' ? '新增角色' : '编辑角色')
const formRules: FormRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }, { pattern: /^[a-zA-Z][a-zA-Z0-9_:-]*$/, message: '角色编码需以字母开头，只能包含字母、数字、下划线、冒号和短横线', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  dataScope: [{ required: true, message: '请选择数据权限', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const permissionNodes = computed(() => filterPermissionTree(menuTree.value, activePermissionTab.value === 'button' ? 'BUTTON' : 'MENU'))

function filterPermissionTree(nodes: MenuItem[], type: 'MENU' | 'BUTTON'): MenuItem[] {
  return nodes.reduce<MenuItem[]>((result, node) => {
    const children = filterPermissionTree(node.children || [], type)
    if (node.menuType === type || children.length) result.push({ ...node, children })
    return result
  }, [])
}

async function loadRoles(preferredId?: string) {
  roleLoading.value = true
  try {
    const page = await getRolePage({ pageNo: 1, pageSize: 100 })
    roles.value = page.records
    const next = roles.value.find((item) => item.id === preferredId) || roles.value.find((item) => item.id === selectedRole.value?.id) || roles.value[0]
    if (next) await selectRole(next)
    else selectedRole.value = null
  } finally { roleLoading.value = false }
}

async function selectRole(role: RoleItem) {
  selectedRole.value = role
  dataPermissionForm.dataScope = role.dataScope || 'ALL'
  dataPermissionForm.dataScopeConfig = role.dataScopeConfig || ''
  permissionLoading.value = true
  try {
    const [tree, checkedIds] = await Promise.all([getMenuTree(), getRoleMenuIds(role.id)])
    menuTree.value = tree
    selectedMenuIds.value = checkedIds.map(String)
    await nextTick()
    permissionTreeRef.value?.setCheckedKeys(selectedMenuIds.value)
  } finally { permissionLoading.value = false }
}

function resetPermission() {
  if (!selectedRole.value) return
  if (activePermissionTab.value === 'data') {
    dataPermissionForm.dataScope = selectedRole.value.dataScope || 'ALL'
    dataPermissionForm.dataScopeConfig = selectedRole.value.dataScopeConfig || ''
  } else permissionTreeRef.value?.setCheckedKeys(selectedMenuIds.value)
}

async function savePermission() {
  if (!selectedRole.value) return
  permissionSaving.value = true
  try {
    if (activePermissionTab.value === 'data') {
      if (dataPermissionForm.dataScope === 'CUSTOM' && !dataPermissionForm.dataScopeConfig.trim()) {
        ElMessage.warning('选择自定义数据时，请填写自定义数据范围')
        return
      }
      const detail = await getRoleDetail(selectedRole.value.id)
      await updateRole(selectedRole.value.id, { roleCode: detail.roleCode, roleName: detail.roleName, dataScope: dataPermissionForm.dataScope, dataScopeConfig: dataPermissionForm.dataScope === 'CUSTOM' ? dataPermissionForm.dataScopeConfig : '', status: detail.status, remark: detail.remark || '' })
    } else {
      const checked = permissionTreeRef.value?.getCheckedKeys(false) || []
      const halfChecked = permissionTreeRef.value?.getHalfCheckedKeys?.() || []
      await assignRoleMenus(selectedRole.value.id, normalizeSelectedMenuIds([...checked, ...halfChecked]))
      selectedMenuIds.value = normalizeSelectedMenuIds([...checked, ...halfChecked])
    }
    ElMessage.success('权限保存成功，用户重新登录后生效')
    await loadRoles(selectedRole.value.id)
  } finally { permissionSaving.value = false }
}

function normalizeSelectedMenuIds(ids: string[]) {
  const selected = new Set(ids.map(String))
  const menuMap = new Map<string, MenuItem>()
  function collect(nodes: MenuItem[]) { nodes.forEach((node) => { menuMap.set(String(node.id), node); collect(node.children || []) }) }
  collect(menuTree.value)
  Array.from(selected).forEach((id) => { let parentId = menuMap.get(id)?.parentId; while (parentId && String(parentId) !== '0') { selected.add(String(parentId)); parentId = menuMap.get(String(parentId))?.parentId } })
  return Array.from(selected)
}

function resetForm() { editingId.value = null; form.roleCode = ''; form.roleName = ''; form.dataScope = 'ALL'; form.dataScopeConfig = ''; form.status = 'ENABLED'; form.remark = '' }
function openCreateDrawer() { drawerMode.value = 'create'; resetForm(); roleDrawerVisible.value = true }
async function openEditDrawer(role: RoleItem) {
  drawerMode.value = 'edit'; resetForm()
  const detail = await getRoleDetail(role.id)
  editingId.value = detail.id; form.roleCode = detail.roleCode; form.roleName = detail.roleName; form.dataScope = detail.dataScope || 'ALL'; form.dataScopeConfig = detail.dataScopeConfig || ''; form.status = detail.status; form.remark = detail.remark || ''
  roleDrawerVisible.value = true
}
async function saveRole() {
  await formRef.value?.validate()
  if (form.dataScope === 'CUSTOM' && !form.dataScopeConfig?.trim()) { ElMessage.warning('选择自定义数据时，请填写自定义数据范围'); return }
  roleSaving.value = true
  try {
    const payload = { ...form, dataScopeConfig: form.dataScope === 'CUSTOM' ? form.dataScopeConfig : '' }
    if (drawerMode.value === 'create') await createRole(payload)
    else if (editingId.value) await updateRole(editingId.value, payload)
    ElMessage.success('保存成功'); roleDrawerVisible.value = false; await loadRoles(editingId.value || undefined)
  } finally { roleSaving.value = false }
}
async function deleteRoleItem(role: RoleItem) {
  await ElMessageBox.confirm(`确定要删除角色「${role.roleName}」吗？已分配给用户的角色不能删除。`, '确认删除', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  await deleteRole(role.id); ElMessage.success('删除成功'); await loadRoles()
}
function dataScopeText(scope?: DataScope) { return ({ ALL: '全部数据', DEPARTMENT: '本部门数据', SELF: '本人数据', CUSTOM: '自定义数据' } as Record<DataScope, string>)[scope || 'ALL'] }
function formatDate(value?: string) { return value ? value.slice(0, 10) : '-' }

watch(activePermissionTab, async () => { await nextTick(); permissionTreeRef.value?.setCheckedKeys(selectedMenuIds.value) })
onMounted(loadRoles)
</script>

<style scoped>
.role-permission-page { min-height: calc(100vh - 104px); padding-bottom: 20px; }
.role-page-title { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px; }
.role-page-title h1 { margin: 0; color: #1f4dbd; font-size: 18px; font-weight: 700; }
.role-page-title :deep(.el-button) { height: 38px; padding: 0 18px; border-radius: 8px; font-weight: 600; }
.role-workspace { display: grid; grid-template-columns: minmax(330px, 36%) minmax(0, 1fr); gap: 16px; align-items: stretch; }
.role-list-panel, .permission-panel { min-height: 728px; border: 1px solid #dce5f2; border-radius: 9px; background: #fff; }
.role-list-panel :deep(.el-card__header), .permission-panel :deep(.el-card__header) { height: 58px; padding: 0 14px; border-bottom: 1px solid #e0e7f2; display: flex; align-items: center; }
.panel-title { color: #0f172a; font-size: 16px; font-weight: 700; }
.role-list { min-height: 650px; }
.role-item { display: block; width: 100%; min-height: 156px; padding: 14px 13px 12px; border: 0; border-bottom: 1px solid #dce5f2; border-left: 3px solid transparent; background: #fff; color: #172033; text-align: left; cursor: pointer; }
.role-item:focus-visible { outline: 2px solid #3b82f6; outline-offset: -2px; }
.role-item:hover { background: #f7faff; }
.role-item.active { border-left-color: #2754c7; background: #e8eefc; }
.role-item-main { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.role-item-main strong { font-size: 16px; }
.role-item-main .el-icon { color: #8c9bb2; }
.role-item p { min-height: 20px; margin: 7px 0; color: #8793a8; font-size: 13px; }
.role-item-meta { display: flex; justify-content: space-between; color: #9aa6b9; font-size: 12px; }
.role-item-actions { display: flex; gap: 8px; margin-top: 10px; }
.role-item-actions :deep(.el-button) { height: 24px; padding: 0 8px; margin: 0; border-radius: 4px; }
.permission-panel :deep(.el-card__body) { display: flex; min-height: 650px; flex-direction: column; padding: 0 14px 14px; }
.permission-tabs { flex: 0 0 auto; }
.permission-tabs :deep(.el-tabs__header) { margin: 0 0 16px; }
.permission-tabs :deep(.el-tabs__nav-scroll) { overflow: visible; }
.permission-tabs :deep(.el-tabs__nav) { display: flex; width: 100%; }
.permission-tabs :deep(.el-tabs__nav-wrap::after) { display: none; }
.permission-tabs :deep(.el-tabs__item) { flex: 1; min-width: 0; height: 36px; padding: 0; color: #7b879a; font-weight: 600; text-align: center; }
.permission-tabs :deep(.el-tabs__item.is-active) { border-radius: 4px; background: #2850bd; color: #fff; }
.permission-tabs :deep(.el-tabs__active-bar) { display: none; }
.permission-tree-wrap { flex: 1; min-height: 480px; padding: 0 14px; overflow: auto; }
.permission-tree-wrap :deep(.el-tree) { color: #172033; }
.permission-tree-wrap :deep(.el-tree-node__content) { height: 40px; }
.permission-tree-wrap :deep(.el-tree-node__label) { font-size: 13px; font-weight: 600; }
.data-permission-form { flex: 1; min-height: 480px; padding: 16px 14px; }
.scope-options { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; }
.permission-footer { display: flex; justify-content: flex-end; gap: 10px; padding-top: 14px; border-top: 1px solid #e5ebf4; }
.permission-footer :deep(.el-button) { min-width: 64px; height: 34px; border-radius: 8px; }
.drawer-footer { display: flex; justify-content: flex-end; gap: 12px; }
@media (max-width: 1100px) { .role-workspace { grid-template-columns: 1fr; } .role-list-panel, .permission-panel { min-height: auto; } .role-list { min-height: 0; max-height: 420px; overflow: auto; } .permission-tree-wrap, .data-permission-form { min-height: 400px; } }
</style>
