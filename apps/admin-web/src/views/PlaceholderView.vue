<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>{{ title }}</h1>
        <p>第一期保留界面与数据结构，真实 AI 算法与自动修复能力后续接入。</p>
      </div>
    </div>
    <el-card shadow="never">
      <el-empty description="模块骨架已预留，下一阶段接入页面和接口" />
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => String(route.meta.title || '功能模块'))
</script>

