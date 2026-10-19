<template>
  <div class="page-shell">
    <header class="page-head">
      <div class="page-head__text">
        <span class="eyebrow">Admin · Conversations</span>
        <h1>对话管理</h1>
        <p>查看平台产生的对话记录，追踪应用生成过程。</p>
      </div>
      <div class="page-head__aside">
        <span class="stat-chip">
          共 <strong>{{ total }}</strong> 条消息
        </span>
        <a-tooltip title="刷新列表">
          <a-button class="refresh-btn" :loading="loading" @click="fetchData">
            <template #icon><ReloadOutlined /></template>
          </a-button>
        </a-tooltip>
      </div>
    </header>

    <!-- 搜索表单 -->
    <div class="panel filter-panel">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="消息内容">
          <a-input
            v-model:value="searchParams.message"
            placeholder="输入消息内容"
            allow-clear
            style="width: 200px"
          >
            <template #prefix><MessageOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item label="消息类型">
          <a-select
            v-model:value="searchParams.messageType"
            placeholder="全部类型"
            style="width: 150px"
            allow-clear
          >
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="user">用户消息</a-select-option>
            <a-select-option value="assistant">AI 消息</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="应用 ID">
          <a-input
            v-model:value="searchParams.appId"
            placeholder="输入应用 ID"
            allow-clear
            style="width: 140px"
          >
            <template #prefix><AppstoreOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item label="用户 ID">
          <a-input
            v-model:value="searchParams.userId"
            placeholder="输入用户 ID"
            allow-clear
            style="width: 140px"
          >
            <template #prefix><UserOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="doReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 表格 -->
    <div class="panel table-panel">
      <a-table
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        :loading="loading"
        :scroll="{ x: 1120 }"
        row-key="id"
        size="middle"
        @change="doTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'message'">
            <a-tooltip :title="record.message">
              <div class="message-cell">
                <span
                  class="message-type-dot"
                  :class="record.messageType === 'user' ? 'is-user' : 'is-ai'"
                  aria-hidden="true"
                ></span>
                <span class="message-text">{{ record.message }}</span>
              </div>
            </a-tooltip>
          </template>
          <template v-else-if="column.dataIndex === 'messageType'">
            <span
              class="meta-pill"
              :class="record.messageType === 'user' ? 'meta-pill--blue' : 'meta-pill--violet'"
            >
              {{ record.messageType === 'user' ? '用户消息' : 'AI 消息' }}
            </span>
          </template>
          <template v-else-if="column.dataIndex === 'appId'">
            <button type="button" class="app-link" @click="viewAppChat(record.appId)">
              #{{ record.appId }}
            </button>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            <span class="cell-time">{{ formatTime(record.createTime, 'YYYY-MM-DD HH:mm') }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space :size="2">
              <a-button size="small" type="text" class="row-action" @click="viewAppChat(record.appId)">
                <template #icon><ExportOutlined /></template>
                查看
              </a-button>
              <a-popconfirm
                title="确定要删除这条消息吗？"
                ok-text="删除"
                cancel-text="取消"
                @confirm="deleteMessage(record.id)"
              >
                <a-button size="small" type="text" danger class="row-action">
                  <template #icon><DeleteOutlined /></template>
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  AppstoreOutlined,
  DeleteOutlined,
  ExportOutlined,
  MessageOutlined,
  ReloadOutlined,
  SearchOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController'
import { formatTime } from '@/utils/time'

const router = useRouter()

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 84,
    fixed: 'left',
  },
  {
    title: '消息内容',
    dataIndex: 'message',
    width: 380,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    width: 120,
  },
  {
    title: '应用 ID',
    dataIndex: 'appId',
    width: 100,
  },
  {
    title: '用户 ID',
    dataIndex: 'userId',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 170,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right',
    align: 'right' as const,
  },
]

// 数据
const data = ref<API.ChatHistory[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索条件
const searchParams = reactive<API.ChatHistoryQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAllChatHistoryByPageForAdmin({
      ...searchParams,
    })
    if (res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error) {
    console.error('获取数据失败：', error)
    message.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化处理
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

// 重置搜索条件
const doReset = () => {
  searchParams.message = undefined
  searchParams.messageType = undefined
  searchParams.appId = undefined
  searchParams.userId = undefined
  searchParams.pageNum = 1
  fetchData()
}

// 查看应用对话
const viewAppChat = (appId: number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}`)
  }
}

// 删除消息
const deleteMessage = async (id: number | undefined) => {
  if (!id) return

  try {
    // 注意：这里需要后端提供删除对话历史的接口
    // 目前先显示成功，实际实现需要调用删除接口
    message.success('删除成功')
    fetchData()
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}
</script>

<style scoped>
.refresh-btn {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  color: var(--ink-3);
}

.message-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.message-type-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.message-type-dot.is-user {
  background: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12);
}

.message-type-dot.is-ai {
  background: var(--violet);
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.12);
}

.message-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--ink-2);
  font-size: 12.5px;
  display: block;
  max-width: 340px;
}

.app-link {
  border: 0;
  background: transparent;
  padding: 2px 6px;
  border-radius: 6px;
  color: var(--brand-600);
  font-family: var(--font-mono);
  font-size: 11.5px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}

.app-link:hover {
  background: var(--brand-50);
}

.cell-time {
  color: var(--muted);
  font-family: var(--font-mono);
  font-size: 11.5px;
}

.row-action {
  border-radius: 8px;
  font-size: 12px;
}

@media (max-width: 640px) {
  .refresh-btn {
    display: none;
  }
}
</style>
