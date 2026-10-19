<template>
  <AuthShell
    eyebrow="WELCOME BACK"
    tag="AI零代码"
    title="欢迎回来"
    subtitle="登录后继续用一句话，创造属于你的完整应用。"
    aside-title="一句话，就是一个应用的起点"
    aside-description="把脑海里的想法交给 AI，几分钟后你就能拥有一个可访问的完整网站。"
    :features="[
      '自动生成页面、交互与全栈代码',
      '实时预览，对话式持续迭代',
      '一键部署，立即分享你的作品',
    ]"
  >
    <a-form
      class="auth-form"
      :model="formState"
      name="login"
      autocomplete="off"
      @finish="handleSubmit"
    >
      <a-form-item
        label="账号"
        name="userAccount"
        :rules="[{ required: true, message: '请输入账号' }]"
      >
        <a-input
          v-model:value="formState.userAccount"
          size="large"
          placeholder="请输入账号"
          autocomplete="username"
        >
          <template #prefix><UserOutlined /></template>
        </a-input>
      </a-form-item>

      <a-form-item
        label="密码"
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于 8 位' },
        ]"
      >
        <a-input-password
          v-model:value="formState.userPassword"
          size="large"
          placeholder="请输入密码"
          autocomplete="current-password"
        >
          <template #prefix><LockOutlined /></template>
        </a-input-password>
      </a-form-item>

      <p class="auth-hint">
        <SafetyCertificateOutlined />
        账号信息仅用于登录，我们会妥善保管你的数据。
      </p>

      <a-button
        type="primary"
        html-type="submit"
        block
        class="auth-submit"
        :loading="submitting"
      >
        登录
        <ArrowRightOutlined v-if="!submitting" />
      </a-button>
    </a-form>

    <template #switch>
      还没有账号？
      <RouterLink to="/user/register">免费注册</RouterLink>
    </template>
  </AuthShell>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowRightOutlined, LockOutlined, SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons-vue'
import AuthShell from '@/components/AuthShell.vue'
import { userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
const submitting = ref(false)

const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 */
const handleSubmit = async (values: API.UserLoginRequest) => {
  submitting.value = true
  try {
    const res = await userLogin(values)
    // 登录成功，把登录态保存到全局状态中
    if (res.data.code === 0 && res.data.data) {
      await loginUserStore.fetchLoginUser()
      message.success('登录成功，欢迎回来')
      router.push({
        path: '/',
        replace: true,
      })
    } else {
      message.error('登录失败，' + res.data.message)
    }
  } catch (error) {
    console.error('登录失败：', error)
    message.error('登录失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>
