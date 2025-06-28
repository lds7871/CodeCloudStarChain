<template>
  <div class="article-list-component" v-loading="loading">
    <!-- 科幻背景效果 -->
    <div class="cyber-background">
      <div class="digital-grid"></div>
      <div class="scan-overlay"></div>
      <div class="particle-field">
        <div v-for="n in 50" :key="n" class="particle" :style="getParticleStyle(n)"></div>
      </div>
    </div>

    <transition-group name="post-list" tag="div" class="posts-list">
      <article v-for="(post, index) in articles" :key="post.id" class="post-item" :style="{ '--index': index }">
        <!-- 猜你喜欢标签 - 只在匹配的文章上显示 -->
        <div v-if="isArticleMatched(post)" class="recommend-label">
          <i class="fas fa-heart"></i>
          <span>猜你喜欢</span>
        </div>
        <!-- 科幻边框装饰 -->
        <div class="cyber-borders">
          <div class="corner corner-tl"></div>
          <div class="corner corner-tr"></div>
          <div class="corner corner-bl"></div>
          <div class="corner corner-br"></div>
        </div>
        
        <!-- 扫描线效果 -->
        <div class="scan-line"></div>
        
        <!-- 全息投影效果 -->
        <div class="hologram-overlay"></div>

        <div class="post-content">
          <div class="post-main">
            <div class="post-text">
              <h3>
                <span v-if="post.isStick" class="stick-tag">
                  <i class="fas fa-thumbtack"></i>
                  <span class="glitch-text">置顶</span>
                  <div class="tag-glow"></div>
                </span>
                <span class="post-title glitch-hover" @click="$emit('article-click', post.id)">
                  {{ post.title }}
                  <div class="title-underline"></div>
                </span>
              </h3>
              <p class="post-excerpt">{{ post.summary }}</p>
              <div class="data-stream">
                <span class="stream-text">数据流传输中...</span>
                <div class="stream-dots">
                  <span>.</span><span>.</span><span>.</span>
                </div>
              </div>
            </div>
            <div class="post-image" @click="$emit('article-click', post.id)">
              <div class="image-frame">
                <img v-lazy="post.cover" :key="post.cover" :alt="post.title">
                <div class="image-overlay"></div>
                <div class="image-scan"></div>
              </div>
              <div class="image-placeholder">
                <i class="fas fa-image"></i>
                <div class="pulse-ring"></div>
              </div>
            </div>
          </div>

          <div class="post-footer">
            <div class="footer-left">
              <div class="author-info">
                <div class="avatar-wrapper">
                  <el-avatar :size="24" :src="post.avatar"></el-avatar>
                  <div class="avatar-ring"></div>
                </div>
                <span class="author-name">{{ post.nickname }}</span>
              </div>
              <div class="post-date cyber-data">
                <i class="far fa-calendar"></i>
                <span class="data-text">{{ formatTime(post.createTime) }}</span>
              </div>
              <div class="post-view cyber-data">
                <i class="far fa-eye"></i>
                <span class="data-text">{{ post.quantity }}</span>
              </div>
            </div>
            <div class="footer-right">
              <span class="category-tag cyber-tag">{{ post.categoryName }}</span>
              <!-- 标签显示 -->
              <div class="tags-info" v-if="post.tags && post.tags.length">
                <i class="fas fa-tags"></i>
                <span v-for="tag in post.tags.slice(0, 3)" :key="tag.id" class="tag-item">
                  {{ tag.name }}
                </span>
                <span v-if="post.tags.length > 3" class="more-tags">+{{ post.tags.length - 3 }}</span>
              </div>
              <span class="read-time cyber-data">
                <i class="far fa-clock"></i>
                <span class="data-text">{{ Math.ceil(post.contentMd.split(" ").length / 300) }}分钟阅读</span>
              </span>
            </div>
          </div>
        </div>
      </article>
    </transition-group>

    <el-empty v-if="!loading && articles.length === 0" description="数据库中暂无记录" class="cyber-empty">
      <template #image>
        <div class="empty-icon">
          <i class="fas fa-database"></i>
          <div class="empty-glow"></div>
        </div>
      </template>
    </el-empty>

    <div class="pagination-box">
      <el-pagination 
        background 
        v-if="articles.length" 
        @current-change="$emit('page-change', $event)"
        :current-page="params.pageNum" 
        :page-size="params.pageSize" 
        layout="prev, pager, next" 
        :total="total"
        class="cyber-pagination">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import { formatTime } from '@/utils/time'
