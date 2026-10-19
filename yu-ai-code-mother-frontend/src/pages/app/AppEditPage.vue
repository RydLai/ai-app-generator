<template>
  <div class="page-shell edit-page">
    <header class="page-head">
      <div class="page-head__text">
        <button type="button" class="back-link" @click="router.push('/')">
          <ArrowLeftOutlined />
          返回首页
        </button>
        <span class="eyebrow">Application Settings</span>
        <h1>编辑应用信息</h1>
        <p>
          更新应用名称{{ isAdmin ? '、封面与精选优先级' : '' }}，其余配置由系统维护。
        </p>
      </div>
      <div class="page-head__aside">
        <a-button @click="goToChat">
          <template #icon><MessageOutlined /></template>
          进入对话
        </a-button>
        <a-button type="primary" :loading="submitting" @click="submitForm">
          <template #icon><SaveOutlined /></template>
          保存修改
        </a-button>
      </div>
    </header>

    <div class="edit-grid">
      <div class="edit-main">
        <!-- 基本信息 -->
        <a-card class="edit-card" :loading="loading">
          <template #title>
            <span class="card-title">
              <span class="card-title__icon"><EditOutlined /></span>
              基本信息
            </span>
          </template>

          <a-form
            ref="formRef"
            :model="formData"
            :rules="rules"
            layout="vertical"
            @finish="handleSubmit"
          >
            <a-form-item label="应用名称" name="appName">
              <a-input
                v-model:value="formData.appName"
                placeholder="请输入应用名称"
                :maxlength="50"
                show-count
                size="large"
              />
            </a-form-item>

            <a-form-item
              v-if="isAdmin"
              label="应用封面"
              name="cover"
              extra="支持图片链接，建议尺寸 400 × 300"
            >
              <a-input v-model:value="formData.cover" placeholder="请输入封面图片链接" />
              <div v-if="formData.cover" class="cover-preview">
                <a-image
                  :src="formData.cover"
                  :width="200"
                  :height="150"
                  fallback="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
                />
              </div>
            </a-form-item>

            <a-form-item v-if="isAdmin" label="优先级" name="priority" extra="设置为 99 表示精选应用">
              <a-input-number
                v-model:value="formData.priority"
                :min="0"
                :max="99"
                style="width: 200px"
              />
            </a-form-item>

            <div class="form-divider"></div>

            <a-form-item label="初始提示词" name="initPrompt">
              <a-textarea
                v-model:value="formData.initPrompt"
                placeholder="请输入初始提示词"
                :rows="4"
                :maxlength="1000"
                show-count
                disabled
              />
              <div class="form-tip">
                <LockOutlined />
                初始提示词是应用的起点，创建后不可修改
              </div>
            </a-form-item>

            <a-form-item label="生成类型" name="codeGenType">
              <a-input :value="formatCodeGenType(formData.codeGenType)" disabled />
              <div class="form-tip">
                <LockOutlined />
                生成类型由首次创建时确定
              </div>
            </a-form-item>

            <a-form-item v-if="formData.deployKey" label="部署密钥" name="deployKey">
              <a-input v-model:value="formData.deployKey" placeholder="部署密钥" disabled />
              <div class="form-tip">
                <LockOutlined />
                部署密钥不可修改
              </div>
            </a-form-item>

            <div class="form-actions">
              <a-button type="primary" html-type="submit" :loading="submitting">
                保存修改
              </a-button>
              <a-button @click="resetForm">重置</a-button>
            </div>
          </a-form>
        </a-card>
      </div>

      <!-- 右侧应用概览 -->
      <aside class="edit-aside">
        <div class="panel overview-card">
          <div class="overview-cover" :style="previewCoverStyle">
            <img v-if="formData.cover" :src="formData.cover" alt="" />
            <span v-else class="overview-initial">{{ previewInitial }}</span>
          </div>

          <div class="overview-body">
            <h2 class="overview-name">{{ formData.appName || '未命名应用' }}</h2>
            <div class="overview-pills">
              <span class="meta-pill meta-pill--violet">
                {{ formatCodeGenType(formData.codeGenType) }}
              </span>
              <span
                class="meta-pill"
                :class="appInfo?.deployedTime ? 'meta-pill--green' : 'meta-pill--slate'"
              >
                <i aria-hidden="true"></i>
                {{ appInfo?.deployedTime ? '已部署' : '未部署' }}
              </span>
              <span v-if="formData.priority === 99" class="meta-pill meta-pill--amber">
                <StarFilled />
                精选
              </span>
            </div>

            <div class="overview-divider"></div>

            <dl class="overview-list">
              <div class="overview-row">
                <dt>应用 ID</dt>
                <dd class="mono">{{ appInfo?.id || '—' }}</dd>
              </div>
              <div class="overview-row">
                <dt>创建者</dt>
                <dd>
                  <UserInfo :user="appInfo?.user" size="small" />
                </dd>
              </div>
              <div class="overview-row">
                <dt>创建时间</dt>
                <dd class="mono">{{ formatTime(appInfo?.createTime, 'YYYY-MM-DD HH:mm') || '—' }}</dd>
              </div>
              <div class="overview-row">
                <dt>更新时间</dt>
                <dd class="mono">{{ formatTime(appInfo?.updateTime, 'YYYY-MM-DD HH:mm') || '—' }}</dd>
              </div>
              <div class="overview-row">
                <dt>部署时间</dt>
                <dd class="mono">
                  {{ appInfo?.deployedTime ? formatTime(appInfo.deployedTime, 'YYYY-MM-DD HH:mm') : '未部署' }}
                </dd>
              </div>
            </dl>

            <div class="overview-actions">
              <a-button v-if="appInfo?.deployKey" block @click="openPreview">
                <template #icon><ExportOutlined /></template>
                查看线上预览
              </a-button>
              <a-button v-else block type="primary" ghost @click="goToChat">
                <template #icon><CloudUploadOutlined /></template>
                去部署应用
              </a-button>
            </div>
          </div>
        </div>

        <p class="aside-tip">
          <BulbOutlined />
          保存后刷新页面即可看到最新应用信息
        </p>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  BulbOutlined,
  CloudUploadOutlined,
  EditOutlined,
  ExportOutlined,
  LockOutlined,
  MessageOutlined,
  SaveOutlined,
  StarFilled,
} from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/api/appController'
import { formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'
import { getStaticPreviewUrl } from '@/config/env'
import type { FormInstance } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用信息
const appInfo = ref<API.AppVO>()
const loading = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  appName: '',
  cover: '',
  priority: 0,
  initPrompt: '',
  codeGenType: '',
  deployKey: '',
})

