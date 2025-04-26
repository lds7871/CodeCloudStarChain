// 导入核心依赖
import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

// 导入动画相关库
import gsap from 'gsap'
import 'animate.css'
import ScrollTrigger from 'gsap/ScrollTrigger'
gsap.registerPlugin(ScrollTrigger)

// 导入图片懒加载插件
import VueLazyload from 'vue-lazyload'

/**
 * 配置图片懒加载插件
 * @param {number} preLoad - 预加载高度比例
 * @param {string} error - 加载失败时显示的图片
 * @param {string} loading - 加载中显示的图片
 * @param {number} attempt - 加载失败后重试次数
 * @param {boolean} observer - 是否使用 IntersectionObserver
 * @param {Object} observerOptions - 观察器的配置选项
 */
Vue.use(VueLazyload, {
  preLoad: 1.3,
  error: 'https://img.shiyit.com/base/mojian/img-error.jpg',
  loading: 'https://blog.nanshengwx.cn/upload/AnimatedEmojies-512px-406.gif',
  attempt: 1,
  observer: true,
  observerOptions: {
    rootMargin: '0px',
    threshold: 0.1
  }
})

// 导入并使用 Element UI
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
Vue.use(ElementUI);

// 注册全局表情选择器组件
import EmojiPicker from '@/components/common/EmojiPicker.vue'
Vue.component('mj-emoji', EmojiPicker)

// 注册全局点击外部事件组件
import ClickOutside from '@/directives/clickOutside'
Vue.directive('click-outside', ClickOutside)

// 注册加载组件
import loading from './directives/loading'
Vue.directive('loading', loading)

// 导入代码高亮样式
import 'highlight.js/styles/atom-one-dark.css'
import { animateOnScroll } from './directives/animate'
Vue.directive('animate-on-scroll', animateOnScroll)

// 注册全局图片预览组件
import ImagePreview from '@/components/common/ImagePreview.vue'
Vue.component('mj-image-preview', ImagePreview)

/**
 * 热更新支持
 * 用于开发环境下的模块热替换功能
 */
if (import.meta.hot) {
  import.meta.hot.accept()
}

// 导入并使用 Markdown 编辑器
import mavonEditor from "mavon-editor";
import "mavon-editor/dist/css/index.css";
Vue.use(mavonEditor);

// 关闭生产环境提示
Vue.config.productionTip = false

// 注册 SVG 图标
import 'virtual:svg-icons-register'
import SvgIcon from '@/components/SvgIcon/index.vue'
Vue.component('svg-icon', SvgIcon)

/**
 * 创建 Vue 根实例
 * 挂载路由和状态管理
 * 渲染根组件
 */
new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')