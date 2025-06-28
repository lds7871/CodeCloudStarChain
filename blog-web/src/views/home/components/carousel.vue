<template>
  <div class="carousel-wrapper">
    <div class="digital-rain">
      <div v-for="n in 20" :key="n" class="rain-column">
        <span v-for="m in 15" :key="m" class="rain-char">{{ getRandomChar() }}</span>
      </div>
    </div>
    <el-carousel 
      v-if="processedSlides && processedSlides.length" 
      :interval="5000"
      class="custom-carousel"
    >
      <el-carousel-item v-for="(slide, index) in processedSlides" :key="index">
        <div class="slide-container">
          <!-- 猜你喜欢标签 - 只在匹配的文章上显示 -->
          <div v-if="isArticleMatched(slide)" class="recommend-label">
            <i class="fas fa-heart"></i>
            <span>猜你喜欢</span>
          </div>
          <!-- 拓展视野标签 - 只在随机选择的文章上显示 -->
          <div v-else-if="isExpandHorizonArticle(slide)" class="expand-label">
            <i class="fas fa-telescope"></i>
            <span>拓展视野</span>
          </div>
          <div class="image-wrapper">
            <img
                :src="slide.cover"
                :key="slide.cover"
                :alt="slide.title"
                @error="handleImageError"
            >
            <div class="image-overlay"></div>
            <div class="scan-line"></div>
          </div>
          <div class="slide-content">
            <div class="content-border">
              <div class="header-row">
                <h3 class="glitch-text" :data-text="slide.title">{{ slide.title }}</h3>
                <button class="cyber-button" @click="$emit('click', slide.id)">
                  <div class="button-stats">
                    <span class="stat-item">
                      <i class="fas fa-eye"></i>
                      {{ slide.quantity || 0 }}
                    </span>
                    <span class="stat-item">
                      <i class="fas fa-heart"></i>
                      {{ slide.likeNum || 0 }}
                    </span>
                    <span class="stat-item">
                      <i class="fas fa-comment"></i>
                      {{ slide.commentNum || 0 }}
                    </span>
                  </div>
                  <span class="button-divider">|</span>
                  <span class="button-text">阅读更多</span>
                  <div class="button-glow"></div>
                  <i class="fas fa-arrow-right"></i>
                </button>
              </div>
              
              <!-- 分类和标签信息 -->
              <div class="meta-info">
                <div class="category-info" v-if="slide.categoryName">
                  <i class="fas fa-folder"></i>
                  <span>{{ slide.categoryName }}</span>
                </div>
                <div class="tags-info" v-if="slide.tags && slide.tags.length">
                  <i class="fas fa-tags"></i>
                  <span v-for="tag in slide.tags.slice(0, 3)" :key="tag.id" class="tag-item">
                    {{ tag.name }}
                  </span>
                  <span v-if="slide.tags.length > 3" class="more-tags">+{{ slide.tags.length - 3 }}</span>
                </div>
              </div>
              
              <p class="description">{{ slide.summary || slide.description }}</p>
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script>
export default {
  name: 'Carousel',
  data() {
    return {
      randomSeed: Math.random() // 用于触发随机文章重新计算
    }
  },
  props: {
    slides: {
      type: Array,
      required: true
    },
    defaultImage: {
      type: String,
      default: ''
    }
  },
  computed: {
    // 获取用户喜欢的标签
    userLikeTags() {
      try {
        const storedTags = localStorage.getItem('user_like_tags')
        return storedTags ? JSON.parse(storedTags) : []
      } catch (error) {
        console.warn('解析用户喜欢的标签时出错:', error)
        return []
      }
    },
    // 处理后的轮播图数据：猜你喜欢的文章 + 随机一篇其他文章
    processedSlides() {
      if (!this.slides || !this.slides.length) {
        return []
      }

      // 分离匹配和不匹配的文章
      const matchedArticles = []
      const unmatchedArticles = []

      this.slides.forEach(slide => {
        if (this.isArticleMatched(slide)) {
          matchedArticles.push(slide)
        } else {
          unmatchedArticles.push(slide)
        }
      })

      // 从不匹配的文章中随机选择一篇（使用randomSeed来触发重新计算）
      const randomArticle = unmatchedArticles.length > 0 
        ? [unmatchedArticles[Math.floor((Math.random() + this.randomSeed) * unmatchedArticles.length) % unmatchedArticles.length]]
        : []

      // 合并匹配的文章和随机文章
      return [...matchedArticles, ...randomArticle]
    }
  },
  watch: {
    // 当slides数据变化时，重新生成随机种子
    slides: {
      handler() {
        this.randomSeed = Math.random()
      },
      deep: true
    }
  },
  methods: {
    handleImageError(e) {
      if (this.defaultImage) {
        e.target.src = this.defaultImage
        console.log("推荐一栏显示默认图片")
      }
    },
    getRandomChar() {
      const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*()_+-=[]{}|;:,.<>?'
      return chars.charAt(Math.floor(Math.random() * chars.length))
    },
    // 检查单篇文章是否有匹配的标签
    isArticleMatched(slide) {
      if (!this.userLikeTags.length || !slide.tags || !slide.tags.length) {
        return false
      }
      
      // 检查当前文章的标签是否与用户喜欢的标签匹配
      return slide.tags.some(tag => 
        this.userLikeTags.includes(tag.name)
      )
    },
    // 检查文章是否是随机选择的"拓展视野"文章
    isExpandHorizonArticle(slide) {
      // 如果文章匹配用户喜好，则不是拓展视野文章
      if (this.isArticleMatched(slide)) {
        return false
      }
      
      // 检查是否在处理后的slides中（即被随机选中的文章）
      return this.processedSlides.some(processedSlide => 
        processedSlide.id === slide.id && !this.isArticleMatched(processedSlide)
      )
    }
  }
}
</script>