export default {
  name: 'ArticleList',
  props: {
    articles: {
      type: Array,
      required: true
    },
    loading: {
      type: Boolean,
      default: false
    },
    total: {
      type: Number,
      default: 0
    },
    params: {
      type: Object,
      default: {
        pageNum: 1,
        pageSize: 10
      }
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
    }
  },
  methods: {
    formatTime(time) {
      return formatTime(time)
    },
    getParticleStyle(index) {
      return {
        left: Math.random() * 100 + '%',
        animationDelay: Math.random() * 3 + 's',
        animationDuration: (Math.random() * 3 + 2) + 's'
      }
    },
    // 检查单篇文章是否有匹配的标签
    isArticleMatched(article) {
      if (!this.userLikeTags.length || !article.tags || !article.tags.length) {
        return false
      }
      
      // 检查当前文章的标签是否与用户喜欢的标签匹配
      return article.tags.some(tag => 
        this.userLikeTags.includes(tag.name)
      )
    }
  }
}
</script>

<style lang="scss" scoped>
.article-list-component {
  position: relative;
  min-height: 100vh;
  background: transparent;
  // border-radius: 20px;
  overflow: hidden;
}

// 科幻背景效果
.cyber-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
  
  .digital-grid {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: 
      linear-gradient(rgba(0, 212, 255, 0.15) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 212, 255, 0.15) 1px, transparent 1px);
    background-size: 50px 50px;
    animation: gridPulse 4s ease-in-out infinite;
  }
  
  .scan-overlay {
    position: absolute;
    top: -100%;
    left: 0;
    width: 100%;
    height: 200%;
    background: linear-gradient(transparent, 
      rgba(0, 212, 255, 0.08) 50%, 
      transparent);
    animation: scanMove 8s linear infinite;
  }
  
  .particle-field {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    
    .particle {
      position: absolute;
      width: 2px;
      height: 2px;
      background: #00d4ff;
      border-radius: 50%;
      box-shadow: 0 0 6px #00d4ff;
      animation: particleFloat 3s ease-in-out infinite;
      opacity: 0.6;
    }
  }
}

// 文章列表
.posts-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
  position: relative;
  z-index: 2;

}

.post-item {
  position: relative;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.95) 0%,
    rgba(248, 250, 252, 0.9) 50%,
    rgba(255, 255, 255, 0.95) 100%);
  border: 1px solid rgba(0, 212, 255, 0.25);
  // border-radius: 15px;
  padding: $spacing-lg;
  transition: all 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
  backdrop-filter: blur(15px);
  overflow: hidden;
  box-shadow: 
    0 4px 16px rgba(0, 212, 255, 0.08),
    0 2px 8px rgba(0, 0, 0, 0.02);
  
  // 猜你喜欢标签样式
  .recommend-label {
    position: absolute;
    top: 15px;
    right: 15px;
    z-index: 10;
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    background: linear-gradient(135deg, rgba(255, 0, 110, 0.9), rgba(255, 64, 129, 0.9));
    color: white;
    border-radius: 20px;
    font-size: 0.8rem;
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
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle at var(--mouse-x, 50%) var(--mouse-y, 50%), 
      rgba(0, 212, 255, 0.05) 0%, 
      transparent 50%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }
  
  &:hover {
    transform: translateY(-5px) scale(1.02);
    box-shadow: 
      0 20px 40px rgba(0, 212, 255, 0.15),
      0 8px 32px rgba(0, 102, 255, 0.1);
    border-color: rgba(0, 212, 255, 0.5);
    
    &::before {
      opacity: 1;
    }
    
    .scan-line {
      animation: scanPass 2s ease-in-out;
    }
    
    .hologram-overlay {
      opacity: 0.1;
    }
  }
  
  // 科幻边框装饰
  .cyber-borders {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    
    .corner {
      position: absolute;
      width: 20px;
      height: 20px;
      border: 2px solid transparent;
      
      &.corner-tl {
        top: 10px;
        left: 10px;
        border-top-color: #00d4ff;
        border-left-color: #00d4ff;
        animation: cornerGlow 3s ease-in-out infinite;
      }
      
      &.corner-tr {
        top: 10px;
        right: 10px;
        border-top-color: #0066ff;
        border-right-color: #0066ff;
        animation: cornerGlow 3s ease-in-out infinite 0.5s;
      }
      
      &.corner-bl {
        bottom: 10px;
        left: 10px;
        border-bottom-color: #4c6ef5;
        border-left-color: #4c6ef5;
        animation: cornerGlow 3s ease-in-out infinite 1s;
      }
      
      &.corner-br {
        bottom: 10px;
        right: 10px;
        border-bottom-color: #339af0;
        border-right-color: #339af0;
        animation: cornerGlow 3s ease-in-out infinite 1.5s;
      }
    }
  }
  
  // 扫描线效果
  .scan-line {
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 2px;
    background: linear-gradient(90deg, 
      transparent, 
      #00d4ff, 
      transparent);
    z-index: 1;
  }
  
  // 全息投影效果
  .hologram-overlay {
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
    opacity: 0;
    transition: opacity 0.3s ease;
    pointer-events: none;
  }
}

