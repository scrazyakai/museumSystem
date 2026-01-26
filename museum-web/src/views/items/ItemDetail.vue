<template>
  <div class="detail-page">
    <!-- Header -->
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="goToHome">博物馆</div>
        <div class="header-nav">
          <el-button type="text" @click="goToHome">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-button>
          <el-button type="text" @click="goToList">
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
    <div class="item-detail" v-loading="loading">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button :icon="ArrowLeft" @click="handleBack">返回列表</el-button>
    </div>

    <!-- 展品详情 -->
    <el-card v-if="item" class="detail-card">
      <!-- 媒体展示 -->
      <div class="media-section">
        <!-- 图片 -->
        <el-image
          v-if="item.mediaKind === 'IMAGE'"
          :src="item.mediaUrl"
          fit="contain"
          class="detail-media"
        >
          <template #error>
            <div class="image-error">
              <el-icon><Picture /></el-icon>
              <span>图片加载失败</span>
            </div>
          </template>
        </el-image>

        <!-- 视频 -->
        <video
          v-else
          :src="item.mediaUrl"
          :poster="item.coverUrl || ''"
          controls
          class="detail-media"
        />
      </div>

      <!-- 展品信息 -->
      <div class="info-section">
        <h1 class="detail-title">{{ item.title }}</h1>

        <el-descriptions :column="1" border class="detail-descriptions">
          <el-descriptions-item label="媒体类型">
            <el-tag :type="item.mediaKind === 'IMAGE' ? 'success' : 'warning'">
              {{ item.mediaKind === 'IMAGE' ? '图片' : '视频' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="item.description" label="展品描述">
            <div class="rich-text-content" v-html="item.description"></div>
          </el-descriptions-item>
          <el-descriptions-item label="展示时间">
            {{ displayTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 评论区 -->
    <el-card class="comment-card">
      <template #header>
        <h2>评论</h2>
      </template>
      <CommentPanel :item-id="itemId" />
    </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Picture, HomeFilled, Collection, SwitchButton, UserFilled } from '@element-plus/icons-vue'
import { getItemDetail, type ExhibitItem } from '@/api/items'
import { logout } from '@/api/auth'
import { useAuthStore } from '@/store/auth'
import CommentPanel from '@/components/CommentPanel.vue'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 用户信息
const userInfo = computed(() => authStore.userInfo)

const itemId = computed(() => Number(route.params.id))
const item = ref<ExhibitItem | null>(null)
const loading = ref(false)

// 展示时间
const displayTime = computed(() => {
  if (!item.value) return ''

  const { startTime, endTime } = item.value
  if (!startTime && !endTime) return '长期展出'

  const start = startTime ? dayjs(startTime).format('YYYY-MM-DD') : '不限'
  const end = endTime ? dayjs(endTime).format('YYYY-MM-DD') : '不限'

  return `${start} 至 ${end}`
})

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 加载展品详情
const loadItemDetail = async () => {
  loading.value = true
  try {
    item.value = await getItemDetail(itemId.value)
  } catch (error) {
    console.error('获取展品详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 返回列表
const handleBack = () => {
  router.push('/items')
}

// 返回首页
const goToHome = () => {
  router.push('/home')
}

// 返回展品列表
const goToList = () => {
  router.push('/items')
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
  loadItemDetail()
})
</script>

<style scoped>
.detail-page {
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
.item-detail {
  flex: 1;
  padding: 24px;
  max-width: 1440px;
  margin: 0 auto;
  width: 100%;
}

.back-button {
  margin-bottom: 20px;
}

.detail-card {
  margin-bottom: 24px;
}

.media-section {
  text-align: center;
  margin-bottom: 24px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  padding: 20px;
}

.detail-media {
  max-width: 50%;
  width: auto;
  height: auto;
  display: block;
  margin: 0 auto;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #909399;
}

.image-error .el-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.info-section {
  padding: 0 20px;
}

.detail-title {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 24px 0;
}

.detail-descriptions {
  margin-top: 16px;
}

.rich-text-content {
  line-height: 1.6;
}

.rich-text-content :deep(img) {
  max-width: 100%;
  height: auto;
}

.rich-text-content :deep(p) {
  margin: 8px 0;
}

.comment-card {
  margin-bottom: 24px;
}

.comment-card h2 {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
}
</style>
