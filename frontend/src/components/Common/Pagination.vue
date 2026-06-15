<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  total: number
  page?: number
  pageSize?: number
  pageSizes?: number[]
  layout?: string
  background?: boolean
}>(), {
  page: 1,
  pageSize: 10,
  pageSizes: () => [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true,
})

const emit = defineEmits<{
  (e: 'update:page', val: number): void
  (e: 'update:pageSize', val: number): void
  (e: 'change'): void
}>()

const currentPage = computed({
  get: () => props.page,
  set: (val: number) => {
    emit('update:page', val)
    emit('change')
  },
})

const currentPageSize = computed({
  get: () => props.pageSize,
  set: (val: number) => {
    emit('update:pageSize', val)
    // 切换每页条数时重置到第一页
    emit('update:page', 1)
    emit('change')
  },
})
</script>

<template>
  <div class="pagination-wrapper">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="currentPageSize"
      :total="total"
      :page-sizes="pageSizes"
      :layout="layout"
      :background="background"
    />
  </div>
</template>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}
</style>
