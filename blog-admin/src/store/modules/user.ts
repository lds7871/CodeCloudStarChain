import { ref } from "vue";
import { defineStore } from 'pinia';
import { loginApi, getUserInfoApi, logoutApi, checkQrCodeStatus, getwxUserInfoApi } from "@/api/system/auth";
import { resetRouter } from "@/router";
import { store } from "@/store";
import { setToken, removeToken, getToken, clearAllCookies } from "@/utils/auth";
import Cookies from 'js-cookie';
import { ElMessage } from "element-plus";

interface UserState {
  roles: string[];
  perms: string[];
  intro: any;
  avatar: any;
  nickname: any;
  permissions: string[];
  headimgurl: any;
}
interface wxUserState {
  roles: string[];
  perms: string[];
  intro: any;
  nickname: any;
  permissions: string[];
  headimgurl: any;
}




export const useUserStore = defineStore("user", () => {
  const user = ref({
    roles: [],
    intro: null,
    avatar: null,
    nickname: null,
    permissions: [],
  });




  /**
   * 账号密码登录
   */
  function login(loginData: any) {
    return new Promise<void>((resolve, reject) => {
      loginApi(loginData)
        .then((response) => {
          console.log(response);

          const { data } = response;
          setToken(data.token);

          // 同时设置Authorization和blog_token cookie（与微信登录保持一致）
          Cookies.set('Authorization', data.token, {
            expires: 1, // 1天过期
            path: '/',
            sameSite: 'lax'
          });

          Cookies.set('blog_token', data.token, {
            expires: 1, // 1天过期
            path: '/',
            sameSite: 'lax'
          });

          console.log('账号密码登录 - 所有token cookies已设置:', {
            'Neat-Admin-Token': data.token,
            'Authorization': data.token,
            'blog_token': data.token
          });

          localStorage.setItem('userId', data.id);
          localStorage.setItem('userInfo', 'email');
          resolve();
        })
        .catch((error) => {
          reject(error);
        });
    });
  }



  /**
   * 获取用户信息
   */
  function getUserInfo() {
    return new Promise<any>((resolve, reject) => {
      getUserInfoApi()
        .then(({ data }) => {
          if (!data) {
            reject("Verification failed, please Login again.");
            return;
          }
          Object.assign(user.value, { ...data });
          resolve(data);
        })
        .catch((error) => {
          reject(error);
        });
    });
  }
  /**
* 退出登录
*/
  function logout() {
    return new Promise<void>((resolve, reject) => {
      const userInfo = localStorage.getItem('userInfo');

      // 强制清理所有相关状态
      const clearAllStates = () => {
        // 清空所有cookies
        clearAllCookies()
        // 清空localStorage
        localStorage.removeItem('userInfo')
        localStorage.removeItem('userId')
        localStorage.removeItem('openId')
        localStorage.removeItem('headimgurl')
        localStorage.removeItem('nickname')
        // 重置用户状态
        Object.assign(user.value, {
          roles: [],
          intro: null,
          avatar: null,
          nickname: null,
          permissions: [],
        });
      };

      logoutApi(userInfo)
        .then(() => {
          clearAllStates();
          ElMessage.success('退出成功');
          setTimeout(() => {
            location.reload();
          }, 1000);
          resolve();
        })
        .catch((error) => {
          // 即使退出登录API失败，也要清理本地状态
          clearAllStates();
          location.reload();
          resolve();
        });
    });
  }

  /**
   * 重置 Token
   */
  function resetToken() {
    return new Promise<void>((resolve) => {
      removeToken()
      resetRouter();
      resolve();
    });
  }

  return {
    user,
    login,
    getUserInfo,
    logout,
    resetToken,
  };
});
export const wxuseUserStore = defineStore("wxuser", () => {
  const wxuser = ref({
    roles: [],
    intro: null,
    headimgurl: null,
    nickname: null,
    permissions: [],
  });



  /**
 * 微信扫码登录
 */
  function wxlogin() {
    return new Promise((resolve, reject) => {
      // 确保开始时是干净状态
      console.log('开始微信登录流程');

      checkQrCodeStatus()
        .then((response) => {
          console.log('wxlogin中的checkQrCodeStatus response:', response);
          // 由于 request.ts 中的特殊处理，实际数据在 response.data 中
          const userData = response.data;
          console.log('wxlogin中的userData:', userData);

          // 检查数据结构：userData.data.token
          if (userData && userData.data && userData.data.token) {
            console.log('设置token:', userData.data.token);
            setToken(userData.data.token);

            // 同时设置Authorization cookie（与账号密码登录保持一致）
            Cookies.set('Authorization', userData.data.token, {
              expires: 1, // 1天过期
              path: '/',
              sameSite: 'lax'
            });

            // 设置blog_token以保持与账号密码登录的一致性
            Cookies.set('blog_token', userData.data.token, {
              expires: 1, // 1天过期
              path: '/',
              sameSite: 'lax'
            });

            console.log('所有token cookies已设置:', {
              'Neat-Admin-Token': userData.data.token,
              'Authorization': userData.data.token,
              'blog_token': userData.data.token
            });

            // 验证token是否设置成功
            setTimeout(() => {
              const currentToken = getToken();
              console.log('token设置验证:', currentToken);
              if (currentToken) {
                // 保存权限信息到wxuser store和localStorage
                if (userData.data.permissions && userData.data.roles) {
                  console.log('保存权限信息到wxuser store:', {
                    permissions: userData.data.permissions,
                    roles: userData.data.roles
                  });

                  // 更新微信用户store的权限信息
                  Object.assign(wxuser.value, {
                    permissions: userData.data.permissions || [],
                    roles: userData.data.roles || [],
                    nickname: userData.data.nickname,
                    headimgurl: userData.data.headimgurl
                  });

                  // 同时保存到localStorage，确保刷新后能恢复权限
                  localStorage.setItem('wxuser_permissions', JSON.stringify(userData.data.permissions || []));
                  localStorage.setItem('wxuser_roles', JSON.stringify(userData.data.roles || []));
                }

                resolve(userData.data);  // 返回实际的用户数据对象
              } else {
                reject(new Error('Token设置失败'));
              }
            }, 50);
          } else {
            reject(new Error('未获取到token，userData为：' + JSON.stringify(userData)));
          }
        })
        .catch((error) => {
          console.error('wxlogin错误:', error);
          reject(error);
        });
    });
  }

  /**
   * 获取微信用户信息
   */
  function getwxUserInfo(openid: string) {
    return new Promise<any>((resolve, reject) => {
      getwxUserInfoApi(openid)
        .then(({ data }) => {
          if (!data) {
            reject("获取用户信息失败");
            return;
          }
          // 更新用户信息
          Object.assign(wxuser.value, { ...data });
          resolve(data);
        })
        .catch((error) => {
          reject(error);
        });
    });
  }


  /**
   * 退出登录
   */
  function logout() {
    return new Promise<void>((resolve, reject) => {

      // 强制清理所有相关状态
      const clearAllStates = () => {
        // 清空所有cookies
        clearAllCookies()
        // 清空localStorage
        localStorage.removeItem('userInfo')
        localStorage.removeItem('openId')
        localStorage.removeItem('headimgurl')
        localStorage.removeItem('nickname')
        localStorage.removeItem('userId')
        localStorage.removeItem('wxuser_permissions')
        localStorage.removeItem('wxuser_roles')
        // 重置微信用户状态
        Object.assign(wxuser.value, {
          roles: [],
          intro: null,
          headimgurl: null,
          nickname: null,
          permissions: [],
        });
      };

      logoutApi()
        .then(() => {
          clearAllStates();
          ElMessage.success('退出成功');
          setTimeout(() => {
            location.reload();
          }, 1000);
          resolve()
        })
        .catch((error) => {
          // 即使退出登录API失败，也要清理本地状态
          clearAllStates();
          location.reload()
          resolve()
        })
    })
  }

  /**
   * 重置 Token
   */
  function resetToken() {
    return new Promise<void>((resolve) => {
      removeToken()
      resetRouter();
      resolve();
    });
  }

  return {
    wxuser,
    wxlogin,
    logout,
    resetToken,
    getwxUserInfo
  };
});

export function useUserStoreHook() {
  return useUserStore(store);
}
