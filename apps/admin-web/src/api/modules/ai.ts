import http, { type PageResult } from '@/api/http'

export type AiTaskType = 'FAULT_PREDICTION' | 'DEFECT_DETECTION' | 'PERFORMANCE_OPTIMIZATION'
export type KnowledgeType = 'FAULT' | 'DEFECT' | 'OPTIMIZATION'
export type TargetType = 'BUSINESS_SYSTEM' | 'DB_RESOURCE'
export type EnableStatus = 'ENABLED' | 'DISABLED'
export type ProcessStatus = 'PENDING' | 'CONFIRMED' | 'FALSE_POSITIVE' | 'CLOSED'
export type FaultAlertStatus = ProcessStatus
export type Level = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export interface AiQuery {
  pageNo: number
  pageSize: number
  keyword?: string
  modelType?: string
  modelId?: string
  creatorName?: string
  createdDate?: string
  taskType?: AiTaskType
  knowledgeType?: KnowledgeType
  targetType?: TargetType
  status?: string
  processStatus?: ProcessStatus | ''
  level?: Level | ''
  defectCode?: string
  taskName?: string
}

export interface AiModelItem {
  id: string
  modelCode: string
  modelName: string
  modelType: string
  modelVersion?: string
  provider?: string
  description?: string
  status: EnableStatus
}

export interface KnowledgeItem {
  id: string
  knowledgeCode: string
  knowledgeName: string
  knowledgeType: KnowledgeType
  knowledgeContent: string
  attachmentNames?: string
  sourceType?: string
  status: EnableStatus
  version?: number
  createdAt?: string
  updatedAt?: string
}

export interface KnowledgeAttachmentItem {
  id: string
  originalFilename: string
  contentType?: string
  fileSize: number
  previewable: boolean
}

export interface FaultKnowledgeItem extends Omit<KnowledgeItem, 'attachmentNames' | 'status' | 'version'> {
  creatorName?: string
  attachments: KnowledgeAttachmentItem[]
}

export interface FaultKnowledgeQuery {
  pageNo: number
  pageSize: number
  knowledgeId?: string
  knowledgeName?: string
  creatorName?: string
  createdDate?: string
  knowledgeType: 'FAULT'
}

export interface DefectKnowledgeQuery extends Omit<FaultKnowledgeQuery, 'knowledgeType'> {
  knowledgeType: 'DEFECT'
}

export interface OptimizationKnowledgeQuery extends Omit<FaultKnowledgeQuery, 'knowledgeType'> {
  knowledgeType: 'OPTIMIZATION'
}

export interface AiTaskItem {
  id: string
  taskCode: string
  taskName: string
  taskType: AiTaskType
  modelId?: string
  modelNameSnapshot?: string
  targetType: TargetType
  targetId: string
  targetNameSnapshot: string
  metricCodes?: string
  paramJson?: string
  scheduleRule?: string
  status: EnableStatus
  lastExecTime?: string
  nextExecTime?: string
  executionCount?: number
  resultCount?: number
  creatorName?: string
  createdAt?: string
  updatedAt?: string
}

export interface TaskExecRecordItem {
  id: string
  taskId: string
  taskType: AiTaskType
  taskNameSnapshot?: string
  executionStatus: string
  triggerType: string
  startTime?: string
  endTime?: string
  resultCount?: number
  errorMessage?: string
}

export interface FaultTaskQuery {
  pageNo: number
  pageSize: number
  keyword?: string
  modelId?: string
  creatorName?: string
  createdDate?: string
}

export interface FaultTaskSchedule {
  time: string
  repeatType: 'daily' | 'weekly'
  weekDays: string[]
  predictionWindowHours: number
}

export interface FaultTaskSavePayload {
  taskName: string
  modelId: string
  targetType: TargetType
  targetId: string
  metricCodes: string[]
  schedules: FaultTaskSchedule[]
}

export interface FaultTaskItem extends Omit<AiTaskItem, 'taskType' | 'metricCodes' | 'scheduleRule'> {
  metricCodes: string
  scheduleRule: string
}

export interface FaultTaskDetail extends FaultTaskItem {
  executionRecords: TaskExecRecordItem[]
}

