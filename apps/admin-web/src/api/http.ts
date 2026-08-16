import axios, { type AxiosError, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  traceId?: string
  timestamp?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNo: number
  pageSize: number
  pages: number
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

http.interceptors.response.use(
  <T>(response: AxiosResponse<ApiResponse<T>>) => {
    if (response.config.responseType === 'blob') {
      return response as unknown as T
    }
    const body = response.data
    if (body.code !== 200) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(body)
    }
    return body.data as T
  },
  (error: AxiosError<ApiResponse<unknown>>) => {
    const message = error.response?.data?.message || error.message || '网络异常'
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      ElMessage.error(message || '登录已过期，请重新登录')
      return Promise.reject(error)
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default http
