<template>
  <div class="app-shell" :class="{ 'app-shell--workspace': isWorkspace }">
    <!-- 顶部导航栏 -->
    <GlobalHeader />
    <!-- 主要内容区域 -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <!-- 底部版权信息（对话工作台不展示，保持沉浸式布局） -->
    <GlobalFooter v-if="!isWorkspace" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'

const route = useRoute()

// 对话页面使用全屏工作台布局
const isWorkspace = computed(() => route.path.startsWith('/app/chat/'))
</script>

<style scoped>
.app-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background:
    radial-gradient(1000px circle at 12% -10%, rgba(99, 102, 241, 0.08), transparent 55%),
    radial-gradient(900px circle at 92% 0%, rgba(236, 72, 153, 0.06), transparent 50%),
    var(--page-bg);
}

.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.app-shell--workspace {
  height: 100vh;
  overflow: hidden;
  background: #f4f5fb;
}

@media (max-width: 980px) {
  .app-shell--workspace {
    height: auto;
    min-height: 100vh;
    overflow: visible;
  }
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.page-fade-leave-to {
  opacity: 0;
}
</style>