<style lang="scss" scoped>
.carousel-wrapper {
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  box-shadow: 
    0 0 30px rgba(0, 245, 255, 0.3),
    0 0 60px rgba(131, 56, 236, 0.2);
  
  &::before {
    content: '';
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    background: linear-gradient(45deg, #00f5ff, #8338ec, #ff006e, #00f5ff);
    background-size: 300% 300%;
    border-radius: 22px;
    z-index: -1;
    animation: borderGlow 4s ease-in-out infinite;
  }
}

// 猜你喜欢标签样式
.recommend-label {
  position: absolute;
  top: 15px;
  left: 15px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg, rgba(255, 0, 110, 0.9), rgba(255, 64, 129, 0.9));
  color: white;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    0 4px 12px rgba(255, 0, 110, 0.3),
    0 0 20px rgba(255, 0, 110, 0.2);
  animation: recommendPulse 2s ease-in-out infinite;
  
  i {
    font-size: 0.9em;
    animation: heartBeat 1.5s ease-in-out infinite;
  }
  
  span {
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
    letter-spacing: 0.3px;
  }
  
  &::before {
    content: '';
    position: absolute;
    top: -1px;
    left: -1px;
    right: -1px;
    bottom: -1px;
    background: linear-gradient(135deg, #ff006e, #ff4081, #ff006e);
    border-radius: 20px;
    z-index: -1;
    opacity: 0;
    animation: recommendGlow 3s ease-in-out infinite;
  }
}

// 拓展视野标签样式
.expand-label {
  position: absolute;
  top: 15px;
  left: 15px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.9), rgba(59, 130, 246, 0.9));
  color: white;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    0 4px 12px rgba(34, 197, 94, 0.3),
    0 0 20px rgba(59, 130, 246, 0.2);
  animation: expandPulse 2s ease-in-out infinite;
  
  i {
    font-size: 0.9em;
    animation: telescopeRotate 3s ease-in-out infinite;
  }
  
  span {
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
    letter-spacing: 0.3px;
  }
  
  &::before {
    content: '';
    position: absolute;
    top: -1px;
    left: -1px;
    right: -1px;
    bottom: -1px;
    background: linear-gradient(135deg, #22c55e, #3b82f6, #22c55e);
    border-radius: 20px;
    z-index: -1;
    opacity: 0;
    animation: expandGlow 3s ease-in-out infinite;
  }
}

@keyframes recommendPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes heartBeat {
  0%, 100% {
    transform: scale(1);
  }
  25% {
    transform: scale(1.1);
  }
  50% {
    transform: scale(1);
  }
  75% {
    transform: scale(1.05);
  }
}

@keyframes recommendGlow {
  0%, 100% {
    opacity: 0;
  }
  50% {
    opacity: 0.3;
  }
}

