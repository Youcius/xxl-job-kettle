import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import i18n from '@/i18n'

const BASE_URL = '/xxl-job-admin'

function getMessage(key) {
  return i18n.global.t(key)
}

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
      const message = res.msg || getMessage('common.requestFailed')
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return response
  },
  error => {
    if (error.response && error.response.status === 302) {
      router.push('/login')
    }
    ElMessage.error(error.message || getMessage('common.networkError'))
    return Promise.reject(error)
  }
)

export default service
