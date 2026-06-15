<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, shallowRef } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

const props = withDefaults(defineProps<{
  option: EChartsOption
  height?: string
  autoResize?: boolean
}>(), {
  height: '400px',
  autoResize: true,
})

const chartRef = ref<HTMLDivElement>()
const chartInstance = shallowRef<echarts.ECharts>()

function initChart() {
  if (!chartRef.value) return
  chartInstance.value = echarts.init(chartRef.value)
  chartInstance.value.setOption(props.option)
}

function handleResize() {
  chartInstance.value?.resize()
}

let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  initChart()

  if (props.autoResize && chartRef.value) {
    resizeObserver = new ResizeObserver(() => {
      handleResize()
    })
    resizeObserver.observe(chartRef.value)
  }
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  chartInstance.value?.dispose()
})

// 深度监听 option 变化，自动更新图表
watch(
  () => props.option,
  (newOption) => {
    if (chartInstance.value) {
      chartInstance.value.setOption(newOption, { notMerge: true })
    }
  },
  { deep: true }
)

// 暴露实例方法供外部调用
defineExpose({
  getInstance: () => chartInstance.value,
  resize: handleResize,
})
</script>

<template>
  <div ref="chartRef" :style="{ width: '100%', height }" />
</template>
