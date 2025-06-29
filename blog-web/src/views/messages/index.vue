<template>
  <div class="modern-container">
    <!-- 动态背景 -->
    <div class="dynamic-background">
      <!-- 渐变球体 -->
      <div class="gradient-orbs">
        <div class="orb orb-1"></div>
        <div class="orb orb-2"></div>
        <div class="orb orb-3"></div>
        <div class="orb orb-4"></div>
        <div class="orb orb-5"></div>
      </div>

      <!-- 流动线条 -->
      <div class="flowing-lines">
        <svg class="line-svg" viewBox="0 0 1200 800">
          <path class="flowing-path" d="M0,300 Q300,100 600,300 T1200,300" />
          <path class="flowing-path" d="M0,500 Q400,200 800,500 T1200,500" />
          <path class="flowing-path" d="M0,400 Q200,600 600,400 T1200,400" />
        </svg>
      </div>

      <!-- 浮动粒子 -->
      <div class="floating-particles">
        <div class="particle" v-for="n in 20" :key="'particle-' + n"></div>
      </div>
    </div>

    <!-- 主界面 -->
    <div class="main-interface">
      <!-- 顶部导航 -->
      <div class="top-nav">
        <div class="nav-brand">
          <div class="brand-icon">
            <div class="icon-inner"></div>
          </div>
          <span class="brand-text">码云树洞</span>
        </div>
        <div class="nav-stats">
          <div class="stat-item">
            <span class="stat-number">{{ barrageList ? barrageList.length : 0 }}</span>
            <span class="stat-label">消息</span>
          </div>
        </div>
      </div>

      <!-- 中央内容区 -->
      <div class="central-content">
        <!-- 主标题 -->
        <div class="hero-section">
          <h1 class="hero-title">
            <span class="title-word">分享</span>
            <span class="title-word">你的想法</span>
          </h1>
          <p class="hero-subtitle">在这个温暖的数字空间里，每个声音都值得被听见</p>
          <div class="title-decoration">
            <div class="deco-line"></div>
            <div class="deco-dot"></div>
            <div class="deco-line"></div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-section">
          <div class="input-container" :class="{ 'focused': show }">
            <div class="input-glass">
              <div class="input-header">
                <span class="input-title">发送消息</span>
                <div class="input-indicator" :class="{ 'active': content.trim() }">
                  <div class="indicator-dot"></div>
                  <span class="indicator-text">{{ content.trim() ? '就绪' : '待机' }}</span>
                </div>
              </div>

              <div class="input-field-row">
                <div class="input-field">
                  <el-input class="glass-input" v-model="content" placeholder="在这里输入你的想法..."
                    @keyup.enter.native="addToList" @focus="show = true" @blur="show = false" maxlength="200"
                    show-word-limit type="textarea" :rows="3" resize="none"></el-input>
                  <div class="input-glow"></div>
                </div>

                <div class="send-button-container">
                  <el-button class="send-button" type="primary" @click="addToList" :disabled="!content.trim()"
                    :loading="sending">
                    <span class="button-content">
                      <i class="el-icon-position button-icon"></i>
                      <span class="button-text">发送</span>
                    </span>
                    <div class="button-ripple"></div>
                  </el-button>
                </div>
              </div>

              <div class="input-footer">
                <div class="cooldown-info" v-if="count">
                  <div class="cooldown-circle">
                    <svg class="cooldown-svg" viewBox="0 0 36 36">
                      <path class="cooldown-bg"
                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                      <path class="cooldown-progress" :style="cooldownProgressStyle"
                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                    </svg>
                    <span class="cooldown-number">{{ count }}</span>
                  </div>
                  <span class="cooldown-text">{{ count }}秒后可再次发送</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 消息流区域 -->
      <div class="message-stream-section">
        <div class="stream-header">
          <h3 class="stream-title">码云消息流</h3>
          <div class="stream-controls">
            <div class="control-item">
              <span class="control-label">速度</span>
              <div class="speed-slider" @click="handleSliderClick" ref="speedSlider">
                <div class="slider-track" :style="{ width: danmakuSpeed + '%' }"></div>
                <div class="slider-thumb" :style="{ left: danmakuSpeed + '%' }" @mousedown="startDrag"></div>
              </div>
              <span class="speed-value">{{ danmakuSpeed }}%</span>
            </div>
          </div>
        </div>

        <div class="stream-container">
          <vue-danmaku class="beautiful-danmaku" :danmus="barrageList.length > 0 ? barrageList : testData"
            :speeds="danmakuSpeed" :channels="8" :loop="true" :debounce="100" useSlot ref="danmaku">
            <template v-slot:dm="{ danmu }">
              <div class="beautiful-message">
                <div class="message-bubble">
                  <div class="message-avatar">
                    <img :src="danmu.avatar" alt="avatar" class="avatar-img" />
                    <div class="avatar-status"></div>
                  </div>
                  <div class="message-content">
                    <div class="message-header">
                      <span class="message-author">{{ danmu.nickname }}</span>
                      <span class="message-time">{{ formatTime() }}</span>
                    </div>
                    <div class="message-text">{{ danmu.content }}</div>
                  </div>
                  <div class="bubble-tail"></div>
                </div>
              </div>
            </template>
          </vue-danmaku>
        </div>
      </div>
    </div>

    <!-- 底部装饰 -->
    <div class="bottom-decoration">
      <div class="wave-container">
        <svg class="wave" viewBox="0 0 1200 120">
          <path d="M0,60 Q300,20 600,60 T1200,60 L1200,120 L0,120 Z" />
        </svg>
        <svg class="wave wave-2" viewBox="0 0 1200 120">
          <path d="M0,80 Q400,40 800,80 T1200,80 L1200,120 L0,120 Z" />
        </svg>
      </div>
    </div>
  </div>