export interface OptimizationTaskSchedule {
  time: string
  repeatType: 'daily' | 'weekly'
  weekDays: string[]
  detectionRange: string
}

export interface OptimizationTaskSavePayload {
  taskName: string
  modelId: string
  targetType: TargetType
  targetId: string
  schedules: OptimizationTaskSchedule[]
}

export interface FaultReportItem {
  id: string
  reportCode: string
  targetType: TargetType
  targetId: string
  targetNameSnapshot: string
  alertTime?: string
  indicatorCategory?: string
  faultType: string
  riskLevel: Level
  confidence?: number
  forecastDescription?: string
  handleSuggestion?: string
  trendJson?: string
  processStatus: ProcessStatus
}

export interface FaultAlertQuery {
  pageNo: number
  pageSize: number
  keyword?: string
  processStatus?: FaultAlertStatus
  riskLevel?: Level
  targetType?: TargetType
}

export interface FaultAlertItem {
  id: string
  reportCode: string
  targetType: TargetType
  targetId: string
  targetNameSnapshot: string
  alertTime?: string
  indicatorCategory?: string
  faultType: string
  riskLevel: Level
  expectedStartTime?: string
  expectedEndTime?: string
  confidence?: number
  forecastDescription?: string
  handleSuggestion?: string
  trendJson?: string
  processStatus: FaultAlertStatus
  handledBy?: string
  handledAt?: string
}

export interface FaultAlertHistoryItem {
  beforeStatus: FaultAlertStatus
  afterStatus: FaultAlertStatus
  operationRemark?: string
  operatorName?: string
  createdAt?: string
}

export interface FaultAlertDetail extends FaultAlertItem {
  histories: FaultAlertHistoryItem[]
  handledByName?: string
}

export interface DefectReportItem {
  id: string
  defectCode: string
  targetType: TargetType
  targetId: string
  targetNameSnapshot: string
  defectType: string
  severityLevel: Level
  defectProblem: string
  defectData?: string
  defectDescription?: string
  repairSuggestion?: string
  processStatus: ProcessStatus
  taskNameSnapshot?: string
  creatorName?: string
  createdAt?: string
}

export interface OptimizationReportItem {
  id: string
  reportCode: string
  targetType: TargetType
  targetId: string
  targetNameSnapshot: string
  optimizationType: string
  optimizationProblem: string
  sqlText?: string
  optimizationContent?: string
  confidence?: number
  executeResult?: string
  processStatus: ProcessStatus
  taskNameSnapshot?: string
  creatorName?: string
  createdAt?: string
  updatedAt?: string
}

export type AiReportItem = FaultReportItem | DefectReportItem | OptimizationReportItem

export function getAiModels(params: AiQuery) {
  return http.get('/ai/models', { params }) as unknown as Promise<PageResult<AiModelItem>>
}

export function getKnowledge(params: AiQuery) {
  return http.get('/ai/knowledge', { params }) as unknown as Promise<PageResult<KnowledgeItem>>
}

export function getKnowledgeDetail(id: string) {
  return http.get(`/ai/knowledge/${id}`) as unknown as Promise<KnowledgeItem>
}

export function createKnowledge(data: Partial<KnowledgeItem>) {
  return http.post('/ai/knowledge', data) as unknown as Promise<KnowledgeItem>
}

export function updateKnowledge(id: string, data: Partial<KnowledgeItem>) {
  return http.put(`/ai/knowledge/${id}`, data) as unknown as Promise<KnowledgeItem>
}

export function deleteKnowledge(id: string) {
  return http.delete(`/ai/knowledge/${id}`) as unknown as Promise<void>
}

export function getFaultKnowledge(params: FaultKnowledgeQuery) {
  return http.get('/ai/knowledge', { params }) as unknown as Promise<PageResult<FaultKnowledgeItem>>
}

export function getFaultKnowledgeDetail(id: string) {
  return http.get(`/ai/knowledge/${id}`) as unknown as Promise<FaultKnowledgeItem>
}

export function createFaultKnowledge(data: FormData) {
  return http.post('/ai/knowledge', data) as unknown as Promise<FaultKnowledgeItem>
}

