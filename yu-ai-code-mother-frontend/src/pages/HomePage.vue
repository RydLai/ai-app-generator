<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'
import {
  AppstoreOutlined,
  ArrowUpOutlined,
  BulbOutlined,
  GlobalOutlined,
  LoadingOutlined,
  LoginOutlined,
  PictureOutlined,
  ReadOutlined,
  RocketOutlined,
  ShopOutlined,
  StarFilled,
  ThunderboltOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)
const composerRef = ref<HTMLElement>()

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const loadingMyApps = ref(false)
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const loadingFeaturedApps = ref(false)
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 快捷灵感：把长提示词收在数据里，模板更干净，也方便日后扩展
const promptIdeas = [
  {
    label: '个人博客',
    icon: ReadOutlined,
    prompt:
      '创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能、评论系统和个人简介页面。采用简洁的设计风格，支持响应式布局，文章支持Markdown格式，首页展示最新文章和热门推荐。',
  },
  {
    label: '企业官网',
    icon: GlobalOutlined,
    prompt:
      '设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。',
  },
  {
    label: '在线商城',
    icon: ShopOutlined,
    prompt:
      '构建一个功能完整的在线商城，包含商品展示、购物车、用户注册登录、订单管理、支付结算等功能。设计现代化的商品卡片布局，支持商品搜索筛选、用户评价、优惠券系统和会员积分功能。',
  },
  {
    label: '作品集',
    icon: PictureOutlined,
    prompt:
      '制作一个精美的作品展示网站，适合设计师、摄影师、艺术家等创作者。包含作品画廊、项目详情页、个人简历、联系方式等模块。采用瀑布流或网格布局展示作品，支持图片放大预览和作品分类筛选。',
  },
]

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
  composerRef.value?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

// 创建应用
const createApp = async () => {
  if (creating.value) return
  if (!userPrompt.value.trim()) {
    message.warning('请先描述你想要创建的应用')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功，正在为你生成…')
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    myApps.value = []
    return
  }

  loadingMyApps.value = true
  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  } finally {
    loadingMyApps.value = false
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  loadingFeaturedApps.value = true
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  } finally {
    loadingFeaturedApps.value = false
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 开始创作：把光标带回输入框
const startCreating = () => {
  composerRef.value?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  const textarea = composerRef.value?.querySelector('textarea')
  textarea?.focus()
}

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
})

// 清理鼠标光效的监听
let removeMouseEffect: (() => void) | undefined
onMounted(() => {
  const handleMouseMove = (e: MouseEvent) => {
    const { clientX, clientY } = e
    const { innerWidth, innerHeight } = window
    document.documentElement.style.setProperty('--mouse-x', `${(clientX / innerWidth) * 100}%`)
    document.documentElement.style.setProperty('--mouse-y', `${(clientY / innerHeight) * 100}%`)
  }

  document.addEventListener('mousemove', handleMouseMove, { passive: true })
  removeMouseEffect = () => document.removeEventListener('mousemove', handleMouseMove)
})

onUnmounted(() => {
  removeMouseEffect?.()
})
</script>

