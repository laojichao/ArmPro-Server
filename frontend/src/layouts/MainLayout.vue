<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const isCollapse = ref(false)

const isAdminRoute = computed(() => {
  return route.path.startsWith('/admin')
})

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}
</script>

<template>
  <el-container style="height: 100vh">
    <el-aside :width="isCollapse ? '64px' : '200px'" style="background-color: #304156; transition: width 0.3s">
      <div style="height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 18px; font-weight: bold;">
        <span v-if="!isCollapse">{{ isAdminRoute ? 'ArmPro 管理后台' : 'ArmPro' }}</span>
        <span v-else>{{ isAdminRoute ? 'AP' : 'AP' }}</span>
      </div>
      <el-menu
        :default-active="$route.path"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <template v-if="isAdminRoute">
          <el-menu-item index="/admin/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>控制台</template>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/softs">
            <el-icon><Grid /></el-icon>
            <template #title>应用管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/cards">
            <el-icon><Ticket /></el-icon>
            <template #title>卡密管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/tasks">
            <el-icon><List /></el-icon>
            <template #title>任务管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/config">
            <el-icon><Setting /></el-icon>
            <template #title>系统配置</template>
          </el-menu-item>
          <el-menu-item index="/admin/notices">
            <el-icon><Bell /></el-icon>
            <template #title>公告管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/versions">
            <el-icon><Upload /></el-icon>
            <template #title>版本管理</template>
          </el-menu-item>
        </template>
        <template v-else>
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
        </template>
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
            <el-avatar :size="32" style="margin-right: 8px;">Admin</el-avatar>
            管理员
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="!isAdminRoute" @click="router.push('/admin')">管理后台</el-dropdown-item>
              <el-dropdown-item v-else @click="router.push('/home')">返回前台</el-dropdown-item>
              <el-dropdown-item divided @click="router.push('/login')">退出登录</el-dropdown-item>
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
