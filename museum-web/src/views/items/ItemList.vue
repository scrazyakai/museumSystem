<template>
  <div class="items-page">
    <!-- Header -->
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="goToHome">博物馆</div>
        <div class="header-nav">
          <el-button type="text" @click="goToHome">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-button>
          <el-button type="text" class="active">
            <el-icon><Collection /></el-icon>
            <span>展品</span>
          </el-button>
        </div>
        <el-dropdown @command="handleCommand" class="user-dropdown">
          <div class="user-info">
            <el-avatar v-if="userInfo?.avatarUrl" :src="userInfo.avatarUrl" :size="32" />
            <el-avatar v-else :icon="UserFilled" :size="32" />
            <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 主内容 -->
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElButton, ElDropdown, ElDropdownMenu } from 'element-plus'
import { HomeFilled, Collection, SwitchButton, UserFilled } from '@element-plus/icons-vue'
import MasonryGrid from '@/components/MasonryGrid.vue'
import ItemCard from '@/components/ItemCard.vue'
import { getItems, type ExhibitItem, type ItemQueryParams, type MediaKind } from '@/api/items'
import { useAuthStore } from '@/store/auth'
import { logout } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

// 用户信息
const userInfo = computed(() => authStore.userInfo)

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

// 返回首页
const goToHome = () => {
  router.push('/home')
}

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  if (command === 'logout') {
    handleLogout()
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    try {
      await logout()
    } catch (error) {
      console.error('退出登录接口调用失败:', error)
    }

    authStore.clearUser()
    ElMessage.success('已退出登录')
    router.push('/login/user')
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.items-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

/* Header 样式 */
.header {
  background-color: #b03128;
  height: 60px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  color: white;
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  white-space: nowrap;
  cursor: pointer;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 40px;
}

.header-nav .el-button {
  color: rgba(255, 255, 255, 0.9);
  border: none;
  padding: 8px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.header-nav .el-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
}

.header-nav .el-button.active {
  background-color: rgba(255, 255, 255, 0.15);
  color: white;
}

.header-nav .el-button .el-icon {
  margin-right: 4px;
}

.user-dropdown {
  margin-left: auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.username {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.el-dropdown :deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 主内容 */
.item-list {
  flex: 1;
  padding: 24px;
  max-width: 1440px;
  margin: 0 auto;
  width: 100%;
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
