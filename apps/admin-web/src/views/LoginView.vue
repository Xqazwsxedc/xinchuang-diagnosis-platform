<template>
  <div class="login-page">
    <section class="login-brand">
      <div class="brand-card">
        <el-icon><Coin /></el-icon>
        <div>
          <h1>信创数据库</h1>
          <h2>智能故障诊断及修复平台</h2>
        </div>
      </div>
      <p>基于 AI 技术的数据库智能运维管理平台，提供故障预测、缺陷检测、性能优化等全方位能力。</p>
    </section>
    <section class="login-panel">
      <el-form :model="form" class="login-form" @submit.prevent="handleLogin">
        <h2>登录系统</h2>
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <div class="login-options">
          <el-checkbox v-model="remember">记住密码</el-checkbox>
          <el-button link type="primary">忘记密码?</el-button>
        </div>
        <el-button class="login-submit" type="primary" native-type="submit" :loading="loading">登录</el-button>
      </el-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Coin } from '@element-plus/icons-vue'
import { loginApi } from '@/api/modules/auth'
import { useUserStore } from '@/stores/user'
import { resolveHomeRoute } from '@/utils/navigation'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const remember = ref(true)
const form = reactive({ username: 'admin', password: 'password' })

async function handleLogin() {
  loading.value = true
  try {
    const result = await loginApi(form)
    userStore.setSession({
      token: result.token,
      id: result.user.id,
      username: result.user.username,
      realName: result.user.realName,
      avatarUrl: result.user.avatarUrl || '',
      department: result.user.department || '',
      phone: result.user.phone || '',
      email: result.user.email || '',
      roles: result.user.roles,
      menus: result.user.menus,
      permissions: result.user.permissions
    })
    router.push(resolveHomeRoute(result.user.menus))
  } finally {
    loading.value = false
  }
}
</script>