<template>
  <div id="homePage">
    <!-- 氛围光斑 -->
    <div class="backdrop" aria-hidden="true">
      <span class="orb orb--indigo"></span>
      <span class="orb orb--pink"></span>
      <span class="orb orb--mint"></span>
      <span class="backdrop-grid"></span>
      <span class="backdrop-glow"></span>
    </div>

    <div class="home-container">
      <!-- 英雄区 -->
      <section class="hero">
        <div class="hero-badge fade-up">
          <span class="badge-pulse" aria-hidden="true"></span>
          AI 驱动 · 零代码 · 开箱即用
        </div>

        <h1 class="hero-title fade-up delay-1">
          把一句想法
          <span class="hero-title-accent">变成完整应用</span>
        </h1>

        <p class="hero-description fade-up delay-2">
          描述你想要的网站，AI 自动完成界面设计、代码编写与一键部署。<br class="only-desktop" />
          从灵感到上线，只需几分钟。
        </p>

        <div class="hero-flow fade-up delay-3" aria-label="使用流程">
          <span class="flow-step"><i>01</i>描述想法</span>
          <span class="flow-arrow" aria-hidden="true">→</span>
          <span class="flow-step"><i>02</i>AI 生成</span>
          <span class="flow-arrow" aria-hidden="true">→</span>
          <span class="flow-step"><i>03</i>一键部署</span>
        </div>
      </section>

      <!-- 创作输入区 -->
      <section ref="composerRef" class="composer fade-up delay-2">
        <div class="composer-head">
          <span class="composer-label">
            <ThunderboltOutlined />
            描述你的应用
          </span>
          <span class="composer-count">{{ userPrompt.length }} / 1000</span>
        </div>

        <a-textarea
          v-model:value="userPrompt"
          class="composer-input"
          :bordered="false"
          :maxlength="1000"
          :auto-size="{ minRows: 3, maxRows: 8 }"
          placeholder="例如：帮我做一个个人博客，支持深色模式、文章分类和评论…"
          @keydown.ctrl.enter.prevent="createApp"
          @keydown.meta.enter.prevent="createApp"
        />

        <div class="composer-footer">
          <div class="composer-tip">
            <BulbOutlined />
            <span>补充页面、配色和功能细节，生成效果会更贴近预期</span>
          </div>

          <div class="composer-actions">
            <span class="composer-kbd" aria-hidden="true">
              <kbd>Ctrl</kbd>
              <b>+</b>
              <kbd>Enter</kbd>
            </span>
            <button
              type="button"
              class="generate-btn"
              :class="{ 'is-loading': creating }"
              :disabled="creating"
              @click="createApp"
            >
              <span>{{ creating ? '正在创建…' : '立即生成' }}</span>
              <LoadingOutlined v-if="creating" />
              <ArrowUpOutlined v-else />
            </button>
          </div>
        </div>

        <div class="composer-ideas">
          <span class="ideas-label">灵感</span>
          <button
            v-for="idea in promptIdeas"
            :key="idea.label"
            type="button"
            class="idea-chip"
            @click="setPrompt(idea.prompt)"
          >
            <component :is="idea.icon" />
            {{ idea.label }}
          </button>
        </div>
      </section>

      <!-- 我的作品 -->
      <section class="section">
        <header class="section-head">
          <div class="section-heading">
            <span class="section-icon">
              <AppstoreOutlined />
            </span>
            <div>
              <h2>我的作品</h2>
              <p>从一句描述开始，持续迭代属于你的应用</p>
            </div>
          </div>
          <span v-if="myAppsPage.total" class="section-count">
            共 {{ myAppsPage.total }} 个应用
          </span>
        </header>

        <div v-if="loadingMyApps" class="app-grid">
          <div v-for="item in 3" :key="item" class="skeleton-card">
            <a-skeleton active :paragraph="{ rows: 2 }" />
          </div>
        </div>

        <div v-else-if="myApps.length" class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>

        <div v-else class="empty-panel">
          <span class="empty-icon">
            <RocketOutlined />
          </span>
          <h3>{{ loginUserStore.loginUser.id ? '还没有作品，灵感就在上方' : '登录后查看你的作品' }}</h3>
          <p v-if="loginUserStore.loginUser.id">
            输入一句描述，AI 会为你生成第一个完整应用。
          </p>
          <p v-else>登录账号后，所有生成过的应用都会出现在这里。</p>
          <a-button v-if="!loginUserStore.loginUser.id" type="primary" @click="router.push('/user/login')">
            <template #icon><LoginOutlined /></template>
            去登录
          </a-button>
          <a-button v-else @click="startCreating">
            开始创作
            <template #icon><ArrowUpOutlined /></template>
          </a-button>
        </div>

        <div v-if="myAppsPage.total > myAppsPage.pageSize" class="pagination-wrapper">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个应用`"
            @change="loadMyApps"
          />
        </div>
      </section>

      <!-- 精选案例 -->
      <section class="section section--featured">
        <header class="section-head">
          <div class="section-heading">
            <span class="section-icon section-icon--amber">
              <StarFilled />
            </span>
            <div>
              <h2>精选案例</h2>
              <p>看看其他人用一句描述，创造出了怎样的作品</p>
            </div>
          </div>
          <span v-if="featuredAppsPage.total" class="section-count">
            {{ featuredAppsPage.total }} 个案例
          </span>
        </header>

        <div v-if="loadingFeaturedApps" class="app-grid">
          <div v-for="item in 3" :key="item" class="skeleton-card">
            <a-skeleton active :paragraph="{ rows: 2 }" />
          </div>
        </div>

        <div v-else-if="featuredApps.length" class="app-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>

        <div v-else class="empty-panel empty-panel--compact">
          <span class="empty-icon empty-icon--amber">
            <StarFilled />
          </span>
          <h3>精选案例正在准备中</h3>
          <p>稍后回来，看看社区里的优秀作品。</p>
        </div>

        <div
          v-if="featuredAppsPage.total > featuredAppsPage.pageSize"
          class="pagination-wrapper"
        >
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个案例`"
            @change="loadFeaturedApps"
          />
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  position: relative;
  isolation: isolate;
  width: 100%;
  overflow: hidden;
  background:
    linear-gradient(180deg, #f7f8fd 0%, #f5f6fc 42%, #f6f7fc 100%);
}

