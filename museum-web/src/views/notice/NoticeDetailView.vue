<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ElCard,
  ElTag,
  ElButton,
  ElSkeleton,
  ElMessage,
  ElMessageBox
} from 'element-plus'
import { ArrowLeft, Bell, Delete } from '@element-plus/icons-vue'
import { getNoticeDetail, deleteNotice, NoticeCategory, ReadFlag, type UserNoticeVO } from '@/api/notice'
import AppHeader from '@/components/common/AppHeader.vue'

const router = useRouter()
const route = useRoute()

// 消息详情数据
const notice = ref<UserNoticeVO | null>(null)
const loading = ref(false)

// 获取分类标签类型
const getCategoryType = (category: NoticeCategory) => {
  const typeMap: Record<NoticeCategory, any> = {
    [NoticeCategory.BOOKING]: 'warning',
    [NoticeCategory.ANNOUNCEMENT]: 'success',
    [NoticeCategory.ACTIVITY]: 'primary',
    [NoticeCategory.LECTURE]: 'danger',
    [NoticeCategory.SYSTEM]: 'info'
  }
  return typeMap[category] || 'info'
}

// 获取分类名称
const getCategoryName = (category: NoticeCategory) => {
  const nameMap: Record<NoticeCategory, string> = {
    [NoticeCategory.BOOKING]: '预约',
    [NoticeCategory.ANNOUNCEMENT]: '公告',
    [NoticeCategory.ACTIVITY]: '活动',
    [NoticeCategory.LECTURE]: '讲座',
    [NoticeCategory.SYSTEM]: '系统'
  }
  return nameMap[category] || '未知'
}

// 获取阅读状态名称
const getReadStatusText = (readFlag: ReadFlag) => {
  return readFlag === ReadFlag.READ ? '已读' : '未读'
}

// 格式化完整时间
const formatFullTime = (time: string | undefined) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 加载消息详情
const loadNoticeDetail = async () => {
  try {
    loading.value = true
    const id = Number(route.params.id)
    const data = await getNoticeDetail(id)
    notice.value = data
  } catch (error: any) {
    ElMessage.error(error.message || '加载消息详情失败')
    // 加载失败，返回列表页
    setTimeout(() => {
      router.push({ name: 'NoticeList' })
    }, 1500)
  } finally {
    loading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push({ name: 'NoticeList' })
}

// 删除消息
const handleDelete = async () => {
  if (!notice.value) return

  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？删除后将无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteNotice(notice.value.id)
    ElMessage.success('删除成功')
    router.push({ name: 'NoticeList' })
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadNoticeDetail()
})
</script>

<template>
  <div class="notice-page">
    <!-- 导航栏 -->
    <AppHeader />

    <div class="notice-detail-container">
      <el-card class="notice-card" shadow="never">
      <!-- 骨架屏 -->
      <div v-if="loading" class="skeleton-container">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- 消息详情 -->
      <div v-else-if="notice" class="notice-detail">
        <!-- 头部操作栏 -->
        <div class="detail-header">
          <el-button :icon="ArrowLeft" @click="goBack">
            返回列表
          </el-button>
          <el-button
            type="danger"
            :icon="Delete"
            @click="handleDelete"
          >
            删除消息
          </el-button>
        </div>

        <!-- 消息标题 -->
        <div class="detail-title">
          <h1>{{ notice.title }}</h1>
        </div>

        <!-- 消息元信息 -->
        <div class="detail-meta">
          <div class="meta-left">
            <el-tag :type="getCategoryType(notice.category)">
              {{ getCategoryName(notice.category) }}
            </el-tag>
            <el-tag :type="notice.readFlag === ReadFlag.READ ? 'info' : 'danger'">
              {{ getReadStatusText(notice.readFlag) }}
            </el-tag>
          </div>
          <div class="meta-right">
            <div class="meta-item">
              <span class="label">发送时间：</span>
              <span class="value">{{ formatFullTime(notice.createdAt) }}</span>
            </div>
            <div v-if="notice.readFlag === ReadFlag.READ" class="meta-item">
              <span class="label">阅读时间：</span>
              <span class="value">{{ formatFullTime(notice.readTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 分割线 -->
        <el-divider />

        <!-- 消息内容 -->
        <div class="detail-content">
          <div class="content-wrapper">
            {{ notice.content }}
          </div>
        </div>
      </div>

      <!-- 加载失败 -->
      <div v-else class="error-container">
        <el-empty description="消息不存在或已被删除" />
        <el-button type="primary" @click="goBack">
          返回列表
        </el-button>
      </div>
    </el-card>
    </div>
  </div>
</template>

<style scoped>
.notice-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.notice-detail-container {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 20px;
  flex: 1;
}

.notice-card {
  border-radius: 8px;
}

.skeleton-container {
  padding: 20px;
}

.notice-detail {
  padding: 20px 40px 40px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.detail-title {
  margin-bottom: 24px;
}

.detail-title h1 {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.meta-item .label {
  color: #909399;
}

.meta-item .value {
  color: #606266;
  font-weight: 500;
}

.detail-content {
  margin-top: 24px;
}

.content-wrapper {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
  word-wrap: break-word;
  padding: 24px;
  background-color: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  min-height: 200px;
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .notice-detail-container {
    margin: 20px auto;
    padding: 0 16px;
  }

  .notice-detail {
    padding: 20px;
  }

  .detail-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .detail-title h1 {
    font-size: 22px;
  }

  .detail-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .meta-right {
    align-items: flex-start;
    width: 100%;
  }

  .content-wrapper {
    padding: 16px;
    font-size: 15px;
  }
}
</style>
