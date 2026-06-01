import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const BASE_URL = '/xxl-job-admin'

const service = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  withCredentials: true
})

service.interceptors.request.use(
  config => {
    config.headers['X-Requested-With'] = 'XMLHttpRequest'
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    // 下载文件等直接返回
    if (response.config.responseType === 'blob') return response

    const res = response.data
    // 部分旧接口直接返回 Map（如 pageList），不做 code 校验
    if (res.code === undefined) return response

    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return response
  },
  error => {
    if (error.response && error.response.status === 302) {
      router.push('/login')
    }
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default service
