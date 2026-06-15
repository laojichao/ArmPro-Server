<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  getDashboardStats,
  getUserGrowthTrend,
  getAppUsageStats,
  getRecentLogins,
  type DashboardStats,
  type RecentLoginUser,
} from '@/api/admin'

const loading = ref(false)
const stats = ref<DashboardStats>({
  totalUsers: 0,
  totalApps: 0,
  todayTasks: 0,
  todayIncome: 0,
})
const recentLogins = ref<RecentLoginUser[]>([])

const userGrowthChart = ref<HTMLElement>()
const appUsageChart = ref<HTMLElement>()

let userGrowthInstance: echarts.ECharts | null = null
let appUsageInstance: echarts.ECharts | null = null

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

async function loadData() {
  loading.value = true
  try {
    const [statsRes, growthRes, usageRes, loginsRes] = await Promise.all([
      getDashboardStats(),
      getUserGrowthTrend(),
      getAppUsageStats(),
      getRecentLogins(),
    ])
    stats.value = statsRes
    recentLogins.value = loginsRes
    initUserGrowthChart(growthRes)
    initAppUsageChart(usageRes)
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function initUserGrowthChart(data: { date: string; count: number }[]) {
  if (!userGrowthChart.value) return
  userGrowthInstance = echarts.init(userGrowthChart.value)
  userGrowthInstance.setOption({
    title: { text: '用户增长趋势（近7天）', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.map((item) => item.date),
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        data: data.map((item) => item.count),
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' },
          ]),
        },
        itemStyle: { color: '#409eff' },
      },
    ],
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  })
}

function initAppUsageChart(data: { appName: string; userCount: number }[]) {
  if (!appUsageChart.value) return
  appUsageInstance = echarts.init(appUsageChart.value)
  appUsageInstance.setOption({
    title: { text: '应用使用统计', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.map((item) => item.appName),
      axisLabel: { rotate: 30 },
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '用户数',
        type: 'bar',
        data: data.map((item) => item.userCount),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#67c23a' },
            { offset: 1, color: '#95d475' },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
      },
    ],
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  })
}

function handleResize() {
  userGrowthInstance?.resize()
  appUsageInstance?.resize()
}
</script>

<template>
  <div class="dashboard-container" v-loading="loading">
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">用户总数</div>
              <div class="stat-value">{{ stats.totalUsers }}</div>
            </div>
            <el-icon class="stat-icon" style="color: #409eff"><User /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">应用总数</div>
              <div class="stat-value">{{ stats.totalApps }}</div>
            </div>
            <el-icon class="stat-icon" style="color: #67c23a"><Grid /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">今日任务</div>
              <div class="stat-value">{{ stats.todayTasks }}</div>
            </div>
            <el-icon class="stat-icon" style="color: #e6a23c"><List /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">今日收入</div>
              <div class="stat-value">&yen;{{ stats.todayIncome }}</div>
            </div>
            <el-icon class="stat-icon" style="color: #f56c6c"><Coin /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :lg="14">
        <el-card shadow="hover">
          <div ref="userGrowthChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="hover">
          <div ref="appUsageChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>
        <span style="font-weight: bold;">最近登录用户</span>
      </template>
      <el-table :data="recentLogins" stripe style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="loginTime" label="登录时间" />
        <el-table-column prop="ip" label="IP地址" />
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 0;
}
.stat-cards {
  margin-bottom: 0;
}
.stat-card .stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.stat-icon {
  font-size: 48px;
  opacity: 0.8;
}
</style>
