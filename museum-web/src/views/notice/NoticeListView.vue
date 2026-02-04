<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElCard,
  ElTag,
  ElEmpty,
  ElBadge,
  ElPagination,
  ElMessage,
  ElMessageBox,
  ElTabs,
  ElTabPane,
  ElSwitch
} from 'element-plus'
import { Bell, ChatLineRound, Calendar, Promotion } from '@element-plus/icons-vue'
import { getMyNotices, deleteNotice, markAllAsRead, NoticeCategory, ReadFlag, type UserNoticeVO } from '@/api/notice'
import AppHeader from '@/components/common/AppHeader.vue'

const router = useRouter()

// 通知开关状态
const notificationEnabled = ref(false)

// 从 localStorage 加载通知设置
const loadNotificationSetting = () => {
  const saved = localStorage.getItem('notificationEnabled')
  notificationEnabled.value = saved === 'true'
}

// 保存通知设置到 localStorage
const saveNotificationSetting = (enabled: boolean) => {
  localStorage.setItem('notificationEnabled', enabled.toString())
  notificationEnabled.value = enabled
  if (enabled) {
    ElMessage.success('已开启消息通知')
  } else {
    ElMessage.info('已关闭消息通知')
  }
}

// 切换通知开关
const handleNotificationChange = (value: boolean) => {
  saveNotificationSetting(value)
}

// 消息列表数据
const noticeList = ref<UserNoticeVO[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 当前选中的分类（0表示全部）
const activeCategory = ref<number>(0)

// 未读消息数量
const unreadCount = computed(() => {
  return noticeList.value.filter(item => item.readFlag === ReadFlag.UNREAD).length
})

// 分类选项
const categoryOptions = [
  { label: '全部', value: 0, icon: Bell },
  { label: '预约', value: NoticeCategory.BOOKING, icon: Calendar },
  { label: '公告', value: NoticeCategory.ANNOUNCEMENT, icon: Bell },
  { label: '活动', value: NoticeCategory.ACTIVITY, icon: Promotion },
  { label: '讲座', value: NoticeCategory.LECTURE, icon: ChatLineRound },
  { label: '系统', value: NoticeCategory.SYSTEM, icon: Bell }
]

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

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes <= 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 加载消息列表
const loadNotices = async () => {
  try {
    loading.value = true
    const { records, total: totalCount } = await getMyNotices(
      currentPage.value,
      pageSize.value,
      activeCategory.value > 0 ? activeCategory.value : undefined,
      undefined
    )
    noticeList.value = records
    total.value = totalCount
  } catch (error: any) {
    ElMessage.error(error.message || '加载消息失败')
  } finally {
    loading.value = false
  }
}

// 切换分类
const handleCategoryChange = (category: number) => {
  activeCategory.value = category
  currentPage.value = 1
  loadNotices()
}

// 查看消息详情
const viewNotice = (notice: UserNoticeVO) => {
  router.push({
    name: 'NoticeDetail',
    params: { id: notice.id }
  })
}

// 删除消息
const handleDelete = async (notice: UserNoticeVO) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteNotice(notice.id)
    ElMessage.success('删除成功')
    loadNotices()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 分页改变
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadNotices()
}

// 全部标记为已读
const handleMarkAllRead = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要将所有消息标记为已读吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    await markAllAsRead()
    ElMessage.success('已将所有消息标记为已读')
    loadNotices()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadNotificationSetting()
  loadNotices()
})
</script>

<template>
  <div class="notice-page">
    <!-- 导航栏 -->
    <AppHeader />

    <div class="notice-list-container">
      <el-card class="notice-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="20"><Bell /></el-icon>
            <span class="title">消息通知</span>
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="badge" />
          </div>
          <div class="header-right">
            <div class="notification-toggle">
              <span class="toggle-label">允许通知</span>
              <el-switch
                v-model="notificationEnabled"
                @change="handleNotificationChange"
                active-text="开"
                inactive-text="关"
              />
            </div>
            <el-button
              v-if="unreadCount > 0"
              type="primary"
              size="small"
              @click="handleMarkAllRead"
            >
              全部已读
            </el-button>
          </div>
        </div>
      </template>

      <!-- 分类标签 -->
      <div class="category-tabs">
        <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
          <el-tab-pane
            v-for="item in categoryOptions"
            :key="item.value"
            :label="item.label"
            :name="item.value"
          >
            <template #label>
              <div class="tab-label">
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </div>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 消息列表 -->
      <div v-loading="loading" class="notice-list">
        <div v-if="noticeList.length === 0 && !loading" class="empty-container">
          <el-empty description="暂无消息" />
        </div>

        <div
          v-for="notice in noticeList"
          :key="notice.id"
          class="notice-item"
          :class="{ unread: notice.readFlag === ReadFlag.UNREAD }"
          @click="viewNotice(notice)"
        >
          <div class="notice-content">
            <div class="notice-header">
              <div class="header-left">
                <el-tag :type="getCategoryType(notice.category)" size="small">
                  {{ getCategoryName(notice.category) }}
                </el-tag>
                <span class="notice-title">{{ notice.title }}</span>
              </div>
              <div class="header-right">
                <span class="notice-time">{{ formatTime(notice.createdAt) }}</span>
                <el-badge
                  v-if="notice.readFlag === ReadFlag.UNREAD"
                  is-dot
                  class="unread-dot"
                />
              </div>
            </div>
            <div class="notice-preview">
              {{ notice.content.substring(0, 100) }}{{ notice.content.length > 100 ? '...' : '' }}
            </div>
          </div>

          <div class="notice-actions" @click.stop>
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDelete(notice)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="loadNotices"
        />
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

.notice-list-container {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
  flex: 1;
}

.notice-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-header .header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.toggle-label {
  font-size: 14px;
  color: #606266;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.badge {
  margin-left: 8px;
}

.category-tabs {
  margin-bottom: 20px;
}

.category-tabs :deep(.el-tabs__item) {
  width: 100px;
  text-align: center;
  padding: 0 20px;
}

.tab-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.notice-list {
  min-height: 400px;
}

.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fff;
}

.notice-item:hover {
  border-color: #b03128;
  box-shadow: 0 2px 8px rgba(176, 49, 40, 0.1);
}

.notice-item.unread {
  background-color: #fef0f0;
  border-left: 3px solid #b03128;
}

.notice-content {
  flex: 1;
  overflow: hidden;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notice-header .header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notice-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.notice-header .header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}

.unread-dot {
  margin-right: 8px;
}

.notice-preview {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-actions {
  margin-left: 16px;
  flex-shrink: 0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 响应式 */
@media (max-width: 768px) {
  .notice-list-container {
    margin: 20px auto;
    padding: 0 16px;
  }

  .notice-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .notice-actions {
    margin-left: 0;
    margin-top: 12px;
    width: 100%;
    display: flex;
    justify-content: flex-end;
  }

  .notice-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
