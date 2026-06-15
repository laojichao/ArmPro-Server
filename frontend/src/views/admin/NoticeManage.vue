<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getNoticeList,
  createNotice,
  updateNotice,
  deleteNotice,
  type AdminNotice,
  type NoticeListParams,
} from '@/api/admin'

const loading = ref(false)
const tableData = ref<AdminNotice[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('发布公告')
const isEdit = ref(false)
const editId = ref(0)

const queryParams = reactive<NoticeListParams>({
  page: 1,
  pageSize: 10,
})

const noticeForm = reactive({
  title: '',
  content: '',
})

const noticeFormRef = ref()

const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { max: 100, message: '标题不能超过100个字符', trigger: 'blur' },
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
  ],
}

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getNoticeList(queryParams)
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('获取公告列表失败:', error)
  } finally {
    loading.value = false
  }
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

function openCreateDialog() {
  dialogTitle.value = '发布公告'
  isEdit.value = false
  editId.value = 0
  noticeForm.title = ''
  noticeForm.content = ''
  dialogVisible.value = true
}

function openEditDialog(row: AdminNotice) {
  dialogTitle.value = '编辑公告'
  isEdit.value = true
  editId.value = row.id
  noticeForm.title = row.title
  noticeForm.content = row.content
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!noticeFormRef.value) return
  try {
    await noticeFormRef.value.validate()
    if (isEdit.value) {
      await updateNotice(editId.value, { title: noticeForm.title, content: noticeForm.content })
      ElMessage.success('更新成功')
    } else {
      await createNotice({ title: noticeForm.title, content: noticeForm.content })
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== false) {
      console.error('操作失败:', error)
    }
  }
}

async function handleDelete(row: AdminNotice) {
  try {
    await ElMessageBox.confirm(`确定要删除公告 "${row.title}" 吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteNotice(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除公告失败:', error)
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
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold;">公告管理</span>
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon>发布公告
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="发布时间" width="170" align="center">
          <template #default="{ row }">{{ formatDate(row.time) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">
              <el-icon><Edit /></el-icon>编辑
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
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="noticeFormRef" :model="noticeForm" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="noticeForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="noticeForm.content"
            type="textarea"
            :rows="8"
            placeholder="请输入公告内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container {
  padding: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
