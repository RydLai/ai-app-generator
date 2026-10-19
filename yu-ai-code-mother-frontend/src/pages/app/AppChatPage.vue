<template>
  <div id="appChatPage">
    <!-- 工作台顶部栏 -->
    <div class="workspace-bar">
      <div class="workspace-bar__left">
        <a-tooltip title="返回首页">
          <button type="button" class="back-btn" aria-label="返回首页" @click="router.push('/')">
            <ArrowLeftOutlined />
          </button>
        </a-tooltip>

        <div class="app-identity">
          <span class="app-avatar">{{ appInitial }}</span>
          <div class="app-identity__text">
            <div class="app-identity__title">
              <h1>{{ appInfo?.appName || '网站生成器' }}</h1>
              <a-tag v-if="appInfo?.codeGenType" class="code-gen-tag">
                {{ formatCodeGenType(appInfo.codeGenType) }}
              </a-tag>
              <span v-if="appInfo?.priority === 99" class="featured-tag">
                <StarFilled />
                精选
              </span>
              <span v-if="isGenerating" class="live-tag">
                <i aria-hidden="true"></i>
                生成中
              </span>
            </div>
            <p class="app-identity__sub">
              {{ isOwner ? '创作者模式 · 对话即修改' : '浏览模式 · 只读访问' }}
            </p>
          </div>
        </div>
      </div>

      <div class="workspace-bar__actions">
        <a-tooltip title="应用详情">
          <button type="button" class="tool-btn" aria-label="应用详情" @click="showAppDetail">
            <InfoCircleOutlined />
          </button>
        </a-tooltip>

        <a-button
          class="soft-btn"
          title="下载代码"
          aria-label="下载代码"
          :loading="downloading"
          :disabled="!isOwner"
          @click="downloadCode"
        >
          <template #icon><DownloadOutlined /></template>
          <span class="btn-label">下载代码</span>
        </a-button>

        <a-button
          type="primary"
          class="deploy-btn"
          title="部署应用"
          aria-label="部署应用"
          :loading="deploying"
          @click="deployApp"
        >
          <template #icon><CloudUploadOutlined /></template>
          <span class="btn-label">部署</span>
        </a-button>
      </div>
    </div>

    <!-- 工作区：左对话 · 右预览 -->
    <div class="workspace">
      <!-- 对话面板 -->
      <section class="panel chat-panel">
        <header class="panel-head">
          <div class="panel-title">
            <span class="panel-icon"><MessageOutlined /></span>
            <div>
              <h2>AI 对话</h2>
              <p>描述你想要的修改，边说边改</p>
            </div>
          </div>
          <span v-if="messages.length" class="panel-count">{{ messages.length }} 条</span>
        </header>

        <!-- 消息区域 -->
        <div ref="messagesContainer" class="messages-container">
          <div v-if="hasMoreHistory" class="load-more-container">
            <button
              type="button"
              class="load-more-btn"
              :disabled="loadingHistory"
              @click="loadMoreHistory"
            >
              <LoadingOutlined v-if="loadingHistory" />
              <HistoryOutlined v-else />
              加载更早的消息
            </button>
          </div>

          <div v-if="!messages.length && !isGenerating" class="chat-empty">
            <span class="chat-empty__icon"><RobotOutlined /></span>
            <h3>从一个想法开始</h3>
            <p>比如「做一个暖色调的咖啡店官网，包含菜单和门店信息」</p>
          </div>

          <div
            v-for="(message, index) in messages"
            :key="index"
            class="message-item"
            :class="`message-item--${message.type}`"
          >
            <template v-if="message.type === 'user'">
              <div class="message-stack">
                <div class="message-meta">
                  <span>你</span>
                  <time v-if="message.createTime">{{ formatTime(message.createTime, 'HH:mm') }}</time>
                </div>
                <div class="user-bubble">{{ message.content }}</div>
              </div>
              <a-avatar
                class="message-avatar message-avatar--user"
                :src="loginUserStore.loginUser.userAvatar"
                :size="34"
              >
                {{ userInitial }}
              </a-avatar>
            </template>

            <template v-else>
              <a-avatar class="message-avatar message-avatar--ai" :src="aiAvatar" :size="34" />
              <div class="message-stack">
                <div class="message-meta">
                  <span>AI零代码</span>
                  <span class="ai-badge">AI</span>
                  <time v-if="message.createTime">{{ formatTime(message.createTime, 'HH:mm') }}</time>
                </div>
                <div class="ai-bubble">
                  <MarkdownRenderer
                    v-if="message.content"
                    :content="message.content"
                    :streaming="message.streaming"
                  />
                  <div v-if="message.loading" class="thinking">
                    <span class="thinking-dots" aria-hidden="true">
                      <i></i><i></i><i></i>
                    </span>
                    <span>AI 正在构思页面…</span>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>

        <!-- 选中元素信息 -->
        <div v-if="selectedElementInfo" class="element-card">
          <span class="element-card__icon"><EditOutlined /></span>
          <div class="element-card__body">
            <div class="element-card__head">
              <strong>已选中页面元素</strong>
              <code>{{ selectedElementInfo.tagName.toLowerCase() }}</code>
              <em v-if="selectedElementInfo.id">#{{ selectedElementInfo.id }}</em>
              <em v-if="selectedElementInfo.className">
                .{{ selectedElementInfo.className.split(' ').join('.') }}
              </em>
            </div>
            <div class="element-card__meta">
              <span v-if="selectedElementInfo.textContent">
                内容：{{ selectedElementInfo.textContent.substring(0, 50) }}
                {{ selectedElementInfo.textContent.length > 50 ? '…' : '' }}
              </span>
              <span v-if="selectedElementInfo.pagePath">
                页面：{{ selectedElementInfo.pagePath }}
              </span>
              <span class="element-selector">
                选择器：<code>{{ selectedElementInfo.selector }}</code>
              </span>
            </div>
          </div>
          <button
            type="button"
            class="element-card__close"
            aria-label="取消选中"
            @click="clearSelectedElement"
          >
            <CloseOutlined />
          </button>
        </div>

        <!-- 输入区域 -->
        <div class="composer-wrap">
          <div class="chat-composer" :class="{ 'is-element-mode': selectedElementInfo }">
            <a-tooltip v-if="!isOwner" title="无法在别人的作品下对话哦~" placement="top">
              <a-textarea
                v-model:value="userInput"
                :placeholder="getInputPlaceholder()"
                :bordered="false"
                :maxlength="1000"
                :auto-size="{ minRows: 2, maxRows: 6 }"
                class="chat-textarea"
                :disabled="true"
              />
            </a-tooltip>
            <a-textarea
              v-else
              v-model:value="userInput"
              :placeholder="getInputPlaceholder()"
              :bordered="false"
              :maxlength="1000"
              :auto-size="{ minRows: 2, maxRows: 6 }"
              class="chat-textarea"
              :disabled="isGenerating"
              @keydown="handleComposerKeydown"
            />

            <div class="chat-composer__foot">
              <span class="composer-hint">
                <template v-if="selectedElementInfo">描述修改想法，AI 会定位到该元素</template>
                <template v-else>Enter 发送 · Shift + Enter 换行</template>
              </span>
              <button
                type="button"
                class="send-btn"
                :disabled="isGenerating || !isOwner || !userInput.trim()"
                :aria-label="isGenerating ? '正在生成' : '发送消息'"
                @click="sendMessage"
              >
                <LoadingOutlined v-if="isGenerating" />
                <SendOutlined v-else />
              </button>
            </div>
          </div>
          <p v-if="!isOwner" class="composer-note">这是他人的作品，暂不支持对话修改</p>
        </div>
      </section>

      <!-- 预览面板 -->
      <section class="panel preview-panel">
        <header class="panel-head preview-head">
          <div class="panel-title">
            <span class="panel-icon panel-icon--preview"><DesktopOutlined /></span>
            <div>
              <h2>实时预览</h2>
              <p>{{ previewUrl ? '生成结果实时同步' : '等待生成' }}</p>
            </div>
          </div>

          <div class="preview-actions">
            <div class="device-switch" role="group" aria-label="预览设备">
              <button
                type="button"
                :class="{ 'is-active': previewDevice === 'desktop' }"
                aria-label="桌面端预览"
                @click="previewDevice = 'desktop'"
              >
                <DesktopOutlined />
              </button>
              <button
                type="button"
                :class="{ 'is-active': previewDevice === 'mobile' }"
                aria-label="移动端预览"
                @click="previewDevice = 'mobile'"
              >
                <MobileOutlined />
              </button>
            </div>

            <a-tooltip
              v-if="isOwner && previewUrl"
              :title="isEditMode ? '退出编辑模式' : '开启可视化编辑'"
            >
              <button
                type="button"
                class="tool-btn tool-btn--sm"
                :class="{ 'is-active': isEditMode }"
                @click="toggleEditMode"
              >
                <EditOutlined />
              </button>
            </a-tooltip>

            <a-tooltip v-if="previewUrl" title="在新窗口打开">
              <button type="button" class="tool-btn tool-btn--sm" @click="openInNewTab">
                <ExportOutlined />
              </button>
            </a-tooltip>
          </div>
        </header>

        <div class="preview-body">
          <div class="browser" :class="`browser--${previewDevice}`">
            <div class="browser-bar">
              <span class="browser-dots" aria-hidden="true"><i></i><i></i><i></i></span>
              <span class="browser-url">
                <LockOutlined />
                {{ browserUrl }}
              </span>
            </div>

            <div class="preview-content">
              <div v-if="!previewUrl && !isGenerating" class="preview-placeholder">
                <span class="placeholder-visual"><GlobalOutlined /></span>
                <h3>网站会在这里出现</h3>
                <p>完成一次对话后，AI 生成的页面将实时展示</p>
              </div>

              <div v-else-if="isGenerating" class="preview-loading">
                <a-spin size="large" />
                <h3>正在生成网站…</h3>
                <p>页面结构、样式与交互正在同步构建</p>
              </div>

              <iframe
                v-else
                :src="previewUrl"
                class="preview-iframe"
                frameborder="0"
                title="生成页面预览"
                @load="onIframeLoad"
              ></iframe>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- 应用详情弹窗 -->
    <AppDetailModal
      v-model:open="appDetailVisible"
      :app="appInfo"
      :show-actions="isOwner || isAdmin"
      @edit="editApp"
      @delete="deleteApp"
    />

    <!-- 部署成功弹窗 -->
    <DeploySuccessModal
      v-model:open="deployModalVisible"
      :deploy-url="deployUrl"
      @open-site="openDeployedSite"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message as antMessage } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import {
  getAppVoById,
  deployApp as deployAppApi,
  deleteApp as deleteAppApi,
} from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { CodeGenTypeEnum, formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import request from '@/request'

import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import DeploySuccessModal from '@/components/DeploySuccessModal.vue'
import aiAvatar from '@/assets/aiAvatar.png'
import { API_BASE_URL, getStaticPreviewUrl } from '@/config/env'
import { VisualEditor, type ElementInfo } from '@/utils/visualEditor'

import {
  ArrowLeftOutlined,
  CloseOutlined,
  CloudUploadOutlined,
  DesktopOutlined,
  DownloadOutlined,
  EditOutlined,
  ExportOutlined,
  GlobalOutlined,
  HistoryOutlined,
  InfoCircleOutlined,
  LoadingOutlined,
  MessageOutlined,
  MobileOutlined,
  RobotOutlined,
  SendOutlined,
  StarFilled,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用信息
const appInfo = ref<API.AppVO>()
const appId = ref<any>()

// 对话相关
interface Message {
  type: 'user' | 'ai'
  content: string
  loading?: boolean
  streaming?: boolean
  createTime?: string
}

const messages = ref<Message[]>([])
const userInput = ref('')
const isGenerating = ref(false)
const messagesContainer = ref<HTMLElement>()

// 对话历史相关
const loadingHistory = ref(false)
const hasMoreHistory = ref(false)
const lastCreateTime = ref<string>()
const historyLoaded = ref(false)

// 预览相关
const previewUrl = ref('')
const previewReady = ref(false)
const previewDevice = ref<'desktop' | 'mobile'>('desktop')

// 部署相关
const deploying = ref(false)
const deployModalVisible = ref(false)
const deployUrl = ref('')

// 下载相关
const downloading = ref(false)

// 可视化编辑相关
const isEditMode = ref(false)
const selectedElementInfo = ref<ElementInfo | null>(null)
const visualEditor = new VisualEditor({
  onElementSelected: (elementInfo: ElementInfo) => {
    selectedElementInfo.value = elementInfo
  },
})

// 权限相关
const isOwner = computed(() => {
  return appInfo.value?.userId === loginUserStore.loginUser.id
})

const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin'
})

