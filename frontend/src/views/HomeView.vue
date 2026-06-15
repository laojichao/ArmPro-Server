<script setup lang="ts">
</script>

<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>欢迎使用 ArmPro Server 管理系统</span>
        </div>
      </template>
      <p>这是一个基于 Vue 3 + TypeScript + Element Plus 的后台管理系统。</p>
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>用户管理</template>
            <p>管理系统用户和权限</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>数据统计</template>
            <p>查看系统运行数据</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>系统设置</template>
            <p>配置系统参数</p>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<style scoped>
.home-container {
  padding: 20px;
}
.welcome-card {
  max-width: 1000px;
  margin: 0 auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
</style>
