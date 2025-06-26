import request from '@/utils/request'

/**
 * 登录
 * @param {*} data 
 * @returns 
 */
export function loginApi(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 退出登录
 * @returns 
 */
export function logoutApi(userInfo) {
  return request({
    url: '/auth/logout',
    method: 'delete',
    params: {
      userInfo: userInfo
    }
  })
}

/**
 * 获取用户信息
 */
export function getUserInfoApi() {
  return request({
    url: `/auth/info`,
    method: 'get',
    params: {
      source: 'web'
    }
  })
}

/**
 * 发送邮箱验证吗
 */
export function sendEmailCodeApi(email) {
  return request({
    url: `/api/sendEmailCode`,
    method: 'get',
    params: {
      email: email
    }
  })
}

/**
 * 注册
 */
export function registerApi(data) {
  return request({
    url: `/api/email/register`,
    method: 'post',
    data
  })
}

/**
 * 忘记密码
 */
export function forgotPasswordApi(data) {
  return request({
    url: `/api/email/forgot`,
    method: 'post',
    data
  })
}

// 微信扫码获取用户信息
export function getQrCode() {
  return request({
    url: '/wechat/qrCode',
    method: 'get',
    responseType: 'text'
  })
}
// 获取微信用户信息
export function getwxUserInfoApi(openid) {
  return request({
    url: "/auth/wxinfo",
    method: "get",
    params: {
      openid: openid,
    }
  })
}

// 检查扫码状态
export function checkQrCodeStatus() {
  return request({
    url: '/wechat/checkQrCodeStatus',
    method: 'get'
  })
}
export function giteeLoginApi() {
  return request({
    url: '/oauth/getGiteeUserInfo',
    method: 'get',
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

// 发送绑定邮箱验证码
export function sendBindEmailCodeApi(email) {
  return request({
    url: `/api/sendBindEmailCode`,
    method: 'get',
    params: { email }
  })
}

// 绑定邮箱
export function bindEmailApi(data) {
  return request({
    url: `/api/email/bind`,
    method: 'post',
    data
  })
}

// 解绑邮箱
export function unbindEmailApi() {
  return request({
    url: `/api/email/unbind`,
    method: 'post'
  })
}