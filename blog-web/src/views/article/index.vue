<template>
  <div class="article-page" v-loading="loading">
    <!-- 阅读进度条 -->
    <div class="reading-progress-bar" :style="{width: readProgress + '%'}"></div>

    <!-- 添加固定操作栏 -->
    <div class="floating-action-bar" :style="{ left: actionBarLeft }">
      <el-tooltip class="item" effect="dark" content="点赞" placement="top-start">
        <div class="action-item" @click="toggleLike">
          <el-badge :value="article.likeNum || 0" class="item">
            <div class="action-button" :class="{ 'is-active': article.isLike }">
              <i class="fas fa-thumbs-up" :class="{ active: article.isLike }"></i>
            </div>
          </el-badge>
        </div>
      </el-tooltip>
      <el-tooltip class="item" effect="dark" content="收藏" placement="top-start">
        <div class="action-item" @click="toggleFavorite">
          <el-badge :value="article.favoriteNum || 0" class="item">
            <div class="action-button" :class="{ 'is-active': article.isFavorite }">
              <i class="fas fa-star" :class="{ active: article.isFavorite }"></i>
            </div>
          </el-badge>
        </div>
      </el-tooltip>
      <el-tooltip class="item" effect="dark" content="沉浸式阅读" placement="top-start">
        <div class="action-item" @click="toggleSidebar">
          <div class="action-button">
            <i class="fas fa-expand"></i>
          </div>
        </div>
      </el-tooltip>
      <div class="action-item reward-item">
        <div class="action-button">
          <i class="fas fa-yen-sign"></i>
        </div>
        <div class="reward-popup">
          <div class="reward-title">感谢您的支持</div>
          <div class="reward-content">
            <div class="reward-qr-container">
              <img v-lazy="$store.state.webSiteInfo.weixinPay" alt="微信支付" class="reward-qr">
              <div class="reward-label">微信支付</div>
            </div>
            <div class="reward-qr-container">
              <img v-lazy="$store.state.webSiteInfo.aliPay" alt="支付宝" class="reward-qr">
              <div class="reward-label">支付宝</div>
            </div>
          </div>
          <div class="reward-text">扫一扫，请我喝杯咖啡</div>
        </div>
      </div>
      <el-tooltip class="item" effect="dark" content="评论" placement="top-start">
        <div class="action-item" @click="scrollToComments">
          <el-badge :value="article.commentNum || 0" class="item">
            <div class="action-button">
              <i class="fas fa-comment"></i>
            </div>
          </el-badge>
        </div>
      </el-tooltip>
      <el-tooltip class="item" effect="dark" content="回到顶部" placement="top-start">
        <div class="action-item" @click="scrollToTop" v-show="showBackToTop">
          <div class="action-button">
            <i class="fas fa-arrow-up"></i>
          </div>
        </div>
      </el-tooltip>
    </div>

    <div class="content-layout" id="articleBox" :class="{ center: !showSidebar }">
      <main class="article-main">
        <!-- 文章头部 -->
        <header class="article-header">
          <div class="article-title">{{ article.title }}</div>
          
          <div class="article-cover-image" v-if="article.cover">
            <img v-lazy="article.cover" alt="文章封面">
          </div>

          <!-- 作者信息和元数据 -->
          <div class="article-info">
            <div class="author-info">
              <img v-lazy="article.avatar" alt="作者头像" class="author-avatar">
              <div class="author-meta">
                <span class="author-name">{{ article.nickname }}</span>
                <div class="post-meta">
                  <time class="publish-time">
                    <i class="far fa-calendar-alt"></i>
                    {{ article.createTime }}
                  </time>
                  <span class="meta-divider">·</span>
                  <span class="category">
                    <i class="fas fa-folder"></i>
                    {{ article.category.name }}
                  </span>
                </div>
              </div>
            </div>

            <div class="article-stats">
              <div class="stat-item">
                <i class="far fa-eye"></i>
                <span>{{ article.quantity }} 阅读</span>
              </div>
              <div class="stat-item">
                <i class="far fa-clock"></i>
                <span>{{ readTime }} 分钟</span>
              </div>
              <div class="stat-item">
                <i class="far fa-comment"></i>
                <span>{{ article.commentNum || 0 }} 评论</span>
              </div>
            </div>
          </div>
        </header>

        <!-- AI简短介绍 -->
        <div v-if="article.aiDescribe" class="ai-description">
          <div class="ai-header" @click="isAiDescriptionExpanded = !isAiDescriptionExpanded">
            <div class="ai-icon-wrapper">
              <i class="fas fa-robot"></i>
            </div>
            <span>AI 摘要</span>
            <i class="fas" :class="isAiDescriptionExpanded ? 'fa-chevron-up' : 'fa-chevron-down'"
              style="margin-left:auto;"></i>
          </div>
          <transition name="expand" @enter="startTransition" @leave="endTransition" mode="out-in">
            <div class="ai-content" v-show="isAiDescriptionExpanded">
              <span class="typing-text" ref="typingText"></span>
              <div class="ai-description-text">
                <i class="fas fa-magic"></i> 摘要由平台通过智能技术生成
              </div>
            </div>
          </transition>
        </div>

        <!-- 文章内容 -->
        <article class="article-content" ref="articleContent">
          <!-- 免费内容 -->
          <div v-if="article.readType === 1 || isPaid" v-html="article.content"></div>

          <!-- 会员内容 -->
          <div v-else-if="article.readType === 2" class="locked-content member">
            <div class="preview-content" v-html="getPreviewContent(article.content)"></div>
            <div class="content-locker">
              <div class="locker-icon">
                <i class="fas fa-crown"></i>
              </div>
              <h3>会员专享内容</h3>
              <p>成为会员即可阅读全文</p>
              <el-button type="primary" @click="handleUpgrade" class="premium-button">立即开通会员</el-button>
            </div>
          </div>

          <!-- 付费内容 -->
          <div v-else class="locked-content paid">
            <div class="preview-content" v-html="getPreviewContent(article.content)"></div>
            <div class="content-locker">
              <div class="locker-icon">
                <i class="fas fa-lock"></i>
              </div>
              <h3>付费内容</h3>
              <p>支付后可查看完整内容</p>
              <el-button type="primary" @click="handlePurchase" class="premium-button">
                立即购买 ￥{{ article.price }}
              </el-button>
            </div>
          </div>
        </article>

        <!-- 文章底部 -->
        <footer class="article-footer">
          <!-- 版权声明提示 -->
          <div class="copyright-notice">
            <div class="notice-header">
              <i class="fas fa-copyright"></i>
              <span>版权声明</span>
            </div>
            <div class="notice-content">
              <div v-if="article.isOriginal" class="notice-item">
                <i class="fas fa-check-circle"></i>
                <span>本文由 <strong>{{ article.nickname }}</strong> 原创发布</span>
              </div>
              <div v-else class="notice-item">
                <i class="fas fa-share-alt"></i>
                <span>本文转载自：<a :href="article.originalUrl" target="_blank" rel="noopener noreferrer">{{
                  article.originalUrl || '未知来源' }}</a></span>
              </div>
              <div class="notice-item">
                <i class="fas fa-calendar-alt"></i>
                <span>发布时间：{{ article.createTime }}</span>
              </div>
              <div class="notice-item">
                <i class="fab fa-creative-commons-sa"></i>
                <span>
                  版权协议：
                  <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/" target="_blank"
                    rel="noopener noreferrer">
                    CC BY-NC-SA 4.0
                  </a>
                </span>
              </div>
              <div class="notice-item notice-warning">
                <i class="fas fa-exclamation-triangle"></i>
                <span>未经许可，禁止转载、摘编、复制或建立镜像。欢迎转发分享！</span>
              </div>
            </div>
          </div>

          <!-- 点赞区域 -->
          <div class="article-appreciation">
            <button class="appreciation-btn" :class="{ 'is-active': article.isLike }" @click="toggleLike">
              <div class="btn-inner">
                <div class="btn-icon">
                  <i class="fas fa-heart"></i>
                </div>
                <div class="btn-text">
                  <span>{{ article.isLike ? '已点赞' : '点赞' }}</span>
                  <span class="count">{{ article.likeNum || 0 }}</span>
                </div>
              </div>
            </button>
          </div>

          <!-- 标签部分 -->
          <div class="tags-section">
            <div class="section-header">
              <i class="fas fa-tags"></i>
              <span>文章标签</span>
            </div>
            <div class="tags-list">
              <router-link v-for="tag in article.tags" :key="tag.id" :to="`/tags?tagId=${tag.id}&tagName=${tag.name}`"
                class="tag-item">
                <span class="tag-name">{{ tag.name }}</span>
              </router-link>
            </div>
          </div>

          <!-- 分享部分 -->
          <div class="share-section">
            <div class="section-header">
              <i class="fas fa-share-alt"></i>
              <span>分享文章</span>
            </div>
            <div class="share-buttons">
              <button class="share-btn qq" @click="shareToQQ">
                <i class="fab fa-qq"></i>
                <span>QQ好友</span>
              </button>
              <button class="share-btn qzone" @click="shareToQzone">
                <i class="fas fa-star"></i>
                <span>QQ空间</span>
              </button>
              <button class="share-btn weibo" @click="shareToWeibo">
                <i class="fab fa-weibo"></i>
                <span>微博</span>
              </button>
              <button class="share-btn wechat" @click="shareToWechat">
                <i class="fab fa-weixin"></i>
                <span>微信</span>
              </button>
              <button class="share-btn link" @click="copyLink">
                <i class="fas fa-link"></i>
                <span>复制链接</span>
              </button>
            </div>
          </div>

          <!-- 导航部分 -->
          <nav class="article-nav" v-if="prevArticle || nextArticle">
            <div class="nav-header">
              <i class="fas fa-exchange-alt"></i>
              <span>相关文章</span>
            </div>
            <div class="nav-container">
              <div v-if="prevArticle" class="nav-item prev" @click="goToArticle(prevArticle.id)">
                <div class="nav-direction">
                  <i class="fas fa-arrow-left"></i>
                  <span>上一篇</span>
                </div>
                <div class="nav-title">{{ prevArticle.title }}</div>
              </div>
              <div v-if="nextArticle" class="nav-item next" @click="goToArticle(nextArticle.id)">
                <div class="nav-direction">
                  <span>下一篇</span>
                  <i class="fas fa-arrow-right"></i>
                </div>
                <div class="nav-title">{{ nextArticle.title }}</div>
              </div>
            </div>
          </nav>
        </footer>

        <!-- 评论组件 -->
        <Comment :article-id="$route.params.id" :comment-count="article.commentNum || 0"
          :article-author-id="article.userId || ''" @comment-added="handleCommentAdded"
          @comment-deleted="handleCommentDeleted" />
      </main>

      <!-- 侧边栏 -->
      <aside v-if="showSidebar" class="article-sidebar desktop-only">
        <div class="toc-container">
          <div class="toc-header">
            <div class="title-wrapper">
              <i class="fas fa-list"></i>
              <span>目录</span>
            </div>
            <div class="progress-wrapper" :class="{ completed: readProgress === 100 }">
              <i class="fas fa-book-reader"></i>
              <span class="progress-text">{{ readProgress }}</span>
            </div>
          </div>
          <div class="toc-content" v-if="tocItems.length > 0">
            <div v-for="(item, index) in tocItems" :key="index" class="toc-item" :class="{
              'active': activeHeading === item.id,
              [`level-${item.level}`]: true
            }" @click="scrollToHeading(item.id)">
              {{ item.text }}
            </div>
          </div>
          <div class="toc-empty" v-else>
            <i class="fas fa-info-circle"></i>
            <span>此文章没有目录</span>
          </div>
        </div>

        <div class="author-card">
          <div class="author-header">
            <img v-lazy="article.avatar" alt="作者头像" class="author-large-avatar">
            <h3 class="author-large-name">{{ article.nickname }}</h3>
            <p class="author-description" v-if="article.userIntro">{{ article.userIntro }}</p>
            <p class="author-description" v-else>这个人很懒，还没有填写个人介绍</p>
          </div>
          <div class="author-stats">
            <div class="stat-block">
              <div class="stat-value">{{ article.articlesCount || 0 }}</div>
              <div class="stat-label">文章</div>
            </div>
            <div class="stat-block">
              <div class="stat-value">{{ article.viewsCount || 0 }}</div>
              <div class="stat-label">阅读</div>
            </div>
            <div class="stat-block">
              <div class="stat-value">{{ article.likesCount || 0 }}</div>
              <div class="stat-label">获赞</div>
            </div>
          </div>
          <div class="author-action">
            <el-button type="primary" class="follow-button" @click="followAuthor" :disabled="isCurrentUser">
              <i class="fas" :class="isFollowing ? 'fa-check' : 'fa-plus'"></i>
              {{ isFollowing ? '已关注' : '关注' }}
            </el-button>
          </div>
        </div>

        <div class="related-articles" v-if="recommendArticles && recommendArticles.length > 0">
          <div class="related-header">
            <i class="fas fa-book"></i>
            <span>推荐阅读</span>
          </div>
          <div class="related-list">
            <div v-for="(item, index) in recommendArticles" :key="index" class="related-item"
              @click="goToArticle(item.id)">
              <div class="related-cover" v-if="item.cover">
                <img v-lazy="item.cover" alt="文章封面">
              </div>
              <div class="related-info">
                <div class="related-title">{{ item.title }}</div>
                <div class="related-meta">
                  <span><i class="far fa-eye"></i> {{ item.quantity }}</span>
                  <span><i class="far fa-heart"></i> {{ item.likeNum }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </div>
    <!-- 图片预览组件 -->
    <mj-image-preview ref="imagePreview" />
    <!-- 支付对话框 -->
    <payment-dialog :visible.sync="showPaymentDialog" :title="article.title" :price="article.price"
      :article-id="$route.params.id" @payment-success="handlePaymentSuccess" />
    <!-- 会员对话框 -->
    <membership-dialog :visible.sync="showMembershipDialog" @membership-success="handleMembershipSuccess" />
  </div>
</template>

<script>
import { getArticleDetailApi, likeArticleApi } from '@/api/article'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'
import Comment from '@/components/Comment/index.vue'
import PaymentDialog from '@/components/PaymentDialog/index.vue'
import MembershipDialog from '@/components/MembershipDialog/index.vue'
import { marked } from 'marked'
import { checkSuccess } from '@/api/user';
export default {
  name: 'Article',
  components: {
    Comment,
    PaymentDialog,
    MembershipDialog
  },
  data() {
    return {
      query: {
        userId: undefined,
        art_id: undefined,
      },
      article: {
        title: '',
        category: {},
        isOriginal: true,
        readType: 1,
        price: 0,
      },
      prevArticle: {
        id: 1,
        title: '默认文章',
      },
      nextArticle: {
        id: 1,
        title: '默认文章',
      },
      tocItems: [],
      readProgress: 0,
      showShareMenu: false,
      activeHeading: '',
      readTime: 0,
      likeDebounce: false,
      loading: false,
      actionBarLeft: '0px',
      showSidebar: true,
      contentLayout: 'center',
      collapsedCodeBlocks: new Set(),
      images: [],
      showPaymentDialog: false,
      showMembershipDialog: false,
      isAiDescriptionExpanded: true,
      isPaid: false,
      showBackToTop: false,
      isFollowing: false,
      isCurrentUser: false,
      recommendArticles: [],
    }
  },
  computed: {
    currentUrl() {
      return window.location.href
    },
    showContent() {
      // 如果文章不需要付费或已经支付，则显示完整内容
      return !this.article.needPay || this.isPaid;
    }
  },
  methods: {
    /**
     * 获取文章详情
     */
    async getArticle() {
      this.loading = true
      hljs.configure({
        ignoreUnescapedHTML: true
      })
      try {
        const res = await getArticleDetailApi(this.$route.params.id)
        this.article = {
          ...res.data,
          content: res.data.content ? this.addLazyLoadToImages(res.data.content) : ''
        }

        // 等待下一个 tick，确保文章内容渲染完成
        await this.$nextTick()

        // 使用 setTimeout 确保 DOM 完全渲染
        setTimeout(() => {
          this.generateToc()
          document.querySelectorAll('pre code').forEach((block) => {
            hljs.highlightBlock(block)
          })
          this.addCopyButtons()
          this.addLineNumbers()
          this.initImagePreview()
          this.updateActionBarPosition()

          // 添加一个额外的延时来处理代码块的展开/折叠
          this.initializeCodeBlocks()

          // AI摘要
          if (this.article.aiDescribe) {
            const typingText = this.$refs.typingText
            if (!typingText) return
            // 使用marked解析Markdown文本
            const htmlContent = marked(this.article.aiDescribe || '')
            typingText.innerHTML = htmlContent
          }
        }, 100)

        // 计算阅读时间
        const textContent = this.article.content.replace(/<[^>]+>/g, ' ')
        this.readTime = Math.ceil(textContent.split(/\s+/).length / 300)

      } catch (error) {
        this.$message.error('获取文章详情失败')
      } finally {
        this.loading = false
      }
    },
    /**
     * 为文章内容中的图片添加懒加载
     */
    addLazyLoadToImages(content) {
      // 使用data-src来存储实际图片地址，并添加lazy-image类用于识别
      return content.replace(
        /<img([^>]*)src="([^"]*)"([^>]*)>/gi,
        '<img$1src="' + this.getLoadingImage() + '" data-src="$2" class="lazy-image"$3>'
      )
    },
    /**
     * 获取加载中的图片
     */
    getLoadingImage() {
      return 'https://blog.nanshengwx.cn/upload/AnimatedEmojies-512px-406.gif'
    },
    /**
     * 生成目录
     */
    generateToc() {

      //测试locaStorage
      console.log("用户喜欢的标签："+localStorage.getItem('user_like_tags'))


      const headings = document.querySelectorAll('.article-content h1,.article-content h2,.article-content h3,.article-content h4,.article-content h5,.article-content h6')
      this.tocItems = Array.from(headings).map(heading => {
        const id = heading.textContent.trim().toLowerCase().replace(/\s+/g, '-')
        heading.id = id
        return {
          id,
          text: heading.textContent,
          level: parseInt(heading.tagName.charAt(1))
        }
      })
    },
    /**
     * 点赞
     */
    toggleLike() {
      //文章详细页面点赞可在console.log中打印出文章的tags
      const names = this.article.tags.map(tag => tag.name)
      const userId = JSON.parse(localStorage.getItem("user"))["id"]
      console.log(names)
      console.log(userId)
      // 将标签名称数组存储到localStorage
      localStorage.setItem('user_like_tags', JSON.stringify(names))
      // 将用户ID存储到localStorage
      // localStorage.setItem('current_user_id', userId)

      //防止频繁点击 设置一个5秒的防抖
      if (this.likeDebounce) {
        this.$message.warning('请于 5 秒后再试')
        return
      }
      // 实现点赞功能
      likeArticleApi(this.$route.params.id).then(res => {
        if (this.article.isLike) {
          this.article.likeNum--
        } else {
          this.article.likeNum++
        }
        this.$message.success(this.article.isLike ? '取消点赞成功' : '点赞成功')
        this.article.isLike = !this.article.isLike
        // 设置一个5秒的防抖
        this.likeDebounce = true
        setTimeout(() => {
          this.likeDebounce = false
        }, 5000)
      })
    },
    /**
     * 分享
     */
    toggleShareMenu() {
      this.showShareMenu = !this.showShareMenu
    },
    /**
     * 关闭分享
     */
    closeShareMenu() {
      this.showShareMenu = false
    },
    /**
     * 分享到QQ
     */
    shareToQQ() {
      const url = encodeURIComponent(this.currentUrl)
      const title = encodeURIComponent(this.article.title)
      const summary = encodeURIComponent(this.article.summary || '')
      const pic = encodeURIComponent(this.article.avatar || '')
      window.open(
        `https://connect.qq.com/widget/shareqq/index.html?url=${url}&title=${title}&summary=${summary}&pics=${pic}`,
        "renren-share", "width=490,height=700");
      this.closeShareMenu()
    },
    /**
     * 分享到QQ空间
     */
    shareToQzone() {
      const url = encodeURIComponent(this.currentUrl)
      const title = encodeURIComponent(this.article.title)
      const summary = encodeURIComponent(this.article.summary || '')
      const pic = encodeURIComponent(this.article.avatar || '')
      window.open(
        `https://sns.qzone.qq.com/cgi-bin/qzshare/cgi_qzshare_onekey?url=${url}&title=${title}&summary=${summary}&pics=${pic}`,
        "renren-share", "width=490,height=700"
      )

      this.closeShareMenu()
    },
    /**
     * 分享到微博
     */
    shareToWeibo() {
      const url = encodeURIComponent(this.currentUrl)
      const title = encodeURIComponent(`${this.article.title} - 拾壹博客`)
      window.open(
        `http://service.weibo.com/share/share.php?url=${url}&title=${title}`,
        "renren-share", "width=490,height=700")
      this.closeShareMenu()
    },
    /**
     * 分享到微信
     */
    shareToWechat() {
      // 由于微信分享需要使用微信SDK，这样简单处理
      window.open(
        `https://api.pwmqr.com/qrcode/create/?url=${window.location.href}`,
        "renren-share", "width=490,height=700");
      this.closeShareMenu()
    },
    /**
     * 复制链接
     */
    async copyLink() {
      try {
        await navigator.clipboard.writeText(this.currentUrl)
        this.$message.success('链接已复制到剪贴板')
      } catch (err) {
        this.$message.error('复制失败，请手动复制')
      }
      this.closeShareMenu()
    },
    /**
     * 跳转到文章
     */
    goToArticle(id) {
      this.$router.push(`/article/${id}`)
    },
    /**
     * 更新阅读进度
     */
    handleScroll() {
      const docEl = document.documentElement
      const winHeight = window.innerHeight
      const docHeight = docEl.scrollHeight - winHeight
      const scrollTop = window.scrollY || docEl.scrollTop
      this.readProgress = Math.round((scrollTop / docHeight) * 100)
      
      // 显示回到顶部按钮
      this.showBackToTop = scrollTop > 300
      
      // 更新激活的标题
      this.updateActiveHeading()
    },
    /**
     * 滚动到标题
     */
    scrollToHeading(id) {
      const element = document.getElementById(id)
      if (element) {
        const header = document.querySelector('.site-header')
        const headerHeight = header ? header.offsetHeight : 0
        const targetPosition = element.offsetTop - headerHeight - 20

        window.scrollTo({
          top: targetPosition,
          behavior: 'smooth'
        })
      }
    },
    /**
     * 更新激活标题
     */
    updateActiveHeading() {
      this.handleScroll()
      const headings = this.tocItems.map(item => document.getElementById(item.id))
      const header = document.querySelector('.site-header')
      const headerHeight = header ? header.offsetHeight : 0

      for (let i = headings.length - 1; i >= 0; i--) {
        const heading = headings[i]
        if (heading && heading.getBoundingClientRect().top <= headerHeight + 100) {
          this.activeHeading = heading.id
          break
        }
      }
    },
    /**
     * 添加复制按钮
     */
    addCopyButtons() {
      const codeBlocks = document.querySelectorAll('.article-content pre')
      if (!codeBlocks.length) return

      codeBlocks.forEach(pre => {
        // 检查是否已经添加过复制按钮
        if (pre.querySelector('.code-header')) return

        // 创建复制按钮容器
        const buttonWrapper = document.createElement('div')
        buttonWrapper.className = 'code-header'

        // 创建复制按钮
        const copyButton = document.createElement('button')
        copyButton.className = 'copy-button'
        copyButton.innerHTML = '<i class="fas fa-copy"></i> 复制'
        copyButton.title = '复制代码'

        // 添加点击事件
        copyButton.addEventListener('click', async () => {
          try {
            const code = pre.querySelector('code')
            await navigator.clipboard.writeText(code.textContent)
            copyButton.innerHTML = '<i class="fas fa-check"></i> 已复制'
            copyButton.classList.add('copied')
            setTimeout(() => {
              copyButton.innerHTML = '<i class="fas fa-copy"></i> 复制'
              copyButton.classList.remove('copied')
            }, 2000)
            this.$message.success('复制成功')
          } catch (err) {
            this.$message.error('复制失败，请手动复制')
          }
        })

        // 将按钮添加到代码块
        buttonWrapper.appendChild(copyButton)
        pre.appendChild(buttonWrapper)
      })
    },
    /**
     * 初始化图片预览
     */
    initImagePreview() {
      // 使用 IntersectionObserver 监听图片
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const img = entry.target
            const actualSrc = img.getAttribute('data-src')
            if (actualSrc) {
              // 创建一个新的图片对象来预加载
              const tempImg = new Image()
              tempImg.onload = () => {
                img.src = actualSrc
                img.classList.add('loaded')
              }
              tempImg.onerror = () => {
                img.src = 'https://img.shiyit.com/base/mojian/img-error.jpg'
                img.classList.add('error')
              }
              tempImg.src = actualSrc
              img.removeAttribute('data-src')
              observer.unobserve(img)
            }
          }
        })
      }, {
        rootMargin: '50px 0px',
        threshold: 0.1
      })

      // 监听所有带有 lazy-image 类的图片
      setTimeout(() => {
        const lazyImages = document.querySelectorAll('.lazy-image')
        lazyImages.forEach(img => {
          observer.observe(img)
        })

        // 收集所有图片URL用于预览
        this.images = Array.from(document.querySelectorAll('.article-content img')).map(img =>
          img.getAttribute('data-src') || img.getAttribute('src')
        )

        // 为图片添加点击事件
        document.querySelectorAll('.article-content img').forEach(img => {
          img.style.cursor = 'zoom-in'
          img.addEventListener('click', this.handleImageClick)
        })
      }, 200)
    },
    /**
     * 处理图片点击
     */
    handleImageClick(e) {
      const img = e.target
      if (img.tagName === 'IMG') {
        this.$refs.imagePreview.show(this.images, this.images.indexOf(img.src))
      }
    },
    toggleDislike() {
      // 实现点踩功能
      if (this.likeDebounce) {
        this.$message.warning('请于 5 秒后再试')
        return
      }
      // TODO: 调用点踩 API
      this.$message.success(this.article.isDislike ? '取消点踩成功' : '点踩成功')
      if (this.article.isDislike) {
        this.article.dislikeNum--
      } else {
        this.article.dislikeNum++
      }
      this.article.isDislike = !this.article.isDislike
      this.likeDebounce = true
      setTimeout(() => {
        this.likeDebounce = false
      }, 5000)
    },
    toggleFavorite() {
      this.$message.warning('暂未开放')
      return
      // 实现收藏功能
      this.$message.success(this.article.isFavorite ? '取消收藏成功' : '收藏成功')
      if (this.article.isFavorite) {
        this.article.favoriteNum--
      } else {
        this.article.favoriteNum++
      }
      this.article.isFavorite = !this.article.isFavorite
    },
    toggleSidebar() {
      this.showSidebar = !this.showSidebar
      // 等待 DOM 更新后重新计算操作栏位置
      this.$nextTick(() => {
        this.updateActionBarPosition()
      })
    },
    scrollToComments() {
      const commentsSection = document.querySelector('.comment-section')
      if (commentsSection) {
        commentsSection.scrollIntoView({ behavior: 'smooth' })
      }
    },
    updateActionBarPosition() {
      const articleBox = document.getElementById("articleBox")
      if (articleBox) {
        const rect = articleBox.getBoundingClientRect()
        this.actionBarLeft = (rect.left - 95) + 'px'
      }
    },
    /**
     * 初始化代码块的展开/折叠功能
     */
    initializeCodeBlocks() {
      const codeBlocks = document.querySelectorAll('.article-content pre')
      codeBlocks.forEach((pre, index) => {
        // 移除可能存在的旧按钮
        const oldButton = pre.querySelector('.expand-button')
        if (oldButton) {
          oldButton.remove()
        }

        // 获取代码块的实际高度
        const actualHeight = pre.scrollHeight

        if (actualHeight > 500) {
          // 添加折叠类
          pre.classList.add('collapsed')

          // 创建展开按钮
          const expandButton = document.createElement('button')
          expandButton.className = 'expand-button'
          expandButton.innerHTML = '<i class="fas fa-chevron-down"></i>展开代码'

          // 添加点击事件
          expandButton.onclick = (e) => {
            e.stopPropagation()
            const isCollapsed = pre.classList.contains('collapsed')
            if (isCollapsed) {
              pre.classList.remove('collapsed')
              expandButton.innerHTML = '<i class="fas fa-chevron-up"></i>收起代码'
              this.collapsedCodeBlocks.delete(index)
            } else {
              pre.classList.add('collapsed')
              expandButton.innerHTML = '<i class="fas fa-chevron-down"></i>展开代码'
              this.collapsedCodeBlocks.add(index)
            }
          }

          pre.appendChild(expandButton)
        }
      })
    },
    /**
     * 添加行号
     */
    addLineNumbers() {
      const codeBlocks = document.querySelectorAll('.article-content pre code')
      codeBlocks.forEach((code) => {
        const pre = code.parentElement

        // 检查是否已添加行号
        if (!pre.querySelector('.line-numbers')) {
          const lines = code.textContent.split('\n').length
          const lineNumbers = document.createElement('div')
          lineNumbers.className = 'line-numbers'

          for (let i = 1; i <= lines; i++) {
            const span = document.createElement('span')
            span.textContent = i
            lineNumbers.appendChild(span)
          }

          pre.insertBefore(lineNumbers, code)
        }
      })
    },
    checkPaymentStatus() {
      // 检查本地存储的支付状态
      const isPaid = localStorage.getItem(`article_paid_${this.$route.params.id}`);
      if (isPaid) {
        this.isPaid = true;
        return;
      }
      // 如果本地没有支付记录，则向服务器查询
      checkSuccess().then(res => {
        if (res.data) {
          this.isPaid = true;
          localStorage.setItem(`article_paid_${this.$route.params.id}`, 'true');
        }
      });
    },
    /**
     * 获取预览内容
     */
    getPreviewContent(content) {
      // 返回内容的前300个字符，并确保HTML标签完整
      const div = document.createElement('div')
      div.innerHTML = content
      const text = div.textContent || div.innerText
      return text.substring(0, 300) + '...'
    },
    /**
     * 处理会员升级
     */
    handleUpgrade() {
      if (!this.$store.state.userInfo) {
        this.$message.warning('请先登录')
        return
      }
      this.showMembershipDialog = true
    },
    /**
     * 处理付费购买
     */
    handlePurchase() {
      if (!this.$store.state.userInfo) {
        this.$message.warning('请先登录')
        return
      }
      this.showPaymentDialog = true
    },
    /**
     * 处理支付成功
     */
    handlePaymentSuccess() {
      // 重新获取文章信息
      this.getArticle()
    },
    /**
     * 处理会员开通成功
     */
    handleMembershipSuccess() {
      // 重新获取文章信息
      this.getArticle()
    },
    /**
     * 处理评论添加
     */
    handleCommentAdded() {
      this.article.commentNum = (this.article.commentNum || 0) + 1;
    },

    /**
     * 处理评论删除
     */
    handleCommentDeleted() {
      this.article.commentNum = Math.max((this.article.commentNum || 0) - 1, 0);
    },

    startTransition(element) {
      element.style.height = 'auto'
      const height = element.scrollHeight
      element.style.height = '0px'
      // 触发回流
      element.offsetHeight
      element.style.height = height + 'px'
    },
    endTransition(element) {
      element.style.height = element.scrollHeight + 'px'
      // 触发回流
      element.offsetHeight
      element.style.height = '0px'
    },
    /**
     * 滚动到顶部
     */
    scrollToTop() {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      })
    },

    /**
     * 关注作者
     */
    followAuthor() {
      // 这里需要实现关注作者的逻辑
      // toggleFollow(this.article.userId).then(...)
      this.$message.success(this.isFollowing ? '已取消关注' : '关注成功')
      this.isFollowing = !this.isFollowing
    },
  },
  async created() {
    await this.getArticle()
    this.checkPaymentStatus();
    window.addEventListener('resize', this.updateActionBarPosition)
  },
  mounted() {
    window.addEventListener('scroll', this.handleScroll)
    this.$nextTick(() => {
      this.initImagePreview()
    })
    if (this.$route.query.out_trade_no) {
      this.isPaid = true;
    }
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.handleScroll)
    window.removeEventListener('resize', this.updateActionBarPosition)
    // 清理图片点击事件监听器
    const images = document.querySelectorAll('.article-content img')
    images.forEach(img => {
      img.removeEventListener('click', this.handleImageClick)
    })
  },
  watch: {
    // 监听路由参数变化
    '$route'(to, from) {
      if (to.params.id !== from.params.id) {
        // 重新获取文章数据
        this.getArticleData()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.article-page {
  max-width: 1300px;
  margin: 0 auto;
  padding: $spacing-lg;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;

  @include responsive(lg) {
    padding: $spacing-sm;
  }
}

.content-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: $spacing-md * 2;
  transition: all 0.3s ease;

  &.center {
    grid-template-columns: 1fr;
    max-width: 900px;
    margin: 0 auto;
  }

  @include responsive(lg) {
    grid-template-columns: 1fr;
    gap: $spacing-lg;
    padding: 0;
  }
}

.article-main {
  background: var(--card-bg);
  border-radius: $border-radius-lg;
  box-shadow: $shadow-md;
  overflow: hidden;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: $shadow-lg;
  }
}

.article-header {
  padding: $spacing-xl $spacing-xl;
  position: relative;
  border-bottom: 1px solid var(--border-color);
  background: var(--card-bg);
  background-image: linear-gradient(to right, rgba($primary, 0.02), rgba($primary, 0.05), rgba($primary, 0.02));

  .article-title {
    color: var(--text-primary);
    font-size: 2em;
    line-height: 1.4;
    margin-bottom: $spacing-lg;
    text-align: left;
    font-weight: 700;
    letter-spacing: -0.5px;
  }

  .article-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: $spacing-md;

    .author-info {
      display: flex;
      align-items: center;
      gap: $spacing-md;

      .author-avatar {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        object-fit: cover;
        border: 2px solid rgba($primary, 0.2);
        padding: 2px;
        background: var(--card-bg);
        transition: all 0.5s ease;
        box-shadow: 0 0 10px rgba($primary, 0.1);

        &:hover {
          transform: rotate(360deg);
          border-color: $primary;
        }
      }

      .author-meta {
        display: flex;
        flex-direction: column;
        gap: $spacing-xs;

        .author-name {
          color: $primary;
          font-weight: 600;
          font-size: 1.1em;
        }

        .post-meta {
          display: flex;
          align-items: center;
          gap: $spacing-sm;
          color: var(--text-secondary);
          font-size: 0.9em;

          i {
            color: $primary;
            margin-right: 4px;
          }

          .meta-divider {
            color: var(--text-secondary);
            opacity: 0.5;
          }

          .category {
            color: $primary;
            font-weight: 500;
            transition: color 0.2s ease;
            
            &:hover {
              color: darken($primary, 10%);
            }
          }
        }
      }
    }

    .article-stats {
      display: flex;
      align-items: center;
      gap: $spacing-lg;
      background: rgba($primary, 0.05);
      padding: 8px 16px;
      border-radius: 20px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: $spacing-xs;
        color: var(--text-secondary);
        font-size: 0.95em;

        i {
          color: $primary;
          font-size: 1.1em;
        }
      }
    }
  }
  
  @include responsive(sm) {
    padding: $spacing-lg $spacing-md;
    
    .article-title {
      font-size: 1.6em;
    }
    
    .article-info {
      flex-direction: column;
      align-items: flex-start;
      
      .article-stats {
        width: 100%;
        justify-content: space-around;
        margin-top: $spacing-md;
      }
    }
  }
}

