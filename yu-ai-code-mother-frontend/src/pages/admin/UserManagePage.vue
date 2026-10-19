<template>
  <div class="page-shell">
    <header class="page-head">
      <div class="page-head__text">
        <span class="eyebrow">Admin · Users</span>
        <h1>用户管理</h1>
        <p>检索平台注册用户，查看账号资料与权限角色。</p>
      </div>
      <div class="page-head__aside">
        <span class="stat-chip">
          共 <strong>{{ total }}</strong> 位用户
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
        <a-form-item label="账号">
          <a-input
            v-model:value="searchParams.userAccount"
            placeholder="输入账号"
            allow-clear
            style="width: 190px"
          >
            <template #prefix><UserOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item label="用户名">
          <a-input
            v-model:value="searchParams.userName"
            placeholder="输入用户名"
            allow-clear
            style="width: 190px"
          >
            <template #prefix><IdcardOutlined /></template>
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
        :scroll="{ x: 900 }"
        row-key="id"
        size="middle"
        @change="doTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'userAvatar'">
            <a-avatar :src="record.userAvatar" :size="38" class="table-avatar">
              {{ record.userName?.charAt(0) || 'U' }}
            </a-avatar>
          </template>
          <template v-else-if="column.dataIndex === 'userAccount'">
            <div class="cell-stack">
              <span class="cell-primary">{{ record.userAccount }}</span>
              <span class="cell-sub">ID · {{ record.id }}</span>
            </div>
          </template>
          <template v-else-if="column.dataIndex === 'userName'">
            <span class="cell-primary">{{ record.userName || '未设置' }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'userProfile'">
            <a-tooltip :title="record.userProfile">
              <span class="cell-profile">{{ record.userProfile || '这个人很懒，什么都没写' }}</span>
            </a-tooltip>
          </template>
          <template v-else-if="column.dataIndex === 'userRole'">
            <span
              class="meta-pill"
              :class="record.userRole === 'admin' ? 'meta-pill--green' : 'meta-pill--blue'"
            >
              <i aria-hidden="true"></i>
              {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
            </span>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            <span class="cell-time">{{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm') }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-popconfirm
              title="确定要删除该用户吗？"
              ok-text="删除"
              cancel-text="取消"
              @confirm="doDelete(record.id)"
            >
              <a-button size="small" type="text" danger class="row-action">
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  DeleteOutlined,
  IdcardOutlined,
  ReloadOutlined,
  SearchOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { deleteUser, listUserVoByPage } from '@/api/userController.ts'

const columns = [
  {
    title: '头像',
    dataIndex: 'userAvatar',
    width: 76,
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    width: 180,
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    width: 140,
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 170,
  },
  {
    title: '操作',
    key: 'action',
    width: 110,
    align: 'right' as const,
  },
]

// 展示的数据
const data = ref<API.UserVO[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listUserVoByPage({
      ...searchParams,
    })
    if (res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error) {
    console.error('获取用户数据失败：', error)
    message.error('获取数据失败，请重试')
  } finally {
    loading.value = false
  }
}

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

// 表格分页变化时的操作
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

// 重置搜索条件
const doReset = () => {
  searchParams.userAccount = undefined
  searchParams.userName = undefined
  searchParams.pageNum = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: number | undefined) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.refresh-btn {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  color: var(--ink-3);
}

.table-avatar {
  background: linear-gradient(135deg, var(--brand-400), var(--violet));
  font-weight: 600;
}

.cell-stack {
  display: flex;
  flex-direction: column;
  line-height: 1.35;
}

.cell-primary {
  color: var(--ink);
  font-weight: 550;
}

.cell-sub {
  color: var(--muted-2);
  font-family: var(--font-mono);
  font-size: 10.5px;
}

.cell-profile {
  display: block;
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--muted);
  font-size: 12.5px;
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
