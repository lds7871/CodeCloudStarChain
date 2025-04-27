import request from '@/utils/request'

interface LoginParams {
  username: string
  password: string
  captchaCode: string
  captchaKey: string
  rememberMe: boolean
}

// 登录接口
export function loginApi(data: LoginParams) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 微信扫码获取用户信息
export function getQrCode() {
  return request({
    url: 'http://53cf52aa.r33.cpolar.top/wechat/qrCode',
    method: 'get',
    responseType: 'text'
  })
}
// 退出登录
export function logoutApi() {
  return request({
    url: '/auth/logout',
    method: 'post',
  })
}
// 检查扫码状态
export function checkQrCodeStatus() {
  return request({
    url: 'http://53cf52aa.r33.cpolar.top/wechat/checkQrCodeStatus',
    method: 'get'
  })
}
// 获取用户信息
export function getUserInfoApi() {
  return request({
    url: "/auth/info",
    method: "get",
    params: {
      source: "admin"
    }
  })
}

// 获取微信用户信息
export function getwxUserInfoApi(openid: string) {
  return request({
    url: "/auth/wxinfo",
    method: "get",
    params: {
      openid: openid,
      source: "admin"
    }
  })
}

export function getRouters() {
  return request({
    url: '/sys/menu/routers',
    method: 'get'
  })
}

// 获取验证码
export function getCaptchaApi() {
  return request({
    url: '/auth/getCaptcha',
    method: 'get'
  })
}


// 获取验证码开关
export function getCaptchaSwitchApi() {
  return request({
    url: '/sys/config/getConfigByKey/slider_verify_switch',
    method: 'get'
  })
}