/* ------------------------------------------------------------------ ambience */
.backdrop {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(8px);
  opacity: 0.7;
}

.orb--indigo {
  top: -180px;
  left: 50%;
  width: 680px;
  height: 520px;
  margin-left: -340px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.28), transparent 66%);
  animation: float-slow 14s ease-in-out infinite;
}

.orb--pink {
  top: 40px;
  right: -160px;
  width: 480px;
  height: 480px;
  background: radial-gradient(circle, rgba(236, 72, 153, 0.16), transparent 68%);
  animation: float-slow 18s ease-in-out infinite reverse;
}

.orb--mint {
  top: 420px;
  left: -180px;
  width: 440px;
  height: 440px;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.13), transparent 68%);
  animation: float-slow 16s ease-in-out infinite;
}

.backdrop-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(79, 70, 229, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(79, 70, 229, 0.045) 1px, transparent 1px);
  background-size: 56px 56px;
  mask-image: radial-gradient(ellipse 90% 55% at 50% 0%, #000 20%, transparent 78%);
  -webkit-mask-image: radial-gradient(ellipse 90% 55% at 50% 0%, #000 20%, transparent 78%);
}

.backdrop-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    560px circle at var(--mouse-x, 50%) var(--mouse-y, 28%),
    rgba(99, 102, 241, 0.07),
    transparent 70%
  );
  transition: background 0.2s ease;
}

.home-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1160px;
  margin: 0 auto;
  padding: 72px 24px 88px;
  box-sizing: border-box;
}

/* ---------------------------------------------------------------------- hero */
.hero {
  max-width: 860px;
  margin: 0 auto;
  text-align: center;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 32px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid rgba(99, 102, 241, 0.16);
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(10px);
  color: var(--ink-3);
  font-size: 12px;
  font-weight: 550;
  letter-spacing: 0.06em;
  box-shadow: var(--shadow-xs);
}

.badge-pulse {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--mint);
  animation: pulse-ring 2.2s infinite;
}

.hero-title {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  margin: 26px 0 0;
  font-size: clamp(38px, 6vw, 64px);
  font-weight: 800;
  line-height: 1.08;
  letter-spacing: -0.04em;
  color: var(--ink);
}