.article-content {
  padding: $spacing-xl $spacing-xl;
  line-height: 1.8;
  color: var(--text-primary);
  font-size: 1.05em;
  letter-spacing: 0.3px;

  @include responsive(sm) {
    padding: $spacing-md;
    font-size: 1em;
  }

  :deep(h2) {
    font-size: 1.8em;
    margin: $spacing-xl 0 $spacing-lg;
    padding-bottom: $spacing-sm;
    border-bottom: 2px solid rgba($primary, 0.1);
    position: relative;
    color: var(--text-primary);
    font-weight: 600;

    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      width: 50px;
      height: 2px;
      background: $primary;
      transition: width 0.3s ease;
    }
    
    &:hover::after {
      width: 100px;
    }
  }

  :deep(h3) {
    font-size: 1.5em;
    margin: $spacing-lg 0;
    color: var(--text-primary);
    position: relative;
    padding-left: $spacing-lg;
    font-weight: 600;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 24px;
      background: $primary;
      border-radius: $border-radius-sm;
    }
  }
  
  :deep(h4) {
    font-size: 1.3em;
    margin: $spacing-md 0;
    color: var(--text-primary);
    font-weight: 600;
  }

  :deep(p) {
    margin: $spacing-md 0;
    color: var(--text-secondary);
    line-height: 1.9;
    letter-spacing: 0.3px;
    text-align: justify;
  }

  :deep(a) {
    color: $primary;
    text-decoration: none;
    border-bottom: 1px dashed $primary;
    transition: all 0.3s ease;
    padding: 0 2px;

    &:hover {
      color: var(--primary-dark);
      border-bottom-style: solid;
      background: rgba($primary, 0.05);
    }
  }

  :deep(blockquote) {
    margin: $spacing-lg 0;
    padding: $spacing-md $spacing-lg;
    background: linear-gradient(to right, rgba($primary, 0.05), rgba($primary, 0.02));
    border-left: 4px solid $primary;
    border-radius: $border-radius-sm;
    color: var(--text-secondary);
    font-style: italic;
    position: relative;
    
    &::before {
      content: '"';
      position: absolute;
      top: 5px;
      left: 10px;
      color: rgba($primary, 0.2);
      font-size: 3em;
      font-family: serif;
      line-height: 1;
    }

    p {
      margin: 0;
      padding-left: 20px;
    }
  }

  :deep(ul),
  :deep(ol) {
    margin: $spacing-md 0 $spacing-md $spacing-md;
    padding-left: $spacing-xl;
    color: var(--text-secondary);

    li {
      margin-bottom: $spacing-sm;
      position: relative;
      line-height: 1.7;

      &::marker {
        color: $primary;
      }
      
      &:hover {
        color: var(--text-primary);
      }
    }
  }

  :deep(code:not([class])) {
    font-size: 14px;
    line-height: 1.5;
    position: relative;
    color: rgb(239, 89, 84);
    background: rgb(243, 244, 244);
    border-radius: 6px;
    padding: 2px 6px;
    margin: 0 $spacing-xs;
    font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
  }

  :deep(pre) {
    margin: 1.5em 0;
    position: relative;
    background: #282c34;
    border-radius: 8px;
    padding-top: 2.5em;
    overflow: hidden;
    max-height: 2000px;
    transition: max-height 0.4s ease-in-out;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);

    &.collapsed {
      max-height: 300px;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 80px;
        background: linear-gradient(transparent, #282c34);
        pointer-events: none;
        z-index: 2;
      }

      .expand-button {
        display: flex !important;
      }
    }

    .expand-button {
      position: absolute;
      bottom: 15px;
      left: 50%;
      transform: translateX(-50%);
      padding: 8px 18px;
      background: rgba(255, 255, 255, 0.1);
      border: 1px solid rgba(255, 255, 255, 0.2);
      border-radius: 20px;
      color: #abb2bf;
      cursor: pointer;
      z-index: 3;
      font-size: 0.9em;
      align-items: center;
      gap: 6px;
      transition: all 0.2s ease;
      white-space: nowrap;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
        color: #fff;
        transform: translateX(-50%) translateY(-2px);
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
      }

      i {
        font-size: 14px;
      }
    }

    /* 添加行号容器样式 */
    .line-numbers {
      position: absolute;
      left: 0;
      top: 2.5em;
      bottom: 0;
      font-size: 14px;
      padding: 1em 0;
      text-align: right;
      color: #606060;
      border-right: 1px solid #404040;
      background: #2d323b;
      user-select: none;
      z-index: 1;
      min-width: 40px;

      span {
        display: block;
        padding: 0 0.5em;
        min-width: 2.5em;
        line-height: 1.5;
      }
    }

    /* 调整代码内容的样式 */
    code {
      display: block;
      padding: 1em;
      padding-left: 4em;
      /* 增加左侧padding */
      margin-left: 0;
      /* 移除margin */
      overflow-x: auto;
      font-family: "Fira Code", "SFMono-Regular", Consolas, Menlo, monospace;
      font-size: 14px;
      line-height: 1.6;
      position: relative;
    }

    /* 添加仿 macOS 风格的按钮 */
    &::before {
      content: '';
      position: absolute;
      top: 12px;
      left: 12px;
      width: 12px;
      height: 12px;
      border-radius: 50%;
      background: #ff5f56;
      box-shadow:
        20px 0 0 #ffbd2e,
        40px 0 0 #27c93f;
    }

    /* 复制按钮容器 */
    .code-header {
      position: absolute;
      top: 8px;
      right: 12px;
      z-index: 2;
      opacity: 0;
      transition: opacity 0.2s ease;
    }

    /* 显示复制按钮 */
    &:hover .code-header {
      opacity: 1;
    }

    /* 复制按钮样式 */
    .copy-button {
      padding: 5px 10px;
      background: rgba(255, 255, 255, 0.1);
      border: none;
      border-radius: 4px;
      color: #abb2bf;
      cursor: pointer;
      font-size: 14px;
      transition: all 0.2s ease;
      display: flex;
      align-items: center;
      gap: 4px;

      i {
        font-size: 14px;
      }

      &:hover {
        background: rgba(255, 255, 255, 0.2);
        color: #fff;
      }

      &.copied {
        background: #98c379;
        color: #fff;
      }
    }
  }

  :deep(img.lazy-image) {
    opacity: 0;
    transition: opacity 0.5s ease, transform 0.3s ease;

    &.loaded {
      opacity: 1;
    }

    &.error {
      opacity: 0.5;
    }
  }

  :deep(img) {
    max-width: 100%;
    border-radius: $border-radius-md;
    margin: $spacing-lg auto;
    transition: all 0.3s ease;
    box-shadow: $shadow-md;
    cursor: zoom-in;
    display: block;

    &:hover {
      transform: translateY(-2px);
      box-shadow: $shadow-lg;
    }
  }

  :deep(table) {
    width: 100%;
    margin: $spacing-lg 0;
    border-collapse: collapse;
    border-radius: $border-radius-md;
    overflow: hidden;
    box-shadow: 0 0 0 1px var(--border-color);

    th, td {
      padding: $spacing-md;
      border: 1px solid var(--border-color);
      transition: background-color 0.2s ease;
    }

    th {
      background: rgba($primary, 0.05);
      color: var(--text-primary);
      font-weight: 600;
      text-align: left;
      position: relative;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 2px;
        background: rgba($primary, 0.2);
      }
    }

    tr:nth-child(even) {
      background: var(--hover-bg);
    }
    
    tr:hover {
      background: rgba($primary, 0.05);
    }
  }

  :deep(hr) {
    margin: $spacing-xl 0;
    border: none;
    height: 1px;
    background: linear-gradient(to right, transparent, var(--border-color), transparent);
    position: relative;

    &::before {
      content: '§';
      position: absolute;
      left: 50%;
      top: 50%;
      transform: translate(-50%, -50%);
      background: var(--card-bg);
      padding: 0 $spacing-lg;
      color: var(--text-secondary);
      font-size: 1.2em;
    }
  }

  .locked-content {
    position: relative;

    .preview-content {
      max-height: 300px;
      overflow: hidden;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 100px;
        background: linear-gradient(transparent, var(--card-bg));
        pointer-events: none;
      }
    }

    .content-locker {
      position: relative;
      margin-top: -60px;
      padding: $spacing-xl;
      text-align: center;
      background: var(--card-bg);
      border-radius: $border-radius-lg;
      box-shadow: $shadow-lg;
      z-index: 1;

      .locker-icon {
        width: 60px;
        height: 60px;
        margin: 0 auto $spacing-lg;
        background: rgba($primary, 0.1);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;

        i {
          font-size: 1.8em;
          color: $primary;
        }
      }

      h3 {
        color: var(--text-primary);
        font-size: 1.4em;
        margin-bottom: $spacing-sm;
      }

      p {
        color: var(--text-secondary);
        margin-bottom: $spacing-lg;
      }

      .el-button {
        padding: $spacing-sm $spacing-xl;
        font-size: 1.1em;
      }
    }

    &.member .locker-icon {
      background: rgba(#FFD700, 0.1);

      i {
        color: #FFD700;
      }
    }

    &.paid .locker-icon {
      background: rgba(#E6162D, 0.1);

      i {
        color: #E6162D;
      }
    }
  }
}

