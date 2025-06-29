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

        // 检查是否需要重新生成路由
        const needsRouteGeneration = permissionStore.routes.length <= 1; // 只有常量路由时需要重新生成

        // 如果是微信登录且刷新后丢失了权限信息，重新恢复
        if (isWxLogin && (!wxUserStore.wxuser.permissions?.length || !userStore.user.permissions?.length)) {
          const savedPermissions = localStorage.getItem('wxuser_permissions');
          const savedRoles = localStorage.getItem('wxuser_roles');
          const savedNickname = localStorage.getItem('nickname');
          const savedHeadimgurl = localStorage.getItem('headimgurl');

          if (savedPermissions && savedRoles) {
            console.log('从localStorage恢复微信用户权限信息');
            const permissions = JSON.parse(savedPermissions);
            const roles = JSON.parse(savedRoles);

            // 更新微信用户store
            Object.assign(wxUserStore.wxuser, {
              permissions: permissions,
              roles: roles,
              nickname: savedNickname,
              headimgurl: savedHeadimgurl
            });

            // 同时同步到普通用户store（用于权限验证和菜单生成）
            Object.assign(userStore.user, {
              permissions: permissions,
              roles: roles,
              nickname: savedNickname,
              avatar: savedHeadimgurl
            });

            console.log('权限信息已恢复:', {
              wxUserStore: wxUserStore.wxuser,
              userStore: userStore.user
            });
          }
        }

        if (!hasUserInfo || needsRouteGeneration) {
          try {
            // 如果没有用户信息，根据登录类型获取用户信息
            if (!hasUserInfo) {
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
            }

            // 生成可访问路由
            console.log('开始生成路由，当前权限信息:', {
              wxuser: wxUserStore.wxuser,
              user: userStore.user,
              isWxLogin: isWxLogin,
              needsRouteGeneration: needsRouteGeneration
            });

            const accessRoutes = await permissionStore.generateRoutes();
            console.log('生成的路由数量:', accessRoutes?.length || 0);

            // 添加路由前检查和打印日志
            if (Array.isArray(accessRoutes) && accessRoutes.length > 0) {
              let addedCount = 0;
              accessRoutes.forEach((route: any) => {
                if (route && typeof route === 'object') {
                  if (route.meta?.isExternal) {
                    return;
                  }
                  router.addRoute(route);
                  addedCount++;
                } else {
                  console.error('Invalid route object:', route);
                }
              });
              console.log(`成功添加 ${addedCount} 个路由`);
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