.post-content {
  position: relative;
  z-index: 2;
}

.post-main {
  display: grid;
  grid-template-columns: 1fr 200px;
  gap: $spacing-lg;
  margin-bottom: $spacing-md;

  .post-text {
    h3 {
      font-size: 1.3em;
      margin-bottom: $spacing-md;
      line-height: 1.4;
      color: rgba(30, 41, 59, 0.9);
      
      .stick-tag {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 0.6em;
        background: linear-gradient(135deg, #00d4ff, #0066ff);
        color: white;
        padding: 6px 12px;
        border-radius: 20px;
        margin-right: $spacing-sm;
        position: relative;
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          top: -50%;
          left: -50%;
          width: 200%;
          height: 200%;
          background: linear-gradient(45deg, 
            transparent, 
            rgba(255, 255, 255, 0.3), 
            transparent);
          animation: tagShine 3s ease-in-out infinite;
        }
        
        i {
          transform: rotate(45deg);
        }
        
        .glitch-text {
          position: relative;
        }
        
        .tag-glow {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: inherit;
          border-radius: 20px;
          filter: blur(8px);
          opacity: 0.7;
          z-index: -1;
        }
      }
    }

    .post-title {
      cursor: pointer;
      position: relative;
      color: rgba(30, 41, 59, 0.9);
      transition: all 0.3s ease;
      
      &:hover {
        color: #00d4ff;
        text-shadow: 0 0 10px rgba(0, 212, 255, 0.3);
      }
      
      .title-underline {
        position: absolute;
        bottom: -2px;
        left: 0;
        width: 0;
        height: 2px;
        background: linear-gradient(90deg, #00d4ff, #0066ff);
        transition: width 0.3s ease;
      }
      
      &:hover .title-underline {
        width: 100%;
      }
    }

    .post-excerpt {
      color: rgba(71, 85, 105, 0.8);
      line-height: 1.6;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      overflow: hidden;
      margin-bottom: 15px;
    }
    
    .data-stream {
      display: flex;
      align-items: center;
      gap: 8px;
      font-family: 'Courier New', monospace;
      font-size: 0.8em;
      color: rgba(0, 212, 255, 0.7);
      
      .stream-text {
        opacity: 0.7;
      }
      
      .stream-dots {
        display: flex;
        gap: 2px;
        
        span {
          animation: dotPulse 1.5s ease-in-out infinite;
          
          &:nth-child(2) {
            animation-delay: 0.2s;
          }
          
          &:nth-child(3) {
            animation-delay: 0.4s;
          }
        }
      }
    }
  }

  .post-image {
    position: relative;
    height: 150px;
    border-radius: 10px;
    overflow: hidden;
    cursor: pointer;
    
    .image-frame {
      width: 100%;
      height: 100%;
      position: relative;
      border: 2px solid rgba(0, 212, 255, 0.2);
      border-radius: 10px;
      overflow: hidden;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: all 0.5s ease;
        filter: brightness(0.95) contrast(1.05);
      }
      
      .image-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(45deg, 
          rgba(0, 212, 255, 0.08), 
          transparent 50%, 
          rgba(0, 102, 255, 0.08));
        opacity: 0;
        transition: opacity 0.3s ease;
      }
      
      .image-scan {
        position: absolute;
        top: -100%;
        left: 0;
        width: 100%;
        height: 200%;
        background: linear-gradient(transparent, 
          rgba(0, 212, 255, 0.15) 50%, 
          transparent);
        opacity: 0;
        transition: all 0.3s ease;
      }
      
      &:hover {
        border-color: rgba(0, 212, 255, 0.6);
        box-shadow: 0 0 20px rgba(0, 212, 255, 0.2);
        
        img {
          transform: scale(1.05);
          filter: brightness(1) contrast(1);
        }
        
        .image-overlay {
          opacity: 1;
        }
        
        .image-scan {
          opacity: 1;
          animation: imageScan 2s ease-in-out;
        }
      }
    }
    
    .image-placeholder {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      color: rgba(0, 212, 255, 0.6);
      font-size: 2em;
      
      .pulse-ring {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 60px;
        height: 60px;
        border: 2px solid rgba(0, 212, 255, 0.4);
        border-radius: 50%;
        animation: pulseRing 2s ease-in-out infinite;
      }
    }
  }
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: $spacing-md;
  border-top: 1px solid rgba(0, 212, 255, 0.15);
  color: rgba(71, 85, 105, 0.8);
  
  .footer-left {
    display: flex;
    align-items: center;
    gap: $spacing-lg;

    .author-info {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      
      .avatar-wrapper {
        position: relative;
        
        .avatar-ring {
          position: absolute;
          top: -2px;
          left: -2px;
          width: 28px;
          height: 28px;
          border: 2px solid transparent;
          border-top-color: #00d4ff;
          border-radius: 50%;
          animation: avatarSpin 3s linear infinite;
        }
      }

      .author-name {
        font-weight: 500;
        color: #00d4ff;
        text-shadow: 0 0 3px rgba(0, 212, 255, 0.3);
      }
    }

    .cyber-data {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      font-family: 'Courier New', monospace;
      font-size: 0.9em;
      color: rgba(71, 85, 105, 0.8);
      
      i {
        color: #00d4ff;
        text-shadow: 0 0 3px rgba(0, 212, 255, 0.3);
      }
      
      .data-text {
        position: relative;
        
        &::after {
          content: '';
          position: absolute;
          right: -8px;
          top: 50%;
          transform: translateY(-50%);
          width: 4px;
          height: 4px;
          background: #00d4ff;
          border-radius: 50%;
          animation: dataBlink 2s ease-in-out infinite;
        }
      }
    }
  }

  .footer-right {
    display: flex;
    align-items: center;
    gap: $spacing-lg;

    .cyber-tag {
      padding: 6px 16px;
      background: linear-gradient(135deg, 
        rgba(0, 212, 255, 0.1), 
        rgba(0, 102, 255, 0.08));
      border: 1px solid rgba(0, 212, 255, 0.3);
      border-radius: 20px;
      font-size: 0.85em;
      color: #0066ff;
      font-family: 'Courier New', monospace;
      text-shadow: 0 0 3px rgba(0, 102, 255, 0.2);
      position: relative;
      overflow: hidden;
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, 
          transparent, 
          rgba(255, 255, 255, 0.4), 
          transparent);
        transition: left 0.5s ease;
      }
      
      &:hover::before {
        left: 100%;
      }
    }
    
    .tags-info {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 0.8em;
      
      i {
        color: #00d4ff;
        font-size: 0.9em;
        text-shadow: 0 0 3px rgba(0, 212, 255, 0.3);
      }
      
      .tag-item {
        padding: 4px 10px;
        background: linear-gradient(135deg, 
          rgba(0, 212, 255, 0.08), 
          rgba(76, 110, 245, 0.06));
        border: 1px solid rgba(0, 212, 255, 0.2);
        border-radius: 12px;
        font-size: 0.85em;
        color: rgba(0, 102, 255, 0.9);
        font-family: 'Courier New', monospace;
        white-space: nowrap;
        transition: all 0.3s ease;
        position: relative;
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: -100%;
          width: 100%;
          height: 100%;
          background: linear-gradient(90deg, 
            transparent, 
            rgba(0, 212, 255, 0.2), 
            transparent);
          transition: left 0.5s ease;
        }
        
        &:hover {
          background: linear-gradient(135deg, 
            rgba(0, 212, 255, 0.15), 
            rgba(76, 110, 245, 0.1));
          border-color: rgba(0, 212, 255, 0.4);
          color: #0066ff;
          transform: translateY(-1px);
          box-shadow: 0 2px 8px rgba(0, 212, 255, 0.15);
          
          &::before {
            left: 100%;
          }
        }
      }
      
      .more-tags {
        color: rgba(0, 212, 255, 0.7);
        font-size: 0.8em;
        font-style: italic;
        font-family: 'Courier New', monospace;
        opacity: 0.8;
        transition: opacity 0.3s ease;
        
        &:hover {
          opacity: 1;
        }
      }
    }
  }
}

