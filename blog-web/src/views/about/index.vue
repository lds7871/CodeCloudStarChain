<template>
  <div class="about-page">
    <div class="tech-background">
      <div class="grid-overlay"></div>
      <div class="circuit-pattern"></div>
      <div class="tech-crosses"></div>
      <div class="corner-decoration top-right"></div>
      <div class="corner-decoration bottom-left"></div>
      <div class="particles" v-for="n in 20" :key="n"></div>
      <div class="digital-lines"></div>
    </div>
    <el-card class="about-card">
      <div class="card-header">
        <div class="header-title">
          <div class="title-icon">
            <i class="fas fa-microchip"></i>
          </div>
          <h1>关于我们</h1>
        </div>
        <div class="digital-badge">
          <span class="badge-text">V 1.0</span>
          <span class="badge-pulse"></span>
        </div>
      </div>
      <div class="about-content" v-html="$store.state.webSiteInfo.aboutMe" ref="content">
      </div>
      <div class="tech-stack-decoration"></div>
      <div class="tech-footer">
        <div class="tech-line"></div>
        <div class="footer-icon">
          <i class="fas fa-code"></i>
        </div>
      </div>
      <mj-image-preview ref="imagePreview" />
    </el-card>
  </div>
</template>

<script>

export default {
  name: 'About',

  mounted() {
    this.initImagePreview();
    this.initDigitalEffect();
  },
  methods: {
    initImagePreview() {
      this.$nextTick(() => {
        setTimeout(() => {
          const content = this.$refs.content;
          if (content) {
            const images = content.getElementsByTagName('img');
            Array.from(images).forEach(img => {
              img.style.cursor = 'zoom-in';
              img.addEventListener('click', () => {
                this.$refs.imagePreview.show(img.src);
              });
            });
          }
        }, 500);
      });
    },
    initDigitalEffect() {
      // 为技术栈部分添加高亮效果
      this.$nextTick(() => {
        setTimeout(() => {
          const content = this.$refs.content;
          if (content) {
            const techElements = content.querySelectorAll('h3, h4');
            Array.from(techElements).forEach(el => {
              if (el.textContent.includes('技术栈') || el.textContent.includes('版本') || 
                  el.textContent.includes('服务器') || el.textContent.includes('框架')) {
                el.classList.add('tech-highlight');
              }
            });
          }
        }, 600);
      });
    }
  }
}
</script>

<style lang="scss" scoped>
.about-page {
  max-width: 1200px;
  margin: 0 auto;
  margin-top: $spacing-lg;
  margin-bottom: $spacing-md;
  position: relative;
  min-height: calc(100vh - 250px);
  overflow: hidden;

  @include responsive(lg) {
    padding: $spacing-sm;
  }
  
  // 添加装饰性几何元素
  &::before, &::after {
    content: '';
    position: absolute;
    border-radius: 50%;
    z-index: -1;
  }
  
  &::before {
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(65, 184, 255, 0.1) 0%, rgba(65, 184, 255, 0) 70%);
    top: -100px;
    right: -100px;
    filter: blur(20px);
  }
  
  &::after {
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba(0, 217, 255, 0.08) 0%, rgba(0, 217, 255, 0) 70%);
    bottom: -50px;
    left: -50px;
    filter: blur(15px);
  }
}

.tech-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: -1;
  background: radial-gradient(circle at 50% 50%, rgba(28, 35, 65, 0.8) 0%, rgba(15, 23, 42, 0.95) 100%);
  
  // 添加背景纹理
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%2341b8ff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
    opacity: 0.5;
  }
}

.grid-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(65, 184, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(65, 184, 255, 0.03) 1px, transparent 1px);
  background-size: 20px 20px;
  z-index: 1;
}

