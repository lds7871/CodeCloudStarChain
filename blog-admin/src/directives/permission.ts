import { DirectiveBinding } from 'vue'
import { useUserStore, wxuseUserStore } from '@/store/modules/user'

export default {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const userStore = useUserStore()
    const wxUserStore = wxuseUserStore()

    // 检查是否是微信登录
    const userInfo = localStorage.getItem('userInfo')
    const isWxLogin = userInfo === 'weixin'

    // 根据登录类型获取权限
    const permissions = isWxLogin
      ? (wxUserStore.wxuser.permissions || [])
      : (userStore.user.permissions || [])

    console.log('权限指令检查:', {
      isWxLogin,
      permissions,
      requiredPerms: value
    })

    if (value && value instanceof Array && value.length > 0) {
      const hasPermission = permissions.some(permission => {
        return value.includes(permission)
      })

      console.log('权限检查结果:', hasPermission)

      if (!hasPermission) {
        el.parentNode?.removeChild(el)
      }
    }
  },

  updated(el: HTMLElement, binding: DirectiveBinding) {
    // 当组件更新时也重新检查权限
    const { value } = binding
    const userStore = useUserStore()
    const wxUserStore = wxuseUserStore()

    // 检查是否是微信登录
    const userInfo = localStorage.getItem('userInfo')
    const isWxLogin = userInfo === 'weixin'

    // 根据登录类型获取权限
    const permissions = isWxLogin
      ? (wxUserStore.wxuser.permissions || [])
      : (userStore.user.permissions || [])

    if (value && value instanceof Array && value.length > 0) {
      const hasPermission = permissions.some(permission => {
        return value.includes(permission)
      })

      if (!hasPermission && el.parentNode) {
        el.parentNode.removeChild(el)
      }
    }
  }
} 