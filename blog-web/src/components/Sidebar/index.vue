<template>
  <aside class="sidebar">
    <el-card class="author-card">
      <div class="floating-cards">
        <span v-for="n in 12" :key="n" class="card-particle"></span>
      </div>
      <div class="sci-fi-corners"></div>
      <div class="data-streams">
        <div v-for="n in 3" :key="n" class="stream-line"></div>
      </div>
      <div class="card-header">
        <div class="author-avatar">
          <el-avatar class="avatar" :src="$store.state.webSiteInfo.authorAvatar" alt="作者头像" />
        </div>
        <div class="status-badge">
          <span>在线</span>
        </div>
      </div>
      <div class="author-info">
        <h3>{{ $store.state.webSiteInfo.author }}</h3>
        <p class="bio">{{ $store.state.webSiteInfo.authorInfo }}</p>
      </div>
      <div class="social-links">
        <div v-for="item in socialLinksWithData" :key="item.type">
          <el-tooltip placement="top" :content="item.content">
            <a v-if="$store.state.webSiteInfo.showList.indexOf(item.type) !== -1" href="javascript:void(0)"
                :title="item.title" :class="`social-btn ${item.type}`" @click="copyToClipboard(item)">
                <i :class="item.icon"></i>
            </a>
          </el-tooltip>
        </div>

      </div>
    </el-card>

    <el-card class="section announcement" v-if="announcements.length">
      <h3>
        <i class="fas fa-bullhorn"></i>
        公告
      </h3>
      <div class="announcement-content">
        <div class="announcement-item" v-for="(item, index) in announcements" :key="index">
          <!-- <i :class="item.icon"></i> -->
          <span v-html="item.content"></span>
        </div>
      </div>
    </el-card>

    <el-card class="section" v-if="hot.length > 0">
      <h3>
        <i class="fas fa-star"></i>
        今日推荐
      </h3>
      <div class="post-list">
        <router-link v-for="post in hot" :key="post.id" :to="`/post/${post.id}`" class="post-item">
          <img v-lazy="post.cover" :key="post.cover" :alt="post.title" @error="handleImageError">
          <div class="post-meta">
            <h4>{{ post.title }}</h4>
            <time>{{ post.createTime }}</time>
          </div>
        </router-link>
      </div>
    </el-card>

    <el-card class="section" >
      <h3>
        <i class="fas fa-tags"></i>
        标签云
      </h3>
      <Tag />
    </el-card>
  </aside>
</template>

<script>
import { getRecommendArticlesApi } from '@/api/article'
import Tag from './components/tagCloud.vue'

