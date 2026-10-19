<template>
  <header class="site-header">
    <div class="header-inner">
      <!-- 品牌 -->
      <RouterLink to="/" class="brand" aria-label="AI零代码应用生成平台首页">
        <span class="brand-mark">
          <img :src="logo" alt="" />
        </span>
        <span class="brand-copy">
          <strong>AI<em>零代码</em></strong>
          <small>应用生成平台</small>
        </span>
      </RouterLink>

      <!-- 主导航 -->
      <nav class="site-nav" aria-label="主导航">
        <RouterLink to="/" class="nav-link" :class="{ 'is-active': route.path === '/' }">
          <HomeOutlined />
          <span>首页</span>
        </RouterLink>

        <a-dropdown v-if="isAdmin" placement="bottomLeft" :trigger="['hover', 'click']">
          <button
            type="button"
            class="nav-link nav-link--button"
            :class="{ 'is-active': route.path.startsWith('/admin') }"
          >
            <AppstoreOutlined />
            <span>控制台</span>
            <DownOutlined class="nav-caret" />
          </button>
          <template #overlay>
            <a-menu class="admin-menu" :selected-keys="[route.path]">
              <a-menu-item key="/admin/userManage" @click="go('/admin/userManage')">
                <TeamOutlined />
                用户管理
              </a-menu-item>
              <a-menu-item key="/admin/appManage" @click="go('/admin/appManage')">
                <AppstoreOutlined />
                应用管理
              </a-menu-item>
              <a-menu-item key="/admin/chatManage" @click="go('/admin/chatManage')">
                <MessageOutlined />
                对话管理
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </nav>

      <!-- 用户区域 -->
      <div class="header-actions">
        <!-- 移动端导航入口 -->
        <a-dropdown v-model:open="mobileMenuOpen" placement="bottomRight" :trigger="['click']">
          <button type="button" class="mobile-nav-btn" aria-label="打开导航菜单">
            <MenuOutlined />
          </button>
          <template #overlay>
            <a-menu class="mobile-menu" :selected-keys="[route.path]">
              <a-menu-item key="/" @click="mobileGo('/')">
                <HomeOutlined />
                首页
              </a-menu-item>
              <template v-if="isAdmin">
                <a-menu-divider />
                <a-menu-item key="/admin/userManage" @click="mobileGo('/admin/userManage')">
                  <TeamOutlined />
                  用户管理
                </a-menu-item>
                <a-menu-item key="/admin/appManage" @click="mobileGo('/admin/appManage')">
                  <AppstoreOutlined />
                  应用管理
                </a-menu-item>
                <a-menu-item key="/admin/chatManage" @click="mobileGo('/admin/chatManage')">
                  <MessageOutlined />
                  对话管理
                </a-menu-item>
              </template>
            </a-menu>
          </template>
        </a-dropdown>

        <template v-if="loginUser.id">
          <a-dropdown placement="bottomRight" :trigger="['hover', 'click']">
            <button type="button" class="user-chip">
              <a-avatar :src="loginUser.userAvatar" :size="34" class="user-avatar">
                {{ initial }}
              </a-avatar>
              <span class="user-copy">
                <strong>{{ displayName }}</strong>
                <small>{{ isAdmin ? '管理员' : '创作者' }}</small>
              </span>
              <DownOutlined class="user-caret" />
            </button>
            <template #overlay>
              <a-menu class="user-menu">
                <div class="user-menu-head">
                  <a-avatar :src="loginUser.userAvatar" :size="40">{{ initial }}</a-avatar>
                  <div>
                    <strong>{{ displayName }}</strong>
                    <span>{{ loginUser.userAccount || '已登录' }}</span>
                  </div>
                </div>
                <a-menu-divider />
                <a-menu-item key="logout" @click="doLogout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>

        <template v-else>
          <RouterLink to="/user/login" class="header-login">登录</RouterLink>
          <RouterLink to="/user/register" class="header-signup">
            免费注册
            <ArrowRightOutlined />
          </RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import logo from '@/assets/logo.png'
import {
  AppstoreOutlined,
  ArrowRightOutlined,
  DownOutlined,
  HomeOutlined,
  LogoutOutlined,
  MenuOutlined,
  MessageOutlined,
  TeamOutlined,
} from '@ant-design/icons-vue'

const loginUserStore = useLoginUserStore()
const router = useRouter()
const route = useRoute()

// 移动端导航下拉状态
const mobileMenuOpen = ref(false)

const loginUser = computed(() => loginUserStore.loginUser)
const isAdmin = computed(() => loginUser.value.userRole === 'admin')
const displayName = computed(() => loginUser.value.userName ?? '无名')
const initial = computed(() => displayName.value.charAt(0) || 'U')

const go = (path: string) => {
  if (route.path !== path) {
    router.push(path)
  }
}

// 移动端导航跳转后收起菜单
const mobileGo = (path: string) => {
  mobileMenuOpen.value = false
  go(path)
}

// 退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.site-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  height: var(--header-height);
  background: rgba(246, 247, 252, 0.78);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.header-inner {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  width: 100%;
  max-width: var(--page-max);
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
}

/* ------------------------------------------------------------------ brand */
.brand {
  display: inline-flex;
  align-items: center;
  gap: 11px;
  flex-shrink: 0;
  color: var(--ink);
}

