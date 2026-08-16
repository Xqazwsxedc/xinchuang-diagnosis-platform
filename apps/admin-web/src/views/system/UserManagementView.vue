<template>
  <section class="page user-page">
    <div class="page-header">
      <div>
        <h1>用户账号管理</h1>
        <p>维护后台用户、账号状态和角色分配，为后续菜单权限与按钮权限扩展打基础。</p>
      </div>
      <el-button v-if="userStore.hasPermission('system:user:create')" type="primary" :icon="Plus" @click="openCreateDrawer">新增用户</el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query" class="user-filter">
        <el-form-item label="账号">
          <el-input v-model="query.username" clearable placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="query.realName" clearable placeholder="请输入姓名" />
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

    <el-card shadow="never" class="user-table-card">
      <el-table :data="records" stripe v-loading="loading" row-key="id">
        <el-table-column prop="username" label="账号" min-width="150" />
        <el-table-column prop="realName" label="姓名" width="140" />
        <el-table-column prop="department" label="部门" min-width="160">
          <template #default="{ row }">{{ row.department || '-' }}</template>
        </el-table-column>
        <el-table-column label="角色" min-width="220">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role.id" class="role-tag" round>{{ role.roleName }}</el-tag>
            <span v-if="!row.roles?.length">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" round>{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="310" fixed="right">
          <template #default="{ row }">
            <el-button v-if="userStore.hasPermission('system:user:update')" type="primary" link :icon="Edit" @click="openEditDrawer(row)">编辑</el-button>
            <el-button v-if="userStore.hasPermission('system:user:status')" type="warning" link :icon="SwitchButton" @click="toggleStatus(row)">
              {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
            <el-button v-if="userStore.hasPermission('system:user:reset-password')" type="primary" link :icon="Key" @click="openPasswordDialog(row)">重置密码</el-button>
            <el-button v-if="userStore.hasPermission('system:user:delete')" type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
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
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" :disabled="drawerMode === 'edit'" maxlength="64" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item v-if="drawerMode === 'create'" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password maxlength="32" placeholder="请输入初始密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" maxlength="64" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="头像地址" prop="avatarUrl">
          <el-input v-model="form.avatarUrl" maxlength="500" placeholder="请输入头像图片地址" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="form.department" maxlength="100" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="32" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" maxlength="128" placeholder="请输入邮箱" />
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

    <el-dialog v-model="passwordVisible" title="重置密码" width="420px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="passwordForm.password" type="password" show-password maxlength="32" placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Key, Plus, RefreshLeft, Search, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createUser,
  deleteUser,
  getRoles,
  getUserDetail,
  getUsers,
  resetUserPassword,
  updateUser,
  updateUserStatus,
  type RoleItem,
  type UserItem,
  type UserStatus
} from '@/api/modules/system'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const records = ref<UserItem[]>([])
const roles = ref<RoleItem[]>([])
const total = ref(0)
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  username: '',
  realName: '',
  status: '' as UserStatus | ''
})

const drawerVisible = ref(false)
const drawerMode = ref<'create' | 'edit'>('create')
const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()
const form = reactive({
  username: '',
  password: '',
  realName: '',
  avatarUrl: '',
  department: '',
  phone: '',
  email: '',
  status: 'ENABLED' as UserStatus,
  remark: '',
  roleIds: [] as string[]
})

const passwordVisible = ref(false)
const passwordUserId = ref<string | null>(null)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({ password: '' })

const drawerTitle = computed(() => drawerMode.value === 'create' ? '新增用户' : '编辑用户')
const passwordPattern = /^\S{6,32}$/
const avatarUrlPattern = /^$|^https?:\/\/.+|^data:image\/.+;base64,.+/
const phonePattern = /^$|^1\d{10}$|^\+?[0-9\- ]{6,32}$/

const formRules = computed<FormRules>(() => ({
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: drawerMode.value === 'create'
    ? [
        { required: true, message: '请输入初始密码', trigger: 'blur' },
        { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' },
        { pattern: passwordPattern, message: '密码不能包含空白字符', trigger: 'blur' }
      ]
    : [],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleIds: [{ required: true, type: 'array', message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: phonePattern, message: '手机号格式不正确', trigger: 'blur' }],
  avatarUrl: [
    { max: 500, message: '头像地址最多 500 个字符', trigger: 'blur' },
    { pattern: avatarUrlPattern, message: '头像地址需为 http(s) 图片地址或 data:image base64', trigger: 'blur' }
  ]
}))

const passwordRules: FormRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' },
    { pattern: passwordPattern, message: '密码不能包含空白字符', trigger: 'blur' }
  ]
}

async function loadData() {
  loading.value = true
  try {
    const page = await getUsers({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      username: query.username || undefined,
      realName: query.realName || undefined,
      status: query.status || undefined
    })
    records.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  roles.value = await getRoles()
}

function statusText(status: UserStatus) {
  return status === 'ENABLED' ? '启用' : '禁用'
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
  query.username = ''
  query.realName = ''
  query.status = ''
  query.pageNo = 1
  loadData()
}

function resetForm() {
  editingId.value = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.avatarUrl = ''
  form.department = ''
  form.phone = ''
  form.email = ''
  form.status = 'ENABLED'
  form.remark = ''
  form.roleIds = []
}

async function openCreateDrawer() {
  drawerMode.value = 'create'
  resetForm()
  await loadRoles()
  drawerVisible.value = true
}

async function openEditDrawer(row: UserItem) {
  drawerMode.value = 'edit'
  resetForm()
  await loadRoles()
  const detail = await getUserDetail(row.id)
  editingId.value = detail.id
  form.username = detail.username
  form.realName = detail.realName
  form.avatarUrl = detail.avatarUrl || ''
  form.department = detail.department || ''
  form.phone = detail.phone || ''
  form.email = detail.email || ''
  form.status = detail.status
  form.remark = detail.remark || ''
  form.roleIds = detail.roleIds || []
  drawerVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (drawerMode.value === 'create') {
      await createUser({ ...form })
    } else if (editingId.value) {
      await updateUser(editingId.value, {
        realName: form.realName,
        avatarUrl: form.avatarUrl,
        department: form.department,
        phone: form.phone,
        email: form.email,
        status: form.status,
        remark: form.remark,
        roleIds: form.roleIds
      })
    }
    ElMessage.success('保存成功')
    drawerVisible.value = false
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

async function toggleStatus(row: UserItem) {
  const nextStatus: UserStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  await updateUserStatus(row.id, nextStatus)
  ElMessage.success(`${statusText(nextStatus)}成功`)
  await loadData()
}

function openPasswordDialog(row: UserItem) {
  passwordUserId.value = row.id
  passwordForm.password = ''
  passwordVisible.value = true
}

async function submitPassword() {
  await passwordFormRef.value?.validate()
  if (!passwordUserId.value) return
  submitLoading.value = true
  try {
    await resetUserPassword(passwordUserId.value, passwordForm.password)
    ElMessage.success('密码已重置')
    passwordVisible.value = false
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: UserItem) {
  await ElMessageBox.confirm(`确定要删除用户「${row.username}」吗？`, '确认删除', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.user-page :deep(.el-card) {
  border-radius: 8px;
}

.user-filter :deep(.el-form-item) {
  margin-bottom: 0;
}

.user-table-card {
  min-height: 520px;
}

.role-tag {
  margin-right: 6px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
