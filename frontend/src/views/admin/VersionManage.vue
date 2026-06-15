<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getVersionList,
  createVersion,
  updateVersion,
  deleteVersion,
  type AdminVersion,
  type VersionListParams,
} from '@/api/admin'

const loading = ref(false)
const tableData = ref<AdminVersion[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('发布新版本')
const isEdit = ref(false)
const editId = ref(0)

const queryParams = reactive<VersionListParams>({
  page: 1,
  pageSize: 10,
})

const versionForm = reactive({
  versionCode: 1,
  versionName: '',
  versionMsg: '',
  downloadUrl: '',
  isForce: false,
  status: 1,
})

const versionFormRef = ref()

const rules = {
  versionCode: [
    { required: true, message: '请输入版本号', trigger: 'blur' },
  ],
  versionName: [
    { required: true, message: '请输入版本名称', trigger: 'blur' },
  ],
  versionMsg: [
    { required: true, message: '请输入更新说明', trigger: 'blur' },
  ],
}

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getVersionList(queryParams)
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('获取版本列表失败:', error)
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
  dialogTitle.value = '发布新版本'
  isEdit.value = false
  editId.value = 0
  versionForm.versionCode = 1
  versionForm.versionName = ''
  versionForm.versionMsg = ''
  versionForm.downloadUrl = ''
  versionForm.isForce = false
  versionForm.status = 1
  dialogVisible.value = true
}

function openEditDialog(row: AdminVersion) {
  dialogTitle.value = '编辑版本'
  isEdit.value = true
  editId.value = row.id
  versionForm.versionCode = row.versionCode
  versionForm.versionName = row.versionName
  versionForm.versionMsg = row.versionMsg
  versionForm.downloadUrl = row.downloadUrl
  versionForm.isForce = row.isForce
  versionForm.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!versionFormRef.value) return
  try {
    await versionFormRef.value.validate()
    if (isEdit.value) {
      await updateVersion(editId.value, { ...versionForm })
      ElMessage.success('更新成功')
    } else {
      await createVersion({ ...versionForm })
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

async function handleDelete(row: AdminVersion) {
  try {
    await ElMessageBox.confirm(`确定要删除版本 "${row.versionName}" 吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteVersion(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除版本失败:', error)
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
          <span style="font-weight: bold;">版本管理</span>
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon>发布新版本
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="versionCode" label="版本号" width="100" align="center" />
        <el-table-column prop="versionName" label="版本名称" width="150" />
        <el-table-column prop="versionMsg" label="更新说明" min-width="250" show-overflow-tooltip />
        <el-table-column prop="downloadUrl" label="下载地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="强制更新" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isForce ? 'danger' : 'info'" size="small">
              {{ row.isForce ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
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
      <el-form ref="versionFormRef" :model="versionForm" :rules="rules" label-width="100px">
        <el-form-item label="版本号" prop="versionCode">
          <el-input-number v-model="versionForm.versionCode" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="版本名称" prop="versionName">
          <el-input v-model="versionForm.versionName" placeholder="例如：v1.0.0" />
        </el-form-item>
        <el-form-item label="更新说明" prop="versionMsg">
          <el-input
            v-model="versionForm.versionMsg"
            type="textarea"
            :rows="5"
            placeholder="请输入更新说明"
          />
        </el-form-item>
        <el-form-item label="下载地址">
          <el-input v-model="versionForm.downloadUrl" placeholder="请输入下载地址（可选）" />
        </el-form-item>
        <el-form-item label="强制更新">
          <el-switch v-model="versionForm.isForce" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="versionForm.status">
            <el-radio :value="1">已发布</el-radio>
            <el-radio :value="0">草稿</el-radio>
          </el-radio-group>
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
