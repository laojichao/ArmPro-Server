import { ref, watch } from 'vue'

const isDark = ref(false)

// 初始化（在模块加载时同步执行，确保 SSR 安全）
if (typeof window !== 'undefined') {
  const saved = localStorage.getItem('theme')
  isDark.value = saved === 'dark' || (!saved && window.matchMedia('(prefers-color-scheme: dark)').matches)
  document.documentElement.classList.toggle('dark', isDark.value)
}

watch(isDark, (val) => {
  document.documentElement.classList.toggle('dark', val)
  localStorage.setItem('theme', val ? 'dark' : 'light')
})

export function useDarkMode() {
  function toggleDark() {
    isDark.value = !isDark.value
  }

  return {
    isDark,
    toggleDark,
  }
}
