<template>
  <div class="login-container" :class="{ dark: isDark }">
    <!-- 左侧背景区域 -->
    <div class="login-background">
      <div class="background-wrapper">
        <!-- 添加简单的装饰图形 -->
        <div class="animated-background">
          <div class="gradient-circle"></div>
          <div class="geometric-shapes">
            <div class="shape" v-for="n in 5" :key="n"></div>
          </div>
        </div>

        <!-- Logo和标题区域 -->
        <div class="brand-content">
          <div class="logo-wrapper">
            <Logo :size="80" class="floating-logo" :color="logoColor" />
          </div>
          <h1 class="brand-title">{{ settings.title }}</h1>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区域 -->
    <div class="login-form">
      <div class="form-wrapper">
        <!-- 主题切换按钮 -->
        <div class="theme-switch">
          <el-button class="theme-button" circle @click="toggleTheme">
            <el-icon>
              <component :is="isDark ? 'Sunny' : 'Moon'" />
            </el-icon>
          </el-button>
        </div>

        <h2 class="welcome-text">欢迎回来</h2>
        <p class="login-tip">请使用您的账号密码登录系统</p>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <div class="tab-item" :class="{ active: loginType === 'account' }" @click="loginType = 'account'">
            <el-icon>
              <User />
            </el-icon>
            账号登录
          </div>
          <div class="tab-item" :class="{ active: loginType === 'qrcode' }" @click="loginType = 'qrcode'">
            <el-icon>
              <component :is="QrCode" />
            </el-icon>
            扫码登录
          </div>
        </div>

        <!-- 登录表单内容 -->
        <transition name="fade-transform" mode="out-in">
          <el-form v-if="loginType === 'account'" ref="loginFormRef" :model="loginForm" :rules="rules"
            @keyup.enter="handleLogin">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock"
                show-password />
            </el-form-item>

            <div class="login-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <a href="#" class="forget-password">忘记密码？</a>
            </div>

            <el-button :loading="loading" type="primary" class="login-button" @click="handleLogin">
              {{ loading ? "登录中..." : "登录" }}
            </el-button>
          </el-form>

          <div v-else class="qrcode-box">
            <div class="qrcode-wrapper">
              <div class="qrcode-scanner"></div>
              <!-- <img :src="qrCodeUrl" alt="二维码" class="qrcode-img" /> -->
              <img :src="qrCodeUrl" alt="微信二维码">
              <transition name="fade">
                <div class="qrcode-mask" v-if="qrCodeExpired">
                  <el-icon class="expired-icon">
                    <Warning />
                  </el-icon>
                  <p>二维码已过期</p>
                  <el-button type="primary" @click="refreshQrCode" round>
                    <el-icon>
                      <RefreshRight />
                    </el-icon>
                    刷新二维码
                  </el-button>
                </div>
              </transition>
            </div>
            <p class="qrcode-tip">
              <el-icon>
                <Iphone />
              </el-icon>
              请使用手机扫码登录
            </p>
          </div>
        </transition>

        <!-- 社交登录 -->
        <div class="social-login">
          <div class="divider">其他登录方式</div>
          <div class="social-icons">
            <div class="social-icon" @click="handleSocialLogin('wechat')">
              <svg-icon name="wechat" />
            </div>
            <div class="social-icon" @click="handleSocialLogin('qq')">
              <svg-icon name="qq" />
            </div>
            <div class="social-icon" @click="handleSocialLogin('gitee')">
              <svg-icon name="gitee" />
            </div>
          </div>
        </div>
      </div>

      <!-- 滑块验证 -->
      <el-dialog title="请拖动滑块完成拼图" width="360px" v-model="showSliderVerify" :close-on-click-modal="false"
        @closed="refresh" append-to-body>
        <slider-verify ref="sliderVerifyRef" @success="onSuccess" @fail="onFail" @again="onAgain" />
      </el-dialog>

      <!-- 页脚版权信息 -->
      <footer class="footer">
        <p>Copyright © 2025 码云星链</p>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import router from "@/router";
import type { FormInstance } from "element-plus";
import { ElMessage } from "element-plus";
import { useUserStore, wxuseUserStore } from "@/store/modules/user";