// 展示信息
const appInitial = computed(() => (appInfo.value?.appName || '应用').trim().charAt(0).toUpperCase())
const userInitial = computed(
  () => loginUserStore.loginUser.userName?.charAt(0) || 'U',
)
const browserUrl = computed(() => {
  if (!previewUrl.value) return 'about:blank'
  return previewUrl.value.replace(/^https?:\/\/[^/]+/i, '')
})

// 应用详情相关
const appDetailVisible = ref(false)

// 显示应用详情
const showAppDetail = () => {
  appDetailVisible.value = true
}

// 加载对话历史
const loadChatHistory = async (isLoadMore = false) => {
  if (!appId.value || loadingHistory.value) return
  loadingHistory.value = true
  try {
    const params: API.listAppChatHistoryParams = {
      appId: appId.value,
      pageSize: 10,
    }
    // 如果是加载更多，传递最后一条消息的创建时间作为游标
    if (isLoadMore && lastCreateTime.value) {
      params.lastCreateTime = lastCreateTime.value
    }
    const res = await listAppChatHistory(params)
    if (res.data.code === 0 && res.data.data) {
      const chatHistories = res.data.data.records || []
      if (chatHistories.length > 0) {
        // 将对话历史转换为消息格式，并按时间正序排列（老消息在前）
        const historyMessages: Message[] = chatHistories
          .map((chat) => ({
            type: (chat.messageType === 'user' ? 'user' : 'ai') as 'user' | 'ai',
            content: chat.message || '',
            createTime: chat.createTime,
          }))
          .reverse() // 反转数组，让老消息在前
        if (isLoadMore) {
          // 加载更多时，将历史消息添加到开头
          messages.value.unshift(...historyMessages)
        } else {
          // 初始加载，直接设置消息列表
          messages.value = historyMessages
        }
        // 更新游标
        lastCreateTime.value = chatHistories[chatHistories.length - 1]?.createTime
        // 检查是否还有更多历史
        hasMoreHistory.value = chatHistories.length === 10
      } else {
        hasMoreHistory.value = false
      }
      historyLoaded.value = true
    }
  } catch (error) {
    console.error('加载对话历史失败：', error)
    antMessage.error('加载对话历史失败')
  } finally {
    loadingHistory.value = false
  }
}

