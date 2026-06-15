<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTaskList,
  stopTask,
  type AdminTask,
  type TaskListParams,
} from '@/api/admin'

const loading = ref(false)
const tableData = ref<AdminTask[]>([])
const total = ref(0)

const queryParams = reactive<TaskListParams>({
  page: 1,
  pageSize: 10,
  status: '',
  username: '',
  uuid: '',
})

const statusOptions = [
  { label: '全部', value: '' },
  { label: '运行中', value: 'running' },
  { label: '已完成', value: 'completed' },
  { label: '失败', value: 'failed' },
  { label: '已终止', value: 'stopped' },
]

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getTaskList(queryParams)
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('获取任务列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  queryParams.status = ''
  queryParams.username = ''
  queryParams.uuid = ''
  queryParams.page = 1
  fetchData()
}

function handlePageChange(page: number) {
  queryParams.page = page
  fetchData()
}

function handleSizeChange(size: number) {
  queryParams.pageSize = size
  queryParams.page = 1
  fetchData()
}

async function handleStopTask(row: AdminTask) {
  try {
    await ElMessageBox.confirm(
      `确定要终止任务 "${row.uuid}" 吗？此操作不可恢复！`,
      '确认终止',
      {
        confirmButtonText: '确定终止',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    await stopTask(row.uuid)
    ElMessage.success('任务已终止')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('终止任务失败:', error)
    }
  }
}

function getStatusType(status: string) {
  const map: Record<string, string> = {
    running: 'primary',
    completed: 'success',
    failed: 'danger',
    stopped: 'info',
  }
  return map[status] || 'info'
}

function getStatusLabel(status: string) {
  const map: Record<string, string> = {
    running: '运行中',
    completed: '已完成',
    failed: '失败',
    stopped: '已终止',
  }
  return map[status] || status
}

function formatDate(date: string | null) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="任务ID">
          <el-input v-model="queryParams.uuid" placeholder="请输入任务UUID" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="用户">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px;">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button @click="fetchData">
            <el-icon><RefreshRight /></el-icon>刷新
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px;">
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%;">
        <el-table-column prop="uuid" label="任务ID" min-width="280" show-overflow-tooltip />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="softName" label="应用" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="150" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :stroke-width="8" :status="row.status === 'completed' ? 'success' : row.status === 'failed' ? 'exception' : undefined" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170" align="center">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="170" align="center">
          <template #default="{ row }">{{ formatDate(row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'running'"
              type="danger"
              link
              size="small"
              @click="handleStopTask(row)"
            >
              <el-icon><Close /></el-icon>终止
            </el-button>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page-container {
  padding: 0;
}
.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.text-muted {
  color: #c0c4cc;
}
</style>
