<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getSoftDetail, getSoftStats, getFeatureToggles, updateFeatureToggle, type SoftInfo, type FeatureToggle } from '@/api/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const softId = Number(route.params.id)

const softInfo = ref<SoftInfo>({
  id: 0,
  name: '',
  packageName: '',
  version: '',
  appKey: '',
  status: 0,
  createdAt: '',
  updatedAt: '',
})

const features = ref<FeatureToggle[]>([])
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

async function fetchData() {
  loading.value = true
  try {
    const [detail, featuresRes, stats] = await Promise.all([
      getSoftDetail(softId),
      getFeatureToggles(softId),
      getSoftStats(softId, 6),
    ])
    softInfo.value = detail
    features.value = featuresRes
    renderChart(stats)
  } catch (error) {
    ElMessage.error('获取应用详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function renderChart(stats: { date: string; count: number }[]) {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption({
    title: { text: '近6天调用统计', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: stats.map((s) => s.date),
      axisLabel: { rotate: 30 },
    },
    yAxis: { type: 'value', name: '调用次数' },
    series: [
      {
        name: '调用次数',
        type: 'line',
        data: stats.map((s) => s.count),
        smooth: true,
        areaStyle: { opacity: 0.3 },
        itemStyle: { color: '#409eff' },
      },
    ],
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  })
}

async function handleToggleChange(feature: FeatureToggle) {
  try {
    await updateFeatureToggle(softId, feature.id, feature.enabled)
    ElMessage.success(`已${feature.enabled ? '开启' : '关闭'}「${feature.name}」`)
  } catch (error) {
    feature.enabled = !feature.enabled
    ElMessage.error('操作失败')
    console.error(error)
  }
}

function goBack() {
  router.push('/user/soft-list')
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', () => chartInstance?.resize())
})
</script>

<template>
  <div class="soft-detail-container" v-loading="loading">
    <!-- 顶部操作栏 -->
    <div style="margin-bottom: 16px;">
      <el-button @click="goBack">
        <el-icon style="margin-right: 4px;"><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <!-- 基本信息 -->
    <el-card shadow="hover" style="margin-bottom: 16px;">
      <template #header>
        <span style="font-weight: bold;">应用信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="应用名称">{{ softInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="包名">{{ softInfo.packageName }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ softInfo.version }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="softInfo.status === 1 ? 'success' : 'danger'">
            {{ softInfo.status === 1 ? '正常' : '已禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="AppKey" :span="2">
          <el-text type="primary" style="font-family: monospace;">{{ softInfo.appKey }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ softInfo.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ softInfo.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 功能开关 -->
    <el-card shadow="hover" style="margin-bottom: 16px;">
      <template #header>
        <span style="font-weight: bold;">功能开关</span>
      </template>
      <el-table :data="features" stripe>
        <el-table-column prop="name" label="功能名称" min-width="150" />
        <el-table-column prop="key" label="标识" min-width="200" />
        <el-table-column prop="description" label="说明" min-width="250" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleToggleChange(row)"
              active-text="开启"
              inactive-text="关闭"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 统计图表 -->
    <el-card shadow="hover">
      <template #header>
        <span style="font-weight: bold;">调用统计</span>
      </template>
      <div ref="chartRef" style="width: 100%; height: 360px;"></div>
    </el-card>
  </div>
</template>

<style scoped>
.soft-detail-container {
  max-width: 1200px;
}
</style>
