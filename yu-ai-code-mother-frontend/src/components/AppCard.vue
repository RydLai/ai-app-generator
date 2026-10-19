<template>
  <article
    class="app-card"
    :class="{ 'app-card--featured': featured }"
    tabindex="0"
    role="button"
    :aria-label="`打开应用 ${app.appName || '未命名应用'}`"
    @click="handleViewChat"
    @keydown.enter="handleViewChat"
  >
    <div class="app-cover">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" class="cover-image" />

      <div v-else class="cover-fallback" :style="coverStyle">
        <span class="cover-glyph" aria-hidden="true">&lt;/&gt;</span>
        <span class="cover-initial">{{ appInitial }}</span>
      </div>

      <span v-if="isFeatured" class="cover-badge cover-badge--featured">
        <StarFilled />
        精选
      </span>
      <span v-if="app.deployKey" class="cover-badge cover-badge--online">
        <i aria-hidden="true"></i>
        已上线
      </span>

      <div class="cover-overlay">
        <button type="button" class="overlay-btn overlay-btn--primary" @click.stop="handleViewChat">
          <MessageOutlined />
          查看对话
        </button>
        <button
          v-if="app.deployKey"
          type="button"
          class="overlay-btn"
          @click.stop="handleViewWork"
        >
          <ExportOutlined />
          访问作品
        </button>
      </div>
    </div>

    <div class="app-info">
      <a-avatar :src="app.user?.userAvatar" :size="38" class="app-avatar">
        {{ authorInitial }}
      </a-avatar>
      <div class="app-text">
        <h3 class="app-title">{{ app.appName || '未命名应用' }}</h3>
        <p class="app-author">
          <span>{{ app.user?.userName || (featured ? '官方精选' : '匿名创作者') }}</span>
          <template v-if="app.createTime">
            <i aria-hidden="true">·</i>
            <span>{{ relativeTime }}</span>
          </template>
        </p>
      </div>
      <span class="app-arrow" aria-hidden="true">
        <ArrowRightOutlined />
      </span>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  ArrowRightOutlined,
  ExportOutlined,
  MessageOutlined,
  StarFilled,
} from '@ant-design/icons-vue'
import { formatRelativeTime } from '@/utils/time'

interface Props {
  app: API.AppVO
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()

const isFeatured = computed(() => props.featured || props.app.priority === 99)
const appInitial = computed(() => (props.app.appName || 'A').trim().charAt(0).toUpperCase())
const authorInitial = computed(() => props.app.user?.userName?.charAt(0) || 'U')
const relativeTime = computed(() => formatRelativeTime(props.app.createTime))

/**
 * 没有封面时，根据应用 ID 生成稳定且柔和的色块，
 * 让每个应用的占位图都有自己的性格，而不是千篇一律的灰色。
 */
const coverStyle = computed(() => {
  const seed = Number(props.app.id) || 1
  const hue = (seed * 53 + 210) % 360
  const second = (hue + 42) % 360
  return {
    background: `radial-gradient(120% 120% at 18% 12%, hsl(${second} 96% 94%) 0%, transparent 55%), linear-gradient(140deg, hsl(${hue} 92% 96%) 0%, hsl(${second} 86% 89%) 100%)`,
    color: `hsl(${hue} 58% 42%)`,
  }
})

const handleViewChat = () => {
  emit('view-chat', props.app.id)
}

const handleViewWork = () => {
  emit('view-work', props.app)
}
</script>

<style scoped>
.app-card {
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 20px;
  border: 1px solid var(--line);
  background: var(--surface);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition:
    transform 0.32s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.32s ease,
    border-color 0.32s ease;
}

.app-card:hover {
  transform: translateY(-6px);
  border-color: rgba(99, 102, 241, 0.24);
  box-shadow: var(--shadow-lg);
}

.app-card:focus-visible {
  outline: 2px solid rgba(99, 102, 241, 0.55);
  outline-offset: 3px;
}

/* ------------------------------------------------------------------ cover */
.app-cover {
  position: relative;
  height: 188px;
  overflow: hidden;
  background: var(--surface-soft);
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}

.app-card:hover .cover-image {
  transform: scale(1.045);
}

.cover-fallback {
  position: relative;
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.cover-fallback::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.5) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.5) 1px, transparent 1px);
  background-size: 26px 26px;
  mask-image: radial-gradient(circle at 50% 50%, #000 10%, transparent 72%);
  -webkit-mask-image: radial-gradient(circle at 50% 50%, #000 10%, transparent 72%);
  opacity: 0.55;
}

.cover-glyph {
  position: absolute;
  top: 14px;
  left: 18px;
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.08em;
  opacity: 0.55;
}

.cover-initial {
  position: relative;
  font-size: 64px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.04em;
  opacity: 0.9;
  transition: transform 0.5s cubic-bezier(0.22, 1, 0.36, 1);
}

.app-card:hover .cover-initial {
  transform: scale(1.06) rotate(-2deg);
}

.cover-badge {
  position: absolute;
  top: 12px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.02em;
  backdrop-filter: blur(8px);
  z-index: 2;
}

.cover-badge--featured {
  left: 12px;
  color: #92400e;
  background: rgba(254, 243, 199, 0.94);
  box-shadow: inset 0 0 0 1px rgba(245, 158, 11, 0.28);
}

.cover-badge--online {
  right: 12px;
  color: #065f46;
  background: rgba(236, 253, 245, 0.92);
  box-shadow: inset 0 0 0 1px rgba(16, 185, 129, 0.28);
}

.cover-badge--online i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--mint);
  animation: pulse-ring 2.4s infinite;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.08) 0%, rgba(15, 23, 42, 0.64) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 3;
}

