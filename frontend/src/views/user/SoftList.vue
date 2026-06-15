<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSoftList, deleteSoft, type SoftInfo } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const tableData = ref<SoftInfo[]>([])
const total = ref(0)

const pagination = reactive({
  page: 1,
  pageSize: 10,
})

const searchKeyword = ref('')

async function fetchList() {
  loading.value = true
  try {
    const res = await getSoftList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value || undefined,
    })
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取应用列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchList()
}

function handlePageChange(page: number) {
  pagination.page = page
  fetchList()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchList()
}

function handleDetail(row: SoftInfo) {
  router.push(`/user/soft-detail/${row.id}`)
}

function handleConfig(row: SoftInfo) {
  router.push(`/user/soft-config/${row.id}`)
}

async function handleDelete(row: SoftInfo) {
  try {
    await ElMessageBox.confirm(`确定要删除应用「${row.name}」吗？此操作不可恢复。`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteSoft(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="soft-list-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold; font-size: 16px;">应用管理</span>
          <div class="search-bar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索应用名称"
              clearable
              style="width: 240px; margin-right: 12px;"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
            <el-button type="primary" @click="handleSearch">
              <el-icon style="margin-right: 4px;"><Search /></el-icon>
              搜索
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="应用名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="packageName" label="包名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column prop="appKey" label="AppKey" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
            <el-button type="success" link size="small" @click="handleConfig(row)">配置</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
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
.soft-list-container {
  max-width: 1400px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.search-bar {
  display: flex;
  align-items: center;
}
</style>
