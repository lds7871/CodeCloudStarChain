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
    playlistType: {
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
    this.initPyjMusic();
  },
  beforeDestroy() {
    // 清理播放器实例
    if (window.ap) {
      window.ap.destroy();
      window.ap = null;
    }
  },
  methods: {
    initPyjMusic() {
      // 设置全局配置变量
      window.userId = this.userId;
      window.userServer = this.userServer;
      window.playlistType = this.playlistType;
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
      // 脚本已经在index.html中加载，直接初始化播放器
      setTimeout(() => {
        this.initPlayer();
      }, 100);
    },

    initPlayer() {
      // 调用pyj.init()来初始化播放器
      if (window.pyj && typeof window.pyj.init === "function") {
        window.pyj.init();
      } else {
        // 如果pyj对象还没有准备好，等待一下再调用
        setTimeout(() => {
          if (window.pyj && typeof window.pyj.init === "function") {
            window.pyj.init();
          }
        }, 100);
      }
    },
  },
};
</script>

<style scoped></style>