.article-footer {
  padding: $spacing-xl;
  border-top: 1px solid var(--border-color);
  background: linear-gradient(to bottom, transparent, rgba($primary, 0.02));

  @include responsive(sm) {
    padding: $spacing-md;
  }

  .copyright-notice {
    margin-bottom: $spacing-xl;
    background: var(--hover-bg);
    border-radius: $border-radius-lg;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0,0,0,0.05);

    .notice-header {
      padding: $spacing-md $spacing-lg;
      background: rgba($primary, 0.1);
      color: $primary;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: $spacing-sm;
    }

    .notice-content {
      padding: $spacing-lg;
      color: var(--text-secondary);
      font-size: 0.95em;
      line-height: 1.6;

      .notice-item {
        display: flex;
        align-items: center;
        gap: $spacing-sm;
        padding: $spacing-xs 0;

        i {
          color: $primary;
          font-size: 1em;
          width: 16px;
          text-align: center;
        }

        a {
          color: $primary;
          text-decoration: none;
          border-bottom: 1px dashed $primary;
          transition: all 0.2s ease;

          &:hover {
            border-bottom-style: solid;
          }
        }

        &.notice-warning {
          margin-top: $spacing-sm;
          padding: $spacing-sm $spacing-md;
          background: rgba($primary, 0.05);
          border-radius: $border-radius-sm;
          border-left: 3px solid #ff9800;

          i {
            color: #ff9800;
          }
        }
      }
    }
  }

  .tags-section {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    margin-bottom: $spacing-xl;
    flex-wrap: wrap;

    i {
      color: $primary;
    }

    .tags-list {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-sm;
    }

    .tag-item {
      padding: $spacing-xs $spacing-md;
      background: var(--hover-bg);
      color: var(--text-secondary);
      border-radius: $border-radius-lg;
      font-size: 0.9em;
      text-decoration: none;
      transition: all 0.3s ease;
      border: 1px solid transparent;

      &:hover {
        background: rgba($primary, 0.1);
        color: $primary;
        transform: translateY(-2px);
        border-color: rgba($primary, 0.2);
        box-shadow: 0 3px 8px rgba($primary, 0.1);
      }
    }
  }

  .article-appreciation {
    margin-bottom: $spacing-xl;

    .appreciation-btn {
      display: inline-flex;
      align-items: center;
      gap: $spacing-sm;
      padding: $spacing-sm $spacing-xl;
      border: none;
      border-radius: 24px;
      font-size: 1em;
      cursor: pointer;
      transition: all 0.3s ease;

      &.is-active {
        background: rgba($primary, 0.1);
        color: $primary;
      }

      &:hover {
        transform: scale(1.05);
        box-shadow: 0 4px 12px rgba($primary, 0.1);
      }
    }
  }

  .share-section {
    margin-bottom: $spacing-xl;

    .section-header {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      font-weight: 500;
      margin-bottom: $spacing-md;
    }

    .share-buttons {
      display: flex;
      gap: $spacing-md;

      .share-btn {
        display: inline-flex;
        align-items: center;
        gap: $spacing-sm;
        padding: $spacing-sm $spacing-xl;
        border: none;
        border-radius: 24px;
        font-size: 1em;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          transform: scale(1.05);
          box-shadow: 0 4px 12px rgba($primary, 0.1);
        }
      }
    }
  }

  .article-nav {
    margin-top: $spacing-xl;

    .nav-header {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      font-weight: 500;
      margin-bottom: $spacing-md;
    }

    .nav-container {
      display: flex;
      gap: $spacing-md;

      .nav-item {
        display: flex;
        align-items: center;
        gap: $spacing-sm;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          color: $primary;
          transform: translateX(5px);
        }
      }
    }
  }
}

