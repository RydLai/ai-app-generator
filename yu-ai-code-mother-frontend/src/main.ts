import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import Antd, { message } from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import '@/styles/theme.css'

import '@/access'

// 顶部有吸顶导航，把全局提示下移，避免遮挡导航与工作台操作区
message.config({
  top: '88px',
  duration: 2.5,
})

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)

app.mount('#app')
