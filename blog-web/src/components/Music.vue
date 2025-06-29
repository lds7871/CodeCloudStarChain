<template>
  <div id="Music-page"></div>
</template>

<script>
export default {
  name: "Music",
  props: {
    // 音乐配置参数
    userId: {
      type: String,
      default: "8668419170",
    },
    userServer: {
      type: String,
      default: "tencent",
    },
    userType: {
      type: String,
      default: "playlist",
    },
    localMusic: {
      type: Array,
      default: () => [],
    },
    remoteMusic: {
      type: String,
      default: "",
    },
  },
  mounted() {
    this.loadStyles();
    this.initHeoMusic();
  },
  beforeDestroy() {
    // 清理播放器实例
    if (window.ap) {
      window.ap.destroy();
      window.ap = null;
    }
  },
  methods: {
    loadStyles() {
      // 动态加载CSS文件
      const loadCSS = (href) => {
        const link = document.createElement("link");
        link.rel = "stylesheet";
        link.type = "text/css";
        link.href = href;
        document.head.appendChild(link);
      };

      // 检查是否已经加载过
      if (!document.querySelector('link[href*="APlayer.css"]')) {
        loadCSS("/blogclient/music/css/APlayer.css");
      }
      if (!document.querySelector('link[href*="main.css"]')) {
        loadCSS("/blogclient/music/css/main.css");
      }
    },

    initHeoMusic() {
      // 设置全局配置变量
      window.userId = this.userId;
      window.userServer = this.userServer;
      window.userType = this.userType;
      window.localMusic = this.localMusic;
      window.remoteMusic = this.remoteMusic;

      // 加载远程音乐配置
      if (this.remoteMusic) {
        fetch(this.remoteMusic)
          .then((response) => response.json())
          .then((data) => {
            if (Array.isArray(data)) {
              window.localMusic = data;
            }
            this.loadMusicScript();
          })
          .catch((error) => {
            console.error("Error fetching remoteMusic:", error);
            this.loadMusicScript();
          });
      } else {
        this.loadMusicScript();
      }
    },

    loadMusicScript() {
      // 首先确保APlayer已加载
      if (typeof APlayer === "undefined") {
        this.loadScript("/blogclient/music/js/APlayer.min.js", () => {
          this.loadHeoMain();
        });
      } else {
        this.loadHeoMain();
      }
    },

    loadHeoMain() {
      // 先加载main.js（包含heo对象）
      this.loadScript("/blogclient/music/js/main.js", () => {
        this.loadMusicEngine();
      });
    },

    loadMusicEngine() {
      if (
        !window.localMusic ||
        !Array.isArray(window.localMusic) ||
        window.localMusic.length === 0
      ) {
        // 加载在线音乐脚本
        this.loadScript("/blogclient/music/js/Meting.js", () => {
          this.initPlayer();
        });
      } else {
        // 加载本地音乐脚本
        this.loadScript("/blogclient/music/js/localEngine.js", () => {
          this.initPlayer();
        });
      }
    },

    loadScript(src, callback) {
      const script = document.createElement("script");
      script.src = src;
      script.onload = callback;
      document.body.appendChild(script);
    },

    initPlayer() {
      // 调用heo.init()来初始化播放器
      if (window.heo && typeof window.heo.init === "function") {
        window.heo.init();
      } else {
        // 如果heo对象还没有准备好，等待一下再调用
        setTimeout(() => {
          if (window.heo && typeof window.heo.init === "function") {
            window.heo.init();
          }
        }, 100);
      }
    },
  },
};
</script>

<style scoped></style>
