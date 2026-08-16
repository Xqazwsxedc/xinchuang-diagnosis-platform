<template>
  <section class="page base-config-page">
    <div class="page-header">
      <div>
        <h1>系统基础配置</h1>
        <p>集中维护系统字典和运行参数。</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="management-tabs">
      <el-tab-pane v-if="userStore.hasPermission('system:dict:view')" label="字典管理" name="dict">
        <DictManagementView embedded />
      </el-tab-pane>
      <el-tab-pane label="系统配置" name="config">
        <ConfigManagementView embedded />
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watchEffect } from 'vue'
import DictManagementView from '@/views/system/DictManagementView.vue'
import ConfigManagementView from '@/views/system/ConfigManagementView.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const canViewDict = computed(() => userStore.hasPermission('system:dict:view'))
const activeTab = ref(canViewDict.value ? 'dict' : 'config')

watchEffect(() => {
  if (!canViewDict.value && activeTab.value === 'dict') {
    activeTab.value = 'config'
  }
})
</script>

<style scoped>
.base-config-page {
  padding-bottom: 20px;
}

.management-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.management-tabs :deep(.el-tabs__content) {
  overflow: visible;
}

.management-tabs :deep(.dict-page),
.management-tabs :deep(.config-page) {
  padding: 0;
}
</style>
