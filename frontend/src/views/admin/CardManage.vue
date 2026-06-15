<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getCardList,
  generateCards,
  getCardStats,
  type AdminCard,
  type CardListParams,
  type GenerateCardParams,
  type CardStats,
} from '@/api/admin'

const loading = ref(false)
const tableData = ref<AdminCard[]>([])
const total = ref(0)
const stats = ref<CardStats>({
  total: 0,
  used: 0,
  unused: 0,
  todayGenerated: 0,
  todayUsed: 0,
})
const generateDialogVisible = ref(false)
const generatedCards = ref<AdminCard[]>([])

const queryParams = reactive<CardListParams>({
  page: 1,
  pageSize: 10,
  card: '',
  type: undefined,
  usable: undefined,
})

const generateForm = reactive<GenerateCardParams>({
  count: 10,
  type: 1,
  value: 1,
  mark: '',
  softId: 0,
})

const cardTypeOptions = [
  { label: '全部', value: undefined },
  { label: '月卡', value: 1 },
  { label: '年卡', value: 2 },
  { label: '试用卡', value: 3 },
]

const generateTypeOptions = [
  { label: '月卡', value: 1 },
  { label: '年卡', value: 2 },
  { label: '试用卡', value: 3 },
]

onMounted(() => {
  fetchData()
  fetchStats()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getCardList(queryParams)
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('获取卡密列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function fetchStats() {
  try {
    stats.value = await getCardStats()
  } catch (error) {
    console.error('获取卡密统计失败:', error)
  }
}

function handleSearch() {
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  queryParams.card = ''
  queryParams.type = undefined
  queryParams.usable = undefined
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

function openGenerateDialog() {
  generateForm.count = 10
  generateForm.type = 1
  generateForm.value = 1
  generateForm.mark = ''
  generatedCards.value = []
  generateDialogVisible.value = true
}

async function handleGenerate() {
  if (generateForm.count <= 0 || generateForm.count > 99) {
    ElMessage.warning('生成数量必须在 1-99 之间')
    return
  }
  if (generateForm.value <= 0 || generateForm.value > 99) {
    ElMessage.warning('面值必须在 1-99 之间')
    return
  }
  try {
    const res = await generateCards(generateForm)
    generatedCards.value = res
    ElMessage.success(`成功生成 ${res.length} 张卡密`)
    fetchData()
    fetchStats()
  } catch (error) {
    console.error('生成卡密失败:', error)
  }
}

function getCardTypeName(type: number) {
  const map: Record<number, string> = { 1: '月卡', 2: '年卡', 3: '试用卡' }
  return map[type] || '未知'
}

function copyCards() {
  const text = generatedCards.value.map((c) => c.card).join('\n')
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('卡密已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}
</script>

<template>
  <div class="page-container">
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">卡密总数</div>
          <div class="stat-value">{{ stats.total }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">已使用</div>
          <div class="stat-value" style="color: #e6a23c;">{{ stats.used }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">未使用</div>
          <div class="stat-value" style="color: #67c23a;">{{ stats.unused }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">今日生成/使用</div>
          <div class="stat-value">{{ stats.todayGenerated }} / {{ stats.todayUsed }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 16px;">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="卡密">
          <el-input v-model="queryParams.card" placeholder="请输入卡密" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="全部" clearable style="width: 120px;">
            <el-option v-for="item in cardTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.usable" placeholder="全部" clearable style="width: 120px;">
            <el-option label="未使用" :value="true" />
            <el-option label="已使用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button type="success" @click="openGenerateDialog">
            <el-icon><Plus /></el-icon>批量生成
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px;">
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="card" label="卡密" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">{{ getCardTypeName(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="value" label="面值" width="80" align="center" />
        <el-table-column prop="mark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column prop="softName" label="所属应用" min-width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.usable ? 'success' : 'info'" size="small">
              {{ row.usable ? '未使用' : '已使用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="使用者" width="120" />
        <el-table-column label="使用时间" width="170" align="center">
          <template #default="{ row }">
            {{ row.useTime ? new Date(row.useTime).toLocaleString('zh-CN') : '-' }}
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

    <el-dialog v-model="generateDialogVisible" title="批量生成卡密" width="600px">
      <el-form :model="generateForm" label-width="100px">
        <el-form-item label="生成数量" required>
          <el-input-number v-model="generateForm.count" :min="1" :max="99" />
        </el-form-item>
        <el-form-item label="卡密类型" required>
          <el-radio-group v-model="generateForm.type">
            <el-radio v-for="item in generateTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面值" required>
          <el-input-number v-model="generateForm.value" :min="1" :max="99" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="generateForm.mark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <el-divider v-if="generatedCards.length > 0" content-position="left">生成结果</el-divider>
      <div v-if="generatedCards.length > 0" class="generated-cards">
        <el-input type="textarea" :rows="6" :model-value="generatedCards.map(c => c.card).join('\n')" readonly />
        <el-button type="primary" size="small" style="margin-top: 8px;" @click="copyCards">
          <el-icon><CopyDocument /></el-icon>复制卡密
        </el-button>
      </div>

      <template #footer>
        <el-button @click="generateDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleGenerate">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container {
  padding: 0;
}
.stat-row {
  margin-bottom: 0;
}
.stat-card {
  text-align: center;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
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
.generated-cards {
  margin-top: 8px;
}
</style>
