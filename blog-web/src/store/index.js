import Vue from 'vue'
import Vuex from 'vuex'
import { loginApi, logoutApi, getUserInfoApi, getwxUserInfoApi, giteeLoginApi, checkQrCodeStatus } from '@/api/auth'
import { getWebConfigApi } from '@/api/site'
import { getToken, setToken, removeToken, removeAuthorization } from '@/utils/cookie'

Vue.use(Vuex)
export default new Vuex.Store({
  state: {
    userInfo: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : null,
    webSiteInfo: {
      showList: []
    },
    token: getToken() || '',
    searchVisible: false,
    mobileMenuVisible: false,
    visitorAccess: 0,
    siteAccess: 0,
    isLoading: false,
    notice: null,
    isUnread: false,
    defaultImage: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Guest' // 默认图片
  },
  mutations: {
    setSiteInfo(state, info) {
      state.webSiteInfo = info
    },
    setDefaultImage(state, image) {
      state.defaultImage = image
    },
    SET_TOKEN(state, token) {
      state.token = token
      setToken(token)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      localStorage.setItem("user", JSON.stringify(userInfo))
      console.log(userInfo);
      if (userInfo != null) {
        localStorage.setItem("userId", userInfo.id)
      }
    },

    SET_SEARCH_VISIBLE(state, visible) {
      state.searchVisible = visible
    },
    SET_MOBILE_MENU_VISIBLE(state, visible) {
      state.mobileMenuVisible = visible
    },
    setMobileMenuVisible(state, value) {
      state.mobileMenuVisible = value
    },
    setVisitorAccess(state, value) {
      state.visitorAccess = value
    },
    setSiteAccess(state, value) {
      state.siteAccess = value
    },
    SET_LOADING(state, status) {
      state.isLoading = status
    },
    SET_NOTICE(state, notice) {
      state.notice = notice
    }
  },
  actions: {

    /**
     * 获取网站配置信息
     */
    async getWebSiteConfig({ commit }) {
      try {
        const res = await getWebConfigApi()
        if (res.data) {
          commit('setSiteInfo', res.data)
          // 设置默认图片为游客头像
          if (res.data.touristAvatar) {
            commit('setDefaultImage', res.data.touristAvatar)
          }
        }
      } catch (error) {
        console.error('获取网站配置失败：', error)
      }
    },

    /**
     * 设置公告信息
     */
    setNotice({ commit }, notice) {
      commit('SET_NOTICE', notice)
    },

    /**
     * 设置站点信息
     */
    setSiteInfo({ commit }, info) {
      commit('setSiteInfo', info)
    },

    /**
     * 获取用户信息
     */
    async getUserInfo({ commit }) {
      try {
        const userInfo = localStorage.getItem("user")
        const loginType = localStorage.getItem("userInfo") // 获取登录类型

        // 如果已有用户信息，直接使用本地存储的信息
        if (userInfo) {
          commit('SET_USER_INFO', JSON.parse(userInfo))
          return
        }

        // 只有在没有用户信息的情况下才去请求
        if (getToken()) {
          const res = await getUserInfoApi()
          commit('SET_USER_INFO', res.data)
          localStorage.setItem("userId", res.data.id);
        }
      } catch (error) {
        console.error('获取用户信息失败：', error)
      }
    },

    async wxlogin({ commit }) {
      try {
        const userInfo = localStorage.getItem("user")
        let data
        if (userInfo) {
          const response = await checkQrCodeStatus()
          console.log('checkQrCodeStatus 响应:', response);
          data = response.data
        } else {
          const res = await getUserInfoApi()
          console.log('getUserInfoApi 响应:', res);
          data = res.data
          console.log("微信数据为：" + data.id);
        }

        console.log('最终data:', data);
        console.log('data.token:', data.token);

        localStorage.setItem("userId", data.id);

        if (data && data.token) {
          commit('SET_TOKEN', data.token)
          commit('SET_USER_INFO', data)
          return Promise.resolve(data)
        }
        return Promise.reject(new Error('未获取到token'))
      } catch (error) {
        console.error('wxlogin 错误:', error);
        return Promise.reject(error)
      }
    },
    /**
     * 登录
     */
    async loginAction({ commit }, loginData) {
      try {
        const res = await loginApi(loginData)
        if (res.data) {
          commit('SET_TOKEN', res.data.token)
          commit('SET_USER_INFO', res.data)
          return Promise.resolve(res)
        }
        return Promise.reject(res)
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async giteeLogin({ commit }, token) {
      commit('SET_TOKEN', token);
      try {
        const res = await giteeLoginApi();
        console.log('Gitee登录响应:', res); // 打印完整响应

        if (res.data) {
          commit('SET_USER_INFO', res.data);
          localStorage.setItem("userInfo", "gitee");
          localStorage.setItem("giteeId", res.data.id);
          localStorage.setItem("avatar", res.data.avatar);
          localStorage.setItem("nickname", res.data.nickname);
          localStorage.setItem("userId", res.data.id);
          // 移除硬编码重定向，让页面组件处理路由跳转
          return Promise.resolve(res.data);
        }
        console.error('登录响应异常:', res); // 添加错误日志
        return Promise.reject(new Error('登录失败：未获取到有效的登录信息'));
      } catch (error) {
        console.error('Gitee登录错误:', error); // 添加详细错误信息
        return Promise.reject(error);
      }
    },
    async getwxUserInfo({ commit }, openid) {
      try {
        const res = await getwxUserInfoApi(openid)
        if (!res.data) {
          return Promise.reject(new Error('获取用户信息失败'))
        }
        commit('SET_USER_INFO', res.data)
        return Promise.resolve(res.data)
      } catch (error) {
        return Promise.reject(error)
      }
    },

    async generateRoutes({ commit }) {
      try {
        // 这里应该调用获取用户权限的 API，然后根据权限生成路由
        const accessRoutes = [] // 这里替换成实际的路由生成逻辑
        return Promise.resolve(accessRoutes)
      } catch (error) {
        return Promise.reject(error)
      }
    },

    /**
     * 退出登录
     */
    async logout({ commit }) {
      try {
        await logoutApi(localStorage.getItem("userInfo"))
      } catch (error) {
        console.error('登出错误:', error);
      } finally {
        removeToken();
        removeAuthorization();
        commit('SET_USER_INFO', null);
        // 清理localStorage
        localStorage.clear();
      }
    },

    showLoading({ commit }) {
      commit('SET_LOADING', true)
    },

    hideLoading({ commit }) {
      commit('SET_LOADING', false)
    }
  }
})