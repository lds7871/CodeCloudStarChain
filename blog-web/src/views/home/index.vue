<template>
  <div>

    <div class="home">
      <!-- 新增背景层 -->
      <div class="background-layer">
        <!-- 添加粒子系统 -->
        <div class="particles">
          <div v-for="n in 50" :key="n" class="particle"></div>
        </div>
        <!-- 添加网格背景 -->
        <div class="grid-overlay"></div>
        
        <div class="centered-content">
          <div class="logo-container">
            <img src="@/assets/LDS/LDSLOGO.png" alt="Centered Image"/>
            <div class="glow-ring"></div>
            <div class="glow-ring ring-2"></div>
          </div>
        </div>
        <div class="FirstText">
          <div class="title" ref="titleRef">
            <span class="title-text">{{ titleText }}</span>
            <div class="title-underline"></div>
          </div>
        </div>
        <div class="clickroll" :class="{ 'fade-out': isClicked }" @click="scrollDown">
          <span>点击开启</span>
        </div>
      </div>
      <!-- 主内容布局 -->
      <!--    占位符-->
      <div class="placeholder"></div>
      <!--    波浪过度-->


      <!--    主要内容-->
      <div class="content-layout">
        <main class="home-main-content">
          <!--           轮播图组件，显示轮播文章-->
          <Carousel
              v-if="carouselSlides?.length > 0"
              :slides="carouselSlides"
              @click="goToPost"
          />
          <!--           动态列表组件-->
          <MomentsList/>

          <div class="tabs-container">
            <!-- 标签页组件 -->
            <el-tabs v-model="activeName" @tab-click="handleClick" class="sci-fi-tabs">
              <!-- 遍历分类，生成每个标签页 -->
              <el-tab-pane v-for="category in categories" :key="category.id" :name="String(category.id)">
                <template slot="label">
                  <span class="label-info">
                    <i :class="category.icon"></i>
                    {{ category.name }}
                  </span>
                </template>
                <!-- 文章列表组件 -->
                <ArticleList :articles="articleList" :loading="loading" :total="total" :params="params"
                             @article-click="goToPost" @page-change="changePage" class="article-list"/>
              </el-tab-pane>
            </el-tabs>
          </div>

        </main>
        <!--       侧边栏组件 -->
        <Sidebar/>

      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import ArticleList from "@/components/ArticleList/index.vue";
import Carousel from "@/views/home/components/carousel.vue";
import Sidebar from "@/components/Sidebar/index.vue";
import MomentsList from "@/views/home/components/moments.vue";
import {getAllCategoriesApi, getArticlesApi, getCarouselArticlesApi,} from "@/api/article";

