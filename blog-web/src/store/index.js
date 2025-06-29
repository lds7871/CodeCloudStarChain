import Vue from 'vue'
import Vuex from 'vuex'
import { loginApi, logoutApi, getUserInfoApi, getwxUserInfoApi, giteeLoginApi, checkQrCodeStatus } from '@/api/auth'
import { getWebConfigApi } from '@/api/site'
import { getToken, setToken, removeToken, removeAuthorization } from '@/utils/cookie'

Vue.use(Vuex)
export default new Vuex.Store({
  state: {
    userInfo: (() => {
      try {
        const userStr = localStorage.getItem("user")
        return userStr && userStr !== "undefined" ? JSON.parse(userStr) : null
      } catch (error) {
        console.error('解析用户信息失败:', error)
        localStorage.removeItem("user") // 清除错误数据
        return null
      }
    })(),
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
      if (userInfo != null && userInfo.id) {
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

        // 始终从API获取最新的用户信息，确保数据最新
        if (getToken()) {
          const res = await getUserInfoApi()
          console.log('API响应完整数据:', res);

          // 尝试多种可能的数据结构
          let userData = null;
          if (res.data?.sysUser) {
            userData = res.data.sysUser;
            console.log('使用 sysUser 数据结构:', userData);
          } else if (res.data && typeof res.data === 'object') {
            userData = res.data;
            console.log('使用 data 数据结构:', userData);
          } else if (res.sysUser) {
            userData = res.sysUser;
            console.log('使用根级 sysUser 数据结构:', userData);
          }

          if (userData && (userData.id || userData.userId)) {
            commit('SET_USER_INFO', userData)
            localStorage.setItem("userId", userData.id || userData.userId);
            console.log('用户信息获取成功:', userData);
          } else {
            console.error('用户数据格式不正确:', {
              fullResponse: res,
              dataProperty: res.data,
              sysUserProperty: res.data?.sysUser
            });
            // 如果API响应格式不对，清除token
            removeToken();
            localStorage.clear();
          }
        } else if (userInfo && userInfo !== "undefined") {
          try {
            // 只有在没有token时才使用本地存储
            commit('SET_USER_INFO', JSON.parse(userInfo))
          } catch (error) {
            console.error('解析本地用户信息失败:', error)
            localStorage.removeItem("user") // 清除错误数据
          }
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

        if (data && data.id) {
          localStorage.setItem("userId", data.id);
        }

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
        console.log('登录API响应:', res);
        if (res.data) {
          commit('SET_TOKEN', res.data.token)
          // 登录后重新获取完整的用户信息
          if (res.data.token) {
            try {
              const userInfoRes = await getUserInfoApi()
              console.log('登录后获取用户信息完整响应:', userInfoRes);

              // 尝试多种可能的数据结构
              let userData = null;
              if (userInfoRes.data?.sysUser) {
                userData = userInfoRes.data.sysUser;
                console.log('使用 sysUser 数据结构');
              } else if (userInfoRes.data && typeof userInfoRes.data === 'object') {
                userData = userInfoRes.data;
                console.log('使用 data 数据结构');
              } else if (userInfoRes.sysUser) {
                userData = userInfoRes.sysUser;
                console.log('使用根级 sysUser 数据结构');
              }

              if (userData && (userData.id || userData.userId)) {
                commit('SET_USER_INFO', userData)
                console.log('用户信息设置成功:', userData);
              } else {
                console.error('登录后获取用户信息失败，数据结构不正确:', {
                  fullResponse: userInfoRes,
                  dataProperty: userInfoRes.data,
                  sysUserProperty: userInfoRes.data?.sysUser
                });
                // 如果获取用户信息失败，至少设置基本的登录信息
                commit('SET_USER_INFO', res.data)
              }
            } catch (userInfoError) {
              console.error('获取用户信息API调用失败:', userInfoError);
              // 如果API调用失败，使用登录返回的基本信息
              commit('SET_USER_INFO', res.data)
            }
          } else {
            commit('SET_USER_INFO', res.data)
          }
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
          if (res.data.id) {
            localStorage.setItem("giteeId", res.data.id);
            localStorage.setItem("userId", res.data.id);
          }
          if (res.data.avatar) {
            localStorage.setItem("avatar", res.data.avatar);
          }
          if (res.data.nickname) {
            localStorage.setItem("nickname", res.data.nickname);
          }
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