.hero-title-accent {
  background: linear-gradient(105deg, #4f46e5 0%, #8b5cf6 42%, #ec4899 82%, #f59e0b 105%);
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  animation: shimmer 7s linear infinite;
  padding-bottom: 6px;
}

.hero-description {
  max-width: 620px;
  margin: 20px auto 0;
  color: var(--muted);
  font-size: 16.5px;
  line-height: 1.75;
}

.only-desktop {
  display: inline;
}

.hero-flow {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  margin-top: 26px;
  flex-wrap: wrap;
  justify-content: center;
}

.flow-step {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: var(--ink-3);
  font-size: 12.5px;
  font-weight: 500;
}

.flow-step i {
  display: grid;
  place-items: center;
  width: 22px;
  height: 22px;
  border-radius: 7px;
  font-style: normal;
  font-family: var(--font-mono);
  font-size: 9.5px;
  font-weight: 700;
  color: var(--brand-600);
  background: rgba(99, 102, 241, 0.1);
}

.flow-arrow {
  color: var(--muted-2);
  font-size: 12px;
  opacity: 0.6;
}

/* ------------------------------------------------------------------ composer */
.composer {
  position: relative;
  max-width: 860px;
  margin: 42px auto 0;
  padding: 20px 22px 16px;
  border-radius: var(--radius-2xl);
  border: 1px solid rgba(99, 102, 241, 0.14);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(18px) saturate(140%);
  box-shadow:
    var(--shadow-lg),
    0 0 0 1px rgba(255, 255, 255, 0.6) inset;
  transition:
    border-color 0.3s ease,
    box-shadow 0.3s ease;
}

.composer::before {
  content: '';
  position: absolute;
  top: -1px;
  left: 12%;
  right: 12%;
  height: 2px;
  border-radius: 999px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(99, 102, 241, 0.55),
    rgba(236, 72, 153, 0.45),
    transparent
  );
  opacity: 0.9;
}

.composer:focus-within {
  border-color: rgba(99, 102, 241, 0.32);
  box-shadow:
    0 34px 70px -34px rgba(49, 46, 129, 0.38),
    0 0 0 4px rgba(99, 102, 241, 0.08);
}

.composer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 2px 8px;
}

.composer-label {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: var(--brand-600);
  font-size: 12px;
  font-weight: 650;
  letter-spacing: 0.04em;
}

.composer-label .anticon {
  font-size: 13px;
}

.composer-count {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--muted-2);
}

.composer-input {
  padding: 4px 2px !important;
  background: transparent !important;
  border: 0 !important;
  box-shadow: none !important;
  font-size: 16px;
  line-height: 1.75;
  color: var(--ink);
  resize: none;
}

.composer-input::placeholder {
  color: var(--muted-2);
  font-size: 15.5px;
}

.composer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 10px;
  padding-top: 14px;
  border-top: 1px dashed var(--line);
  flex-wrap: wrap;
}

.composer-tip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: var(--muted-2);
  font-size: 12px;
  min-width: 0;
}

.composer-tip .anticon {
  color: var(--amber);
  font-size: 13px;
  flex-shrink: 0;
}

.composer-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.composer-kbd {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--muted-2);
  font-size: 10.5px;
}

.composer-kbd kbd {
  display: inline-grid;
  place-items: center;
  min-width: 22px;
  height: 20px;
  padding: 0 5px;
  border-radius: 5px;
  border: 1px solid var(--line-strong);
  background: var(--surface-soft);
  box-shadow: 0 1px 0 var(--line-strong);
  font-family: var(--font-sans);
  font-size: 10px;
  color: var(--ink-3);
}

.composer-kbd b {
  font-weight: 400;
  opacity: 0.6;
}

.generate-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 42px;
  padding: 0 20px;
  border: 0;
  border-radius: 999px;
  color: #fff;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 650;
  letter-spacing: 0.01em;
  background: linear-gradient(135deg, var(--brand-500), var(--violet) 70%, #a855f7);
  box-shadow: 0 14px 26px -12px rgba(79, 70, 229, 0.8);
  cursor: pointer;
  transition:
    transform 0.22s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.22s ease,
    filter 0.22s ease;
}

.generate-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.05);
  box-shadow: 0 18px 30px -12px rgba(79, 70, 229, 0.9);
}

.generate-btn:active:not(:disabled) {
  transform: translateY(0);
}

.generate-btn:disabled {
  cursor: not-allowed;
  opacity: 0.75;
}

.generate-btn .anticon {
  font-size: 13px;
}

.generate-btn.is-loading .anticon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.composer-ideas {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
}

.ideas-label {
  font-size: 11px;
  font-weight: 650;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--muted-2);
  margin-right: 2px;
}

