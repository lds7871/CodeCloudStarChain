import router from '@/router'
import { usePermissionStore } from '@/store/modules/permission'
import { useUserStore, useSettingsStore } from '@/store'
import { wxuseUserStore } from '@/store/modules/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
NProgress.configure({ showSpinner: false })

const whiteList = ['/login'] // 路由白名单

export function setupPermission() {
  router.beforeEach(async (to, from, next) => {
    const dynamicTitle = useSettingsStore().dynamicTitle
    if (dynamicTitle && to.meta.title) {
      document.title = to.meta.title as string
    }
    NProgress.start();
    const hasToken = getToken();

    if (hasToken) {
      if (to.path === "/login") {
        // 访问登录页面时，暂时允许直接进入，让登录页面自己处理清理逻辑
        console.log('访问登录页面，允许进入');
        next();
      } else {
        const userStore = useUserStore();
        const wxUserStore = wxuseUserStore();
        const permissionStore = usePermissionStore();

        // 检查是否是微信登录
        const userInfo = localStorage.getItem('userInfo');
        const isWxLogin = userInfo === 'weixin';

        // 判断是否已经获取过用户信息
        const hasUserInfo = isWxLogin
          ? (wxUserStore.wxuser.nickname || localStorage.getItem('nickname'))
          : userStore.user.nickname;

        if (!hasUserInfo) {
          try {
            // 根据登录类型获取用户信息
            if (isWxLogin) {
              // 微信登录：直接从localStorage获取用户信息，不需要调用API
              const nickname = localStorage.getItem('nickname');
              const headimgurl = localStorage.getItem('headimgurl');
              const openId = localStorage.getItem('openId');

              if (nickname && openId) {
                // 设置微信用户信息到store
                Object.assign(wxUserStore.wxuser, {
                  nickname: nickname,
                  headimgurl: headimgurl,
                  openId: openId
                });
                console.log('微信用户信息已从localStorage恢复:', wxUserStore.wxuser);
              } else {
                throw new Error('微信用户信息不完整');
              }
            } else {
              // 普通登录：调用API获取用户信息
              await userStore.getUserInfo();
            }

            // 生成可访问路由
            const accessRoutes = await permissionStore.generateRoutes();
            // 添加路由前检查和打印日志
            if (Array.isArray(accessRoutes) && accessRoutes.length > 0) {
              accessRoutes.forEach((route: any) => {
                if (route && typeof route === 'object') {
                  if (route.meta?.isExternal) {
                    return;
                  }
                  router.addRoute(route);
                } else {
                  console.error('Invalid route object:', route);
                }
              });
              // 使用 replace 进行重定向，确保路由添加完成
              next({ ...to, replace: true });
            } else {
              console.error('No valid routes generated');
              next();
            }
          } catch (error) {
            console.error('Permission error:', error);
            // 根据登录类型重置token
            if (isWxLogin) {
              await wxUserStore.resetToken();
            } else {
              await userStore.resetToken();
            }
            next(`/login`);
            NProgress.done();
          }
        } else {
          // 已经有用户信息，直接放行
          next();
        }
      }
    } else {
      // 未登录可以访问白名单页面
      if (whiteList.indexOf(to.path) !== -1) {
        next();
      } else {
        next(`/login`);
        NProgress.done();
      }
    }
  });

  router.afterEach(() => {
    NProgress.done();
    setTimeout(() => {
    }, 300)
  });
}

