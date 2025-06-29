<template>
  <div class="friends-page">
    <el-card class="content-card" shadow="hover">
      <div class="page-header">
        <h1>友情链接</h1>
        <div class="header-divider">
          <span class="line"></span>
          <i class="fas fa-link"></i>
          <span class="line"></span>
        </div>
        <p>与优秀的人同行，分享技术与生活</p>
        <div class="site-info">
          <div class="site-avatar">
            <div class="avatar-wrapper">
              <el-avatar class="avatar" :src="$store.state.webSiteInfo.logo" />
              <div class="copy-overlay" @click="copyLogoUrl">
                <i class="fas fa-copy"></i>
                <span>复制图片链接</span>
                <input type="text" :value="$store.state.webSiteInfo.logo" readonly ref="logoInput" style="position: absolute; opacity: 0;">
              </div>
            </div>
          </div>
          <div class="site-details">
            <h2>{{ $store.state.webSiteInfo.name }}</h2>
            <p>{{ $store.state.webSiteInfo.summary }}</p>
            <div class="site-url">
              <i class="fas fa-link"></i>
              <input type="text" :value="$store.state.webSiteInfo.webUrl" readonly ref="urlInput" @click="copyUrl">
              <button class="copy-btn" @click="copyUrl">
                <i class="fas fa-copy"></i>
              </button>
            </div>
          </div>
        </div>
        <div class="copy-tip">
          <i class="fas fa-info-circle"></i>
          申请友链前请先添加本站链接
          <span class="tip-highlight">「 点击上方链接可快速复制 」</span>
        </div>


        <div class="friend-main">
          <div class="form-wrap" :class="{expanded: isExpanded}">
            <img src="@/assets/friendLetterTop.png" class="before-img">
            <div class="envelope" @click="!isExpanded && toggleEnvelope()" :class="{ 'envelope-expanded': isExpanded }">

              <div class="form-main">
                <img src="@/assets/friendLetterMiddle.jpg" style="width: 100%;">
                <div class="form-content" :class="{expanded: isExpanded}">
                  <div class="form-header">
                    <h3>有朋自远方来</h3>
                    <button v-if="isExpanded" class="close-btn" @click.stop="toggleEnvelope()">
                      <i class="fas fa-circle"></i>
                    </button>
                  </div>

                  <div class="apply-form" @click.stop>
                    <el-form
                        size="small"
                        :model="form"
                        :rules="rules"
                        ref="ruleForm"
                        label-width="100px"
                        @click.stop
                    >
                        <div class="form-group">
                          <el-form-item prop="name">
                            <template v-slot:label>
                              <i class="fas fa-signature"></i> 网站名称
                            </template>
                          </el-form-item>
                          <el-input type="text" v-model="form.name" placeholder="请输入您的网站名称" />
                        </div>
                        <div class="form-group">
                          <el-form-item prop="url">
                            <template v-slot:label>
                              <i class="fas fa-link"></i> 网站链接
                            </template>
                          </el-form-item>
                          <el-input type="url" v-model="form.url" placeholder="请输入您的网站链接" />
                        </div>
                        <div class="form-group">
                          <el-form-item prop="info">
                            <template v-slot:label>
                              <i class="fas fa-quote-left"></i> 网站描述
                            </template>
                          </el-form-item>
                          <el-input type="url" v-model="form.info" placeholder="一句话描述您的网站" />
                        </div>
                        <div class="form-group">
                          <el-form-item prop="avatar">
                            <template v-slot:label>
                              <i class="fas fa-image"></i> 头像链接
                            </template>
                          </el-form-item>
                          <el-input type="url" v-model="form.avatar" placeholder="请输入您的头像链接" />
                        </div>
                        <div class="form-group">
                          <el-form-item prop="email">
                            <template v-slot:label>
                              <i class="fas fa-envelope"></i> 联系邮箱
                            </template>
                          </el-form-item>
                          <el-input type="url" v-model="form.email" placeholder="邮箱用于联系" />
                        </div>




                        <div class="form-footer">
                          <el-button class="submit-btn" type="primary" @click="submitApplication">
                            <i class="fas fa-paper-plane"></i>
                            提交申请
                          </el-button>
                        </div>
