import axios from 'axios'
import { getToken, removeToken } from '@/utils/cookie'
import store from '@/store'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 可以在这里添加请求头等配置
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    if (response.config.responseType === 'text') {
      return response.data
    }
    // 二进制数据则直接返回
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }
    const res = response.data
    // 对特殊接口的处理 - 这些接口不需要标准的错误处理
    if (response.config.url?.includes('/wechat/checkQrCodeStatus') ||
      response.config.url?.includes('/wechat/qrCode') ||
      response.config.url?.includes('/gitee/login') ||
      response.config.url?.includes('/alipay/pay')) {
      return res
    }

    if (res.code === 200) {
      return res
    } else if (res.code === 404) {
      return Promise.reject(new Error('请求路径不存在'))
    } else if (res.code === 401) {
      removeToken()
      store.commit('SET_USER_INFO', null)
      router.push('/login')
      return Promise.reject(new Error('当前登录已过期，请重新登录'))
    } else {
      console.log('响应数据:', res) // 添加日志
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    console.error('请求错误:', error) // 添加错误日志
    return Promise.reject(error)
  }
)

export default service