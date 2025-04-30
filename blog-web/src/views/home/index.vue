<template>
  <div>

    <div class="home">
      <!-- 新增背景层 -->
      <div class="background-layer">
        <div class="centered-content">
          <img src="src/assets/LDS/LDSLOGO.png" alt="Centered Image"/>
        </div>
        <div class="FirstText"  >
          <div class="title" ref="titleRef">
            {{ titleText }}
          </div>
        </div>
        <div class="clickroll" :class="{ 'fade-out': isClicked }" @click="scrollDown" >
          点击开启
        </div>
      </div>
      <!-- 主内容布局 -->
      <!--    占位符-->
      <div class="placeholder"></div>
      <!--    波浪过度-->


      <!--    主要内容-->
      <div class="content-layout">
        <main class="home-main-content">
          <!-- 轮播图组件，显示轮播文章 -->
          <!--                <Carousel-->
          <!--                    v-if="carouselSlides?.length > 0"-->
          <!--                    :slides="carouselSlides"-->
          <!--                    @click="goToPost"-->
          <!--                />-->
          <!-- 动态列表组件 -->
          <MomentsList/>

          <div>
            <!-- 标签页组件 -->
            <el-tabs v-model="activeName" @tab-click="handleClick">
              <!-- 遍历分类，生成每个标签页 -->
              <el-tab-pane
                  v-for="category in categories"
                  :key="category.id"
                  :name="String(category.id)"
              >
                <template slot="label">
                        <span class="label-info">
                          <i :class="category.icon"></i>
                          {{ category.name }}
                        </span>
                </template>
                <!-- 文章列表组件 -->
                <ArticleList
                    :articles="articleList"
                    :loading="loading"
                    :total="total"
                    :params="params"
                    @article-click="goToPost"
                    @page-change="changePage"
                    class="article-list"
                />
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
import {
  getArticlesApi,
  getCarouselArticlesApi,
  getAllCategoriesApi,
} from "@/api/article";

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
      titleText:'',
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
<<<<<<< HEAD
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
=======
  },
  computed: {

  },
>>>>>>> e1d5090ff65b7b8653640d6352d117786bc98195
};
</script>

<style lang="scss" scoped>
.clickroll {
  border-radius: 15px;
  position: absolute;
  left: 50%;
  top: 80%; // FirstText 在 70%，所以这里设置 80% 让它在下方
  transform: translateX(-50%);
  color: rgba(255, 255, 255, 0.8);
  font-size: 3rem;
  letter-spacing: 10px;
  backdrop-filter: blur(8px);
  text-align: center;
  animation: fadeInOut 2s ease-in-out infinite;
  transition: all 0.8s ease; // 添加过渡效果
  visibility: visible; // 初始状态可见
  &.fade-out {
    opacity: 0;
    transform: translateX(-50%) translateY(-800%); // 添加向下移动效果
    pointer-events: none; // 防止点击
    visibility: hidden; // 完全隐藏元素
  }
}

@keyframes fadeInOut {
  0%, 100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

.title {
  width: 100%;
  letter-spacing: 0.1em;
  -webkit-text-fill-color: transparent;
  font-weight: 700;
  background: linear-gradient(90deg, rgb(161, 248, 80), rgb(95, 241, 129), rgb(44, 107, 122), rgb(13, 65, 188), rgb(80, 115, 184), rgb(16, 152, 173), rgb(7, 179, 155), rgb(111, 186, 130)) text;
  text-align: center; /* 水平居中 */
  display: flex; /* 使用 flex 布局 */
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  height: 100%; /* 确保父容器有高度 */
}

.FirstText {
  width: 100%;
  position: absolute;
  top: 73%; /* 垂直居中 */
  left: 50%; /* 水平居中 */
  transform: translate(-50%, -50%); /* 修正偏移 */
  font-size: 2.5rem; /* 根据需要调整字体大小 */
  padding: 0.5em;
  border-radius: 0.5em;
  height: 90px;
  z-index: 1;
  opacity: 1;

}

.centered-content {
  position: absolute;
  top: 40%; /* 垂直居中向上偏移  */
  left: 50%; /* 水平居中 */
  transform: translate(-50%, -50%); /* 修正偏移 */
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
  background-color: transparent; // 可根据需求设置背景颜色
}

.background-layer {
  z-index: -1; // 将背景层置于最底层
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  background: url('src/assets/LDS/R-C.jpg') no-repeat center center; // 替换为图片路径
  background-size: cover; // 确保图片填充并适应
  //pointer-events: none; // 防止影响鼠标事件
}

.home {
  z-index: 2;
  //max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  height: 100%; // 确保父级有高度
  position: relative;
  @include responsive(lg) {
    padding: $spacing-sm;
  }
}

.content-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px; // 主内容和侧边栏布局
  gap: $spacing-lg * 2;
  padding: $spacing-xl *2.5 $spacing-xl *1.5;
  //margin-bottom: $spacing-xl * 2;
  min-height: calc(100vh - 80px);
  align-items: start;
  //backdrop-filter: blur(10px); // 添加毛玻璃模糊效果

  background: var(--content-gradient);

  transition: background 0.3s ease; // 添加过渡效果
  @include responsive(lg) {
    grid-template-columns: 1fr; // 小屏幕下单列布局
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

:deep(.el-tabs__nav-scroll) {
  overflow-x: scroll !important;

  &::-webkit-scrollbar {
    display: none !important;
  }
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.label-info {
  display: flex;
  align-items: center;
  gap: $spacing-base;
  color: var(--text-primary);
}
</style>