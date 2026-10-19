<template>
  <section class="auth-page">
    <div class="auth-card fade-up">
      <!-- 品牌叙事区 -->
      <aside class="auth-aside" aria-hidden="true">
        <div class="aside-grid"></div>
        <span class="aside-orb aside-orb--one"></span>
        <span class="aside-orb aside-orb--two"></span>

        <div class="aside-brand">
          <span class="aside-logo">
            <img :src="logo" alt="" />
          </span>
          <span class="aside-brand-name">AI<em>零代码</em>应用生成平台</span>
        </div>

        <div class="aside-copy">
          <span class="aside-eyebrow">{{ eyebrow }}</span>
          <h2>{{ asideTitle }}</h2>
          <p>{{ asideDescription }}</p>
        </div>

        <ul class="aside-features">
          <li v-for="feature in features" :key="feature">
            <CheckCircleFilled />
            <span>{{ feature }}</span>
          </li>
        </ul>

        <div class="aside-preview">
          <div class="preview-row">
            <span class="preview-avatar">你</span>
            <p>做一个有氛围感的咖啡店官网</p>
          </div>
          <div class="preview-row preview-row--ai">
            <span class="preview-avatar preview-avatar--ai">
              <img :src="aiAvatar" alt="" />
            </span>
            <div class="preview-bubble">
              <span class="preview-status">
                <i aria-hidden="true"></i>
                正在生成首页与菜单…
              </span>
              <span class="preview-bar"><b></b></span>
            </div>
          </div>
        </div>

        <p class="aside-footnote">AI零代码应用生成平台 · by Ryd</p>
      </aside>

      <!-- 表单区 -->
      <div class="auth-main">
        <div class="auth-form-wrap">
          <header class="auth-head">
            <span class="auth-tag">{{ tag }}</span>
            <h1>{{ title }}</h1>
            <p>{{ subtitle }}</p>
          </header>

          <slot />

          <footer class="auth-switch">
            <slot name="switch" />
          </footer>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { CheckCircleFilled } from '@ant-design/icons-vue'
import logo from '@/assets/logo.png'
import aiAvatar from '@/assets/aiAvatar.png'

interface Props {
  eyebrow: string
  tag: string
  title: string
  subtitle: string
  asideTitle: string
  asideDescription: string
  features: string[]
}

withDefaults(defineProps<Props>(), {
  eyebrow: 'AI APP GENERATOR',
  tag: 'AI零代码',
  title: '欢迎回来',
  subtitle: '登录后继续用一句话创造你的应用',
  asideTitle: '一句话，就是一个应用的起点',
  asideDescription: '把脑海里的想法交给 AI，几分钟后你就能拥有一个可访问的完整网站。',
  features: () => ['自动生成页面、交互与全栈代码', '实时预览，对话式持续迭代', '一键部署，立即分享你的作品'],
})
</script>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: calc(100vh - var(--header-height) - 190px);
  padding: 48px 24px 64px;
  box-sizing: border-box;
}

.auth-card {
  display: grid;
  grid-template-columns: 0.92fr 1.08fr;
  width: 100%;
  max-width: 980px;
  overflow: hidden;
  border-radius: 28px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  background: var(--surface);
  box-shadow:
    0 40px 90px -50px rgba(49, 46, 129, 0.45),
    0 8px 24px -16px rgba(30, 41, 59, 0.18);
}

/* ------------------------------------------------------------------ aside */
.auth-aside {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 26px;
  padding: 38px 38px 30px;
  overflow: hidden;
  color: #fff;
  background:
    radial-gradient(520px circle at 10% 0%, rgba(129, 140, 248, 0.55), transparent 55%),
    radial-gradient(480px circle at 100% 100%, rgba(236, 72, 153, 0.32), transparent 58%),
    linear-gradient(158deg, #312e81 0%, #4f46e5 52%, #7c3aed 100%);
  isolation: isolate;
}

.aside-grid {
  position: absolute;
  inset: 0;
  z-index: -1;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.09) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.09) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: radial-gradient(ellipse 85% 70% at 30% 20%, #000 10%, transparent 80%);
  -webkit-mask-image: radial-gradient(ellipse 85% 70% at 30% 20%, #000 10%, transparent 80%);
}

.aside-orb {
  position: absolute;
  z-index: -1;
  border-radius: 50%;
  filter: blur(2px);
  pointer-events: none;
}

.aside-orb--one {
  top: -60px;
  right: -70px;
  width: 220px;
  height: 220px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.22), transparent 65%);
}