.brand:hover {
  color: var(--ink);
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  padding: 2px;
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
  box-shadow:
    0 6px 16px -8px rgba(15, 23, 42, 0.28),
    0 0 0 1px rgba(15, 23, 42, 0.05);
  transition: transform 0.25s ease;
}

.brand:hover .brand-mark {
  transform: rotate(-4deg) scale(1.04);
}

.brand-mark img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
}

.brand-copy {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}

.brand-copy strong {
  font-size: 17px;
  font-weight: 750;
  letter-spacing: -0.02em;
}

.brand-copy strong em {
  font-style: normal;
  background: linear-gradient(120deg, var(--brand-600), var(--violet) 60%, #ec4899);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-copy small {
  margin-top: 2px;
  font-size: 10.5px;
  font-weight: 500;
  letter-spacing: 0.14em;
  color: var(--muted-2);
  text-transform: uppercase;
}

/* -------------------------------------------------------------------- nav */
.site-nav {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(15, 23, 42, 0.05);
}

.nav-link {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  border: 0;
  background: transparent;
  color: var(--ink-3);
  font-size: 13.5px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition:
    color 0.2s ease,
    background 0.2s ease,
    box-shadow 0.2s ease;
}

.nav-link:hover {
  color: var(--brand-600);
  background: rgba(99, 102, 241, 0.07);
}

.nav-link.is-active {
  color: var(--brand-600);
  background: #fff;
  box-shadow:
    0 4px 12px -6px rgba(30, 41, 59, 0.22),
    0 0 0 1px rgba(99, 102, 241, 0.1);
}

.nav-link .anticon {
  font-size: 14px;
}

.nav-caret {
  font-size: 9px;
  opacity: 0.6;
}

.nav-link--button {
  appearance: none;
}

/* ---------------------------------------------------------------- actions */
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.mobile-nav-btn {
  display: none;
  place-items: center;
  width: 34px;
  height: 34px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.85);
  color: var(--ink-3);
  font-size: 14px;
  cursor: pointer;
  box-shadow: var(--shadow-xs);
  transition:
    color 0.2s ease,
    border-color 0.2s ease,
    background 0.2s ease;
}

.mobile-nav-btn:hover {
  color: var(--brand-600);
  border-color: var(--brand-200);
  background: #fff;
}

.mobile-menu {
  min-width: 176px;
  padding: 6px;
  border-radius: 14px;
  border: 1px solid var(--line);
  box-shadow: var(--shadow-md);
}

.header-login {
  padding: 8px 12px;
  color: var(--ink-3);
  font-weight: 500;
  border-radius: 10px;
}

.header-login:hover {
  color: var(--brand-600);
  background: rgba(99, 102, 241, 0.06);
}

.header-signup {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 38px;
  padding: 0 18px;
  border-radius: 999px;
  color: #fff;
  font-size: 13.5px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--brand-500), var(--violet));
  box-shadow: 0 10px 22px -12px rgba(79, 70, 229, 0.75);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.header-signup:hover {
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 14px 26px -12px rgba(79, 70, 229, 0.85);
}

.header-signup .anticon {
  font-size: 11px;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  height: 44px;
  padding: 4px 12px 4px 5px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.85);
  box-shadow: var(--shadow-xs);
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    background 0.2s ease;
}

.user-chip:hover {
  border-color: var(--brand-200);
  background: #fff;
  box-shadow: var(--shadow-sm);
}

.user-avatar {
  background: linear-gradient(135deg, var(--brand-400), var(--violet));
  font-weight: 600;
}

.user-copy {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.2;
  max-width: 120px;
}

.user-copy strong {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 120px;
}

.user-copy small {
  font-size: 10.5px;
  color: var(--muted-2);
}

.user-caret {
  font-size: 9px;
  color: var(--muted-2);
}

/* ---------------------------------------------------------- dropdown menus */
.admin-menu {
  min-width: 168px;
  padding: 6px;
  border-radius: 14px;
  border: 1px solid var(--line);
  box-shadow: var(--shadow-md);
}

.user-menu {
  min-width: 230px;
  padding: 8px;
  border-radius: 16px;
  border: 1px solid var(--line);
  box-shadow: var(--shadow-md);
}

.user-menu-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 8px 10px;
}

.user-menu-head > div {
  display: flex;
  flex-direction: column;
  min-width: 0;
  line-height: 1.3;
}

.user-menu-head strong {
  font-size: 13.5px;
  color: var(--ink);
}

.user-menu-head span {
  font-size: 12px;
  color: var(--muted-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* --------------------------------------------------------------- responsive */
@media (max-width: 980px) {
  .brand-copy small {
    display: none;
  }

  .nav-link {
    padding: 0 11px;
  }
}

@media (max-width: 860px) {
  .site-nav {
    display: none;
  }

  .mobile-nav-btn {
    display: grid;
  }
}

@media (max-width: 640px) {
  .header-inner {
    padding: 0 16px;
    gap: 12px;
  }

  .brand-copy {
    display: none;
  }

  .brand-mark {
    width: 38px;
    height: 38px;
  }

  .header-login {
    padding: 6px 8px;
  }

  .header-signup {
    height: 34px;
    padding: 0 14px;
    font-size: 12.5px;
  }

  .user-copy {
    display: none;
  }

  .user-chip {
    height: 40px;
    padding: 3px 8px 3px 3px;
  }
}
</style>