.app-card:hover .cover-overlay,
.app-card:focus-within .cover-overlay {
  opacity: 1;
}

.overlay-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 15px;
  border: 0;
  border-radius: 999px;
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--ink);
  background: rgba(255, 255, 255, 0.94);
  cursor: pointer;
  transform: translateY(8px);
  transition:
    transform 0.28s cubic-bezier(0.22, 1, 0.36, 1),
    background 0.2s ease,
    color 0.2s ease;
}

.app-card:hover .overlay-btn,
.app-card:focus-within .overlay-btn {
  transform: translateY(0);
}

.overlay-btn:hover {
  background: #fff;
}

.overlay-btn--primary {
  color: #fff;
  background: linear-gradient(135deg, var(--brand-500), var(--violet));
  box-shadow: 0 10px 20px -10px rgba(79, 70, 229, 0.9);
}

.overlay-btn--primary:hover {
  color: #fff;
  background: linear-gradient(135deg, #7079f6, #9b6ef8);
}

/* ------------------------------------------------------------------- meta */
.app-info {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 14px 16px 15px;
}

.app-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--brand-400), var(--violet));
  font-weight: 600;
}

.app-text {
  flex: 1;
  min-width: 0;
}

.app-title {
  margin: 0 0 2px;
  font-size: 15px;
  font-weight: 650;
  letter-spacing: -0.01em;
  color: var(--ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s ease;
}

.app-card:hover .app-title {
  color: var(--brand-600);
}

.app-author {
  display: flex;
  align-items: center;
  gap: 5px;
  margin: 0;
  font-size: 12px;
  color: var(--muted-2);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-author i {
  font-style: normal;
  opacity: 0.6;
}

.app-arrow {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  flex-shrink: 0;
  color: var(--muted-2);
  background: var(--surface-soft);
  transition:
    background 0.25s ease,
    color 0.25s ease,
    transform 0.25s ease;
}

.app-card:hover .app-arrow {
  color: #fff;
  background: linear-gradient(135deg, var(--brand-500), var(--violet));
  transform: translateX(2px);
}

.app-card--featured {
  border-color: rgba(245, 158, 11, 0.18);
}

@media (max-width: 480px) {
  .app-cover {
    height: 168px;
  }
}

/* 触摸设备没有 hover：让卡片操作常驻，避免“访问作品”无法触达 */
@media (hover: none) {
  .app-card:hover {
    transform: none;
  }

  .cover-overlay {
    opacity: 1;
    align-items: flex-end;
    padding-bottom: 13px;
    background: linear-gradient(180deg, rgba(15, 23, 42, 0) 38%, rgba(15, 23, 42, 0.52) 100%);
  }

  .overlay-btn {
    height: 32px;
    padding: 0 13px;
    font-size: 12px;
    transform: translateY(0);
  }

  .app-card:hover .cover-image {
    transform: none;
  }

  .app-card:hover .cover-initial {
    transform: none;
  }
}
</style>