// 添加电路板纹理
.circuit-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2;
  opacity: 0.07;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 304 304' width='304' height='304'%3E%3Cpath fill='%2341b8ff' fill-opacity='1' d='M44.1 224a5 5 0 1 1 0 2H0v-2h44.1zm160 48a5 5 0 1 1 0 2H82v-2h122.1zm57.8-46a5 5 0 1 1 0-2H304v2h-42.1zm0 16a5 5 0 1 1 0-2H304v2h-42.1zm6.2-114a5 5 0 1 1 0 2h-86.2a5 5 0 1 1 0-2h86.2zm-256-48a5 5 0 1 1 0 2H0v-2h12.1zm185.8 34a5 5 0 1 1 0-2h86.2a5 5 0 1 1 0 2h-86.2zM258 12.1a5 5 0 1 1-2 0V0h2v12.1zm-64 208a5 5 0 1 1-2 0v-54.2a5 5 0 1 1 2 0v54.2zm48-198.2V80h62v2h-64V21.9a5 5 0 1 1 2 0zm16 16V64h46v2h-48V37.9a5 5 0 1 1 2 0zm-128 96V208h16v12.1a5 5 0 1 1-2 0V210h-16v-76.1a5 5 0 1 1 2 0zm-5.9-21.9a5 5 0 1 1 0 2H114v48H85.9a5 5 0 1 1 0-2H112v-48h12.1zm-6.2 130a5 5 0 1 1 0-2H176v-74.1a5 5 0 1 1 2 0V242h-60.1zm-16-64a5 5 0 1 1 0-2H114v48h10.1a5 5 0 1 1 0 2H112v-48h-10.1zM66 284.1a5 5 0 1 1-2 0V274H50v30h-2v-32h18v12.1zM236.1 176a5 5 0 1 1 0 2H226v94h48v32h-2v-30h-48v-98h12.1zm25.8-30a5 5 0 1 1 0-2H274v44.1a5 5 0 1 1-2 0V146h-10.1zm-64 96a5 5 0 1 1 0-2H208v-80h16v-14h-42.1a5 5 0 1 1 0-2H226v18h-16v80h-12.1zm86.2-210a5 5 0 1 1 0 2H272V0h2v32h10.1zM98 101.9V146H53.9a5 5 0 1 1 0-2H96v-42.1a5 5 0 1 1 2 0zM53.9 34a5 5 0 1 1 0-2H80V0h2v34H53.9zm60.1 3.9V66H82v64H69.9a5 5 0 1 1 0-2H80V64h32V37.9a5 5 0 1 1 2 0zM101.9 82a5 5 0 1 1 0-2H128V37.9a5 5 0 1 1 2 0V82h-28.1zm16-64a5 5 0 1 1 0-2H146v44.1a5 5 0 1 1-2 0V18h-26.1zm102.2 270a5 5 0 1 1 0 2H98v14h-2v-16h124.1zM242 149.9V160h16v34h-16v62h48v48h-2v-46h-48v-66h16v-30h-16v-12.1a5 5 0 1 1 2 0zM53.9 18a5 5 0 1 1 0-2H64V2H48V0h18v18H53.9zm112 32a5 5 0 1 1 0-2H192V0h50v2h-48v48h-28.1zm-48-48a5 5 0 0 1-9.8-2h2.07a3 3 0 1 0 5.66 0H178v34h-18V21.9a5 5 0 1 1 2 0V32h14V2h-58.1zm0 96a5 5 0 1 1 0-2H137l32-32h39V21.9a5 5 0 1 1 2 0V66h-40.17l-32 32H117.9zm28.1 90.1a5 5 0 1 1-2 0v-76.51L175.59 80H224V21.9a5 5 0 1 1 2 0V82h-49.59L146 112.41v75.69zm16 32a5 5 0 1 1-2 0v-99.51L184.59 96H300.1a5 5 0 0 1 3.9-3.9v2.07a3 3 0 0 0 0 5.66v2.07a5 5 0 0 1-3.9-3.9H185.41L162 121.41v98.69zm-144-64a5 5 0 1 1-2 0v-3.51l48-48V48h32V0h2v50H66v55.41l-48 48v2.69zM50 53.9v43.51l-48 48V208h26.1a5 5 0 1 1 0 2H0v-65.41l48-48V53.9a5 5 0 1 1 2 0zm-48 6V34.8a5 5 0 1 1 2 0v25.69l-48 48v23.62l47.79-47.78a5 5 0 1 1 .42.4L2 124.44v-20.8l48-48V60a5 5 0 1 1 2 0zm0 0v-28.5a5 5 0 1 1 2 0v28.09l48-48V21.9a5 5 0 1 1 2 0V43.59l-48 48v2.69z'%3E%3C/path%3E%3C/svg%3E");
}

