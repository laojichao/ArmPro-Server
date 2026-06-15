<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSoftList,
  getSoftDetail,
  deleteSoft,
  type AdminSoft,
  type SoftListParams,
} from '@/api/admin'

const loading = ref(false)
const tableData = ref<AdminSoft[]>([])
const total = ref(0)
const detailDialogVisible = ref(false)
const currentSoft = ref<AdminSoft | null>(null)

const queryParams = reactive<SoftListParams>({
  page: 1,
  pageSize: 10,
  softName: '',
  packageName: '',
  username: '',
})

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getSoftList(queryParams)
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('获取应用列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  queryParams.softName = ''
  queryParams.packageName = ''
  queryParams.username = ''
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

async function handleViewDetail(row: AdminSoft) {
  try {
    const res = await getSoftDetail(row.id)
    currentSoft.value = res
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取应用详情失败:', error)
  }
}

async function handleDelete(row: AdminSoft) {
  try {
    await ElMessageBox.confirm(
      `确定要删除应用 "${row.softName}" 吗？此操作将同时删除该应用的所有配置和卡密数据，且不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    await deleteSoft(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除应用失败:', error)
    }
  }
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
        <el-form-item label="应用名">
          <el-input v-model="queryParams.softName" placeholder="请输入应用名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="包名">
          <el-input v-model="queryParams.packageName" placeholder="请输入包名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px;">
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="softName" label="应用名称" min-width="150" />
        <el-table-column prop="appKey" label="AppKey" min-width="200" show-overflow-tooltip />
        <el-table-column prop="packageName" label="包名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="username" label="所属用户" width="120" />
        <el-table-column prop="totalUser" label="总用户数" width="100" align="center" />
        <el-table-column prop="todayUser" label="今日活跃" width="100" align="center" />
        <el-table-column label="创建时间" width="170" align="center">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
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

    <el-dialog v-model="detailDialogVisible" title="应用详情" width="600px">
      <el-descriptions v-if="currentSoft" :column="2" border>
        <el-descriptions-item label="ID">{{ currentSoft.id }}</el-descriptions-item>
        <el-descriptions-item label="应用名称">{{ currentSoft.softName }}</el-descriptions-item>
        <el-descriptions-item label="AppKey" :span="2">{{ currentSoft.appKey }}</el-descriptions-item>
        <el-descriptions-item label="包名" :span="2">{{ currentSoft.packageName }}</el-descriptions-item>
        <el-descriptions-item label="所属用户">{{ currentSoft.username }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentSoft.userId }}</el-descriptions-item>
        <el-descriptions-item label="总用户数">{{ currentSoft.totalUser }}</el-descriptions-item>
        <el-descriptions-item label="今日活跃">{{ currentSoft.todayUser }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(currentSoft.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentSoft.status === 1 ? 'success' : 'danger'" size="small">
            {{ currentSoft.status === 1 ? '正常' : '已禁用' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
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
</style>
