<template>
  <section class="page audit-page">
    <div v-if="!embedded" class="page-header">
      <div>
        <h1>日志审计</h1>
        <p>集中查看系统登录行为和核心业务操作记录，支撑账号安全、问题追踪和审计留痕。</p>
      </div>
    </div>

    <el-card shadow="never" class="audit-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="登录日志" name="login">
          <el-form :inline="true" :model="loginQuery" class="audit-filter">
            <el-form-item label="账号">
              <el-input v-model="loginQuery.username" clearable placeholder="请输入账号" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="loginQuery.loginStatus" clearable placeholder="全部状态" style="width: 140px">
                <el-option label="成功" value="SUCCESS" />
                <el-option label="失败" value="FAILED" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间">
              <el-date-picker
                v-model="loginTimeRange"
                type="datetimerange"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="searchLoginLogs">查询</el-button>
              <el-button :icon="RefreshLeft" @click="resetLoginLogs">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="loginRecords" stripe v-loading="loginLoading" row-key="id">
            <el-table-column prop="username" label="账号" min-width="150" />
            <el-table-column prop="loginStatus" label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="row.loginStatus === 'SUCCESS' ? 'success' : 'danger'" round>{{ logStatusText(row.loginStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="loginIp" label="登录 IP" min-width="150">
              <template #default="{ row }">{{ row.loginIp || '-' }}</template>
            </el-table-column>
            <el-table-column prop="failureReason" label="失败原因" min-width="180">
              <template #default="{ row }">{{ row.failureReason || '-' }}</template>
            </el-table-column>
            <el-table-column prop="deviceInfo" label="客户端信息" min-width="260" show-overflow-tooltip>
              <template #default="{ row }">{{ row.deviceInfo || '-' }}</template>
            </el-table-column>
            <el-table-column prop="loginTime" label="登录时间" width="180">
              <template #default="{ row }">{{ formatTime(row.loginTime) }}</template>
            </el-table-column>
          </el-table>

          <div class="table-footer">
            <el-pagination
              v-model:current-page="loginQuery.pageNo"
              v-model:page-size="loginQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :total="loginTotal"
              :page-sizes="[10, 20, 50, 100]"
              @change="loadLoginLogs"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="!loginOnly" label="操作日志" name="operation">
          <el-form :inline="true" :model="operationQuery" class="audit-filter">
            <el-form-item label="操作人">
              <el-input v-model="operationQuery.operatorName" clearable placeholder="请输入操作人" />
            </el-form-item>
            <el-form-item label="模块">
              <el-select v-model="operationQuery.moduleName" clearable placeholder="全部模块" style="width: 160px">
                <el-option label="数据台账" value="数据台账" />
                <el-option label="用户管理" value="用户管理" />
                <el-option label="角色管理" value="角色管理" />
                <el-option label="菜单权限管理" value="菜单权限管理" />
              </el-select>
            </el-form-item>
            <el-form-item label="操作">
              <el-select v-model="operationQuery.operationType" clearable placeholder="全部操作" style="width: 150px">
                <el-option label="新增" value="CREATE" />
                <el-option label="编辑" value="UPDATE" />
                <el-option label="删除" value="DELETE" />
                <el-option label="状态变更" value="STATUS" />
                <el-option label="重置密码" value="RESET_PASSWORD" />
                <el-option label="分配权限" value="ASSIGN_PERMISSION" />
              </el-select>
            </el-form-item>
            <el-form-item label="结果">
              <el-select v-model="operationQuery.operationResult" clearable placeholder="全部结果" style="width: 130px">
                <el-option label="成功" value="SUCCESS" />
                <el-option label="失败" value="FAILED" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间">
              <el-date-picker
                v-model="operationTimeRange"
                type="datetimerange"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="searchOperationLogs">查询</el-button>
              <el-button :icon="RefreshLeft" @click="resetOperationLogs">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="operationRecords" stripe v-loading="operationLoading" row-key="id">
            <el-table-column prop="operatorNameSnapshot" label="操作人" width="130">
              <template #default="{ row }">{{ row.operatorNameSnapshot || '-' }}</template>
            </el-table-column>
            <el-table-column prop="moduleName" label="模块" width="140" />
            <el-table-column prop="operationType" label="操作类型" width="120">
              <template #default="{ row }">{{ operationTypeText(row.operationType) }}</template>
            </el-table-column>
            <el-table-column prop="operationResult" label="结果" width="100">
              <template #default="{ row }">
                <el-tag :type="row.operationResult === 'SUCCESS' ? 'success' : 'danger'" round>{{ logStatusText(row.operationResult) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="targetType" label="对象类型" width="140">
              <template #default="{ row }">{{ targetTypeText(row.targetType) }}</template>
            </el-table-column>
            <el-table-column prop="operationContent" label="操作内容" min-width="260" show-overflow-tooltip>
              <template #default="{ row }">{{ row.operationContent || '-' }}</template>
            </el-table-column>
            <el-table-column prop="requestIp" label="请求 IP" width="140">
              <template #default="{ row }">{{ row.requestIp || '-' }}</template>
            </el-table-column>
            <el-table-column prop="createdAt" label="操作时间" width="180">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
          </el-table>

          <div class="table-footer">
            <el-pagination
              v-model:current-page="operationQuery.pageNo"
              v-model:page-size="operationQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :total="operationTotal"
              :page-sizes="[10, 20, 50, 100]"
              @change="loadOperationLogs"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { RefreshLeft, Search } from '@element-plus/icons-vue'
import {
  getLoginLogs,
  getOperationLogs,
  getSecurityLoginLogs,
  type LogStatus,
  type LoginLogItem,
  type OperationLogItem
} from '@/api/modules/system'

const props = withDefaults(defineProps<{ embedded?: boolean; loginOnly?: boolean; loginSource?: 'audit' | 'security' }>(), {
  embedded: false,
  loginOnly: false,
  loginSource: 'audit'
})

const activeTab = ref<'login' | 'operation'>('login')
const loginLoading = ref(false)
const operationLoading = ref(false)
const loginRecords = ref<LoginLogItem[]>([])
const operationRecords = ref<OperationLogItem[]>([])
const loginTotal = ref(0)
const operationTotal = ref(0)
const loginTimeRange = ref<[string, string] | null>(null)
const operationTimeRange = ref<[string, string] | null>(null)

const loginQuery = reactive({
  pageNo: 1,
  pageSize: 10,
  username: '',
  loginStatus: '' as LogStatus | ''
})

const operationQuery = reactive({
  pageNo: 1,
  pageSize: 10,
  operatorName: '',
  moduleName: '',
  operationType: '',
  operationResult: '' as LogStatus | ''
})

async function loadLoginLogs() {
  loginLoading.value = true
  try {
    const query = {
      pageNo: loginQuery.pageNo,
      pageSize: loginQuery.pageSize,
      username: loginQuery.username || undefined,
      loginStatus: loginQuery.loginStatus || undefined,
      startTime: loginTimeRange.value?.[0],
      endTime: loginTimeRange.value?.[1]
    }
    const page = props.loginSource === 'security' ? await getSecurityLoginLogs(query) : await getLoginLogs(query)
    loginRecords.value = page.records
    loginTotal.value = page.total
  } finally {
    loginLoading.value = false
  }
}

async function loadOperationLogs() {
  operationLoading.value = true
  try {
    const page = await getOperationLogs({
      pageNo: operationQuery.pageNo,
      pageSize: operationQuery.pageSize,
      operatorName: operationQuery.operatorName || undefined,
      moduleName: operationQuery.moduleName || undefined,
      operationType: operationQuery.operationType || undefined,
      operationResult: operationQuery.operationResult || undefined,
      startTime: operationTimeRange.value?.[0],
      endTime: operationTimeRange.value?.[1]
    })
    operationRecords.value = page.records
    operationTotal.value = page.total
  } finally {
    operationLoading.value = false
  }
}

function handleTabChange(name: string | number) {
  if (name === 'operation' && operationRecords.value.length === 0) {
    loadOperationLogs()
  }
}

function searchLoginLogs() {
  loginQuery.pageNo = 1
  loadLoginLogs()
}

function resetLoginLogs() {
  loginQuery.username = ''
  loginQuery.loginStatus = ''
  loginQuery.pageNo = 1
  loginTimeRange.value = null
  loadLoginLogs()
}

function searchOperationLogs() {
  operationQuery.pageNo = 1
  loadOperationLogs()
}

function resetOperationLogs() {
  operationQuery.operatorName = ''
  operationQuery.moduleName = ''
  operationQuery.operationType = ''
  operationQuery.operationResult = ''
  operationQuery.pageNo = 1
  operationTimeRange.value = null
  loadOperationLogs()
}

function formatTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

function logStatusText(status: LogStatus) {
  return status === 'SUCCESS' ? '成功' : '失败'
}

function operationTypeText(type?: string) {
  const map: Record<string, string> = {
    CREATE: '新增',
    UPDATE: '编辑',
    DELETE: '删除',
    STATUS: '状态变更',
    RESET_PASSWORD: '重置密码',
    ASSIGN_PERMISSION: '分配权限'
  }
  return type ? map[type] || type : '-'
}

function targetTypeText(type?: string) {
  const map: Record<string, string> = {
    BUSINESS_SYSTEM: '业务系统',
    DB_RESOURCE: '分布式数据库',
    SYS_USER: '系统用户',
    SYS_ROLE: '系统角色',
    SYS_MENU: '菜单权限'
  }
  return type ? map[type] || type : '-'
}

onMounted(loadLoginLogs)
</script>

<style scoped>
.audit-page :deep(.el-card) {
  border-radius: 8px;
}

.audit-filter {
  margin-bottom: 16px;
}

.audit-filter :deep(.el-form-item) {
  margin-bottom: 12px;
}

.audit-card {
  min-height: 560px;
}
</style>