// 加载更多历史消息
const loadMoreHistory = async () => {
  await loadChatHistory(true)
}

// 获取应用信息
const fetchAppInfo = async () => {
  const id = route.params.id as string
  if (!id) {
    antMessage.error('应用ID不存在')
    router.push('/')
    return
  }

  appId.value = id

  try {
    const res = await getAppVoById({ id: id as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data

      // 先加载对话历史
      await loadChatHistory()
      // 如果有至少2条对话记录，展示对应的网站
      if (messages.value.length >= 2) {
        updatePreview()
      }
      // 检查是否需要自动发送初始提示词
      // 只有在是自己的应用且没有对话历史时才自动发送
      if (
        appInfo.value.initPrompt &&
        isOwner.value &&
        messages.value.length === 0 &&
        historyLoaded.value
      ) {
        await sendInitialMessage(appInfo.value.initPrompt)
      }
    } else {
      antMessage.error('获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取应用信息失败：', error)
    antMessage.error('获取应用信息失败')
    router.push('/')
  }
}

// 发送初始消息
const sendInitialMessage = async (prompt: string) => {
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: prompt,
  })

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
    streaming: true,
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  isGenerating.value = true
  await generateCode(prompt, aiMessageIndex)
}

// 输入框键盘事件：Enter 发送，Shift + Enter 换行，兼容中文输入法
const handleComposerKeydown = (event: KeyboardEvent) => {
  if (event.key !== 'Enter' || event.shiftKey) return
  if (event.isComposing || event.keyCode === 229) return
  event.preventDefault()
  sendMessage()
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || isGenerating.value || !isOwner.value) {
    return
  }

  let message = userInput.value.trim()
  // 如果有选中的元素，将元素信息添加到提示词中
  if (selectedElementInfo.value) {
    let elementContext = `\n\n选中元素信息：`
    if (selectedElementInfo.value.pagePath) {
      elementContext += `\n- 页面路径: ${selectedElementInfo.value.pagePath}`
    }
    elementContext += `\n- 标签: ${selectedElementInfo.value.tagName.toLowerCase()}\n- 选择器: ${selectedElementInfo.value.selector}`
    if (selectedElementInfo.value.textContent) {
      elementContext += `\n- 当前内容: ${selectedElementInfo.value.textContent.substring(0, 100)}`
    }
    message += elementContext
  }
  userInput.value = ''
  // 添加用户消息（包含元素信息）
  messages.value.push({
    type: 'user',
    content: message,
  })

  // 发送消息后，清除选中元素并退出编辑模式
  if (selectedElementInfo.value) {
    clearSelectedElement()
    if (isEditMode.value) {
      toggleEditMode()
    }
  }

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
    streaming: true,
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  isGenerating.value = true
  await generateCode(message, aiMessageIndex)
}

