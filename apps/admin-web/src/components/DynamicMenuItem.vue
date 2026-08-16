<template>
  <el-sub-menu v-if="visibleChildren.length" :index="menu.id">
    <template #title>
      <el-icon v-if="iconComponent"><component :is="iconComponent" /></el-icon>
      <span>{{ menu.menuName }}</span>
    </template>
    <DynamicMenuItem v-for="child in visibleChildren" :key="child.id" :menu="child" />
  </el-sub-menu>
  <el-menu-item v-else-if="menu.routePath" :index="menu.routePath">
    <el-icon v-if="iconComponent"><component :is="iconComponent" /></el-icon>
    <span>{{ menu.menuName }}</span>
  </el-menu-item>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  Coin,
  Monitor,
  Odometer,
  Setting,
  TrendCharts,
  Warning
} from '@element-plus/icons-vue'
import type { AuthMenuItem } from '@/api/modules/auth'

const props = defineProps<{ menu: AuthMenuItem }>()

const visibleChildren = computed(() =>
  (props.menu.children || []).filter((item) => item.menuType === 'MENU' && item.visible === 1 && item.status === 'ENABLED')
)

const iconMap = {
  Coin,
  Monitor,
  Odometer,
  Setting,
  TrendCharts,
  Warning
}

const iconComponent = computed(() => props.menu.icon ? iconMap[props.menu.icon as keyof typeof iconMap] : null)
</script>