export function updateFaultKnowledge(id: string, data: FormData) {
  return http.put(`/ai/knowledge/${id}`, data) as unknown as Promise<FaultKnowledgeItem>
}

export function getDefectKnowledge(params: DefectKnowledgeQuery) {
  return http.get('/ai/knowledge', { params }) as unknown as Promise<PageResult<FaultKnowledgeItem>>
}

export function getDefectKnowledgeDetail(id: string) {
  return http.get(`/ai/knowledge/${id}`, { params: { knowledgeType: 'DEFECT' } }) as unknown as Promise<FaultKnowledgeItem>
}

export function createDefectKnowledge(data: FormData) {
  return http.post('/ai/knowledge', data) as unknown as Promise<FaultKnowledgeItem>
}

export function updateDefectKnowledge(id: string, data: FormData) {
  return http.put(`/ai/knowledge/${id}`, data) as unknown as Promise<FaultKnowledgeItem>
}

export function getOptimizationKnowledge(params: OptimizationKnowledgeQuery) {
  return http.get('/ai/knowledge', { params }) as unknown as Promise<PageResult<FaultKnowledgeItem>>
}

export function getOptimizationKnowledgeDetail(id: string) {
  return http.get(`/ai/knowledge/${id}`) as unknown as Promise<FaultKnowledgeItem>
}

export function createOptimizationKnowledge(data: FormData) {
  return http.post('/ai/knowledge', data) as unknown as Promise<FaultKnowledgeItem>
}

export function updateOptimizationKnowledge(id: string, data: FormData) {
  return http.put(`/ai/knowledge/${id}`, data) as unknown as Promise<FaultKnowledgeItem>
}

export function fetchKnowledgeAttachment(attachmentId: string, preview = false) {
  const action = preview ? 'preview' : 'download'
  return http.get(`/ai/knowledge/attachments/${attachmentId}/${action}`, { responseType: 'blob' }) as unknown as Promise<{ data: Blob; headers: Record<string, string> }>
}

export function getAiTasks(params: AiQuery) {
  return http.get('/ai/tasks', { params }) as unknown as Promise<PageResult<AiTaskItem>>
}

export function getFaultTasks(params: FaultTaskQuery) {
  return http.get('/ai/fault-prediction/tasks', { params }) as unknown as Promise<PageResult<FaultTaskItem>>
}

export function getFaultTaskDetail(id: string) {
  return http.get(`/ai/fault-prediction/tasks/${id}`) as unknown as Promise<FaultTaskDetail>
}

export function createFaultTask(data: FaultTaskSavePayload) {
  return http.post('/ai/fault-prediction/tasks', data) as unknown as Promise<FaultTaskDetail>
}

export function updateFaultTask(id: string, data: FaultTaskSavePayload) {
  return http.put(`/ai/fault-prediction/tasks/${id}`, data) as unknown as Promise<FaultTaskDetail>
}

export function executeFaultTask(id: string) {
  return http.post(`/ai/fault-prediction/tasks/${id}/execute`, { triggerType: 'MANUAL' }) as unknown as Promise<TaskExecRecordItem>
}

export function deleteFaultTask(id: string) {
  return http.delete(`/ai/fault-prediction/tasks/${id}`) as unknown as Promise<void>
}

export function getAiTaskDetail(id: string) {
  return http.get(`/ai/tasks/${id}`) as unknown as Promise<AiTaskItem>
}

export function createAiTask(data: Partial<AiTaskItem>) {
  return http.post('/ai/tasks', data) as unknown as Promise<AiTaskItem>
}

export function updateAiTask(id: string, data: Partial<AiTaskItem>) {
  return http.put(`/ai/tasks/${id}`, data) as unknown as Promise<AiTaskItem>
}

export function executeAiTask(id: string) {
  return http.post(`/ai/tasks/${id}/execute`, { triggerType: 'MANUAL' }) as unknown as Promise<TaskExecRecordItem>
}

export function deleteAiTask(id: string) {
  return http.delete(`/ai/tasks/${id}`) as unknown as Promise<void>
}

export function getTaskExecutions(params: AiQuery) {
  return http.get('/ai/tasks/executions', { params }) as unknown as Promise<PageResult<TaskExecRecordItem>>
}