export default {
  name: "Home",
  components: {
    ArticleList,
    Carousel,
    Sidebar,
    MomentsList,
  },
  data() {
    return {
      total: 0, // 文章总数
      isClicked: false,
      params: {
        pageNum: 1, // 当前页码
        pageSize: 10, // 每页文章数量
      },
      articleList: [], // 文章列表数据
      carouselSlides: [], // 轮播图数据
      loading: false, // 加载状态
      activeName: "all", // 当前激活的标签页
      categories: [
        {
          id: "all", // 默认分类
          name: "全部",
          icon: "el-icon-menu",
        },
      ],
      titleText: '',
    };
  },
  methods: {
    /**
     * 切换标签
     * @param {string} tab 标签
     */
    async fetchSentence() {
      try {
        const response = await axios.get('https://api.apiopen.top/api/sentences')
        if (response.data.code === 200) {
          this.titleText = response.data.result.name
        }
      } catch (error) {
        console.log('获取句子失败:', error)
        this.titleText = '欢迎来到我的博客' // 设置默认文本
      }
    },
    handleClick(tab) {
      // 根据标签切换分类
      this.params.categoryId = tab.name === "all" ? null : tab.name;
      this.params.pageNum = 1; // 重置页码
      this.getArticleList(); // 获取文章列表
    },
    /**
     * 跳转到文章详情
     * @param {number} id 文章id
     */
    goToPost(id) {
      this.$router.push(`/post/${id}`); // 跳转到文章详情页
    },
    /**
     * 切换页码
     * @param {number} page 页码
     */
    changePage(page) {
      this.params.pageNum = page; // 更新页码
      this.getArticleList(); // 获取文章列表
      // 平滑滚动到顶部
      window.scrollTo({
        top: this.$refs.postsSection?.offsetTop - 80,
        behavior: "smooth",
      });
    },
    /**
     * 获取文章列表
     */
    getArticleList() {
      this.loading = true; // 开启加载状态
      getArticlesApi(this.params)
          .then((res) => {
            this.articleList = res.data.records; // 更新文章列表
            this.total = res.data.total; // 更新文章总数
          })
          .catch((error) => {
            console.error("Failed to fetch articles:", error); // 错误处理
          })
          .finally(() => {
            this.loading = false; // 关闭加载状态
          });
    },
    /**
     * 获取轮播和推荐文章
     */
    getCarouselArticles() {
      getCarouselArticlesApi().then((res) => {
        this.carouselSlides = res.data; // 更新轮播图数据
      });
    },
    /**
     * 获取所有分类
     */
    getAllCategories() {
      getAllCategoriesApi().then((res) => {
        // 为每个分类随机分配图标
        const icons = [
          "el-icon-document",
          "el-icon-collection",
          "el-icon-reading",
          "el-icon-coffee-cup",
          "el-icon-notebook-2",
          "el-icon-edit",
        ];
        const categoriesWithIcons = res.data.map((category) => ({
          ...category,
          icon: icons[Math.floor(Math.random() * icons.length)],
        }));
        this.categories.push(...categoriesWithIcons); // 添加分类到列表
      });
    },
    scrollDown() {
      this.isClicked = true;
      window.scrollBy({
        top: window.innerHeight, // 滚动 100vh
        behavior: 'smooth', // 平滑滚动
      });
    }
  },
  created() {
    // 初始化时获取数据
    this.fetchSentence();
    this.getArticleList();
    this.getCarouselArticles();
    this.getAllCategories();
  }, mounted() {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    if (token) {
      this.$store.dispatch('giteeLogin', token)
          .then((data) => {
            if (data) {
              this.$message.success("登录成功");
              // 登录成功后刷新用户信息
              this.$store.dispatch('getUserInfo');
              // 移除 URL 中的 code 参数，但不刷新页面
              const newUrl = window.location.pathname;
              window.history.replaceState({}, '', newUrl);
              // 延迟跳转，确保数据已经保存
              setTimeout(() => {
                this.$router.replace('/');
              }, 300);
            } else {
              this.$message.error("登录失败：未获取到用户信息");
            }
          })
          .catch(error => {
            this.$message.error("登录失败，请重试");
            console.error(error);
            // 登录失败后清理状态
            this.$store.dispatch('logout');
          });
    }
  }
  ,
  computed: {},
};
</script>

