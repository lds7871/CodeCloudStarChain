import request from '@/utils/request'
import { getToken } from '@/utils/cookie'

export function sendMsg(data) {
  return request({
    url: `/chat/sendMsg`,
    method: 'post',
    data
  })
}

export function recallMsgApi(data) {
  return request({
    url: `/chat/recallMsg`,
    method: 'post',
    data
  })
}

export function getChatMsgListApi(params) {
  return request({
    url: `/chat/list`,
    method: 'get',
    params
  })
}

// AI聊天相关API
export function chatWithAI(data) {
  console.log('普通AI API调用，URL: /ai/chat')
  return request({
    url: `/ai/chat`,
    method: 'post',
    data
  })
}

// AI流式聊天API
export function chatWithAIStream(data, onMessage, onComplete, onError) {
  // 调试baseURL配置
  console.log('VITE_APP_BASE_API:', import.meta.env.VITE_APP_BASE_API)

  // 使用与其他API一致的baseURL配置，如果没有配置则使用空字符串（相对路径）
  const baseURL = import.meta.env.VITE_APP_BASE_API || ''
  const fullURL = `${baseURL}/ai/chatStream`

  console.log('流式API完整URL:', fullURL)

  // 使用与其他API一致的token获取方式
  const token = getToken() || ''
  console.log('使用的token:', token ? token.substring(0, 10) + '...' : '无token')

  return fetch(fullURL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token,
      'Accept': 'text/event-stream',
      'Cache-Control': 'no-cache'
    },
    body: JSON.stringify(data)
  }).then(response => {
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    function readStream() {
      return reader.read().then(({ done, value }) => {
        if (done) {
          if (onComplete) onComplete()
          return
        }

        const chunk = decoder.decode(value, { stream: true })
        console.log('收到SSE数据块:', chunk)

        // 更强大的SSE数据处理方式
        const lines = chunk.split('\n')

        for (let i = 0; i < lines.length; i++) {
          const line = lines[i].trim()

          if (!line) continue // 跳过空行

          console.log('=== 处理SSE行:', line)

          let dataContent = null
          let eventType = 'message' // 默认为message事件

          if (line.startsWith('event:')) {
            eventType = line.substring(6).trim()
            console.log('=== SSE事件类型:', eventType)
          } else if (line.startsWith('data:')) {
            dataContent = line.substring(5).trim()
            console.log('=== SSE数据内容:', dataContent)
          } else {
            // 如果不是event:或data:开头，尝试直接解析为JSON数据
            try {
              JSON.parse(line)
              dataContent = line
              console.log('=== 直接解析为JSON数据:', dataContent)
            } catch (e) {
              console.log('=== 不是JSON数据，跳过:', line)
              continue
            }
          }

          // 如果有数据内容，立即处理
          if (dataContent && dataContent !== 'undefined' && dataContent !== 'null') {
            console.log('=== 处理数据内容:', dataContent)

            try {
              const jsonData = JSON.parse(dataContent)
              console.log('=== 解析成功的JSON数据:', jsonData)

              if (eventType === 'message' && jsonData) {
                console.log('=== 调用onMessage回调，数据:', jsonData)
                if (onMessage) {
                  onMessage(jsonData)
                  console.log('=== onMessage回调调用完成')
                }
              } else if (eventType === 'complete') {
                console.log('=== 收到完成事件')
                if (onComplete) onComplete()
                return
              } else if (eventType === 'error') {
                console.log('=== 收到错误事件:', dataContent)
                if (onError) onError({ message: dataContent })
                return
              }
            } catch (e) {
              console.error('=== 解析SSE数据失败:', e, '原始数据:', dataContent)
            }
          }
        }

        return readStream() // 继续读取
      })
    }

    return readStream()
  }).catch(error => {
    console.error('Stream request failed:', error)
    if (onError) onError(error)
  })
}

export function clearAISession(params) {
  return request({
    url: `/ai/clearSession`,
    method: 'post',
    params
  })
}

export function getAISessionStatus(params) {
  return request({
    url: `/ai/sessionStatus`,
    method: 'get',
    params
  })
} 