.aside-orb--two {
  bottom: 80px;
  left: -90px;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.22), transparent 66%);
}

.aside-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.aside-logo {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  overflow: hidden;
  padding: 2px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 8px 18px -8px rgba(15, 23, 42, 0.5);
}

.aside-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
}

.aside-brand-name {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.aside-brand-name em {
  font-style: normal;
  color: #fbcfe8;
}

.aside-copy {
  margin-top: 6px;
}

.aside-eyebrow {
  display: inline-block;
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 0.22em;
  color: rgba(224, 231, 255, 0.72);
}

.aside-copy h2 {
  margin: 12px 0 10px;
  font-size: 28px;
  font-weight: 750;
  line-height: 1.28;
  letter-spacing: -0.025em;
}

.aside-copy p {
  margin: 0;
  max-width: 320px;
  color: rgba(224, 231, 255, 0.78);
  font-size: 13px;
  line-height: 1.8;
}

.aside-features {
  display: flex;
  flex-direction: column;
  gap: 11px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.aside-features li {
  display: flex;
  align-items: center;
  gap: 9px;
  color: rgba(238, 242, 255, 0.92);
  font-size: 12.5px;
}

.aside-features .anticon {
  color: #a7f3d0;
  font-size: 13px;
  flex-shrink: 0;
}

.aside-preview {
  margin-top: auto;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.preview-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.preview-row p {
  margin: 0;
  padding: 7px 11px;
  border-radius: 10px 10px 10px 3px;
  background: rgba(255, 255, 255, 0.16);
  color: #f8fafc;
  font-size: 11.5px;
  line-height: 1.5;
}

.preview-avatar {
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  border-radius: 8px;
  flex-shrink: 0;
  font-size: 10px;
  font-weight: 700;
  color: #3730a3;
  background: #e0e7ff;
  overflow: hidden;
}

.preview-avatar--ai img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-row--ai {
  flex-direction: row;
}

.preview-bubble {
  flex: 1;
  min-width: 0;
  padding: 8px 11px;
  border-radius: 10px 10px 3px 10px;
  background: rgba(15, 23, 42, 0.24);
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.preview-status {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(224, 231, 255, 0.9);
  font-size: 10.5px;
}

.preview-status i {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #6ee7b7;
  animation: pulse-ring 2s infinite;
}

.preview-bar {
  display: block;
  width: 100%;
  height: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  overflow: hidden;
}

.preview-bar b {
  display: block;
  width: 68%;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #a5b4fc, #f9a8d4);
  animation: shimmer 2.6s linear infinite;
  background-size: 200% auto;
}

.aside-footnote {
  margin: 0;
  color: rgba(199, 210, 254, 0.48);
  font-size: 10.5px;
  letter-spacing: 0.04em;
}

/* ------------------------------------------------------------------- main */
.auth-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 46px 52px;
  background:
    radial-gradient(520px circle at 100% 0%, rgba(99, 102, 241, 0.05), transparent 55%),
    var(--surface);
}

.auth-form-wrap {
  width: 100%;
  max-width: 360px;
}

.auth-tag {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: var(--brand-50);
  color: var(--brand-600);
  font-size: 10.5px;
  font-weight: 650;
  letter-spacing: 0.08em;
}

.auth-head h1 {
  margin: 14px 0 8px;
  font-size: 29px;
  font-weight: 750;
  letter-spacing: -0.03em;
  color: var(--ink);
}

.auth-head p {
  margin: 0 0 28px;
  color: var(--muted);
  font-size: 13.5px;
  line-height: 1.7;
}

.auth-switch {
  margin-top: 22px;
  padding-top: 20px;
  border-top: 1px dashed var(--line);
  text-align: center;
  color: var(--muted);
  font-size: 12.5px;
}

.auth-switch :deep(a) {
  margin-left: 5px;
  font-weight: 600;
  color: var(--brand-600);
}

@media (max-width: 900px) {
  .auth-card {
    grid-template-columns: 1fr;
    max-width: 480px;
  }

  .auth-aside {
    display: none;
  }

  .auth-main {
    padding: 40px 28px;
  }
}

@media (max-width: 480px) {
  .auth-page {
    padding: 24px 14px 40px;
  }

  .auth-card {
    border-radius: 22px;
  }

  .auth-main {
    padding: 32px 22px;
  }

  .auth-head h1 {
    font-size: 25px;
  }
}
</style>