import { useSettingsStore } from "@/store/modules/settings";
import Logo from "@/layouts/components/Sidebar/Logo.vue";
import settings from "@/config/settings";
import SliderVerify from "./components/SliderVerify.vue";
import { getCaptchaSwitchApi, getQrCode, checkQrCodeStatus, getRouters } from "@/api/system/auth";
import { usePermissionStore } from "@/store/modules/permission";
const QrCode = markRaw({
  name: "QrCode",
  render() {
    return h(
      "svg",
      {
        viewBox: "0 0 1024 1024",
        width: "1em",
        height: "1em",
        fill: "currentColor",
      },
      [
        h("path", {
          d: "M468 128H160c-17.7 0-32 14.3-32 32v308c0 4.4 3.6 8 8 8h332c4.4 0 8-3.6 8-8V136c0-4.4-3.6-8-8-8zm-56 284H192V192h220v220zm-138-74h56c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8h-56c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8zm444-140H556c-4.4 0-8 3.6-8 8v332c0 4.4 3.6 8 8 8h276c4.4 0 8-3.6 8-8V160c0-17.7-14.3-32-32-32zm-56 284H556V192h220v220zm-138-74h56c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8h-56c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8zM192 556v308c0 17.7 14.3 32 32 32h308c4.4 0 8-3.6 8-8V556c0-4.4-3.6-8-8-8H160c-4.4 0-8 3.6-8 8zm56 284V556h220v284H192zm-64-220h56c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8h-56c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8zm500 220c0 4.4 3.6 8 8 8h108v108c0 4.4 3.6 8 8 8h56c4.4 0 8-3.6 8-8V556c0-4.4-3.6-8-8-8H556c-4.4 0-8 3.6-8 8v332zm64-216h108v108H748V624z",
        }),
      ]
    );
  },
});
const userStore = useUserStore();
const wxuserStore = wxuseUserStore();
const settingsStore = useSettingsStore();
const loginFormRef = ref<FormInstance>();
const loading = ref(false);
const rememberMe = ref(false);
const loginType = ref("account");
const permissionStore = usePermissionStore();
const qrCodeExpired = ref(false);

const showSliderVerify = ref(false);
const sliderVerifyRef = ref();

const loginForm = reactive({
  username: "admin",
  password: "123456",
  rememberMe: false,
  source: "ADMIN",
  nonceStr: "",
  value: "",
});

const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "长度在 3 到 20 个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
  ],
};

const isDark = computed(() => settingsStore.theme === "dark");

const refresh = () => {
  sliderVerifyRef.value?.refresh();
};

const onSuccess = (captcha: any) => {
  loginForm.nonceStr = captcha.nonceStr;
  loginForm.value = captcha.value;

  login();
};
const qrCodeUrl = ref("");
const getQrCodes = () => {
  getQrCode().then((res) => {
    console.log(res);
    qrCodeUrl.value = res;
    console.log("数据为：" + qrCodeUrl.value);
  });
}
const login = () => {
  loading.value = true;
  userStore
    .login(loginForm)
    .then(() => {
      sliderVerifyRef?.value?.verifySuccessEvent();
      router.push("/");
      ElMessage.success("登录成功");
    })
    .catch(() => {
      refresh();
    })
    .finally(() => {
      loading.value = false;
    });
};


/* 滑动验证失败*/
const onFail = (msg: string) => {
  refresh();
};
/* 滑动验证异常*/
const onAgain = () => {
  ElMessage.error("滑动操作异常，请重试");
};

const toggleTheme = () => {
  const newTheme = isDark.value ? "light" : "dark";
  settingsStore.saveSettings({ theme: newTheme });
};

const handleLogin = async () => {
  loginFormRef.value?.validate((flag) => {
    getCaptchaSwitchApi().then((res) => {
      if (!res.data || res.data.configValue === "Y") {
        showSliderVerify.value = true;
      } else {
        login();
      }
    });
  });
};

const handleSocialLogin = (type: string) => {
  ElMessage.success(type + "登录测试");
};