@keyframes expandPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes telescopeRotate {
  0%, 100% {
    transform: rotate(0deg) scale(1);
  }
  25% {
    transform: rotate(5deg) scale(1.1);
  }
  50% {
    transform: rotate(0deg) scale(1);
  }
  75% {
    transform: rotate(-5deg) scale(1.1);
  }
}

@keyframes expandGlow {
  0%, 100% {
    opacity: 0;
  }
  50% {
    opacity: 0.3;
  }
}

@keyframes borderGlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

// 数字雨背景
.digital-rain {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
  opacity: 0.1;
  
  .rain-column {
    position: absolute;
    top: -100%;
    width: 20px;
    height: 200%;
    animation: rain-fall 8s linear infinite;
    
         &:nth-child(1) { left: 5%; animation-delay: 0.2s; animation-duration: 6s; }
     &:nth-child(2) { left: 10%; animation-delay: 0.4s; animation-duration: 7s; }
     &:nth-child(3) { left: 15%; animation-delay: 0.6s; animation-duration: 8s; }
     &:nth-child(4) { left: 20%; animation-delay: 0.8s; animation-duration: 9s; }
     &:nth-child(5) { left: 25%; animation-delay: 1s; animation-duration: 6s; }
     &:nth-child(6) { left: 30%; animation-delay: 1.2s; animation-duration: 7s; }
     &:nth-child(7) { left: 35%; animation-delay: 1.4s; animation-duration: 8s; }
     &:nth-child(8) { left: 40%; animation-delay: 1.6s; animation-duration: 9s; }
     &:nth-child(9) { left: 45%; animation-delay: 1.8s; animation-duration: 6s; }
     &:nth-child(10) { left: 50%; animation-delay: 2s; animation-duration: 7s; }
     &:nth-child(11) { left: 55%; animation-delay: 2.2s; animation-duration: 8s; }
     &:nth-child(12) { left: 60%; animation-delay: 2.4s; animation-duration: 9s; }
     &:nth-child(13) { left: 65%; animation-delay: 2.6s; animation-duration: 6s; }
     &:nth-child(14) { left: 70%; animation-delay: 2.8s; animation-duration: 7s; }
     &:nth-child(15) { left: 75%; animation-delay: 3s; animation-duration: 8s; }
     &:nth-child(16) { left: 80%; animation-delay: 3.2s; animation-duration: 9s; }
     &:nth-child(17) { left: 85%; animation-delay: 3.4s; animation-duration: 6s; }
     &:nth-child(18) { left: 90%; animation-delay: 3.6s; animation-duration: 7s; }
     &:nth-child(19) { left: 95%; animation-delay: 3.8s; animation-duration: 8s; }
     &:nth-child(20) { left: 100%; animation-delay: 4s; animation-duration: 9s; }
    
    .rain-char {
      display: block;
      color: #00f5ff;
      font-family: 'Courier New', monospace;
      font-size: 14px;
      line-height: 20px;
      text-shadow: 0 0 10px #00f5ff;
      opacity: 0.8;
      
      &:nth-child(odd) {
        color: #0080ff;
      }
    }
  }
}

@keyframes rain-fall {
  0% {
    transform: translateY(-100%);
  }
  100% {
    transform: translateY(100vh);
  }
}



