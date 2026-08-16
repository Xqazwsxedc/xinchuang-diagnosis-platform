<template>
  <section class="page profile-page">
    <div class="page-header">
      <div>
        <h1>个人中心</h1>
        <p>查看个人信息，维护头像、名称和登录密码。</p>
      </div>
    </div>

    <div class="profile-layout">
      <el-card shadow="never" class="profile-summary">
        <el-avatar :src="profileForm.avatarUrl" :size="96">{{ avatarText }}</el-avatar>
        <h2>{{ profileForm.realName || userStore.realName || '-' }}</h2>
        <p>{{ userStore.username }}</p>
        <div class="role-list">
          <el-tag v-for="role in roleNames" :key="role" round>{{ role }}</el-tag>
          <span v-if="roleNames.length === 0">-</span>
        </div>
      </el-card>

      <el-card shadow="never" class="profile-card">
        <template #header>个人资料</template>
        <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-position="top">
          <el-form-item label="账号">
            <el-input v-model="profileForm.username" disabled />
          </el-form-item>
          <el-form-item label="头像地址" prop="avatarUrl">
            <el-input v-model="profileForm.avatarUrl" maxlength="500" placeholder="请输入头像图片地址" />
          </el-form-item>
          <el-form-item label="名称" prop="realName">
            <el-input v-model="profileForm.realName" maxlength="64" placeholder="请输入名称" />
          </el-form-item>
          <el-form-item label="部门" prop="department">
            <el-input v-model="profileForm.department" maxlength="100" placeholder="请输入部门" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="profileForm.phone" maxlength="32" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="profileForm.email" maxlength="128" placeholder="请输入邮箱" />
          </el-form-item>
          <div class="form-actions">
            <el-button type="primary" :loading="profileLoading" @click="submitProfile">保存资料</el-button>
          </div>
        </el-form>
      </el-card>

      <el-card shadow="never" class="password-card">
        <template #header>修改密码</template>
        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password maxlength="32" placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password maxlength="32" placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password maxlength="32" placeholder="请再次输入新密码" />
          </el-form-item>
          <div class="form-actions">
            <el-button type="primary" :loading="passwordLoading" @click="submitPassword">修改密码</el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { changePassword, getCurrentUser, getProfile, updateProfile } from '@/api/modules/auth'
import { useUserStore } from '@/stores/user'

interface ProfileRole {
  roleName?: string
  roleCode?: string
}

const userStore = useUserStore()
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const profileLoading = ref(false)
const passwordLoading = ref(false)
const roleNames = ref<string[]>([])

const profileForm = reactive({
  username: '',
  realName: '',
  avatarUrl: '',
  department: '',
  phone: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const avatarText = computed(() => (profileForm.realName || userStore.username || '用').slice(0, 1))

const passwordPattern = /^\S{6,32}$/
const avatarUrlPattern = /^$|^https?:\/\/.+|^data:image\/.+;base64,.+/
const phonePattern = /^$|^1\d{10}$|^\+?[0-9\- ]{6,32}$/

const profileRules: FormRules = {
  realName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  avatarUrl: [
    { max: 500, message: '头像地址最多 500 个字符', trigger: 'blur' },
    { pattern: avatarUrlPattern, message: '头像地址需为 http(s) 图片地址或 data:image base64', trigger: 'blur' }
  ],
  department: [{ max: 100, message: '部门最多 100 个字符', trigger: 'blur' }],
  phone: [{ pattern: phonePattern, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' },
    { pattern: passwordPattern, message: '密码不能包含空白字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' },
    { pattern: passwordPattern, message: '密码不能包含空白字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) callback(new Error('两次输入的新密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

async function loadProfile() {
  const profile = await getProfile()
  profileForm.username = profile.username
  profileForm.realName = profile.realName
  profileForm.avatarUrl = profile.avatarUrl || ''
  profileForm.department = profile.department || ''
  profileForm.phone = profile.phone || ''
  profileForm.email = profile.email || ''
  roleNames.value = (profile.roles as ProfileRole[] || []).map((role) => role.roleName || role.roleCode || '').filter(Boolean)
}

async function refreshSessionUser() {
  const user = await getCurrentUser()
  userStore.setUser({
    id: user.id,
    username: user.username,
    realName: user.realName,
    avatarUrl: user.avatarUrl || '',
    department: user.department || '',
    phone: user.phone || '',
    email: user.email || '',
    roles: user.roles,
    menus: user.menus,
    permissions: user.permissions
  })
}

async function submitProfile() {
  await profileFormRef.value?.validate()
  profileLoading.value = true
  try {
    await updateProfile({
      realName: profileForm.realName,
      avatarUrl: profileForm.avatarUrl,
      department: profileForm.department,
      phone: profileForm.phone,
      email: profileForm.email
    })
    await refreshSessionUser()
    ElMessage.success('个人资料已保存')
  } finally {
    profileLoading.value = false
  }
}

async function submitPassword() {
  await passwordFormRef.value?.validate()
  if (passwordForm.oldPassword === passwordForm.newPassword) {
    ElMessage.warning('新密码不能与原密码一致')
    return
  }
  passwordLoading.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    ElMessage.success('密码已修改')
  } finally {
    passwordLoading.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 16px;
}

.profile-summary {
  align-self: start;
  text-align: center;
}

.profile-summary h2 {
  margin: 16px 0 6px;
  font-size: 20px;
}

.profile-summary p {
  margin: 0 0 14px;
  color: var(--app-text-secondary);
}

.role-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.profile-card,
.password-card {
  min-width: 0;
}

.password-card {
  grid-column: 2;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .password-card {
    grid-column: auto;
  }
}
</style>
