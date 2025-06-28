<template>
  <div class="moments-list" v-if="moments.length > 0">
    <div class="hologram-effect">
      <div class="scan-lines"></div>
    </div>
    <div class="moments-content">
      <div class="moments-row">
        <div class="moments-header">
          <div class="icon-wrapper">
            <i class="fas fa-comment-dots"></i>
            <div class="icon-pulse"></div>
          </div>
          <span class="header-text">最新说说:</span>
          <div class="header-line"></div>
        </div>
        <transition name="cyber-fade" mode="out-in">
          <div class="moment-item" :key="currentIndex">
            <div class="moment-wrapper">
              <span class="moment-text" @click="goToMoments" v-html="moments[currentIndex].content" />
              <div class="text-glow"></div>
            </div>
          </div>
        </transition>
      </div>
    </div>
    <div class="corner-decorations">
      <div class="corner corner-tl"></div>
      <div class="corner corner-tr"></div>
      <div class="corner corner-bl"></div>
      <div class="corner corner-br"></div>
    </div>
  </div>
</template>

<script>
import { getMoments } from '@/api/moments'

export default {
  name: 'MomentsList',
  data() {
    return {
      moments: [],
      currentIndex: 0,
      timer: null
    }
  },
  created() {
    this.getMomentsList()
  },
  mounted() {
    this.startRotation()
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    getMomentsList() {
      getMoments({ pageSize: 5, pageNum: 1 }).then(res => {
        if (res.data && res.data.records) {
          this.moments = res.data.records
          this.startRotation()
        }
      })
    },
    goToMoments() {
      this.$router.push('/moments')
    },
    startRotation() {
      if (this.timer) {
        clearInterval(this.timer)
      }
      this.timer = setInterval(() => {
        this.currentIndex = (this.currentIndex + 1) % this.moments.length
      }, 4000) // 每4秒切换一次
    }
  }
}
</script>

<style lang="scss" scoped>
.moments-list {
  margin-top: 30px;
  position: relative;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.95) 0%,
    rgba(248, 250, 252, 0.9) 50%,
    rgba(255, 255, 255, 0.95) 100%);
  border-radius: 12px;
  padding: $spacing-md;
  margin-bottom: $spacing-lg;
  border: 1px solid rgba(0, 212, 255, 0.25);
  box-shadow: 
    0 0 20px rgba(0, 212, 255, 0.1),
    inset 0 0 20px rgba(0, 212, 255, 0.03);
  overflow: hidden;
  transition: all 0.3s ease;
  backdrop-filter: blur(15px);
  
  &::before {
    content: '';
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    background: linear-gradient(45deg, 
      transparent, 
      rgba(0, 212, 255, 0.2), 
      transparent, 
      rgba(0, 102, 255, 0.2), 
      transparent);
    background-size: 300% 300%;
    border-radius: 17px;
    z-index: -1;
    animation: borderPulse 4s ease-in-out infinite;
    opacity: 0;
    transition: opacity 0.3s ease;
  }
  
  &:hover {
    box-shadow: 
      0 0 40px rgba(0, 212, 255, 0.2),
      inset 0 0 30px rgba(0, 212, 255, 0.05);
    transform: translateY(-2px);
    
    &::before {
      opacity: 1;
    }
  }
}

