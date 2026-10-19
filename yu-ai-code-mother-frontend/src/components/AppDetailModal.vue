<template>
  <a-modal v-model:open="visible" :footer="null" :width="520" centered>
    <template #title>
      <div class="modal-title">
        <span class="modal-title__icon"><AppstoreOutlined /></span>
        <div class="modal-title__copy">
          <strong>应用详情</strong>
          <span>{{ app?.appName || '未命名应用' }}</span>
        </div>
      </div>
    </template>

    <div class="detail-hero">
      <div class="detail-cover" :style="coverStyle">
        <img v-if="app?.cover" :src="app.cover" :alt="app?.appName" />
        <span v-else>{{ appInitial }}</span>
      </div>
      <div class="detail-hero__text">
        <h3>{{ app?.appName || '未命名应用' }}</h3>
        <div class="detail-pills">
          <span class="meta-pill meta-pill--violet">
            {{ formatCodeGenType(app?.codeGenType) }}
          </span>
          <span
            class="meta-pill"
            :class="app?.deployedTime ? 'meta-pill--green' : 'meta-pill--slate'"
          >
            <i aria-hidden="true"></i>
            {{ app?.deployedTime ? '已部署' : '未部署' }}
          </span>
          <span v-if="app?.priority === 99" class="meta-pill meta-pill--amber">
            <StarFilled />
            精选
          </span>
        </div>
      </div>
    </div>

    <div class="detail-list">
      <div class="detail-row">
        <span class="detail-label"><UserOutlined />创建者</span>
        <span class="detail-value">
          <UserInfo :user="app?.user" size="small" />
        </span>
      </div>
      <div class="detail-row">
        <span class="detail-label"><ClockCircleOutlined />创建时间</span>
        <span class="detail-value mono">{{ formatTime(app?.createTime, 'YYYY-MM-DD HH:mm') || '—' }}</span>
      </div>
      <div class="detail-row">
        <span class="detail-label"><CloudUploadOutlined />部署时间</span>
        <span class="detail-value mono">
          {{ app?.deployedTime ? formatTime(app.deployedTime, 'YYYY-MM-DD HH:mm') : '暂未部署' }}
        </span>
      </div>
      <div class="detail-row">
        <span class="detail-label"><IdcardOutlined />应用 ID</span>
        <span class="detail-value mono">{{ app?.id ?? '—' }}</span>
      </div>
    </div>

    <div v-if="showActions" class="detail-actions">
      <a-button block @click="handleEdit">
        <template #icon><EditOutlined /></template>
        修改信息
      </a-button>
      <a-popconfirm
        title="确定要删除这个应用吗？"
        ok-text="确定"
        cancel-text="取消"
        @confirm="handleDelete"
      >
        <a-button block danger>
          <template #icon><DeleteOutlined /></template>
          删除应用
        </a-button>
      </a-popconfirm>
    </div>
    <div v-else class="detail-actions">
      <a-button block @click="visible = false">关闭</a-button>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  AppstoreOutlined,
  ClockCircleOutlined,
  CloudUploadOutlined,
  DeleteOutlined,
  EditOutlined,
  IdcardOutlined,
  StarFilled,
  UserOutlined,
} from '@ant-design/icons-vue'
import UserInfo from './UserInfo.vue'
import { formatTime } from '@/utils/time'
import { formatCodeGenType } from '../utils/codeGenTypes.ts'

interface Props {
  open: boolean
  app?: API.AppVO
  showActions?: boolean
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'edit'): void
  (e: 'delete'): void
}

const props = withDefaults(defineProps<Props>(), {
  showActions: false,
})

const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const appInitial = computed(() => (props.app?.appName || 'A').trim().charAt(0).toUpperCase())

const coverStyle = computed(() => {
  const seed = Number(props.app?.id) || 1
  const hue = (seed * 53 + 210) % 360
  const second = (hue + 42) % 360
  return {
    background: `radial-gradient(120% 120% at 18% 12%, hsl(${second} 96% 94%) 0%, transparent 55%), linear-gradient(140deg, hsl(${hue} 92% 96%) 0%, hsl(${second} 86% 89%) 100%)`,
    color: `hsl(${hue} 58% 42%)`,
  }
})

const handleEdit = () => {
  emit('edit')
}

const handleDelete = () => {
  emit('delete')
}
</script>

<style scoped>
.modal-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.modal-title__icon {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  color: var(--brand-600);
  background: var(--brand-50);
  box-shadow: inset 0 0 0 1px rgba(99, 102, 241, 0.12);
  font-size: 15px;
}

.modal-title__copy {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.modal-title__copy strong {
  font-size: 16px;
  color: var(--ink);
}

.modal-title__copy span {
  font-size: 11px;
  color: var(--muted-2);
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-hero {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  margin-bottom: 16px;
  border-radius: 16px;
  border: 1px solid var(--line);
  background: var(--surface-soft);
}

.detail-cover {
  display: grid;
  place-items: center;
  width: 74px;
  height: 74px;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 16px;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.04em;
  box-shadow: inset 0 0 0 1px rgba(15, 23, 42, 0.04);
}

.detail-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-hero__text {
  min-width: 0;
}

.detail-hero__text h3 {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-pills {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 4px 2px;
}

.detail-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 42px;
  padding: 6px 4px;
  border-bottom: 1px dashed var(--line);
}

.detail-row:last-child {
  border-bottom: 0;
}

.detail-label {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  flex-shrink: 0;
  color: var(--muted);
  font-size: 12.5px;
}

.detail-label .anticon {
  font-size: 12px;
  color: var(--muted-2);
}

.detail-value {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-width: 0;
  color: var(--ink-3);
  font-size: 12.5px;
}

.detail-value.mono {
  font-family: var(--font-mono);
  font-size: 11.5px;
}

.detail-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid var(--line);
}

.detail-actions > :only-child {
  grid-column: 1 / -1;
}
</style>