// 是否为管理员
const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin'
})

// 右侧封面预览
const previewInitial = computed(() =>
  (formData.appName || 'A').trim().charAt(0).toUpperCase(),
)

const previewCoverStyle = computed(() => {
  const seed = Number(appInfo.value?.id) || 1
  const hue = (seed * 53 + 210) % 360
  const second = (hue + 42) % 360
  return {
    background: `radial-gradient(120% 120% at 18% 12%, hsl(${second} 96% 94%) 0%, transparent 55%), linear-gradient(140deg, hsl(${hue} 92% 96%) 0%, hsl(${second} 86% 89%) 100%)`,
  }
})

// 表单验证规则
const rules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 1, max: 50, message: '应用名称长度在 1-50 个字符', trigger: 'blur' },
  ],
  cover: [{ type: 'url', message: '请输入有效的 URL', trigger: 'blur' }],
  priority: [{ type: 'number', min: 0, max: 99, message: '优先级范围 0-99', trigger: 'blur' }],
}

// 获取应用信息
const fetchAppInfo = async () => {
  const id = route.params.id as string
  if (!id) {
    message.error('应用ID不存在')
    router.push('/')
    return
  }

  loading.value = true
  try {
    const res = await getAppVoById({ id: id as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data

      // 检查权限
      if (!isAdmin.value && appInfo.value.userId !== loginUserStore.loginUser.id) {
        message.error('您没有权限编辑此应用')
        router.push('/')
        return
      }

      // 填充表单数据
      formData.appName = appInfo.value.appName || ''
      formData.cover = appInfo.value.cover || ''
      formData.priority = appInfo.value.priority || 0
      formData.initPrompt = appInfo.value.initPrompt || ''
      formData.codeGenType = appInfo.value.codeGenType || ''
      formData.deployKey = appInfo.value.deployKey || ''
    } else {
      message.error('获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取应用信息失败：', error)
    message.error('获取应用信息失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!appInfo.value?.id) return

  submitting.value = true
  try {
    let res
    if (isAdmin.value) {
      // 管理员可以修改更多字段
      res = await updateAppByAdmin({
        id: appInfo.value.id,
        appName: formData.appName,
        cover: formData.cover,
        priority: formData.priority,
      })
    } else {
      // 普通用户只能修改应用名称
      res = await updateApp({
        id: appInfo.value.id,
        appName: formData.appName,
      })
    }

    if (res.data.code === 0) {
      message.success('修改成功')
      await fetchAppInfo()
    } else {
      message.error('修改失败：' + res.data.message)
    }
  } catch (error) {
    console.error('修改失败：', error)
    message.error('修改失败')
  } finally {
    submitting.value = false
  }
}

// 顶部保存按钮：先校验再提交
const submitForm = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  await handleSubmit()
}

// 重置表单
const resetForm = () => {
  if (appInfo.value) {
    formData.appName = appInfo.value.appName || ''
    formData.cover = appInfo.value.cover || ''
    formData.priority = appInfo.value.priority || 0
  }
  formRef.value?.clearValidate()
}

// 进入对话页面
const goToChat = () => {
  if (appInfo.value?.id) {
    router.push(`/app/chat/${appInfo.value.id}`)
  }
}

// 打开预览
const openPreview = () => {
  if (appInfo.value?.codeGenType && appInfo.value?.id) {
    const url = getStaticPreviewUrl(appInfo.value.codeGenType, String(appInfo.value.id))
    window.open(url, '_blank')
  }
}

// 页面加载时获取应用信息
onMounted(() => {
  fetchAppInfo()
})
</script>

<style scoped>
.edit-page {
  max-width: 1120px;
}

.back-link {
  display: flex;
  width: fit-content;
  align-items: center;
  gap: 5px;
  margin-bottom: 12px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--muted);
  font-family: inherit;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s ease;
}

.back-link:hover {
  color: var(--brand-600);
}

.page-head__text .eyebrow {
  margin-top: 2px;
}

.edit-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 20px;
  align-items: start;
}

.edit-card {
  border-radius: var(--radius-lg);
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  font-weight: 650;
}

.card-title__icon {
  display: grid;
  place-items: center;
  width: 27px;
  height: 27px;
  border-radius: 8px;
  font-size: 12px;
  color: var(--brand-600);
  background: var(--brand-50);
}

.cover-preview {
  margin-top: 12px;
  padding: 12px;
  border-radius: var(--radius-md);
  border: 1px dashed var(--line-strong);
  background: var(--surface-soft);
}

.form-divider {
  height: 1px;
  margin: 6px 0 22px;
  background: var(--line);
}

.form-tip {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 6px;
  color: var(--muted-2);
  font-size: 11.5px;
}

.form-tip .anticon {
  font-size: 10px;
}

.form-actions {
  display: flex;
  gap: 10px;
  padding-top: 4px;
}

/* ---------------------------------------------------------------- overview */
.edit-aside {
  position: sticky;
  top: calc(var(--header-height) + 20px);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.overview-card {
  overflow: hidden;
}

.overview-cover {
  position: relative;
  display: grid;
  place-items: center;
  height: 148px;
  overflow: hidden;
}

.overview-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.overview-initial {
  font-size: 52px;
  font-weight: 800;
  letter-spacing: -0.04em;
  color: rgba(49, 46, 129, 0.55);
}

.overview-body {
  padding: 16px 18px 18px;
}

.overview-name {
  margin: 0 0 8px;
  font-size: 16.5px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.overview-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.overview-divider {
  height: 1px;
  margin: 16px 0 14px;
  background: var(--line);
}

.overview-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 0;
}

.overview-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 12px;
}

.overview-row dt {
  flex-shrink: 0;
  color: var(--muted-2);
}

.overview-row dd {
  margin: 0;
  min-width: 0;
  color: var(--ink-3);
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.overview-row dd.mono {
  font-family: var(--font-mono);
  font-size: 11px;
}

.overview-actions {
  margin-top: 18px;
}

.aside-tip {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin: 0;
  padding: 0 6px;
  color: var(--muted-2);
  font-size: 11px;
  line-height: 1.6;
}

.aside-tip .anticon {
  margin-top: 2px;
  color: var(--amber);
  flex-shrink: 0;
}

:deep(.ant-form-item-label > label) {
  color: var(--ink-3);
  font-weight: 550;
}

:deep(.ant-descriptions-item-label) {
  background: var(--surface-soft);
  font-weight: 500;
}

@media (max-width: 980px) {
  .edit-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .edit-aside {
    position: static;
    order: -1;
  }

  .overview-card {
    display: grid;
    grid-template-columns: 190px 1fr;
  }

  .overview-cover {
    height: 100%;
    min-height: 170px;
  }
}

@media (max-width: 640px) {
  .overview-card {
    display: block;
  }

  .overview-cover {
    min-height: 130px;
    height: 130px;
  }

  .page-head__aside {
    width: 100%;
  }
}
</style>