<!--                        <div class="form-group mascot">
                          <img src="@/assets/friendLetterBiLi.png">
                        </div>-->

                      </el-form>
                    </div>

                  </div>
                </div>
              </div>
              <img src="@/assets/friendLetterBottom.png" class="after-img">
            </div>

          </div>
        </div>



      <div class="friends-container">
        <div class="section-title">
          <h2>友链列表</h2>
          <span class="count">{{ friends.length }} 个伙伴</span>
        </div>
        <div class="friends-grid">
          <div v-for="friend in friends" :key="friend.id" class="friend-card" @click="visitFriend(friend.url)">
            <div class="friend-card-inner">
              <div class="friend-avatar">
                <img v-lazy="friend.avatar" :key="friend.avatar" :alt="friend.name">
                <div class="status" :class="{ online: friend.online }"></div>
              </div>
              <div class="friend-info">
                <h3>{{ friend.name }}</h3>
                <p>{{ friend.info }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getFriendsApi, applyFriendApi } from '@/api/friends'

export default {
  name: 'Friends',

  data() {
    return {
      isExpanded: false,
      showApplyForm: false,
      form: {
        name: '',
        url: '',
        info: '',
        avatar: '',
        email: ''
      },
      friends: [],
      rules: {
        name: [
          { required: true, message: '请输入网站名称', trigger: 'blur' }
        ],
        url: [
          { required: true, message: '请输入网站链接', trigger: 'blur' },
          { type: 'url', message: '请输入正确的链接格式', trigger: 'blur' }
        ],
        info: [
          { required: true, message: '请输入网站描述', trigger: 'blur' }
        ],
        avatar: [
          { required: true, message: '请输入头像链接', trigger: 'blur' },
          { type: 'url', message: '请输入正确的链接格式', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入联系邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ]
      }
    }
  },
  async created() {
    const res = await getFriendsApi()
    this.friends = res.data
  },
  methods: {
    toggleEnvelope() {
      this.isExpanded = !this.isExpanded;
    },

    visitFriend(url) {
      window.open(url, '_blank')
    },
    handleApply() {
      this.form = {
        name: '',
        url: '',
        info: '',
        avatar: '',
        email: ''
      },
        this.showApplyForm = true
    },
    handleClose() {
      this.showApplyForm = false
    },
    submitApplication() {
      this.$refs['ruleForm'].validate((valid) => {
        if (valid) {
          try {
            const res = applyFriendApi(this.form)
            this.showApplyForm = false
            this.$message.success('申请已提交，请等待审核')
            this.$refs['ruleForm'].resetFields();
          } catch (error) {
            this.$message.error(error.message)
          }
        } else {
          console.log('error submit!!')
        }
      })

    },
    copyLogoUrl() {
      const input = this.$refs.logoInput
      input.select()
      document.execCommand('copy')
      this.$message.success('Logo链接已复制到剪贴板')
    },
    copyUrl() {
      const input = this.$refs.urlInput
      input.select()
      document.execCommand('copy')
      this.$message.success('链接已复制到剪贴板')
    }
  }
}
</script>

<style lang="scss" scoped>

:deep(.el-form-item__content){
  margin-left: 0 !important;
}
.friends-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: $spacing-lg;
  min-height: calc(100vh - 200px);
  animation: fadeIn 0.8s ease-out;
  background: radial-gradient(circle at 10% 20%, rgba(216, 241, 230, 0.2) 0%, rgba(233, 226, 226, 0.1) 90.1%);
  @include responsive(lg) {
    padding: $spacing-sm;
  }
}

.content-card {
  padding: $spacing-sm;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.8);
}