export function getFaultReports(params: AiQuery) {
  return http.get('/ai/fault-reports', { params }) as unknown as Promise<PageResult<FaultReportItem>>
}

export function getFaultAlerts(params: FaultAlertQuery) {
  return http.get('/ai/fault-prediction/alerts', { params }) as unknown as Promise<PageResult<FaultAlertItem>>
}

export function getFaultAlertDetail(id: string) {
  return http.get(`/ai/fault-prediction/alerts/${id}`) as unknown as Promise<FaultAlertDetail>
}

export function handleFaultAlert(id: string, processStatus: FaultAlertStatus, remark: string) {
  return http.patch(`/ai/fault-prediction/alerts/${id}/handle`, { processStatus, remark }) as unknown as Promise<FaultAlertDetail>
}

export function deleteFaultAlert(id: string) {
  return http.delete(`/ai/fault-prediction/alerts/${id}`) as unknown as Promise<void>
}

export function createFaultReport(data: Partial<FaultReportItem>) {
  return http.post('/ai/fault-reports', data) as unknown as Promise<FaultReportItem>
}

export function updateFaultReport(id: string, data: Partial<FaultReportItem>) {
  return http.put(`/ai/fault-reports/${id}`, data) as unknown as Promise<FaultReportItem>
}

export function handleFaultReport(id: string, processStatus: ProcessStatus, remark?: string) {
  return http.patch(`/ai/fault-reports/${id}/handle`, { processStatus, remark }) as unknown as Promise<void>
}

export function deleteFaultReport(id: string) {
  return http.delete(`/ai/fault-reports/${id}`) as unknown as Promise<void>
}

export function getDefectReports(params: AiQuery) {
  return http.get('/ai/defect-reports', { params }) as unknown as Promise<PageResult<DefectReportItem>>
}

export function getDefectReportDetail(id: string) {
  return http.get(`/ai/defect-reports/${id}`) as unknown as Promise<DefectReportItem>
}

export function exportDefectReports(reportIds: string[]) {
  return http.get('/ai/defect-reports/export', { params: { reportIds }, responseType: 'blob' }) as unknown as Promise<{ data: Blob; headers: Record<string, string> }>
}

export function createDefectReport(data: Partial<DefectReportItem>) {
  return http.post('/ai/defect-reports', data) as unknown as Promise<DefectReportItem>
}

export function updateDefectReport(id: string, data: Partial<DefectReportItem>) {
  return http.put(`/ai/defect-reports/${id}`, data) as unknown as Promise<DefectReportItem>
}

export function handleDefectReport(id: string, processStatus: ProcessStatus, remark?: string) {
  return http.patch(`/ai/defect-reports/${id}/handle`, { processStatus, remark }) as unknown as Promise<void>
}

export function deleteDefectReport(id: string) {
  return http.delete(`/ai/defect-reports/${id}`) as unknown as Promise<void>
}

export function getOptimizationReports(params: AiQuery) {
  return http.get('/ai/optimization-reports', { params }) as unknown as Promise<PageResult<OptimizationReportItem>>
}

export function getOptimizationReportDetail(id: string) {
  return http.get(`/ai/optimization-reports/${id}`) as unknown as Promise<OptimizationReportItem>
}

export function exportOptimizationReports(reportIds: string[]) {
  return http.get('/ai/optimization-reports/export', { params: { reportIds }, responseType: 'blob' }) as unknown as Promise<{ data: Blob; headers: Record<string, string> }>
}

export function createOptimizationReport(data: Partial<OptimizationReportItem>) {
  return http.post('/ai/optimization-reports', data) as unknown as Promise<OptimizationReportItem>
}

export function updateOptimizationReport(id: string, data: Partial<OptimizationReportItem>) {
  return http.put(`/ai/optimization-reports/${id}`, data) as unknown as Promise<OptimizationReportItem>
}

export function handleOptimizationReport(id: string, processStatus: ProcessStatus, remark?: string) {
  return http.patch(`/ai/optimization-reports/${id}/handle`, { processStatus, remark }) as unknown as Promise<void>
}

export function deleteOptimizationReport(id: string) {
  return http.delete(`/ai/optimization-reports/${id}`) as unknown as Promise<void>
}