const refreshQrCode = async () => {
  qrCodeExpired.value = false;
  getQrCodes(); // 调用获取新二维码的方法
};
let openId: String;
let qrCodeTimer: number;
let headimgurl: String;
let nickname: String;
watch(loginType, (newVal) => {
  if (newVal === "qrcode") {
    refreshQrCode();
    qrCodeTimer = window.setInterval(() => {
      checkQrCodeStatus().then(res => {
        console.log('二维码状态：', res);
        if (res.status === 'CONFIRMED') {
          wxuserStore.wxlogin()
            .then((res) => {
              openId = res.openid;
              headimgurl = res.headimgurl;
              nickname = res.nickname;
              // 确保 res.openid 存在
              if (!res.openid) {
                throw new Error('未获取到openid');
              }
              return wxuserStore.getwxUserInfo(res.openid);
            })
            .then(() => {
              // 获取并生成路由
              return permissionStore.generateRoutes();
            })
            .then((accessRoutes) => {
              // 添加路由
              accessRoutes.forEach(route => {
                router.addRoute(route);
              });
              return nextTick();
            })
            .then(() => {
              ElMessage.success("扫码登录成功");
              clearInterval(qrCodeTimer);
              localStorage.setItem("userInfo", "weixin");
              localStorage.setItem("openId", openId);
              localStorage.setItem("headimgurl", headimgurl);
              localStorage.setItem("nickname", nickname);
              router.push("/");
            })
            .catch(error => {
              console.error('登录失败：', error);
              ElMessage.error("登录失败，请重试");
            });
        } else if (res.status === 'EXPIRED') {
          qrCodeExpired.value = true;
          clearInterval(qrCodeTimer);
        } else if (res.status === 'SCANNED') {
          ElMessage.success("已扫码，请在手机上确认");
        }
      }).catch(error => {
        console.error('检查二维码状态失败：', error);
      });
    }, 3000);
  } else {
    clearInterval(qrCodeTimer);
  }
});

onUnmounted(() => {
  clearInterval(qrCodeTimer);
});

onMounted(() => {
  getQrCodes();
});
// 添加 logo 颜色计算
const logoColor = computed(() => {
  return isDark.value ? "#4ecdc4" : "#ff6b6b";
});
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  // background: var(--el-bg-color);
}

/* 左侧背景区域样式 */
.login-background {
  flex: 1;
  position: relative;
  // background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  overflow: hidden;
  background: url("/src/assets/login/login-bg.png");
  width: 50%;
  background-position: -500px 0px;
}

.background-wrapper {
  position: relative;
  height: 100%;
  display: flex;
  justify-content: center;
  margin-top: 130px;
}

/* 品牌内容样式优化 */
.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.logo-wrapper {
  margin-bottom: 30px;
  position: relative;
}

.brand-title {
  font-size: 36px;
  font-weight: bold;
  color: #1565c0;
  margin-bottom: 16px;
  text-shadow: 0 0 10px rgba(33, 150, 243, 0.5);
}

.brand-description {
  font-size: 16px;
  color: #1976d2;
  max-width: 400px;
  margin: 0 auto;
  line-height: 1.6;
}

/* 移除之前的动画相关样式 */
.animated-background,
.gradient-circle,
.geometric-shapes {
  display: none;
}

