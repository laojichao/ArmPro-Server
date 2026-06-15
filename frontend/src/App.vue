<script setup lang="ts">
// 暗黑模式初始化在 composables/useDarkMode.ts 模块加载时自动执行
// 子组件可通过 useDarkMode() 切换
</script>

<template>
  <router-view />
</template>

<style>
/* ========== 全局基础样式 ========== */
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

*,
*::before,
*::after {
  box-sizing: border-box;
}

/* ========== 滚动条美化 ========== */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

/* ========== 暗黑模式全局变量覆盖 ========== */
html.dark {
  color-scheme: dark;
}

html.dark body {
  background-color: #141414;
  color: #e5eaf3;
}

/* ========== 通用过渡 ========== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ========== 文本选择 ========== */
::selection {
  background-color: #409eff;
  color: #fff;
}
</style>
