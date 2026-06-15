<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDashboardStats, getUserInfo } from '@/api/user'

const router = useRouter()
const loading = ref(false)

const userInfo = ref({
  username: '',
  nickname: '',
  email: '',
  vipExpireTime: '',
})

const stats = ref({
  todayTotal: 0,
  todayUsed: 0,
  todayRemaining: 0,
  appCount: 0,
})

async function fetchData() {
  loading.value = true
  try {
    const [userRes, statsRes] = await Promise.all([
      getUserInfo(),
      getDashboardStats(),
    ])
    userInfo.value = userRes
    stats.value = statsRes
  } catch (error) {
    ElMessage.error('获取数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function goToSoftList() {
  router.push('/user/soft-list')
}

function goToRecharge() {
  router.push('/user/profile')
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="dashboard-container" v-loading="loading">
    <!-- 用户信息卡片 -->
    <el-card class="user-card" shadow="hover">
      <div class="user-info">
        <el-avatar :size="64" style="background: #409eff;">
          {{ userInfo.nickname ? userInfo.nickname[0] : 'U' }}
        </el-avatar>
        <div class="user-detail">
          <h3>欢迎回来，{{ userInfo.nickname || userInfo.username }}</h3>
          <p>邮箱：{{ userInfo.email || '未绑定' }}</p>
          <p>
            VIP到期时间：
            <el-tag v-if="userInfo.vipExpireTime" type="success">{{ userInfo.vipExpireTime }}</el-tag>
            <el-tag v-else type="info">未开通</el-tag>
          </p>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-value" style="color: #409eff;">{{ stats.todayTotal }}</div>
            <div class="stat-label">今日任务总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-value" style="color: #67c23a;">{{ stats.todayUsed }}</div>
            <div class="stat-label">今日已用</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-value" style="color: #e6a23c;">{{ stats.todayRemaining }}</div>
            <div class="stat-label">今日剩余</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-value" style="color: #f56c6c;">{{ stats.appCount }}</div>
            <div class="stat-label">应用数量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card style="margin-top: 20px;" shadow="hover">
      <template #header>
        <span style="font-weight: bold;">快捷操作</span>
      </template>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12">
          <el-button type="primary" size="large" style="width: 100%; margin-bottom: 12px;" @click="goToSoftList">
            <el-icon style="margin-right: 8px;"><Grid /></el-icon>
            查看应用
          </el-button>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-button type="success" size="large" style="width: 100%; margin-bottom: 12px;" @click="goToRecharge">
            <el-icon style="margin-right: 8px;"><Ticket /></el-icon>
            充值卡密
          </el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard-container {
  max-width: 1200px;
}
.user-card .user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}
.user-detail h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #303133;
}
.user-detail p {
  margin: 4px 0;
  color: #606266;
  font-size: 14px;
}
.stat-card {
  text-align: center;
  margin-bottom: 12px;
}
.stat-item {
  padding: 10px 0;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
  line-height: 1.2;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}
</style>
