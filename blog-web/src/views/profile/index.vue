<template>
  <div class="profile-container">
    <!-- 左侧固定导航 -->
    <div class="profile-sidebar" role="complementary">
      <!-- 用户信息卡片 -->
      <el-card class="user-card">
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="showCropper = true" role="button" tabindex="0" aria-label="更换头像">
            <el-avatar :size="100" :src="userInfo.avatar || userInfo.headimgurl || userInfo.avatarUrl"
              alt="用户头像"></el-avatar>
            <div class="avatar-wrapper" @click="showCropper = true" role="button" tabindex="0" aria-label="更换头像">
              <el-avatar :size="100" :src="userInfo.avatar" alt="用户头像"></el-avatar>
              <div class="upload-overlay" inert>
                <i class="el-icon-camera"></i>
              </div>
            </div>
          </div>
          <h3 class="username">{{ userInfo.nickname || userInfo.name }}</h3>
          <p class="signature">{{ userInfo.user_info || '这个人很懒，还没有写简介...' }}</p>

          <!-- 添加签到按钮 -->
          <div class="sign-in-section">
            <el-button type="primary" :disabled="signInStatus" @click="handleSignIn" size="small"
              :loading="signInLoading">
              <i class="el-icon-check"></i>
              {{ signInStatus ? '今日已签到' : '立即签到' }}
            </el-button>
            <div class="sign-in-stats">
              <div class="stat-item">
                <span class="label">连续签到</span>
                <span class="value">{{ signInStats.continuousDays }}天</span>
              </div>
              <div class="stat-item">
                <span class="label">累计签到</span>
                <span class="value">{{ signInStats.totalDays }}天</span>
              </div>
            </div>
          </div>

          <div class="user-stats" role="list">
            <div class="stat-item" role="listitem">
              <span class="number">{{ statistics.posts }}</span>
              <span class="label">文章</span>
            </div>
            <div class="stat-item" role="listitem">
              <span class="number">{{ statistics.followers }}</span>
              <span class="label">关注者</span>
            </div>
            <div class="stat-item" role="listitem">
              <span class="number">{{ statistics.likes }}</span>
              <span class="label">获赞</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 导航菜单 -->
      <el-menu class="nav-menu" :default-active="currentTab" @select="currentTab = $event" role="navigation">
        <el-menu-item v-for="tab in tabs" :key="tab.key" :index="tab.key" :aria-label="tab.label">
          <i :class="tab.icon"></i>
          <span>{{ tab.label }}</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 右侧内容区 -->
    <main class="content-area" role="main">
      <!-- 个人资料 -->
      <div v-if="currentTab === 'profile'" class="content-section">
        <h2 class="section-title">个人资料</h2>
        <el-form ref="profileForm" :model="profileForm" :rules="profileRules" label-width="80px" class="profile-form"
          @submit.prevent="submitProfile">
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="profileForm.nickname" :placeholder="'请输入昵称'" aria-label="昵称输入框"></el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="profileForm.email" placeholder="点击绑定按钮绑定" readonly aria-label="邮箱输入框">
              <template v-if="!profileForm.email" slot="append">
                <span style="color:#409eff;cursor:pointer;"
                  @click="$refs.bindingTab && ($refs.bindingTab.active = 'binding'); showBindEmail = true">绑定</span>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input type="textarea" v-model="profileForm.user_info" :rows="4" placeholder="介绍一下自己吧..."></el-input>
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="profileForm.sex">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="2">女</el-radio>
              <el-radio :label="0">保密</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitProfile" :loading="loading" icon="el-icon-edit" size="small">保存修改
            </el-button>
            <el-button size="small" @click="resetProfile" icon="el-icon-refresh">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 账号绑定 -->
      <div v-if="currentTab === 'binding'" cla1110ss="content-section">
        <h2 class="section-title">账号绑定</h2>
        <div class="binding-tips">
          <el-alert title="账号绑定提示" type="info" description="绑定第三方账号后，您可以直接使用第三方账号登录本站，还可以同步您的个人信息。" show-icon
            :closable="false">
          </el-alert>
        </div>
        <div class="binding-list">
          <el-card v-for="account in boundAccounts" :key="account.type" class="binding-item">
            <div class="binding-content">
              <div class="account-info">
                <div class="account-icon">
                  <i :class="account.icon" :style="{ color: account.color }"></i>
                </div>
                <div class="account-details">
                  <span class="account-name">{{ account.name }}</span>
                  <span class="account-desc">{{ account.isBound ? account.username : '未绑定账号' }}</span>
                </div>
              </div>
              <div class="binding-status">
                <el-tag :type="account.isBound ? 'success' : 'info'" size="small" effect="dark" class="status-tag">
                  {{ account.isBound ? '已绑定' : '未绑定' }}
                </el-tag>
                <el-button :type="account.isBound ? 'danger' : 'primary'" size="small"
                  :icon="account.isBound ? 'el-icon-close' : 'el-icon-link'"
                  @click="account.isBound ? unbindAccount(account.type) : bindAccount(account.type)">
                  {{ account.isBound ? '解除绑定' : '立即绑定' }}
                </el-button>
              </div>
            </div>
          </el-card>
          <el-card class="binding-item">
            <div class="binding-content">
              <div class="account-info">
                <div class="account-icon">
                  <i class="el-icon-message" style="color: #409EFF"></i>
                </div>
                <div class="account-details">
                  <span class="account-name">邮箱</span>
                  <span class="account-desc">{{ userInfo.email ? userInfo.email : '未绑定邮箱' }}</span>
                </div>
              </div>
              <div class="binding-status">
                <el-tag :type="userInfo.email ? 'success' : 'info'" size="small" effect="dark" class="status-tag">
                  {{ userInfo.email ? '已绑定' : '未绑定' }}
                </el-tag>
                <el-button v-if="!userInfo.email" type="primary" size="small" icon="el-icon-link"
                  @click="showBindEmail = true">立即绑定</el-button>
                <el-button v-else type="danger" size="small" icon="el-icon-close" :loading="unbindEmailLoading"
                  @click="handleUnbindEmail">解绑</el-button>
              </div>
            </div>
          </el-card>
        </div>

      </div>

      <!-- 我的文章 -->
      <div v-if="currentTab === 'posts'" class="content-section">
        <h2 class="section-title">我的文章</h2>
        <div class="action-bar">
          <div>
            <el-input v-model="params.title" size="mini" placeholder="输入文字标题搜索文章..." prefix-icon="el-icon-search"
              style="width: 300px;margin-right: 10px"></el-input>
            <el-button type="primary" size="mini" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          </div>

          <el-button type="primary" icon="el-icon-edit" size="mini" @click="$router.push('/editor')">写文章</el-button>
        </div>

        <div v-loading="loading" v-if="posts.length">
          <el-card v-for="post in posts" :key="post.id" class="post-item">
            <div class="post-content">
              <h3 class="post-title" @click="viewPost(post.id)">{{ post.title }}</h3>
              <p class="post-excerpt">{{ post.summary }}</p>
              <div class="post-meta">
                <el-tag size="small"><i class="el-icon-date"></i>{{ post.createTime }}</el-tag>
                <el-tag size="small" type="info"><i class="el-icon-view"></i>{{ post.quantity }} 阅读</el-tag>
                <el-tag size="small" type="success"><i class="el-icon-chat-line-square"></i>{{ post.commentNum || 0 }}
                  评论
                </el-tag>
                <el-tag size="small" type="warning"><i class="el-icon-star-off"></i>{{ post.likeNum || 0 }} 点赞
                </el-tag>
              </div>
            </div>
            <div class="post-actions">
              <el-button type="text" icon="el-icon-view" @click="viewPost(post.id)">查看</el-button>
              <el-button type="text" icon="el-icon-edit" @click="editPost(post.id)">编辑</el-button>
              <el-button type="text" icon="el-icon-delete" class="delete" @click="deletePost(post)">删除</el-button>
            </div>
          </el-card>

          <div class="pagination-box">
            <el-pagination background @current-change="handlePostChange" :current-page="params.pageNum"
              :page-size="params.pageSize" :total="total" layout="prev, pager, next" class="pagination"></el-pagination>
          </div>
        </div>
        <el-empty v-else description="暂无文章，快去发布你的文章吧~~"></el-empty>
      </div>



      <!-- 我的评论 -->
      <div v-if="currentTab === 'comments'" class="content-section">
        <h2 class="section-title">我的评论</h2>
        <div v-if="myComments.length" v-loading="loading">
          <el-card v-for="comment in myComments" :key="comment.id" class="comment-item">
            <div class="comment-actions">
              <p class="comment-text" v-html="parseContent(comment.content)"></p>
              <el-button type="text" icon="el-icon-delete" class="delete" @click="deleteComment(comment.id)">删除
              </el-button>
            </div>
            <div class="comment-meta">
              <el-link type="primary" @click="viewPost(comment.articleId)">文章：{{ comment.articleTitle }}</el-link>
              <el-tag size="small">
                <i class="el-icon-time"></i>
                {{ comment.createTime }}
              </el-tag>
              <el-tag size="small" type="success"><i class="el-icon-star-off"></i>{{
                comment.likeCount ?
                  comment.likeCount : 0
              }} 赞
              </el-tag>
            </div>
          </el-card>
          <div class="pagination-box">
            <el-pagination background v-if="myComments.length" @current-change="handlePageChange"
              :current-page="params.pageNum" :page-size="params.pageSize" layout="prev, pager, next" :total="total">
            </el-pagination>
          </div>
        </div>
        <el-empty v-else description="暂无评论数据"></el-empty>
      </div>

      <!-- 我的回复 -->
      <div v-if="currentTab === 'replies'" class="content-section">
        <h2 class="section-title">我的回复</h2>
        <div v-if="myReplies.length" v-loading="loading">
          <el-card v-for="reply in myReplies" :key="reply.id" class="reply-item">
            <div class="reply-content">
              <div class="comment-actions">
                <p class="reply-text">
                  <el-tag size="small" type="info">回复 @{{ reply.replyNickname }}</el-tag>
                <p v-html="parseContent(reply.content)"></p>
                </p>
                <el-button type="text" icon="el-icon-delete" class="delete" @click="deleteReply(reply.id)">删除
                </el-button>
              </div>
              <div class="reply-meta">
                <el-link type="primary" @click="viewPost(reply.articleId)">文章：{{ reply.articleTitle }}</el-link>
                <el-tag size="small">
                  <i class="el-icon-time"></i>
                  {{ reply.createTime }}
                </el-tag>
              </div>
            </div>
          </el-card>

          <div class="pagination-box">
            <el-pagination background v-if="myReplies.length" @current-change="handleReplyPageChange"
              :current-page="params.pageNum" :page-size="params.pageSize" layout="prev, pager, next" :total="total">
            </el-pagination>
          </div>
        </div>

        <el-empty v-else description="暂无回复评论数据"></el-empty>

      </div>

      <!-- 我的点赞 -->
      <div v-if="currentTab === 'likes'" class="content-section">
        <h2 class="section-title">我的点赞</h2>
        <div v-if="myLikes.length" v-loading="loading">
          <el-card v-for="like in myLikes" :key="like.id" class="like-item">
            <div class="like-content">
              <div class="comment-actions">
                <el-link class="article-title" @click="viewPost(like.id)">{{ like.title }}</el-link>
                <el-button type="text" icon="el-icon-star-off" class="delete" @click="cancelLike(like.id)">取消点赞
                </el-button>
              </div>
              <div class="like-meta">
                <!-- <el-tag size="small"><i class="el-icon-user"></i>{{ like.author }}</el-tag> -->
                <el-tag size="small">
                  <i class="el-icon-time"></i>
                  {{ like.createTime }}
                </el-tag>
              </div>
            </div>
          </el-card>
          <div class="pagination-box">
            <el-pagination background v-if="myLikes.length" @current-change="handleLikePageChange"
              :current-page="params.pageNum" :page-size="params.pageSize" layout="prev, pager, next" :total="total">
            </el-pagination>
          </div>
        </div>

        <el-empty v-else description="暂无点赞数据"></el-empty>
      </div>

      <!-- 修改密码 -->
      <div v-if="currentTab === 'security'" class="content-section">
        <h2 class="section-title">修改密码</h2>

        <div class="binding-tips">
          <el-alert title="修改密码提示" type="info" description="只有邮箱登录的才可修改密码，其他第三方登录不存在修改密码功能。" show-icon
            :closable="false">
          </el-alert>
        </div>
        <el-form ref="passwordForm" :model="passwordForm" :rules="passwordRules" label-width="100px"
          class="security-form">
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input type="password" v-model="passwordForm.oldPassword" show-password placeholder="请输入当前密码"></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input type="password" v-model="passwordForm.newPassword" show-password placeholder="请输入新密码"></el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input type="password" v-model="passwordForm.confirmPassword" show-password
              placeholder="请再次输入新密码"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button size="small" type="primary" @click="submitPasswordChange" icon="el-icon-edit"
              :loading="loading">确认修改
            </el-button>
          </el-form-item>
        </el-form>
      </div>



    </main>

    <AvatarCropper :visible.sync="showCropper" :user="userInfo" @update-avatar="handleAvatarUpdate" />

    <!-- 邮箱绑定弹窗 -->
    <el-dialog title="绑定邮箱" :visible.sync="showBindEmail" width="420px" custom-class="bind-email-dialog"
      @close="resetBindEmailForm" append-to-body :close-on-click-modal="true">
      <el-alert title="为了您的账户安全，绑定邮箱后部分操作将需要邮箱验证。" type="info" :closable="false" show-icon style="margin-bottom: 24px;">
      </el-alert>
      <el-form :model="bindEmailForm" :rules="bindEmailRules" ref="bindEmailForm" label-width="80px"
        class="bind-email-form">
        <el-form-item label="邮箱地址" prop="email">
          <el-input v-model="bindEmailForm.email" placeholder="请输入您的邮箱" prefix-icon="el-icon-message"
            clearable></el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <el-input v-model="bindEmailForm.code" placeholder="请输入6位验证码" prefix-icon="el-icon-key">
            <el-button slot="append"
              :disabled="emailCodeTimer > 0 || !bindEmailForm.email || !isValidEmail(bindEmailForm.email)"
              @click="sendBindEmailCode">
              {{ emailCodeTimer > 0 ? `${emailCodeTimer}s 后重发` : '获取验证码' }}
            </el-button>
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showBindEmail = false" size="medium">取 消</el-button>
        <el-button type="primary" @click="submitBindEmail" :loading="bindEmailLoading" size="medium"
          icon="el-icon-check">确认绑定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import {
  delMyCommentApi,
  getMyCommentApi,
  getMyLikeApi,
  getMyReplyApi,
  getSignInStatsApi,
  getSignInStatusApi,
  getUserInfoApi,
  signInApi,
  updatePasswordApi,
  updateProfileApi
} from '@/api/user'
import { delArticleApi, getMyArticleApi, likeArticleApi } from '@/api/article'
import { getDictDataApi } from '@/api/dict'
import AvatarCropper from '@/components/common/AvatarCropper.vue'
import { bindEmailApi, getwxUserInfoApi, giteeLoginApi, sendBindEmailCodeApi, unbindEmailApi } from '@/api/auth'

