<template>
  <div id="app">
    <TheHeader />
    <MobileMenu />
    <SearchDialog />
    <router-view class="main-container" />
    <TheFooter />
    <FloatingButtons />

    <div class="cursor-container"></div>
    <ContextMenu ref="contextMenuRef" />
  </div>
</template>

<script>
import TheHeader from '@/layout/Header/index.vue'
import TheFooter from '@/layout/Footer/index.vue'
import FloatingButtons from '@/components/common/FloatingButtons.vue'
import { getWebConfigApi, reportApi, getNoticeApi } from '@/api/site'
import { mapActions } from 'vuex'
import { initTheme } from '@/utils/theme'
import SearchDialog from '@/components/Search/index.vue'
import MobileMenu from '@/layout/MobileMenu/index.vue'
import { getCookie, removeCookie } from '@/utils/cookie'
import ContextMenu from '@/components/ContextMenu/index.vue'

export default {
  name: 'App',
  components: {
    TheHeader,
    TheFooter,
    FloatingButtons,
    SearchDialog,
    MobileMenu,
    ContextMenu,
  },

  async created() {
    await reportApi()
    const res = await getWebConfigApi()
    this.setSiteInfo(res.data)
    this.$store.commit('setVisitorAccess', res.extra.visitorCount)
    this.$store.commit('setSiteAccess', res.extra.blogViewsCount)

    const noticeRes = await getNoticeApi()
    this.$store.commit('SET_NOTICE', noticeRes.data)
    initTheme()
    await this.handleThirdPartyLogin()
    //这里等待第三方登录处理完成在获取用户信息
    await this.getUserInfo();

    //跳转到缓存地址
    let url = getCookie('redirectUrl')
    if (url) {
      removeCookie('redirectUrl')
      window.location.href = url
    }
  },
  methods: {
    ...mapActions(['setSiteInfo', 'getUserInfo']),

    /**
     * 处理第三方登录用回调逻辑
     */
    async handleThirdPartyLogin() {
      let flag = window.location.href.indexOf("token") != -1;
      if (flag) {
        let token = window.location.href.split("token=")[1];

        try {
          // 1. 设置token到store (调用mutation)
          this.$store.commit('SET_TOKEN', token);

          // 2. 执行Gitee登录流程 (调用action)
          const userData = await this.$store.dispatch('giteeLogin', token);

          if (userData) {
            this.$message.success("登录成功");
            // 清除URL中的token参数
            const currentUrl = new URL(window.location.href);
            currentUrl.searchParams.delete('token');
            const newUrl = currentUrl.pathname + currentUrl.hash;
            window.history.replaceState({}, '', newUrl);

            // 延迟跳转，让消息先显示出来
            setTimeout(() => {
              this.$router.push('/');
            }, 1500); // 1.5秒后跳转
          }
        } catch (error) {
          console.error('登录失败:', error);
          this.$message.error("登录失败，请重试");
          // 登录失败也要清除URL中的token，但不跳转
          setTimeout(() => {
            const currentUrl = new URL(window.location.href);
            currentUrl.searchParams.delete('token');
            const newUrl = currentUrl.pathname + currentUrl.hash;
            window.history.replaceState({}, '', newUrl);
          }, 1500);
        }
      }
    },
    /**
     * 初始化鼠标点击效果
     */
    initCursorEffect() {
      const container = document.querySelector('.cursor-container')

      document.addEventListener('click', (e) => {
        const cursor = document.createElement('div')
        cursor.className = 'cursor-fx'
        cursor.style.left = `${e.clientX}px`
        cursor.style.top = `${e.clientY}px`
        container.appendChild(cursor)

        cursor.addEventListener('animationend', () => {
          cursor.remove()
        })
      })
    },

    initContextMenu() {
      const handleContextMenu = (e) => {
        this.$refs.contextMenuRef.show(e)
      }

      const handleClick = () => {
        this.$refs.contextMenuRef.hide()
      }

      document.addEventListener('contextmenu', handleContextMenu)
      document.addEventListener('click', handleClick)

      // 在组件销毁时移除事件监听
      this.$once('hook:beforeDestroy', () => {
        document.removeEventListener('contextmenu', handleContextMenu)
        document.removeEventListener('click', handleClick)
      })
    }
  },
  mounted() {
    this.initCursorEffect()
    this.initContextMenu()
  }
}
</script>

<style lang="scss">
@import 'animate.css';
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');

* {
  margin: 0;
  padding: 0;
  font-family: "font";
  box-sizing: border-box;
}
</style>