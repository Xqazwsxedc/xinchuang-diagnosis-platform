<template>
  <section class="page security-page">
    <div class="page-header">
      <div>
        <h1>安全登录管控</h1>
        <p>查看账号登录行为和异常登录记录。</p>
      </div>
    </div>

    <AuditLogView embedded login-only login-source="security" />
  </section>
</template>

<script setup lang="ts">
import AuditLogView from '@/views/system/AuditLogView.vue'
</script>

<style scoped>
.security-page {
  padding-bottom: 20px;
}

.security-page :deep(.audit-page) {
  padding: 0;
}
</style>