.custom-carousel {
  width: 100%;
  border-radius: 18px;
  overflow: hidden;
  position: relative;
  z-index: 2;
  
  :deep(.el-carousel__container) {
    height: 400px;
    border-radius: 18px;
    
    @include responsive(sm) {
      height: 280px;
    }
  }
  
  :deep(.el-carousel__indicator) {
    width: 30px;
    height: 4px;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 2px;
    transition: all 0.3s ease;
    
    &.is-active {
      background: linear-gradient(90deg, #00f5ff, #0080ff);
      box-shadow: 0 0 10px rgba(0, 245, 255, 0.6);
      transform: scaleX(1.5);
    }
  }
  
  :deep(.el-carousel__button) {
    width: 100%;
    height: 100%;
    border-radius: 2px;
    background: transparent;
  }
  
  :deep(.el-carousel__arrow) {
    width: 50px !important;
    height: 50px !important;
    background: rgba(0, 0, 0, 0.3) !important;
    border: 2px solid rgba(0, 245, 255, 0.5) !important;
    color: #00f5ff !important;
    font-size: 20px !important;
    border-radius: 50% !important;
    backdrop-filter: blur(10px);
    transition: all 0.3s ease !important;
    
    &:hover {
      background: rgba(0, 245, 255, 0.2) !important;
      border-color: #00f5ff !important;
      box-shadow: 
        0 0 20px rgba(0, 245, 255, 0.6),
        0 0 40px rgba(0, 245, 255, 0.3) !important;
      border-width: 3px !important;
    }
  }
  
  :deep(.el-carousel__arrow--left) {
    left: 20px !important;
  }
  
  :deep(.el-carousel__arrow--right) {
    right: 20px !important;
  }
}

.slide-container {
  position: relative;
  height: 100%;
  width: 100%;
}

.image-wrapper {
  position: relative;
  height: 100%;
  width: 100%;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
    filter: brightness(0.8) contrast(1.2);
  }
  
  .image-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      135deg,
      rgba(0, 245, 255, 0.1) 0%,
      rgba(131, 56, 236, 0.1) 50%,
      rgba(255, 0, 110, 0.1) 100%
    );
    mix-blend-mode: overlay;
  }
  
  .scan-line {
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 2px;
    background: linear-gradient(90deg, transparent, rgba(0, 245, 255, 0.8), transparent);
    animation: scan 3s ease-in-out infinite;
  }
}

@keyframes scan {
  0% {
    left: -100%;
    top: 20%;
  }
  50% {
    left: 100%;
    top: 50%;
  }
  100% {
    left: -100%;
    top: 80%;
  }
}

.slide-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: $spacing-lg;
  background: linear-gradient(
    transparent,
    rgba(0, 0, 0, 0.3) 20%,
    rgba(0, 0, 0, 0.8)
  );
  color: white;
  // backdrop-filter: blur(10px);
  
  .content-border {
    position: relative;
    padding: 10px 12px;
    border: 1px solid rgba(0, 245, 255, 0.3);
    border-radius: 8px;
    background: rgba(0, 0, 0, 0.2);
    backdrop-filter: blur(10px);
    &::before {
      content: '';
      position: absolute;
      top: -1px;
      left: -1px;
      right: -1px;
      bottom: -1px;
      background: linear-gradient(45deg, transparent, rgba(0, 245, 255, 0.5), transparent);
      border-radius: 8px;
      z-index: -1;
      opacity: 0;
      transition: opacity 0.3s ease;
    }
    
    &:hover::before {
      opacity: 1;
    }
  }

  .header-row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 10px;
    margin-bottom: 6px;
    flex-wrap: nowrap;
    min-height: fit-content;
  }

  .glitch-text {
    font-size: 1.6em;
    margin-bottom: 0;
    font-weight: 600;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    position: relative;
    display: inline-block;
    color: white;
    line-height: 1.1;
    flex: 1;
    min-width: 0;
    
    &::before,
    &::after {
      content: attr(data-text);
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      opacity: 0.8;
    }
    
    &::before {
      animation: glitch1 2s infinite;
      color: #00f5ff;
      z-index: -1;
    }
    
    &::after {
      animation: glitch2 2s infinite;
      color: #ff006e;
      z-index: -2;
    }
  }

  .description {
    margin-bottom: 0;
    opacity: 0.9;
    font-size: 0.85em;
    max-width: 800px;
    line-height: 1.3;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
    // 限制描述文字显示行数，避免内容过长
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .cyber-button {
    position: relative;
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    background: rgba(0, 245, 255, 0.1);
    color: #00f5ff;
    border: 1px solid rgba(0, 245, 255, 0.4);
    border-radius: 6px;
    cursor: pointer;
    font-family: inherit;
    font-size: 0.8rem;
    font-weight: 400;
    text-transform: uppercase;
    letter-spacing: 0.3px;
    overflow: hidden;
    transition: all 0.2s ease;
    flex-shrink: 0;
    align-self: flex-start;
    
    .button-stats {
      display: flex;
      gap: 6px;
      align-items: center;
      
      .stat-item {
        display: flex;
        align-items: center;
        gap: 2px;
        font-size: 0.7rem;
        position: relative;
        z-index: 2;
        
        i {
          font-size: 0.8em;
          opacity: 0.8;
        }
      }
    }
    
    .button-divider {
      opacity: 0.5;
      font-size: 0.8rem;
      position: relative;
      z-index: 2;
    }
    
    .button-text {
      position: relative;
      z-index: 2;
      font-size: 0.7rem;
    }
    
    .button-glow {
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(0, 245, 255, 0.2), transparent);
      transition: left 0.3s ease;
    }
    
    > i {
      position: relative;
      z-index: 2;
      transition: transform 0.3s ease;
    }

    &:hover {
      background: rgba(0, 245, 255, 0.2);
      border-color: rgba(0, 245, 255, 0.6);
      color: #00f5ff;
      transform: translateY(-1px);
      
      .button-glow {
        left: 100%;
      }
      
      > i {
        transform: translateX(2px);
      }
    }
    
    &:active {
      transform: translateY(0) scale(0.99);
    }
  }

  .meta-info {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 6px;
    
    .category-info,
    .tags-info {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 0.75rem;
      color: rgba(255, 255, 255, 0.8);
      
      i {
        color: #00f5ff;
        font-size: 0.8em;
      }
    }
    
    .tags-info {
      display: flex;
      align-items: center;
      gap: 4px;
      
      .tag-item {
        padding: 1px 6px;
        background: rgba(0, 245, 255, 0.2);
        border: 1px solid rgba(0, 245, 255, 0.3);
        border-radius: 8px;
        font-size: 0.65rem;
        white-space: nowrap;
        
        &:hover {
          background: rgba(0, 245, 255, 0.3);
          border-color: rgba(0, 245, 255, 0.5);
        }
      }
      
      .more-tags {
        color: rgba(255, 255, 255, 0.6);
        font-size: 0.65rem;
        font-style: italic;
      }
    }
  }
}