@keyframes borderPulse {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

// 全息投影效果
.hologram-effect {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
  
  .scan-lines {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: repeating-linear-gradient(
      0deg,
      transparent,
      transparent 2px,
      rgba(0, 212, 255, 0.02) 2px,
      rgba(0, 212, 255, 0.02) 4px
    );
    animation: scanMove 2s linear infinite;
  }
}

@keyframes scanMove {
  0% {
    transform: translateY(-10px);
  }
  100% {
    transform: translateY(10px);
  }
}

// 角落装饰
.corner-decorations {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 2;
  
  .corner {
    position: absolute;
    width: 16px;
    height: 16px;
    
    &::before,
    &::after {
      content: '';
      position: absolute;
      background: currentColor;
      transition: all 0.3s ease;
    }
    
    &.corner-tl {
      top: 8px;
      left: 8px;
      color: #00d4ff;
      
      &::before {
        top: 0;
        left: 0;
        width: 16px;
        height: 2px;
      }
      
      &::after {
        top: 0;
        left: 0;
        width: 2px;
        height: 16px;
      }
      
      animation: cornerGlow 3s ease-in-out infinite;
    }
    
    &.corner-tr {
      top: 8px;
      right: 8px;
      color: #0066ff;
      
      &::before {
        top: 0;
        right: 0;
        width: 16px;
        height: 2px;
      }
      
      &::after {
        top: 0;
        right: 0;
        width: 2px;
        height: 16px;
      }
      
      animation: cornerGlow 3s ease-in-out infinite 0.5s;
    }
    
    &.corner-bl {
      bottom: 8px;
      left: 8px;
      color: #4c6ef5;
      
      &::before {
        bottom: 0;
        left: 0;
        width: 16px;
        height: 2px;
      }
      
      &::after {
        bottom: 0;
        left: 0;
        width: 2px;
        height: 16px;
      }
      
      animation: cornerGlow 3s ease-in-out infinite 1s;
    }
    
    &.corner-br {
      bottom: 8px;
      right: 8px;
      color: #339af0;
      
      &::before {
        bottom: 0;
        right: 0;
        width: 16px;
        height: 2px;
      }
      
      &::after {
        bottom: 0;
        right: 0;
        width: 2px;
        height: 16px;
      }
      
      animation: cornerGlow 3s ease-in-out infinite 1.5s;
    }
  }
}

@keyframes cornerGlow {
  0%, 100% {
    opacity: 0.6;
    filter: brightness(1);
    transform: scale(1);
  }
  50% {
    opacity: 1;
    filter: brightness(1.3) drop-shadow(0 0 8px currentColor);
    transform: scale(1.05);
  }
}

.moments-content {
  position: relative;
  z-index: 3;
  
  .moments-row {
    display: flex;
    align-items: center;
    gap: $spacing-md;
  }
}

.moments-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  color: rgba(30, 41, 59, 0.9);
  font-weight: 500;
  white-space: nowrap;
  font-size: 1.2em;
  position: relative;
  
  .icon-wrapper {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    
    i {
      color: #00d4ff;
      font-size: 1.1em;
      text-shadow: 0 0 5px rgba(0, 212, 255, 0.4);
      z-index: 2;
      position: relative;
    }
    
    .icon-pulse {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 24px;
      height: 24px;
      border: 2px solid rgba(0, 212, 255, 0.3);
      border-radius: 50%;
      animation: iconPulse 2s ease-in-out infinite;
    }
  }
  
  .header-text {
    background: linear-gradient(45deg, #00d4ff, #0066ff);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    text-shadow: 0 0 5px rgba(0, 212, 255, 0.2);
    font-weight: 600;
  }
  
  .header-line {
    flex: 1;
    height: 1px;
    background: linear-gradient(90deg, 
      rgba(0, 212, 255, 0.4), 
      rgba(0, 212, 255, 0.15), 
      transparent);
    margin-left: $spacing-sm;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      top: -1px;
      left: 0;
      width: 30px;
      height: 3px;
      background: linear-gradient(90deg, #00d4ff, transparent);
      animation: lineFlow 2s ease-in-out infinite;
    }
  }
}

@keyframes iconPulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.5;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0.8;
  }
}

@keyframes lineFlow {
  0% {
    transform: translateX(-30px);
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  100% {
    transform: translateX(100px);
    opacity: 0;
  }
}

.moment-item {
  flex: 1;
  display: flex;
  align-items: center;
  min-width: 0;
  
  .moment-wrapper {
    position: relative;
    width: 100%;
    padding: 6px 10px;
    background: rgba(248, 250, 252, 0.8);
    border: 1px solid rgba(0, 212, 255, 0.15);
    border-radius: 6px;
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(135deg, 
        rgba(0, 212, 255, 0.05), 
        transparent, 
        rgba(0, 102, 255, 0.05));
      border-radius: 10px;
      opacity: 0;
      transition: opacity 0.3s ease;
    }
    
    &:hover {
      border-color: rgba(0, 212, 255, 0.4);
      box-shadow: 
        0 0 20px rgba(0, 212, 255, 0.15),
        inset 0 0 20px rgba(0, 212, 255, 0.05);
      transform: translateX(5px);
      
      &::before {
        opacity: 1;
      }
      
      .text-glow {
        opacity: 1;
      }
    }
    
    .moment-text {
      color: rgba(71, 85, 105, 0.8);
      white-space: normal;
      overflow: hidden;
      text-overflow: ellipsis;
      width: 100%;
      cursor: pointer;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      line-height: 1.3;
      font-size: 0.75em;
      max-height: 2.6em;
      position: relative;
      z-index: 2;
      transition: color 0.3s ease;
      
      &:hover {
        color: #00d4ff;
        text-shadow: 0 0 3px rgba(0, 212, 255, 0.2);
      }
    }
    
    .text-glow {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: radial-gradient(circle, 
        rgba(0, 212, 255, 0.08) 0%, 
        transparent 70%);
      border-radius: 10px;
      opacity: 0;
      transition: opacity 0.3s ease;
      pointer-events: none;
    }
  }
}

// 科幻过渡动画
.cyber-fade-enter-active,
.cyber-fade-leave-active {
  transition: all 0.6s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.cyber-fade-enter-from {
  opacity: 0;
  transform: translateX(30px) scale(0.95);
  filter: blur(2px);
}

.cyber-fade-leave-to {
  opacity: 0;
  transform: translateX(-30px) scale(0.95);
  filter: blur(2px);
}

.cyber-fade-enter-to,
.cyber-fade-leave-from {
  opacity: 1;
  transform: translateX(0) scale(1);
  filter: blur(0);
}

// 响应式调整
@media (max-width: 768px) {
  .moments-list {
    padding: $spacing-sm;
  }
  
  .moments-header {
    font-size: 1.1em;
    
    .icon-wrapper {
      width: 28px;
      height: 28px;
      
      i {
        font-size: 1em;
      }
      
      .icon-pulse {
        width: 20px;
        height: 20px;
      }
    }
  }
  
  .moment-wrapper {
    padding: 5px 8px;
    
    .moment-text {
      font-size: 0.7em;
      line-height: 1.2;
    }
  }
  
  .corner-decorations .corner {
    width: 12px;
    height: 12px;
  }
}
</style>