</template>

<script>
import {addMessageApi, getMessagesApi} from "@/api/message";
import VueDanmaku from 'vue-danmaku';

export default {
  components: {
    VueDanmaku
  },

  data() {
    return {
      show: false,
      content: "",
      count: null,
      timer: null,
      sending: false,
      barrageList: [],
      user: this.$store.state.userInfo,
      danmakuSpeed: 50, // 弹幕速度，范围 10-100
      speedUpdateTimeout: null, // 速度更新防抖定时器
      testData: [
        {
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1&backgroundColor=b6e3f4',
          nickname: '梦想家',
          content: '每一个想法都值得被听见 ✨'
        },
        {
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=2&backgroundColor=c0aede',
          nickname: '探索者',
          content: '在数字世界中寻找真实的连接 🌟'
        },
        {
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=3&backgroundColor=d1d4f9',
          nickname: '思考者',
          content: '分享让世界变得更美好 💫'
        },
        {
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=4&backgroundColor=ffd93d',
          nickname: '创造者',
          content: '用文字编织美丽的故事 🎨'
        }
      ]
    };
  },

  computed: {
    cooldownProgressStyle() {
      const progress = this.count ? (30 - this.count) / 30 * 100 : 0;
      return {
        strokeDasharray: `${progress}, 100`
      };
    }
  },

  watch: {
    // 监听速度变化，实时更新弹幕速度
    danmakuSpeed: {
      handler(newSpeed) {
        // 清除之前的定时器
        if (this.speedUpdateTimeout) {
          clearTimeout(this.speedUpdateTimeout);
        }

        // 使用更平滑的方式更新速度
        this.updateDanmakuSpeedSmooth();
      },
      immediate: false
    }
  },

  mounted() {
    // 首先设置初始速度变量
    this.initSpeedVariables();

    this.listMessage();
    this.initAnimations();
    this.optimizePerformance();

    // 调试：检查弹幕组件
    this.$nextTick(() => {
      console.log('弹幕组件实例:', this.$refs.danmaku);
      console.log('测试数据:', this.testData);
      console.log('弹幕列表:', this.barrageList);
    });
  },

  beforeDestroy() {
    // 清理定时器
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
    if (this.speedUpdateTimeout) {
      clearTimeout(this.speedUpdateTimeout);
      this.speedUpdateTimeout = null;
    }
  },

  methods: {
    /**
     * 添加留言
     */
    async addToList() {
      if (this.count) {
        this.$message.warning("请稍等片刻再发送");
        return;
      }
      if (this.content.trim() === "") {
        this.$message.warning("请输入消息内容");
        return;
      }

      this.sending = true;

      const message = {
        avatar: this.user ? this.user.avatar : this.$store.state.webSiteInfo.touristAvatar,
        status: 1,
        nickname: this.user ? this.user.nickname : "游客",
        content: this.content
      };

      try {
        await addMessageApi(message);
        this.barrageList.push(message);
        this.content = "";
        this.$message.success("消息发送成功！");
        this.startCooldown();
      } catch (error) {
        this.$message.error("发送失败，请重试");
      } finally {
        this.sending = false;
      }
    },

    /**
     * 开始冷却
     */
    startCooldown() {
      const TIME_COUNT = 3;
      this.count = TIME_COUNT;
      this.timer = setInterval(() => {
        if (this.count > 0) {
          this.count--;
        } else {
          clearInterval(this.timer);
          this.timer = null;
        }
      }, 1000);
    },

    /**
     * 获取留言列表
     */
    listMessage() {
      getMessagesApi().then(res => {
        console.log('获取到的留言数据:', res.data);
        this.barrageList = res.data || [];
        console.log('设置后的barrageList:', this.barrageList);
        console.log('消息统计数量:', this.barrageList.length);
      }).catch(err => {
        console.error('获取留言失败:', err);
        // 如果获取失败，确保barrageList是空数组而不是undefined
        this.barrageList = [];
      });
    },

    /**
     * 格式化时间
     */
    formatTime() {
      const now = new Date();
      return now.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    /**
     * 初始化动画
     */
    initAnimations() {
      // 初始化粒子位置
      const particles = document.querySelectorAll('.particle');
      particles.forEach((particle, index) => {
        particle.style.left = Math.random() * 100 + '%';
        particle.style.top = Math.random() * 100 + '%';
        particle.style.animationDelay = Math.random() * 10 + 's';
        particle.style.animationDuration = (Math.random() * 20 + 15) + 's';
      });
    },

    /**
     * 处理滑块点击
     */
    handleSliderClick(event) {
      const slider = this.$refs.speedSlider;
      const rect = slider.getBoundingClientRect();
      const percentage = Math.min(100, Math.max(0, ((event.clientX - rect.left) / rect.width) * 100));
      this.danmakuSpeed = Math.round(percentage);
    },

    /**
     * 开始拖拽
     */
    startDrag(event) {
      event.preventDefault();
      const slider = this.$refs.speedSlider;
      const rect = slider.getBoundingClientRect();

      const onMouseMove = (e) => {
        const percentage = Math.min(100, Math.max(0, ((e.clientX - rect.left) / rect.width) * 100));
        this.danmakuSpeed = Math.round(percentage);
      };

      const onMouseUp = () => {
        document.removeEventListener('mousemove', onMouseMove);
        document.removeEventListener('mouseup', onMouseUp);
      };

      document.addEventListener('mousemove', onMouseMove);
      document.addEventListener('mouseup', onMouseUp);
    },

    /**
 * 初始化速度变量
 */
    initSpeedVariables() {
      try {
        const speedRatio = this.danmakuSpeed / 50;

        // 设置全局速度变量
        document.documentElement.style.setProperty('--danmaku-speed-ratio', speedRatio);
        document.documentElement.style.setProperty('--current-speed', speedRatio);

        console.log(`🎯 速度变量初始化完成：${this.danmakuSpeed}% (比例: ${speedRatio})`);
      } catch (error) {
        console.warn('速度变量初始化失败:', error);
      }
    },

    /**
     * 性能优化设置
     */
    optimizePerformance() {
      try {
        // 优化浏览器渲染性能
        requestAnimationFrame(() => {
          // 设置CSS自定义属性来优化动画
          document.documentElement.style.setProperty('--gpu-acceleration', 'true');

          // 强制启用硬件加速
          const container = document.querySelector('.modern-container');
          if (container) {
            container.style.transform = 'translate3d(0, 0, 0)';
            container.style.willChange = 'transform';
          }

          // 优化弹幕容器
          const danmakuContainer = document.querySelector('.beautiful-danmaku');
          if (danmakuContainer) {
            danmakuContainer.style.willChange = 'scroll-position, contents';
            danmakuContainer.style.contain = 'layout style paint';
          }

          console.log('🚀 性能优化设置已启用');

        });
      } catch (error) {
        console.warn('性能优化设置失败:', error);
      }
    },
    /**
     * 立即更新CSS动画速度（平滑过渡）
     */
    updateDanmakuSpeedSmooth() {
      try {
        // 计算速度比例
        const speedRatio = this.danmakuSpeed / 50; // 基准是50%

        // 直接更新CSS变量，让浏览器自动处理动画速度变化
        document.documentElement.style.setProperty('--danmaku-speed-ratio', speedRatio);
        document.documentElement.style.setProperty('--current-speed', speedRatio);

        console.log(`✅ 弹幕速度平滑调整：${this.danmakuSpeed}% (比例: ${speedRatio.toFixed(2)}) - 使用CSS变量无卡顿控制`);

      } catch (error) {
        console.warn('❌ 平滑更新弹幕速度失败:', error);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
// 全局CSS变量定义（用于控制弹幕速度）
:root {
  --danmaku-speed-ratio: 1;
  --current-speed: 1;
  --base-duration: 15s;
}

// 主容器（性能优化版）
.modern-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  // 性能优化设置
  contain: layout style paint;
  will-change: scroll-position;
  transform: translateZ(0);
  backface-visibility: hidden;

  // 优化渲染性能
  * {
    box-sizing: border-box;
  }

  // 针对弹幕优化字体渲染
  .danmaku-item * {
    text-rendering: optimizeSpeed !important;
    font-smooth: never !important;
    -webkit-font-smoothing: none !important;
    -moz-osx-font-smoothing: unset !important;
  }

  // 全局动画性能优化
  * {
    // 优化动画性能
    animation-fill-mode: both;
    backface-visibility: hidden;
  }

  // 专门针对弹幕的性能优化
  .danmaku-item,
  .beautiful-message {
    // 强制GPU加速
    transform: translate3d(0, 0, 0) !important;
    will-change: transform !important;
    backface-visibility: hidden !important;
    perspective: 1000px !important;

    // 优化动画性能
    animation-fill-mode: both !important;
    animation-timing-function: linear !important;

    // 减少重绘
    contain: layout style paint !important;
    pointer-events: none !important;

    // 字体渲染优化
    text-rendering: optimizeSpeed !important;
    -webkit-font-smoothing: none !important;
    font-smooth: never !important;
  }
}

// 动态背景
.dynamic-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;

  // 渐变球体
  .gradient-orbs {
    position: absolute;
    width: 100%;
    height: 100%;

    .orb {
      position: absolute;
      border-radius: 50%;
      filter: blur(60px);
      animation: float 20s ease-in-out infinite;

      &.orb-1 {
        width: 300px;
        height: 300px;
        background: radial-gradient(circle, rgba(255, 182, 193, 0.6) 0%, transparent 70%);
        top: 10%;
        left: 10%;
        animation-delay: 0s;
      }

      &.orb-2 {
        width: 250px;
        height: 250px;
        background: radial-gradient(circle, rgba(173, 216, 230, 0.6) 0%, transparent 70%);
        top: 60%;
        right: 20%;
        animation-delay: 5s;
      }

      &.orb-3 {
        width: 200px;
        height: 200px;
        background: radial-gradient(circle, rgba(255, 218, 185, 0.6) 0%, transparent 70%);
        bottom: 20%;
        left: 30%;
        animation-delay: 10s;
      }

      &.orb-4 {
        width: 180px;
        height: 180px;
        background: radial-gradient(circle, rgba(221, 160, 221, 0.6) 0%, transparent 70%);
        top: 30%;
        right: 40%;
        animation-delay: 15s;
      }

      &.orb-5 {
        width: 220px;
        height: 220px;
        background: radial-gradient(circle, rgba(152, 251, 152, 0.6) 0%, transparent 70%);
        top: 70%;
        left: 60%;
        animation-delay: 7s;
      }
    }
  }

  // 流动线条
  .flowing-lines {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    opacity: 0.4;

    .line-svg {
      width: 100%;
      height: 100%;

      .flowing-path {
        fill: none;
        stroke: rgba(255, 255, 255, 0.8);
        stroke-width: 2;
        stroke-dasharray: 10, 5;
        animation: flow 8s linear infinite;
      }
    }
  }

  // 浮动粒子
  .floating-particles {
    position: absolute;
    width: 100%;
    height: 100%;

    .particle {
      position: absolute;
      width: 6px;
      height: 6px;
      background: rgba(255, 255, 255, 0.8);
      border-radius: 50%;
      animation: particle-float 15s linear infinite;
    }
  }
}

// 主界面
.main-interface {
  position: relative;
  z-index: 10;
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 80px 20px 20px 20px;
  box-sizing: border-box;
}

// 顶部导航
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 20;

  .nav-brand {
    display: flex;
    align-items: center;
    gap: 15px;

    .brand-icon {
      width: 40px;
      height: 40px;
      background: linear-gradient(45deg, #ff9a9e, #fecfef);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      animation: icon-pulse 3s ease-in-out infinite;
      box-shadow: 0 4px 15px rgba(255, 154, 158, 0.4);

      .icon-inner {
        width: 20px;
        height: 20px;
        background: white;
        border-radius: 6px;
      }
    }

    .brand-text {
      font-size: 24px;
      font-weight: 700;
      color: white;
      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
    }
  }

  .nav-stats {
    display: flex;
    gap: 30px;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      color: white;

      .stat-number {
        font-size: 20px;
        font-weight: 700;
        margin-bottom: 4px;
        text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
      }

      .stat-label {
        font-size: 12px;
        opacity: 0.9;
      }
    }
  }
}

// 中央内容
.central-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
  position: relative;
  z-index: 15;
}

// 主标题区
.hero-section {
  text-align: center;
  margin-bottom: 50px;

  .hero-title {
    font-size: 3.5rem;
    font-weight: 800;
    margin: 0 0 20px 0;
    color: white;
    text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);

    .title-word {
      display: inline-block;
      margin: 0 15px;
      position: relative;
      animation: title-glow 4s ease-in-out infinite alternate;

      &:nth-child(2) {
        animation-delay: 2s;
      }
    }
  }

  .hero-subtitle {
    font-size: 1.2rem;
    color: rgba(255, 255, 255, 0.9);
    margin: 0 0 30px 0;
    font-weight: 300;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  }

  .title-decoration {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 20px;

    .deco-line {
      width: 60px;
      height: 2px;
      background: linear-gradient(90deg, transparent, white, transparent);
      animation: line-glow 2s ease-in-out infinite alternate;
    }

    .deco-dot {
      width: 8px;
      height: 8px;
      background: white;
      border-radius: 50%;
      animation: dot-pulse 2s ease-in-out infinite;
      box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
    }
  }
}

