<template>
  <div class="item-list">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-radio-group v-model="filterType" size="large" @change="handleFilterChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="IMAGE">图片</el-radio-button>
        <el-radio-button label="VIDEO">视频</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 瀑布流展示 -->
    <MasonryGrid v-loading="loading" class="masonry-container">
      <ItemCard
        v-for="item in items"
        :key="item.id"
        :item="item"
        @click="handleItemClick"
      />
    </MasonryGrid>

    <!-- 加载更多 -->
    <div v-if="hasMore" class="load-more">
      <el-button @click="loadMore" :loading="loadingMore">
        加载更多
      </el-button>
    </div>

    <!-- 没有更多数据 -->
    <div v-else-if="items.length > 0" class="no-more">
      没有更多数据了
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && items.length === 0" description="暂无展品" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MasonryGrid from '@/components/MasonryGrid.vue'
import ItemCard from '@/components/ItemCard.vue'
import { getItems, type ExhibitItem, type ItemQueryParams, type MediaKind } from '@/api/items'

const router = useRouter()

const items = ref<ExhibitItem[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const filterType = ref<MediaKind | ''>('')

const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 是否还有更多数据
const hasMore = computed(() => {
  return items.value.length < total.value
})

// 加载展品列表
const loadItems = async (isLoadMore = false) => {
  if (isLoadMore) {
    loadingMore.value = true
  } else {
    loading.value = true
  }

  try {
    const params: ItemQueryParams = {
      current: currentPage.value,
      size: pageSize.value
    }

    if (filterType.value) {
      params.mediaKind = filterType.value
    }

    const result = await getItems(params)

    if (isLoadMore) {
      items.value = [...items.value, ...result.records]
    } else {
      items.value = result.records
    }

    total.value = result.total
  } catch (error) {
    console.error('获取展品列表失败:', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 筛选类型改变
const handleFilterChange = () => {
  currentPage.value = 1
  loadItems()
}

// 加载更多
const loadMore = () => {
  currentPage.value++
  loadItems(true)
}

// 点击展品卡片
const handleItemClick = (item: ExhibitItem) => {
  router.push(`/items/${item.id}`)
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.item-list {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.filter-bar {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.masonry-container {
  min-height: 400px;
}

.load-more {
  text-align: center;
  margin: 24px 0;
}

.no-more {
  text-align: center;
  color: #909399;
  margin: 24px 0;
}
</style>
