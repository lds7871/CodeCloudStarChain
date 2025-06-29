import Cookies from 'js-cookie'

const TokenKey = 'Neat-Admin-Token'

// 设置token到cookie，过期时间1小时
export function setToken(token: string) {
  console.log('setToken called with:', token);

  // 使用更宽松的cookie设置
  const result = Cookies.set(TokenKey, token, {
    expires: 1, // 1天过期，避免过期时间太短
    path: '/', // 确保整个应用都能访问
    sameSite: 'lax' // 提高兼容性
  });

  console.log('Cookie设置结果:', result);

  // 立即验证设置是否成功
  const verification = Cookies.get(TokenKey);
  console.log('Cookie设置验证:', verification);

  return result;
}

export function getToken() {
  const token = Cookies.get(TokenKey);
  console.log('getToken result:', token);
  return token;
}

export function removeToken() {
  console.log('removeToken called');
  // 同时清理所有token相关的cookie
  Cookies.remove(TokenKey, { path: '/' });
  Cookies.remove('Authorization', { path: '/' });
  Cookies.remove('blog_token', { path: '/' });
  return true;
}

// 清空所有cookies
export function clearAllCookies() {
  console.log('clearAllCookies called');

  // 方法1：通过js-cookie获取所有cookie名称并删除
  const allCookies = Cookies.get();
  Object.keys(allCookies).forEach(cookieName => {
    Cookies.remove(cookieName, { path: '/' });
    Cookies.remove(cookieName, { path: '/', domain: window.location.hostname });
    Cookies.remove(cookieName); // 默认路径
  });

  // 方法2：通过原生JavaScript清空（备用方案）
  document.cookie.split(";").forEach(cookie => {
    const eqPos = cookie.indexOf("=");
    const name = eqPos > -1 ? cookie.substr(0, eqPos).trim() : cookie.trim();
    if (name) {
      // 尝试不同的路径和域名组合
      document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/`;
      document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/;domain=${window.location.hostname}`;
      document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT`;
    }
  });

  console.log('所有cookies已清空');
} 