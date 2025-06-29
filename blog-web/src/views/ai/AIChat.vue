<template>
  <div class="ai-chat">
    <div class="chat-layout">
      <!-- 左侧聊天记录面板 -->
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <h3>聊天记录</h3>
          <button @click="createNewChat" class="new-chat-btn" title="新建聊天">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
          </button>
        </div>

        <div class="chat-history">
          <div v-for="chat in chatHistory" :key="chat.id" class="chat-item"
            :class="{ active: currentChatId === chat.id }" @click="currentChatId = chat.id" :data-chat-id="chat.id"
            :data-is-active="currentChatId === chat.id" :title="`点击切换到: ${chat.title || '新对话'}`">
            <div class="chat-preview">
              <div class="chat-title">{{ chat.title || '新对话' }}</div>
              <div class="chat-last-message">{{ getLastMessage(chat) }}</div>
              <div class="chat-time">{{ formatChatTime(chat.updatedAt) }}</div>
            </div>
            <button @click.stop="deleteChat(chat.id)" class="delete-chat-btn" v-if="chatHistory.length > 1">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>

          <div v-if="chatHistory.length === 0" class="empty-history">
            <p>暂无聊天记录</p>
            <button @click="createNewChat" class="create-first-chat">开始新对话</button>
          </div>
        </div>
      </div>

      <!-- 右侧对话区域 -->
      <div class="chat-container">
        <!-- 头部导航 -->
        <div class="chat-header">
          <router-link to="/" class="back-btn">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
          </router-link>
          <div class="header-center">
            <h2>{{ getCurrentChatTitle() }}</h2>
            <div v-if="userInfo && userInfo.nickname" class="user-info">
              与 {{ userInfo.nickname }} 的对话
            </div>
          </div>
          <div class="status" :class="{ online: isOnline }">
            {{ isOnline ? '在线' : '离线' }}
          </div>
        </div>

        <!-- 聊天消息区域 -->
        <div class="chat-messages" ref="messagesContainer">
          <div v-if="getCurrentMessages().length === 0" class="welcome-message">
            <div class="welcome-icon">🤖</div>
            <h3>欢迎使用AI智能助手</h3>
            <p>我是您的专属AI助手，有什么可以帮助您的吗？</p>
          </div>

          <div v-for="(message, index) in getCurrentMessages()" :key="index" class="message"
            :class="{ 'user-message': message.isUser, 'ai-message': !message.isUser }">
            <!-- AI消息：头像在左，内容在右 -->
            <template v-if="!message.isUser">
              <div class="message-avatar-wrapper">
                <div class="message-avatar">
                  <div class="ai-avatar">🤖</div>
                </div>
                <div class="ai-nickname">AI客服</div>
              </div>
              <div class="message-content">
                <div class="message-bubble">
                  <div v-if="message.isLoading" class="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                  <div v-else>
                    <div v-html="formatMessage(message.text)"></div>
                    <span v-if="message.isStreaming" class="streaming-cursor">|</span>
                  </div>
                </div>
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              </div>
            </template>

            <!-- 用户消息：内容在左，头像在右 -->
            <template v-else>
              <div class="message-content">
                <div class="message-bubble">
                  <div v-html="formatMessage(message.text)"></div>
                </div>
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              </div>
              <div class="message-avatar-wrapper">
                <div class="message-avatar">
                  <div class="user-avatar">
                    <img :src="userAvatar" alt="用户头像" @error="handleImageError" @load="handleImageLoad">
                  </div>
                </div>
                <div class="user-nickname">{{ userInfo && userInfo.nickname ? userInfo.nickname : '用户' }}</div>
              </div>
            </template>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input">
          <div class="input-container">
            <textarea v-model="inputMessage" @keydown.enter.prevent="handleKeydown" @input="adjustTextareaHeight"
              placeholder="输入您的问题..." rows="1" ref="messageInput" :disabled="isLoading"></textarea>
            <button @click="sendMessage" :disabled="!inputMessage.trim() || isLoading" class="send-btn">
              <svg v-if="!isLoading" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
              <div v-else class="loading-spinner"></div>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { chatWithAI, chatWithAIStream } from '@/api/chat.js'