// 生成代码 - 使用 EventSource 处理流式响应
const generateCode = async (userMessage: string, aiMessageIndex: number) => {
  let eventSource: EventSource | null = null
  let streamCompleted = false
  let fullContent = ''
  let flushTimer: number | undefined
  let lastFlush = 0

  /**
   * 流式生成时降低刷新频率，并使用纯文本渲染，
   * 避免每个 token 都重新解析 Markdown / 高亮整段代码导致页面卡死。
   */
  const flushStreamContent = () => {
    if (flushTimer !== undefined) {
      window.clearTimeout(flushTimer)
      flushTimer = undefined
    }
    lastFlush = performance.now()
    const target = messages.value[aiMessageIndex]
    if (!target) return
    target.content = fullContent
    target.loading = false
    scrollToBottom()
  }

  const scheduleContentFlush = () => {
    if (flushTimer !== undefined) return
    const delay = Math.max(0, 120 - (performance.now() - lastFlush))
    flushTimer = window.setTimeout(flushStreamContent, delay)
  }

  const failStream = (error: unknown) => {
    if (flushTimer !== undefined) {
      window.clearTimeout(flushTimer)
      flushTimer = undefined
    }
    handleError(error, aiMessageIndex)
  }

  try {
    // 获取 axios 配置的 baseURL
    const baseURL = request.defaults.baseURL || API_BASE_URL

    // 构建URL参数
    const params = new URLSearchParams({
      appId: appId.value || '',
      message: userMessage,
    })

    const url = `${baseURL}/app/chat/gen/code?${params}`

    // 创建 EventSource 连接
    eventSource = new EventSource(url, {
      withCredentials: true,
    })

    let fullContent = ''

    // 处理接收到的消息
    eventSource.onmessage = function (event) {
      if (streamCompleted) return

      try {
        // 解析JSON包装的数据
        const parsed = JSON.parse(event.data)
        const content = parsed.d

        // 拼接内容，按固定频率刷新界面
        if (content !== undefined && content !== null) {
          fullContent += content
          if (performance.now() - lastFlush > 120) {
            flushStreamContent()
          } else {
            scheduleContentFlush()
          }
        }
      } catch (error) {
        console.error('解析消息失败:', error)
        failStream(error)
      }
    }

    // 处理done事件
    eventSource.addEventListener('done', function () {
      if (streamCompleted) return

      streamCompleted = true
      isGenerating.value = false
      eventSource?.close()

      // 结束后一次性渲染完整内容
      flushStreamContent()
      const target = messages.value[aiMessageIndex]
      if (target) {
        target.streaming = false
        target.loading = false
      }

      // 延迟更新预览，确保后端已完成处理
      setTimeout(async () => {
        await fetchAppInfo()
        updatePreview()
      }, 1000)
    })

    // 处理business-error事件（后端限流等错误）
    eventSource.addEventListener('business-error', function (event: MessageEvent) {
      if (streamCompleted) return

      try {
        const errorData = JSON.parse(event.data)
        console.error('SSE业务错误事件:', errorData)

        // 显示具体的错误信息
        const errorMessage = errorData.message || '生成过程中出现错误'
        messages.value[aiMessageIndex].content = `❌ ${errorMessage}`
        messages.value[aiMessageIndex].loading = false
        messages.value[aiMessageIndex].streaming = false
        antMessage.error(errorMessage)

        streamCompleted = true
        isGenerating.value = false
        eventSource?.close()
      } catch (parseError) {
        console.error('解析错误事件失败:', parseError, '原始数据:', event.data)
        failStream(new Error('服务器返回错误'))
      }
    })

    // 处理错误
    eventSource.onerror = function () {
      if (streamCompleted || !isGenerating.value) return
      // 检查是否是正常的连接关闭
      if (eventSource?.readyState === EventSource.CONNECTING) {
        streamCompleted = true
        isGenerating.value = false
        eventSource?.close()

        flushStreamContent()
        const target = messages.value[aiMessageIndex]
        if (target) {
          target.streaming = false
          target.loading = false
        }

        setTimeout(async () => {
          await fetchAppInfo()
          updatePreview()
        }, 1000)
      } else {
        failStream(new Error('SSE连接错误'))
      }
    }
  } catch (error) {
    console.error('创建 EventSource 失败：', error)
    failStream(error)
  }
}

// 错误处理函数
const handleError = (error: unknown, aiMessageIndex: number) => {
  console.error('生成代码失败：', error)
  messages.value[aiMessageIndex].content = '抱歉，生成过程中出现了错误，请重试。'
  messages.value[aiMessageIndex].loading = false
  messages.value[aiMessageIndex].streaming = false
  antMessage.error('生成失败，请重试')
  isGenerating.value = false
}

