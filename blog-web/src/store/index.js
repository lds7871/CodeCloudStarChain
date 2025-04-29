import Vue from 'vue'
import Vuex from 'vuex'
import { loginApi, logoutApi, getUserInfoApi, getwxUserInfoApi, giteeLoginApi, checkQrCodeStatus } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/cookie'

Vue.use(Vuex)
export default new Vuex.Store({
  state: {
    userInfo: sessionStorage.getItem("user") ? JSON.parse(sessionStorage.getItem("user")) : null,
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
    isUnread: false
  },
  mutations: {
    setSiteInfo(state, info) {
      state.webSiteInfo = info
    },
    SET_TOKEN(state, token) {
      state.token = token
      setToken(token)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      sessionStorage.setItem("user", JSON.stringify(userInfo))
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
        const userInfo = sessionStorage.getItem("user")
        if (userInfo) {
          const response = await checkQrCodeStatus()
          commit('SET_USER_INFO', response.data)
        } else if (getToken()) {
          const res = await getUserInfoApi()
          commit('SET_USER_INFO', res.data)
        }
      } catch (error) {
        console.error('获取用户信息失败：', error)
      }
    },

    async wxlogin({ commit }) {
      try {
        const userInfo = sessionStorage.getItem("user")
        let data
        if (userInfo) {
          const response = await checkQrCodeStatus()
          data = response.data
        } else {
          const res = await getUserInfoApi()
          data = res.data
        }
        alert("我进来了")
        if (data.token) {
          commit('SET_TOKEN', data.token)
          alert("我进来了111")
          commit('SET_USER_INFO', data)
          return Promise.resolve(data)
        }
        return Promise.reject(new Error('未获取到token'))
      } catch (error) {
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
    async giteeLogin({ commit }, code) {
      try {
        const res = await giteeLoginApi(code);
        console.log('Gitee登录响应:', res.data); // 添加日志
        alert("我进来了")
        if (res.data && res.data.token) {
          // 先设置 token
          commit('SET_TOKEN', res.data.token);
          // 再设置用户信息
          commit('SET_USER_INFO', res.data);

          // 存储 Gitee 相关信息到 localStorage
          localStorage.setItem("userInfo", "gitee");
          localStorage.setItem("giteeId", res.data.id);
          localStorage.setItem("avatar", res.data.avatarUrl);
          localStorage.setItem("nickname", res.data.name);

          // 不要立即获取用户信息，等token设置完成
          return Promise.resolve(res.data);
        }
                return Promise.reject(new Error('登录失败：未获取到有效的登录信息'));
      } catch (error) {
        console.error('Gitee登录错误:', error); // 添加错误日志
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
        await logoutApi();
      } catch (error) {
        console.error('登出错误:', error);
      } finally {
        removeToken();
        commit('SET_USER_INFO', null);
        // 清理localStorage
        localStorage.removeItem("userInfo");
        localStorage.removeItem("giteeId");
        localStorage.removeItem("avatar");
        localStorage.removeItem("nickname");
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