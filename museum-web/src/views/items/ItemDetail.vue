<template>
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

        <el-descriptions :column="2" border class="detail-descriptions">
          <el-descriptions-item label="媒体类型">
            <el-tag :type="item.mediaKind === 'IMAGE' ? 'success' : 'warning'">
              {{ item.mediaKind === 'IMAGE' ? '图片' : '视频' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="展示时间">
            {{ displayTime }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(item.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="item.description" label="展品描述" :span="2">
            {{ item.description }}
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
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Picture } from '@element-plus/icons-vue'
import { getItemDetail, type ExhibitItem } from '@/api/items'
import CommentPanel from '@/components/CommentPanel.vue'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

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
.item-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
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
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}

.detail-media {
  max-width: 100%;
  max-height: 600px;
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

.comment-card {
  margin-bottom: 24px;
}

.comment-card h2 {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
}
</style>