// 更新预览
const updatePreview = () => {
  if (appId.value) {
    const codeGenType = appInfo.value?.codeGenType || CodeGenTypeEnum.HTML
    const newPreviewUrl = getStaticPreviewUrl(codeGenType, appId.value)
    previewUrl.value = newPreviewUrl
    previewReady.value = true
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 下载代码
const downloadCode = async () => {
  if (!appId.value) {
    antMessage.error('应用ID不存在')
    return
  }
  downloading.value = true
  try {
    const API_BASE_URL = request.defaults.baseURL || ''
    const url = `${API_BASE_URL}/app/download/${appId.value}`
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include',
    })
    if (!response.ok) {
      throw new Error(`下载失败: ${response.status}`)
    }
    // 获取文件名
    const contentDisposition = response.headers.get('Content-Disposition')
    const fileName = contentDisposition?.match(/filename="(.+)"/)?.[1] || `app-${appId.value}.zip`
    // 下载文件
    const blob = await response.blob()
    const downloadUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = fileName
    link.click()
    // 清理
    URL.revokeObjectURL(downloadUrl)
    antMessage.success('代码下载成功')
  } catch (error) {
    console.error('下载失败：', error)
    antMessage.error('下载失败，请重试')
  } finally {
    downloading.value = false
  }
}

// 部署应用
const deployApp = async () => {
  if (!appId.value) {
    antMessage.error('应用ID不存在')
    return
  }

  deploying.value = true
  try {
    const res = await deployAppApi({
      appId: appId.value as unknown as number,
    })

    if (res.data.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      deployModalVisible.value = true
      antMessage.success('部署成功')
    } else {
      antMessage.error('部署失败：' + res.data.message)
    }
  } catch (error) {
    console.error('部署失败：', error)
    antMessage.error('部署失败，请重试')
  } finally {
    deploying.value = false
  }
}

// 在新窗口打开预览
const openInNewTab = () => {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  }
}

// 打开部署的网站
const openDeployedSite = () => {
  if (deployUrl.value) {
    window.open(deployUrl.value, '_blank')
  }
}

// iframe加载完成
const onIframeLoad = () => {
  previewReady.value = true
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (iframe) {
    visualEditor.init(iframe)
    visualEditor.onIframeLoad()
  }
}

// 编辑应用
const editApp = () => {
  if (appInfo.value?.id) {
    router.push(`/app/edit/${appInfo.value.id}`)
  }
}

// 删除应用
const deleteApp = async () => {
  if (!appInfo.value?.id) return

  try {
    const res = await deleteAppApi({ id: appInfo.value.id })
    if (res.data.code === 0) {
      antMessage.success('删除成功')
      appDetailVisible.value = false
      router.push('/')
    } else {
      antMessage.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    antMessage.error('删除失败')
  }
}

// 可视化编辑相关函数
const toggleEditMode = () => {
  // 检查 iframe 是否已经加载
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (!iframe) {
    antMessage.warning('请等待页面加载完成')
    return
  }
  // 确保 visualEditor 已初始化
  if (!previewReady.value) {
    antMessage.warning('请等待页面加载完成')
    return
  }
  const newEditMode = visualEditor.toggleEditMode()
  isEditMode.value = newEditMode
}

const clearSelectedElement = () => {
  selectedElementInfo.value = null
  visualEditor.clearSelection()
}

const getInputPlaceholder = () => {
  if (selectedElementInfo.value) {
    return `正在编辑 ${selectedElementInfo.value.tagName.toLowerCase()} 元素，描述您想要的修改…`
  }
  return '描述你想生成或修改的网站，越详细效果越好…'
}

// 页面加载时获取应用信息
onMounted(() => {
  fetchAppInfo()

  // 监听 iframe 消息
  window.addEventListener('message', (event) => {
    visualEditor.handleIframeMessage(event)
  })
})

// 清理资源
onUnmounted(() => {
  // EventSource 会在组件卸载时自动清理
})
</script>

<style scoped>
#appChatPage {
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: calc(100vh - var(--header-height));
  padding: 16px 20px 20px;
  box-sizing: border-box;
  overflow: hidden;
  background:
    radial-gradient(900px circle at 8% -20%, rgba(99, 102, 241, 0.09), transparent 55%),
    radial-gradient(800px circle at 100% 0%, rgba(236, 72, 153, 0.05), transparent 50%),
    #f4f5fb;
}

/* -------------------------------------------------------------- workspace bar */
.workspace-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  flex-shrink: 0;
  flex-wrap: wrap;
}

.workspace-bar__left {
  display: flex;
  align-items: center;
  gap: 13px;
  min-width: 0;
}

.back-btn {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.9);
  color: var(--ink-3);
  font-size: 14px;
  cursor: pointer;
  box-shadow: var(--shadow-xs);
  transition:
    background 0.2s ease,
    color 0.2s ease,
    border-color 0.2s ease,
    transform 0.2s ease;
}

.back-btn:hover {
  color: var(--brand-600);
  border-color: var(--brand-200);
  background: #fff;
  transform: translateX(-1px);
}

.app-identity {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 0;
}

.app-avatar {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  flex-shrink: 0;
  border-radius: 13px;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(140deg, var(--brand-500), var(--violet) 70%, #ec4899);
  box-shadow: 0 10px 20px -12px rgba(79, 70, 229, 0.75);
}

.app-identity__text {
  min-width: 0;
}

.app-identity__title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.app-identity__title h1 {
  margin: 0;
  max-width: 420px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--ink);
}

.app-identity__sub {
  margin: 2px 0 0;
  color: var(--muted-2);
  font-size: 11.5px;
  letter-spacing: 0.01em;
}

.code-gen-tag {
  margin: 0;
  color: var(--brand-600);
  background: var(--brand-50);
  border: 1px solid rgba(99, 102, 241, 0.14);
  font-size: 10.5px;
  line-height: 18px;
}

.featured-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 10.5px;
  font-weight: 600;
  color: #92400e;
  background: #fef3c7;
  border: 1px solid rgba(245, 158, 11, 0.22);
}