.page-header {
  text-align: center;
  margin-bottom: $spacing-xl;
  position: relative;
  animation: slideDown 0.8s ease-out;

  h1 {
    font-size: 2.5em;
    margin-bottom: $spacing-md;
    font-weight: 800;
    background: linear-gradient(135deg, $primary, #00d9ff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    letter-spacing: 2px;
    text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.1);
    position: relative;
    display: inline-block;
    
    &::after {
      content: '';
      position: absolute;
      bottom: -10px;
      left: 50%;
      transform: translateX(-50%);
      width: 80px;
      height: 3px;
      background: linear-gradient(90deg, $primary, #00d9ff);
      border-radius: 3px;
    }
  }

  .header-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-sm;
    margin: $spacing-lg 0;
    opacity: 0.8;

    .line {
      width: 80px;
      height: 2px;
      background: linear-gradient(90deg,
          transparent,
          var(--text-secondary),
          transparent);
    }

    i {
      color: $primary;
      font-size: 1.4em;
      transform: rotate(45deg);
      animation: pulse 2s infinite;
    }
  }

  p {
    color: var(--text-secondary);
    font-size: 1.3em;
    margin-bottom: $spacing-lg;
    font-weight: 300;
    letter-spacing: 0.5px;
    font-style: italic;
  }
}

.friends-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: $spacing-md;
  padding: $spacing-md;
  animation: fadeIn 1s ease-out;
}

.friend-card {
  perspective: 1000px;
  height: 140px;
  cursor: pointer;
  
  &:nth-child(3n+1) { animation: fadeInUp 0.6s ease-out; }
  &:nth-child(3n+2) { animation: fadeInUp 0.6s ease-out 0.1s; }
  &:nth-child(3n+3) { animation: fadeInUp 0.6s ease-out 0.2s; }
  
  .friend-card-inner {
    @include card;
    background: rgba(255, 255, 255, 0.7);
    border: 1px solid rgba(255, 255, 255, 0.3);
    padding: $spacing-lg;
    display: flex;
    align-items: center;
    gap: $spacing-md;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;
    height: 100%;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    border-radius: 12px;
    backdrop-filter: blur(5px);

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 4px;
      background: linear-gradient(90deg, $primary, #00d9ff);
      opacity: 0;
      transition: opacity 0.3s ease;
    }
    
    &::after {
      content: '';
      position: absolute;
      width: 40px;
      height: 100%;
      background: rgba(255, 255, 255, 0.3);
      transform: skewX(-20deg);
      top: 0;
      left: -80px;
      transition: all 0.6s ease;
      opacity: 0;
    }
  }

  &:hover .friend-card-inner {
    transform: translateY(-10px);
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
    border-color: rgba($primary, 0.5);
    background: rgba(255, 255, 255, 0.8);

    &::before {
      opacity: 1;
    }
    
    &::after {
      left: 150%;
      opacity: 1;
    }
  }

  .friend-avatar {
    position: relative;
    width: 70px;
    height: 70px;
    flex-shrink: 0;
    border-radius: 50%;
    padding: 3px;
    background: linear-gradient(135deg, $primary, $secondary);

    img {
      width: 100%;
      height: 100%;
      border-radius: 50%;
      object-fit: cover;
      transition: transform 0.5s ease;
      border: 3px solid var(--card-bg);
      overflow-clip-margin: content-box;
      overflow: clip;
    }

    .status {
      position: absolute;
      bottom: 3px;
      right: 3px;
      width: 14px;
      height: 14px;
      border-radius: 50%;
      background: #9ca3af;
      border: 2px solid var(--card-bg);
      box-shadow: 0 0 0 2px var(--card-bg);
      z-index: 1;

      &.online {
        background: #10b981;
        animation: pulse 2s infinite;
      }
    }
  }

  &:hover .friend-avatar img {
    transform: scale(1.1) rotate(5deg);
  }

  .friend-info {
    flex: 1;
    min-width: 0;

    h3 {
      color: var(--text-primary);
      font-size: 1.3em;
      margin-bottom: $spacing-xs;
      font-weight: 600;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      transition: color 0.3s ease;
    }

    p {
      color: var(--text-secondary);
      font-size: 0.95em;
      line-height: 1.6;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      transition: color 0.3s ease;
    }
  }
  
  &:hover .friend-info h3 {
    color: $primary;
    text-shadow: 0 0 1px rgba($primary, 0.3);
  }
}

