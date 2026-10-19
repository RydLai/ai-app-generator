<template>
  <div class="page-shell">
    <header class="page-head">
      <div class="page-head__text">
        <span class="eyebrow">Admin · Applications</span>
        <h1>应用管理</h1>
        <p>浏览平台生成的应用，设置精选并管理部署状态。</p>
      </div>
      <div class="page-head__aside">
        <span class="stat-chip">
          共 <strong>{{ total }}</strong> 个应用
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
        <a-form-item label="应用名称">
          <a-input
            v-model:value="searchParams.appName"
            placeholder="输入应用名称"
            allow-clear
            style="width: 180px"
          >
            <template #prefix><AppstoreOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item label="创建者">
          <a-input
            v-model:value="searchParams.userId"
            placeholder="输入用户 ID"
            allow-clear
            style="width: 150px"
          >
            <template #prefix><UserOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item label="生成类型">
          <a-select
            v-model:value="searchParams.codeGenType"
            placeholder="全部类型"
            style="width: 168px"
            allow-clear
          >
            <a-select-option value="">全部</a-select-option>
            <a-select-option
              v-for="option in CODE_GEN_TYPE_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
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
        :scroll="{ x: 1164 }"
        row-key="id"
        size="middle"
        @change="doTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'appName'">
            <div class="app-cell">
              <a-image
                v-if="record.cover"
                :src="record.cover"
                :width="58"
                :height="42"
                class="app-thumb"
                :preview="false"
              />
              <div v-else class="app-thumb app-thumb--empty">{{ appInitial(record) }}</div>
              <div class="cell-stack">
                <span class="cell-primary">{{ record.appName || '未命名应用' }}</span>
                <span class="cell-sub">ID · {{ record.id }}</span>
              </div>
            </div>
          </template>
          <template v-else-if="column.dataIndex === 'initPrompt'">
            <a-tooltip :title="record.initPrompt">
              <span class="prompt-text">{{ record.initPrompt || '—' }}</span>
            </a-tooltip>
          </template>
          <template v-else-if="column.dataIndex === 'codeGenType'">
            <span class="meta-pill meta-pill--slate">
              {{ formatCodeGenType(record.codeGenType) }}
            </span>
          </template>
          <template v-else-if="column.dataIndex === 'priority'">
            <span v-if="record.priority === 99" class="meta-pill meta-pill--amber">
              <StarFilled />
              精选
            </span>
            <span v-else class="cell-muted">{{ record.priority || 0 }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'deployedTime'">
            <span v-if="record.deployedTime" class="meta-pill meta-pill--green">
              <i aria-hidden="true"></i>
              已部署
            </span>
            <span v-else class="cell-muted">未部署</span>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            <span class="cell-time">{{ formatTime(record.createTime, 'YYYY-MM-DD HH:mm') }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'user'">
            <UserInfo :user="record.user" size="small" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space :size="2">
              <a-button size="small" type="text" class="row-action" @click="editApp(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-button
                size="small"
                type="text"
                class="row-action"
                :class="{ 'is-featured': record.priority === 99 }"
                @click="toggleFeatured(record)"
              >
                <StarFilled v-if="record.priority === 99" />
                <StarOutlined v-else />
                {{ record.priority === 99 ? '取消' : '精选' }}
              </a-button>
              <a-popconfirm
                title="确定要删除这个应用吗？"
                ok-text="删除"
                cancel-text="取消"
                @confirm="deleteApp(record.id)"
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
  EditOutlined,
  ReloadOutlined,
  SearchOutlined,
  StarFilled,
  StarOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { listAppVoByPageByAdmin, deleteAppByAdmin, updateAppByAdmin } from '@/api/appController'
import { CODE_GEN_TYPE_OPTIONS, formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'

const router = useRouter()

const columns = [
  {
    title: '应用',
    dataIndex: 'appName',
    width: 200,
    fixed: 'left',
  },
  {
    title: '初始提示词',
    dataIndex: 'initPrompt',
    width: 210,
  },
  {
    title: '生成类型',
    dataIndex: 'codeGenType',
    width: 110,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 88,
  },
  {
    title: '部署状态',
    dataIndex: 'deployedTime',
    width: 96,
  },
  {
    title: '创建者',
    dataIndex: 'user',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 150,
  },
  {
    title: '操作',
    key: 'action',
    width: 190,
    fixed: 'right',
    align: 'right' as const,
  },
]

// 数据
const data = ref<API.AppVO[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索条件
const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

const appInitial = (app: API.AppVO) => (app.appName || 'A').trim().charAt(0).toUpperCase()

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAppVoByPageByAdmin({
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
  searchParams.appName = undefined
  searchParams.userId = undefined
  searchParams.codeGenType = undefined
  searchParams.pageNum = 1
  fetchData()
}

// 编辑应用
const editApp = (app: API.AppVO) => {
  router.push(`/app/edit/${app.id}`)
}

// 切换精选状态
const toggleFeatured = async (app: API.AppVO) => {
  if (!app.id) return

  const newPriority = app.priority === 99 ? 0 : 99

  try {
    const res = await updateAppByAdmin({
      id: app.id,
      priority: newPriority,
    })

    if (res.data.code === 0) {
      message.success(newPriority === 99 ? '已设为精选' : '已取消精选')
      fetchData()
    } else {
      message.error('操作失败：' + res.data.message)
    }
  } catch (error) {
    console.error('操作失败：', error)
    message.error('操作失败')
  }
}

// 删除应用
const deleteApp = async (id: number | undefined) => {
  if (!id) return

  try {
    const res = await deleteAppByAdmin({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      fetchData()
    } else {
      message.error('删除失败：' + res.data.message)
    }
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

.app-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.app-thumb {
  flex-shrink: 0;
  border-radius: 9px;
  overflow: hidden;
  border: 1px solid var(--line);
}

.app-thumb--empty {
  display: grid;
  place-items: center;
  width: 58px;
  height: 42px;
  color: var(--brand-600);
  font-size: 18px;
  font-weight: 700;
  background:
    radial-gradient(circle at 30% 20%, rgba(99, 102, 241, 0.16), transparent 70%),
    var(--brand-50);
}

.cell-stack {
  display: flex;
  flex-direction: column;
  min-width: 0;
  line-height: 1.35;
}

.cell-primary {
  color: var(--ink);
  font-weight: 550;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px;
}

.cell-sub {
  color: var(--muted-2);
  font-family: var(--font-mono);
  font-size: 10.5px;
}

.cell-muted {
  color: var(--muted-2);
  font-size: 12px;
}

.cell-time {
  color: var(--muted);
  font-family: var(--font-mono);
  font-size: 11.5px;
}

.prompt-text {
  display: block;
  max-width: 230px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--muted);
  font-size: 12.5px;
}

.row-action {
  border-radius: 8px;
  font-size: 12px;
}

.row-action.is-featured {
  color: #b45309;
}

@media (max-width: 640px) {
  .refresh-btn {
    display: none;
  }
}
</style>