.live-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 1px 9px;
  border-radius: 999px;
  font-size: 10.5px;
  font-weight: 600;
  color: #047857;
  background: #ecfdf5;
  border: 1px solid rgba(16, 185, 129, 0.22);
}

.live-tag i {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--mint);
  animation: pulse-ring 1.8s infinite;
}

.workspace-bar__actions {
  display: flex;
  align-items: center;
  gap: 9px;
  flex-shrink: 0;
}

.tool-btn {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 11px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.9);
  color: var(--ink-3);
  font-size: 14px;
  cursor: pointer;
  box-shadow: var(--shadow-xs);
  transition:
    color 0.2s ease,
    border-color 0.2s ease,
    background 0.2s ease;
}

.tool-btn:hover {
  color: var(--brand-600);
  border-color: var(--brand-200);
  background: #fff;
}

.tool-btn.is-active {
  color: #fff;
  border-color: transparent;
  background: linear-gradient(135deg, var(--brand-500), var(--violet));
  box-shadow: var(--shadow-brand);
}

.tool-btn--sm {
  width: 32px;
  height: 32px;
  border-radius: 9px;
  font-size: 13px;
}

.soft-btn,
.deploy-btn {
  height: 38px;
  border-radius: 11px;
  font-size: 13px;
}

.soft-btn {
  border-color: var(--line);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-xs);
}

.deploy-btn {
  box-shadow: var(--shadow-brand);
}

/* ----------------------------------------------------------------- workspace */
.workspace {
  display: flex;
  gap: 14px;
  flex: 1;
  min-height: 0;
}

.panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
  border-radius: 22px;
  border: 1px solid rgba(15, 23, 42, 0.07);
  background: rgba(255, 255, 255, 0.92);
  box-shadow:
    0 24px 50px -36px rgba(30, 41, 59, 0.3),
    0 2px 6px -2px rgba(30, 41, 59, 0.05);
}

.chat-panel {
  flex: 0 0 clamp(360px, 36%, 470px);
}

.preview-panel {
  flex: 1;
  min-width: 0;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-shrink: 0;
  height: 62px;
  padding: 0 18px;
  border-bottom: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.7);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.panel-icon {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  flex-shrink: 0;
  font-size: 14px;
  color: var(--brand-600);
  background: linear-gradient(140deg, rgba(99, 102, 241, 0.14), rgba(139, 92, 246, 0.08));
  box-shadow: inset 0 0 0 1px rgba(99, 102, 241, 0.1);
}

.panel-icon--preview {
  color: #0e7490;
  background: linear-gradient(140deg, rgba(6, 182, 212, 0.14), rgba(14, 165, 233, 0.08));
  box-shadow: inset 0 0 0 1px rgba(6, 182, 212, 0.14);
}

.panel-title h2 {
  margin: 0;
  font-size: 14px;
  font-weight: 650;
  color: var(--ink);
  letter-spacing: -0.01em;
}

.panel-title p {
  margin: 1px 0 0;
  font-size: 11px;
  color: var(--muted-2);
}

.panel-count {
  padding: 3px 10px;
  border-radius: 999px;
  background: var(--surface-soft);
  border: 1px solid var(--line);
  color: var(--muted);
  font-size: 10.5px;
  font-family: var(--font-mono);
}

/* ------------------------------------------------------------------- messages */
.messages-container {
  flex: 1;
  min-height: 0;
  padding: 18px 18px 10px;
  overflow-y: auto;
  scroll-behavior: smooth;
  background:
    radial-gradient(420px circle at 100% 0%, rgba(99, 102, 241, 0.035), transparent 55%),
    linear-gradient(180deg, #fcfcff 0%, #ffffff 100%);
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  margin-bottom: 18px;
}

.message-item--user {
  justify-content: flex-end;
}

.message-item--user .message-stack {
  align-items: flex-end;
}

.message-stack {
  display: flex;
  flex-direction: column;
  gap: 5px;
  max-width: 82%;
  min-width: 0;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 2px;
  color: var(--muted-2);
  font-size: 10.5px;
  line-height: 1;
}

.message-meta span:first-child {
  font-weight: 600;
  color: var(--ink-3);
  font-size: 11px;
}

.message-meta time {
  font-family: var(--font-mono);
  font-size: 9.5px;
  opacity: 0.7;
}

.ai-badge {
  display: inline-flex;
  align-items: center;
  height: 14px;
  padding: 0 5px;
  border-radius: 4px;
  font-size: 8.5px;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: #fff;
  background: linear-gradient(120deg, var(--brand-500), var(--violet));
}

.message-avatar {
  flex-shrink: 0;
  margin-top: 18px;
  border-radius: 11px !important;
}

.message-avatar--user {
  background: linear-gradient(135deg, var(--brand-400), var(--violet));
  font-weight: 600;
}

.message-avatar--ai {
  padding: 2px;
  background: #fff;
  box-shadow:
    0 0 0 1px rgba(99, 102, 241, 0.14),
    0 4px 10px -4px rgba(30, 41, 59, 0.25);
}

.message-avatar--ai :deep(img) {
  border-radius: 9px;
}

.user-bubble {
  padding: 10px 14px;
  border-radius: 16px 16px 4px 16px;
  color: #fff;
  font-size: 13px;
  line-height: 1.7;
  word-break: break-word;
  white-space: pre-wrap;
  background: linear-gradient(135deg, #5b5ef0, #7c5cf6);
  box-shadow: 0 12px 24px -16px rgba(79, 70, 229, 0.85);
}

.ai-bubble {
  padding: 10px 14px;
  border-radius: 4px 16px 16px 16px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--ink-2);
  font-size: 13px;
  line-height: 1.72;
  word-break: break-word;
  box-shadow: var(--shadow-xs);
}

.ai-bubble :deep(.markdown-content) {
  font-size: 13px;
  line-height: 1.72;
  color: var(--ink-2);
}

.ai-bubble :deep(.markdown-content p:first-child) {
  margin-top: 0;
}

.ai-bubble :deep(.markdown-content p:last-child) {
  margin-bottom: 0;
}

.thinking {
  display: flex;
  align-items: center;
  gap: 9px;
  color: var(--muted);
  font-size: 12px;
}

.thinking-dots {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.thinking-dots i {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--brand-400);
  animation: thinking-dot 1.2s infinite;
}

.thinking-dots i:nth-child(2) {
  animation-delay: 0.15s;
}

.thinking-dots i:nth-child(3) {
  animation-delay: 0.3s;
}

.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  min-height: 100%;
  padding: 40px 24px;
  color: var(--muted);
}