.article-sidebar {
  .toc-container {
    position: sticky;
    top: 90px;
    background: var(--card-bg);
    border-radius: $border-radius-lg;
    box-shadow: $shadow-md;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border: 1px solid var(--border-color);
    backdrop-filter: blur(10px);

    &:hover {
      box-shadow: $shadow-lg;
      border-color: rgba($primary, 0.2);
      
      &::before {
        opacity: 1;
      }
    }

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(45deg, transparent, rgba($primary, 0.05), transparent);
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    .toc-header {
      padding: $spacing-lg;
      background: rgba($primary, 0.05);
      color: var(--text-primary);
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      position: relative;
      border-bottom: 1px solid var(--border-color);
      justify-content: space-between;

      .title-wrapper {
        display: flex;
        align-items: center;
        gap: $spacing-sm;

        i {
          color: $primary;
          font-size: 1.1em;
          transform-origin: center;
          animation: tocIconPulse 2s infinite;
        }
      }

      .progress-wrapper {
        font-size: 0.9em;
        color: var(--text-secondary);
        display: flex;
        align-items: center;
        gap: $spacing-xs;
        padding: 4px 10px;
        background: rgba($primary, 0.1);
        border-radius: $border-radius-lg;
        transition: all 0.3s ease;

        &.completed {
          background: rgba(green, 0.1);
          color: green;
          
          i {
            color: green;
          }
        }

        i {
          color: $primary;
          font-size: 0.9em;
        }

        .progress-text {
          font-variant-numeric: tabular-nums;
          min-width: 3em;
          text-align: right;

          &::after {
            content: '%';
            margin-left: 2px;
            opacity: 0.7;
          }
        }
      }
    }

    .toc-content {
      padding: $spacing-lg;
      max-height: calc(100vh - 200px);
      overflow-y: auto;
      position: relative;

      /* 添加美化滚动条 */
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: rgba($primary, 0.2);
        border-radius: 3px;
      }
      
      &::-webkit-scrollbar-track {
        background: transparent;
      }

      &::before {
        content: '';
        position: absolute;
        left: 24px;
        top: 0;
        bottom: 0;
        width: 1px;
        background: linear-gradient(to bottom,
            transparent,
            rgba($primary, 0.1),
            rgba($primary, 0.1),
            transparent);
      }

      .toc-item {
        padding: $spacing-sm $spacing-md;
        margin: 3px 0;
        cursor: pointer;
        border-radius: $border-radius-sm;
        color: var(--text-secondary);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        font-size: 0.95em;
        line-height: 1.4;
        position: relative;
        display: flex;
        align-items: center;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        padding-left: 16px;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 0;
          height: 0;
          background: $primary;
          border-radius: 50%;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          opacity: 0;
          box-shadow: 0 0 4px rgba($primary, 0.4);
        }

        &:hover {
          color: $primary;
          background: linear-gradient(90deg,
              rgba($primary, 0.05),
              rgba($primary, 0.02));
          padding-left: 20px;

          &::before {
            width: 6px;
            height: 6px;
            opacity: 1;
          }
        }

        &.active {
          color: $primary;
          background: linear-gradient(90deg,
              rgba($primary, 0.1),
              rgba($primary, 0.05));
          font-weight: 500;
          padding-left: 20px;

          &::before {
            width: 6px;
            height: 6px;
            opacity: 1;
            animation: tocDotPulse 1.5s infinite;
          }
        }

        &.level-1 {
          font-weight: 600;
          font-size: 1em;
          padding-left: 12px;
        }

        &.level-2 {
          font-size: 0.95em;
          margin-left: 8px;
        }

        &.level-3 {
          font-size: 0.9em;
          margin-left: 16px;
        }

        &.level-4 {
          font-size: 0.88em;
          margin-left: 24px;
        }

        &.level-5,
        &.level-6 {
          font-size: 0.86em;
          margin-left: 32px;
          opacity: 0.8;

          &:hover {
            opacity: 1;
          }
        }
      }
    }
  }

  .author-card {
    padding: $spacing-lg;
    background: var(--card-bg);
    border-radius: $border-radius-lg;
    box-shadow: $shadow-md;
    margin-top: $spacing-md;

    .author-header {
      display: flex;
      align-items: center;
      gap: $spacing-md;
      margin-bottom: $spacing-lg;

      .author-large-avatar {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        object-fit: cover;
      }

      .author-large-name {
        font-size: 1.4em;
        font-weight: 600;
      }

      .author-description {
        color: var(--text-secondary);
        font-size: 0.9em;
      }
    }

    .author-stats {
      display: flex;
      gap: $spacing-md;
      margin-bottom: $spacing-lg;

      .stat-block {
        display: flex;
        flex-direction: column;
        align-items: center;

        .stat-value {
          font-size: 1.2em;
          font-weight: 600;
        }

        .stat-label {
          color: var(--text-secondary);
          font-size: 0.9em;
        }
      }
    }

    .author-action {
      text-align: right;
    }
  }

  .related-articles {
    padding: $spacing-lg;
    background: var(--card-bg);
    border-radius: $border-radius-lg;
    margin-top: $spacing-md;

    .related-header {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      font-weight: 500;
      margin-bottom: $spacing-md;
    }

    .related-list {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-md;

      .related-item {
        width: calc(50% - 8px);
        display: flex;
        align-items: center;
        gap: $spacing-md;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          color: $primary;
          transform: translateX(5px);
        }

        .related-cover {
          width: 100px;
          height: 100px;
          border-radius: $border-radius-md;
          overflow: hidden;
          margin-right: $spacing-md;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }

        .related-info {
          flex: 1;

          .related-title {
            font-size: 1.2em;
            font-weight: 600;
          }

          .related-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            color: var(--text-secondary);
            font-size: 0.9em;

            i {
              margin-left: 4px;
            }
          }
        }
      }
    }
  }
}

