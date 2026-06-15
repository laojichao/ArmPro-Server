<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserList,
  getUserDetail,
  banUser,
  unbanUser,
  type AdminUser,
  type UserListParams,
} from '@/api/admin'

const loading = ref(false)
const tableData = ref<AdminUser[]>([])
const total = ref(0)
const detailDialogVisible = ref(false)
const currentUser = ref<AdminUser | null>(null)

const queryParams = reactive<UserListParams>({
  page: 1,
  pageSize: 10,
  username: '',
  email: '',
})

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  queryParams.username = ''
  queryParams.email = ''
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

async function handleViewDetail(row: AdminUser) {
  try {
    const res = await getUserDetail(row.id)
    currentUser.value = res
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取用户详情失败:', error)
  }
}

async function handleBan(row: AdminUser) {
  try {
    await ElMessageBox.confirm(`确定要封禁用户 "${row.username}" 吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await banUser(row.id)
    ElMessage.success('封禁成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('封禁用户失败:', error)
    }
  }
}

async function handleUnban(row: AdminUser) {
  try {
    await ElMessageBox.confirm(`确定要解封用户 "${row.username}" 吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    await unbanUser(row.id)
    ElMessage.success('解封成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解封用户失败:', error)
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
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="queryParams.email" placeholder="请输入邮箱" clearable @keyup.enter="handleSearch" />
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
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="注册时间" min-width="170" align="center">
          <template #default="{ row }">{{ formatDate(row.regTime) }}</template>
        </el-table-column>
        <el-table-column prop="loginCount" label="登录次数" width="100" align="center" />
        <el-table-column label="VIP状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isVip ? 'success' : 'info'" size="small">
              {{ row.isVip ? 'VIP' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isBanned ? 'danger' : 'success'" size="small">
              {{ row.isBanned ? '已封禁' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-button v-if="!row.isBanned" type="danger" link size="small" @click="handleBan(row)">
              <el-icon><Lock /></el-icon>封禁
            </el-button>
            <el-button v-else type="success" link size="small" @click="handleUnban(row)">
              <el-icon><Unlock /></el-icon>解封
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

    <el-dialog v-model="detailDialogVisible" title="用户详情" width="600px">
      <el-descriptions v-if="currentUser" :column="2" border>
        <el-descriptions-item label="ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
        <el-descriptions-item label="登录次数">{{ currentUser.loginCount }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDate(currentUser.regTime) }}</el-descriptions-item>
        <el-descriptions-item label="VIP到期时间">{{ formatDate(currentUser.vipExpire) }}</el-descriptions-item>
        <el-descriptions-item label="VIP状态">
          <el-tag :type="currentUser.isVip ? 'success' : 'info'" size="small">
            {{ currentUser.isVip ? 'VIP用户' : '普通用户' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="账号状态">
          <el-tag :type="currentUser.isBanned ? 'danger' : 'success'" size="small">
            {{ currentUser.isBanned ? '已封禁' : '正常' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最后登录时间">{{ formatDate(currentUser.lastLoginTime) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录IP">{{ currentUser.lastLoginIp || '-' }}</el-descriptions-item>
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