@keyframes glitch1 {
  0%, 100% {
    transform: translate(0);
  }
  20% {
    transform: translate(-2px, 2px);
  }
  40% {
    transform: translate(-2px, -2px);
  }
  60% {
    transform: translate(2px, 2px);
  }
  80% {
    transform: translate(2px, -2px);
  }
}

@keyframes glitch2 {
  0%, 100% {
    transform: translate(0);
  }
  20% {
    transform: translate(2px, -2px);
  }
  40% {
    transform: translate(2px, 2px);
  }
  60% {
    transform: translate(-2px, -2px);
  }
  80% {
    transform: translate(-2px, 2px);
  }
}

// 悬停效果
.carousel-wrapper:hover {
  .digital-rain {
    opacity: 0.3;
  }
  
  .image-wrapper img {
    transform: scale(1.05);
  }
}

// 响应式调整
@media (max-width: 768px) {
  .recommend-label,
  .expand-label {
    top: 10px;
    left: 10px;
    padding: 4px 8px;
    font-size: 0.75rem;
    border-radius: 15px;
    
    i {
      font-size: 0.8em;
    }
  }
  
  .slide-content {
    padding: $spacing-sm;
    
    .content-border {
      padding: 8px 10px;
    }
    
    .header-row {
      flex-direction: column;
      align-items: flex-start;
      gap: 6px;
      margin-bottom: 4px;
    }
    
    .glitch-text {
      font-size: 1.3em;
      margin-bottom: 0;
      line-height: 1.0;
    }
    
    .meta-info {
      gap: 6px;
      margin-bottom: 4px;
      
      .category-info,
      .tags-info {
        font-size: 0.7rem;
      }
      
      .tags-info {
        .tag-item {
          font-size: 0.6rem;
          padding: 1px 4px;
        }
        
        .more-tags {
          font-size: 0.6rem;
        }
      }
    }
    
    .description {
      font-size: 0.8em;
      margin-bottom: 0;
      line-height: 1.2;
    }
    
    .cyber-button {
      padding: 4px 8px;
      font-size: 0.7rem;
      gap: 6px;
      align-self: flex-start;
      border-radius: 4px;
      border-width: 1px;
      
      .button-stats {
        gap: 4px;
        
        .stat-item {
          font-size: 0.65rem;
          gap: 1px;
        }
      }
      
      .button-text {
        font-size: 0.65rem;
      }
    }
  }
  
  .digital-rain {
    opacity: 0.05;
    
    .rain-char {
      font-size: 12px;
      line-height: 16px;
    }
  }
}
</style>
