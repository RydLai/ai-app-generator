<template>
  <footer class="site-footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <span class="footer-mark">
          <img :src="logo" alt="" />
        </span>
        <div class="footer-brand-copy">
          <strong>AI零代码应用生成平台</strong>
          <p>不写一行代码，生成完整应用</p>
        </div>
      </div>

      <nav class="footer-links" aria-label="页脚导航">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/user/login">登录</RouterLink>
        <RouterLink to="/user/register">注册</RouterLink>
      </nav>
    </div>

    <div class="footer-meta">
      <span class="footer-status">
        <i aria-hidden="true"></i>
        All systems operational
      </span>
      <span class="footer-copy">
        © {{ year }} AI零代码应用生成平台 · by Ryd
      </span>
    </div>
  </footer>
</template>

<script setup lang="ts">
import logo from '@/assets/logo.png'

const year = new Date().getFullYear()
</script>

<style scoped>
.site-footer {
  margin-top: 32px;
  border-top: 1px solid rgba(15, 23, 42, 0.07);
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(10px);
}

.footer-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
  width: 100%;
  max-width: var(--page-max);
  margin: 0 auto;
  padding: 28px 24px 18px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 11px;
}

.footer-mark {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 0 0 1px rgba(15, 23, 42, 0.06);
  flex-shrink: 0;
}

.footer-mark img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.footer-brand-copy strong {
  display: block;
  font-size: 13.5px;
  font-weight: 650;
  color: var(--ink);
}

.footer-brand-copy p {
  margin: 1px 0 0;
  font-size: 12px;
  color: var(--muted-2);
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 22px;
  flex-wrap: wrap;
}

.footer-links a {
  color: var(--muted);
  font-size: 13px;
}

.footer-links a:hover {
  color: var(--brand-600);
}

.footer-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  width: 100%;
  max-width: var(--page-max);
  margin: 0 auto;
  padding: 14px 24px 24px;
  border-top: 1px dashed rgba(15, 23, 42, 0.08);
  font-size: 12px;
  color: var(--muted-2);
}

.footer-status {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  letter-spacing: 0.02em;
}

.footer-status i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--mint);
  animation: pulse-ring 2.4s infinite;
}

.footer-copy {
  letter-spacing: 0.01em;
}

@media (max-width: 640px) {
  .footer-inner,
  .footer-meta {
    padding-left: 16px;
    padding-right: 16px;
  }

  .footer-inner {
    gap: 16px;
  }

  .footer-links {
    gap: 16px;
  }
}
</style>