import { marked } from "marked";

export default {
  name: 'Profile',
  components: {
    AvatarCropper
  },
  data() {
    // 密码确认验证规则
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    return {
      userInfo: {},
      editForm: {
        username: '',
        email: '',
        userInfo: ''
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      statistics: {
        posts: 0,
        likes: 0,
        followers: 0
      },
      currentTab: 'profile',
      tabs: [
        { key: 'profile', label: '个人资料', icon: 'fas fa-user' },
        { key: 'binding', label: '账号绑定', icon: 'fas fa-link' },
        { key: 'posts', label: '我的文章', icon: 'fas fa-file-alt' },

        { key: 'comments', label: '我的评论', icon: 'fas fa-comments' },
        { key: 'replies', label: '我的回复', icon: 'fas fa-reply' },
        { key: 'likes', label: '我的点赞', icon: 'fas fa-heart' },
        { key: 'security', label: '修改密码', icon: 'fas fa-lock' },

      ],
      boundAccounts: [
        {
          type: 'wechat',
          name: '微信公众号',
          icon: 'fab fa-weixin',
          isBound: true,
          username: 'wx_user123',
          color: '#10b981'
        },
        {
          type: 'qq',
          name: 'QQ',
          icon: 'fab fa-qq',
          isBound: false,
          color: '#60a5fa'
        },
        {
          type: 'gitee',
          name: '码云',
          icon: 'fab fa-git-alt',
          isBound: true,
          username: 'github_user',
          color: '#FF0000'
        }
      ],

      posts: [],
      myComments: [],
      myReplies: [],
      myLikes: [],
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入当前密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      // 个人资料表单
      profileForm: {
        nickname: '',
        email: '',
        sex: 2,
        user_info: ''
      },
      profileRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
      },
      params: {
        pageNum: 1,
        pageSize: 10,
        title: '',
      },
      total: 0,
      loading: false,

      signInStatus: false,
      signInStats: {
        continuousDays: 0,
        totalDays: 0
      },
      signInLoading: false,
      showCropper: false,

      showBindEmail: false,
      bindEmailForm: { email: '', code: '' },
      bindEmailRules: {
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' }
        ]
      },
      emailCodeTimer: 0,
      bindEmailLoading: false,
      unbindEmailLoading: false,
      emailCodeTimerInterval: null,
    }
  },

  watch: {
    '$store.state.userInfo': {
      handler(newVal, oldVal) {
        if (!newVal) {
          this.$router.push('/')
        }
      },
      deep: true
    },
    currentTab(newVal, oldVal) {
      switch (newVal) {
        case 'posts':
          this.params.pageNum = 1
          this.getMyArticle()
          break
        case 'comments':
          this.params.pageNum = 1
          this.getMyComment()
          break
        case 'replies':
          this.params.pageNum = 1
          this.getMyReplies()
          break
        case 'likes':
          this.params.pageNum = 1
          this.getMyLikes()
          break

        default:
          break
      }
    },
    showBindEmail(val) {
      if (!val) {
        this.resetBindEmailForm()
      }
    }
  },
  created() {
    const openId = localStorage.getItem("openId");
    if (localStorage.getItem('userInfo') && openId) {
      getwxUserInfoApi(openId).then(res => {
        console.log(res.data);
        this.userInfo = res.data
        Object.assign(this.profileForm, res.data)
        // 统一字段名
        if (res.data.userInfo) {
          this.profileForm.user_info = res.data.userInfo
        }
        if (res.data.sex == "男") {
          this.profileForm.sex = 1
        } else if (res.data.sex == "女") {
          this.profileForm.sex = 0
        } else {
          this.profileForm.sex = 2
        }
      })
    } else if (localStorage.getItem("giteeInfo")) {
      giteeLoginApi().then(res => {
        console.log(res.data);
        this.userInfo = res.data
        Object.assign(this.profileForm, res.data)
        if (res.data.name) {
          this.profileForm.nickname = res.data.name
        }
        // 统一字段名
        if (res.data.userInfo) {
          this.profileForm.user_info = res.data.userInfo
        }
        if (res.data.sex == "男") {
          this.profileForm.sex = 1
        } else if (res.data.sex == "女") {
          this.profileForm.sex = 0
        } else {
          this.profileForm.sex = 2
        }
      })
    } else {
      getUserInfoApi().then(res => {
        this.userInfo = res.data.sysUser
        Object.assign(this.profileForm, res.data.sysUser)
      })
    }
    getUserInfoApi().then(res => {
      this.userInfo = res.data.sysUser
      Object.assign(this.profileForm, res.data.sysUser)
    })

    // 获取签到状态和统计
    this.getSignInStatus()
    this.getSignInStats()
  },
  methods: {

    /**
     * 获取我的评论
     */
    getMyComment() {
      this.loading = true
      getMyCommentApi(this.params).then(res => {
        this.myComments = res.data.records
        this.total = res.data.total
      }).finally(() => {
        this.loading = false
      })
    },
    /**
     * 解析评论内容
     */
    parseContent(content) {
      return marked(content || "");
    },
    /**
     * 分页
     * @param val
     */
    handlePageChange(val) {
      this.params.pageNum = val
      this.getMyComment()
    },

    /**
     * 获取我的文章
     */
    getMyArticle() {
      this.loading = true
      getMyArticleApi(this.params).then(res => {
        this.posts = res.data.records
        this.total = res.data.total
      }).finally(() => {
        this.loading = false
      })
    },

    /**
     * 跳转文章详情
     * @param id
     */
    viewPost(id) {
      this.$router.push(`/post/${id}`)
    },
    /**
     * 编辑文章
     * @param id
     */
    editPost(id) {
      this.$router.push(`/editor?id=${id}`)
    },
    /**
     * 删除文章
     * @param id
     */
    deletePost(row) {
      this.$confirm(`确定要删除标题为 '${row.title}' 的文章吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delArticleApi(row.id).then(res => {
          this.$message.success('删除成功')
          this.getMyArticle()
        }).catch(err => {
          this.$message.error(err.message || '删除失败')
        })
      }).catch(() => {
      })
    },
    /**
     * 搜索文章
     * @param id
     */
    handleSearch() {
      this.params.pageNum = 1
      this.getMyArticle()
    },
    /**
     * 分页
     * @param val
     */
    handlePostChange(page) {
      this.params.pageNum = page
      this.getMyArticle()
    },
    /**
     * 绑定账号
     * @param type
     */
    bindAccount(type) {
      // 模拟绑定过程
      const account = this.boundAccounts.find(acc => acc.type === type)
      if (account) {
        this.$confirm('确定要绑定该账号吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          account.isBound = true
          this.$message.success('绑定成功')
        }).catch(() => {
        })
      }
    },
    unbindAccount(type) {
      const account = this.boundAccounts.find(acc => acc.type === type)
      if (account) {
        this.$confirm('确定要解除绑定吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          account.isBound = false
          this.$message.success('已解除绑定')
        }).catch(() => {
        })
      }
    },
    /**
     * 删除评论
     */
    deleteComment(id) {
      this.$confirm('此操作会把该评论下的子评论也一并删除，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delMyCommentApi(id).then(res => {
          this.$message.success('删除成功')
          this.getMyComment()
        })
      }).catch(() => {
      })
    },
    /**
     * 删除回复
     */
    deleteReply(id) {
      this.$confirm('确定要删除这条回复吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delMyCommentApi(id).then(res => {
          this.$message.success('删除成功')
          this.getMyReplies()
        })
      }).catch(() => {
      })
    },
    /**
     * 取消点赞
     * @param id
     */
    cancelLike(id) {
      this.$confirm('确定要取消点赞吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        likeArticleApi(id).then(res => {
          this.$message.success('已取消点赞')
          this.getMyLikes()
        })
      }).catch(() => {
      })
    },


    // 提交密码修改
    submitPasswordChange() {
      this.$refs.passwordForm.validate(valid => {
        if (valid) {
          this.loading = true
          updatePasswordApi(this.passwordForm).then(res => {
            this.$message.success('密码修改成功！')
            this.$refs.passwordForm.resetFields()
          }).catch(err => {
            this.$message.error(err.message)
          }).finally(() => {
            this.loading = false

          })
        }
      })
    },
    // 提交个人资料
    submitProfile() {
      this.loading = true
      updateProfileApi(this.profileForm).then(res => {
        this.$message.success('个人资料更新成功！')
        // 方案1：重新获取用户信息（推荐）
        this.refreshUserInfo()

        // 方案2：整页刷新（可选，注释掉方案1使用此方案）
        // setTimeout(() => {
        //   window.location.reload()
        // }, 1000) // 1秒后刷新页面，让用户看到成功提示
      }).catch(err => {
        this.$message.error(err.message)
      }).finally(() => {
        this.loading = false
      })
    },
    // 重置个人资料
    resetProfile() {
      this.profileForm = { ...{} }
    },
    /**
     * 获取我的回复
     */
    getMyReplies() {
      this.loading = true
      getMyReplyApi(this.params).then(res => {
        this.myReplies = res.data.records
        this.total = res.data.total
      }).finally(() => {
        this.loading = false
      })
    },
    /**
     * 回复分页
     */
    handleReplyPageChange(val) {
      this.params.pageNum = val
      this.getMyReplies()
    },
    /**
     * 获取我的点赞
     */
    getMyLikes() {
      this.loading = true
      getMyLikeApi(this.params).then(res => {
        this.myLikes = res.data.records
        this.total = res.data.total
      }).finally(() => {
        this.loading = false
      })
    },
    /**
     * 点赞分页
     */
    handleLikePageChange(val) {
      this.params.pageNum = val
      this.getMyLikes()
    },

    /**
     * 获取签到状态
     */
    getSignInStatus() {
      getSignInStatusApi().then(res => {
        this.signInStatus = res.data
      })
    },

    /**
     * 获取签到统计
     */
    getSignInStats() {
      getSignInStatsApi().then(res => {
        this.signInStats = res.data
      })
    },

    /**
     * 签到
     */
    handleSignIn() {
      if (this.signInStatus.hasSignedIn) return

      this.signInLoading = true
      signInApi().then(res => {
        this.$message.success('签到成功！')
        this.getSignInStatus()
        this.getSignInStats()
      }).catch(err => {
        this.$message.error(err.message || '签到失败')
      }).finally(() => {
        this.signInLoading = false
      })
    },

    /**
     * 更新头像
     */
    handleAvatarUpdate(newAvatarUrl) {
      this.userInfo.avatar = newAvatarUrl;
    },
    sendBindEmailCode() {
      this.$refs.bindEmailForm.validateField('email', error => {
        if (error) return
        this.emailCodeTimer = 60
        sendBindEmailCodeApi(this.bindEmailForm.email).then(() => {
          this.$message.success('验证码已发送，请查收邮箱')
          this.emailCodeTimerInterval = setInterval(() => {
            this.emailCodeTimer--
            if (this.emailCodeTimer <= 0) {
              clearInterval(this.emailCodeTimerInterval)
              this.emailCodeTimerInterval = null
            }
          }, 1000)
        }).catch(err => {
          this.emailCodeTimer = 0
          if (err.message && err.message.includes('已被绑定')) {
            this.$message.error('该邮箱已被其他账号绑定，请更换邮箱')
          } else {
            this.$message.error(err.message || '验证码发送失败')
          }
        })
      })
    },
    // 绑定邮箱
    submitBindEmail() {
      this.$refs.bindEmailForm.validate(valid => {
        if (!valid) return
        this.bindEmailLoading = true
        bindEmailApi(this.bindEmailForm).then(() => {
          this.$message.success('邮箱绑定成功')
          this.showBindEmail = false
          this.resetBindEmailForm()
          this.getUserInfo() // 刷新用户信息
        }).catch(err => {
          if (err.message && err.message.includes('已被绑定')) {
            this.$message.error('该邮箱已被其他账号绑定，请更换邮箱')
          } else {
            this.$message.error(err.message || '绑定失败')
          }
        }).finally(() => {
          this.bindEmailLoading = false
        })
      })
    },
    // 刷新用户信息
    getUserInfo() {
      getUserInfoApi().then(res => {
        this.userInfo = res.data.sysUser
        Object.assign(this.profileForm, res.data.sysUser)
      })
    },
    // 重新获取用户信息
    refreshUserInfo() {
      getUserInfoApi().then(res => {
        this.userInfo = res.data.sysUser
        Object.assign(this.profileForm, res.data.sysUser)
        // 更新store中的用户信息
        this.$store.commit('SET_USER_INFO', res.data.sysUser)
      }).catch(err => {
        console.error('刷新用户信息失败:', err)
      })
    },
    // 弹窗关闭时清理
    resetBindEmailForm() {
      this.bindEmailForm = { email: '', code: '' }
      this.emailCodeTimer = 0
      if (this.emailCodeTimerInterval) {
        clearInterval(this.emailCodeTimerInterval)
        this.emailCodeTimerInterval = null
      }
    },
    isValidEmail(email) {
      return /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)
    },
    // 解绑邮箱
    handleUnbindEmail() {
      this.$confirm('解绑后将无法通过该邮箱找回密码，确定要解绑吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.unbindEmailLoading = true
        unbindEmailApi().then(() => {
          this.$message.success('邮箱解绑成功')
          this.getUserInfo()
        }).catch(err => {
          this.$message.error(err.message || '解绑失败')
        }).finally(() => {
          this.unbindEmailLoading = false
        })
      }).catch(() => { })
    },
  }
}
</script>

<style scoped lang="scss">



:deep(input[aria-hidden=true]) {
  display: none !important;
}

.delete {
  color: red;
}

.profile-container {
  display: flex;
  gap: 20px;
  padding: 20px;
  min-height: 100vh;
}

.profile-sidebar {
  position: sticky;
  top: 80px;
  height: fit-content;
  width: 300px;
  flex-shrink: 0;


  @include responsive(sm) {
    position: unset;

    .el-dialog {
      width: 95% !important;
    }
  }
}


.user-card {
  text-align: center;
  background: var(--card-bg);
  border: var(--border-color);

  .avatar-section {
    margin-bottom: 16px;
  }

  .avatar-wrapper {
    width: 100px;
    height: 100px;
    margin: 0 auto;
    position: relative;
    cursor: pointer;
    border-radius: 50%;
    overflow: hidden;

    .upload-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;

      i {
        font-size: 24px;
        color: white;
      }
    }

    &:hover .upload-overlay {
      opacity: 1;
    }
  }

  .username {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 8px;
    color: var(--text-primary);
  }

  .signature {
    color: var(--text-secondary);
    font-size: 14px;
    margin: 0 0 16px;
    line-height: 1.5;
  }

  .user-stats {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    padding-top: 16px;
    border-top: 1px solid var(--border-color);

    .stat-item {
      .number {
        display: block;
        font-size: 18px;
        font-weight: 600;
        color: var(--primary-color);
      }

      .label {
        font-size: 12px;
        color: var(--text-secondary);
      }
    }
  }
}

.el-menu-item {
  color: var(--text-secondary) !important;
}

.nav-menu {
  border-radius: 8px;
  background: var(--card-bg);
  border-right: none;
  margin-top: $spacing-md;


  .is-active {
    background: var(--hover-bg);
    color: $primary;
  }

  :deep(.el-menu-item) {
    height: 48px;
    line-height: 48px;

    &:hover {
      background: var(--hover-bg);
      color: $primary;
    }

    i {
      margin-right: 12px;
    }
  }
}

.content-area {
  flex: 1;
  min-width: 0;
  background: var(--card-bg);
  border-radius: 12px;
  padding: 24px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
  color: var(--text-primary);
}

.profile-form,
.security-form,


.post-item {
  margin-bottom: 16px;

  .post-content {
    margin-bottom: 16px;
  }

  .post-title {
    font-size: 18px;
    color: var(--text-secondary);
    margin: 0 0 12px;
    cursor: pointer;

    &:hover {
      color: $primary;
    }
  }

  .post-excerpt {
    color: var(--text-secondary);
    margin: 0 0 12px;
    line-height: 1.5;
  }

  .post-meta {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;

    .el-tag {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .post-actions {
    display: flex;
    gap: 16px;
    justify-content: flex-end;
    padding-top: 16px;
    border-top: 1px solid var(--border-color);
  }
}

.comment-item,
.reply-item,
.like-item {
  margin-bottom: 16px;

  .comment-actions,
  .reply-actions,
  .like-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .article-title {
      font-size: 18px;
      font-weight: 700;
    }
  }

  .comment-text,
  .reply-text {
    color: var(--text-secondary);
    margin: 0 0 12px;
    line-height: 1.5;

    :deep(img) {
      max-width: 200px !important;
      max-height: 200px !important;
    }
  }

  .comment-meta,
  .reply-meta,
  .like-meta {
    display: flex;
    gap: 12px;
    align-items: center;
  }
}


.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.binding-item {
  margin-bottom: 16px;

  .account-info {
    display: flex;
    align-items: center;
    gap: 16px;

    i {
      font-size: 24px;
      color: var(--primary-color);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
  }

  .profile-sidebar {
    width: 100%;
  }

  .content-area {
    padding: 16px;
  }

  .action-bar {
    .el-input {
      width: 100% !important;
    }
  }
}

.hidden-input {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.binding-item,
.comment-item,
.reply-item,
.like-item,
.post-item {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
}


.binding-tips {
  margin-bottom: 24px;

  :deep(.el-alert) {
    border-radius: 8px;

    .el-alert__title {
      font-size: 15px;
      font-weight: 500;
    }

    .el-alert__description {
      margin: 8px 0 0;
      font-size: 13px;
      color: var(--text-secondary);
    }
  }
}

.binding-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;

  .binding-item {
    .binding-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px;
    }

    .account-info {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .account-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--hover-bg);
      transition: all 0.3s ease;

      i {
        font-size: 24px;
        color: var(--primary-color);
      }
    }

    .account-details {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .account-name {
      font-size: 16px;
      font-weight: 500;
      color: var(--text-primary);
    }

    .account-desc {
      font-size: 13px;
      color: var(--text-secondary);
    }

    .binding-status {
      display: flex;
      align-items: center;
      gap: 16px;

      .status-tag {
        min-width: 64px;
        text-align: center;
      }
    }

    &:hover {
      .account-icon {
        transform: scale(1.05);
        background: var(--primary-color);

        i {
          color: $primary;
        }
      }
    }
  }
}



.posts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .write-btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    border-radius: 4px;
    background: $primary;
    color: white;
    text-decoration: none;
    transition: all 0.3s ease;

    &:hover {
      background: darken($primary, 10%);
    }

    i {
      font-size: 14px;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
  color: var(--text-secondary);

  i {
    font-size: 48px;
    margin-bottom: 16px;
  }

  p {
    margin-bottom: 24px;
  }

  .btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 8px 24px;
    border-radius: 4px;
    background: var(--hover-bg);
    color: var(--text-primary);
    text-decoration: none;
    transition: all 0.3s ease;

    &:hover {
      background: var(--hover-bg-dark);
    }

    &.primary {
      background: $primary;
      color: white;

      &:hover {
        background: darken($primary, 10%);
      }
    }
  }
}

.sign-in-section {
  padding: 16px 0;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
  margin: 16px 0;

  .sign-in-stats {
    display: flex;
    justify-content: center;
    gap: 24px;
    margin-top: 16px;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;

      .label {
        font-size: 12px;
        color: var(--text-secondary);
      }

      .value {
        font-size: 16px;
        font-weight: 600;
        color: var(--primary-color);
      }
    }
  }
}

:deep(.bind-email-dialog) {
  border-radius: 10px;

  .el-dialog__header {
    font-weight: 600;
    color: var(--text-primary);
    border-bottom: 1px solid var(--border-color);
    padding: 16px 24px;
  }

  .el-dialog__body {
    padding: 24px 30px 0;
  }

  .el-dialog__footer {
    padding: 20px 24px;
  }

  .el-form-item__label {
    font-weight: 500;
  }

  .el-input-group__append .el-button {
    border-top-left-radius: 0;
    border-bottom-left-radius: 0;
  }
}
</style>