export default {
  name: 'Sidebar',
  components: {
    Tag
  },
  data() {
    return {
      hot: [],
      announcements: [],
      socialLinks: [
        {
          icon: 'fab fa-github',
          type: 'github',
          content: '点击跳转GitHub主页',
          icCopy: false
        },
        {
          icon: 'fab fa-git-alt',
          type: 'gitee',
          content: '点击跳转GitEE主页',
          icCopy: false
        },
        {
          icon: 'fab fa-qq',
          title: 'QQ',
          type: 'qq',
          content: '点击复制QQ号',
          icCopy: true
        },
        {
          icon: 'fas fa-users',
          title: 'QQ群',
          type: 'qqGroup',
          content: '点击复制QQ群号',
          icCopy: true
        },
        {
          icon: 'fas fa-at',
          title: '邮箱',
          type: 'email',
          content: '点击复制邮箱',
          icCopy: true
        },
        {
          icon: 'fab fa-weixin',
          title: '微信',
          type: 'wechat',
          content: '点击复制微信号',
          icCopy: true
        }
      ],
      tags: [],
    }
  },
  computed: {
    socialLinksWithData() {
      return this.socialLinks.map(item => {
        const linkMap = {
          github: this.$store.state.webSiteInfo.github,
          gitee: this.$store.state.webSiteInfo.gitee,
          qq: this.$store.state.webSiteInfo.qqNumber,
          qqGroup: this.$store.state.webSiteInfo.qqGroup,
          email: this.$store.state.webSiteInfo.email,
          wechat: this.$store.state.webSiteInfo.wechat
        }
        return {
          ...item,
          link: linkMap[item.type]
        }
      })
    }
  },
  watch: {
    '$store.state.notice'(val) {
      if (val && val.right) {
        this.announcements = val.right
      }
    } 
  },
  mounted() {
    getRecommendArticlesApi().then(res => {
      const articles = res.data
      if (articles && articles.length > 0) {
        this.setDailyRecommendedArticle(articles)
      }
    })
    // 初始化公告数据
    if (this.$store.state.notice && this.$store.state.notice.right) {
      this.announcements = this.$store.state.notice.right
    }
  },
  methods: {
    /**
     * 处理图片加载失败
     */
    handleImageError(e) {
      e.target.src = this.$store.state.defaultImage
      e.target.classList.add('fallback')
    },
    /**
     * 复制到剪贴板
     */
    copyToClipboard(item) {
      if (item.icCopy) {
        navigator.clipboard.writeText(item.link).then(() => {
          this.$message.success(`${item.title}账号已复制到剪贴板`);
        }).catch(() => {
          this.$message.error('复制失败，请手动复制');
        });
      } else {
        window.open(item.link, '_blank')
      }
    },
    /**
     * 设置每日推荐文章
     */
    setDailyRecommendedArticle(articles) {
      const today = new Date().toDateString() // 获取今天的日期字符串
      const storageKey = 'dailyRecommendedArticle'
      const dateKey = 'dailyRecommendedDate'
      
      // 获取存储的日期和文章
      const storedDate = localStorage.getItem(dateKey)
      const storedArticle = localStorage.getItem(storageKey)
      
      // 如果是同一天且有存储的文章，直接使用
      if (storedDate === today && storedArticle) {
        try {
          const article = JSON.parse(storedArticle)
          this.hot = [article]
          return
        } catch (error) {
          console.error('解析存储的文章数据失败:', error)
        }
      }
      
      // 如果是新的一天或没有存储的文章，重新随机选择
      const randomIndex = Math.floor(Math.random() * articles.length)
      const selectedArticle = articles[randomIndex]
      
      // 存储新选择的文章和日期
      localStorage.setItem(storageKey, JSON.stringify(selectedArticle))
      localStorage.setItem(dateKey, today)
      
      this.hot = [selectedArticle]
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebar {
  position: sticky;
  top: 80px;
  width: 100%;
  max-width: 320px;
  
  // 整体科幻容器效果
  &::before {
    content: '';
    position: absolute;
    top: -10px;
    left: -10px;
    right: -10px;
    bottom: -10px;
    background: linear-gradient(45deg, 
      rgba(0, 245, 255, 0.1) 0%,
      transparent 25%,
      rgba(131, 56, 236, 0.1) 50%,
      transparent 75%,
      rgba(255, 0, 110, 0.1) 100%);
    border-radius: 25px;
    z-index: -1;
    opacity: 0;
    transition: opacity 0.3s ease;
    animation: sidebarGlow 8s ease-in-out infinite;
  }
  
  &:hover::before {
    opacity: 1;
  }

  .author-card {
    // padding: $spacing-md;
    margin-bottom: $spacing-lg;
    position: relative;
    background: linear-gradient(135deg, 
      rgba(0, 0, 0, 0.9) 0%,
      rgba(0, 245, 255, 0.05) 50%,
      rgba(0, 0, 0, 0.9) 100%);
    border: 1px solid rgba(0, 245, 255, 0.3);
    border-radius: 20px;
    box-shadow: 
      0 0 30px rgba(0, 245, 255, 0.2),
      inset 0 0 30px rgba(0, 245, 255, 0.05);
    overflow: hidden;
    backdrop-filter: blur(15px);
    transition: all 0.3s ease;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 120px;
      background: linear-gradient(135deg,
        rgba(0, 245, 255, 0.2) 0%,
        rgba(131, 56, 236, 0.2) 50%,
        rgba(255, 0, 110, 0.2) 100%);
      transition: opacity 0.3s ease;
      border-top-left-radius: 20px;
      border-top-right-radius: 20px;
      z-index: 1;
    }
    
    // 多个科幻装饰元素
    &::after {
      content: '';
      position: absolute;
      top: 20px;
      right: 20px;
      width: 30px;
      height: 30px;
      background: transparent;
      border: 2px solid rgba(0, 245, 255, 0.4);
      clip-path: polygon(30% 0%, 70% 0%, 100% 30%, 100% 70%, 70% 100%, 30% 100%, 0% 70%, 0% 30%);
      animation: hexRotate 6s linear infinite;
      z-index: 3;
    }
    
    // 科幻边框装饰
    .sci-fi-corners {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      pointer-events: none;
      z-index: 4;
      
      &::before, &::after {
        content: '';
        position: absolute;
        width: 20px;
        height: 20px;
        border: 2px solid transparent;
      }
      
      // 左上角
      &::before {
        top: 15px;
        left: 15px;
        border-top-color: #00f5ff;
        border-left-color: #00f5ff;
        animation: cornerPulse 3s ease-in-out infinite;
      }
      
      // 右下角
      &::after {
        bottom: 15px;
        right: 15px;
        border-bottom-color: #ff006e;
        border-right-color: #ff006e;
        animation: cornerPulse 3s ease-in-out infinite 1.5s;
      }
    }
    
    // 数据流效果
    .data-streams {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      pointer-events: none;
      z-index: 1;
      overflow: hidden;
      
      .stream-line {
        position: absolute;
        width: 1px;
        height: 100%;
        background: linear-gradient(180deg, 
          transparent 0%,
          rgba(0, 245, 255, 0.6) 30%,
          rgba(0, 245, 255, 0.8) 50%,
          rgba(0, 245, 255, 0.6) 70%,
          transparent 100%);
        animation: streamFlow 4s ease-in-out infinite;
        
        &:nth-child(1) {
          left: 25%;
          animation-delay: 0s;
        }
        
        &:nth-child(2) {
          left: 50%;
          animation-delay: 1.3s;
          background: linear-gradient(180deg, 
            transparent 0%,
            rgba(131, 56, 236, 0.6) 30%,
            rgba(131, 56, 236, 0.8) 50%,
            rgba(131, 56, 236, 0.6) 70%,
            transparent 100%);
        }
        
        &:nth-child(3) {
          left: 75%;
          animation-delay: 2.6s;
          background: linear-gradient(180deg, 
            transparent 0%,
            rgba(255, 0, 110, 0.6) 30%,
            rgba(255, 0, 110, 0.8) 50%,
            rgba(255, 0, 110, 0.6) 70%,
            transparent 100%);
        }
      }
    }
    
    // 边框发光效果
    &:hover {
      border-color: rgba(0, 245, 255, 0.6);
      box-shadow: 
        0 0 50px rgba(0, 245, 255, 0.4),
        inset 0 0 50px rgba(0, 245, 255, 0.1);
      transform: translateY(-5px);
      
      &::before {
        opacity: 0.5;
      }
    }

    .card-header {
      position: relative;
      margin-bottom: 20px;
      display: flex;
      justify-content: center;
      z-index: 2;

      .author-avatar {
        width: 88px;
        height: 88px;
        position: relative;

        .avatar {
          width: 100%;
          height: 100%;
          border-radius: 50%;
          object-fit: cover;
          border: 3px solid #00f5ff;
          box-shadow: 
            0 0 20px rgba(0, 245, 255, 0.4),
            inset 0 0 20px rgba(0, 245, 255, 0.1);
          transition: all 0.3s ease;
          position: relative;
          z-index: 2;

          &:hover {
            transform: scale(1.05);
            box-shadow: 
              0 0 30px rgba(0, 245, 255, 0.6),
              inset 0 0 30px rgba(0, 245, 255, 0.2);
          }
        }
        
        // 头像光环效果
        &::before {
          content: '';
          position: absolute;
          top: -5px;
          left: -5px;
          right: -5px;
          bottom: -5px;
          border-radius: 50%;
          background: conic-gradient(from 0deg, #00f5ff, #8338ec, #ff006e, #00f5ff);
          animation: avatarGlow 3s linear infinite;
          z-index: 1;
        }
      }

      .status-badge {
        position: absolute;
        bottom: 0;
        right: 50%;
        transform: translateX(32px);
        background: linear-gradient(135deg, #00f5ff, #0080ff);
        border: 2px solid rgba(0, 245, 255, 0.8);
        border-radius: 15px;
        padding: 6px 14px;
        font-size: 0.75rem;
        color: rgba(240, 240, 240, 0.95);
        box-shadow: 
          0 0 15px rgba(0, 245, 255, 0.4),
          inset 0 0 15px rgba(255, 255, 255, 0.2);
        text-shadow: 0 0 5px rgba(0, 245, 255, 0.8);
        animation: badgePulse 2s ease-in-out infinite;

        span {
          display: flex;
          align-items: center;
          gap: 6px;
          font-weight: 600;

          &::before {
            content: '';
            display: inline-block;
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #fff;
            box-shadow: 0 0 10px #fff;
            animation: dotPulse 1s ease-in-out infinite;
          }
        }
      }
    }

    .author-info {
      text-align: center;
      margin-bottom: 20px;
      position: relative;
      z-index: 2;

      h3 {
        font-size: 1.4rem;
        font-weight: 700;
        background: linear-gradient(45deg, #00f5ff, #0080ff, #8338ec);
        background-clip: text;
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        text-shadow: 0 0 10px rgba(0, 245, 255, 0.3);
        margin-bottom: 8px;
        animation: textShimmer 3s ease-in-out infinite;
      }

      .bio {
        font-size: 0.95rem;
        color: rgba(150, 150, 150, 0.9);
        line-height: 1.6;
        margin-bottom: 16px;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
      }
    }

    .social-links {
      display: flex;
      justify-content: center;
      gap: 12px;
      margin-top: 20px;
      position: relative;
      z-index: 2;

      .social-btn {
        width: 42px;
        height: 42px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 12px;
        background: rgba(0, 0, 0, 0.3);
        border: 2px solid transparent;
        font-size: 1.2rem;
        transition: all 0.3s ease;
        position: relative;
        text-decoration: none;
        overflow: hidden;
        backdrop-filter: blur(10px);
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.1), transparent);
          transform: translateX(-100%);
          transition: transform 0.5s ease;
        }
        
        &:hover::before {
          transform: translateX(100%);
        }
      }

      .qq {
        color: #60a5fa;
        border-color: rgba(96, 165, 250, 0.3);

        &:hover {
          background: rgba(255, 255, 255, 0.2);
          border-color: #fff;
          color: #000;
          box-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
        }
      }

      .qqGroup {
        color: #e1c235;
        border-color: rgba(225, 194, 53, 0.3);

        &:hover {
          background: rgba(255, 255, 255, 0.2);
          border-color: #fff;
          color: #000;
          box-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
        }
      }

      .github {
        color: #fff;
        border-color: rgba(255, 255, 255, 0.3);

        &:hover {
          background: rgba(255, 255, 255, 0.2);
          border-color: #fff;
          color: #000;
          box-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
        }
      }

      .gitee {
        color: #ee3434;
        border-color: rgba(238, 52, 52, 0.3);

        &:hover {
          background: rgba(255, 255, 255, 0.2);
          border-color: #fff;
          color: #000;
          box-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
        }
      }

      .email {
        color: #d872a7;
        border-color: rgba(216, 114, 167, 0.3);

        &:hover {
          background: rgba(255, 255, 255, 0.2);
          border-color: #fff;
          color: #000;
          box-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
        }
      }

      .wechat {
        color: #10b981;
        border-color: rgba(16, 185, 129, 0.3);

        &:hover {
          background: rgba(255, 255, 255, 0.2);
          border-color: #fff;
          color: #000;
          box-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
        }
      }
    }

    .floating-cards {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      pointer-events: none;
      overflow: hidden;
      opacity: 0;
      transition: opacity 0.3s ease;
      z-index: 1;
    }

    &:hover .floating-cards {
      opacity: 1;
    }

    .card-particle {
      position: absolute;
      width: 6px;
      height: 6px;
      background: #00f5ff;
      border-radius: 50%;
      opacity: 0;
      animation: particleFall 4s ease-in-out infinite;
      box-shadow: 0 0 6px currentColor;

      &:nth-child(1) { left: 10%; animation-delay: 0ms; background: #6366f1; }
      &:nth-child(2) { left: 20%; animation-delay: 300ms; background: #8b5cf6; }
      &:nth-child(3) { left: 30%; animation-delay: 600ms; background: #ec4899; }
      &:nth-child(4) { left: 40%; animation-delay: 900ms; background: #10b981; }
      &:nth-child(5) { left: 50%; animation-delay: 1200ms; background: #f59e0b; }
      &:nth-child(6) { left: 60%; animation-delay: 1500ms; background: #ef4444; }
      &:nth-child(7) { left: 70%; animation-delay: 1800ms; background: #00f5ff; }
      &:nth-child(8) { left: 80%; animation-delay: 2100ms; background: #0080ff; }
      &:nth-child(9) { left: 90%; animation-delay: 2400ms; background: #8338ec; }
      &:nth-child(10) { left: 95%; animation-delay: 2700ms; background: #ff006e; }
      &:nth-child(11) { left: 85%; animation-delay: 3000ms; background: #6366f1; }
      &:nth-child(12) { left: 15%; animation-delay: 3300ms; background: #8b5cf6; }
    }
  }
}

.section {
  margin-bottom: $spacing-lg;
  position: relative;
  background: linear-gradient(135deg, 
    rgba(0, 0, 0, 0.9) 0%,
    rgba(0, 245, 255, 0.05) 50%,
    rgba(0, 0, 0, 0.9) 100%);
  border: 1px solid rgba(0, 245, 255, 0.2);
  border-radius: 15px;
  // padding: $spacing-lg;
  backdrop-filter: blur(10px);
  box-shadow: 
    0 0 20px rgba(0, 245, 255, 0.1),
    inset 0 0 20px rgba(0, 245, 255, 0.05);
  transition: all 0.3s ease;
  overflow: hidden;
  
  // 多重扫描线效果
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 2px;
    background: linear-gradient(90deg, transparent, rgba(0, 245, 255, 0.8), transparent);
    animation: sectionScan 3s ease-in-out infinite;
  }
  
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    right: -100%;
    width: 100%;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(255, 0, 110, 0.6), transparent);
    animation: sectionScanReverse 4s ease-in-out infinite;
  }
  
  &:hover {
    border-color: rgba(0, 245, 255, 0.4);
    box-shadow: 
      0 0 30px rgba(0, 245, 255, 0.2),
      inset 0 0 30px rgba(0, 245, 255, 0.1);
    transform: translateY(-2px);
  }

  h3 {
    font-size: 1.2rem;
    font-weight: 600;
    background: linear-gradient(45deg, #00f5ff, #0080ff, #8338ec);
    background-size: 200% 200%;
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid rgba(0, 245, 255, 0.2);
    position: relative;
    display: flex;
    align-items: center;
    gap: 10px;
    animation: titleGradient 4s ease-in-out infinite;

    i {
      color: #00f5ff;
      text-shadow: 0 0 10px rgba(0, 245, 255, 0.6);
      font-size: 1.1em;
      animation: iconPulse 2s ease-in-out infinite;
    }

    // 科幻下划线效果
    &::after {
      content: '';
      position: absolute;
      bottom: -1px;
      left: 0;
      width: 40px;
      height: 2px;
      background: linear-gradient(90deg, #00f5ff, #8338ec, transparent);
      box-shadow: 0 0 10px rgba(0, 245, 255, 0.6);
      animation: underlineExpand 3s ease-in-out infinite;
    }
    
    // 科幻边框装饰
    &::before {
      content: '';
      position: absolute;
      top: -5px;
      left: -5px;
      width: 20px;
      height: 20px;
      border: 1px solid rgba(0, 245, 255, 0.3);
      border-radius: 3px;
      transform: rotate(45deg);
      animation: rotateBorder 6s linear infinite;
    }
  }

  .post-list {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .post-item {
      display: flex;
      gap: 12px;
      padding: 12px;
      border-radius: 10px;
      background: rgba(0, 245, 255, 0.02);
      border: 1px solid rgba(0, 245, 255, 0.1);
      transition: all 0.3s ease;
      text-decoration: none;
      color: inherit;
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
          rgba(0, 245, 255, 0.2), 
          rgba(131, 56, 236, 0.1),
          transparent);
        transition: left 0.5s ease;
      }
      
      // 科幻边框指示器
      &::after {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        width: 3px;
        height: 100%;
        background: linear-gradient(180deg, 
          transparent, 
          rgba(0, 245, 255, 0.8), 
          transparent);
        opacity: 0;
        transition: opacity 0.3s ease;
      }
      
      &:hover {
        border-color: rgba(0, 245, 255, 0.3);
        background: rgba(0, 245, 255, 0.05);
        box-shadow: 0 0 15px rgba(0, 245, 255, 0.2);
        transform: translateX(5px);
        
        &::before {
          left: 100%;
        }
        
        &::after {
          opacity: 1;
        }
      }

      img {
        width: 60px;
        height: 45px;
        border-radius: 8px;
        object-fit: cover;
        border: 1px solid rgba(0, 245, 255, 0.2);
        transition: all 0.3s ease;
        
        &:hover {
          border-color: rgba(0, 245, 255, 0.5);
          box-shadow: 0 0 10px rgba(0, 245, 255, 0.3);
        }

        &.fallback {
          opacity: 0.6;
          filter: grayscale(100%);
        }
      }

      .post-meta {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        h4 {
          font-size: 0.9rem;
          font-weight: 500;
          color: rgba(120, 120, 120, 0.9);
          margin: 0 0 6px 0;
          line-height: 1.4;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
          transition: color 0.3s ease;
          
          &:hover {
            color: #00f5ff;
          }
        }

        time {
          font-size: 0.75rem;
          color: rgba(100, 100, 100, 0.8);
          font-family: 'Courier New', monospace;
        }
      }
    }
  }

  .announcement-content {
    .announcement-item {
      padding: 12px 0;
      border-bottom: 1px solid rgba(0, 245, 255, 0.1);
      color: rgba(130, 130, 130, 0.9);
      line-height: 1.6;
      position: relative;
      
      &:last-child {
        border-bottom: none;
      }
      
      &::before {
        content: '▶';
        color: #00f5ff;
        margin-right: 8px;
        font-size: 0.8em;
        animation: blink 2s ease-in-out infinite;
      }
    }
  }
}

// 动画定义
@keyframes hexRotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes avatarGlow {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes badgePulse {
  0%, 100% {
    transform: translateX(32px) scale(1);
    box-shadow: 
      0 0 15px rgba(0, 245, 255, 0.4),
      inset 0 0 15px rgba(255, 255, 255, 0.2);
  }
  50% {
    transform: translateX(32px) scale(1.05);
    box-shadow: 
      0 0 25px rgba(0, 245, 255, 0.6),
      inset 0 0 25px rgba(255, 255, 255, 0.3);
  }
}

@keyframes dotPulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.8);
  }
}

@keyframes textShimmer {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes particleFall {
  0% {
    opacity: 0;
    transform: translateY(-10px);
  }
  10%, 90% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: translateY(100px);
  }
}

@keyframes sectionScan {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

@keyframes sectionScanReverse {
  0% {
    right: -100%;
  }
  100% {
    right: 100%;
  }
}

@keyframes sidebarGlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes cornerPulse {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
}

@keyframes streamFlow {
  0% {
    transform: translateY(-100%);
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  100% {
    transform: translateY(100%);
    opacity: 0;
  }
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0.3;
  }
}

@keyframes titleGradient {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes underlineExpand {
  0%, 100% {
    width: 40px;
    opacity: 0.6;
  }
  50% {
    width: 80px;
    opacity: 1;
  }
}

@keyframes rotateBorder {
  from {
    transform: rotate(45deg);
  }
  to {
    transform: rotate(405deg);
  }
}

// 响应式调整
@media (max-width: 768px) {
  .sidebar {
    max-width: 100%;
    
    .author-card {
      padding: $spacing-sm;
      
      .card-header .author-avatar {
        width: 70px;
        height: 70px;
      }
      
      .author-info h3 {
        font-size: 1.2rem;
      }
      
      .social-links {
        gap: 8px;
        
        .social-btn {
          width: 36px;
          height: 36px;
          font-size: 1rem;
        }
      }
    }
    
    .section {
      padding: $spacing-md;
      
      h3 {
        font-size: 1.1rem;
      }
      
      .post-list .post-item {
        gap: 10px;
        padding: 10px;
        
        img {
          width: 50px;
          height: 38px;
        }
        
        .post-meta h4 {
          font-size: 0.85rem;
        }
      }
    }
  }
}
</style>