// 输入区域
.input-section {
  width: 100%;
  margin-bottom: 40px;
  position: relative;
  z-index: 20;

  .input-container {
    transition: all 0.3s ease;

    &.focused {
      transform: translateY(-5px);
    }

    .input-glass {
      background: rgba(255, 255, 255, 0.25);
      backdrop-filter: blur(20px);
      border-radius: 24px;
      border: 1px solid rgba(255, 255, 255, 0.3);
      padding: 30px;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);

      .input-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;

        .input-title {
          font-size: 18px;
          font-weight: 600;
          color: white;
          text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
        }

        .input-indicator {
          display: flex;
          align-items: center;
          gap: 8px;
          color: rgba(255, 255, 255, 0.8);
          font-size: 14px;

          .indicator-dot {
            width: 8px;
            height: 8px;
            background: rgba(255, 255, 255, 0.5);
            border-radius: 50%;
            transition: all 0.3s ease;
          }

          &.active .indicator-dot {
            background: #4ade80;
            box-shadow: 0 0 10px rgba(74, 222, 128, 0.5);
          }
        }
      }

      .input-field-row {
        display: flex;
        gap: 15px;
        align-items: flex-end;
        margin-bottom: 20px;

        .input-field {
          flex: 1;
          position: relative;

          .glass-input {
            :deep(.el-textarea__inner) {
              background: rgba(255, 255, 255, 0.9) !important;
              border: 1px solid rgba(255, 255, 255, 0.3) !important;
              border-radius: 16px !important;
              color: #333 !important;
              font-size: 16px !important;
              padding: 20px !important;
              line-height: 1.5 !important;
              resize: none !important;
              transition: all 0.3s ease !important;
              z-index: 10 !important;
              position: relative !important;

              &::placeholder {
                color: rgba(102, 102, 102, 0.8) !important;
              }

              &:focus {
                border-color: rgba(255, 255, 255, 0.8) !important;
                box-shadow: 0 0 20px rgba(255, 255, 255, 0.3) !important;
                background: rgba(255, 255, 255, 0.95) !important;
                outline: none !important;
              }
            }

            :deep(.el-input__count) {
              color: rgba(102, 102, 102, 0.7) !important;
              background: transparent !important;
              bottom: 8px !important;
              right: 15px !important;
            }
          }

          .input-glow {
            position: absolute;
            top: -2px;
            left: -2px;
            right: -2px;
            bottom: -2px;
            background: linear-gradient(45deg, #ff9a9e, #fecfef, #a8edea, #fed6e3);
            border-radius: 18px;
            opacity: 0;
            z-index: -1;
            transition: opacity 0.3s ease;
          }

          &:focus-within .input-glow {
            opacity: 0.4;
            animation: glow-pulse 2s ease-in-out infinite;
          }
        }

        .send-button-container {
          .send-button {
            background: linear-gradient(45deg, #ff9a9e, #fecfef) !important;
            border: none !important;
            border-radius: 16px !important;
            padding: 20px 24px !important;
            color: white !important;
            font-weight: 600 !important;
            font-size: 16px !important;
            position: relative !important;
            overflow: hidden !important;
            transition: all 0.3s ease !important;
            box-shadow: 0 4px 15px rgba(255, 154, 158, 0.4) !important;
            min-height: 80px !important;
            z-index: 10 !important;

            &:hover:not(:disabled) {
              transform: translateY(-2px) !important;
              box-shadow: 0 6px 20px rgba(255, 154, 158, 0.6) !important;
            }

            &:disabled {
              opacity: 0.6 !important;
              cursor: not-allowed !important;
            }

            .button-content {
              display: flex !important;
              flex-direction: column !important;
              align-items: center !important;
              gap: 6px !important;
              position: relative !important;
              z-index: 2 !important;

              .button-icon {
                font-size: 20px !important;
              }

              .button-text {
                font-size: 14px !important;
              }
            }

            .button-ripple {
              position: absolute !important;
              top: 50% !important;
              left: 50% !important;
              width: 0 !important;
              height: 0 !important;
              background: rgba(255, 255, 255, 0.3) !important;
              border-radius: 50% !important;
              transform: translate(-50%, -50%) !important;
              transition: all 0.6s ease !important;
            }

            &:active .button-ripple {
              width: 300px !important;
              height: 300px !important;
            }
          }
        }
      }

      .input-footer {
        .cooldown-info {
          display: flex;
          align-items: center;
          gap: 12px;
          color: rgba(255, 255, 255, 0.8);

          .cooldown-circle {
            position: relative;
            width: 40px;
            height: 40px;

            .cooldown-svg {
              width: 100%;
              height: 100%;
              transform: rotate(-90deg);

              .cooldown-bg {
                fill: none;
                stroke: rgba(255, 255, 255, 0.3);
                stroke-width: 3;
              }

              .cooldown-progress {
                fill: none;
                stroke: #ff9a9e;
                stroke-width: 3;
                stroke-linecap: round;
                transition: stroke-dasharray 0.3s ease;
              }
            }

            .cooldown-number {
              position: absolute;
              top: 50%;
              left: 50%;
              transform: translate(-50%, -50%);
              font-size: 14px;
              font-weight: 600;
              color: white;
            }
          }

          .cooldown-text {
            font-size: 14px;
          }
        }
      }
    }
  }
}

