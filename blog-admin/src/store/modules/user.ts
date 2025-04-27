import { ref } from "vue";
import { defineStore } from 'pinia';
import { loginApi, getUserInfoApi, logoutApi, checkQrCodeStatus, getwxUserInfoApi } from "@/api/system/auth";
import { resetRouter } from "@/router";
import { store } from "@/store";
import { setToken, removeToken } from "@/utils/auth";

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
          const { data } = response;
          setToken(data.token)
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
      logoutApi()
        .then(() => {
          removeToken()
          location.reload();
          resolve();
        })
        .catch((error) => {
          reject(error);
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
      checkQrCodeStatus()
        .then((response) => {
          const { data } = response;
          if (data.token) {
            setToken(data.token);
            resolve(data);  // 返回完整的数据对象
          } else {
            reject(new Error('未获取到token'));
          }
        })
        .catch((error) => {
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
      logoutApi()
        .then(() => {
          removeToken()
          // 清除微信相关的本地存储
          localStorage.removeItem('userInfo')
          localStorage.removeItem('openId')
          localStorage.removeItem('headimgurl')
          localStorage.removeItem('nickname')
          location.reload()
          resolve()
        })
        .catch((error) => {
          reject(error)
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
