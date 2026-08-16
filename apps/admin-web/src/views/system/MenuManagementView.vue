<template>
  <section class="page menu-page">
    <div v-if="!embedded" class="page-header">
      <div>
        <h1>菜单权限管理</h1>
        <p>维护系统菜单、按钮权限编码和路由信息，为角色权限树和动态菜单提供配置来源。</p>
      </div>
      <el-button v-if="userStore.hasPermission('system:menu:create')" type="primary" :icon="Plus" @click="openCreateDrawer">新增菜单</el-button>
    </div>

    <el-card shadow="never" class="menu-table-card">
      <el-table
        :data="menuTree"
        row-key="id"
        stripe
        v-loading="loading"
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="menuName" label="名称" min-width="220" />
        <el-table-column prop="menuType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.menuType === 'MENU' ? 'primary' : 'info'" round>{{ row.menuType === 'MENU' ? '菜单' : '按钮' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="permissionCode" label="权限编码" min-width="220">
          <template #default="{ row }">{{ row.permissionCode || '-' }}</template>
        </el-table-column>
        <el-table-column prop="routePath" label="路由" min-width="180">
          <template #default="{ row }">{{ row.routePath || '-' }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column prop="visible" label="显示" width="90">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'" round>{{ row.visible === 1 ? '显示' : '隐藏' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" round>{{ row.status === 'ENABLED' ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-if="userStore.hasPermission('system:menu:update')" type="primary" link :icon="Edit" @click="openEditDrawer(row)">编辑</el-button>
            <el-button v-if="userStore.hasPermission('system:menu:status')" type="warning" link :icon="SwitchButton" @click="toggleStatus(row)">
              {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
            <el-button v-if="userStore.hasPermission('system:menu:delete')" type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top">
        <el-form-item label="父级菜单" prop="parentId">
          <el-select v-model="form.parentId" filterable style="width: 100%">
            <el-option label="根节点" value="0" />
            <el-option v-for="item in parentOptions" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio-button label="MENU">菜单</el-radio-button>
            <el-radio-button label="BUTTON">按钮</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="名称" prop="menuName">
          <el-input v-model="form.menuName" maxlength="64" placeholder="请输入菜单或按钮名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="permissionCode">
          <el-input v-model="form.permissionCode" maxlength="128" placeholder="例如：system:menu:create" />
        </el-form-item>
        <el-form-item label="路由地址">
          <el-input v-model="form.routePath" maxlength="255" placeholder="例如：/system/menus" />
        </el-form-item>
        <el-form-item label="组件路径预留">
          <el-input v-model="form.componentPath" maxlength="255" placeholder="后续动态路由组件路径预留" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" maxlength="64" placeholder="例如：Setting" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="是否显示" prop="visible">
          <el-switch v-model="visibleSwitch" active-text="显示" inactive-text="隐藏" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请输入备注" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createMenu,
  deleteMenu,
  getMenuDetail,
  getMenuTree,
  updateMenu,
  updateMenuStatus,
  type MenuItem,
  type MenuPayload,
  type UserStatus
} from '@/api/modules/system'
import { useUserStore } from '@/stores/user'

withDefaults(defineProps<{ embedded?: boolean }>(), {
  embedded: false
})

interface ParentOption {
  id: string
  label: string
}

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const menuTree = ref<MenuItem[]>([])
const drawerVisible = ref(false)
const drawerMode = ref<'create' | 'edit'>('create')
const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<MenuPayload>({
  parentId: '0',
  menuType: 'MENU',
  menuName: '',
  permissionCode: '',
  routePath: '',
  componentPath: '',
  icon: '',
  sortOrder: 0,
  visible: 1,
  status: 'ENABLED',
  remark: ''
})

const drawerTitle = computed(() => drawerMode.value === 'create' ? '新增菜单权限' : '编辑菜单权限')
const visibleSwitch = computed({
  get: () => form.visible === 1,
  set: (value: boolean) => {
    form.visible = value ? 1 : 0
  }
})

const parentOptions = computed(() => {
  const result: ParentOption[] = []
  function walk(list: MenuItem[], level = 0) {
    for (const item of list) {
      if (item.menuType === 'MENU' && item.id !== editingId.value) {
        result.push({ id: item.id, label: `${'　'.repeat(level)}${item.menuName}` })
        walk(item.children || [], level + 1)
      }
    }
  }
  walk(menuTree.value)
  return result
})

const formRules: FormRules = {
  parentId: [{ required: true, message: '请选择父级菜单', trigger: 'change' }],
  menuType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  menuName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  permissionCode: [{ max: 128, message: '权限编码最多 128 个字符', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    menuTree.value = await getMenuTree()
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editingId.value = null
  form.parentId = '0'
  form.menuType = 'MENU'
  form.menuName = ''
  form.permissionCode = ''
  form.routePath = ''
  form.componentPath = ''
  form.icon = ''
  form.sortOrder = 0
  form.visible = 1
  form.status = 'ENABLED'
  form.remark = ''
}

function openCreateDrawer() {
  drawerMode.value = 'create'
  resetForm()
  drawerVisible.value = true
}

async function openEditDrawer(row: MenuItem) {
  drawerMode.value = 'edit'
  resetForm()
  const item = await getMenuDetail(row.id)
  editingId.value = item.id
  form.parentId = item.parentId || '0'
  form.menuType = item.menuType
  form.menuName = item.menuName
  form.permissionCode = item.permissionCode || ''
  form.routePath = item.routePath || ''
  form.componentPath = item.componentPath || ''
  form.icon = item.icon || ''
  form.sortOrder = item.sortOrder
  form.visible = item.visible
  form.status = item.status
  form.remark = item.remark || ''
  drawerVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (drawerMode.value === 'create') {
      await createMenu(form)
    } else if (editingId.value) {
      await updateMenu(editingId.value, form)
    }
    ElMessage.success('保存成功')
    drawerVisible.value = false
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

async function toggleStatus(row: MenuItem) {
  const nextStatus: UserStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  await updateMenuStatus(row.id, nextStatus)
  ElMessage.success(nextStatus === 'ENABLED' ? '启用成功' : '禁用成功')
  await loadData()
}

async function handleDelete(row: MenuItem) {
  await ElMessageBox.confirm(`确定要删除「${row.menuName}」吗？存在子节点或已分配角色时不能删除。`, '确认删除', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  })
  await deleteMenu(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.menu-page :deep(.el-card) {
  border-radius: 8px;
}

.menu-table-card {
  min-height: 560px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