// 消息流区域
.message-stream-section {
  position: absolute;
  top: 280px;
  left: 0;
  right: 0;
  bottom: 100px;
  padding: 0 20px;
  z-index: 5;

  .stream-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 0 20px;

    .stream-title {
      font-size: 20px;
      font-weight: 600;
      color: white;
      margin: 0;
      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
    }

    .stream-controls {
      display: flex;
      gap: 20px;

      .control-item {
        display: flex;
        align-items: center;
        gap: 10px;

        .control-label {
          font-size: 14px;
          color: rgba(255, 255, 255, 0.9);
        }

        .speed-slider {
          width: 100px;
          height: 4px;
          background: rgba(255, 255, 255, 0.3);
          border-radius: 2px;
          position: relative;
          cursor: pointer;
          transition: all 0.3s ease;
          // 性能优化
          will-change: background-color;
          contain: layout style;

          &:hover {
            background: rgba(255, 255, 255, 0.4);
          }

          .slider-track {
            height: 100%;
            background: linear-gradient(90deg, #ff9a9e, #fecfef);
            border-radius: 2px;
            transition: width 0.2s ease;
            // 性能优化
            will-change: width;
            contain: layout;
          }

          .slider-thumb {
            position: absolute;
            top: -4px;
            width: 12px;
            height: 12px;
            background: white;
            border-radius: 50%;
            box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
            cursor: grab;
            transition: all 0.2s ease;
            transform: translateX(-50%);
            // 性能优化
            will-change: transform;
            backface-visibility: hidden;
            contain: layout style;

            &:hover {
              transform: translateX(-50%) scale(1.1);
              box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
            }

            &:active {
              cursor: grabbing;
              transform: translateX(-50%) scale(1.05);
            }
          }
        }

        .speed-value {
          font-size: 12px;
          color: rgba(255, 255, 255, 0.9);
          margin-left: 8px;
          min-width: 30px;
        }
      }
    }
  }

  .stream-container {
    height: 100%;

    .beautiful-danmaku {
      height: 100%;
      width: 100%;
      // 性能优化：隔离重排重绘
      contain: layout style paint;

      :deep(.danmaku-container) {
        height: 100% !important;
        width: 100% !important;
        position: relative !important;
        overflow: hidden !important;
        // 性能优化
        contain: layout style paint !important;
        transform: translateZ(0) !important;
      }

      :deep(.danmaku-item) {
        display: inline-block !important;
        white-space: nowrap !important;
        // GPU加速优化
        will-change: transform !important;
        transform: translate3d(0, 0, 0) !important;
        backface-visibility: hidden !important;
        perspective: 1000px !important;
        // 动画设置 - 使用CSS变量控制速度
        animation-duration: calc(var(--base-duration) / var(--current-speed)) !important;
        animation-timing-function: linear !important;
        animation-fill-mode: both !important;
        // 性能优化：隔离重排重绘
        contain: layout style paint !important;
        // 优化渲染
        image-rendering: optimizeSpeed !important;
        pointer-events: none !important; // 弹幕不参与交互，提升性能
        // 移除过渡效果，避免干扰动画
        // transition: animation-duration 0.3s ease !important;
      }

      :deep(.danmaku-channel) {
        height: auto !important;
        min-height: 60px !important;
        margin-bottom: 10px !important;
        // 性能优化
        contain: layout !important;
      }
    }
  }
}

