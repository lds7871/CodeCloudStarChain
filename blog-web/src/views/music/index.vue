<template>
  <div class="music-page">

    <!-- 音乐播放器容器 -->
    <div class="music-player-container" v-animate-on-scroll>

      <Music
        :userId="musicConfig.userId"
        :userServer="musicConfig.userServer"
        :playlistType="musicConfig.playlistType"
        :localMusic="localMusic"
        :remoteMusic="remoteMusic"
      />
    </div>
  </div>
</template>

<script>
import Music from "@/components/Music.vue";

export default {
  name: "MusicPlayer",
  components: {
    Music,
  },
  data() {
    return {
      musicConfig: {
        userId: "8152976493",
        userServer: "netease",
        playlistType: "playlist",
      },
      localMusic: [],
      remoteMusic: "",
    };
  },
  mounted() {
    // 进入音乐页面时隐藏滚动条
    document.body.style.overflow = 'hidden';
  },
  beforeDestroy() {
    // 离开音乐页面时恢复滚动条
    document.body.style.overflow = '';
  },
  beforeUnmount() {
    // Vue 3 兼容性，确保在组件卸载时恢复滚动条
    document.body.style.overflow = '';
  },
};
</script>

<style scoped>
.music-page {
  width: 100%;
  height: 92vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 音乐播放器容器 */
.music-player-container {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  opacity: 0;
  transform: translateY(30px) scale(0.95);
  animation: musicPlayerEnter 2s cubic-bezier(0.25, 0.8, 0.25, 1) forwards;
  animation-delay: 0.4s;
  border-radius: 15px;
}

@keyframes musicPlayerEnter {
  0% {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
    filter: blur(2px);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
    filter: blur(0);
  }
}
</style>