// 添加角落装饰
.corner-decoration {
  position: absolute;
  width: 150px;
  height: 150px;
  z-index: 2;
  
  &.top-right {
    top: 0;
    right: 0;
    background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M0 0h100v100H0V0zm1 1v98h98V1H1z' fill='%2341b8ff' fill-opacity='0.1'/%3E%3C/svg%3E");
    transform: rotate(0deg);
  }
  
  &.bottom-left {
    bottom: 0;
    left: 0;
    background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M0 0h100v100H0V0zm1 1v98h98V1H1z' fill='%2341b8ff' fill-opacity='0.1'/%3E%3C/svg%3E");
    transform: rotate(180deg);
  }
}

.particles {
  position: absolute;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(65, 184, 255, 0.6);
  box-shadow: 0 0 10px rgba(65, 184, 255, 0.8), 0 0 20px rgba(65, 184, 255, 0.4);
  animation: float 8s infinite ease-in-out;
  opacity: 0.4;
  
  @for $i from 1 through 20 {
    &:nth-child(#{$i + 2}) {
      left: random(100) + 0%;
      top: random(100) + 0%;
      width: random(6) + 2px;
      height: random(6) + 2px;
      animation-delay: random(8) + 0s;
      animation-duration: (random(10) + 5) + s;
      opacity: (random(6) + 2) / 10;
    }
  }
}

.digital-lines {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  
  &::before, &::after {
    content: '';
    position: absolute;
    height: 2px;
    background: linear-gradient(90deg, transparent, rgba(65, 184, 255, 0.2), transparent);
    width: 200%;
    animation: digitalLine 15s infinite linear;
  }
  
  &::before {
    top: 25%;
    animation-delay: 0s;
  }
  
  &::after {
    top: 75%;
    animation-delay: 5s;
    animation-duration: 20s;
  }
}

.about-card {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 10;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25);
  }
  
  // 添加卡片边框装饰
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border: 2px solid transparent;
    border-radius: 12px;
    background: linear-gradient(45deg, rgba(65, 184, 255, 0.5), transparent 30%, transparent 70%, rgba(0, 217, 255, 0.5)) border-box;
    -webkit-mask: 
      linear-gradient(#fff 0 0) padding-box, 
      linear-gradient(#fff 0 0);
    -webkit-mask-composite: destination-out;
    mask-composite: exclude;
    pointer-events: none;
  }
}

.card-header {
  padding: $spacing-md $spacing-lg;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(90deg, rgba(65, 184, 255, 0.1), transparent);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    width: 30px;
    height: 200%;
    background: rgba(255, 255, 255, 0.2);
    transform: rotate(30deg);
    top: -50%;
    left: -100px;
    animation: shine 5s infinite linear;
  }
  
  // 添加标题背景装饰
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: url("data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%2341b8ff' fill-opacity='0.05' fill-rule='evenodd'%3E%3Ccircle cx='3' cy='3' r='3'/%3E%3Ccircle cx='13' cy='13' r='3'/%3E%3C/g%3E%3C/svg%3E");
    opacity: 0.5;
    z-index: -1;
  }
}

.header-title {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  
  .title-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, $primary, #00d9ff);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 5px 15px rgba($primary, 0.3);
    
    i {
      color: white;
      font-size: 1.2rem;
    }
  }
  
  h1 {
    font-size: 1.8rem;
    margin: 0;
    background: linear-gradient(135deg, $primary, #00d9ff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    font-weight: 700;
    letter-spacing: 1px;
  }
}