.chat-empty__icon {
  display: grid;
  place-items: center;
  width: 54px;
  height: 54px;
  margin-bottom: 14px;
  border-radius: 17px;
  font-size: 22px;
  color: var(--brand-600);
  background: linear-gradient(140deg, rgba(99, 102, 241, 0.14), rgba(236, 72, 153, 0.08));
  box-shadow: inset 0 0 0 1px rgba(99, 102, 241, 0.1);
}

.chat-empty h3 {
  margin: 0 0 5px;
  font-size: 14px;
  font-weight: 650;
  color: var(--ink);
}

.chat-empty p {
  margin: 0;
  max-width: 290px;
  font-size: 12px;
  line-height: 1.7;
  color: var(--muted-2);
}

/* --------------------------------------------------------------- load more */
.load-more-container {
  display: flex;
  justify-content: center;
  padding: 2px 0 16px;
}

.load-more-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 28px;
  padding: 0 13px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--muted);
  font-family: inherit;
  font-size: 11px;
  cursor: pointer;
  box-shadow: var(--shadow-xs);
  transition: all 0.2s ease;
}

.load-more-btn:hover:not(:disabled) {
  color: var(--brand-600);
  border-color: var(--brand-200);
  background: var(--brand-50);
}

.load-more-btn:disabled {
  cursor: wait;
  opacity: 0.7;
}

/* ---------------------------------------------------------- selected element */
.element-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  flex-shrink: 0;
  margin: 0 16px 10px;
  padding: 11px 13px;
  border-radius: 14px;
  border: 1px solid rgba(99, 102, 241, 0.18);
  background: linear-gradient(135deg, rgba(238, 242, 255, 0.9), rgba(245, 243, 255, 0.9));
  box-shadow: var(--shadow-xs);
}

.element-card__icon {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  border-radius: 9px;
  color: var(--brand-600);
  background: #fff;
  box-shadow: 0 0 0 1px rgba(99, 102, 241, 0.16);
  font-size: 12px;
}

.element-card__body {
  flex: 1;
  min-width: 0;
}

.element-card__head {
  display: flex;
  align-items: center;
  gap: 5px;
  flex-wrap: wrap;
  font-size: 11px;
}

.element-card__head strong {
  color: var(--brand-700);
  font-size: 11.5px;
  font-weight: 650;
}

.element-card__head code,
.element-card__head em {
  font-family: var(--font-mono);
  font-style: normal;
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 4px;
  background: #fff;
  color: var(--brand-600);
  box-shadow: 0 0 0 1px rgba(99, 102, 241, 0.12);
}

.element-card__head em {
  color: #0e7490;
  box-shadow: 0 0 0 1px rgba(6, 182, 212, 0.16);
}

.element-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  margin-top: 5px;
  color: var(--muted);
  font-size: 10.5px;
  line-height: 1.5;
}

.element-selector code {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--ink-3);
}

.element-card__close {
  display: grid;
  place-items: center;
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--muted-2);
  font-size: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.element-card__close:hover {
  color: var(--ink);
  background: rgba(255, 255, 255, 0.8);
}

/* ------------------------------------------------------------------ composer */
.composer-wrap {
  flex-shrink: 0;
  padding: 0 16px 14px;
  background: #fff;
}

.chat-composer {
  padding: 10px 12px 8px;
  border-radius: 16px;
  border: 1px solid var(--line-strong);
  background: var(--surface-soft);
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    background 0.2s ease;
}

.chat-composer:focus-within {
  border-color: var(--brand-300);
  background: #fff;
  box-shadow: 0 0 0 3.5px rgba(99, 102, 241, 0.1);
}

.chat-composer.is-element-mode {
  border-color: rgba(99, 102, 241, 0.35);
  background: #fff;
}

.chat-textarea {
  padding: 0 !important;
  background: transparent !important;
  border: 0 !important;
  box-shadow: none !important;
  resize: none;
  font-size: 13px;
  line-height: 1.65;
  color: var(--ink);
}

.chat-textarea::placeholder {
  color: var(--muted-2);
}

.chat-composer__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 6px;
  padding-top: 7px;
  border-top: 1px dashed var(--line);
}

.composer-hint {
  color: var(--muted-2);
  font-size: 10px;
  letter-spacing: 0.01em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.send-btn {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border: 0;
  border-radius: 10px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  background: linear-gradient(135deg, var(--brand-500), var(--violet));
  box-shadow: 0 8px 18px -10px rgba(79, 70, 229, 0.9);
  transition:
    transform 0.2s ease,
    filter 0.2s ease,
    opacity 0.2s ease;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.06);
}

.send-btn:disabled {
  cursor: not-allowed;
  opacity: 0.45;
  box-shadow: none;
}

.composer-note {
  margin: 7px 0 0;
  text-align: center;
  color: var(--muted-2);
  font-size: 10.5px;
}