.site-info {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  margin: $spacing-xl auto;
  max-width: 700px;
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  animation: fadeIn 0.8s ease-out 0.2s backwards;
  backdrop-filter: blur(10px);

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);

    .site-avatar .avatar {
      transform: scale(1.05);
    }
  }

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, $primary, #00d9ff);
  }

  .site-avatar {
    width: 120px;
    height: 120px;
    flex-shrink: 0;
    position: relative;

    .avatar-wrapper {
      width: 100%;
      height: 100%;
      position: relative;
      cursor: pointer;
      border-radius: 50%;
      overflow: hidden;
      background: linear-gradient(135deg, $primary, $secondary);
      padding: 3px;
      
      .avatar {
        width: 100%;
        height: 100%;
        border-radius: 50%;
        object-fit: cover;
        border: 3px solid var(--card-bg);
        background: var(--card-bg);
        transition: transform 0.5s ease;
      }

      .copy-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.6);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s ease;
        color: white;
        border-radius: 50%;

        i {
          font-size: 1.5em;
          margin-bottom: 4px;
        }

        span {
          font-size: 0.8em;
        }
      }

      &:hover {
        .copy-overlay {
          opacity: 1;
        }

        .avatar {
          transform: scale(1.05);
        }
      }
    }
  }

  .site-details {
    flex: 1;
    min-width: 0;

    h2 {
      color: var(--text-primary);
      font-size: 1.7em;
      margin-bottom: $spacing-sm;
      font-weight: 700;
      background: linear-gradient(135deg, $primary, #00d9ff);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      letter-spacing: 0.5px;
    }

    p {
      color: var(--text-secondary);
      margin-bottom: $spacing-md;
      font-size: 1.05em;
      line-height: 1.6;
    }

    .site-url {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      padding: $spacing-sm $spacing-md;
      background: rgba(0, 0, 0, 0.04);
      border-radius: $border-radius-lg;
      border: 1px solid rgba(255, 255, 255, 0.5);
      transition: all 0.3s ease;
      box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.05);
      position: relative;
      overflow: hidden;
      backdrop-filter: blur(5px);

      &::before {
        content: '';
        position: absolute;
        top: -2px;
        left: -2px;
        right: -2px;
        bottom: -2px;
        background: linear-gradient(45deg, rgba($primary, 0), rgba(#00d9ff, 0.1), rgba($primary, 0));
        background-size: 400% 400%;
        animation: gradientFlow 3s ease infinite;
        z-index: -1;
        border-radius: calc($border-radius-lg + 2px);
      }

      &:hover {
        border-color: $primary;
        box-shadow: 0 0 0 2px rgba($primary, 0.1), inset 0 2px 5px rgba(0, 0, 0, 0.05);
        background: rgba(0, 0, 0, 0.02);

        &::before {
          animation: gradientFlow 1.5s ease infinite;
        }

        .copy-btn {
          transform: translateX(0);
          opacity: 1;
        }
      }

      i {
        font-size: 1em;
        color: $primary;
        filter: drop-shadow(0 0 2px rgba($primary, 0.3));
      }

      input {
        background: none;
        border: none;
        color: var(--text-primary);
        font-size: 1em;
        flex: 1;
        min-width: 0;
        cursor: pointer;
        padding: $spacing-xs;
        font-family: 'Consolas', monospace;
        letter-spacing: 0.5px;

        &:focus {
          outline: none;
        }
        
        &::selection {
          background: rgba($primary, 0.2);
        }
      }

      .copy-btn {
        background: linear-gradient(135deg, $primary, #00d9ff);
        border: none;
        color: white;
        cursor: pointer;
        padding: $spacing-xs $spacing-sm;
        transition: all 0.3s ease;
        border-radius: $border-radius-sm;
        box-shadow: 0 3px 8px rgba($primary, 0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        width: 32px;
        height: 32px;
        position: relative;
        overflow: hidden;
        transform: translateX(5px);
        opacity: 0.8;
        
        i {
          color: white;
          position: relative;
          z-index: 2;
          filter: none;
        }
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: rgba(255, 255, 255, 0.2);
          transform: translateX(-100%);
          transition: transform 0.3s ease;
        }

        &:hover {
          transform: translateX(0) scale(1.05);
          opacity: 1;
          
          &::before {
            transform: translateX(0);
          }
        }
        
        &:active {
          transform: translateX(0) scale(0.95);
        }
      }
    }
  }
}

.copy-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  color: var(--text-secondary);
  margin: $spacing-lg 0;
  font-size: 1em;
  padding: $spacing-md;
  background: rgba($primary, 0.08);
  border-radius: $border-radius-lg;
  animation: fadeIn 0.8s ease-out 0.4s backwards;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    width: 40px;
    height: 100%;
    background: rgba(255, 255, 255, 0.3);
    transform: skewX(-20deg);
    top: 0;
    left: -80px;
    animation: shimmer 3s infinite;
  }

  i {
    color: $primary;
    animation: bounce 2s infinite;
    font-size: 1.2em;
    filter: drop-shadow(0 0 2px rgba($primary, 0.3));
  }

  .tip-highlight {
    color: $primary;
    margin-left: $spacing-sm;
    font-weight: 500;
    text-decoration: underline;
    text-decoration-color: rgba($primary, 0.3);
    text-decoration-thickness: 2px;
    text-underline-offset: 3px;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      width: 100%;
      height: 2px;
      background: linear-gradient(90deg, rgba($primary, 0.3), rgba(#00d9ff, 0.6), rgba($primary, 0.3));
      background-size: 200% 100%;
      animation: gradientSlide 2s linear infinite;
    }
  }
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-md;
  border-bottom: 2px solid var(--border-color);
  position: relative;
  animation: slideInRight 0.8s ease-out;

  &::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    width: 100px;
    height: 2px;
    background: linear-gradient(90deg, $primary, $secondary);
    animation: expandWidth 1.5s ease-out forwards;
  }

  h2 {
    color: var(--text-primary);
    font-size: 1.8em;
    font-weight: 700;
  }

  .count {
    color: white;
    font-size: 0.95em;
    padding: $spacing-xs $spacing-md;
    background: linear-gradient(135deg, $primary, $secondary);
    border-radius: 20px;
    font-weight: 500;
    box-shadow: 0 3px 8px rgba($primary, 0.2);
    animation: pulse 2s infinite;
  }
}

