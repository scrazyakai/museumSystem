<template>
  <div class="masonry-grid" :style="{ columnCount: columns }">
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  minColumnWidth?: number
}>()

const columns = ref(4)

// 根据屏幕宽度计算列数
const updateColumns = () => {
  const width = window.innerWidth
  const minWidth = props.minColumnWidth || 280

  if (width < 768) {
    columns.value = 1
  } else if (width < 1024) {
    columns.value = 2
  } else if (width < 1280) {
    columns.value = 3
  } else {
    // 根据最小列宽动态计算
    const calculatedColumns = Math.floor(width / minWidth)
    columns.value = Math.max(3, Math.min(calculatedColumns, 6))
  }
}

onMounted(() => {
  updateColumns()
  window.addEventListener('resize', updateColumns)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateColumns)
})
</script>

<style scoped>
.masonry-grid {
  column-gap: 16px;
}

.masonry-grid :deep(> *) {
  break-inside: avoid;
  margin-bottom: 16px;
}
</style>