<style lang="scss" scoped>
// 粒子系统
.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
  
  .particle {
    position: absolute;
    width: 2px;
    height: 2px;
    background: linear-gradient(45deg, #00f5ff, #0080ff);
    border-radius: 50%;
    animation: float 6s ease-in-out infinite;
    box-shadow: 0 0 10px #00f5ff;
    
    &:nth-child(1) { left: 10%; top: 20%; animation-delay: 0ms; animation-duration: 4s; width: 2px; height: 2px; }
    &:nth-child(2) { left: 30%; top: 40%; animation-delay: 200ms; animation-duration: 5s; width: 3px; height: 3px; }
    &:nth-child(3) { left: 50%; top: 60%; animation-delay: 400ms; animation-duration: 6s; width: 1px; height: 1px; }
    &:nth-child(4) { left: 70%; top: 80%; animation-delay: 600ms; animation-duration: 7s; width: 2px; height: 2px; }
    &:nth-child(5) { left: 90%; top: 10%; animation-delay: 800ms; animation-duration: 4s; width: 3px; height: 3px; }
    &:nth-child(6) { left: 20%; top: 70%; animation-delay: 1000ms; animation-duration: 5s; width: 2px; height: 2px; }
    &:nth-child(7) { left: 40%; top: 90%; animation-delay: 1200ms; animation-duration: 6s; width: 1px; height: 1px; }
    &:nth-child(8) { left: 60%; top: 30%; animation-delay: 1400ms; animation-duration: 7s; width: 3px; height: 3px; }
    &:nth-child(9) { left: 80%; top: 50%; animation-delay: 1600ms; animation-duration: 4s; width: 2px; height: 2px; }
    &:nth-child(10) { left: 15%; top: 85%; animation-delay: 1800ms; animation-duration: 5s; width: 1px; height: 1px; }
    &:nth-child(11) { left: 35%; top: 15%; animation-delay: 2000ms; animation-duration: 6s; width: 3px; height: 3px; }
    &:nth-child(12) { left: 55%; top: 45%; animation-delay: 2200ms; animation-duration: 7s; width: 2px; height: 2px; }
    &:nth-child(13) { left: 75%; top: 75%; animation-delay: 2400ms; animation-duration: 4s; width: 1px; height: 1px; }
    &:nth-child(14) { left: 95%; top: 25%; animation-delay: 2600ms; animation-duration: 5s; width: 3px; height: 3px; }
    &:nth-child(15) { left: 25%; top: 95%; animation-delay: 2800ms; animation-duration: 6s; width: 2px; height: 2px; }
    &:nth-child(16) { left: 45%; top: 35%; animation-delay: 3000ms; animation-duration: 7s; width: 1px; height: 1px; }
    &:nth-child(17) { left: 65%; top: 65%; animation-delay: 3200ms; animation-duration: 4s; width: 3px; height: 3px; }
    &:nth-child(18) { left: 85%; top: 5%; animation-delay: 3400ms; animation-duration: 5s; width: 2px; height: 2px; }
    &:nth-child(19) { left: 5%; top: 55%; animation-delay: 3600ms; animation-duration: 6s; width: 1px; height: 1px; }
    &:nth-child(20) { left: 75%; top: 25%; animation-delay: 3800ms; animation-duration: 7s; width: 3px; height: 3px; }
    &:nth-child(21) { left: 12%; top: 78%; animation-delay: 4000ms; animation-duration: 4s; width: 2px; height: 2px; }
    &:nth-child(22) { left: 32%; top: 18%; animation-delay: 4200ms; animation-duration: 5s; width: 1px; height: 1px; }
    &:nth-child(23) { left: 52%; top: 48%; animation-delay: 4400ms; animation-duration: 6s; width: 3px; height: 3px; }
    &:nth-child(24) { left: 72%; top: 88%; animation-delay: 4600ms; animation-duration: 7s; width: 2px; height: 2px; }
    &:nth-child(25) { left: 92%; top: 38%; animation-delay: 4800ms; animation-duration: 4s; width: 1px; height: 1px; }
    &:nth-child(26) { left: 22%; top: 68%; animation-delay: 5000ms; animation-duration: 5s; width: 3px; height: 3px; }
    &:nth-child(27) { left: 42%; top: 8%; animation-delay: 5200ms; animation-duration: 6s; width: 2px; height: 2px; }
    &:nth-child(28) { left: 62%; top: 58%; animation-delay: 5400ms; animation-duration: 7s; width: 1px; height: 1px; }
    &:nth-child(29) { left: 82%; top: 28%; animation-delay: 5600ms; animation-duration: 4s; width: 3px; height: 3px; }
    &:nth-child(30) { left: 2%; top: 82%; animation-delay: 5800ms; animation-duration: 5s; width: 2px; height: 2px; }
    &:nth-child(31) { left: 38%; top: 12%; animation-delay: 0ms; animation-duration: 6s; width: 1px; height: 1px; }
    &:nth-child(32) { left: 58%; top: 42%; animation-delay: 200ms; animation-duration: 7s; width: 3px; height: 3px; }
    &:nth-child(33) { left: 78%; top: 72%; animation-delay: 400ms; animation-duration: 4s; width: 2px; height: 2px; }
    &:nth-child(34) { left: 98%; top: 32%; animation-delay: 600ms; animation-duration: 5s; width: 1px; height: 1px; }
    &:nth-child(35) { left: 28%; top: 92%; animation-delay: 800ms; animation-duration: 6s; width: 3px; height: 3px; }
    &:nth-child(36) { left: 48%; top: 22%; animation-delay: 1000ms; animation-duration: 7s; width: 2px; height: 2px; }
    &:nth-child(37) { left: 68%; top: 52%; animation-delay: 1200ms; animation-duration: 4s; width: 1px; height: 1px; }
    &:nth-child(38) { left: 88%; top: 2%; animation-delay: 1400ms; animation-duration: 5s; width: 3px; height: 3px; }
    &:nth-child(39) { left: 8%; top: 62%; animation-delay: 1600ms; animation-duration: 6s; width: 2px; height: 2px; }
    &:nth-child(40) { left: 78%; top: 32%; animation-delay: 1800ms; animation-duration: 7s; width: 1px; height: 1px; }
    &:nth-child(41) { left: 14%; top: 76%; animation-delay: 2000ms; animation-duration: 4s; width: 3px; height: 3px; }
    &:nth-child(42) { left: 34%; top: 16%; animation-delay: 2200ms; animation-duration: 5s; width: 2px; height: 2px; }
    &:nth-child(43) { left: 54%; top: 46%; animation-delay: 2400ms; animation-duration: 6s; width: 1px; height: 1px; }
    &:nth-child(44) { left: 74%; top: 86%; animation-delay: 2600ms; animation-duration: 7s; width: 3px; height: 3px; }
    &:nth-child(45) { left: 94%; top: 36%; animation-delay: 2800ms; animation-duration: 4s; width: 2px; height: 2px; }
    &:nth-child(46) { left: 24%; top: 66%; animation-delay: 3000ms; animation-duration: 5s; width: 1px; height: 1px; }
    &:nth-child(47) { left: 44%; top: 6%; animation-delay: 3200ms; animation-duration: 6s; width: 3px; height: 3px; }
    &:nth-child(48) { left: 64%; top: 56%; animation-delay: 3400ms; animation-duration: 7s; width: 2px; height: 2px; }
    &:nth-child(49) { left: 84%; top: 26%; animation-delay: 3600ms; animation-duration: 4s; width: 1px; height: 1px; }
    &:nth-child(50) { left: 4%; top: 84%; animation-delay: 3800ms; animation-duration: 5s; width: 3px; height: 3px; }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) translateX(0px) scale(1);
    opacity: 0.3;
  }
  25% {
    transform: translateY(-20px) translateX(10px) scale(1.2);
    opacity: 0.8;
  }
  50% {
    transform: translateY(-10px) translateX(-15px) scale(0.8);
    opacity: 1;
  }
  75% {
    transform: translateY(-30px) translateX(5px) scale(1.1);
    opacity: 0.6;
  }
}