// 空状态
.cyber-empty {
  .empty-icon {
    position: relative;
    font-size: 4em;
    color: rgba(0, 212, 255, 0.6);
    
    .empty-glow {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 120px;
      height: 120px;
      border: 2px solid rgba(0, 212, 255, 0.2);
      border-radius: 50%;
      animation: emptyPulse 3s ease-in-out infinite;
    }
  }
}

// 分页器
.pagination-box {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  
  .cyber-pagination {
    :deep(.el-pager li) {
      background: rgba(255, 255, 255, 0.8);
      border: 1px solid rgba(0, 212, 255, 0.2);
      color: rgba(71, 85, 105, 0.8);
      margin: 0 3px;
      border-radius: 6px;
      transition: all 0.3s ease;
      
      &:hover, &.active {
        background: rgba(0, 212, 255, 0.1);
        border-color: #00d4ff;
        color: #0066ff;
        box-shadow: 0 0 10px rgba(0, 212, 255, 0.2);
      }
    }
    
    :deep(.btn-prev), :deep(.btn-next) {
      background: rgba(255, 255, 255, 0.8);
      border: 1px solid rgba(0, 212, 255, 0.2);
      color: rgba(71, 85, 105, 0.8);
      border-radius: 6px;
      
      &:hover {
        background: rgba(0, 212, 255, 0.1);
        border-color: #00d4ff;
        color: #0066ff;
      }
    }
  }
}

