import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/modules/user'

let isRelogin = { show: false }; // 是否显示弹框

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 5000,
  headers: { "Content-Type": "application/json;charset=utf-8" },
})

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    console.log('请求拦截器 - URL:', config.url, 'Token:', token);
    if (token) {
      config.headers['Authorization'] = token
      console.log('请求拦截器 - Token已添加到Authorization header');
    } else {
      console.log('请求拦截器 - 警告：没有token可添加');
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    // 如果是文本响应（用于处理base64数据），直接返回数据
    if (response.config.responseType === 'text') {
      return response.data
    }

    // 二进制数据则直接返回
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }

    // 处理二维码状态检查的特殊响应
    const res = response.data
    if (response.config.url?.includes('/wechat/checkQrCodeStatus')) {
      // 对于二维码状态检查，直接返回响应，不进行错误处理
      return { data: res }
    }

    if (res.code !== 200) {
      const currentPath = window.location.pathname;
      const isLoginPage = currentPath === '/login';

      // 对于401错误的特殊处理
      if (res.code === 401) {
        // 如果在登录页面，不显示任何弹窗，只记录错误
        if (isLoginPage) {
          console.log('登录页面401错误，静默处理:', res);
          return Promise.reject(new Error(res.message || '请求错误'))
        }

        // 非登录页面的401错误，显示失效提示
        const hasToken = localStorage.getItem('token') || sessionStorage.getItem('token');
        if (hasToken && !isRelogin.show) {
          isRelogin.show = true;
          ElMessageBox.confirm("当前页面已失效，请重新登录", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          })
            .then(() => {
              const userStore = useUserStore()
              userStore.logout()
            })
            .finally(() => {
              isRelogin.show = false;
            })
        }
      } else {
        // 非401错误，根据页面情况决定是否显示错误消息
        if (!isLoginPage) {
          ElMessage.error(res.message || '请求错误')
        } else {
          console.log('登录页面错误，静默处理:', res);
        }
      }

      return Promise.reject(new Error(res.message || '请求错误'))
    }

    return res
  },
  (error) => {
    console.log('请求错误:', error);

    const currentPath = window.location.pathname;
    const isLoginPage = currentPath === '/login';

    if (error.response?.status === 401) {
      // 登录页面的401错误静默处理
      if (isLoginPage) {
        console.log('登录页面网络401错误，静默处理:', error);
        return Promise.reject(error);
      }

      // 非登录页面的401错误
      const hasToken = localStorage.getItem('token') || sessionStorage.getItem('token');
      if (hasToken && !isRelogin.show) {
        isRelogin.show = true;
        ElMessageBox.confirm("当前页面已失效，请重新登录", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        })
          .then(() => {
            const userStore = useUserStore()
            userStore.logout()
          })
          .finally(() => {
            isRelogin.show = false;
          });
      }
    } else if (error.response?.status === 500) {
      if (!isLoginPage) {
        ElMessage.error('后端接口连接异常')
      } else {
        console.log('登录页面500错误，静默处理:', error);
      }
    } else if (error.response?.status !== 401) {
      // 非401错误根据页面决定是否显示
      if (!isLoginPage) {
        ElMessage.error('请求错误')
      } else {
        console.log('登录页面网络错误，静默处理:', error);
      }
    }
    return Promise.reject(error)
  }
)

export default service 