// 网格背景
.grid-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(0, 245, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 245, 255, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridPulse 4s ease-in-out infinite;
  z-index: 0;
}

@keyframes gridPulse {
  0%, 100% {
    opacity: 0.3;
  }
  50% {
    opacity: 0.6;
  }
}

// Logo容器增强
.logo-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  
  img {
    position: relative;
    z-index: 2;
    filter: drop-shadow(0 0 20px rgba(0, 245, 255, 0.5));
    animation: logoGlow 3s ease-in-out infinite;
  }
  
  .glow-ring {
    position: absolute;
    border: 2px solid transparent;
    border-radius: 50%;
    animation: ringRotate 8s linear infinite;
    
    &:nth-child(2) {
      width: 620px;
      height: 620px;
      background: conic-gradient(from 0deg, transparent, #00f5ff, transparent, #0080ff, transparent);
      mask: radial-gradient(circle, transparent 300px, black 302px, black 308px, transparent 310px);
      -webkit-mask: radial-gradient(circle, transparent 300px, black 302px, black 308px, transparent 310px);
    }
    
    &.ring-2 {
      width: 650px;
      height: 650px;
      animation-duration: 12s;
      animation-direction: reverse;
      background: conic-gradient(from 180deg, transparent, #ff006e, transparent, #8338ec, transparent);
      mask: radial-gradient(circle, transparent 315px, black 317px, black 323px, transparent 325px);
      -webkit-mask: radial-gradient(circle, transparent 315px, black 317px, black 323px, transparent 325px);
    }
  }
}

@keyframes logoGlow {
  0%, 100% {
    filter: drop-shadow(0 0 20px rgba(0, 245, 255, 0.5));
  }
  50% {
    filter: drop-shadow(0 0 40px rgba(0, 245, 255, 0.8)) drop-shadow(0 0 60px rgba(255, 0, 110, 0.3));
  }
}

@keyframes ringRotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 点击按钮增强
.clickroll {
  cursor: pointer;
  border-radius: 25px;
  position: absolute;
  left: 50%;
  top: 80%;
  transform: translateX(-50%);
  color: rgba(255, 255, 255, 0.9);
  font-size: 2.5rem;
  letter-spacing: 8px;
  // backdrop-filter: blur(15px); 
  text-align: center;
  transition: all 0.8s ease;
  visibility: visible;
  padding: 20px 40px;
  // border: 2px solid rgba(0, 245, 255, 0.3);
  // background: rgba(0, 0, 0, 0.2);
  position: relative;
  overflow: hidden;
  
  span {
    position: relative;
    z-index: 2;
    text-shadow: 0 0 10px rgba(0, 245, 255, 0.8);
  }
  

  
  &:hover {
    border-color: rgba(0, 245, 255, 0.8);
    box-shadow: 
      0 0 20px rgba(0, 245, 255, 0.4),
      inset 0 0 20px rgba(0, 245, 255, 0.1);
    transform: translateX(-50%) scale(1.05);
  }

  &.fade-out {
    opacity: 0;
    transform: translateX(-50%) translateY(-800%) scale(0.8);
    pointer-events: none;
    visibility: hidden;
  }
}



// 标题增强
.title {
  width: 100%;
  letter-spacing: 0.1em;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  position: relative;
  
  .title-text {
    -webkit-text-fill-color: transparent;
    font-weight: 700;
    background: linear-gradient(
      45deg, 
      #00f5ff, 
      #0080ff, 
      #8338ec, 
      #ff006e, 
      #00f5ff
    );
    background-size: 300% 300%;
    background-clip: text;
    -webkit-background-clip: text;
    animation: gradientShift 4s ease-in-out infinite;
    text-shadow: 0 0 30px rgba(0, 245, 255, 0.5);
    filter: drop-shadow(0 0 10px rgba(0, 245, 255, 0.3));
  }
  
  .title-underline {
    width: 200px;
    height: 2px;
    background: linear-gradient(90deg, transparent, #00f5ff, #0080ff, #00f5ff, transparent);
    margin-top: 15px;
    animation: underlineGlow 2s ease-in-out infinite;
  }
}

@keyframes gradientShift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes underlineGlow {
  0%, 100% {
    box-shadow: 0 0 5px rgba(0, 245, 255, 0.3);
  }
  50% {
    box-shadow: 0 0 15px rgba(0, 245, 255, 0.8);
  }
}

.FirstText {
  width: 100%;
  position: absolute;
  top: 73%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 2.5rem;
  padding: 0.5em;
  border-radius: 0.5em;
  height: 90px;
  z-index: 1;
  opacity: 1;
}

.centered-content {
  position: absolute;
  top: 40%;
  left: 50%;
  transform: translate(-50%, -50%);

  img {
    width: 600px;
    height: 600px;
    max-width: 100%;
    max-height: 100%;
  }
}

.placeholder {
  width: 1%;
  height: 93vh;
  background-color: transparent;
}

.background-layer {
  z-index: -1;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  background: url('@/assets/LDS/bg.jpg') no-repeat center center;
  background-size: cover;
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle at 30% 70%, rgba(0, 245, 255, 0.1) 0%, transparent 50%),
                radial-gradient(circle at 70% 30%, rgba(131, 56, 236, 0.1) 0%, transparent 50%),
                rgba(0, 0, 0, 0.3);
  }
}