// 美丽的消息（性能优化版）
.beautiful-message {
  display: inline-block !important;
  // 性能优化
  contain: layout style paint !important;

  .message-bubble {
    display: inline-flex !important;
    align-items: flex-start !important;
    // 背景优化：移除耗费性能的backdrop-filter
    background: rgba(255, 255, 255, 0.95) !important;
    // backdrop-filter: blur(10px) !important; // 注释掉，性能杀手
    border-radius: 20px !important;
    padding: 12px 16px !important;
    margin: 5px 0 !important;
    // 阴影优化：使用更轻量的阴影
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08) !important;
    max-width: 350px !important;
    min-width: 200px !important;
    position: relative !important;
    border: 1px solid rgba(255, 255, 255, 0.6) !important;
    white-space: nowrap !important;
    // GPU加速优化
    will-change: transform !important;
    transform: translate3d(0, 0, 0) !important;
    backface-visibility: hidden !important;
    // 性能优化
    contain: layout style paint !important;
    pointer-events: none !important; // 不参与交互，提升性能
    animation: none !important;

    .message-avatar {
      position: relative;
      margin-right: 12px;
      flex-shrink: 0;
      // 性能优化
      contain: layout !important;

      .avatar-img {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        border: 3px solid white;
        // 阴影优化：使用更轻量的阴影
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
        // GPU加速
        transform: translateZ(0);
        backface-visibility: hidden;
        // 图片渲染优化
        image-rendering: optimizeSpeed;
      }

      .avatar-status {
        position: absolute;
        bottom: 2px;
        right: 2px;
        width: 12px;
        height: 12px;
        background: #4ade80;
        border: 2px solid white;
        border-radius: 50%;
        // GPU加速
        transform: translateZ(0);
        backface-visibility: hidden;
      }
    }

    .message-content {
      flex: 1;
      min-width: 0;
      white-space: nowrap !important;
      // 性能优化
      contain: layout style !important;

      .message-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;
        // 性能优化
        contain: layout !important;

        .message-author {
          font-size: 12px;
          font-weight: 600;
          color: #374151;
          margin-right: 8px;
        }

        .message-time {
          font-size: 10px;
          color: #9ca3af;
        }
      }

      .message-text {
        font-size: 14px;
        line-height: 1.3;
        color: #4b5563;
        white-space: nowrap !important;
        overflow: visible !important;
      }
    }

    .bubble-tail {
      position: absolute;
      left: -8px;
      top: 20px;
      width: 0;
      height: 0;
      border-top: 8px solid transparent;
      border-bottom: 8px solid transparent;
      border-right: 8px solid rgba(255, 255, 255, 0.95);
    }
  }
}