.apply-btn {
  padding: $spacing-sm $spacing-lg;
  background: linear-gradient(135deg, $primary, $secondary);
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 1em;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: $spacing-sm;
  box-shadow: 0 4px 15px rgba($primary, 0.3);

  i {
    font-size: 1.1em;
    transition: transform 0.3s ease;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba($primary, 0.4);

    i {
      transform: rotate(180deg);
    }
  }

  &:active {
    transform: translateY(1px);
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba($primary, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba($primary, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba($primary, 0);
  }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-5px);
  }
  60% {
    transform: translateY(-3px);
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes fadeInUp {
  from { 
    opacity: 0;
    transform: translateY(20px);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideDown {
  from { 
    opacity: 0;
    transform: translateY(-20px);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInRight {
  from { 
    opacity: 0;
    transform: translateX(-20px);
  }
  to { 
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes expandWidth {
  from { width: 0; }
  to { width: 100px; }
}

.apply-form {
  .form-group {
    display: flex;
    gap: $spacing-md;
    margin-bottom: 20px;
    margin-right: 50px;
    margin-left: 50px;
    transition: all 0.3s ease;

    &:hover .el-input__inner {
      border-color: $primary;
    }
  }
  
  .mascot {
    height: 180px;
    background: white;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
    
    img {
      width: 100%;
      height: 36px;
      margin: 5px auto;
      transition: transform 0.5s ease;
    }
    
    &:hover img {
      transform: scale(1.05);
    }
  }
  
  .el-form-item {
    flex: 1;
    margin-bottom: 0;
    opacity: 1;
    transform: none;

    &__label {
      font-weight: 500;
      color: var(--text-primary);
    }
  }

  .el-input {
    .el-input__inner {
      border-radius: $border-radius-md;
      border: 1px solid var(--border-color);
      background: var(--input-bg);
      color: var(--text-primary);
      transition: all 0.3s ease;

      &:focus {
        border-color: $primary;
        box-shadow: 0 0 0 2px rgba($primary, 0.1);
      }

      &::placeholder {
        color: var(--text-secondary);
      }
    }
  }

  .form-footer {
    display: flex;
    justify-content: center;
    margin-top: $spacing-lg;
    background: white;
    padding: $spacing-md 0 $spacing-xl;
    position: relative;
    z-index: 20;

    .submit-btn {
      background: linear-gradient(135deg, $primary, $secondary);
      border: none;
      padding: $spacing-sm $spacing-xl;
      font-size: 1em;
      border-radius: 25px;
      box-shadow: 0 4px 15px rgba($primary, 0.3);
      transition: all 0.3s ease;
      margin-bottom: 20px;

      i {
        margin-right: $spacing-xs;
        transition: transform 0.3s ease;
      }

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 8px 20px rgba($primary, 0.4);
        
        i {
          transform: translateX(3px);
        }
      }

      &:active {
        transform: translateY(1px);
      }
    }
  }
}



@for $i from 1 through 5 {
  .form-group:nth-child(#{$i}) {
    animation: fadeInUp 0.5s ease-out #{$i * 0.1}s backwards;
  }
}





.friend-main {
  max-width: 1000px;
  margin: 60px auto;
  border-radius: 16px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
  animation: fadeIn 0.8s ease-out 0.6s backwards;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle at center, rgba($primary, 0.05) 0%, rgba(#00d9ff, 0.02) 30%, rgba(255,255,255,0) 70%);
    opacity: 0.8;
    z-index: -1;
  }
}



.form-wrap {
  margin: 0 auto;
  width: 530px;
  position: relative;
  transition: all 0.6s ease-in-out;
  perspective: 1000px;
  cursor: pointer;
  
  &:hover {
    transform: translateY(-5px);
  }
  
  &.expanded {
    margin-top: -150px;
  }
}

.envelope {
  position: relative;
  margin: 0 auto;
  transition: all 1s ease-in-out .3s;
  padding: 150px 30px 20px;
  
  &.envelope-expanded {
    transform: translateY(20px);
  }
}


.after-img, .before-img {
  position: absolute;
  left: 0;
  background-repeat: no-repeat;
  width: 530px;
  transition: all 0.3s ease;
}

.before-img {
  bottom: 126px;
  height: 317px;
  z-index: -100;
  filter: drop-shadow(0 10px 15px rgba(0, 0, 0, 0.1));
}



.after-img{
  bottom: -2px;
  height: 259px;
  z-index: 100;
  pointer-events: none;
  filter: drop-shadow(0 10px 15px rgba(0, 0, 0, 0.1));
}

  .form-main {
    background: var(--white);
    margin: 0 auto;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.1);
  }

  .form-content {
    max-height: 0;
    overflow: hidden;
    transition: max-height 0.6s ease-in-out;
    background: white;
    position: relative;
    z-index: 10;

    &.expanded {
      max-height: 600px;
      margin-bottom: 100px;
    }
  }

.form-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-sm 0;
  
  h3 {
    text-align: center;
    margin: 20px 0;
    color: var(--text-primary);
    font-size: 1.6em;
    font-weight: 600;
    background: linear-gradient(135deg, $primary, $secondary);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  .close-btn {
    position: absolute;
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    color: rgba($primary, 0.6);
    font-size: 1.3em;
    cursor: pointer;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    padding: 0;
    
    &:hover {
      color: $primary;
      transform: translateY(-50%) scale(1.1);
    }
    
    &:active {
      transform: translateY(-50%) scale(0.95);
    }
  }
}

@keyframes gradientFlow {
  0% { background-position: 0% 50% }
  50% { background-position: 100% 50% }
  100% { background-position: 0% 50% }
}

@keyframes shimmer {
  0% { left: -80px; }
  100% { left: 100%; }
}

@keyframes gradientSlide {
  0% { background-position: 0% 0%; }
  100% { background-position: 200% 0%; }
}

</style>
