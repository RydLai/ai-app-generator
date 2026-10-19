<template>
  <AuthShell
    eyebrow="GET STARTED"
    tag="免费注册"
    title="创建你的账号"
    subtitle="加入平台，用一个账号保存、迭代并部署你的所有作品。"
    aside-title="从想法到上线，只差一个账号"
    aside-description="无需配置环境，不必编写代码。注册后，AI 会替你完成从设计到部署的全部工作。"
    :features="[
      '无限次对话，持续打磨每个细节',
      '作品云端保存，随时继续创作',
      '一键生成可分享的在线地址',
    ]"
  >
    <a-form
      class="auth-form"
      :model="formState"
      name="register"
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
          { min: 8, message: '密码不能小于 8 位' },
        ]"
      >
        <a-input-password
          v-model:value="formState.userPassword"
          size="large"
          placeholder="至少 8 位字符"
          autocomplete="new-password"
        >
          <template #prefix><LockOutlined /></template>
        </a-input-password>
      </a-form-item>

      <a-form-item
        label="确认密码"
        name="checkPassword"
        :rules="[
          { required: true, message: '请确认密码' },
          { min: 8, message: '密码不能小于 8 位' },
          { validator: validateCheckPassword },
        ]"
      >
        <a-input-password
          v-model:value="formState.checkPassword"
          size="large"
          placeholder="请再次输入密码"
          autocomplete="new-password"
        >
          <template #prefix><SafetyCertificateOutlined /></template>
        </a-input-password>
      </a-form-item>

      <a-button
        type="primary"
        html-type="submit"
        block
        class="auth-submit"
        :loading="submitting"
      >
        创建账号
        <ArrowRightOutlined v-if="!submitting" />
      </a-button>
    </a-form>

    <template #switch>
      已有账号？
      <RouterLink to="/user/login">直接登录</RouterLink>
    </template>
  </AuthShell>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowRightOutlined,
  LockOutlined,
  SafetyCertificateOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import AuthShell from '@/components/AuthShell.vue'
import { userRegister } from '@/api/userController.ts'

const router = useRouter()
const submitting = ref(false)

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

/**
 * 验证确认密码
 */
const validateCheckPassword = (
  rule: unknown,
  value: string,
  callback: (error?: Error) => void,
) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  submitting.value = true
  try {
    const res = await userRegister(values)
    // 注册成功，跳转到登录页面
    if (res.data.code === 0) {
      message.success('注册成功，请登录')
      router.push({
        path: '/user/login',
        replace: true,
      })
    } else {
      message.error('注册失败，' + res.data.message)
    }
  } catch (error) {
    console.error('注册失败：', error)
    message.error('注册失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>