// 底部装饰
.bottom-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 5;

  .wave-container {
    position: relative;
    height: 120px;

    .wave {
      position: absolute;
      bottom: 0;
      width: 100%;
      height: 120px;
      fill: rgba(255, 255, 255, 0.2);
      animation: wave-move 8s ease-in-out infinite;

      &.wave-2 {
        fill: rgba(255, 255, 255, 0.1);
        animation-delay: 2s;
        animation-duration: 10s;
      }
    }
  }
}

// 动画定义
@keyframes float {

  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }

  33% {
    transform: translateY(-20px) rotate(120deg);
  }

  66% {
    transform: translateY(10px) rotate(240deg);
  }
}

@keyframes flow {
  0% {
    stroke-dashoffset: 0;
  }

  100% {
    stroke-dashoffset: 30;
  }
}

@keyframes particle-float {
  0% {
    transform: translateY(100vh) translateX(0px);
    opacity: 0;
  }

  10% {
    opacity: 1;
  }

  90% {
    opacity: 1;
  }

  100% {
    transform: translateY(-100px) translateX(100px);
    opacity: 0;
  }
}

@keyframes icon-pulse {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.05);
  }
}

@keyframes title-glow {
  0% {
    text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  }

  100% {
    text-shadow: 0 4px 30px rgba(255, 255, 255, 0.4);
  }
}