.home {
  z-index: 2;
  margin: 0 auto;
  width: 100%;
  height: 100%;
  position: relative;

  @include responsive(lg) {
    padding: $spacing-sm;
  }
}

.content-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: $spacing-lg * 2;
  padding: $spacing-xl *2.5 $spacing-xl *1.5;
  min-height: calc(100vh - 80px);
  align-items: start;

  background: var(--content-gradient);
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: 
      radial-gradient(circle at 20% 80%, rgba(0, 245, 255, 0.05) 0%, transparent 50%),
      radial-gradient(circle at 80% 20%, rgba(255, 0, 110, 0.05) 0%, transparent 50%);
    pointer-events: none;
    z-index: -1;
  }

  transition: background 0.3s ease;

  @include responsive(lg) {
    grid-template-columns: 1fr;
    padding: $spacing-sm;
  }
}

.home-main-content {
  min-width: 0;
  width: 100%;
  height: 100%;

  .carousel {
    margin-bottom: $spacing-xl;
    width: 100%;
    max-height: 480px;

    @include responsive(md) {
      margin-bottom: $spacing-xl;
      max-height: 280px;

      :deep(h3) {
        font-size: 1.2em;
      }
    }
  }
}

// 科幻标签页样式
.tabs-container {
  position: relative;
  border: 1px solid rgba(0, 245, 255, 0.2);
  border-radius: 15px;
  padding: 20px;
  // background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  overflow: hidden;
  
  // 流光边框效果
  &::before {
    content: '';
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    background: linear-gradient(45deg, 
      rgba(0, 245, 255, 0.2) 0%,
      rgba(131, 56, 236, 0.2) 25%,
      rgba(255, 0, 110, 0.2) 50%,
      rgba(0, 245, 255, 0.2) 75%,
      rgba(131, 56, 236, 0.2) 100%);
    background-size: 400% 400%;
    border-radius: 17px;
    z-index: -1;
    opacity: 0;
    transition: opacity 1s ease;
    animation: borderFlow 3s linear infinite;
  }
  
    &:hover {
    border-color: rgba(0, 245, 255, 0.4);
    
    &::before {
      opacity: 1;
    }
  }
}

