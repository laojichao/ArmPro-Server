<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const isCollapse = ref(false)

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleLogout() {
  localStorage.removeItem('token')
  router.push('/user/login')
}
</script>

<template>
  <el-container style="height: 100vh">
    <el-aside :width="isCollapse ? '64px' : '220px'" style="background-color: #1d1e2c; transition: width 0.3s">
      <div style="height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 18px; font-weight: bold;">
        <span v-if="!isCollapse">ArmPro 用户平台</span>
        <span v-else>AP</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        background-color="#1d1e2c"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/user/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>控制台</template>
        </el-menu-item>
        <el-menu-item index="/user/soft-list">
          <el-icon><Grid /></el-icon>
          <template #title>应用管理</template>
        </el-menu-item>
        <el-menu-item index="/user/card-manage">
          <el-icon><Ticket /></el-icon>
          <template #title>卡密管理</template>
        </el-menu-item>
        <el-menu-item index="/user/profile">
          <el-icon><User /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #eee; background: #fff;">
        <el-icon style="cursor: pointer; font-size: 20px;" @click="toggleCollapse">
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
        <el-dropdown>
          <span style="cursor: pointer; display: flex; align-items: center;">
            <el-avatar :size="32" style="margin-right: 8px;">U</el-avatar>
            用户
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/user/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main style="background: #f0f2f5; padding: 20px;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.el-menu {
  border-right: none;
}
</style>
