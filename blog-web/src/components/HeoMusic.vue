<template>
  <div class="heo-music-container">
    <div id="heoMusic-page"></div>
  </div>
</template>

<script>
export default {
  name: 'HeoMusic',
  props: {
    // 音乐配置参数
    userId: {
      type: String,
      default: '8668419170'
    },
    userServer: {
      type: String,
      default: 'tencent'
    },
    userType: {
      type: String,
      default: 'playlist'
    },
    localMusic: {
      type: Array,
      default: () => []
    },
    remoteMusic: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      local: false,
      isScrolling: false,
      scrollTimer: null,
      animationFrameId: null,
      volume: 0.8,
      heo: null,
      playerKey: 0 // 添加key来强制重新渲染
    }
  },
  watch: {
    // 监听props变化，重新初始化播放器
    userId() {
      this.reloadPlayer()
    },
    userServer() {
      this.reloadPlayer()
    },
    userType() {
      this.reloadPlayer()
    },
    localMusic: {
      handler() {
        this.reloadPlayer()
      },
      deep: true
    },
    remoteMusic() {
      this.reloadPlayer()
    }
  },
  mounted() {
    this.loadStyles()
    this.initHeoMusic()
  },
  beforeDestroy() {
    // 清理定时器和事件监听
    if (this.scrollTimer) {
      clearTimeout(this.scrollTimer)
    }
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId)
    }
    // 清理播放器实例
    if (window.ap) {
      window.ap.destroy()
      window.ap = null
    }
  },
  methods: {
    loadStyles() {
      // 动态加载CSS文件
      const loadCSS = (href) => {
        const link = document.createElement('link')
        link.rel = 'stylesheet'
        link.type = 'text/css'
        link.href = href
        document.head.appendChild(link)
      }
      
      // 检查是否已经加载过
      if (!document.querySelector('link[href*="APlayer.css"]')) {
        loadCSS('/blogclient/heo-music/css/APlayer.css')
      }
      if (!document.querySelector('link[href*="main.css"]')) {
        loadCSS('/blogclient/heo-music/css/main.css')
      }
    },

    reloadPlayer() {
      // 清理旧的播放器
      if (window.ap) {
        window.ap.destroy()
        window.ap = null
      }
      
      // 清理DOM
      const heoMusicPage = document.getElementById("heoMusic-page")
      if (heoMusicPage) {
        heoMusicPage.innerHTML = ''
      }
      
      // 重新初始化
      this.playerKey++
      this.$nextTick(() => {
        this.initHeoMusic()
      })
    },

    initHeoMusic() {
      // 设置全局配置变量
      window.userId = this.userId
      window.userServer = this.userServer
      window.userType = this.userType
      window.localMusic = this.localMusic
      window.remoteMusic = this.remoteMusic

      // 加载远程音乐配置
      if (this.remoteMusic) {
        fetch(this.remoteMusic)
          .then(response => response.json())
          .then(data => {
            if (Array.isArray(data)) {
              window.localMusic = data
            }
            this.loadMusicScript()
          })
          .catch(error => {
            console.error('Error fetching remoteMusic:', error)
            this.loadMusicScript()
          })
      } else {
        this.loadMusicScript()
      }
    },

    loadMusicScript() {
      // 首先确保APlayer已加载
      if (typeof APlayer === 'undefined') {
        this.loadScript('/blogclient/heo-music/js/APlayer.min.js', () => {
          this.loadHeoMain()
        })
      } else {
        this.loadHeoMain()
      }
    },

    loadHeoMain() {
      // 先加载main.js（包含heo对象）
      this.loadScript('/blogclient/heo-music/js/main.js', () => {
        this.loadMusicEngine()
      })
    },

    loadMusicEngine() {
      if (!window.localMusic || !Array.isArray(window.localMusic) || window.localMusic.length === 0) {
        // 加载在线音乐脚本
        this.loadScript('/blogclient/heo-music/js/Meting.js', () => {
          this.initPlayer()
        })
      } else {
        // 加载本地音乐脚本
        this.loadScript('/blogclient/heo-music/js/localEngine.js', () => {
          this.local = true
          this.initPlayer()
        })
      }
    },

    loadScript(src, callback) {
      const script = document.createElement('script')
      script.src = src
      script.onload = callback
      document.body.appendChild(script)
    },

    initPlayer() {
      // 初始化播放器
      this.heo = {
        handleScrollOrTouch: (event, isTouchEvent) => {
          let targetElement = event.target
          let isInTargetArea = false
          
          while (targetElement && targetElement !== document) {
            if (targetElement.classList) {
              if (isTouchEvent) {
                if (targetElement.classList.contains('aplayer-body') || 
                    targetElement.classList.contains('aplayer-lrc')) {
                  isInTargetArea = true
                  break
                }
              } else {
                if (targetElement.classList.contains('aplayer-body')) {
                  isInTargetArea = true
                  break
                }
              }
            }
            targetElement = targetElement.parentNode
          }
          
          if (isInTargetArea) {
            if (this.animationFrameId !== null) {
              cancelAnimationFrame(this.animationFrameId)
              this.animationFrameId = null
            }
            
            this.isScrolling = true
            
            if (this.scrollTimer !== null) {
              clearTimeout(this.scrollTimer)
            }
            
            const timeoutDuration = isTouchEvent ? 4500 : 4000
            this.scrollTimer = setTimeout(() => {
              this.isScrolling = false
              this.scrollLyric()
            }, timeoutDuration)
          }
        },

        scrollLyric: () => {
          if (this.isScrolling) {
            return
          }
          
          const lrcContent = document.querySelector('.aplayer-lrc')
          const currentLyric = document.querySelector('.aplayer-lrc-current')

          if (lrcContent && currentLyric) {
            let startScrollTop = lrcContent.scrollTop
            let targetScrollTop = currentLyric.offsetTop - (window.innerHeight - 150) * 0.3
            let distance = targetScrollTop - startScrollTop
            let duration = 600
            let startTime = null

            const easeOutQuad = (t) => t * (2 - t)

            const animateScroll = (currentTime) => {
              if (this.isScrolling) {
                this.animationFrameId = null
                return
              }
              
              if (startTime === null) startTime = currentTime
              let timeElapsed = currentTime - startTime
              let progress = Math.min(timeElapsed / duration, 1)
              let easeProgress = window.innerWidth < 768 ? progress : easeOutQuad(progress)
              lrcContent.scrollTop = startScrollTop + (distance * easeProgress)
              
              if (timeElapsed < duration) {
                this.animationFrameId = requestAnimationFrame(animateScroll)
              } else {
                this.animationFrameId = null
              }
            }

            if (this.animationFrameId !== null) {
              cancelAnimationFrame(this.animationFrameId)
            }
            
            this.animationFrameId = requestAnimationFrame(animateScroll)
          }
        },

        getCustomPlayList: () => {
          const heoMusicPage = document.getElementById("heoMusic-page")
          const params = new URLSearchParams(window.location.search)
          const playlistType = params.get("type") || "playlist"

          if (params.get("id") && params.get("server")) {
            console.log("获取到自定义内容")
            const id = params.get("id")
            const server = params.get("server")
            heoMusicPage.innerHTML = `<meting-js id="${id}" server="${server}" type="${playlistType}" mutex="true" preload="auto" order="random"></meting-js>`
          } else {
            console.log("无自定义内容")
            heoMusicPage.innerHTML = `<meting-js id="${window.userId}" server="${window.userServer}" type="${window.userType}" mutex="true" preload="auto" order="random"></meting-js>`
          }
        },

        initScrollEvents: () => {
          document.addEventListener('wheel', (event) => {
            this.heo.handleScrollOrTouch(event, false)
          }, { passive: true })
          
          document.addEventListener('touchmove', (event) => {
            this.heo.handleScrollOrTouch(event, true)
          }, { passive: true })
        }
      }

      // 调用heo.init()来初始化播放器
      if (window.heo && typeof window.heo.init === 'function') {
        window.heo.init()
      } else {
        // 如果heo对象还没有准备好，等待一下再调用
        setTimeout(() => {
          if (window.heo && typeof window.heo.init === 'function') {
            window.heo.init()
          }
        }, 100)
      }
    }
  }
}
</script>

<style scoped>
.heo-music-container {
  width: 100%;
  height: 100%;
}
</style> 