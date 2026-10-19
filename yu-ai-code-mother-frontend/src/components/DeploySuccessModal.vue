<template>
  <a-modal v-model:open="visible" :footer="null" :width="520" centered>
    <div class="deploy-success">
      <span class="confetti confetti--1" aria-hidden="true"></span>
      <span class="confetti confetti--2" aria-hidden="true"></span>
      <span class="confetti confetti--3" aria-hidden="true"></span>
      <span class="confetti confetti--4" aria-hidden="true"></span>

      <div class="success-icon">
        <CheckCircleFilled />
      </div>

      <h2>网站部署成功</h2>
      <p class="success-desc">你的作品已经上线，把下面的链接分享给朋友吧。</p>

      <div class="deploy-url">
        <span class="deploy-url__icon"><LinkOutlined /></span>
        <span class="deploy-url__text">{{ deployUrl }}</span>
        <button
          type="button"
          class="copy-btn"
          :class="{ 'is-copied': copied }"
          :aria-label="copied ? '已复制' : '复制链接'"
          @click="handleCopyUrl"
        >
          <CheckOutlined v-if="copied" />
          <CopyOutlined v-else />
        </button>
      </div>

      <div class="deploy-actions">
        <a-button type="primary" block size="large" @click="handleOpenSite">
          <template #icon><ExportOutlined /></template>
          访问网站
        </a-button>
        <a-button block size="large" @click="handleClose">稍后再说</a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  CheckCircleFilled,
  CheckOutlined,
  CopyOutlined,
  ExportOutlined,
  LinkOutlined,
} from '@ant-design/icons-vue'

interface Props {
  open: boolean
  deployUrl: string
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'open-site'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const copied = ref(false)

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const handleCopyUrl = async () => {
  try {
    await navigator.clipboard.writeText(props.deployUrl)
    copied.value = true
    message.success('链接已复制到剪贴板')
    window.setTimeout(() => {
      copied.value = false
    }, 2000)
  } catch (error) {
    console.error('复制失败：', error)
    message.error('复制失败，请手动选择链接复制')
  }
}

const handleOpenSite = () => {
  emit('open-site')
}

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.deploy-success {
  position: relative;
  padding: 14px 8px 4px;
  text-align: center;
  overflow: hidden;
}

.success-icon {
  display: grid;
  place-items: center;
  width: 76px;
  height: 76px;
  margin: 0 auto 18px;
  border-radius: 26px;
  font-size: 38px;
  color: #fff;
  background: linear-gradient(135deg, #34d399, #10b981 55%, #059669);
  box-shadow:
    0 22px 40px -22px rgba(16, 185, 129, 0.85),
    0 0 0 8px rgba(16, 185, 129, 0.08);
  animation: pop-in 0.5s cubic-bezier(0.22, 1.28, 0.36, 1) both;
}

@keyframes pop-in {
  from {
    opacity: 0;
    transform: scale(0.6) rotate(-8deg);
  }
  to {
    opacity: 1;
    transform: scale(1) rotate(0);
  }
}

.confetti {
  position: absolute;
  width: 7px;
  height: 7px;
  border-radius: 2px;
  opacity: 0.75;
  animation: float-slow 6s ease-in-out infinite;
}

.confetti--1 {
  top: 26px;
  left: 18%;
  background: #a5b4fc;
  transform: rotate(24deg);
}

.confetti--2 {
  top: 12px;
  right: 20%;
  width: 6px;
  height: 10px;
  background: #fbcfe8;
  transform: rotate(-18deg);
  animation-delay: 0.8s;
}

.confetti--3 {
  top: 66px;
  right: 9%;
  border-radius: 50%;
  background: #fcd34d;
  animation-delay: 1.4s;
}

.confetti--4 {
  top: 84px;
  left: 11%;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #6ee7b7;
  animation-delay: 0.4s;
}

.deploy-success h2 {
  margin: 0 0 8px;
  font-size: 23px;
  font-weight: 750;
  letter-spacing: -0.025em;
  color: var(--ink);
}

.success-desc {
  margin: 0 0 22px;
  color: var(--muted);
  font-size: 13px;
}

.deploy-url {
  display: flex;
  align-items: center;
  gap: 9px;
  margin-bottom: 22px;
  padding: 10px 10px 10px 12px;
  border-radius: 13px;
  border: 1px solid var(--line-strong);
  background: var(--surface-soft);
  text-align: left;
}

.deploy-url__icon {
  display: grid;
  place-items: center;
  width: 26px;
  height: 26px;
  flex-shrink: 0;
  border-radius: 8px;
  color: var(--brand-600);
  background: var(--brand-50);
  font-size: 12px;
}

.deploy-url__text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--ink-2);
  font-family: var(--font-mono);
  font-size: 12px;
}

.copy-btn {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border: 1px solid var(--line);
  border-radius: 9px;
  background: #fff;
  color: var(--ink-3);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.copy-btn:hover {
  color: var(--brand-600);
  border-color: var(--brand-200);
}

.copy-btn.is-copied {
  color: #047857;
  border-color: rgba(16, 185, 129, 0.3);
  background: #ecfdf5;
}

.deploy-actions {
  display: flex;
  flex-direction: column;
  gap: 9px;
}
</style>
