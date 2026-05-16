import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearToken, getToken } from './auth'

const service = axios.create({
    baseURL: '/api',
    timeout: 10000
})

service.interceptors.request.use((config) => {
    const token = getToken()
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

service.interceptors.response.use(
    (response) => {
        if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
            return response.data
        }
        const payload = response.data
        if (payload.code === 401) {
            clearToken()
            ElMessage.error(payload.message || '登录已失效，请重新登录')
            if (window.location.pathname !== '/login') {
                window.location.href = '/login'
            }
            return Promise.reject(new Error(payload.message || 'Unauthorized'))
        }
        if (payload.code !== 200) {
            ElMessage.error(payload.message || '请求失败')
            return Promise.reject(new Error(payload.message || 'Request failed'))
        }
        return payload
    },
    (error) => {
        const message = error.response?.data?.message || error.message || '网络异常'
        if (error.response?.status === 401) {
            clearToken()
            ElMessage.error(message || '登录已失效，请重新登录')
            if (window.location.pathname !== '/login') {
                window.location.href = '/login'
            }
            return Promise.reject(error)
        }
        if (error.response?.status === 403) {
            ElMessage.error(message || '无权访问当前资源')
            return Promise.reject(error)
        }
        ElMessage.error(message)
        return Promise.reject(error)
    }
)

export default service