// 动画定义
@keyframes borderFlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes gridPulse {
  0%, 100% {
    opacity: 0.3;
  }
  50% {
    opacity: 0.7;
  }
}

@keyframes scanMove {
  0% {
    transform: translateY(-100%);
  }
  100% {
    transform: translateY(100%);
  }
}

@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0px);
    opacity: 0.4;
  }
  50% {
    transform: translateY(-20px);
    opacity: 0.8;
  }
}

@keyframes scanPass {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

@keyframes cornerGlow {
  0%, 100% {
    opacity: 0.4;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.1);
  }
}

@keyframes tagShine {
  0% {
    transform: translateX(-200%) translateY(-200%) rotate(45deg);
  }
  100% {
    transform: translateX(200%) translateY(200%) rotate(45deg);
  }
}

@keyframes dotPulse {
  0%, 100% {
    opacity: 0.4;
  }
  50% {
    opacity: 1;
  }
}

@keyframes imageScan {
  0% {
    transform: translateY(-100%);
  }
  100% {
    transform: translateY(100%);
  }
}

@keyframes pulseRing {
  0% {
    transform: translate(-50%, -50%) scale(0.8);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.4);
    opacity: 0;
  }
}

@keyframes avatarSpin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes dataBlink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

@keyframes emptyPulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.4;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0.7;
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

// 响应式调整
@include responsive(md) {
  .post-main {
    grid-template-columns: 1fr;
    
    .post-image {
      height: 200px;
      order: -1;
    }
  }
}

@include responsive(sm) {
  .posts-list {
    padding: 10px;
  }
  
  .post-item {
    padding: $spacing-sm;
    
    .recommend-label {
      top: 10px;
      right: 10px;
      padding: 4px 8px;
      font-size: 0.7rem;
      border-radius: 15px;
      
      i {
        font-size: 0.8em;
      }
    }
  }

  .post-footer {
    flex-direction: column;
    gap: $spacing-md;
    
    .footer-left, .footer-right {
      width: 100%;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: $spacing-sm;
    }
    
    .footer-right {
      .tags-info {
        .tag-item {
          font-size: 0.75em;
          padding: 2px 6px;
        }
        
        .more-tags {
          font-size: 0.7em;
        }
      }
    }
  }
}

// 保持动画效果
.post-list-enter-active {
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: calc(0.1s * var(--index));
}

.post-list-leave-active {
  transition: all 0.6s ease;
}

.post-list-enter,
.post-list-leave-to {
  opacity: 0;
  transform: translateY(50px) scale(0.95);
}
</style>