.digital-badge {
  padding: 6px 12px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid rgba(65, 184, 255, 0.3);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
    transform: translateX(-100%);
    animation: shimmer 3s infinite;
  }
  
  .badge-text {
    font-family: 'Consolas', monospace;
    font-size: 0.9rem;
    font-weight: 600;
    color: $primary;
  }
  
  .badge-pulse {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: #00d9ff;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      top: -4px;
      left: -4px;
      right: -4px;
      bottom: -4px;
      border-radius: 50%;
      background-color: rgba(0, 217, 255, 0.3);
      animation: pulse 2s infinite;
    }
  }
  }

  .about-content {
    line-height: 1.8;
    color: var(--text-primary);
    padding: $spacing-lg;
  position: relative;
  z-index: 10;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 3px;
    height: 100%;
    background: linear-gradient(to bottom, $primary, transparent);
    opacity: 0.3;
  }
  
  // 添加内容区域装饰
  &::after {
    content: '';
    position: absolute;
    top: 20px;
    right: 20px;
    width: 60px;
    height: 60px;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 56 56' width='56' height='56'%3E%3Cpath fill='%2341b8ff' fill-opacity='0.05' d='M28 0c15.464 0 28 12.536 28 28S43.464 56 28 56 0 43.464 0 28 12.536 0 28 0zm0 5c-12.703 0-23 10.297-23 23s10.297 23 23 23 23-10.297 23-23S40.703 5 28 5zm0 5c9.941 0 18 8.059 18 18s-8.059 18-18 18S10 37.941 10 28s8.059-18 18-18zm0 5c-7.18 0-13 5.82-13 13s5.82 13 13 13 13-5.82 13-13-5.82-13-13-13zm0 5c4.418 0 8 3.582 8 8s-3.582 8-8 8-8-3.582-8-8 3.582-8 8-8z'/%3E%3C/svg%3E");
    opacity: 0.7;
    pointer-events: none;
  }
}

// 添加技术栈背景装饰
.tech-stack-decoration {
  position: absolute;
  bottom: 50px;
  right: 50px;
  width: 200px;
  height: 200px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 80 80' width='80' height='80'%3E%3Cpath fill='%2341b8ff' fill-opacity='0.03' d='M0 0h80v80H0V0zm20 20v40h40V20H20zm20 35a15 15 0 1 1 0-30 15 15 0 0 1 0 30z' fill-rule='evenodd'/%3E%3C/svg%3E");
  z-index: 1;
  opacity: 0.5;
  pointer-events: none;
}

.tech-footer {
  padding: $spacing-md;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  
  .tech-line {
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba($primary, 0.3), transparent);
    width: 80%;
    position: absolute;
  }
  
  .footer-icon {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: white;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba($primary, 0.2);
    z-index: 2;
    
    i {
      color: $primary;
      font-size: 1rem;
    }
  }
}

:deep(.tech-highlight) {
  background: linear-gradient(90deg, $primary, #00d9ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  padding-left: 10px;
  border-left: 3px solid $primary;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    left: -3px;
    top: 0;
    height: 100%;
    width: 3px;
    background: linear-gradient(to bottom, $primary, #00d9ff);
    animation: pulseHeight 2s infinite;
  }
}

:deep(h3), :deep(h4) {
  margin-top: 1.5em;
  position: relative;
  padding-left: 15px;
  
  &::before {
    content: '>';
    color: $primary;
    position: absolute;
    left: 0;
    font-weight: bold;
  }
}

:deep(ul) {
  list-style-type: none;
  padding-left: 20px;
  
  li {
    position: relative;
    margin-bottom: 10px;
    
    &::before {
      content: '•';
      color: $primary;
      position: absolute;
      left: -15px;
      font-weight: bold;
    }
  }
}

:deep(img) {
  border-radius: 8px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  
  &:hover {
    transform: scale(1.02);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) translateX(0);
  }
  25% {
    transform: translateY(-15px) translateX(10px);
  }
  50% {
    transform: translateY(5px) translateX(-10px);
  }
  75% {
    transform: translateY(-5px) translateX(5px);
  }
}

@keyframes digitalLine {
  0% {
    transform: translateX(-50%);
  }
  100% {
    transform: translateX(0%);
  }
}

@keyframes shine {
  0% {
    left: -100px;
  }
  20% {
    left: 100%;
  }
  100% {
    left: 100%;
  }
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  40%, 100% {
    transform: translateX(100%);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.5);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}

@keyframes pulseHeight {
  0% {
    opacity: 0.8;
  }
  50% {
    opacity: 0.3;
  }
  100% {
    opacity: 0.8;
  }
}

@include responsive(sm) {
  :deep(img) {
    width: 100% !important;
  }
  
  .card-header {
    flex-direction: column;
    gap: $spacing-sm;
    align-items: flex-start;
  }
  
  .digital-badge {
    align-self: flex-end;
  }
}
</style>