@keyframes tocIconPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

@keyframes tocDotPulse {
  0% {
    box-shadow: 0 0 0 0 rgba($primary, 0.4);
  }

  70% {
    box-shadow: 0 0 0 4px rgba($primary, 0);
  }

  100% {
    box-shadow: 0 0 0 0 rgba($primary, 0);
  }
}

.desktop-only {
  @include responsive(lg) {
    display: none;
  }
}

.floating-action-bar {
  position: fixed;
  top: 40%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: $spacing-md * 1.5;
  padding: $spacing-sm;
  border-radius: $border-radius-lg;
  z-index: 100;
  transition: left 0.3s ease;

  @include responsive(lg) {
    display: none;
  }

  .action-item {
    position: relative;
    cursor: pointer;

    .action-button {
      width: 45px;
      height: 45px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      background: var(--card-bg);
      transition: all 0.3s ease;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      border: 2px solid transparent;

      i {
        font-size: 1.2em;
        color: var(--text-secondary);
        transition: all 0.3s ease;

        &.active {
          color: $primary;
        }
      }
    }

    .action-count {
      font-size: 0.8em;
      color: var(--text-secondary);
      min-width: 20px;
      text-align: center;
    }

    &:hover {
      .action-button {
        background: rgba($primary, 0.1);
        transform: translateY(-2px);
        border-color: rgba($primary, 0.3);
        box-shadow: 0 5px 15px rgba($primary, 0.2);

        i {
          color: $primary;
          transform: scale(1.1);
        }
      }
    }
  }

  .reward-item {
    position: relative;

    .reward-popup {
      position: absolute;
      left: calc(100% + 16px);
      top: 50%;
      transform: translateY(-50%);
      background: var(--card-bg);
      border-radius: $border-radius-lg;
      padding: $spacing-lg;
      box-shadow: $shadow-lg;
      opacity: 0;
      visibility: hidden;
      transition: all 0.3s ease;
      pointer-events: none;
      width: 510px;
      border: 1px solid var(--border-color);

      &::before {
        content: '';
        position: absolute;
        left: -6px;
        top: 50%;
        transform: translateY(-50%) rotate(45deg);
        width: 12px;
        height: 12px;
        background: var(--card-bg);
        border-radius: 2px;
        border-left: 1px solid var(--border-color);
        border-bottom: 1px solid var(--border-color);
      }

      .reward-title {
        font-size: 1.4em;
        font-weight: 500;
        margin-bottom: $spacing-md;
      }

      .reward-content {
        display: flex;
        gap: $spacing-md;
        margin-bottom: $spacing-md;

        .reward-qr-container {
          width: 250px;
          height: 250px;
          border-radius: $border-radius-sm;
          overflow: hidden;
          border: 1px solid rgba($primary, 0.1);
          transition: transform 0.3s ease;
          
          &:hover {
            transform: scale(1.02);
          }

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }

        .reward-label {
          color: var(--text-secondary);
          font-size: 0.9em;
          padding: $spacing-sm;
          background: rgba($primary, 0.05);
          border-radius: $border-radius-sm;
          font-weight: 500;
        }
      }

      .reward-text {
        text-align: center;
        color: var(--text-secondary);
        font-size: 0.95em;
        padding: $spacing-sm;
        background: rgba($primary, 0.05);
        border-radius: $border-radius-sm;
        font-weight: 500;
      }
    }

    &:hover {
      .reward-popup {
        opacity: 1;
        visibility: visible;
        pointer-events: auto;
        transform: translateY(-50%) translateX(8px);
      }
    }
  }
}

.ai-description {
  margin: $spacing-md $spacing-xl;
  border-radius: $border-radius-lg;
  background-image: linear-gradient(180deg, rgba(247, 232, 255, .6), rgba(191, 223, 255, .4));
  border: 1px solid rgba(0, 150, 136, 0.1);
  transition: all 0.3s ease;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba($primary, 0.1);

  @include responsive(sm) {
    margin: $spacing-sm;
  }

  .ai-header {
    padding: $spacing-sm $spacing-md;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    justify-content: flex-start;
    color: #a855f7;
    cursor: pointer;
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(5px);

    i {
      font-size: 1.2em;
      transition: transform 0.3s ease;
    }
    
    &:hover i {
      transform: rotate(15deg);
    }
  }

  .ai-content {
    padding: $spacing-md $spacing-lg;
    overflow: hidden;
    background: rgba(255, 255, 255, 0.7);
    border-top: 1px dashed rgba($primary, 0.2);
  }

  .ai-description-text {
    margin-top: $spacing-sm;
    color: #8c8a8e;
    font-style: italic;
    text-align: right;
    font-size: 0.9em;
  }
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  height: 0;
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
}
</style>