.sci-fi-tabs {
  :deep(.el-tabs__header) {
    border-bottom: 2px solid rgba(0, 245, 255, 0.3);
    margin-bottom: 30px;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      width: 100%;
      height: 2px;
      background: linear-gradient(90deg, transparent, rgba(0, 245, 255, 0.6), transparent);
      animation: scanLine 3s ease-in-out infinite;
    }
  }
  
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }
  
  :deep(.el-tabs__nav-scroll) {
    overflow-x: scroll !important;

    &::-webkit-scrollbar {
      display: none !important;
    }
  }
  
  :deep(.el-tabs__item) {
    color: var(--text-primary);
    font-weight: 500;
    position: relative;
    transition: all 0.3s ease;
    border-radius: 8px 8px 0 0;
    margin-right: 5px;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(135deg, rgba(0, 245, 255, 0.1), rgba(131, 56, 236, 0.1));
      border-radius: 8px 8px 0 0;
      opacity: 0;
      transition: opacity 0.3s ease;
    }
    
    &:hover::before {
      opacity: 1;
    }
    
    &.is-active {
      color: #00f5ff;
      background: rgba(0, 245, 255, 0.1);
      border-color: rgba(0, 245, 255, 0.3);
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60%;
        height: 2px;
        background: linear-gradient(90deg, transparent, #00f5ff, transparent);
        box-shadow: 0 0 10px rgba(0, 245, 255, 0.6);
      }
    }
  }
}

@keyframes scanLine {
  0%, 100% {
    transform: translateX(-100%);
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  100% {
    transform: translateX(100%);
  }
}

@keyframes borderFlow {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.label-info {
  display: flex;
  align-items: center;
  gap: $spacing-base;
  color: var(--text-primary);
  transition: all 0.3s ease;
  
  i {
    transition: transform 0.3s ease;
  }
  
  &:hover i {
    transform: scale(1.2) rotate(5deg);
  }
}

// 响应式调整
@media (max-width: 768px) {
  .centered-content img {
    width: 400px;
    height: 400px;
  }
  
  .FirstText {
    font-size: 2rem;
  }
  
  .clickroll {
    font-size: 2rem;
    padding: 15px 30px;
  }
  
  .glow-ring {
    &:nth-child(2) {
      width: 420px;
      height: 420px;
    }
    
    &.ring-2 {
      width: 440px;
      height: 440px;
    }
  }
}
</style>