<template>
  <div class="item-detail-page">
    <!-- 导航栏 -->
    <AppHeader />

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
import { ElMessage } from 'element-plus'
import { ArrowLeft, Picture } from '@element-plus/icons-vue'
import { getItemDetail, type ExhibitItem } from '@/api/items'
import { useAuthStore } from '@/store/auth'
import CommentPanel from '@/components/CommentPanel.vue'
import AppHeader from '@/components/common/AppHeader.vue'
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

onMounted(() => {
  loadItemDetail()
})
</script>

<style scoped>
.item-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
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