/* 右侧登录表单区域样式 */
.login-form {
  width: 500px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: var(--el-bg-color);
  position: relative;
  box-shadow: -10px 0 20px rgba(0, 0, 0, 0.05);

  .form-wrapper {
    max-width: 400px;
    margin: 0 auto;
    width: 100%;
  }

  /* 登录方式切换 */
  .login-tabs {
    display: flex;
    margin-bottom: 30px;
    background: var(--el-fill-color-light);
    padding: 5px;
    border-radius: 12px;
    position: relative;

    .tab-item {
      flex: 1;
      padding: 12px;
      text-align: center;
      cursor: pointer;
      border-radius: 8px;
      transition: all 0.3s;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: var(--el-text-color-secondary);

      &.active {
        background: var(--el-bg-color);
        color: var(--el-color-primary);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      .el-icon {
        font-size: 18px;
      }
    }
  }

  /* 表单项样式 */
  :deep(.el-form-item) {
    margin-bottom: 24px;

    .el-input__wrapper {
      padding: 0 15px;
      height: 44px;
      border-radius: 8px;
      background: var(--el-fill-color-light);
      border: 2px solid transparent;
      transition: all 0.3s;

      &:hover,
      &.is-focus {
        border-color: var(--el-color-primary);
        background: var(--el-bg-color);
      }

      .el-input__inner {
        font-size: 14px;
      }
    }
  }

  /* 登录按钮 */
  .login-button {
    width: 100%;
    height: 44px;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
    margin-top: 10px;
    background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
    border: none;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 5px 15px rgba(255, 107, 107, 0.3);
    }
  }

  /* 二维码登录样式 */
  .qrcode-box {
    text-align: center;
    padding: 30px 0;

    .qrcode-wrapper {
      width: 200px;
      height: 200px;
      margin: 0 auto;
      padding: 15px;
      background: white;
      border-radius: 12px;
      position: relative;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

      .qrcode-scanner {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 2px;
        background: var(--el-color-primary);
        animation: scan 2s linear infinite;
      }

      .qrcode-img {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }

      .qrcode-mask {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(255, 255, 255, 0.9);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 12px;
        border-radius: 12px;
      }
    }

    .qrcode-tip {
      margin-top: 20px;
      color: var(--el-text-color-secondary);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    }
  }

  /* 社交登录样式 */
  .social-login {
    margin-top: 40px;

    .divider {
      display: flex;
      align-items: center;
      color: var(--el-text-color-secondary);
      font-size: 14px;
      margin-bottom: 20px;

      &::before,
      &::after {
        content: "";
        flex: 1;
        height: 1px;
        background: var(--el-border-color);
        margin: 0 16px;
      }
    }

    .social-icons {
      display: flex;
      justify-content: center;
      gap: 20px;

      .social-icon {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s;
        background: var(--el-fill-color-light);
        color: var(--el-text-color-regular);

        &:hover {
          transform: translateY(-2px);
          background: var(--el-color-primary-light-9);
          color: var(--el-color-primary);
        }
      }
    }
  }

  /* 页脚样式 */
  .footer {
    text-align: center;
    color: var(--el-text-color-secondary);
    font-size: 14px;

    a {
      color: inherit;
      text-decoration: none;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }
}

/* 动画相关 */
@keyframes scan {
  0% {
    top: 0;
  }

  50% {
    top: calc(100% - 2px);
  }

  100% {
    top: 0;
  }
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-20px);
  }
}

@keyframes rotate {
  0% {
    transform: rotate(0);
  }

  100% {
    transform: rotate(360deg);
  }
}

/* 主题切换按钮样式 */
.theme-switch {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;

  .theme-button {
    width: 40px;
    height: 40px;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border: none;
    color: var(--el-text-color-primary);
    transition: all 0.3s;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
      transform: translateY(-2px);
    }
  }
}

/* 登录选项样式 */
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;

  .el-checkbox {
    color: var(--el-text-color-regular);
  }

  .forget-password {
    color: var(--el-text-color-regular);
    text-decoration: none;
    font-size: 14px;
    transition: all 0.3s;

    &:hover {
      color: var(--el-color-primary);
    }
  }
}

/* 深色模式适配 */
.dark {
  .login-form {
    background: var(--el-bg-color-overlay);

    .login-tabs {
      background: rgba(255, 255, 255, 0.05);

      .tab-item.active {
        background: rgba(0, 0, 0, 0.3);
      }
    }

    :deep(.el-input__wrapper) {
      background: rgba(0, 0, 0, 0.2);

      &:hover,
      &.is-focus {
        background: rgba(0, 0, 0, 0.3);
      }
    }

    .qrcode-wrapper {
      background: var(--el-bg-color);
    }

    .social-icon {
      background: rgba(255, 255, 255, 0.05);

      &:hover {
        background: rgba(255, 255, 255, 0.1);
      }
    }
  }

  .theme-switch {
    .theme-button {
      background: rgba(0, 0, 0, 0.2);

      &:hover {
        background: rgba(0, 0, 0, 0.3);
      }
    }
  }

  .logo-wrapper {
    :deep(svg path) {
      fill: #2196f3;
    }
  }
}

/* 更新动画 */
@keyframes matrixMove {
  0% {
    background-position: 0 0;
  }

  100% {
    background-position: 0 30px;
  }
}

@keyframes glowPulse {

  0%,
  100% {
    opacity: 0.3;
    transform: scale(0.95);
  }

  50% {
    opacity: 0.8;
    transform: scale(1.05);
  }
}

@keyframes rotateBorder {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}
</style>