export default {
  name: 'AIChatPage',
  data() {
    return {
      chatHistory: [],
      currentChatId: null,
      inputMessage: '',
      isLoading: false,
      isOnline: true,
      userInfo: null,
      userAvatar: '@/assets/images/user.png'
    }
  },
  computed: {
    currentChatIndex() {
      return this.chatHistory.findIndex(chat => chat.id === this.currentChatId)
    }
  },
  mounted() {
    this.checkOnlineStatus()
    this.loadUserInfo()
    this.loadChatHistory()
    this.validateAndFixChatIds()
    this.fixAllTimestamps()
    if (this.chatHistory.length === 0) {
      this.createNewChat()
    } else {
      this.currentChatId = this.chatHistory[0].id
    }
    this.addWelcomeMessage()
  },
  methods: {
    // 从本地存储获取用户信息
    loadUserInfo() {
      try {
        const localUser = localStorage.getItem('user')
        if (localUser) {
          this.userInfo = JSON.parse(localUser)
          if (this.userInfo.avatar) {
            this.userAvatar = this.userInfo.avatar
          }
          console.log('从本地存储获取用户信息:', {
            nickname: this.userInfo.nickname,
            avatar: this.userAvatar
          })
        }
      } catch (error) {
        console.error('获取本地用户信息失败:', error)
      }
    },

    // 图片加载成功处理
    handleImageLoad(event) {
      console.log('用户头像加载成功:', event.target.src)
    },

    // 图片加载失败处理
    handleImageError(event) {
      console.error('用户头像加载失败:', event.target.src)
      // 使用默认头像
      this.userAvatar = '@/assets/images/user.png'
      console.log('已切换到默认头像')
    },

    // 聊天记录管理
    loadChatHistory() {
      const saved = localStorage.getItem('aiChatHistory')
      if (saved) {
        try {
          this.chatHistory = JSON.parse(saved)
          // 确保每个对话都有必要的字段
          this.chatHistory.forEach((chat, index) => {
            if (!chat.id) chat.id = this.generateChatId()
            if (!chat.createdAt) chat.createdAt = new Date()
            else chat.createdAt = new Date(chat.createdAt)
            if (!chat.updatedAt) chat.updatedAt = new Date()
            else chat.updatedAt = new Date(chat.updatedAt)
            if (!chat.messages) chat.messages = []

            // 修复消息中的时间戳格式
            chat.messages.forEach(message => {
              if (message.timestamp && !(message.timestamp instanceof Date)) {
                message.timestamp = new Date(message.timestamp)
              }
            })
          })


          // 为没有标题的对话分配正确编号
          this.fixChatTitles()
        } catch (error) {
          console.error('加载聊天记录失败:', error)
          this.chatHistory = []
        }
      }
    },

    saveChatHistory() {
      try {
        localStorage.setItem('aiChatHistory', JSON.stringify(this.chatHistory))
      } catch (error) {
        console.error('保存聊天记录失败:', error)
      }
    },

    generateChatId() {
      return 'chat_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    },

    // 确保对话ID唯一性
    ensureUniqueChatId(proposedId) {
      let finalId = proposedId
      let counter = 1

      // 检查ID是否已存在，如果存在则生成新的
      while (this.chatHistory.some(chat => chat.id === finalId)) {
        finalId = proposedId + '_' + counter
        counter++
      }

      return finalId
    },

    // 验证和修复对话ID
    validateAndFixChatIds() {
      console.log('开始验证对话ID...')
      const usedIds = new Set()

      this.chatHistory.forEach((chat, index) => {
        // 如果没有ID或ID为空，生成新的
        if (!chat.id || chat.id.trim() === '') {
          chat.id = this.generateChatId()
          console.log(`为对话 ${index} 生成新ID: ${chat.id}`)
        }

        // 如果ID重复，生成新的唯一ID
        if (usedIds.has(chat.id)) {
          const oldId = chat.id
          chat.id = this.generateChatId()
          console.log(`对话ID重复，从 ${oldId} 改为 ${chat.id}`)
        }

        usedIds.add(chat.id)

        // 确保ID格式正确（以chat_开头）
        if (!chat.id.startsWith('chat_')) {
          const oldId = chat.id
          chat.id = 'chat_' + Date.now() + '_' + oldId
          console.log(`修复对话ID格式，从 ${oldId} 改为 ${chat.id}`)
        }
      })

      console.log('对话ID验证完成，当前所有对话ID:')
      this.chatHistory.forEach((chat, index) => {
        console.log(`  对话 ${index}: ${chat.id} - ${chat.title}`)
      })
    },

    createNewChat() {
      // 计算下一个对话编号
      const nextChatNumber = this.getNextChatNumber()

      // 生成唯一ID
      let newChatId = this.generateChatId()
      newChatId = this.ensureUniqueChatId(newChatId)

      const newChat = {
        id: newChatId,
        title: `对话 ${nextChatNumber}`,
        messages: [],
        createdAt: new Date(),
        updatedAt: new Date()
      }

      console.log('创建新对话:', {
        id: newChat.id,
        title: newChat.title,
        timestamp: newChat.createdAt
      })

      this.chatHistory.unshift(newChat)
      this.currentChatId = newChat.id
      this.saveChatHistory()

      // 添加欢迎消息到新对话
      setTimeout(() => {
        this.addWelcomeMessage()
      }, 100)
    },

    getNextChatNumber() {
      // 找出所有现有对话中的最大编号
      let maxNumber = 0
      this.chatHistory.forEach(chat => {
        if (chat.title) {
          // 匹配 "对话 X" 格式的标题
          const match = chat.title.match(/^对话\s+(\d+)$/)
          if (match) {
            const number = parseInt(match[1])
            if (number > maxNumber) {
              maxNumber = number
            }
          }
        }
      })
      return maxNumber + 1
    },

    handleChatClick(chatId) {
      console.log('=== 点击事件触发 ===')
      console.log('handleChatClick触发，chatId:', chatId)
      console.log('当前对话历史:', this.chatHistory.map(c => ({ id: c.id, title: c.title })))
      console.log('当前选中的对话ID:', this.currentChatId)

      // 强制更新
      this.$forceUpdate()

      this.switchToChat(chatId)
    },

    handleChatMouseDown(chatId) {
      console.log('handleChatMouseDown触发，chatId:', chatId)
      // 处理鼠标按下事件，可以用于备用点击处理
    },

    switchToChat(chatId) {
      console.log('点击切换对话，目标ID:', chatId, '当前ID:', this.currentChatId)
      if (this.currentChatId !== chatId) {
        console.log('执行切换操作')
        this.currentChatId = chatId
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      } else {
        console.log('已经是当前对话，无需切换')
      }
    },

    async deleteChat(chatId) {
      console.log('删除对话:', chatId)
      if (this.chatHistory.length <= 1) {
        this.$message.warning('至少需要保留一个对话')
        return
      }

      try {
        await this.$confirm('确定要删除这个对话吗？删除后将无法恢复。', '删除对话', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          confirmButtonClass: 'el-button--danger',
          customClass: 'delete-chat-confirm'
        })

        // 确认删除后执行删除操作
        const chatIndex = this.chatHistory.findIndex(chat => chat.id === chatId)
        console.log('找到对话索引:', chatIndex)
        if (chatIndex !== -1) {
          this.chatHistory.splice(chatIndex, 1)
          console.log('删除后剩余对话数:', this.chatHistory.length)

          // 如果删除的是当前对话，切换到第一个对话
          if (this.currentChatId === chatId) {
            if (this.chatHistory.length > 0) {
              this.currentChatId = this.chatHistory[0].id
              console.log('切换到新的当前对话:', this.currentChatId)
              this.$nextTick(() => {
                this.scrollToBottom()
              })
            } else {
              this.currentChatId = null
              console.log('没有剩余对话，设置当前对话为null')
            }
          }

          this.saveChatHistory()
          this.$message.success('对话已删除')
        }
      } catch (error) {
        // 用户取消删除，不做任何操作
        console.log('用户取消删除操作')
      }
    },

    getCurrentChat() {
      return this.chatHistory.find(chat => chat.id === this.currentChatId) || null
    },

    getCurrentMessages() {
      const currentChat = this.getCurrentChat()
      if (!currentChat || !currentChat.messages) {
        return []
      }

      // 确保所有消息的timestamp都是Date对象
      currentChat.messages.forEach((message, index) => {
        if (message.timestamp && !(message.timestamp instanceof Date)) {
          console.log(`修复消息 ${index} 的时间戳:`, message.timestamp)
          message.timestamp = new Date(message.timestamp)
        }
        if (!message.timestamp) {
          console.log(`为消息 ${index} 添加默认时间戳`)
          message.timestamp = new Date()
        }
      })

      return currentChat.messages
    },

    getCurrentChatTitle() {
      const currentChat = this.getCurrentChat()
      if (!currentChat) return 'AI智能聊天'

      return currentChat.title || '新对话'
    },

    getLastMessage(chat) {
      if (!chat.messages || chat.messages.length === 0) {
        return '暂无消息'
      }

      const lastMessage = chat.messages[chat.messages.length - 1]
      const text = lastMessage.text || '...'
      return text.length > 30 ? text.substring(0, 30) + '...' : text
    },

    formatChatTime(timestamp) {
      try {
        // 确保timestamp是Date对象
        let date = timestamp
        if (!(timestamp instanceof Date)) {
          date = new Date(timestamp)
        }

        // 检查是否是有效的日期
        if (isNaN(date.getTime())) {
          return '刚刚'
        }

        const now = new Date()
        const diffTime = now - date
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))

        if (diffDays === 0) {
          return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
        } else if (diffDays === 1) {
          return '昨天'
        } else if (diffDays < 7) {
          return `${diffDays}天前`
        } else {
          return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
        }
      } catch (error) {
        console.error('格式化聊天时间失败:', error)
        return '刚刚'
      }
    },

    updateCurrentChat() {
      const currentChat = this.getCurrentChat()
      if (currentChat) {
        currentChat.updatedAt = new Date()

        // 完全禁用自动标题生成，保持原有标题不变
        console.log('=== updateCurrentChat 调用 ===')
        console.log('当前对话ID:', currentChat.id)
        console.log('当前对话标题:', currentChat.title)
        console.log('对话消息数量:', currentChat.messages.length)
        console.log('=============================')

        // 移动当前对话到列表顶部
        const chatIndex = this.chatHistory.findIndex(chat => chat.id === this.currentChatId)
        if (chatIndex > 0) {
          const chat = this.chatHistory.splice(chatIndex, 1)[0]
          this.chatHistory.unshift(chat)
        }

        this.saveChatHistory()
      }
    },

    // 原有方法
    adjustTextareaHeight() {
      this.$nextTick(() => {
        const textarea = this.$refs.messageInput
        if (textarea) {
          textarea.style.height = 'auto'
          textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
        }
      })
    },

    addWelcomeMessage() {
      const currentChat = this.getCurrentChat()
      if (!currentChat) return

      // 检查是否已经有欢迎消息
      const hasWelcome = currentChat.messages.some(msg =>
        !msg.isUser && msg.text.includes('您好！我是AI智能助手')
      )

      if (!hasWelcome) {
        setTimeout(() => {
          if (currentChat && currentChat.messages) {
            currentChat.messages.push({
              text: '您好！我是AI智能助手，很高兴为您服务。请告诉我您想了解什么，我会尽力为您提供帮助。',
              isUser: false,
              timestamp: new Date(),
              isLoading: false
            })
            // 只更新时间戳和保存，不调用updateCurrentChat避免标题被修改
            currentChat.updatedAt = new Date()
            this.saveChatHistory()
            this.scrollToBottom()
          }
        }, 1000)
      }
    },

    handleKeydown(event) {
      if (event.shiftKey) {
        return // 允许 Shift+Enter 换行
      }
      this.sendMessage()
    },

    async sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) return

      const currentChat = this.getCurrentChat()
      if (!currentChat) return

      const userMessage = this.inputMessage.trim()
      this.inputMessage = ''

      // 重置输入框高度
      this.$nextTick(() => {
        const textarea = this.$refs.messageInput
        if (textarea) {
          textarea.style.height = 'auto'
        }
      })

      // 添加用户消息
      currentChat.messages.push({
        text: userMessage,
        isUser: true,
        timestamp: new Date(),
        isLoading: false
      })

      // 添加AI加载消息
      const loadingMessageIndex = currentChat.messages.length
      currentChat.messages.push({
        text: '',
        isUser: false,
        timestamp: new Date(),
        isLoading: true
      })

      this.isLoading = true
      this.scrollToBottom()

      try {
        // 调用流式AI API
        await this.callStreamAI(userMessage, currentChat, loadingMessageIndex)
      } catch (error) {
        console.error('发送消息失败:', error)
        if (currentChat.messages[loadingMessageIndex]) {
          currentChat.messages[loadingMessageIndex] = {
            text: '抱歉，我现在无法回答您的问题。请稍后再试。',
            isUser: false,
            timestamp: new Date(),
            isLoading: false
          }
        }
      } finally {
        this.isLoading = false
        // 直接更新时间戳和保存，避免调用updateCurrentChat修改标题
        const currentChat = this.getCurrentChat()
        if (currentChat) {
          currentChat.updatedAt = new Date()

          // 移动当前对话到列表顶部（如果不是第一个）
          const chatIndex = this.chatHistory.findIndex(chat => chat.id === this.currentChatId)
          if (chatIndex > 0) {
            const chat = this.chatHistory.splice(chatIndex, 1)[0]
            this.chatHistory.unshift(chat)
          }

          this.saveChatHistory()
          console.log('发送消息后更新，保持标题不变:', currentChat.title)
        }
        this.scrollToBottom()
      }
    },

    // 生成模拟回复（用于测试）
    generateMockResponse(question) {
      const responses = [
        '这是一个很有趣的问题。让我来为您详细解答...',
        '根据我的理解，您想了解的是...',
        '我建议您可以从以下几个方面来考虑这个问题：\n1. 首先...\n2. 其次...\n3. 最后...',
        '这个问题涉及到多个方面，我来为您逐一分析。',
        '非常好的问题！基于我的知识，我认为...'
      ]

      return responses[Math.floor(Math.random() * responses.length)]
    },

    // 调用流式AI API
    async callStreamAI(question, currentChat, loadingMessageIndex) {
      const requestData = {
        message: question,
        sessionId: this.currentChatId,
        newSession: false
      }

      console.log('发送流式请求:', requestData)

      let fullResponse = ''

      await chatWithAIStream(
        requestData,
        // onMessage - 处理每个流式数据块
        (data) => {
          if (data && data.reply) {
            fullResponse += data.reply

            // 实时更新消息内容
            if (currentChat.messages[loadingMessageIndex]) {
              currentChat.messages[loadingMessageIndex] = {
                text: fullResponse,
                isUser: false,
                timestamp: new Date(),
                isLoading: false,
                isStreaming: true
              }
              this.scrollToBottom()
            }
          }
        },
        // onComplete - 流式完成
        () => {
          console.log('流式输出完成，最终内容:', fullResponse)
          if (currentChat.messages[loadingMessageIndex]) {
            currentChat.messages[loadingMessageIndex].isStreaming = false
          }
        },
        // onError - 错误处理
        (error) => {
          console.error('流式AI调用失败:', error)
          if (currentChat.messages[loadingMessageIndex]) {
            currentChat.messages[loadingMessageIndex] = {
              text: fullResponse || '抱歉，AI服务出现错误，请稍后再试。',
              isUser: false,
              timestamp: new Date(),
              isLoading: false,
              isError: true
            }
          }
        }
      )
    },

    // 调用真实AI API (保留作为备用)
    async callRealAI(question) {
      try {
        console.log('发送给AI的问题:', question)

        // 构造请求数据，匹配后端接口格式
        const requestData = {
          message: question,
          sessionId: this.currentChatId, // 使用当前聊天ID作为会话ID
          newSession: false // 默认不开始新会话
        }

        console.log('发送的请求数据:', requestData)

        const response = await chatWithAI(requestData)
        console.log('API返回的原始响应:', response)

        // 检查响应格式
        if (response && response.code === 200 && response.data) {
          const aiReply = response.data.reply
          console.log('AI回复内容:', aiReply)

          if (aiReply && aiReply.trim().length > 0) {
            return aiReply
          } else {
            console.warn('AI回复内容为空')
            return '抱歉，我暂时没有回复内容。'
          }
        } else {
          console.warn('响应格式异常:', response)
          return '抱歉，服务器返回了异常响应。'
        }
      } catch (error) {
        console.error('AI API调用失败:', error)
        console.error('错误详情:', error.response?.data || error.message)
        console.error('完整的错误对象:', error)

        // 尝试从错误响应中获取具体错误信息
        if (error.response?.data?.message) {
          return `抱歉，AI服务出现错误：${error.response.data.message}`
        }

        return '抱歉，AI服务暂时不可用。请检查网络连接或稍后再试。'
      }
    },

    formatMessage(text) {
      if (!text) return ''

      // Markdown格式化，但去除原始符号
      let formattedText = text
        // 先处理单独的#号行
        .replace(/^###\s*$/gm, '')  // 删除单独的###
        .replace(/^##\s*$/gm, '')   // 删除单独的##
        .replace(/^#\s*$/gm, '')    // 删除单独的#
        // 再处理带内容的标题
        .replace(/^####\s*(.+)$/gm, '<h4 style="margin: 1rem 0 0.5rem 0; color: #333; font-size: 1.05rem; font-weight: bold;">$1</h4>')  // 四级标题
        .replace(/^###\s*(.+)$/gm, '<h3 style="margin: 1rem 0 0.5rem 0; color: #333; font-size: 1.15rem; font-weight: bold;">$1</h3>')  // 三级标题
        .replace(/^##\s*(.+)$/gm, '<h2 style="margin: 1rem 0 0.5rem 0; color: #333; font-size: 1.25rem; font-weight: bold;">$1</h2>')  // 二级标题
        .replace(/^#\s*(.+)$/gm, '<h1 style="margin: 1rem 0 0.5rem 0; color: #333; font-size: 1.35rem; font-weight: bold;">$1</h1>')  // 一级标题
        // 格式化粗体和斜体
        .replace(/\*\*(.+?)\*\*/g, '<strong style="font-weight: bold; color: #2c3e50;">$1</strong>')  // 粗体，去除**
        .replace(/\*([^*]+?)\*/g, '<em style="font-style: italic;">$1</em>')  // 斜体，去除*
        // 最后处理换行符
        .replace(/\n/g, '<br>')  // 换行符转换
        // 清理多余的换行
        .replace(/<br>\s*<br>/g, '<br>')

      return formattedText
    },

    formatTime(timestamp) {
      try {
        // 确保timestamp是Date对象
        let date = timestamp
        if (!(timestamp instanceof Date)) {
          date = new Date(timestamp)
        }

        // 检查是否是有效的日期
        if (isNaN(date.getTime())) {
          return '刚刚'
        }

        return date.toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch (error) {
        console.error('格式化时间失败:', error)
        return '刚刚'
      }
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },

    checkOnlineStatus() {
      this.isOnline = navigator.onLine
      window.addEventListener('online', () => {
        this.isOnline = true
      })
      window.addEventListener('offline', () => {
        this.isOnline = false
      })
    },

    fixAllTimestamps() {
      console.log('开始修复所有时间戳...')
      this.chatHistory.forEach((chat, chatIndex) => {
        if (chat.messages) {
          chat.messages.forEach((message, msgIndex) => {
            if (message.timestamp && !(message.timestamp instanceof Date)) {
              console.log(`修复对话 ${chatIndex} 消息 ${msgIndex} 的时间戳:`, message.timestamp)
              message.timestamp = new Date(message.timestamp)
            }
            if (!message.timestamp) {
              console.log(`为对话 ${chatIndex} 消息 ${msgIndex} 添加默认时间戳`)
              message.timestamp = new Date()
            }
          })
        }
      })
      console.log('时间戳修复完成')
    },

    fixChatTitles() {
      console.log('开始修复对话标题...')
      let maxNumber = 0

      // 首先找出所有现有的编号
      this.chatHistory.forEach(chat => {
        if (chat.title) {
          const match = chat.title.match(/^对话\s+(\d+)$/)
          if (match) {
            const number = parseInt(match[1])
            if (number > maxNumber) {
              maxNumber = number
            }
          }
        }
      })

      // 为没有标题或标题为空的对话分配编号
      this.chatHistory.forEach(chat => {
        if (!chat.title || chat.title.trim() === '') {
          maxNumber++
          chat.title = `对话 ${maxNumber}`
          console.log(`为对话分配标题: ${chat.title}`)
        }
      })

      console.log('对话标题修复完成')
    }
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 隐藏所有滚动条 */
* {
  scrollbar-width: none;
  /* Firefox */
  -ms-overflow-style: none;
  /* IE and Edge */
}

*::-webkit-scrollbar {
  display: none;
  /* Chrome, Safari and Opera */
}

.ai-chat {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: #f5f5f5;
  z-index: 9999;
  overflow: hidden;
}

.chat-layout {
  display: flex;
  width: 100%;
  height: 100vh;
}

/* 左侧聊天记录面板 */
.chat-sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  padding: 1rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.new-chat-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.new-chat-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.new-chat-btn svg {
  width: 18px;
  height: 18px;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem 0;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
  position: relative;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}

.chat-item:hover {
  background: #f8f9fa;
}

.chat-item.active {
  background: #e3f2fd;
  border-left-color: #667eea;
}

.chat-preview {
  flex: 1;
  min-width: 0;
  cursor: pointer;
  pointer-events: auto;
}

.chat-title {
  font-weight: 600;
  color: #333;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-last-message {
  color: #666;
  font-size: 0.8rem;
  line-height: 1.3;
  margin-bottom: 0.25rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.chat-time {
  color: #999;
  font-size: 0.75rem;
}

.delete-chat-btn {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  opacity: 0;
  margin-left: 0.5rem;
  pointer-events: auto;
  z-index: 10;
  flex-shrink: 0;
}

.chat-item:hover .delete-chat-btn {
  opacity: 1;
}

.delete-chat-btn:hover {
  background: #ffebee;
  color: #f44336;
}

.delete-chat-btn svg {
  width: 14px;
  height: 14px;
}

.empty-history {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #999;
  text-align: center;
}

.create-first-chat {
  margin-top: 1rem;
  padding: 0.5rem 1rem;
  border: 1px solid #667eea;
  border-radius: 20px;
  background: white;
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s ease;
}

.create-first-chat:hover {
  background: #667eea;
  color: white;
}

/* 右侧对话区域 */
.chat-container {
  flex: 1;
  background: white;
  display: flex;
  flex-direction: column;
  max-width: calc(100% - 300px);
  overflow: hidden;
}

/* 确保页面级别不显示滚动条并隐藏footer */
html,
body {
  overflow: hidden !important;
  scrollbar-width: none !important;
  -ms-overflow-style: none !important;
}

html::-webkit-scrollbar,
body::-webkit-scrollbar {
  display: none !important;
}

/* 隐藏可能的footer元素 */
body footer,
body .footer,
body #footer {
  display: none !important;
}

/* 确保AI聊天页面完全覆盖 */
.ai-chat {
  background: white !important;
}

.chat-header {
  padding: 1rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-btn {
  color: white;
  text-decoration: none;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.3s;
}

.back-btn:hover {
  opacity: 0.8;
}

.header-center {
  flex: 1;
  text-align: center;
  padding: 0 1rem;
}

.chat-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-info {
  font-size: 0.75rem;
  opacity: 0.8;
  margin-top: 0.25rem;
}

.status {
  font-size: 0.875rem;
  padding: 0.25rem 0.75rem;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.2);
}

.status.online {
  background: rgba(46, 213, 115, 0.3);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  background: #f8f9fa;
  min-height: 0;
  margin-bottom: 0;
  scrollbar-width: none;
  /* Firefox */
  -ms-overflow-style: none;
  /* IE and Edge */
}

.chat-messages::-webkit-scrollbar {
  display: none;
  /* Chrome, Safari and Opera */
}

.welcome-message {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.welcome-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.welcome-message h3 {
  margin: 0 0 0.5rem 0;
  color: #333;
}

.message {
  display: flex;
  margin-bottom: 1rem;
  animation: fadeIn 0.3s ease-in;
  max-width: 100%;
  overflow: hidden;
}

.message.user-message {
  justify-content: flex-end;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  margin: 0;
}

.user-message .message-avatar {
  margin: 0;
}

.ai-message .message-avatar {
  margin: 0 0.5rem 0 0;
}

.user-avatar {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  width: 100%;
  height: 100%;
  border-radius: 50%;
  position: relative;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  display: block;
}

.ai-avatar {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  word-wrap: break-word;
  word-break: break-all;
  overflow: hidden;
}

.user-message .message-content {
  align-items: flex-end;
}

.ai-message .message-content {
  align-items: flex-start;
}

.message-bubble {
  padding: 0.75rem 1rem;
  border-radius: 18px;
  word-wrap: break-word;
  word-break: break-word;
  line-height: 1.6;
  max-width: 100%;
  overflow-wrap: break-word;
  white-space: pre-wrap;
  text-align: left;
}

.message-bubble h3 {
  margin: 1rem 0 0.5rem 0 !important;
  color: #333 !important;
  font-size: 1.1rem !important;
  font-weight: bold !important;
}

.message-bubble strong {
  font-weight: bold !important;
  color: #2c3e50 !important;
}

.message-bubble div {
  margin: 0.5rem 0 !important;
}

.user-message .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.ai-message .message-bubble {
  background: white;
  color: #333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-time {
  font-size: 0.75rem;
  color: #999;
  margin: 0.25rem 0.5rem;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ccc;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

.chat-input {
  padding: 1.5rem;
  background: white;
  border-top: 1px solid #eee;
  position: relative;
  z-index: 100;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 0.75rem;
  max-width: 600px;
  width: 100%;
  position: relative;
  z-index: 101;
  margin: 0 auto;
}

.input-container textarea {
  flex: 1;
  min-height: 52px;
  max-height: 120px;
  padding: 1rem 1.25rem;
  border: 2px solid #e9ecef;
  border-radius: 26px;
  resize: none;
  font-size: 1rem;
  font-family: inherit;
  outline: none;
  transition: all 0.3s ease;
  background: white;
  box-sizing: border-box;
  line-height: 1.4;
  position: relative;
  z-index: 101;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.input-container textarea:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1), 0 4px 12px rgba(0, 0, 0, 0.1);
}

.send-btn {
  width: 52px;
  height: 52px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  position: relative;
  z-index: 102;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn svg {
  width: 20px;
  height: 20px;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes typing {

  0%,
  60%,
  100% {
    transform: translateY(0);
  }

  30% {
    transform: translateY(-10px);
  }
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

@keyframes blink {

  0%,
  50% {
    opacity: 1;
  }

  51%,
  100% {
    opacity: 0;
  }
}

.streaming-cursor {
  display: inline-block;
  animation: blink 1s infinite;
  color: #667eea;
  font-weight: bold;
  margin-left: 2px;
}

/* 确保用户消息不超出边界 */
.user-message .message-content {
  max-width: calc(70% - 70px);
}

.ai-message .message-content {
  max-width: calc(70% - 70px);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .chat-layout {
    flex-direction: column;
  }

  .chat-sidebar {
    width: 100%;
    height: 200px;
    border-right: none;
    border-bottom: 1px solid #e9ecef;
  }

  .chat-container {
    max-width: 100%;
    flex: 1;
  }

  .chat-item {
    padding: 1rem;
    -webkit-tap-highlight-color: rgba(102, 126, 234, 0.1);
    tap-highlight-color: rgba(102, 126, 234, 0.1);
  }

  .chat-item:active {
    background: #e3f2fd;
    transform: scale(0.98);
  }

  .chat-input {
    padding: 1rem;
  }

  .input-container {
    gap: 0.5rem;
    max-width: 95%;
  }

  .input-container textarea {
    padding: 0.875rem 1rem;
    border-radius: 22px;
    min-height: 48px;
  }

  .send-btn {
    width: 48px;
    height: 48px;
  }

  .user-message .message-content {
    max-width: calc(80% - 70px);
  }

  .ai-message .message-content {
    max-width: calc(80% - 70px);
  }

  .user-message .message-avatar-wrapper {
    margin: 0 0 0 0.25rem;
  }

  .ai-message .message-avatar-wrapper {
    margin: 0 0.25rem 0 0;
  }

  .user-nickname,
  .ai-nickname {
    font-size: 0.65rem;
    max-width: 50px;
  }

  .chat-header h2 {
    font-size: 1rem;
  }

  .sidebar-header h3 {
    font-size: 1rem;
  }
}

/* 删除对话确认框样式 */
::v-deep .delete-chat-confirm {
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

::v-deep .delete-chat-confirm .el-message-box__header {
  padding: 24px 24px 12px;
  border-bottom: none;
}

::v-deep .delete-chat-confirm .el-message-box__title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

::v-deep .delete-chat-confirm .el-message-box__content {
  padding: 12px 24px 24px;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

::v-deep .delete-chat-confirm .el-message-box__btns {
  padding: 16px 24px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

::v-deep .delete-chat-confirm .el-button {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  border: none;
  min-width: 80px;
}

::v-deep .delete-chat-confirm .el-button--default {
  background: #f5f7fa;
  color: #606266;
  transition: all 0.3s ease;
}

::v-deep .delete-chat-confirm .el-button--default:hover {
  background: #e4e7ed;
  color: #303133;
}

::v-deep .delete-chat-confirm .el-button--danger {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
  color: white;
  transition: all 0.3s ease;
}

::v-deep .delete-chat-confirm .el-button--danger:hover {
  background: linear-gradient(135deg, #ff5252 0%, #e53935 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(238, 90, 82, 0.3);
}

::v-deep .delete-chat-confirm .el-message-box__status.el-icon-warning {
  color: #f39c12;
  font-size: 24px;
}

.message-avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 0 0 0.5rem;
}

.user-message .message-avatar-wrapper {
  margin: 0 0 0 0.5rem;
}

.ai-message .message-avatar-wrapper {
  margin: 0 0.5rem 0 0;
}

.user-nickname {
  font-size: 0.7rem;
  color: #666;
  text-align: center;
  margin-top: 0.25rem;
  word-break: break-word;
  max-width: 60px;
}

.ai-nickname {
  font-size: 0.7rem;
  color: #666;
  text-align: center;
  margin-top: 0.25rem;
  word-break: break-word;
  max-width: 60px;
}
</style>