/* ------------------------------------------------------------------- preview */
.preview-head {
  padding-right: 14px;
}

.preview-actions {
  display: flex;
  align-items: center;
  gap: 7px;
}

.device-switch {
  display: inline-flex;
  gap: 2px;
  padding: 3px;
  border-radius: 9px;
  background: var(--surface-soft);
  border: 1px solid var(--line);
}

.device-switch button {
  display: grid;
  place-items: center;
  width: 26px;
  height: 24px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--muted-2);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.device-switch button:hover {
  color: var(--ink-3);
}

.device-switch button.is-active {
  color: var(--brand-600);
  background: #fff;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.12);
}

.preview-body {
  flex: 1;
  min-height: 0;
  padding: 14px;
  background:
    radial-gradient(600px circle at 50% 0%, rgba(99, 102, 241, 0.05), transparent 60%),
    #f7f8fc;
}

.browser {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  border-radius: 15px;
  border: 1px solid var(--line);
  background: #fff;
  box-shadow: 0 12px 30px -22px rgba(30, 41, 59, 0.4);
  transition:
    max-width 0.35s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.35s ease;
}

.browser--desktop {
  width: 100%;
}

.browser--mobile {
  max-width: 390px;
  margin: 0 auto;
  border-radius: 22px;
  box-shadow:
    0 20px 50px -28px rgba(30, 41, 59, 0.5),
    0 0 0 6px rgba(255, 255, 255, 0.6);
}

.browser-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 38px;
  flex-shrink: 0;
  padding: 0 13px;
  border-bottom: 1px solid var(--line);
  background: linear-gradient(180deg, #fbfcfe, #f6f8fc);
}

.browser-dots {
  display: inline-flex;
  gap: 5px;
  flex-shrink: 0;
}

.browser-dots i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e2e8f0;
}

.browser-dots i:nth-child(1) {
  background: #fca5a5;
}

.browser-dots i:nth-child(2) {
  background: #fcd34d;
}

.browser-dots i:nth-child(3) {
  background: #86efac;
}

.browser-url {
  display: flex;
  align-items: center;
  gap: 5px;
  flex: 1;
  min-width: 0;
  height: 24px;
  padding: 0 10px;
  border-radius: 7px;
  background: #fff;
  border: 1px solid var(--line);
  color: var(--muted);
  font-family: var(--font-mono);
  font-size: 10.5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.browser-url .anticon {
  font-size: 9px;
  color: var(--mint);
  flex-shrink: 0;
}

.preview-content {
  position: relative;
  flex: 1;
  min-height: 0;
  background: #fff;
  overflow: hidden;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
  background: #fff;
}

.preview-placeholder,
.preview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 32px;
  text-align: center;
}

.placeholder-visual {
  display: grid;
  place-items: center;
  width: 66px;
  height: 66px;
  margin-bottom: 18px;
  border-radius: 20px;
  font-size: 27px;
  color: var(--brand-500);
  background:
    radial-gradient(circle at 30% 25%, rgba(99, 102, 241, 0.18), transparent 70%),
    #f5f6ff;
  box-shadow: inset 0 0 0 1px rgba(99, 102, 241, 0.12);
}

.preview-placeholder h3,
.preview-loading h3 {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 650;
  color: var(--ink);
}

.preview-placeholder p,
.preview-loading p {
  margin: 0;
  color: var(--muted-2);
  font-size: 12px;
}

.preview-loading :deep(.ant-spin) {
  margin-bottom: 20px;
}

.preview-loading :deep(.ant-spin-dot-item) {
  background: var(--brand-500);
}

/* ------------------------------------------------------------------ responsive */
@media (max-width: 1180px) {
  .chat-panel {
    flex-basis: clamp(330px, 40%, 430px);
  }
}

@media (max-width: 980px) {
  #appChatPage {
    height: auto;
    min-height: calc(100vh - var(--header-height));
    padding: 14px 14px 20px;
    overflow: visible;
  }

  .workspace {
    flex-direction: column;
    flex: none;
    overflow: visible;
  }

  .chat-panel {
    flex: none;
    height: 480px;
  }

  .preview-panel {
    flex: none;
    height: 560px;
  }
}

@media (max-width: 640px) {
  #appChatPage {
    padding: 12px 10px 16px;
  }

  .workspace-bar {
    flex-wrap: nowrap;
    gap: 8px;
  }

  .workspace-bar__left {
    gap: 8px;
    flex: 1;
    overflow: hidden;
  }

  .back-btn {
    width: 34px;
    height: 34px;
    border-radius: 10px;
    font-size: 13px;
    flex-shrink: 0;
  }

  .app-identity {
    gap: 8px;
    overflow: hidden;
  }

  .app-avatar {
    width: 36px;
    height: 36px;
    border-radius: 11px;
    font-size: 16px;
  }

  .app-identity__title {
    flex-wrap: nowrap;
    gap: 6px;
  }

  .app-identity__title h1 {
    max-width: clamp(64px, 26vw, 108px);
    font-size: 14px;
  }

  .app-identity__sub {
    display: none;
  }

  .code-gen-tag,
  .featured-tag,
  .live-tag {
    display: none;
  }

  .workspace-bar__actions {
    gap: 6px;
    flex-shrink: 0;
  }

  .btn-label {
    display: none;
  }

  .tool-btn,
  .soft-btn,
  .deploy-btn {
    width: 34px;
    height: 34px;
    padding: 0;
    border-radius: 10px;
  }

  .chat-panel {
    height: 520px;
  }

  .preview-panel {
    height: 520px;
  }

  .message-stack {
    max-width: 88%;
  }

  .preview-body {
    padding: 10px;
  }

  .browser--mobile {
    max-width: 100%;
  }
}
</style>