.idea-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 28px;
  padding: 0 11px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: var(--surface-soft);
  color: var(--ink-3);
  font-family: inherit;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition:
    background 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease,
    transform 0.2s ease;
}

.idea-chip .anticon {
  font-size: 12px;
  color: var(--muted);
  transition: color 0.2s ease;
}

.idea-chip:hover {
  color: var(--brand-600);
  border-color: var(--brand-200);
  background: var(--brand-50);
  transform: translateY(-1px);
}

.idea-chip:hover .anticon {
  color: var(--brand-500);
}

/* ------------------------------------------------------------------- sections */
.section {
  margin-top: 72px;
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 13px;
}

.section-icon {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 14px;
  color: var(--brand-600);
  background: linear-gradient(140deg, rgba(99, 102, 241, 0.14), rgba(139, 92, 246, 0.08));
  box-shadow: inset 0 0 0 1px rgba(99, 102, 241, 0.1);
  font-size: 18px;
  flex-shrink: 0;
}

.section-icon--amber {
  color: #b45309;
  background: linear-gradient(140deg, rgba(245, 158, 11, 0.16), rgba(251, 191, 36, 0.08));
  box-shadow: inset 0 0 0 1px rgba(245, 158, 11, 0.14);
}

.section-heading h2 {
  margin: 0;
  font-size: 23px;
  font-weight: 720;
  letter-spacing: -0.025em;
  color: var(--ink);
}

.section-heading p {
  margin: 1px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.section-count {
  padding: 5px 12px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.75);
  color: var(--muted);
  font-size: 12px;
  font-weight: 500;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 22px;
}

.skeleton-card {
  padding: 18px;
  border-radius: 20px;
  border: 1px solid var(--line);
  background: var(--surface);
}

/* -------------------------------------------------------------------- empty */
.empty-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 48px 24px 52px;
  border-radius: var(--radius-xl);
  border: 1px dashed rgba(99, 102, 241, 0.22);
  background:
    radial-gradient(420px circle at 50% 0%, rgba(99, 102, 241, 0.06), transparent 70%),
    rgba(255, 255, 255, 0.6);
}

.empty-panel--compact {
  padding: 38px 24px 42px;
}

.empty-icon {
  display: grid;
  place-items: center;
  width: 58px;
  height: 58px;
  margin-bottom: 16px;
  border-radius: 18px;
  font-size: 23px;
  color: #fff;
  background: linear-gradient(140deg, var(--brand-500), var(--violet));
  box-shadow: 0 16px 30px -16px rgba(79, 70, 229, 0.7);
}

.empty-icon--amber {
  background: linear-gradient(140deg, #f59e0b, #fbbf24);
  box-shadow: 0 16px 30px -16px rgba(245, 158, 11, 0.65);
}

.empty-panel h3 {
  margin: 0 0 6px;
  font-size: 17px;
  font-weight: 650;
  color: var(--ink);
}

.empty-panel p {
  margin: 0 0 18px;
  max-width: 380px;
  color: var(--muted);
  font-size: 13.5px;
  line-height: 1.7;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* ------------------------------------------------------------------ responsive */
@media (max-width: 768px) {
  .home-container {
    padding: 48px 16px 64px;
  }

  .hero-title {
    margin-top: 20px;
  }

  .hero-description {
    font-size: 15px;
  }

  .only-desktop {
    display: none;
  }

  .composer {
    margin-top: 32px;
    padding: 18px 16px 14px;
    border-radius: 24px;
  }

  .composer-tip {
    display: none;
  }

  .composer-footer {
    justify-content: flex-end;
    gap: 10px;
  }

  .composer-kbd {
    display: none;
  }

  .generate-btn {
    height: 40px;
    padding: 0 18px;
  }

  .section {
    margin-top: 52px;
  }

  .section-heading h2 {
    font-size: 20px;
  }

  .app-grid {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 420px) {
  .hero-flow {
    gap: 8px;
  }

  .flow-step {
    font-size: 11.5px;
  }

  .flow-arrow {
    display: none;
  }

  .flow-step i {
    width: 20px;
    height: 20px;
  }

  .section-count {
    display: none;
  }
}
</style>