@keyframes line-glow {
  0% {
    opacity: 0.6;
  }

  100% {
    opacity: 1;
  }
}

@keyframes dot-pulse {

  0%,
  100% {
    transform: scale(1);
    opacity: 0.8;
  }

  50% {
    transform: scale(1.2);
    opacity: 1;
  }
}

@keyframes glow-pulse {

  0%,
  100% {
    opacity: 0.4;
  }

  50% {
    opacity: 0.7;
  }
}

@keyframes message-appear {
  0% {
    opacity: 0;
    transform: translateX(-20px) scale(0.9);
  }

  100% {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

@keyframes wave-move {

  0%,
  100% {
    transform: translateX(0px);
  }

  50% {
    transform: translateX(-50px);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .hero-section .hero-title {
    font-size: 2.5rem;
  }

  .top-nav {
    padding: 15px 20px;

    .nav-brand .brand-text {
      font-size: 20px;
    }

    .nav-stats {
      gap: 20px;
    }
  }

  .input-section .input-container .input-glass {
    padding: 20px;

    .input-field-row {
      flex-direction: column;
      gap: 15px;

      .send-button-container .send-button {
        width: 100%;
        min-height: 60px;

        .button-content {
          flex-direction: row;
          gap: 10px;
        }
      }
    }
  }

  .beautiful-message .message-bubble {
    max-width: 300px;
    padding: 12px 16px;
  }
}

@media (max-width: 480px) {
  .hero-section .hero-title {
    font-size: 2rem;
  }

  .main-interface {
    padding: 15px;
  }

  .top-nav {
    flex-direction: column;
    gap: 15px;
    text-align: center;

    .nav-stats {
      justify-content: center;
      gap: 30px;

      .stat-item {
        .stat-number {
          font-size: 18px;
        }

        .stat-label {
          font-size: 11px;
        }
      }
    }
  }

  .stream-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;

    .stream-controls .control-item {
      justify-content: center;

      .speed-slider {
        width: 80px;
      }
    }
  }

  .beautiful-message .message-bubble {
    max-width: 250px;
  }
}
</style>