<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCardList, generateCards, deleteCards, freezeCards, unfreezeCards, type CardInfo } from '@/api/user'

const loading = ref(false)
const generating = ref(false)
const tableData = ref<CardInfo[]>([])
const total = ref(0)
const selectedCards = ref<CardInfo[]>([])
const statusFilter = ref<number | undefined>(undefined)

const pagination = reactive({
  page: 1,
  pageSize: 10,
})

const searchKeyword = ref('')

const generateForm = reactive({
  count: 10,
  duration: 30,
  type: 'day',
  remark: '',
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getCardList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      status: statusFilter.value,
      keyword: searchKeyword.value || undefined,
    })
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取卡密列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchList()
}

function handleStatusChange() {
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

function handleSelectionChange(val: CardInfo[]) {
  selectedCards.value = val
}

async function handleGenerate() {
  if (generateForm.count <= 0) {
    ElMessage.warning('生成数量必须大于0')
    return
  }
  generating.value = true
  try {
    await generateCards(generateForm)
    ElMessage.success(`成功生成 ${generateForm.count} 张卡密`)
    fetchList()
  } catch (error) {
    ElMessage.error('生成卡密失败')
    console.error(error)
  } finally {
    generating.value = false
  }
}

async function handleBatchDelete() {
  if (selectedCards.value.length === 0) {
    ElMessage.warning('请先选择要删除的卡密')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedCards.value.length} 张卡密吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteCards(selectedCards.value.map((c) => c.id))
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

async function handleBatchFreeze() {
  if (selectedCards.value.length === 0) {
    ElMessage.warning('请先选择要冻结的卡密')
    return
  }
  try {
    await freezeCards(selectedCards.value.map((c) => c.id))
    ElMessage.success('冻结成功')
    fetchList()
  } catch (error) {
    ElMessage.error('冻结失败')
    console.error(error)
  }
}

async function handleBatchUnfreeze() {
  if (selectedCards.value.length === 0) {
    ElMessage.warning('请先选择要解冻的卡密')
    return
  }
  try {
    await unfreezeCards(selectedCards.value.map((c) => c.id))
    ElMessage.success('解冻成功')
    fetchList()
  } catch (error) {
    ElMessage.error('解冻失败')
    console.error(error)
  }
}

function getStatusType(status: number) {
  const map: Record<number, string> = {
    0: 'info',
    1: 'success',
    2: 'danger',
  }
  return map[status] || 'info'
}

function getStatusLabel(status: number) {
  const map: Record<number, string> = {
    0: '未使用',
    1: '已使用',
    2: '已冻结',
  }
  return map[status] || '未知'
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="card-manage-container">
    <!-- 生成卡密表单 -->
    <el-card shadow="hover" style="margin-bottom: 16px;">
      <template #header>
        <span style="font-weight: bold;">生成卡密</span>
      </template>
      <el-form :model="generateForm" inline>
        <el-form-item label="数量">
          <el-input-number v-model="generateForm.count" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="时长">
          <el-input-number v-model="generateForm.duration" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="generateForm.type" style="width: 120px;">
            <el-option label="天" value="day" />
            <el-option label="月" value="month" />
            <el-option label="年" value="year" />
            <el-option label="永久" value="permanent" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="generateForm.remark" placeholder="可选" style="width: 180px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="generating" @click="handleGenerate">
            <el-icon style="margin-right: 4px;"><Plus /></el-icon>
            生成
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 卡密列表 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold;">卡密列表</span>
          <div class="header-actions">
            <el-select
              v-model="statusFilter"
              placeholder="状态筛选"
              clearable
              style="width: 120px; margin-right: 8px;"
              @change="handleStatusChange"
            >
              <el-option label="未使用" :value="0" />
              <el-option label="已使用" :value="1" />
              <el-option label="已冻结" :value="2" />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索卡密"
              clearable
              style="width: 200px; margin-right: 8px;"
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

      <!-- 批量操作 -->
      <div style="margin-bottom: 12px;">
        <el-button type="danger" :disabled="selectedCards.length === 0" @click="handleBatchDelete">
          批量删除
        </el-button>
        <el-button type="warning" :disabled="selectedCards.length === 0" @click="handleBatchFreeze">
          批量冻结
        </el-button>
        <el-button type="success" :disabled="selectedCards.length === 0" @click="handleBatchUnfreeze">
          批量解冻
        </el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%;"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="cardKey" label="卡密" min-width="240">
          <template #default="{ row }">
            <el-text style="font-family: monospace;" copyable>{{ row.cardKey }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="80" />
        <el-table-column prop="duration" label="时长" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usedBy" label="使用者" width="120" show-overflow-tooltip />
        <el-table-column prop="usedAt" label="使用时间" width="180" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
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
.card-manage-container {
  max-width: 1400px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.header-actions {
  display: flex;
  align-items: center;
}
</style>
